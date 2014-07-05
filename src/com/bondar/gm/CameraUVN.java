package com.bondar.gm;

import com.bondar.geom.Vector3D;
import com.bondar.geom.Point3D;
import java.awt.Dimension;

/**
 *
 * @author truebondar
 */
public class CameraUVN extends Camera{

    public static final int UVN_MODE_SIMPLE = 0;
    public static final int UVN_MODE_SPHERICAL = 1;
    
    private Vector3D u;		// vectors to track the camera orientation
    private Vector3D v;
    private Vector3D n;
    
    public CameraUVN(int attr, Point3D pos, Vector3D dir, double nearClipZ, double farClipZ, 
	    double dist, double fov, Dimension vp, Point3D target, int mode) {
	super(attr, pos, dir, nearClipZ, farClipZ, dist, fov, vp, target, mode);
	this.u = new Vector3D(1, 0, 0);
	this.v = new Vector3D(0, 1, 0);
	this.n = new Vector3D(0, 0, 1);
    }

    @Override
    public Matrix builtMatrix(int mode) {
	Matrix invM = Matrix.buildTranlateMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
	if (mode == UVN_MODE_SPHERICAL) {
	    double phi = dir.getX();	// elevation
	    double theta = dir.getY();	// heading
	    target = new Vector3D(
		    -1 * Math.sin(phi) * Math.sin(theta), 
		    Math.cos(phi), 
		    Math.sin(phi) * Math.cos(theta));
	}
	// Step 1: n = <target position - view reference point>
	n = new Vector3D(pos, target);
	// Step 2: Let v = <0,1,0>
	v = new Vector3D(0, 1, 0);
	// Step 3: u = (v x n)
	u = v.mul(n);
	// Step 4: v = (n x u)
	v = n.mul(u);
	
	u.normalize();
	v.normalize();
	n.normalize();
	Matrix uvnM = Matrix.buildUVNMatrix(u, v, n);
	return invM.multiply(uvnM);
    }
}
