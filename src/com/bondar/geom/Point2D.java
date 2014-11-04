package com.bondar.geom;

import java.awt.Point;

/**
 *
 * @author bondar
 */
public class Point2D {

    public final static Point2D Zero = new Point2D(0, 0);
    private double x, y;
    
    //////////////////////////////////////////////////
    public Point2D() {
        x = y = 0;
    }
    
    public Point2D(Point point) {
        this(point.x, point.y);
    }
    
    public Point2D(Point2D point) {
        this(point.x, point.y);
    }
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Point2D(double[] v) {
	if (v == null) return;
	if (v.length < 2) throw new RuntimeException("Размер массива < 2");
        this.x = v[0];
        this.y = v[1];
    }
    
    //////////////////////////////////////////////////
    // operations
    public Point2D add(Point2D p) {
	return new Point2D(x + p.getX(), y + p.getY());
    }
    
    public Point2D sub(Point2D p) {
	return new Point2D(x - p.getX(), y - p.getY());
    }
    
    public Point2D mul(Point2D p) {
	return new Point2D(x * p.getX(), y * p.getY());
    }
    
    //////////////////////////////////////////////////
    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof Point2D)) {
	    return false;
	}
	Point2D p = (Point2D) obj;
	return (p.x == x && p.y == y);
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 89 * hash + (int) (java.lang.Double.doubleToLongBits(this.x) 
		^ (java.lang.Double.doubleToLongBits(this.x) >>> 32));
	hash = 89 * hash + (int) (java.lang.Double.doubleToLongBits(this.y) 
		^ (java.lang.Double.doubleToLongBits(this.y) >>> 32));
	return hash;
    }
      
    //////////////////////////////////////////////////
    // set
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
    
    //////////////////////////////////////////////////
    // get
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }

    // convert
    public double[] toArray4Odn() {
        return new double[] { x, y, 0, 1 };
    }
}
