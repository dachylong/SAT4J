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

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.RandomAccessModel;
import org.sat4j.specs.Lbool;

/**
 * Class allowing to express the search as a tree in the dot language. The
 * resulting file can be viewed in a tool like <a
 * href="http://www.graphviz.org/">Graphviz</a>
 * 
 * To use only on small benchmarks.
 * 
 * Note that also does not make sense to use such a listener on a distributed or
 * remote solver.
 * 
 * @author daniel
 * @since 2.2
 */
public class DotSearchTracing<T> extends SearchListenerAdapter<ISolverService> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Vec<String> pile;

    private String currentNodeName = null;

    private transient Writer out;

    private boolean estOrange = false;

    private final Map<Integer, T> mapping;

    /**
     * @since 2.1
     */
    public DotSearchTracing(final String fileNameToSave, Map<Integer, T> mapping) {
        this.pile = new Vec<String>();
        this.mapping = mapping;
        try {
            this.out = new FileWriter(fileNameToSave);
        } catch (IOException e) {
            System.err.println("Problem when created file.");
        }
    }

    private String node(int dimacs) {
        if (this.mapping != null) {
            int var = Math.abs(dimacs);
            T t = this.mapping.get(var);
            if (t != null) {
                if (dimacs > 0) {
                    return t.toString();
                }
                return "-" + t.toString();
            }
        }
        return Integer.toString(dimacs);
    }

    @Override
    public final void assuming(final int p) {
        final int absP = Math.abs(p);
        String newName;

        if (this.currentNodeName == null) {
            newName = "" + absP;
            this.pile.push(newName);
            saveLine(lineTab("\"" + newName + "\"" + "[label=\"" + node(p)
                    + "\", shape=circle, color=blue, style=filled]"));
        } else {
            newName = this.currentNodeName;
            this.pile.push(newName);
            saveLine(lineTab("\"" + newName + "\"" + "[label=\"" + node(p)
                    + "\", shape=circle, color=blue, style=filled]"));
        }
        this.currentNodeName = newName;
    }

    /**
     * @since 2.1
     */
    @Override
    public final void propagating(final int p, IConstr reason) {
        String newName = this.currentNodeName + "." + p;

        if (this.currentNodeName == null) {
            saveLine(lineTab("\"null\" [label=\"\", shape=point]"));
        }
        final String couleur = this.estOrange ? "orange" : "green";

        saveLine(lineTab("\"" + newName + "\"" + "[label=\"" + node(p)
                + "\",shape=point, color=black]"));
        saveLine(lineTab("\"" + this.currentNodeName + "\"" + " -- " + "\""
                + newName + "\"" + "[label=" + "\" " + node(p)
                + "\", fontcolor =" + couleur + ", color = " + couleur
                + ", style = bold]"));
        this.currentNodeName = newName;
        this.estOrange = false;
    }

    @Override
    public final void backtracking(final int p) {
        final String temp = this.pile.last();
        this.pile.pop();
        saveLine("\"" + temp + "\"" + "--" + "\"" + this.currentNodeName + "\""
                + "[label=\"\", color=red, style=dotted]");
        this.currentNodeName = temp;
    }

    @Override
    public final void adding(final int p) {
        this.estOrange = true;
    }

    /**
     * @since 2.1
     */
    @Override
    public final void learn(final IConstr clause) {
    }

    @Override
    public final void delete(final IConstr c) {
    }

    /**
     * @since 2.1
     */
    @Override
    public final void conflictFound(IConstr confl, int dlevel, int trailLevel) {
        saveLine(lineTab("\"" + this.currentNodeName
                + "\" [label=\"\", shape=box, color=\"red\", style=filled]"));
    }

    /**
     * @since 2.1
     */
    @Override
    public final void conflictFound(int p) {
        saveLine(lineTab("\"" + this.currentNodeName
                + "\" [label=\"\", shape=box, color=\"red\", style=filled]"));
    }

    @Override
    public final void solutionFound(int[] model, RandomAccessModel lazyModel) {
        saveLine(lineTab("\"" + this.currentNodeName
                + "\" [label=\"\", shape=box, color=\"green\", style=filled]"));
    }

    @Override
    public final void beginLoop() {
    }

    @Override
    public final void start() {
        saveLine("graph G {");
    }

    /**
     * @since 2.1
     */
    @Override
    public final void end(Lbool result) {
        saveLine("}");
    }

    private String lineTab(final String line) {
        return "\t" + line;
    }

    private void saveLine(final String line) {
        try {
            this.out.write(line + '\n');
            if ("}".equals(line)) {
                this.out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        // if the solver is serialized, out is linked to stdout
        stream.defaultReadObject();
        this.out = new PrintWriter(System.out);
    }
}
