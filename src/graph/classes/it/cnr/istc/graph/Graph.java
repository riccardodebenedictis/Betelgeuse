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
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Graph extends Canvas {

    private static final double C = .2;
    private static final double SPEED = .2;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private double k;
    private double max_disp;
    private double width;
    private double height;
    private double pan_x = 0;
    private double pan_y = 0;
    private double initial_disp_x = 0;
    private double initial_disp_y = 0;
    private double scale = 1;
    private Color background = Color.WHITESMOKE;
    private final AnimationTimer loop;

    public Graph() {
        widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            width = (1d / scale) * newValue.doubleValue();
            k = scale * (C * Math.sqrt(width * height / (nodes.size() + 5)));
            max_disp = scale * Math.sqrt(width * height) / 10;
        });
        heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            height = (1d / scale) * newValue.doubleValue();
            k = scale * (C * Math.sqrt(width * height / (nodes.size() + 5)));
            max_disp = scale * Math.sqrt(width * height) / 10;
        });

        addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
                    Platform.runLater(() -> {
                        initial_disp_x = event.getX();
                        initial_disp_y = event.getY();
                    });
                }
                if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    Platform.runLater(() -> {
                        pan_x = event.getX() - initial_disp_x;
                        pan_y = event.getY() - initial_disp_y;
                    });
                }
                System.out.println(event);
            }
        });
        addEventHandler(ScrollEvent.SCROLL, (ScrollEvent event) -> {
            Platform.runLater(() -> {
                scale -= event.getDeltaY() * 0.001;
                width = (1d / scale) * getWidth();
                height = (1d / scale) * getHeight();
                k = scale * (C * Math.sqrt(width * height / (nodes.size() + 5)));
                max_disp = scale * Math.sqrt(width * height) / 10;
            });
        });

        final GraphicsContext gc = getGraphicsContext2D();
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {

                gc.save();

                gc.setFill(background);
                gc.fillRect(0, 0, width, height);

                gc.translate(pan_x, pan_y);
                gc.scale(scale, scale);

                gc.setStroke(Color.BLACK);
                gc.strokeRect(0, 0, width, height);

                for (Node node : nodes) {
                    node.disp_x = 0;
                    node.disp_y = 0;
                }

                for (int i = 0; i < nodes.size(); i++) {
                    Node n_i = nodes.get(i);

                    // we compute the repulsive forces..
                    for (int j = i + 1; j < nodes.size(); j++) {
                        Node n_j = nodes.get(j);

                        while (n_i.pos_x == n_j.pos_x && n_i.pos_y == n_j.pos_y) {
                            n_i.pos_x += Math.random() * k * 0.2 - 0.1;
                            n_i.pos_y += Math.random() * k * 0.2 - 0.1;
                            n_j.pos_x += Math.random() * k * 0.2 - 0.1;
                            n_j.pos_y += Math.random() * k * 0.2 - 0.1;
                        }

                        double x_dist = n_i.pos_x - n_j.pos_x;
                        double y_dist = n_i.pos_y - n_j.pos_y;
                        double dist = Math.sqrt(x_dist * x_dist + y_dist * y_dist);
                        double r_force = k * k / dist;

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
                    if (node.disp_x > max_disp) {
                        node.disp_x = max_disp;
                    }
                    if (node.disp_x < -max_disp) {
                        node.disp_x = -max_disp;
                    }
                    if (node.disp_y > max_disp) {
                        node.disp_y = max_disp;
                    }
                    if (node.disp_y < -max_disp) {
                        node.disp_y = -max_disp;
                    }

                    // we update the displacement according to the speed..
                    node.disp_x *= SPEED;
                    node.disp_y *= SPEED;

                    // we update the position..
                    node.pos_x += node.disp_x;
                    node.pos_y += node.disp_y;

                    // we frame the positions..
                    if (node.pos_x < node.width / 2 - pan_x * (1 / scale)) {
                        node.pos_x = node.width / 2 - pan_x * (1 / scale);
                    } else if (node.pos_x > width - node.width / 2 - pan_x * (1 / scale)) {
                        node.pos_x = width - node.width / 2 - pan_x * (1 / scale);
                    }
                    if (node.pos_y < 20) {
                        node.pos_y = 20;
                    } else if (node.pos_y > height - 50) {
                        node.pos_y = height - 50;
                    }
                    if(node.pos_x>750)
                    System.out.println(node.pos_x + ", " + node.pos_y);

                    // we paint the node..
                    node.draw(gc);
                }

                gc.restore();
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
        edges.add(e);
        return e;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    public void start() {
        loop.start();
    }

    public void stop() {
        loop.stop();
    }
}
