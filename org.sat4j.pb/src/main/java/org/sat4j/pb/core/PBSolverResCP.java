package org.sat4j.pb.core;

import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;

public class PBSolverResCP extends PBSolverCP {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final long MAXCONFLICTS = 100000L;

	private long bound;

	public PBSolverResCP(LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, IOrder order) {
		this(learner, dsf, order, MAXCONFLICTS);
	}

	public PBSolverResCP(LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, IOrder order, long bound) {
		super(learner, dsf, order);
		this.bound = bound;
	}

	public PBSolverResCP(LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, SearchParams params, IOrder order,
			RestartStrategy restarter) {
		super(learner, dsf, params, order, restarter);
	}

	public PBSolverResCP(LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, SearchParams params, IOrder order) {
		super(learner, dsf, params, order);
		// TODO Auto-generated constructor stub
	}

	@Override
	boolean someCriteria() {
		if (stats.conflicts == bound) {
			this.setSimplifier(NO_SIMPLIFICATION);
			this.reduceDB();
			stats.numberOfCP++;
			return true;
		} else if (stats.conflicts > bound) {
			stats.numberOfCP++;
			return true;
		} else {
			stats.numberOfResolution++;
			return false;
		}
	}

}
