package com.bondar.gm;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author truebondar
 */
public class _Triangle3D implements Serializable {

    private static final Color DEF_COLOR = Color.BLACK;
    private Point3D v1, v2, v3;
    private double side1, side2, side3;
    private double space;
    private Color color;

    //////////////////////////////////////////////////
    public _Triangle3D() {
	v1 = new Point3D();
	v2 = new Point3D();
	v3 = new Point3D();
	side1 = side2 = side3 = 0;
	space = 0;
	color = DEF_COLOR;
    }

    public _Triangle3D(Point3D p1, Point3D p2, Point3D p3) {
	setTriangle(p1, p2, p3, DEF_COLOR);
    }

    public _Triangle3D(_Triangle3D tria) {
	setTriangle(tria.v1, tria.v2, tria.v3, tria.getColor());
    }

    public _Triangle3D(Point3D p1, Point3D p2, Point3D p3, Color col) {
	setTriangle(p1, p2, p3, col);
    }

    public _Triangle3D(_Triangle3D tria, Color col) {
	setTriangle(tria.v1, tria.v2, tria.v3, col);
    }
    // set
    public void setTriangle(Point3D p1, Point3D p2, Point3D p3) {
	setTriangle(p1, p2, p3, this.color);
    }
    
    public void setTriangle(Point3D p1, Point3D p2, Point3D p3, Color col) {
	if (p1 == null || p2 == null || p3 == null) {
	    throw new IllegalArgumentException();
	}
	if (p1.equals(p2) || p1.equals(p3) || p2.equals(p3)) {
	    throw new RuntimeException("Координаты треугольника совпадают!");
	}
	this.v1 = p1;
	this.v2 = p2;
	this.v3 = p3;
	color = col;
	defineSides();
	defineSpace();
    }
    //////////////////////////////////////////////////
    // стороны
    private void defineSides() {
	side1 = getSide(v1, v2);
	side2 = getSide(v2, v3);
	side3 = getSide(v1, v3);
    }

    public static double getSide(Point3D p1, Point3D p2) {
	return (Math.sqrt(Math.pow((p1.getX() - p2.getX()), 2.)
		+ Math.pow((p1.getY() - p2.getY()), 2.)
		+ Math.pow((p1.getZ() - p2.getZ()), 2.)));
    }
    //////////////////////////////////////////////////
    // площадь
    private void defineSpace() {
	double p = getSemiperimeter();
	space = Math.sqrt(p * (p - side1) * (p - side2) * (p - side3));
    }

    public String spaceToString() {
	return String.format("%.2f", space);
    }
    //////////////////////////////////////////////////
    // полупериметр
    private double getSemiperimeter() {
	return ((side1 + side2 + side3) / 2);
    }
    //////////////////////////////////////////////////
    // лежит ли точка на поверхности
    public boolean isPointInto(Point3D point) {
	double s1 = new _Triangle3D(v1, v2, point).getSpace();
	double s2 = new _Triangle3D(v2, v3, point).getSpace();
	double s3 = new _Triangle3D(v1, v3, point).getSpace();
	final double e = 1e-6;

	return ((s1 + s2 + s3) - space < e);
    }

    public static boolean isPointInto(_Triangle3D tria, Point3D point) {
	return new _Triangle3D(tria).isPointInto(point);
    }

    //////////////////////////////////////////////////
    // 
    public double getZbyXY(double x, double y) {
	/*
	public float GetZbyXY(float x, float y) {
            float A = F.Y * (S.Z - T.Z) + S.Y * (T.Z - F.Z) + T.Y * (F.Z - S.Z);
            float B = F.Z * (S.X - T.X) + S.Z * (T.X - F.X) + T.Z * (F.X - S.X);
            float C = F.X * (S.Y - T.Y) + S.X * (T.Y - F.Y) + T.X * (F.Y - S.Y);
            float D = -(F.X * (S.Y * T.Z - T.Y * S.Z) + S.X * (T.Y * F.Z - F.Y * T.Z) + T.X * (F.Y * S.Z - S.Y * F.Z));
            return (-D - A * x - B * y) / C;
        }
	 */
	/*Point3DOdn F = v1, S = v2, T = v3;
	double A = F.getY() * (S.getZ() - T.getZ()) + S.getY() * (T.getZ() - F.getZ()) + T.getY() * (F.getZ() - S.getZ());
	double B = F.getZ() * (S.getX() - T.getX()) + S.getZ() * (T.getX() - F.getX()) + T.getZ() * (F.getX() - S.getX());
	double C = F.getX() * (S.getY() - T.getY()) + S.getX() * (T.getY() - F.getY()) + T.getX() * (F.getY() - S.getY());
	double D = -(F.getX() * (S.getY() * T.getZ() - T.getY() * S.getZ())+ S.getX() * (T.getY() * F.getZ() - F.getY() * T.getZ()) + 
		T.getX() * (F.getY() * S.getZ() - S.getY() * F.getZ()));*/
	
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

    //////////////////////////////////////////////////
    // переопределение базовых методов
    @Override
    protected Object clone()
	    throws CloneNotSupportedException {
	return super.clone();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof _Triangle3D)) {
	    return false;
	}
	_Triangle3D tria = (_Triangle3D) obj;
	return (v1.equals(tria.v1)
		&& v2.equals(tria.v2)
		&& v3.equals(tria.v3));
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = result * 37 + (int) v1.getX();
	result = result * 37 + (int) v1.getY();
	result = result * 37 + (int) v2.getX();
	result = result * 37 + (int) v2.getY();
	result = result * 37 + (int) v3.getX();
	result = result * 37 + (int) v3.getY();
	return result;
    }
    
    @Override
    public String toString() {
	return String.format("[%.2f][%.2f][%.2f]", side1, side2, side3);
    }
    
    //////////////////////////////////////////////////
    // конвертация в строку
    public String vertexesToString() {
	return String.format("%s,%s,%s", vertexToString(v1), vertexToString(v2), vertexToString(v3));
    }

    public String vertexToString(Point3D p) {
	return String.format("(%d,%d,%d)", p.getX(), p.getY(), p.getZ());
    }

    //////////////////////////////////////////////////
    // get
    public Point3D getV1() {
	return v1;
    }

    public Point3D getV2() {
	return v2;
    }

    public Point3D getV3() {
	return v3;
    }

    public Point3D[] getVertexes() {
	return new Point3D[]{v1, v2, v3};
    }
    
    public double getSide1() {
	return side1;
    }

    public double getSide2() {
	return side2;
    }

    public double getSide3() {
	return side3;
    }
 
    public double[] getSides() {
	return new double[]{side1, side2, side3};
    }

    public static double[] getSides(_Triangle3D tria) {
	return new _Triangle3D(tria).getSides();
    }

    public double getSpace() {
	return space;
    }

    public static double getSpace(_Triangle3D tria) {
	return new _Triangle3D(tria).getSpace();
    }
  
    public Color getColor() {
	return color;
    }
    
    public void setColor(Color col) {
	color = col;
    }
}
