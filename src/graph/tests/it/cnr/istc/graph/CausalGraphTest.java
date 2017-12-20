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

import it.cnr.istc.solver.Solver;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.FileReader;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class CausalGraphTest {

    @Test
    public void testGraph() throws Exception {
        Solver s = new Solver();

        if (!GraphicsEnvironment.isHeadless()) {
            CausalGraph graph = new CausalGraph();
            s.listen(graph);

            JFrame frame = new JFrame("Causal graph");
            frame.setSize(new Dimension(800, 600));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(graph);
            frame.setVisible(true);

            ToolTipManager.sharedInstance().setDismissDelay(60000);
        }

        s.init();
        s.read(new FileReader("domains/logistics_state_variables/logistics_domain.rddl"), new FileReader("domains/logistics_state_variables/logistics_problem_2.rddl"));
        s.solve();
    }
}
