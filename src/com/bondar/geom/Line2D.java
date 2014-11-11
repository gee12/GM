package com.bondar.geom;

import java.awt.Color;

/**
 *
 * @author Иван
 */
public class Line2D extends Line {
    
    private Point2D p1, p2;

    public Line2D() {
	setLine(Point2D.Zero, Point2D.Zero, DEF_COLOR, true);
    }
    
    public Line2D(Point2D p1, Point2D p2) {
	setLine(p1, p2, DEF_COLOR, true);
    }
    
    public Line2D(double x0, double y0, double x1, double y1, boolean isVisible) {
	setLine(new Point2D(x0,y0), new Point2D(x1,y1), DEF_COLOR, isVisible);
    }
    
    public Line2D(double x0, double y0, double x1, double y1, Color col) {
	setLine(new Point2D(x0,y0), new Point2D(x1,y1), col, true);
    }    
    
    public Line2D(double x0, double y0, double x1, double y1, Color col, boolean isVisible) {
	setLine(new Point2D(x0,y0), new Point2D(x1,y1), col, isVisible);
    }    
      
    // set
    public final void setLine(Point2D p1, Point2D p2, Color col, boolean isVisible) {
	this.p1 = p1;
	this.p2 = p2;
	this.isVisible = isVisible;
	this.color = col;
    }
    
    public void setP1(Point2D p1) {
	this.p1 = p1;
    }

    public void setP2(Point2D p2) {
	this.p2 = p2;
    }

    // get
    public Point2D getP1() {
	return p1;
    }

    public Point2D getP2() {
	return p2;
    }
}
