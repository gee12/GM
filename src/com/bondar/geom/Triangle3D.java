package com.bondar.geom;

import com.bondar.tools.Mathem;
import java.awt.Color;

/**
 * Polygon as triangle with indexes for 3 vertexes.
 * @author truebondar
 */
public class Triangle3D extends Polygon3D {
    
    public Triangle3D(Point3D[] verts, int i1, int i2, int i3, Color fill, Color border, int attr) {
	super(verts, new int[] { i1,i2,i3 }, fill, border, attr);
    }
    
    public Triangle3D(Triangle3D tria) {
	super(tria.getVertexes(), tria.getIndexes(), tria.getFillColor(), tria.getBorderColor(), tria.getAttributes());
    }

    public boolean isPointInto(Point3D point) {
	return isPointInto(getV1(), getV2(), getV3(), point);
    }
    
    public static boolean isPointInto(Point3D v1, Point3D v2, Point3D v3, Point3D point) {
	double s1 = getSquare(v1, v2, point);
	double s2 = getSquare(v1, v3, point);
	double s3 = getSquare(v2, v3, point);
	return ((s1 + s2 + s3) - getSquare(v1,v2,v3) < Mathem.EPSILON_E6);
    }
    
    public double zByXY(double x, double y) {
	return zByXY(getV1(), getV2(), getV3(), x, y);
    }
    
    public static double zByXY(Point3D v1, Point3D v2, Point3D v3, double x, double y) {
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
    
    @Override
    public Triangle3D getCopy() {
	return new Triangle3D(vertexes, indexes[0], indexes[1], indexes[2], srcColor, borderColor, attributes);
    }
}