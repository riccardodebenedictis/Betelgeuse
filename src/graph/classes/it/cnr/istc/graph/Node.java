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
import java.util.Collection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Node {

    double pos_x;
    double pos_y;
    double disp_x;
    double disp_y;
    double width = 60;
    double height = 30;
    Collection<Edge> incoming_edges = new ArrayList<>();
    Collection<Edge> outgoing_edges = new ArrayList<>();
    private Color fill_color = Color.ALICEBLUE;
    private Color stroke_color = Color.DARKGRAY;

    Node(double pos_x, double pos_y, double disp_x, double disp_y) {
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.disp_x = disp_x;
        this.disp_y = disp_y;
    }

    void draw(GraphicsContext gc) {
        gc.setFill(fill_color);
        gc.setStroke(stroke_color);
        gc.fillRoundRect(pos_x - width / 2, pos_y - height / 2, width, height, 10, 10);
        gc.setLineDashes(2, 5);
        gc.strokeRoundRect(pos_x - width / 2, pos_y - height / 2, width, height, 10, 10);
    }
}
