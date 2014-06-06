package com.bondar.gm;

import com.bondar.gm.Matrix.AXIS;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class Solid3D {

    private Point3DOdn[] vertexes;
    private Triangle3D[] triangles;
    private DrawOrMove[] doms;

    /////////////////////////////////////////////////////////
    public Solid3D(Point3DOdn[] vertexes, Triangle3D[] triangles, DrawOrMove[] doms) {
	this.vertexes = vertexes;
	this.triangles = triangles;
	this.doms = doms;
    }

    public Solid3D(Point3DOdn[] vertexes, Triangle3D[] triangles) {
	this.vertexes = vertexes;
	this.triangles = triangles;
	this.doms = createDrawOrMoves(vertexes);
    }

    public Solid3D(Point3DOdn[] vertexes) {
	this.vertexes = vertexes;
	this.triangles = createTriangles(vertexes);
	this.doms = createDrawOrMoves(vertexes);
    }

    /////////////////////////////////////////////////////////
    // triangulation
    public static Triangle3D[] createTriangles(Point3DOdn[] vertexes) {
	if (vertexes == null) {
	    return null;
	}
	//
	int triasCount = 1;
	Triangle3D[] res = null;//new Triangle3D[triasCount];

	// триангуляция

	return res;
    }

    /////////////////////////////////////////////////////////
    // 
    public static DrawOrMove[] createDrawOrMoves(Point3DOdn[] points) {
	if (points == null) {
	    return null;
	}
	int size = points.length;
	int arraySize = size * (size - 1);
	DrawOrMove[] res = new DrawOrMove[arraySize];
	int ind = 0;
	for (int i = 0; i < size-1; i++) {
	    for (int j = i+1; j < size; j++) {
		res[ind] = new DrawOrMove(points[i], DrawOrMove.Operation.MOVE);
		res[ind+1] = new DrawOrMove(points[j], DrawOrMove.Operation.DRAW);
		ind += 2;
	    }
	}
	return res;
    }

    /////////////////////////////////////////////////////////
    // clipping
    public Triangle3D[] getVisibleTriangles(Point3DOdn camera) {
	return Solid3D.getVisibleTriangles(triangles, camera);
    }

    public static Triangle3D[] getVisibleTriangles(Triangle3D[] trias, Point3DOdn camera) {
	if (trias == null || camera == null) {
	    return null;
	}
	//
	List<Triangle3D> res = new ArrayList();

	// отбор видимых граней

	return trias;//res.toArray(new Triangle3D[res.size()]);
    }

    /////////////////////////////////////////////////////////
    public static Solid3D readFromFile(String svcFileName)
	    throws IOException {
	BufferedReader reader = new BufferedReader(
		new InputStreamReader(new FileInputStream(svcFileName), "UTF-8"));
	// Вершины
	int vertCount = Integer.parseInt(reader.readLine());
	Point3DOdn[] vertexes = new Point3DOdn[vertCount];
	String line = null;
	int lineNumber = 0;
	while (lineNumber < vertCount && (line = reader.readLine()) != null) {
	    String[] coordsStr = line.split(",");
	    int coordsCount = coordsStr.length;
	    if (coordsCount < 3) {
		throw new RuntimeException("Необходимо 3 координаты");
	    }
	    double[] coords = new double[coordsCount];
	    for (int i = 0; i < coordsCount; i++) {
		coords[i] = Double.parseDouble(coordsStr[i]);
	    }
	    vertexes[lineNumber] = new Point3DOdn(coords[0], coords[1], coords[2]);
	    lineNumber++;
	}
	// Треугольники по 3-м вершинам
	int triasCount = Integer.parseInt(reader.readLine());
	Triangle3D[] triangles = new Triangle3D[triasCount];
	lineNumber = 0;
	while (lineNumber < triasCount && (line = reader.readLine()) != null) {
	    String[] pointsStr = line.split(",");
	    int pointsCount = pointsStr.length;
	    if (pointsCount < 3) {
		throw new RuntimeException("Необходимо 3 вершины");
	    }
	    int[] points = new int[pointsCount];
	    for (int i = 0; i < pointsCount; i++) {
		points[i] = Integer.parseInt(pointsStr[i]);
	    }
	    triangles[lineNumber] = new Triangle3D(
		    vertexes[points[0]],
		    vertexes[points[1]],
		    vertexes[points[2]]);
	    lineNumber++;
	}

	// Отображение ребер
	int domCount = Integer.parseInt(reader.readLine());
	DrawOrMove[] domPoints = new DrawOrMove[domCount];
	lineNumber = 0;
	while (lineNumber < domCount && (line = reader.readLine()) != null) {
	    String[] domStr = line.split(",");
	    int elemCount = domStr.length;
	    if (elemCount < 2) {
		throw new RuntimeException("Необходимо 2 числа (идентификатор точки и тип операции)");
	    }
	    int pointNum = Integer.parseInt(domStr[0]);
	    int domOper = Integer.parseInt(domStr[1]);
	    domPoints[lineNumber] = new DrawOrMove(
		    vertexes[pointNum],
		    DrawOrMove.Operation.values()[domOper]);
	    lineNumber++;
	}

	reader.close();

	return new Solid3D(vertexes, triangles, domPoints);
    }

    /////////////////////////////////////////////////////////
    // operations
    public void translate(double tx, double ty, double tz) {
	Matrix transMatrix = Matrix.getTransferMatrix(tx, ty, tz);
	for (Point3DOdn point : vertexes) {
	    point.multiply(transMatrix);
	}
    }

    public void rotate(double angle, AXIS axis) {
	Matrix rotateMatrix = Matrix.getRotationMatrix(angle, axis);
	for (Point3DOdn point : vertexes) {
	    point.multiply(rotateMatrix);
	}
    }

    public void scale(double sx, double sy, double sz) {
	Matrix scaleMatrix = Matrix.getScaleMatrix(sx, sy, sz);
	for (Point3DOdn point : vertexes) {
	    point.multiply(scaleMatrix);
	}
    }

    public void scale(double s) {
	Matrix scaleMatrix = Matrix.getScaleMatrix(s);
	for (Point3DOdn point : vertexes) {
	    point.multiply(scaleMatrix);
	}
    }

    public void perspective1(double p) {
	Matrix perspMatrix = Matrix.getPerspective1Matrix(p);
	for (Point3DOdn point : vertexes) {
	    point.multiply(perspMatrix);
	}
    }

    public void perspective2(double p1, double p2) {
	Matrix perspMatrix = Matrix.getPerspective1Matrix(p1, p2);
	for (Point3DOdn point : vertexes) {
	    point.multiply(perspMatrix);
	}
    }

    public void perspective(double dist, double ro, double theta, double phi) {
	Matrix V = Matrix.getViewMatrix(ro, theta, phi);
	for (Point3DOdn vertex : vertexes) {
	    /*double xe = V.getMatrix()[0][0] * point.getX() + V.getMatrix()[1][0] * point.getY();
	     double ye = V.getMatrix()[0][1] * point.getX() + V.getMatrix()[1][1] * point.getY() + V.getMatrix()[2][1] * point.getZ();
	     double ze = V.getMatrix()[0][2] * point.getX() + V.getMatrix()[1][2] * point.getY() + V.getMatrix()[2][2] * point.getZ() + V.getMatrix()[3][2];
	     */
	    vertex.multiply(V);
	    double xe = vertex.getX();
	    double ye = vertex.getY();
	    double ze = vertex.getZ();
	    vertex.setX(dist * xe / ze);
	    vertex.setY(dist * ye / ze);
	}
    }

    /////////////////////////////////////////////////////////
    // get
    public int getSize() {
	return vertexes.length;
    }

    public Point3DOdn getVertex(int i) {
	if (i >= vertexes.length || i < 0) {
	    return null;
	}
	return vertexes[i];
    }

    public Point3DOdn[] getVertexes() {
	return vertexes;
    }

    public Point2D[] getVertexes2D() {
	Point2D[] res = new Point2D[getSize()];
	for (int i = 0; i < getSize(); i++) {
	    res[i] = vertexes[i].toPoint2D();
	}
	return res;
    }

    public Triangle3D[] getTriangles() {
	return triangles;
    }
    
    public DrawOrMove[] getDoms() {
	return doms;
    }
}
