/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 * 
 * Based on the pseudo boolean algorithms described in:
 * A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
 * Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
 * Volume 24, Issue 3, March 2005 Page(s): 305 - 317
 * 
 * and 
 * Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
 * Framework. Ph.D. Dissertation, University of Oregon.
 *******************************************************************************/
package org.sat4j.pb.constraints;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.card.MinWatchCard;
import org.sat4j.minisat.constraints.cnf.Clauses;
import org.sat4j.minisat.constraints.cnf.LearntBinaryClause;
import org.sat4j.minisat.constraints.cnf.LearntWLClause;
import org.sat4j.minisat.constraints.cnf.OriginalBinaryClause;
import org.sat4j.minisat.constraints.cnf.OriginalWLClause;
import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class CompetResolutionPBMixedWLClauseCardConstrDataStructure extends
		PBMaxClauseCardConstrDataStructure {

	private static final long serialVersionUID = 1L;

	@Override
	public Constr createClause(IVecInt literals) throws ContradictionException {
		IVecInt v = Clauses.sanityCheck(literals, getVocabulary(), solver);
		if (v == null)
			return null;
		if (v.size() == 2) {
			return OriginalBinaryClause.brandNewClause(solver, getVocabulary(),
					v);
		}
		return OriginalWLClause.brandNewClause(solver, getVocabulary(), v);
	}

	@Override
	protected Constr constructClause(IVecInt v) {
		if (v == null)
			return null;
		if (v.size() == 2) {
			return OriginalBinaryClause.brandNewClause(solver, getVocabulary(),
					v);
		}
		return OriginalWLClause.brandNewClause(solver, getVocabulary(), v);
	}

	@Override
	protected Constr constructLearntClause(IVecInt resLits) {
		if (resLits.size() == 1) {
			return new UnitClause(resLits.last());
		}
		if (resLits.size() == 2) {
			return new LearntBinaryClause(resLits, getVocabulary());
		}
		return new LearntWLClause(resLits, getVocabulary());
	}

	@Override
	protected Constr constructCard(IVecInt theLits, int degree)
			throws ContradictionException {
		return MinWatchCard.minWatchCardNew(solver, getVocabulary(), theLits,
				MinWatchCard.ATLEAST, degree);
	}

	@Override
	protected Constr constructLearntCard(IDataStructurePB dspb) {
		IVecInt resLits = new VecInt();
		IVec<BigInteger> resCoefs = new Vec<BigInteger>();
		dspb.buildConstraintFromConflict(resLits, resCoefs);
		return new MinWatchCard(getVocabulary(), resLits, true, dspb
				.getDegree().intValue());
	}

}