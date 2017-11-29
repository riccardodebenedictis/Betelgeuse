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

import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Graph extends Canvas {

    private static final double C = .2;
    private final List<Node> nodes = new ArrayList<>();
    private double k;
    private double width;
    private double height;
    private Color background = Color.WHITESMOKE;
    private final AnimationTimer loop;

    public Graph() {
        widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            width = newValue.floatValue();
            k = (C * Math.sqrt(width * height / nodes.size()));
        });
        heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            height = newValue.floatValue();
            k = (C * Math.sqrt(width * height / nodes.size()));
        });
        final GraphicsContext gc = getGraphicsContext2D();
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.setFill(background);
                gc.fillRect(0, 0, width, height);

                for (Node node : nodes) {
                    node.disp_x = 0;
                    node.disp_y = 0;
                }

                for (int i = 0; i < nodes.size(); i++) {
                    Node n_i = nodes.get(i);

                    // we compute the repulsive forces..
                    for (int j = i + 1; j < nodes.size(); j++) {
                        Node n_j = nodes.get(j);

                        double x_dist = n_i.pos_x - n_j.pos_x;
                        double y_dist = n_i.pos_y - n_j.pos_y;
                        double dist = Math.sqrt(x_dist * x_dist + y_dist * y_dist);
                        double r_force = -k * k / dist;

                        n_i.disp_x += x_dist / dist * r_force;
                        n_i.disp_y += y_dist / dist * r_force;
                        n_j.disp_x -= x_dist / dist * r_force;
                        n_j.disp_y -= y_dist / dist * r_force;
                    }

                    // we compute the attractive forces..
                    for (Edge o_edge : n_i.outgoing_edges) {
                        Node n_j = o_edge.to;

                        double x_dist = n_i.pos_x - n_j.pos_x;
                        double y_dist = n_i.pos_y - n_j.pos_y;
                        double dist = Math.sqrt(x_dist * x_dist + y_dist * y_dist);
                        double a_force = dist * dist / k;

                        n_i.disp_x += x_dist / dist * a_force;
                        n_i.disp_y += y_dist / dist * a_force;
                        n_j.disp_x -= x_dist / dist * a_force;
                        n_j.disp_y -= y_dist / dist * a_force;
                    }
                }

                for (Node node : nodes) {
                    // we update the position..
                    node.pos_x += node.disp_x;
                    node.pos_y += node.disp_y;

                    // we frame the positions..
                    if (node.pos_x < 0) {
                        node.pos_x = 0;
                    } else if (node.pos_x > width) {
                        node.pos_x = width;
                    }
                    if (node.pos_y < 0) {
                        node.pos_y = 0;
                    } else if (node.pos_y > height) {
                        node.pos_y = height;
                    }

                    // we paint the node..
                    node.draw(gc);
                }
            }
        };
    }

    public Node newNode() {
        Node n = new Node((float) (Math.random() * width), (float) (Math.random() * height), 0, 0);
        nodes.add(n);
        k = (float) (0.2 * Math.sqrt(width * height / nodes.size()));
        return n;
    }

    public Edge newEdge(final Node from, final Node to) {
        Edge e = new Edge(from, to);
        from.outgoing_edges.add(e);
        to.incoming_edges.add(e);
        return e;
    }
}
