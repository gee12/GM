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
	Z,
        None
    }
    private static final int DEF_SIZE = 4;
    private double[][] m;
    private int rows;
    private int cols;

    public Matrix() {
	reinit();
    }
    
    public Matrix(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        m = new double[rows][];
        for (int i = 0; i < rows; i++) {
            m[i] = new double[cols];
        }
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
    public static Matrix rotationMatrix(double angle, AXIS axis) {
	switch (axis) {
	    case X: 
		return Matrix.rotationXMatrix(angle);
	    case Y:
		return Matrix.rotationYMatrix(angle);
	    case Z:
		return Matrix.rotationZMatrix(angle);
	    default: return new Matrix();
	}
    }
    
    public static Matrix rotationZMatrix(double a) {
	double cosA = Math.cos(a);
        double sinA = Math.sin(a);
	Matrix m = new Matrix(new double[][] {
		{cosA, sinA, 0, 0},
		{-sinA, cosA, 0, 0},
		{0, 0, 1, 0},
		{0, 0, 0, 1}});
        return m;
    }

    public static Matrix rotationYMatrix(double a) {
	double cosA = Math.cos(a);
        double sinA = Math.sin(a);
	Matrix m = new Matrix(new double[][] {
		{cosA, 0, -sinA, 0},
		{0, 1, 0, 0},
		{sinA, 0, cosA, 0},
		{0, 0, 0, 1}});
        return m;
    }

    public static Matrix rotationXMatrix(double a) {
        double cosA = Math.cos(a);
        double sinA = Math.sin(a);
	Matrix m = new Matrix(new double[][] {
		{1, 0, 0, 0},
		{0, cosA, sinA, 0},
		{0, -sinA, cosA, 0},
		{0, 0, 0, 1}});
        return m;
    }
    
    //////////////////////////////////////////////////
    public static Matrix rotationTransMatrix(double angle, Point3D p0, AXIS axis) {
	switch (axis) {
	    case X: 
		return Matrix.rotationTransXMatrix(angle, p0);
	    case Y:
		return Matrix.rotationTransYMatrix(angle, p0);
	    case Z:
		return Matrix.rotationTransZMatrix(angle, p0);
	    default: return new Matrix();
	}
    }
    public static Matrix rotationTransZMatrix(double a, Point3D p0) {
        double cosA = Math.cos(a);
        double sinA = Math.sin(a);
	Matrix m = new Matrix(new double[][] {
		{cosA, sinA, 0, 0},
		{-sinA, cosA, 0, 0},
		{0, 0, 1, 0},
		{p0.getX()*(1-cosA)+p0.getY()*sinA,
		    p0.getY()*(1-cosA)-p0.getX()*sinA, 0, 1}});
        return m;
    }

    public static Matrix rotationTransYMatrix(double a, Point3D p0) {
        double cosA = Math.cos(a);
        double sinA = Math.sin(a);
	Matrix m = new Matrix(new double[][] {
		{cosA, 0, -sinA, 0},
		{0, 1, 0, 0},
		{sinA, 0, cosA, 0},
		{p0.getX()*(1-cosA)+p0.getY()*sinA,
		    p0.getY()*(1-cosA)-p0.getX()*sinA, 0, 1}});
        return m;
    }

    public static Matrix rotationTransXMatrix(double a, Point3D p0) {
        double cosA = Math.cos(a);
        double sinA = Math.sin(a);
	Matrix m = new Matrix(new double[][] {
		{1, 0, 0, 0},
		{0, cosA, sinA, 0},
		{0, -sinA, cosA, 0},
		{p0.getX()*(1-cosA)+p0.getY()*sinA,
		    p0.getY()*(1-cosA)-p0.getX()*sinA, 0, 1}});
        return m;
    }    
    
    //////////////////////////////////////////////////
    public static Matrix tranlateMatrix(double tx, double ty, double tz) {
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
    public static Matrix scaleMatrix(double sx, double sy, double sz) {
	return new Matrix(new double[][]{
	    {sx, 0, 0, 0},
	    {0, sy, 0, 0},
	    {0, 0, sz, 0},
	    {0, 0, 0, 1}});
    }
    
    public static Matrix scaleMatrix(double s) {
	return new Matrix(new double[][]{
	    {1, 0, 0, 0},
	    {0, 1, 0, 0},
	    {0, 0, 1, 0},
	    {0, 0, 0, s}});
    }
    
    //////////////////////////////////////////////////
    public static Matrix viewMatrix(double ro, double theta, double phi) {
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);
	Matrix m = new Matrix(new double[][]{
	    {-sinTheta, -(cosPhi * cosTheta), -(sinPhi * cosTheta), 0},
	    {cosTheta, -(cosPhi * cosTheta), -(sinPhi * sinTheta), 0},
	    {0, sinPhi, -cosPhi, 0},
	    {0, 0, ro, 1}});
        return m;
    }
    
    //////////////////////////////////////////////////
    public static Matrix perspectMatrix(double d, double ar) {
	return new Matrix(new double[][]{
	    {d, 0, 0, 0},
	    {0, d*ar, 0, 0},
	    {0, 0, 1, 1},
	    {0, 0, 0, 0}});
    }

    //////////////////////////////////////////////////
    public static Matrix perspectToScreenMatrix(double width, double height) {
	double alpha = (0.5 * width - 0.5);
	double beta  = (0.5 * height - 0.5);
	return new Matrix(new double[][]{
	    {alpha, 0, 0, 0},
	    {0, -beta, 0, 0},
	    {alpha, beta, 1, 0},
	    {0, 0, 0, 1}});
    }
     
    public static Matrix cameraToScreenMatrix(double width, double height, double dist) {
	double alpha = (0.5 * width - 0.5);
	double beta  = (0.5 * height - 0.5);
	return new Matrix(new double[][]{
	    {dist, 0, 0, 0},
	    {0, -dist, 0, 0},
	    {alpha, beta, 1, 0},
	    {0, 0, 0, 1}});
    }   
          
    //////////////////////////////////////////////////
     public static Matrix UVNMatrix(Vector3D u, Vector3D v, Vector3D n) {
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
    
    public Matrix multiplyConst(double s) {
	for (int i = 0; i < rows; i++) {
	    for (int j = 0; j < cols; j++) {
		m[i][j] *= s;
	    }
	}
        return new Matrix(m);
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
	/*double[][] res =  new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res[i][j] = m[i][j];
            }
        }
        return res;*/
        return m;
    }
    
    public double getAt(int i, int j) {
	return m[i][j];
    }
    
    public void setAt(int i, int j, double value) {
	m[i][j] = value;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
    
    public Point3DOdn getTranslate() {
	return new Point3DOdn(m[3][0], m[3][1], m[3][2]);
    }
}
