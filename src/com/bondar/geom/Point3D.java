
package com.bondar.geom;

import com.bondar.gm.Matrix;

/**
 *
 * @author truebondar
 */
public class Point3D {
    
    public final static Point3D Zero = new Point3D(0, 0, 0);
    protected Matrix m;
    
    //////////////////////////////////////////////////
    public Point3D() {
	m = new Matrix(new double[][] {{0,0,0}});
    }
    
    public Point3D(double a, double b, double c) {
	m = new Matrix(new double[][] {{a,b,c}});
    }
    
    public Point3D(double[] v) {
	if (v == null) return;
	if (v.length < 3) throw new RuntimeException("Размер массива < 3");
	m = new Matrix(new double[][] {{v[0],v[1],v[2]}});
    }
    
    public Point3D(Point3D p3D) {
	this(p3D.toArray3());
    } 
    
    public Point3D(Point2D p2D) {
	this(p2D.getX(), p2D.getY(), 0);
    }
    
    //////////////////////////////////////////////////
    // operations
    public Point3D add(Point3D other) {
	for (int i = 0; i < 3; i++) {
	    m.getMatrix()[0][i] += other.m.getAt(0, i);
	}
	return this;
    }
    
    public Point3D sub(Point3D other) {
	for (int i = 0; i < 3; i++) {
	    m.getMatrix()[0][i] -= other.m.getAt(0, i);
	}
	return this;
    }
    
    public Point3D mul(Matrix other) {
	m = m.multiply(other);
	return this;
    }
    
    public Point3D mul(double s) {
	for (int i = 0; i < 3; i++) {
	    m.getMatrix()[0][i] *= s;
	}
	return this;
    }

    //////////////////////////////////////////////////
    // set
    public void setX(double x) {
	m.setAt(0, 0, x);
    }

    public void setY(double y) {
	m.setAt(0, 1, y);
    }
    
    public void setZ(double z) {
	m.setAt(0, 2, z);
    }
   
    //////////////////////////////////////////////////
    // get
    public double getX() {
	return m.getAt(0, 0);
    }
    
    public double getY() {
	return m.getAt(0, 1);
    }
    
    public double getZ() {
	return m.getAt(0, 2);
    }
	
    public Point3D getCopy() {
	return new Point3D(getX(), getY(), getZ());
    }
    
    // convert
    public Point2D toPoint2D() {
	return new Point2D(getX(), getY());
    }
    
    public Point3DOdn toPoint3DOdn() {
	return new Point3DOdn(m.getMatrix()[0]);
    }
    
    public double[] toArray3() {
        return m.getMatrix()[0];
    }
    
    public Vector3D toVector3D() {
        return new Vector3D(getX(), getY(), getZ());
    }
}
