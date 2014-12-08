package com.bondar.panels;

import com.bondar.gm.DrawManager;
import com.bondar.gm.ZBufferDrawManager;
import java.awt.Dimension;
import java.awt.Graphics;
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
	drawManager = new ZBufferDrawManager(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	drawManager.setGraphics(g);
//	drawManager.setDimension(g.getClipBounds().width, g.getClipBounds().height);
        app.setDrawPanelDimension(g.getClipBounds().width, g.getClipBounds().height);
	app.paint(drawManager);
    }
    
    public DrawManager getDrawManager() {
	return drawManager;
    }
}
