package com.bondar.geom;

/**
 *
 * @author truebondar
 */
public class Plane3D {
    private Point3D p;
    private Vector3D v;

    public Plane3D() {
	this.p = new Point3D();
	this.v = new Vector3D();
    }
    
    public Plane3D(Point3D p, Vector3D v) {
	setPlane3D(p, v, true);
    }

    // set
    public void setPlane3D(Point3D p, Vector3D v, boolean needNormalize) {
	this.p = p;
	this.v = v;
	if (needNormalize)
	    v.normalize();
    }
    
    // get
    public Point3D getP() {
	return p;
    }

    public Vector3D getV() {
	return v;
    }
}
