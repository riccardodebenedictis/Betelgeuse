/*
 * Copyright (C) 2017 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.graph;

import it.cnr.istc.smt.LBool;
import it.cnr.istc.solver.Flaw;
import it.cnr.istc.solver.Resolver;
import prefuse.Display;
import it.cnr.istc.solver.SolverListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.StrokeLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class CausalGraph extends Display implements SolverListener {

    private static final String GRAPH = "graph";
    private static final String NODES = "graph.nodes";
    private static final String EDGES = "graph.edges";
    private static final String NODE_TYPE = "node_type";
    private static final String NODE_COST = "node_cost";
    private static final String NODE_STATE = "node_state";
    private static final String NODE_CONTENT = "node_content";
    private static final String EDGE_STATE = "edge_state";
    private static final String EDGE_DECORATORS = "edgeDeco";
    private static final String NODE_DECORATORS = "nodeDeco";
    private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();

    static {
        DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
        DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 7));
    }
    private final Graph g = new Graph(true);
    private final VisualGraph vg;
    private final Map<Flaw, Node> flaws = new HashMap<>();
    private final Map<Resolver, Node> resolvers = new HashMap<>();
    private Flaw current_flaw;
    private Resolver current_resolver;

    public CausalGraph() {
        // initialize display and data
        super(new Visualization());

        Logger.getLogger("prefuse").setLevel(Level.OFF);

        g.getNodeTable().addColumn(VisualItem.LABEL, String.class);
        g.getNodeTable().addColumn(NODE_TYPE, String.class);
        g.getNodeTable().addColumn(NODE_COST, Double.class);
        g.getNodeTable().addColumn(NODE_STATE, Integer.class);
        g.getNodeTable().addColumn(NODE_CONTENT, Object.class);
        g.getEdgeTable().addColumn(VisualItem.LABEL, String.class);
        g.getEdgeTable().addColumn(EDGE_STATE, Integer.class);

        // add visual data groups
        vg = m_vis.addGraph(GRAPH, g);

        m_vis.setInteractive(EDGES, null, false);
        m_vis.setValue(NODES, null, VisualItem.SHAPE, Constants.SHAPE_ELLIPSE);

        // set up the renderers
        // draw the nodes as basic shapes
        LabelRenderer flaw_renderer = new LabelRenderer(VisualItem.LABEL);
        flaw_renderer.setRoundedCorner(3, 3);
        LabelRenderer resolver_renderer = new LabelRenderer(VisualItem.LABEL);
        resolver_renderer.setRoundedCorner(15, 15);

        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(flaw_renderer);
        drf.add("ingroup('" + NODES + "')&&" + NODE_TYPE + "==\"resolver\"", resolver_renderer);
        drf.setDefaultEdgeRenderer(new EdgeRenderer(Constants.EDGE_TYPE_CURVE));
        m_vis.setRendererFactory(drf);

        // adding decorators, one group for the nodes, one for the edges
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(50));
        m_vis.addDecorators(EDGE_DECORATORS, EDGES, DECORATOR_SCHEMA);

        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
        m_vis.addDecorators(NODE_DECORATORS, NODES, DECORATOR_SCHEMA);

        // set up the visual operators
        // first set up all the color actions
        ColorAction nFill = new DataColorAction(NODES, NODE_COST, Constants.ORDINAL, VisualItem.FILLCOLOR, ColorLib.getHotPalette());
        nFill.add(VisualItem.HOVER, ColorLib.gray(200));
        nFill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 230, 230));
        nFill.add(NODE_STATE + " == " + 0, ColorLib.gray(235));

        ColorAction nStrokeColor = new ColorAction(NODES, VisualItem.STROKECOLOR);
        nStrokeColor.setDefaultColor(ColorLib.gray(100));
        nStrokeColor.add(VisualItem.HOVER, ColorLib.gray(200));

        StrokeAction nStroke = new StrokeAction(NODES, StrokeLib.getStroke(5));
        nStroke.add(NODE_STATE + " == " + LBool.False.ordinal(), StrokeLib.getStroke(0.1f, StrokeLib.DOTS));
        nStroke.add(NODE_STATE + " == " + LBool.True.ordinal(), StrokeLib.getStroke(0.5f));
        nStroke.add(NODE_STATE + " == " + LBool.Undefined.ordinal(), StrokeLib.getStroke(1, StrokeLib.DASHES));

        ColorAction eStrokeColor = new ColorAction(EDGES, VisualItem.STROKECOLOR);
        eStrokeColor.setDefaultColor(ColorLib.gray(100));

        StrokeAction eStroke = new StrokeAction(EDGES, StrokeLib.getStroke(5));
        eStroke.add(EDGE_STATE + " == " + LBool.False.ordinal(), StrokeLib.getStroke(0.1f, StrokeLib.DOTS));
        eStroke.add(EDGE_STATE + " == " + LBool.True.ordinal(), StrokeLib.getStroke(0.5f));
        eStroke.add(EDGE_STATE + " == " + LBool.Undefined.ordinal(), StrokeLib.getStroke(1, StrokeLib.DASHES));

        ColorAction eFill = new ColorAction(EDGES, VisualItem.FILLCOLOR);
        eFill.setDefaultColor(ColorLib.gray(100));

        // bundle the color actions
        ActionList colors = new ActionList();
        colors.add(nFill);
        colors.add(nStrokeColor);
        colors.add(nStroke);
        colors.add(eStrokeColor);
        colors.add(eStroke);
        colors.add(eFill);

        // now create the main layout routine
        ActionList layout = new ActionList(Activity.INFINITY);
        layout.add(colors);
        layout.add(new LabelLayout2(EDGE_DECORATORS));
        layout.add(new LabelLayout2(NODE_DECORATORS));
        layout.add(new ForceDirectedLayout(GRAPH, true));
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // set up the display
        setHighQuality(true);
        addControlListener(new PanControl());
        addControlListener(new DragControl());
        addControlListener(new ZoomToFitControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new ControlAdapter() {
            @Override
            public void itemEntered(VisualItem vi, MouseEvent me) {
                Display d = (Display) me.getSource();
                if (vi.getSourceTuple() instanceof Node) {
                    Node nodeData = (Node) vi.getSourceTuple();
                    String t_text = "";
                    switch ((String) nodeData.get(NODE_TYPE)) {
                        case "flaw":
                            t_text += Character.toString('\u03C6');
                            break;
                        case "resolver":
                            t_text += Character.toString('\u03C1');
                            break;
                    }
                    t_text += ": ";
                    switch ((int) nodeData.get(NODE_STATE)) {
                        case 0:
                            t_text += "False";
                            break;
                        case 1:
                            t_text += "True";
                            break;
                        case 2:
                            t_text += "Undefined";
                            break;
                        default:
                            break;
                    }
                    t_text += ", cost: " + (-(Double) nodeData.get(NODE_COST));
//                    d.setToolTipText(t_text);
                    d.setToolTipText(nodeData.get(NODE_CONTENT).toString());
                }
            }

            @Override
            public void itemExited(VisualItem vi, MouseEvent me) {
                Display d = (Display) me.getSource();
                d.setToolTipText(null);
            }

            @Override
            public void itemClicked(VisualItem vi, MouseEvent me) {
                if (vi.getSourceTuple() instanceof Node) {
                    Node nodeData = (Node) vi.getSourceTuple();
                    switch ((String) nodeData.get(NODE_TYPE)) {
                        case "flaw":
                            break;
                        case "resolver":
                            break;
                    }
                }
            }
        });

        // set things running
        m_vis.run("layout");
    }

    @Override
    public void flawCreated(Flaw f) {
        synchronized (m_vis) {
            assert !flaws.containsKey(f) : "the flaw already exists..";
            assert f.getCauses().stream().allMatch(c -> resolvers.containsKey(c)) : "the flaw's cause does not exist";
            Node flaw_node = g.addNode();
            flaw_node.set(VisualItem.LABEL, f.getLabel());
            flaw_node.set(NODE_TYPE, "flaw");
            flaw_node.set(NODE_COST, -f.getEstimatedCost().doubleValue());
            flaw_node.set(NODE_STATE, f.getSolver().sat_core.value(f.getPhi()).ordinal());
            flaws.put(f, flaw_node);
            for (Resolver cause : f.getCauses()) {
                Edge c_edge = g.addEdge(flaw_node, resolvers.get(cause));
                c_edge.set(EDGE_STATE, resolvers.get(cause).get(NODE_STATE));
            }
        }
    }

    @Override
    public void flawStateChanged(Flaw f) {
        synchronized (m_vis) {
            assert flaws.containsKey(f) : "the flaw does not exist..";
            Node flaw_node = flaws.get(f);
            flaw_node.set(NODE_STATE, f.getSolver().sat_core.value(f.getPhi()).ordinal());
        }
    }

    @Override
    public void currentFlaw(Flaw f) {
        assert flaws.containsKey(f) : "the flaw does not exist..";
        synchronized (m_vis) {
            if (current_flaw != null) {
                m_vis.getVisualItem(NODES, flaws.get(current_flaw)).setHighlighted(false);
            }
            if (current_resolver != null) {
                m_vis.getVisualItem(NODES, resolvers.get(current_resolver)).setHighlighted(false);
            }

            current_flaw = f;
            m_vis.getVisualItem(NODES, flaws.get(f)).setHighlighted(true);
        }
    }

    @Override
    public void resolverCreated(Resolver r) {
        synchronized (m_vis) {
            assert !resolvers.containsKey(r) : "the resolver already exists..";
            assert flaws.containsKey(r.getEffect()) : "the resolver's solved flaw does not exist..";
            Node resolver_node = g.addNode();
            resolver_node.set(VisualItem.LABEL, r.getLabel());
            resolver_node.set(NODE_TYPE, "resolver");
            resolver_node.set(NODE_COST, -r.getEstimatedCost().doubleValue());
            resolver_node.set(NODE_STATE, r.getSolver().sat_core.value(r.rho).ordinal());
            resolvers.put(r, resolver_node);
            Edge c_edge = g.addEdge(resolver_node, flaws.get(r.getEffect()));
            c_edge.set(EDGE_STATE, r.getSolver().sat_core.value(r.rho).ordinal());
            flaws.get(r.getEffect()).set(NODE_COST, -r.getEffect().getEstimatedCost().doubleValue());
        }
    }

    @Override
    public void resolverStateChanged(Resolver r) {
        synchronized (m_vis) {
            assert resolvers.containsKey(r) : "the resolver does not exist..";
            Node resolver_node = resolvers.get(r);
            resolver_node.set(NODE_STATE, r.getSolver().sat_core.value(r.rho).ordinal());
            Iterator<Edge> c_edges = resolver_node.edges();
            while (c_edges.hasNext()) {
                c_edges.next().set(EDGE_STATE, r.getSolver().sat_core.value(r.rho).ordinal());
            }
        }
    }

    @Override
    public void resolverCostChanged(Resolver r) {
        synchronized (m_vis) {
            assert resolvers.containsKey(r) : "the resolver does not yet exist..";
            Node resolver_node = resolvers.get(r);
            resolver_node.set(NODE_COST, -r.getEstimatedCost().doubleValue());
            flaws.get(r.getEffect()).set(NODE_COST, -r.getEffect().getEstimatedCost().doubleValue());
        }
    }

    @Override
    public void currentResolver(Resolver r) {
        assert resolvers.containsKey(r) : "the resolver does not exist..";
        synchronized (m_vis) {
            current_resolver = r;
            m_vis.getVisualItem(NODES, resolvers.get(r)).setHighlighted(true);
        }
    }

    @Override
    public void newCausalLink(Flaw f, Resolver r) {
        synchronized (m_vis) {
            assert flaws.containsKey(f) : "the flaw does not exist..";
            assert resolvers.containsKey(r) : "the resolver does not exist..";
            Edge c_edge = g.addEdge(flaws.get(f), resolvers.get(r));
            c_edge.set(EDGE_STATE, resolvers.get(r).get(NODE_STATE));
            flaws.get(r.getEffect()).set(NODE_COST, -r.getEffect().getEstimatedCost().doubleValue());
        }
    }

    /**
     * Set label positions. Labels are assumed to be DecoratorItem instances,
     * decorating their respective nodes. The layout simply gets the bounds of
     * the decorated node and assigns the label coordinates to the center of
     * those bounds.
     */
    private static class LabelLayout2 extends Layout {

        LabelLayout2(String group) {
            super(group);
        }

        @Override
        public void run(double frac) {
            Iterator<?> iter = m_vis.items(m_group);
            while (iter.hasNext()) {
                DecoratorItem decorator = (DecoratorItem) iter.next();
                VisualItem decoratedItem = decorator.getDecoratedItem();
                Rectangle2D bounds = decoratedItem.getBounds();

                double x = bounds.getCenterX();
                double y = bounds.getCenterY();

                setX(decorator, null, x);
                setY(decorator, null, y);
            }
        }
    }
}
