package com.bondar.gm;

import java.awt.Color;

/**
 *
 * @author bondar
 */
public final class Line {

    private static final Color DEF_COLOR = Color.BLACK;
    private Point3DOdn p1, p2;
    private Color color;
    private boolean isVisible;

    public Line() {
	setLine(Point3DOdn.Zero, Point3DOdn.Zero, DEF_COLOR, false);
    }
    // 2D
    public Line(Point2D p1, Point2D p2) {
	setLine(p1, p2, DEF_COLOR, true);
    }
    public Line(double x0, double y0, double x1, double y1, boolean isVisible) {
	setLine(new Point3DOdn(x0,y0,0), new Point3DOdn(x1,y1,0), DEF_COLOR, isVisible);
    }
    // 3D
    public Line(Point3DOdn p1, Point3DOdn p2, boolean isVisible) {
	setLine(p1, p2, DEF_COLOR, isVisible);
    }

    public Line(Point3DOdn p1, Point3DOdn p2) {
	setLine(p1, p2, DEF_COLOR, true);
    }

    public Line(Point3DOdn p1, Point3DOdn p2, Color col, boolean isVisible) {
	setLine(p1, p2, col, isVisible);
    }
    
    public Line(Point3DOdn p1, Point3DOdn p2, Color col) {
	setLine(p1, p2, col, true);
    }
    // set
    public void setLine(Point2D p1, Point2D p2, Color col, boolean isVisible) {
	setLine(new Point3DOdn(p1), new Point3DOdn(p2), col, isVisible);
    }
    
    public void setLine(Point3DOdn p1, Point3DOdn p2, Color col, boolean isVisible) {
	this.p1 = p1;
	this.p2 = p2;
	this.isVisible = isVisible;
	this.color = col;
    }

    public void setP1(Point3DOdn p1) {
	this.p1 = p1;
    }

    public void setP2(Point3DOdn p2) {
	this.p2 = p2;
    }

    public void setColor(Color color) {
	this.color = color;
    }

    public void setVisible(boolean isVisible) {
	this.isVisible = isVisible;
    }
    // get
    public Point3DOdn getP1() {
	return p1;
    }

    public Point3DOdn getP2() {
	return p2;
    }

    public Color getColor() {
	return color;
    }

    public boolean isVisible() {
	return isVisible;
    }
}
