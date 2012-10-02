/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.tools;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * This is a tool for computing all the MUSes (Minimum Unsatisfiable Sets) of a
 * set of clauses.
 * 
 * @author sroussel
 * @since 2.3.3
 */
public class AllMUSesGroup {

    private final GroupClauseSelectorSolver<ISolver> gcss;
    private final List<IVecInt> mssList;
    private final List<IVecInt> secondPhaseClauses;
    private final List<IVecInt> musList;

    public AllMUSesGroup() {
        this.gcss = new GroupClauseSelectorSolver<ISolver>(
                SolverFactory.newDefault());
        mssList = new ArrayList<IVecInt>();
        musList = new ArrayList<IVecInt>();
        secondPhaseClauses = new ArrayList<IVecInt>();
    }

    /**
     * Gets an instance of ISolver that can be used to compute all MUSes
     * 
     * @return the instance of ISolver to which the clauses will be added
     */
    public IGroupSolver getSolverInstance() {
        return this.gcss;
    }

    /**
     * Computes all the MUSes associated to the set of constraints added to the
     * solver
     * 
     * @param solver
     *            the <code>ISolver</code> that contains the set of clauses
     * @return a list containing all the MUSes
     */
    public static List<IVecInt> computeAllMUSes(ISolver solver) {
        List<IVecInt> muses = new ArrayList<IVecInt>();

        GroupClauseSelectorSolver<ISolver> gcss;

        int nVar = solver.nVars();

        if (!(solver instanceof GroupClauseSelectorSolver<?>)) {
            return muses;
        } else {
            gcss = (GroupClauseSelectorSolver<ISolver>) solver;
        }

        IVecInt pLits = new VecInt();
        for (Integer i : gcss.getAddedVars()) {
            pLits.push(i);
        }

        Minimal4InclusionModel min4Inc = new Minimal4InclusionModel(gcss, pLits);

        IVecInt hardClause;

        List<IVecInt> secondPhase = new ArrayList<IVecInt>();
        IVecInt secondPhaseClause;

        int clause;

        // first phase
        try {

            while (min4Inc.isSatisfiable()) {
                int[] fullmodel = min4Inc.modelWithInternalVariables();

                hardClause = new VecInt();
                secondPhaseClause = new VecInt();

                for (int i = 0; i < pLits.size(); i++) {
                    clause = Math.abs(pLits.get(i));
                    if (fullmodel[clause - 1] > 0) {
                        hardClause.push(-clause);
                        secondPhaseClause.push(clause);
                    }
                }

                secondPhase.add(secondPhaseClause);
                gcss.addNonControlableClause(hardClause);
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {

        }

        System.out.println("MSS = " + secondPhase);

        ISolver solver2 = SolverFactory.newDefault();

        IVecInt mus;

        try {
            for (IVecInt v : secondPhase) {
                solver2.addClause(v);
            }

            Minimal4InclusionModel solver2Phase = new Minimal4InclusionModel(
                    solver2, pLits);

            while (solver2Phase.isSatisfiable()) {
                hardClause = new VecInt();
                mus = new VecInt();

                int[] model = solver2Phase.model();

                for (int i = 0; i < model.length; i++) {
                    if (model[i] > 0) {
                        hardClause.push(-model[i]);
                        mus.push(model[i] - nVar);
                    }
                }

                muses.add(mus);

                solver2Phase.addClause(hardClause);

            }

        } catch (ContradictionException e) {
            // muses.add(e)
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        System.out.println("MUSes = " + muses + "\n");

        return muses;
    }
}
