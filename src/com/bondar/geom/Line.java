package com.bondar.geom;

import java.awt.Color;

/**
 *
 * @author Иван
 */
public class Line {
    
    public static final Color DEF_COLOR = Color.BLACK;

    protected Color color;
    protected boolean isVisible;

    // set
    public void setColor(Color color) {
	this.color = color;
    }

    public void setVisible(boolean isVisible) {
	this.isVisible = isVisible;
    }
    
    // get
    public Color getColor() {
	return color;
    }

    public boolean isVisible() {
	return isVisible;
    }
}
