package com.bondar.gm;

import java.awt.Color;

/**
 * Polygon as triangle with indexes for 3 vertexes.
 * @author truebondar
 */
public class Triangle3D extends Polygon3D {
    
    public Triangle3D(Point3D[] verts, int i1, int i2, int i3, Color color) {
	super(verts, new int[] { i1,i2,i3 }, color);
    }
    
    public Triangle3D(Triangle3D tria) {
	super(tria.getVertexes(), tria.getIndexes(), tria.getColor());
    }
    
    public double getZbyXY(double x, double y) {
	Point3D v1 = getV1(), v2 = getV2(), v3 = getV3();
	double A = v1.getY() * (v2.getZ() - v3.getZ()) 
		+ v2.getY() * (v3.getZ() - v1.getZ()) 
		+ v3.getY() * (v1.getZ() - v2.getZ());
	double B = v1.getZ() * (v2.getX() - v3.getX()) 
		+ v2.getZ() * (v3.getX() - v1.getX()) 
		+ v3.getZ() * (v1.getX() - v2.getX());
	double C = v1.getX() * (v2.getY() - v3.getY()) 
		+ v2.getX() * (v3.getY() - v1.getY()) 
		+ v3.getX() * (v1.getY() - v2.getY());
	double D = -(v1.getX() * (v2.getY() * v3.getZ() - v3.getY() * v2.getZ()) 
		+ v2.getX() * (v3.getY() * v1.getZ() - v1.getY() * v3.getZ()) 
		+ v3.getX() * (v1.getY() * v2.getZ()- v2.getY() * v1.getZ()));
	return (-D - A * x - B * y) / C;
    }    
    
    public static double getSide(Point3D p1, Point3D p2) {
	return (Math.sqrt(((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()))
		+ ((p1.getY() - p2.getY()) * (p1.getY() - p2.getY()))
		+ ((p1.getZ() - p2.getZ()) * (p1.getZ() - p2.getZ()))));
    }
    
    public static double getSquare(Point3D v1, Point3D v2, Point3D v3) {
	double side1 = getSide(v1, v2);
	double side2 = getSide(v1, v3);
	double side3 = getSide(v2, v3);
	double p = (side1 + side2 + side3) / 2.;
	return Math.sqrt(p * (p - side1) * (p - side2) * (p - side3));
    }

    public double getSquare() {
	return getSquare(getV1(), getV2(), getV3());
    }

    public boolean isPointInto(Point3D point) {
	double s1 = getSquare(getV1(), getV2(), point);
	double s2 = getSquare(getV2(), getV3(), point);
	double s3 = getSquare(getV1(), getV3(), point);
	return ((s1 + s2 + s3) - getSquare() < 1e-6);
    }

    // get
    public Point3D getV1() {
	return vertexes[indexes[0]];
    }

    public Point3D getV2() {
	return vertexes[indexes[1]];
    }

    public Point3D getV3() {
	return vertexes[indexes[2]];
    }
}