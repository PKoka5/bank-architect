param(
	[string] $InputDir = "tools/research/community-templates/cache/local-imports",
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $MetadataPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-sort-metadata.tsv",
	[string] $OutputPath = "tools/research/community-templates/cache/local-imports/family-candidate-analysis.json",
	[int] $MinimumTemplateSupport = 3,
	[double] $MinimumAdjacencyConfidence = 0.6
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ($MinimumTemplateSupport -lt 2)
{
	throw "MinimumTemplateSupport must be at least 2."
}
if ($MinimumAdjacencyConfidence -lt 0 -or $MinimumAdjacencyConfidence -gt 1)
{
	throw "MinimumAdjacencyConfidence must be between 0 and 1."
}
foreach ($requiredPath in @($InputDir, $RegistryPath, $MetadataPath))
{
	if (!(Test-Path -LiteralPath $requiredPath))
	{
		throw "Required research input not found: $requiredPath"
	}
}

function New-IntSet
{
	return New-Object System.Collections.Generic.HashSet[int]
}

function New-StringSet
{
	return New-Object System.Collections.Generic.HashSet[string]
}

function Get-Median([double[]] $values)
{
	$sorted = @($values | Sort-Object)
	if ($sorted.Count -eq 0) { return 0 }
	$middle = [Math]::Floor($sorted.Count / 2)
	if ($sorted.Count % 2 -eq 1) { return $sorted[$middle] }
	return ($sorted[$middle - 1] + $sorted[$middle]) / 2
}

function Measure-Shape([object[]] $placements, [int] $columns)
{
	$positions = @{}
	foreach ($placement in $placements)
	{
		$positions[[int] $placement.AbsolutePosition] = $placement
	}
	$positionKeys = @($positions.Keys)
	if ($positionKeys.Count -eq 0)
	{
		throw "Cannot measure an empty placement set."
	}

	$minRow = ($placements.Row | Measure-Object -Minimum).Minimum
	$maxRow = ($placements.Row | Measure-Object -Maximum).Maximum
	$minColumn = ($placements.Column | Measure-Object -Minimum).Minimum
	$maxColumn = ($placements.Column | Measure-Object -Maximum).Maximum
	$width = [int] $maxColumn - [int] $minColumn + 1
	$height = [int] $maxRow - [int] $minRow + 1

	$unseen = New-IntSet
	foreach ($position in $positionKeys)
	{
		[void] $unseen.Add([int] $position)
	}
	$queue = New-Object System.Collections.Generic.Queue[int]
	$seed = [int] ($positionKeys | Sort-Object | Select-Object -First 1)
	$queue.Enqueue($seed)
	[void] $unseen.Remove($seed)
	$visited = 0
	while ($queue.Count -gt 0)
	{
		$position = [int] $queue.Dequeue()
		$visited++
		$column = $position % $columns
		$neighbors = @(([int] $position - $columns), ([int] $position + $columns))
		if ($column -gt 0) { $neighbors += ([int] $position - 1) }
		if ($column -lt $columns - 1) { $neighbors += ([int] $position + 1) }
		foreach ($neighbor in $neighbors)
		{
			if ($unseen.Remove([int] $neighbor))
			{
				$queue.Enqueue([int] $neighbor)
			}
		}
	}

	$connected = $visited -eq $positionKeys.Count
	$shape = if (!$connected)
	{
		"DISPERSED"
	}
	elseif ($height -eq 1)
	{
		"HORIZONTAL_RUN"
	}
	elseif ($width -eq 1)
	{
		"VERTICAL_RUN"
	}
	else
	{
		"BLOCK"
	}

	return [pscustomobject] @{
		Shape = $shape
		Width = $width
		Height = $height
		Density = [Math]::Round($positionKeys.Count / ($width * $height), 4)
	}
}

function Get-ValueDirection([int[]] $values)
{
	$ascending = $true
	$descending = $true
	for ($index = 1; $index -lt $values.Count; $index++)
	{
		if ($values[$index] -le $values[$index - 1]) { $ascending = $false }
		if ($values[$index] -ge $values[$index - 1]) { $descending = $false }
	}
	if ($ascending) { return "ASCENDING_VALUE" }
	if ($descending) { return "DESCENDING_VALUE" }
	return "MIXED_VALUE"
}

function Test-DirectContact([object[]] $firstPlacements, [object[]] $secondPlacements, [int] $columns)
{
	$secondPositions = New-IntSet
	foreach ($placement in $secondPlacements)
	{
		[void] $secondPositions.Add([int] $placement.AbsolutePosition)
	}
	foreach ($placement in $firstPlacements)
	{
		$position = [int] $placement.AbsolutePosition
		$column = $position % $columns
		$neighbors = @(([int] $position - $columns), ([int] $position + $columns))
		if ($column -gt 0) { $neighbors += ([int] $position - 1) }
		if ($column -lt $columns - 1) { $neighbors += ([int] $position + 1) }
		foreach ($neighbor in $neighbors)
		{
			if ($secondPositions.Contains([int] $neighbor)) { return $true }
		}
	}
	return $false
}

function Get-Sha256Text([string] $value)
{
	$sha256 = [System.Security.Cryptography.SHA256]::Create()
	try
	{
		$bytes = [System.Text.Encoding]::UTF8.GetBytes($value)
		return ([BitConverter]::ToString($sha256.ComputeHash($bytes))).Replace("-", "").ToLowerInvariant()
	}
	finally
	{
		$sha256.Dispose()
	}
}

function Convert-ObservationDistribution([object[]] $observations, [string] $propertyName, [string] $outputName)
{
	return @($observations | Group-Object -Property $propertyName |
		Sort-Object Name |
		ForEach-Object {
			$propertyValue = $_.Name
			if ($propertyName -in @("Width", "MaxSemanticRowLength", "DistinctFamilyStartColumns"))
			{
				$propertyValue = [int] $propertyValue
			}
			[pscustomobject] @{
				$outputName = $propertyValue
				Observations = $_.Count
				TemplateSupport = @($_.Group.TemplateId | Sort-Object -Unique).Count
			}
		})
}

$registry = @{}
Get-Content -LiteralPath $RegistryPath |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, Name, Category, ConstantName |
	ForEach-Object { $registry[[int] $_.ItemId] = $_ }

$metadata = @{}
$metadataByFamily = @{}
Get-Content -LiteralPath $MetadataPath |
	Where-Object { $_ -and !$_.StartsWith("#") } |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, Family, VariantKind, VariantValue, FoodRole, HealModel, ImmediateHealMin, ImmediateHealMax, SecondaryHeal, AreaRestriction, SourceKey |
	ForEach-Object {
		$itemId = [int] $_.ItemId
		$metadata[$itemId] = $_
		$family = [string] $_.Family
		if (!$metadataByFamily.ContainsKey($family))
		{
			$metadataByFamily[$family] = New-Object System.Collections.Generic.List[object]
		}
		$metadataByFamily[$family].Add($_)
	}

$files = @(Get-ChildItem -LiteralPath $InputDir -File -Filter "*.normalized.json" | Sort-Object Name)
if ($files.Count -eq 0)
{
	throw "No normalized template files found in: $InputDir"
}

$itemStats = @{}
$itemTabKeys = @{}
$pairStats = @{}
$tabRecords = New-Object System.Collections.Generic.List[object]
$layoutHashes = New-Object System.Collections.Generic.List[string]
$positivePlacementCount = 0
$tabCount = 0

function Add-ItemObservation([int] $itemId, [int] $templateId, [string] $tabKey)
{
	if (!$itemStats.ContainsKey($itemId))
	{
		$itemStats[$itemId] = [pscustomobject] @{
			ItemId = $itemId
			Occurrences = 0
			TemplateIds = New-IntSet
		}
		$itemTabKeys[$itemId] = New-StringSet
	}
	$itemStats[$itemId].Occurrences++
	[void] $itemStats[$itemId].TemplateIds.Add($templateId)
	[void] $itemTabKeys[$itemId].Add($tabKey)
}

function Add-AdjacentPair([int] $firstItemId, [int] $secondItemId, [string] $orientation, [int] $templateId)
{
	$lowerItemId = [Math]::Min($firstItemId, $secondItemId)
	$higherItemId = [Math]::Max($firstItemId, $secondItemId)
	$key = "$lowerItemId|$higherItemId"
	if (!$pairStats.ContainsKey($key))
	{
		$pairStats[$key] = [pscustomobject] @{
			LowerItemId = $lowerItemId
			HigherItemId = $higherItemId
			Occurrences = 0
			AdjacentTemplateIds = New-IntSet
			HorizontalTemplateIds = New-IntSet
			VerticalTemplateIds = New-IntSet
			HorizontalForwardTemplateIds = New-IntSet
			HorizontalReverseTemplateIds = New-IntSet
			VerticalForwardTemplateIds = New-IntSet
			VerticalReverseTemplateIds = New-IntSet
		}
	}

	$pair = $pairStats[$key]
	$pair.Occurrences++
	[void] $pair.AdjacentTemplateIds.Add($templateId)
	$direction = if ($firstItemId -eq $lowerItemId) { "Forward" } else { "Reverse" }
	$templateProperty = "${orientation}TemplateIds"
	$directionProperty = "${orientation}${direction}TemplateIds"
	[void] $pair.$templateProperty.Add($templateId)
	[void] $pair.$directionProperty.Add($templateId)
}

foreach ($file in $files)
{
	$document = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
	$templateId = [int] $document.TemplateId
	$layoutHashes.Add(([string] $document.LayoutSha256).ToLowerInvariant())
	$columns = [int] $document.Columns
	if ($columns -le 0)
	{
		throw "Template $templateId has invalid column count: $columns"
	}

	foreach ($tab in $document.Tabs)
	{
		$tabCount++
		$tabIndex = [int] $tab.TabIndex
		$tabKey = "${templateId}:$tabIndex"
		$allPlacements = @($document.Placements | Where-Object {
			[int] $_.TabIndex -eq $tabIndex
		})
		$placements = @($allPlacements | Where-Object State -eq "item")
		$positions = @{}
		foreach ($placement in $placements)
		{
			$itemId = [int] $placement.ItemId
			$position = [int] $placement.AbsolutePosition
			$positions[$position] = $itemId
			$positivePlacementCount++
			Add-ItemObservation $itemId $templateId $tabKey
		}

		foreach ($position in @($positions.Keys))
		{
			$firstItemId = [int] $positions[$position]
			$column = [int] $position % $columns
			if ($column -lt $columns - 1 -and $positions.ContainsKey([int] $position + 1))
			{
				Add-AdjacentPair $firstItemId ([int] $positions[[int] $position + 1]) "Horizontal" $templateId
			}
			if ($positions.ContainsKey([int] $position + $columns))
			{
				Add-AdjacentPair $firstItemId ([int] $positions[[int] $position + $columns]) "Vertical" $templateId
			}
		}

		$tabRecords.Add([pscustomobject] @{
			TemplateId = $templateId
			TabIndex = $tabIndex
			Columns = $columns
			Placements = $placements
			AllPlacements = $allPlacements
		})
	}
}

$recurringPairCount = 0
$eligibleEdges = New-Object System.Collections.Generic.List[object]
foreach ($pair in @($pairStats.Values | Sort-Object LowerItemId, HigherItemId))
{
	$adjacentTemplateSupport = $pair.AdjacentTemplateIds.Count
	if ($adjacentTemplateSupport -lt $MinimumTemplateSupport)
	{
		continue
	}
	$recurringPairCount++

	$lowerTabs = $itemTabKeys[$pair.LowerItemId]
	$higherTabs = $itemTabKeys[$pair.HigherItemId]
	$smallerTabs = if ($lowerTabs.Count -le $higherTabs.Count) { $lowerTabs } else { $higherTabs }
	$largerTabs = if ($lowerTabs.Count -le $higherTabs.Count) { $higherTabs } else { $lowerTabs }
	$coTabTemplateIds = New-IntSet
	foreach ($tabKey in $smallerTabs)
	{
		if ($largerTabs.Contains($tabKey))
		{
			[void] $coTabTemplateIds.Add([int] ($tabKey -split ":", 2)[0])
		}
	}
	if ($coTabTemplateIds.Count -eq 0)
	{
		throw "Recurring adjacent pair has no same-tab support: $($pair.LowerItemId)|$($pair.HigherItemId)"
	}

	$confidence = [Math]::Round($adjacentTemplateSupport / $coTabTemplateIds.Count, 4)
	if ($confidence -lt $MinimumAdjacencyConfidence)
	{
		continue
	}

	$horizontalSupport = $pair.HorizontalTemplateIds.Count
	$verticalSupport = $pair.VerticalTemplateIds.Count
	$dominantOrientation = if ($horizontalSupport -gt $verticalSupport)
	{
		"HORIZONTAL"
	}
	elseif ($verticalSupport -gt $horizontalSupport)
	{
		"VERTICAL"
	}
	else
	{
		"MIXED"
	}
	$orientationConsistency = [Math]::Round(
		[Math]::Max($horizontalSupport, $verticalSupport) / $adjacentTemplateSupport, 4)

	$forwardSupport = 0
	$reverseSupport = 0
	if ($dominantOrientation -eq "HORIZONTAL")
	{
		$forwardSupport = $pair.HorizontalForwardTemplateIds.Count
		$reverseSupport = $pair.HorizontalReverseTemplateIds.Count
	}
	elseif ($dominantOrientation -eq "VERTICAL")
	{
		$forwardSupport = $pair.VerticalForwardTemplateIds.Count
		$reverseSupport = $pair.VerticalReverseTemplateIds.Count
	}
	else
	{
		$forwardSupport = $pair.HorizontalForwardTemplateIds.Count + $pair.VerticalForwardTemplateIds.Count
		$reverseSupport = $pair.HorizontalReverseTemplateIds.Count + $pair.VerticalReverseTemplateIds.Count
	}
	$orderDenominator = $forwardSupport + $reverseSupport
	$orderConsistency = if ($orderDenominator -eq 0)
	{
		0
	}
	else
	{
		[Math]::Round([Math]::Max($forwardSupport, $reverseSupport) / $orderDenominator, 4)
	}

	$lowerRegistry = if ($registry.ContainsKey($pair.LowerItemId)) { $registry[$pair.LowerItemId] } else { $null }
	$higherRegistry = if ($registry.ContainsKey($pair.HigherItemId)) { $registry[$pair.HigherItemId] } else { $null }
	$eligibleEdges.Add([pscustomobject] @{
		LowerItemId = $pair.LowerItemId
		LowerName = if ($null -ne $lowerRegistry) { [string] $lowerRegistry.Name } else { "Unknown item $($pair.LowerItemId)" }
		LowerCategory = if ($null -ne $lowerRegistry) { [string] $lowerRegistry.Category } else { "UNREGISTERED" }
		HigherItemId = $pair.HigherItemId
		HigherName = if ($null -ne $higherRegistry) { [string] $higherRegistry.Name } else { "Unknown item $($pair.HigherItemId)" }
		HigherCategory = if ($null -ne $higherRegistry) { [string] $higherRegistry.Category } else { "UNREGISTERED" }
		Occurrences = $pair.Occurrences
		AdjacentTemplateSupport = $adjacentTemplateSupport
		CoTabTemplateSupport = $coTabTemplateIds.Count
		AdjacencyConfidence = $confidence
		DominantOrientation = $dominantOrientation
		OrientationConsistency = $orientationConsistency
		CanonicalOrder = if ($forwardSupport -ge $reverseSupport) { "LOWER_ID_FIRST" } else { "HIGHER_ID_FIRST" }
		OrderConsistency = $orderConsistency
		TemplateIds = $pair.AdjacentTemplateIds
	})
}

$graph = @{}
foreach ($edge in $eligibleEdges)
{
	foreach ($itemId in @([int] $edge.LowerItemId, [int] $edge.HigherItemId))
	{
		if (!$graph.ContainsKey($itemId))
		{
			$graph[$itemId] = New-IntSet
		}
	}
	[void] $graph[$edge.LowerItemId].Add([int] $edge.HigherItemId)
	[void] $graph[$edge.HigherItemId].Add([int] $edge.LowerItemId)
}

$components = New-Object System.Collections.Generic.List[object]
$seen = New-IntSet
foreach ($seed in @($graph.Keys | Sort-Object { [int] $_ }))
{
	$seed = [int] $seed
	if (!$seen.Add($seed))
	{
		continue
	}
	$queue = New-Object System.Collections.Generic.Queue[int]
	$queue.Enqueue($seed)
	$memberIds = New-IntSet
	while ($queue.Count -gt 0)
	{
		$itemId = [int] $queue.Dequeue()
		[void] $memberIds.Add($itemId)
		foreach ($neighbor in @($graph[$itemId] | Sort-Object))
		{
			if ($seen.Add([int] $neighbor))
			{
				$queue.Enqueue([int] $neighbor)
			}
		}
	}

	$componentEdges = @($eligibleEdges | Where-Object {
		$memberIds.Contains([int] $_.LowerItemId) -and $memberIds.Contains([int] $_.HigherItemId)
	})
	$templateIds = New-IntSet
	foreach ($edge in $componentEdges)
	{
		foreach ($templateId in $edge.TemplateIds)
		{
			[void] $templateIds.Add([int] $templateId)
		}
	}
	$shapeObservations = New-Object System.Collections.Generic.List[object]
	foreach ($tabRecord in $tabRecords)
	{
		$present = @($tabRecord.Placements | Where-Object {
			$memberIds.Contains([int] $_.ItemId)
		})
		$presentMemberCount = @($present | ForEach-Object { [int] $_.ItemId } |
			Sort-Object -Unique).Count
		if ($presentMemberCount -lt 2)
		{
			continue
		}
		$shape = Measure-Shape $present ([int] $tabRecord.Columns)
		$shapeObservations.Add([pscustomobject] @{
			TemplateId = [int] $tabRecord.TemplateId
			Shape = $shape.Shape
			Width = $shape.Width
			Density = $shape.Density
		})
	}
	$connectedObservations = @($shapeObservations | Where-Object Shape -ne "DISPERSED")
	$categoryDistribution = @($memberIds | ForEach-Object {
		if ($registry.ContainsKey([int] $_)) { [string] $registry[[int] $_].Category } else { "UNREGISTERED" }
	} | Group-Object | Sort-Object Name | ForEach-Object {
		[pscustomobject] @{ Category = $_.Name; Members = $_.Count }
	})
	$itemOutput = @($memberIds | Sort-Object | ForEach-Object {
		$itemId = [int] $_
		$registryItem = if ($registry.ContainsKey($itemId)) { $registry[$itemId] } else { $null }
		[pscustomobject] @{
			ItemId = $itemId
			Name = if ($null -ne $registryItem) { [string] $registryItem.Name } else { "Unknown item $itemId" }
			Category = if ($null -ne $registryItem) { [string] $registryItem.Category } else { "UNREGISTERED" }
			MetadataFamily = if ($metadata.ContainsKey($itemId)) { [string] $metadata[$itemId].Family } else { $null }
			Occurrences = $itemStats[$itemId].Occurrences
			TemplateSupport = $itemStats[$itemId].TemplateIds.Count
		}
	})
	$componentPlacementCount = ($itemOutput | Measure-Object Occurrences -Sum).Sum
	$components.Add([pscustomobject] @{
		CandidateKey = "adjacency.$seed"
		Members = $memberIds.Count
		Edges = $componentEdges.Count
		PlacementCount = $componentPlacementCount
		TemplateSupport = $templateIds.Count
		MetadataMembers = @($itemOutput | Where-Object { $null -ne $_.MetadataFamily }).Count
		MinimumEdgeConfidence = [Math]::Round((@($componentEdges | ForEach-Object {
			[double] $_.AdjacencyConfidence
		}) | Measure-Object -Minimum).Minimum, 4)
		MedianEdgeConfidence = [Math]::Round((Get-Median @($componentEdges | ForEach-Object {
			[double] $_.AdjacencyConfidence
		})), 4)
		CategoryDistribution = $categoryDistribution
		Items = $itemOutput
		ShapeDistribution = Convert-ObservationDistribution $shapeObservations "Shape" "Shape"
		ConnectedWidthDistribution = Convert-ObservationDistribution $connectedObservations "Width" "Width"
		ConnectedTemplateSupport = @($connectedObservations | ForEach-Object {
			[int] $_.TemplateId
		} | Sort-Object -Unique).Count
		MedianConnectedDensity = [Math]::Round((Get-Median @($connectedObservations | ForEach-Object {
			[double] $_.Density
		})), 4)
	})
}

$familySignals = New-Object System.Collections.Generic.List[object]
$familyAtoms = New-Object System.Collections.Generic.List[object]
foreach ($family in @($metadataByFamily.Keys | Sort-Object))
{
	$familyRows = @($metadataByFamily[$family] | ForEach-Object { $_ })
	$variantKinds = @($familyRows | ForEach-Object { [string] $_.VariantKind } | Sort-Object -Unique)
	if ($variantKinds.Count -ne 1 -or $variantKinds[0] -eq "NONE")
	{
		continue
	}
	$observedItemIds = @($familyRows | ForEach-Object { [int] $_.ItemId } | Where-Object {
		$itemStats.ContainsKey([int] $_)
	} | Sort-Object -Unique)
	if ($observedItemIds.Count -lt 2)
	{
		continue
	}

	$familyItemIds = New-IntSet
	foreach ($itemId in @($familyRows | ForEach-Object { [int] $_.ItemId }))
	{
		[void] $familyItemIds.Add([int] $itemId)
	}
	$namespace = ([string] $family -split "\.", 2)[0]
	$stageSignature = @($familyRows | ForEach-Object { [int] $_.VariantValue } |
		Sort-Object -Unique) -join ","
	$classKey = "$namespace|$($variantKinds[0])|$stageSignature"
	$observations = New-Object System.Collections.Generic.List[object]

	foreach ($templateTabs in @($tabRecords | Group-Object TemplateId | Sort-Object { [int] $_.Name }))
	{
		$tabsWithFamily = New-Object System.Collections.Generic.List[object]
		foreach ($tabRecord in @($templateTabs.Group | Sort-Object TabIndex))
		{
			$present = @($tabRecord.Placements | Where-Object {
				$familyItemIds.Contains([int] $_.ItemId)
			})
			if ($present.Count -gt 0)
			{
				$tabsWithFamily.Add([pscustomobject] @{
					TabRecord = $tabRecord
					Placements = $present
				})
			}
		}
		if ($tabsWithFamily.Count -eq 0)
		{
			continue
		}
		if ($tabsWithFamily.Count -gt 1)
		{
			$splitMemberCount = @($tabsWithFamily | ForEach-Object { $_.Placements } |
				ForEach-Object { [int] $_.ItemId } | Sort-Object -Unique).Count
			if ($splitMemberCount -ge 2)
			{
				$observations.Add([pscustomobject] @{
					TemplateId = [int] $templateTabs.Name
					Classification = "AMBIGUOUS_SPLIT_TABS"
					Complete = $splitMemberCount -eq $familyRows.Count
					Cohesive = $false
					Width = 0
					Density = 0
				})
			}
			continue
		}

		$familyTab = $tabsWithFamily[0]
		$tabRecord = $familyTab.TabRecord
		$present = @($familyTab.Placements | ForEach-Object { $_ })
		$memberIds = @($present | ForEach-Object { [int] $_.ItemId } | Sort-Object -Unique)
		if ($memberIds.Count -lt 2)
		{
			continue
		}
		$hasDuplicate = @($present | Group-Object ItemId | Where-Object Count -gt 1).Count -gt 0
		if ($hasDuplicate)
		{
			$observations.Add([pscustomobject] @{
				TemplateId = [int] $templateTabs.Name
				Classification = "AMBIGUOUS_DUPLICATE_ID"
				Complete = $memberIds.Count -eq $familyRows.Count
				Cohesive = $false
				Width = 0
				Density = 0
			})
			continue
		}

		$shape = Measure-Shape $present ([int] $tabRecord.Columns)
		$classification = "FRAGMENTED"
		if ($shape.Shape -eq "HORIZONTAL_RUN" -or $shape.Shape -eq "VERTICAL_RUN")
		{
			$orderedPlacements = if ($shape.Shape -eq "HORIZONTAL_RUN")
			{
				@($present | Sort-Object Column, AbsolutePosition)
			}
			else
			{
				@($present | Sort-Object Row, AbsolutePosition)
			}
			$values = @($orderedPlacements | ForEach-Object {
				[int] $metadata[[int] $_.ItemId].VariantValue
			})
			$valueDirection = Get-ValueDirection $values
			if ($valueDirection -ne "MIXED_VALUE")
			{
				$axis = if ($shape.Shape -eq "HORIZONTAL_RUN") { "HORIZONTAL" } else { "VERTICAL" }
				$classification = "${axis}_$valueDirection"
			}
			else
			{
				$classification = "COHESIVE_OTHER"
			}
		}
		elseif ($shape.Shape -eq "BLOCK")
		{
			$classification = "COHESIVE_OTHER"
		}
		$cohesive = $classification -ne "FRAGMENTED"
		$complete = $memberIds.Count -eq $familyRows.Count
		$observations.Add([pscustomobject] @{
			TemplateId = [int] $tabRecord.TemplateId
			Classification = $classification
			Complete = $complete
			Cohesive = $cohesive
			Width = $shape.Width
			Density = $shape.Density
		})
		if ($cohesive)
		{
			$familyAtoms.Add([pscustomobject] @{
				FamilyKey = $family
				ClassKey = $classKey
				TemplateId = [int] $tabRecord.TemplateId
				TabIndex = [int] $tabRecord.TabIndex
				Columns = [int] $tabRecord.Columns
				Placements = $present
				AllPlacements = $tabRecord.AllPlacements
				MemberCount = $memberIds.Count
				Complete = $complete
			})
		}
	}
	if ($observations.Count -eq 0)
	{
		continue
	}

	$cohesiveObservations = @($observations | Where-Object Cohesive)
	$placementCount = ($observedItemIds | ForEach-Object { $itemStats[[int] $_].Occurrences } |
		Measure-Object -Sum).Sum
	$familySignals.Add([pscustomobject] @{
		FamilyKey = $family
		FamilyClass = $classKey
		VariantKind = $variantKinds[0]
		CatalogMembers = $familyRows.Count
		ObservedMembers = $observedItemIds.Count
		PlacementCount = $placementCount
		EligibleObservations = $observations.Count
		TemplateSupport = @($observations | ForEach-Object { [int] $_.TemplateId } |
			Sort-Object -Unique).Count
		CompleteObservations = @($observations | Where-Object Complete).Count
		IncompleteObservations = @($observations | Where-Object { !$_.Complete }).Count
		AmbiguousSplitTemplates = @($observations | Where-Object Classification -eq "AMBIGUOUS_SPLIT_TABS").Count
		AmbiguousDuplicateTemplates = @($observations | Where-Object Classification -eq "AMBIGUOUS_DUPLICATE_ID").Count
		CohesiveTemplateSupport = @($cohesiveObservations | ForEach-Object { [int] $_.TemplateId } |
			Sort-Object -Unique).Count
		ClassificationDistribution = Convert-ObservationDistribution $observations "Classification" "Classification"
		CohesiveWidthDistribution = Convert-ObservationDistribution $cohesiveObservations "Width" "Width"
		MedianCohesiveDensity = [Math]::Round((Get-Median @($cohesiveObservations | ForEach-Object {
			[double] $_.Density
		})), 4)
	})
}

$eligibleClassTemplateIds = @{}
$blockObservations = New-Object System.Collections.Generic.List[object]
foreach ($atomGroup in @($familyAtoms | Group-Object {
	"$($_.TemplateId)|$($_.TabIndex)|$($_.ClassKey)"
} | Sort-Object Name))
{
	$atoms = @($atomGroup.Group | Sort-Object FamilyKey)
	$totalMembers = ($atoms | Measure-Object MemberCount -Sum).Sum
	if ($atoms.Count -lt 2 -or $totalMembers -lt 4)
	{
		continue
	}
	$classKey = [string] $atoms[0].ClassKey
	if (!$eligibleClassTemplateIds.ContainsKey($classKey))
	{
		$eligibleClassTemplateIds[$classKey] = New-IntSet
	}
	[void] $eligibleClassTemplateIds[$classKey].Add([int] $atoms[0].TemplateId)

	$atomGraph = @{}
	for ($index = 0; $index -lt $atoms.Count; $index++)
	{
		$atomGraph[$index] = New-IntSet
	}
	for ($leftIndex = 0; $leftIndex -lt $atoms.Count; $leftIndex++)
	{
		for ($rightIndex = $leftIndex + 1; $rightIndex -lt $atoms.Count; $rightIndex++)
		{
			if (Test-DirectContact $atoms[$leftIndex].Placements $atoms[$rightIndex].Placements ([int] $atoms[0].Columns))
			{
				[void] $atomGraph[$leftIndex].Add($rightIndex)
				[void] $atomGraph[$rightIndex].Add($leftIndex)
			}
		}
	}

	$seenAtoms = New-IntSet
	for ($seedIndex = 0; $seedIndex -lt $atoms.Count; $seedIndex++)
	{
		if ($atomGraph[$seedIndex].Count -eq 0 -or !$seenAtoms.Add($seedIndex))
		{
			continue
		}
		$queue = New-Object System.Collections.Generic.Queue[int]
		$queue.Enqueue($seedIndex)
		$componentIndexes = New-IntSet
		while ($queue.Count -gt 0)
		{
			$index = [int] $queue.Dequeue()
			[void] $componentIndexes.Add($index)
			foreach ($neighbor in @($atomGraph[$index] | Sort-Object))
			{
				if ($seenAtoms.Add([int] $neighbor))
				{
					$queue.Enqueue([int] $neighbor)
				}
			}
		}
		if ($componentIndexes.Count -lt 2)
		{
			continue
		}

		$componentAtoms = @($componentIndexes | Sort-Object | ForEach-Object { $atoms[[int] $_] })
		$semanticPlacements = @($componentAtoms | ForEach-Object { $_.Placements } |
			ForEach-Object { $_ } | Sort-Object AbsolutePosition, ItemId)
		if ($semanticPlacements.Count -lt 4)
		{
			continue
		}
		$shape = Measure-Shape $semanticPlacements ([int] $atoms[0].Columns)
		$minRow = ($semanticPlacements.Row | Measure-Object -Minimum).Minimum
		$maxRow = ($semanticPlacements.Row | Measure-Object -Maximum).Maximum
		$minColumn = ($semanticPlacements.Column | Measure-Object -Minimum).Minimum
		$maxColumn = ($semanticPlacements.Column | Measure-Object -Maximum).Maximum
		$semanticPositions = New-IntSet
		foreach ($placement in $semanticPlacements)
		{
			[void] $semanticPositions.Add([int] $placement.AbsolutePosition)
		}
		$insideBoundingBox = @($atoms[0].AllPlacements | Where-Object {
			[int] $_.Row -ge $minRow -and [int] $_.Row -le $maxRow -and
			[int] $_.Column -ge $minColumn -and [int] $_.Column -le $maxColumn
		})
		$foreignRealItems = @($insideBoundingBox | Where-Object {
			$_.State -eq "item" -and !$semanticPositions.Contains([int] $_.AbsolutePosition)
		}).Count
		$sentinels = @($insideBoundingBox | Where-Object State -eq "sentinel").Count
		$occupiedDenominator = $semanticPlacements.Count + $foreignRealItems
		$foreignSpilloverRatio = if ($occupiedDenominator -eq 0)
		{
			0
		}
		else
		{
			[Math]::Round($foreignRealItems / $occupiedDenominator, 4)
		}
		$rowGroups = @($semanticPlacements | Group-Object Row | Sort-Object { [int] $_.Name })
		$sameStartRows = @($rowGroups | Where-Object {
			($_.Group.Column | Measure-Object -Minimum).Minimum -eq $minColumn
		}).Count
		$startColumnConsistency = [Math]::Round($sameStartRows / $rowGroups.Count, 4)
		$maxSemanticRowLength = ($rowGroups | ForEach-Object { $_.Count } |
			Measure-Object -Maximum).Maximum
		$raggedFinalRow = $false
		if ($rowGroups.Count -gt 1)
		{
			$priorMaximum = ($rowGroups | Select-Object -First ($rowGroups.Count - 1) |
				ForEach-Object { $_.Count } | Measure-Object -Maximum).Maximum
			$raggedFinalRow = $rowGroups[-1].Count -lt $priorMaximum
		}
		$distinctFamilyStartColumns = @($componentAtoms | ForEach-Object {
			($_.Placements.Column | Measure-Object -Minimum).Minimum
		} | Sort-Object -Unique).Count

		$blockObservations.Add([pscustomobject] @{
			ClassKey = $classKey
			TemplateId = [int] $atoms[0].TemplateId
			FamilyKeys = @($componentAtoms | ForEach-Object { [string] $_.FamilyKey } | Sort-Object -Unique)
			Families = $componentAtoms.Count
			SemanticMembers = $semanticPlacements.Count
			Width = $shape.Width
			MaxSemanticRowLength = $maxSemanticRowLength
			DistinctFamilyStartColumns = $distinctFamilyStartColumns
			SemanticDensity = $shape.Density
			ForeignSpilloverRatio = $foreignSpilloverRatio
			StartColumnConsistency = $startColumnConsistency
			RaggedFinalRow = $raggedFinalRow
			SentinelFree = $sentinels -eq 0
		})
	}
}

$blockClassSignals = New-Object System.Collections.Generic.List[object]
foreach ($classGroup in @($blockObservations | Group-Object ClassKey | Sort-Object Name))
{
	$observations = @($classGroup.Group | ForEach-Object { $_ })
	$templateSupport = @($observations | ForEach-Object { [int] $_.TemplateId } |
		Sort-Object -Unique).Count
	if ($templateSupport -lt $MinimumTemplateSupport)
	{
		continue
	}
	$eligibleTemplates = $eligibleClassTemplateIds[[string] $classGroup.Name].Count
	$observedWidthDistribution = @($observations | Group-Object Width | Sort-Object { [int] $_.Name } |
		ForEach-Object {
			$widthTemplateSupport = @($_.Group | ForEach-Object { [int] $_.TemplateId } |
				Sort-Object -Unique).Count
			$familyKeys = @($_.Group | ForEach-Object { $_.FamilyKeys } |
				ForEach-Object { [string] $_ } | Sort-Object -Unique)
			[pscustomobject] @{
				Width = [int] $_.Name
				ComponentCount = $_.Count
				DistinctTemplateSupport = $widthTemplateSupport
				DistinctFamilySupport = $familyKeys.Count
				EligibleTemplateShare = [Math]::Round($widthTemplateSupport / $eligibleTemplates, 4)
			}
		})
	$widthDistribution = @($observedWidthDistribution |
		Where-Object DistinctTemplateSupport -ge $MinimumTemplateSupport)
	if ($widthDistribution.Count -eq 0)
	{
		continue
	}
	$completeWidthDistribution = @(1..8 | ForEach-Object {
		$width = [int] $_
		$observed = @($observedWidthDistribution | Where-Object Width -eq $width)
		if ($observed.Count -eq 1)
		{
			$observed[0]
		}
		else
		{
			[pscustomobject] @{
				Width = $width
				ComponentCount = 0
				DistinctTemplateSupport = 0
				DistinctFamilySupport = 0
				EligibleTemplateShare = 0
			}
		}
	})
	$rankedWidths = @($completeWidthDistribution | Sort-Object -Property `
		@{ Expression = "DistinctTemplateSupport"; Descending = $true }, `
		@{ Expression = "Width"; Ascending = $true })
	$topWidth = $rankedWidths[0]
	$runnerUpSupport = if ($rankedWidths.Count -gt 1) { $rankedWidths[1].DistinctTemplateSupport } else { 0 }
	$preferredWidth = $null
	if ($topWidth.DistinctTemplateSupport -ge 5 -and $topWidth.DistinctFamilySupport -ge 3 -and
		([long] $topWidth.DistinctTemplateSupport * 5) -ge ([long] $eligibleTemplates * 3) -and
		$topWidth.DistinctTemplateSupport - $runnerUpSupport -ge 2)
	{
		$preferredWidth = $topWidth.Width
	}
	$familySupport = @($observations | ForEach-Object { $_.FamilyKeys } |
		ForEach-Object { [string] $_ } | Sort-Object -Unique).Count
	$blockClassSignals.Add([pscustomobject] @{
		FamilyClass = [string] $classGroup.Name
		EligibleTemplates = $eligibleTemplates
		BlockObservations = $observations.Count
		DistinctTemplateSupport = $templateSupport
		DistinctFamilySupport = $familySupport
		PreferredWidth = $preferredWidth
		WidthDistribution = $widthDistribution
		CompleteWidthDistribution = $completeWidthDistribution
		MedianSemanticDensity = [Math]::Round((Get-Median @($observations | ForEach-Object {
			[double] $_.SemanticDensity
		})), 4)
		MedianForeignSpilloverRatio = [Math]::Round((Get-Median @($observations | ForEach-Object {
			[double] $_.ForeignSpilloverRatio
		})), 4)
		MostlySameStartComponents = @($observations | Where-Object StartColumnConsistency -ge 0.75).Count
		RaggedFinalRowComponents = @($observations | Where-Object RaggedFinalRow).Count
		SentinelFreeComponents = @($observations | Where-Object SentinelFree).Count
		MaxSemanticRowLengthDistribution = Convert-ObservationDistribution $observations "MaxSemanticRowLength" "Length"
		DistinctFamilyStartColumnDistribution = Convert-ObservationDistribution $observations "DistinctFamilyStartColumns" "Columns"
	})
}

$candidateDegree = @{}
foreach ($edge in $eligibleEdges)
{
	foreach ($itemId in @([int] $edge.LowerItemId, [int] $edge.HigherItemId))
	{
		if (!$candidateDegree.ContainsKey($itemId)) { $candidateDegree[$itemId] = 0 }
		$candidateDegree[$itemId]++
	}
}
$missingItems = @($itemStats.Values | Where-Object {
	!$metadata.ContainsKey([int] $_.ItemId)
} | ForEach-Object {
	$itemId = [int] $_.ItemId
	$registryItem = if ($registry.ContainsKey($itemId)) { $registry[$itemId] } else { $null }
	[pscustomobject] @{
		ItemId = $itemId
		Name = if ($null -ne $registryItem) { [string] $registryItem.Name } else { "Unknown item $itemId" }
		Category = if ($null -ne $registryItem) { [string] $registryItem.Category } else { "UNREGISTERED" }
		Occurrences = $_.Occurrences
		TemplateSupport = $_.TemplateIds.Count
		RecurringAdjacencyDegree = if ($candidateDegree.ContainsKey($itemId)) { $candidateDegree[$itemId] } else { 0 }
	}
} | Sort-Object -Property @{ Expression = "TemplateSupport"; Descending = $true },
	@{ Expression = "Occurrences"; Descending = $true }, ItemId)

$missingCategorySummary = @($missingItems | Group-Object Category | ForEach-Object {
	[pscustomobject] @{
		Category = $_.Name
		Placements = ($_.Group | Measure-Object Occurrences -Sum).Sum
		UniqueItemIds = $_.Count
		ItemsInRecurringAdjacency = @($_.Group | Where-Object RecurringAdjacencyDegree -gt 0).Count
	}
} | Sort-Object -Property @{ Expression = "Placements"; Descending = $true }, Category)

$publicEdges = @($eligibleEdges | ForEach-Object {
	[pscustomobject] @{
		LowerItemId = $_.LowerItemId
		LowerName = $_.LowerName
		LowerCategory = $_.LowerCategory
		HigherItemId = $_.HigherItemId
		HigherName = $_.HigherName
		HigherCategory = $_.HigherCategory
		Occurrences = $_.Occurrences
		AdjacentTemplateSupport = $_.AdjacentTemplateSupport
		CoTabTemplateSupport = $_.CoTabTemplateSupport
		AdjacencyConfidence = $_.AdjacencyConfidence
		DominantOrientation = $_.DominantOrientation
		OrientationConsistency = $_.OrientationConsistency
		CanonicalOrder = $_.CanonicalOrder
		OrderConsistency = $_.OrderConsistency
	}
} | Sort-Object -Property @{ Expression = "AdjacentTemplateSupport"; Descending = $true },
	@{ Expression = "AdjacencyConfidence"; Descending = $true }, LowerItemId, HigherItemId)

$metadataHash = (Get-FileHash -LiteralPath $MetadataPath -Algorithm SHA256).Hash.ToLowerInvariant()
$registryHash = (Get-FileHash -LiteralPath $RegistryPath -Algorithm SHA256).Hash.ToLowerInvariant()
$fingerprintMaterial = @(
	"analyzer-schema=3"
	"minimum-template-support=$MinimumTemplateSupport"
	"minimum-adjacency-confidence=$MinimumAdjacencyConfidence"
	"metadata=$metadataHash"
	"registry=$registryHash"
	@($layoutHashes | Sort-Object | ForEach-Object { "layout=$_" })
) -join "`n"
$cohortFingerprint = Get-Sha256Text $fingerprintMaterial

if (@($publicEdges | Where-Object {
	$_.AdjacentTemplateSupport -lt $MinimumTemplateSupport -or
	$_.AdjacencyConfidence -lt $MinimumAdjacencyConfidence
}).Count -gt 0)
{
	throw "Eligible recurring edge output violated its evidence thresholds."
}
if (@($blockClassSignals | ForEach-Object { $_.WidthDistribution } | Where-Object {
	$_.Width -lt 1 -or $_.Width -gt 8 -or $_.DistinctTemplateSupport -lt $MinimumTemplateSupport
}).Count -gt 0)
{
	throw "Curated block output contained an invalid or under-supported width."
}
if (@($blockClassSignals | Where-Object {
	$signal = $_
	$signal.CompleteWidthDistribution.Count -ne 8 -or
	(@($signal.CompleteWidthDistribution | ForEach-Object { [int] $_.Width }) -join ',') -ne
		'1,2,3,4,5,6,7,8' -or
	@($signal.CompleteWidthDistribution | Where-Object {
		$_.Width -lt 1 -or $_.Width -gt 8 -or $_.DistinctTemplateSupport -lt 0 -or
		$_.DistinctTemplateSupport -gt $signal.EligibleTemplates -or
		$_.DistinctFamilySupport -lt 0 -or
		(($_.DistinctTemplateSupport -eq 0) -ne ($_.DistinctFamilySupport -eq 0))
	}).Count -gt 0
}).Count -gt 0)
{
	throw "Curated block output contained an invalid complete width distribution."
}

$result = [ordered] @{
	SchemaVersion = 3
	Input = "git-ignored normalized local imports"
	Method = "exact-ID candidate discovery plus ordered curated-family and family-class block measurement"
	CohortFingerprint = $cohortFingerprint
	Thresholds = [ordered] @{
		MinimumTemplateSupport = $MinimumTemplateSupport
		MinimumAdjacencyConfidence = $MinimumAdjacencyConfidence
	}
	TemplateCount = $files.Count
	TabCount = $tabCount
	PositivePlacementCount = $positivePlacementCount
	MetadataPlacementCount = $positivePlacementCount - ($missingItems | Measure-Object Occurrences -Sum).Sum
	MissingMetadataPlacementCount = ($missingItems | Measure-Object Occurrences -Sum).Sum
	RecurringAdjacentPairsBeforeConfidenceFilter = $recurringPairCount
	EligibleRecurringEdges = $publicEdges.Count
	MissingMetadataByCategory = $missingCategorySummary
	MissingMetadataItems = $missingItems
	CuratedFamilySignals = @($familySignals | Sort-Object -Property `
		@{ Expression = "CohesiveTemplateSupport"; Descending = $true }, `
		@{ Expression = "FamilyKey"; Ascending = $true })
	CuratedBlockClassSignals = @($blockClassSignals | Sort-Object FamilyClass)
	AdjacencyFamilyCandidates = @($components | Sort-Object -Property `
		@{ Expression = "TemplateSupport"; Descending = $true }, `
		@{ Expression = "PlacementCount"; Descending = $true }, `
		@{ Expression = "CandidateKey"; Ascending = $true })
	RecurringEdges = $publicEdges
}

$outputDirectory = Split-Path -Parent $OutputPath
if ($outputDirectory)
{
	New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($OutputPath, ($result | ConvertTo-Json -Depth 10), $utf8)

Write-Host "Analyzed $($files.Count) templates, $tabCount tabs, and $positivePlacementCount placements."
Write-Host "Recurring pairs: $recurringPairCount before confidence filtering; $($publicEdges.Count) eligible edges."
Write-Host "Curated family signals: $($familySignals.Count); block classes: $($blockClassSignals.Count)."
Write-Host "Exploratory adjacency candidates: $($components.Count)."
Write-Host "Family candidate output: $OutputPath"
