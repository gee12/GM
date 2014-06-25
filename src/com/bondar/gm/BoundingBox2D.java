package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class BoundingBox2D {
    protected Bound x;
    protected Bound y;

    public BoundingBox2D() {
	x = new Bound();
	y = new Bound();
    }
    
    public BoundingBox2D(Point2D[] verts) {
	setBounds(verts);
    }
    
    public void setBounds(Point2D[] verts) {
	if (verts == null) {
	    return;
	}
	int size = verts.length;
	if (size == 0) return;
	final Point2D first = verts[0];
	final double fx = first.getX(), fy = first.getY();
	x = new Bound(fx, fx);
	y = new Bound(fy, fy);
	for (Point2D v : verts) {
	    // x
	    if (v.getX() < x.getMin()) {
		x.setMin(v.getX());
	    }
	    if (v.getX() > x.getMax()) {
		x.setMax(v.getX());
	    }
	    // y
	    if (v.getY() < y.getMin()) {
		y.setMin(v.getY());
	    }
	    if (v.getY() > y.getMax()) {
		y.setMax(v.getY());
	    }
	}
    }

    public boolean isPointInto(Point2D p) {
	if (p == null) {
	    return false;
	}
	double px = p.getX(), py = p.getY();
	if (px >= x.getMin() && px <= x.getMax() 
		&& py >= y.getMin() && py <= y.getMax()) {
	    return true;
	}
	return false;
    }
    
    public boolean isIntersect(BoundingBox2D bb) {
	if (bb == null) {
	    return false;
	}
	if (bb.x.getMin() <= x.getMax() && bb.x.getMin() >= x.getMin()
		&& bb.y.getMin() <= y.getMax() && bb.y.getMin() >= y.getMin()) {
	    return true;
	}
	return false;
    }

    public Bound getX() {
	return x;
    }

    public Bound getY() {
	return y;
    }
}
