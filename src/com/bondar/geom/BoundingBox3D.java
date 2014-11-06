package com.bondar.geom;

import com.bondar.geom.Point3D;
import com.bondar.geom.Bound;

/**
 *
 * @author truebondar
 */
public class BoundingBox3D extends BoundingBox2D {
    
    private Bound z;
    private double maxRadius;
    private double avgRadius;

    public BoundingBox3D() {
	super();
	z = new Bound();
    }
    
    public BoundingBox3D(Point3D[] verts) {
	resetBounds(verts);
    }
    
    /////////////////////////////////////////////////////////
    public void resetBounds(Point3D[] verts) {
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
        resetMaxRadius();
        resetAvgRadius();
    }
    
    /////////////////////////////////////////////////////////
    // before calling need to init bounds
    public void resetMaxRadius() {
        maxRadius = maxRadius(this); //localVerts);
    }
    
    // before calling need to init bounds
    public void resetAvgRadius() {
        maxRadius = avgRadius(this); //localVerts);
    }
    
    /////////////////////////////////////////////////////////
    public boolean isLocalPointInto(Point3D lp) {
	if (lp == null) return false;
	double px = lp.getX(), py = lp.getY(), pz = lp.getZ();
	if (px >= x.getMin() && px <= x.getMax() 
		&& py >= y.getMin() && py <= y.getMax()
		&& pz >= z.getMin() && pz <= z.getMax()) {
	    return true;
	}
	return false;
    }
    
    /////////////////////////////////////////////////////////
    public boolean isIntersect(BoundingBox3D bb) {
	if (bb == null) return false;
	return (bb.x.getMin() <= x.getMax() && bb.x.getMin() >= x.getMin()
		&& bb.y.getMin() <= y.getMax() && bb.y.getMin() >= y.getMin()
		&& bb.z.getMin() <= z.getMax() && bb.z.getMin() >= z.getMin());
    }
    
    /////////////////////////////////////////////////////////
    //
    public static double maxRadius(BoundingBox3D bb) {
        double res = 0;
	if (bb == null) return res;
        for (Bound b : bb.toArray()) {
            double max = b.getMax();
            if (max > res) res = max;
            double absMin = Math.abs(b.getMin());
            if (absMin > res) res = absMin;
        }
        return res;
    }
    
    public static double maxRadius(Point3D[] verts) {
	double res = 0;
        if (verts == null || verts.length == 0) return res;
        for (Point3D v : verts) {
	    // x
            double vx = Math.abs(v.getX());
	    if (vx > res) res = vx;
	    // y
            double vy = Math.abs(v.getY());
	    if (vy > res) res = vy;
	    // z
            double vz = Math.abs(v.getZ());
	    if (vz > res) res = vz;
        }
	return res;
    }
    
    // arithmetic average radius
    public static double avgRadius(Point3D[] verts) {
	double avg = 0;
        if (verts == null) return avg;
        final int size = verts.length;
        if (size == 0) return avg;
        double sum = 0;
        for (Point3D v : verts) {
            double vx = Math.abs(v.getX());
            double vy = Math.abs(v.getY());
            double vz = Math.abs(v.getZ());
            sum += (vx + vy + vz);
        }
        avg = sum / (size * 3);
	return avg;
    }
    
    public static double avgRadius(BoundingBox3D bb) {
        double avg = 0;
	if (bb == null) return avg;
        double sum = 0;
        for (Bound b : bb.toArray()) {
            double absMin = Math.abs(b.getMin());
            double max = b.getMax();
            sum += (absMin + max);
        }
        // 6 = 3 (axes) * 2 (min,max)
        avg = sum / 6;
        return avg;
    }
        
    public static Point3D center(BoundingBox3D bb) {
	if (bb == null) return null;
	double cx = (bb.getX().getMin() + bb.getX().getMax()) / 2;
	double cy = (bb.getY().getMin() + bb.getY().getMax()) / 2;
	double cz = (bb.getZ().getMin() + bb.getZ().getMax()) / 2;
	return new Point3D(cx, cy, cz);
    }
    
    /////////////////////////////////////////////////////////
    public Bound getZ() {
	return z;
    }
    
    public Bound[] toArray() {
        return new Bound[] { x, y, z};
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    public double getAvgRadius() {
        return avgRadius;
    }
}
