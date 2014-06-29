package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class BoundingBox3D extends BoundingBox2D {
    private Bound z;

    public BoundingBox3D() {
	super();
	z = new Bound();
    }
    
    public BoundingBox3D(Point3D[] verts) {
	setBounds(verts);
    }
    
    public void setBounds(Point3D[] verts) {
	if (verts == null) {
	    return;
	}
	int size = verts.length;
	if (size == 0) return;
	final Point3D first = verts[0];
	final double fx = first.getX(), fy = first.getY(), fz = first.getZ();
	x = new Bound(fx, fx);
	y = new Bound(fy, fy);
	z = new Bound(fz, fz);
	for (Point3D v : verts) {
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
	    // z
	    if (v.getZ() < z.getMin()) {
		z.setMin(v.getZ());
	    }
	    if (v.getZ() > z.getMax()) {
		z.setMax(v.getZ());
	    }
	}
    }

    public boolean isPointInto(Point3D p) {
	if (p == null) {
	    return false;
	}
	double px = p.getX(), py = p.getY(), pz = p.getZ();
	if (px >= x.getMin() && px <= x.getMax() 
		&& py >= y.getMin() && py <= y.getMax()
		&& pz >= z.getMin() && pz <= z.getMax()) {
	    return true;
	}
	return false;
    }
    
    public boolean isIntersect(BoundingBox3D bb) {
	if (bb == null) {
	    return false;
	}
	if (bb.x.getMin() <= x.getMax() && bb.x.getMin() >= x.getMin()
		&& bb.y.getMin() <= y.getMax() && bb.y.getMin() >= y.getMin()
		&& bb.z.getMin() <= z.getMax() && bb.z.getMin() >= z.getMin()) {
	    return true;
	}
	return false;
    }

    public Bound getZ() {
	return z;
    }
}