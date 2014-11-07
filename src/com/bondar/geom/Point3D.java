
package com.bondar.geom;

import com.bondar.gm.Matrix;

/**
 *
 * @author truebondar
 */
public class Point3D {
    
    public final static Point3D Zero = new Point3D(0, 0, 0);
    protected Matrix vector;
    
    //////////////////////////////////////////////////
    public Point3D() {
	vector = new Matrix(new double[][] {{0,0,0}});
    }
    
    public Point3D(double a, double b, double c) {
	vector = new Matrix(new double[][] {{a,b,c}});
    }
    
    public Point3D(double[] v) {
	if (v == null) return;
	if (v.length < 3) throw new RuntimeException("Размер массива < 3");
	vector = new Matrix(new double[][] {{v[0],v[1],v[2]}});
    }
    
    public Point3D(Point3D p) {
	this(p.toArray3());
    } 
    
    public Point3D(Point2D p) {
	this(p.getX(), p.getY(), 0);
    }
    
    //////////////////////////////////////////////////
    // operations
    public Point3D add(Point3D other) {
	for (int i = 0; i < 3; i++) {
	    vector.getMatrix()[0][i] += other.vector.getAt(0, i);
	}
	return this;
    }
    
    public Point3D sub(Point3D other) {
	for (int i = 0; i < 3; i++) {
	    vector.getMatrix()[0][i] -= other.vector.getAt(0, i);
	}
	return this;
    }
    
    public Point3D mul(Matrix other) {
	vector = vector.multiply(other);
	return this;
    }
    
    public Point3D mul(double s) {
	for (int i = 0; i < 3; i++) {
	    vector.getMatrix()[0][i] *= s;
	}
	return this;
    }

    //////////////////////////////////////////////////
    // set
    public void setX(double x) {
	vector.setAt(0, 0, x);
    }

    public void setY(double y) {
	vector.setAt(0, 1, y);
    }
    
    public void setZ(double z) {
	vector.setAt(0, 2, z);
    }
   
    //////////////////////////////////////////////////
    // get
    public double getX() {
	return vector.getAt(0, 0);
    }
    
    public double getY() {
	return vector.getAt(0, 1);
    }
    
    public double getZ() {
	return vector.getAt(0, 2);
    }
	
    public Point3D getCopy() {
	return new Point3D(getX(), getY(), getZ());
    }
    
    // convert
    public Point2D toPoint2D() {
	return new Point2D(getX(), getY());
    }
    
    public Point3DOdn toPoint3DOdn() {
	return new Point3DOdn(vector.getMatrix()[0]);
    }
    
    public double[] toArray3() {
        return vector.getMatrix()[0];
        /*double[] res = new double[vector.getCols()];
        for (int i = 0; i < vector.getCols(); i++) {
            res[i] = vector.getAt(0, i);
        }
        return res;*/
    }
    
    public Vector3D toVector3D() {
        return new Vector3D(getX(), getY(), getZ());
    }
}
