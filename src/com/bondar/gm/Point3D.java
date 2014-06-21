
package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class Point3D {
    
    public final static Point3D Zero = new Point3D(0, 0, 0);
    protected Matrix vector;
    
    //
    public Point3D() {
	vector = new Matrix(new double[][] {{0,0,0}});
    }
    
    public Point3D(double a, double b, double c) {
	vector = new Matrix(new double[][] {{a,b,c}});
    }
    
    public Point3D(double[] v) {
	if (v == null) return;
	if (v.length < 3) throw new RuntimeException("Размер вектора < 3");
	vector = new Matrix(new double[][] {{v[0],v[1],v[2]}});
    }
    
    public Point3D(Point2D p2d) {
	this(p2d.getX(), p2d.getY(), 0);
    }
    // operations
    public Point3D mul(Matrix other) {
	vector = vector.multiply(other);
	return this;
    }
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
   
     // get
    public Point2D toPoint2D() {
	return new Point2D(getX(), getY());
    }
    
    public Point3DOdn toPoint3DOdn() {
	return new Point3DOdn(getX(), getY(), getZ());
    }
    
    public double[] toArray3() {
        return vector.getMatrix()[0];
    }
    
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
}
