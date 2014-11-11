package com.bondar.geom;

import java.awt.Color;

/**
 *
 * @author bondar
 */
public final class Line3D extends Line {

    private Point3D p1, p2;

    public Line3D() {
	setLine(Point3D.Zero, Point3D.Zero, DEF_COLOR, true);
    }

    public Line3D(Point3D p1, Point3D p2) {
	setLine(p1, p2, DEF_COLOR, true);
    }

    public Line3D(Point3D p1, Point3D p2, Color col, boolean isVisible) {
	setLine(p1, p2, col, isVisible);
    }
    
    // set
    public void setLine(Point3D p1, Point3D p2, Color col, boolean isVisible) {
	this.p1 = p1;
	this.p2 = p2;
	this.isVisible = isVisible;
	this.color = col;
    }

    public void setP1(Point3D p1) {
	this.p1 = p1;
    }

    public void setP2(Point3D p2) {
	this.p2 = p2;
    }

    // get
    public Point3D getP1() {
	return p1;
    }

    public Point3D getP2() {
	return p2;
    }
}
