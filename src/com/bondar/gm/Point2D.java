package com.bondar.gm;

import java.awt.Point;

/**
 *
 * @author bondar
 */
public class Point2D extends Point {

    private double x, y;
    
    public final static Point2D Zero = new Point2D(0, 0);

    public Point2D() {
        x = y = 0;
    }
    
    public Point2D(Point2D point) {
        this(point.x, point.y);
    }
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

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
	hash = 89 * hash + (int) (java.lang.Double.doubleToLongBits(this.x) ^ (java.lang.Double.doubleToLongBits(this.x) >>> 32));
	hash = 89 * hash + (int) (java.lang.Double.doubleToLongBits(this.y) ^ (java.lang.Double.doubleToLongBits(this.y) >>> 32));
	return hash;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
    
    public double[] getVector3() {
        return new double[] { x, y, 0, 1 };
    }
    
    public Point2D addPoint(Point2D p) {
	return new Point2D(x + p.getX(), y + p.getY());
    }
    
    public Point2D subPoint(Point2D p) {
	return new Point2D(x - p.getX(), y - p.getY());
    }
    
    public Point2D multPoint(Point2D p) {
	return new Point2D(x * p.getX(), y * p.getY());
    }
}
