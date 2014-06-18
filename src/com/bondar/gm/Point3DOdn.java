
package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class Point3DOdn {
    
    public final static Point3DOdn Zero = new Point3DOdn(0, 0, 0);
    private Matrix vector;
    
    //
    public Point3DOdn() {
	vector = new Matrix(new double[][] {{0,0,0,1}});
    }
    
    public Point3DOdn(double a, double b, double c) {
	vector = new Matrix(new double[][] {{a,b,c,1}});
    }
    
    public Point3DOdn(double[] v) {
	if (v == null) return;
	if (v.length < 3) throw new RuntimeException("Размер вектора < 3");
	vector = new Matrix(new double[][] {{v[0],v[1],v[2],1}});
    }
    
    public Point3DOdn(Point2D p2d) {
	this(p2d.getX(), p2d.getY(), 0);
    }
    // multiply
    public Point3DOdn multiply(Matrix other) {
	vector = vector.multiply(other);
	return this;
    }
    public void normalizeByW() {
	// div on w
	double w = 1 / getW();
	for (int i = 0; i < 4; i++) {
	    vector.setAt(0, i, vector.getAt(0, i) * w);
	}
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
    
    public void setW(double w) {
	vector.setAt(0, 3, w);
    }    
     // get
    public Point2D toPoint2D() {
	return new Point2D(vector.getAt(0,0), vector.getAt(0,1));
    }
    
    public double[] toVector3() {
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
    
    public double getW() {
	return vector.getAt(0, 3);
    }
	
    public Point3DOdn getCopy() {
	return new Point3DOdn(getX(), getY(), getZ());
    }
}
