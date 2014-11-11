package com.bondar.panels;

import com.bondar.gm.DrawManager;
import com.bondar.gm.GraphicSystem2D;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author bondar
 */
public class DrawablePanel extends JPanel {

    private final Application app;
    private final DrawManager drawManager;
    
    public DrawablePanel(Application app, int width, int height) {
	this.app = app;
	setPreferredSize(new Dimension(width, height));
	drawManager = new DrawManager(width, height);
    }
    
    public DrawManager getDrawManager() {
	return drawManager;
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	drawManager.setGraphics(g);
	drawManager.setDimension(g.getClipBounds().width, g.getClipBounds().height);
	app.paint(drawManager);
    }
}
