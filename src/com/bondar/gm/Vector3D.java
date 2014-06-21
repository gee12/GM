package com.bondar.gm;

import com.bondar.tools.Mathem;

/**
 *
 * @author truebondar
 */
public class Vector3D extends Point3D {
    public Vector3D() {
	super();
    }
    
   public Vector3D(double a, double b, double c) {
	super(a, b, c);
    }
    
    public Vector3D(double[] v) {
	super(v);
    }
    
    public void normalize() {
	double length = Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
	if (length < Mathem.EPSILON_E5)
	    return;
	mul(1. / length);
    }
}
