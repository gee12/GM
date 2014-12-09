package com.bondar.geom;

import com.bondar.gm.Matrix;
import com.bondar.tools.Mathem;

/**
 *
 * @author truebondar
 */
public class Vector3D extends Point3DOdn {
    
    public Vector3D() {
	super();
    }
    
    /////////////////////////////////////////////////////////
    public Vector3D(double a, double b, double c) {
	super(a, b, c);
    }
    
    public Vector3D(Point3D p0, Point3D p1) {
	//super(p1.getCopy().sub(p0).toArray3());
	super((p1.getX() - p0.getX()),
		p1.getY() - p0.getY(),
		p1.getZ() - p0.getZ());
    }
    
    public Vector3D(double[] v) {
	super(v);
    }
    
    /////////////////////////////////////////////////////////
    //
    public Vector3D normalize() {
	double length = length();
	if (length < Mathem.EPSILON_E5)
	    return null;
	return this.mul(1. / length);
    }

    public Vector3D mul(double s) {
	super.mul(s);
        return this;
    }
        
    public Vector3D mul(Vector3D v) {
	return mul(this, v);
    }
    
    public static Vector3D mul(Vector3D v1, Vector3D v2) {
	double x = ((v1.getY() * v2.getZ()) - (v1.getZ() * v2.getY()));
	double y = -((v1.getX() * v2.getZ()) - (v1.getZ() * v2.getX()));
	double z = ((v1.getX() * v2.getY()) - (v1.getY() * v2.getX()));
	return new Vector3D(x, y, z);
    }
    
    public Vector3D mul(Matrix other) {
	vector = vector.multiply(other);
	return this;
    }
     
    public double dot(Vector3D v) {
	return dot(this, v);
    }
       
    public static double dot(Vector3D v1, Vector3D v2) {
	return ((v1.getX() * v2.getX()) + (v1.getY() * v2.getY()) + (v1.getZ() * v2.getZ()));
    }
    
    public double length() {
	return Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
    }
    
    public double fastLength() {
        return Mathem.fastDistance3D(getX(), getY(), getZ());
    }
}
