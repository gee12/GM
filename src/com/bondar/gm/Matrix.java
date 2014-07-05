package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Vector3D;
import com.bondar.geom.Point3DOdn;

/**
 *
 * @author bondar
 */
public class Matrix {

    public static enum AXIS {
	X,
	Y,
	Z
    }
    private static final int DEF_SIZE = 4;
    private double[][] m;
    private int rows;
    private int cols;

    public Matrix() {
	reinit();
    }

    public Matrix(double[][] m) {
	if (m == null) {
	    reinit();
	    return;
	}
	this.m = m;
	rows = m.length;
	if (rows > 0) {
	    cols = m[0].length;
	}
    }

    public void reinit() {
	m = new double[][]{
	    {1, 0, 0, 0},
	    {0, 1, 0, 0},
	    {0, 0, 1, 0},
	    {0, 0, 0, 1}
	};
	rows = cols = DEF_SIZE;
    }

    //////////////////////////////////////////////////
    public static Matrix buildRotationMatrix(double angle, AXIS axis) {
	switch (axis) {
	    case X: 
		return Matrix.buildRotationXMatrix(angle);
	    case Y:
		return Matrix.buildRotationYMatrix(angle);
	    case Z:
		return Matrix.buildRotationZMatrix(angle);
	    default: return new Matrix();
	}
    }
    
    public static Matrix buildRotationZMatrix(double a) {
	return new Matrix(new double[][] {
		{Math.cos(a), Math.sin(a), 0, 0},
		{-Math.sin(a), Math.cos(a), 0, 0},
		{0, 0, 1, 0},
		{0, 0, 0, 1}});
    }

    public static Matrix buildRotationYMatrix(double a) {
	return new Matrix(new double[][] {
		{Math.cos(a), 0, -Math.sin(a), 0},
		{0, 1, 0, 0},
		{Math.sin(a), 0, Math.cos(a), 0},
		{0, 0, 0, 1}});
    }

    public static Matrix buildRotationXMatrix(double a) {
	return new Matrix(new double[][] {
		{1, 0, 0, 0},
		{0, Math.cos(a), Math.sin(a), 0},
		{0, -Math.sin(a), Math.cos(a), 0},
		{0, 0, 0, 1}});
    }
    
    //////////////////////////////////////////////////
    public static Matrix buildRotationTransMatrix(double angle, Point3D p0, AXIS axis) {
	switch (axis) {
	    case X: 
		return Matrix.buildRotationTransXMatrix(angle, p0);
	    case Y:
		return Matrix.buildRotationTransYMatrix(angle, p0);
	    case Z:
		return Matrix.buildRotationTransZMatrix(angle, p0);
	    default: return new Matrix();
	}
    }
    public static Matrix buildRotationTransZMatrix(double a, Point3D p0) {
	return new Matrix(new double[][] {
		{Math.cos(a), Math.sin(a), 0, 0},
		{-Math.sin(a), Math.cos(a), 0, 0},
		{0, 0, 1, 0},
		{p0.getX()*(1-Math.cos(a))+p0.getY()*Math.sin(a),
		    p0.getY()*(1-Math.cos(a))-p0.getX()*Math.sin(a), 0, 1}});
    }

    public static Matrix buildRotationTransYMatrix(double a, Point3D p0) {
	return new Matrix(new double[][] {
		{Math.cos(a), 0, -Math.sin(a), 0},
		{0, 1, 0, 0},
		{Math.sin(a), 0, Math.cos(a), 0},
		{p0.getX()*(1-Math.cos(a))+p0.getY()*Math.sin(a),
		    p0.getY()*(1-Math.cos(a))-p0.getX()*Math.sin(a), 0, 1}});
    }

    public static Matrix buildRotationTransXMatrix(double a, Point3D p0) {
	return new Matrix(new double[][] {
		{1, 0, 0, 0},
		{0, Math.cos(a), Math.sin(a), 0},
		{0, -Math.sin(a), Math.cos(a), 0},
		{p0.getX()*(1-Math.cos(a))+p0.getY()*Math.sin(a),
		    p0.getY()*(1-Math.cos(a))-p0.getX()*Math.sin(a), 0, 1}});
    }    
    
    //////////////////////////////////////////////////
    public static Matrix buildTranlateMatrix(double tx, double ty, double tz) {
	return new Matrix(new double[][]{
	    {1, 0, 0, 0},
	    {0, 1, 0, 0},
	    {0, 0, 1, 0},
	    {tx, ty, tz, 1}});
    }

    public void translate(double tx, double ty) {
	translate(tx, ty, 0);
    }
    
     public void translate(double tx, double ty, double tz) {
	if (rows < 3 || cols < 4) {
	    throw new RuntimeException("Неверный размер матрицы");
	}
	m[3][0] += tx;
	m[3][1] += ty;
	m[3][2] += tz;
    }
     
    //////////////////////////////////////////////////
    public static Matrix buildScaleMatrix(double sx, double sy, double sz) {
	return new Matrix(new double[][]{
	    {sx, 0, 0, 0},
	    {0, sy, 0, 0},
	    {0, 0, sz, 0},
	    {0, 0, 0, 1}});
    }
    
    public static Matrix buildScaleMatrix(double s) {
	return new Matrix(new double[][]{
	    {1, 0, 0, 0},
	    {0, 1, 0, 0},
	    {0, 0, 1, 0},
	    {0, 0, 0, s}});
    }
    
    //////////////////////////////////////////////////
    public static Matrix buildViewMatrix(double ro, double theta, double phi) {
	return new Matrix(new double[][]{
	    {-Math.sin(theta), -(Math.cos(phi) * Math.cos(theta)), -(Math.sin(phi) * Math.cos(theta)), 0},
	    {Math.cos(theta), -(Math.cos(phi) * Math.cos(theta)), -(Math.sin(phi) * Math.sin(theta)), 0},
	    {0, Math.sin(phi), -(Math.cos(phi)), 0},
	    {0, 0, ro, 1}});
    }
    public static Matrix buildPerspectiveMatrix(double d, double ar) {
	return new Matrix(new double[][]{
	    {d, 0, 0, 0},
	    {0, d*ar, 0, 0},
	    {0, 0, 1, 1},
	    {0, 0, 0, 0}});
    }

    //////////////////////////////////////////////////
    public static Matrix buildPerspectToScreenMatrix(double width, double height) {
	double alpha = (0.5 * width - 0.5);
	double beta  = (0.5 * height - 0.5);
	return new Matrix(new double[][]{
	    {alpha, 0, 0, 0},
	    {0, -beta, 0, 0},
	    {alpha, beta, 1, 0},
	    {0, 0, 0, 1}});
    }
     
    public static Matrix buildCameraToScreenMatrix(double width, double height, double dist) {
	double alpha = (0.5 * width - 0.5);
	double beta  = (0.5 * height - 0.5);
	return new Matrix(new double[][]{
	    {dist, 0, 0, 0},
	    {0, -dist, 0, 0},
	    {alpha, beta, 1, 0},
	    {0, 0, 0, 1}});
    }   
          
    //////////////////////////////////////////////////
     public static Matrix buildUVNMatrix(Vector3D u, Vector3D v, Vector3D n) {
	return new Matrix(new double[][]{
	    {u.getX(), v.getX(), n.getX(), 0},
	    {u.getY(), v.getY(), n.getY(), 0},
	    {u.getZ(), v.getZ(), n.getZ(), 0},
	    {0, 0, 0, 1}});
    }
     
    //////////////////////////////////////////////////
    public void multiply(double s) {
	for (int i = 0; i < rows; i++) {
	    for (int j = 0; j < cols; j++) {
		m[i][j] *= s;
	    }
	}
    }

    public Matrix multiply(Matrix other) {
	if (other == null) {
	    return null;
	}
	if (other.rows != cols) {
	    throw new RuntimeException("Столбцов 1 матрицы != строк 2 матрицы (размеры матриц не совпадают)");
	}
	double[][] res = new double[rows][other.cols];
	for (int i = 0; i < rows; i++) {
	    for (int j = 0; j < other.cols; j++) {
		for (int k = 0; k < cols; k++) {
		    res[i][j] += m[i][k] * other.m[k][j];
		}
	    }
	}
	return new Matrix(res);
    }
    
    //////////////////////////////////////////////////
    // возвращает вектор-СТРОКУ
    public final double[] applyTransform(double[] vector) {
	final int sizeVector = vector.length;
	if (sizeVector != rows) {
	    throw new RuntimeException("Строк матрицы != размеру вектора");
	}
	double[] res = new double[sizeVector];
	for (int i = 0; i < cols; i++) {
	    for (int j = 0; j < rows; j++) {
		res[i] += vector[j] * m[j][i];
	    }
	}
	return res;
    }

    //////////////////////////////////////////////////
    public void reset() {
	for (int i = 0; i < rows; i++) {
	    for (int j = 0; j < cols; j++) {
		int v = (i == j) ? 1 : 0;
		m[i][j] = v;
	    }
	}
    }

    //////////////////////////////////////////////////
    public double[][] getMatrix() {
	return m;
    }
    
    public double getAt(int i, int j) {
	return m[i][j];
    }
    
    public void setAt(int i, int j, double value) {
	m[i][j] = value;
    }
    
    public Point3DOdn getTranslate() {
	return new Point3DOdn(m[3][0], m[3][1], m[3][2]);
    }
}
