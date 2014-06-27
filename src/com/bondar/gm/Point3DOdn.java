
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
    
    public Point3DOdn(Point3DOdn p3DOdn) {
	this(p3DOdn.toArray4Odn());
    }
    
    public Point3DOdn(Point2D p2D) {
	this(p2D.getX(), p2D.getY(), 0);
    }

    public Point3DOdn divByW() {
	// div by w
	double w = 1 / getW();
	for (int i = 0; i < 4; i++) {
	    vector.setAt(0, i, vector.getAt(0, i) * w);
	}
	return this;
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
    
    // convert
    public Point3D toPoint3D() {
	return (Point3D)divByW();
    }

    public double[] toArray4Odn() {
        return vector.getMatrix()[0];
    }
}
