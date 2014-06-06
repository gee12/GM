package com.bondar.panels;

import com.bondar.gm.GraphicSystem;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author bondar
 */
public class DrawablePanel extends JPanel {

    private final Application app;
    private final GraphicSystem graphicSystem;


    public DrawablePanel(Application app, int width, int height) {
	this.app = app;
	setPreferredSize(new Dimension(width, height));
	graphicSystem = new GraphicSystem();
    }
    
    public GraphicSystem getGraphicSystem() {
	return graphicSystem;
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	graphicSystem.setGraphics(g);
	app.paint(graphicSystem);
    }
}
