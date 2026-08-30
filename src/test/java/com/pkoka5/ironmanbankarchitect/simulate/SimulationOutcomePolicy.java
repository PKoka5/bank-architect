package com.pkoka5.ironmanbankarchitect.simulate;

import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Outcome;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.SimulationResult;

/** Shared pass/fail policy for random-bank verification entry points. */
final class SimulationOutcomePolicy
{
	private SimulationOutcomePolicy()
	{
	}

	static boolean isHardFailure(SimulationResult result)
	{
		return result.getOutcome() == Outcome.PLAN_BUILD_ERROR
			|| result.getOutcome() == Outcome.STALLED
			|| result.getOutcome() == Outcome.NON_TERMINATING
			|| result.getOutcome() == Outcome.ADVISOR_BLOCKED
			|| result.getOutcome() == Outcome.COMPLETED && !result.isFinalOrderVerified();
	}
}
