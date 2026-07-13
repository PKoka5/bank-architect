package com.pkoka5.ironmanbankarchitect.organize;

public final class BankBlueprintTextExporter
{
	private static final int BANK_COLUMNS = 8;

	private BankBlueprintTextExporter()
	{
	}

	public static String export(BankOrganizationPreview preview)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Ironman Bank Architect blueprint export\n")
			.append("Preset: ").append(preview.getPreset().getName()).append('\n')
			.append("Columns: ").append(BANK_COLUMNS).append('\n');

		int tabNumber = 1;
		for (BankCategoryPreview category : preview.getCategories())
		{
			builder.append('\n')
				.append("TAB ").append(tabNumber)
				.append(" | placement=").append(tabNumber == 1 ? "MAIN" : "NUMBERED_CANDIDATE")
				.append(" | key=").append(category.getCategory().getKey())
				.append(" | name=").append(category.getCategory().getName())
				.append(" | items=").append(category.getItemCount())
				.append('\n');

			for (int slot = 0; slot < category.getItems().size(); slot++)
			{
				BankPreviewItem item = category.getItems().get(slot);
				int row = slot / BANK_COLUMNS + 1;
				int column = slot % BANK_COLUMNS + 1;
				builder.append("row=").append(row)
					.append(" col=").append(column)
					.append(" slot=").append(slot + 1);

				if (item.isBlank())
				{
					builder.append(" | EMPTY\n");
					continue;
				}

				builder.append(" | id=").append(item.getItemId())
					.append(" | name=").append(item.getDisplayName())
					.append(" | quantity=").append(item.getQuantity())
					.append(" | placeholder=").append(item.isPlaceholder())
					.append(" | catalogCategory=").append(item.getItemCategory())
					.append(" | subcategory=").append(item.getSubcategory())
					.append('\n');
			}

			tabNumber++;
		}

		return builder.toString();
	}
}
