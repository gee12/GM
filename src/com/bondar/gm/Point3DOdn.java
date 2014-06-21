
package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class Point3DOdn extends Point3D {
    
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

    public void normalizeByW() {
	// div by w
	double w = 1 / getW();
	for (int i = 0; i < 4; i++) {
	    vector.setAt(0, i, vector.getAt(0, i) * w);
	}
    }
    // set
    public void setW(double w) {
	vector.setAt(0, 3, w);
    }    
     // get
    public double getW() {
	return vector.getAt(0, 3);
    }
    
    @Override
    public Point3DOdn getCopy() {
	return new Point3DOdn(getX(), getY(), getZ());
    }
}
