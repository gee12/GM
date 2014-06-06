package com.bondar.gm;

import com.bondar.gm.Matrix.AXIS;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class Solid3D {

    private Point3DOdn[] vertexes;
    private int[][] indsToTrias;
    private Triangle3D[] triangles = null;
    private DrawOrMove[] doms;
    private String name;
    
    private double angleX, angleY, angleZ;
    private double dx, dy, dz;
    private double scale;
    private boolean isNeedPerspective = false;
    private double dist, ro, theta, phi;

    /////////////////////////////////////////////////////////
    public Solid3D(String name, Point3DOdn[] vertexes, int[][] indsToTrias/* Triangle3D[] triangles,*/, DrawOrMove[] doms) {
	this.name = name;
	this.vertexes = vertexes;
	this.triangles = getTriasFromVerts(vertexes);
	this.doms = doms;
    }

    public Solid3D(String name, Point3DOdn[] vertexes, int[][] indsToTrias/*Triangle3D[] triangles*/) {
	this.name = name;
	this.vertexes = vertexes;
	this.triangles = getTriasFromVerts(vertexes);
	this.doms = createDrawOrMoves(vertexes);
    }

    public Solid3D(String name, Point3DOdn[] vertexes) {
	this.name = name;
	this.vertexes = vertexes;
	this.triangles = getTriasFromVerts(vertexes);
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
    /*public Triangle3D[] getVisibleTriangles(Point3DOdn camera) {
	return Solid3D.getVisibleTriangles(triangles, camera);
    }*/

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
    public static Solid3D readFromFile(String fileName)
	    throws IOException {
	BufferedReader reader = new BufferedReader(
		new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
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
	//Triangle3D[] triangles = new Triangle3D[triasCount];
	int[][] indsToTrias = new int[triasCount][];
	lineNumber = 0;
	while (lineNumber < triasCount && (line = reader.readLine()) != null) {
	    String[] pointsStr = line.split(",");
	    int pointsCount = pointsStr.length;
	    if (pointsCount < 3) {
		throw new RuntimeException("Необходимо 3 вершины");
	    }
	    indsToTrias[lineNumber] = new int[3];
	    for (int i = 0; i < pointsCount; i++) {
		indsToTrias[lineNumber][i] = Integer.parseInt(pointsStr[i]);
	    }
	    /*int[] points = new int[pointsCount];
	    for (int i = 0; i < pointsCount; i++) {
		points[i] = Integer.parseInt(pointsStr[i]);
	    }
	    triangles[lineNumber] = new Triangle3D(
		    // по ссылке (!)
		    vertexes[points[0]],
		    vertexes[points[1]],
		    vertexes[points[2]]);*/
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

	// get file name without path & extension
	Path path = Paths.get(fileName);
	String withoutPath = path.getFileName().toString();
	String withoutExt = withoutPath.replaceFirst("[.][^.]+$", "");
	return new Solid3D(withoutExt, vertexes, indsToTrias, domPoints);
    }
    
    private Triangle3D[] getTriasFromVerts(Point3DOdn[] vertexes) {
	if (vertexes == null) return null;
	int size = vertexes.length;
	Triangle3D[] trias = new Triangle3D[size];
	for (int i = 0; i < size; i++) {
	    trias[i].setTriangle(
		    vertexes[indsToTrias[i][0]],
		    vertexes[indsToTrias[i][1]],
		    vertexes[indsToTrias[i][2]]);
	}
	return trias;
    }
    
    public Triangle3D[] setTriasFromVerts(Point3DOdn[] vertexes) {
	this.triangles = getTriasFromVerts(vertexes);
	return this.triangles;
    }

    /////////////////////////////////////////////////////////
    // operations
    public Point3DOdn[] makeTransformations() {
	Matrix rotateXM = Matrix.getRotationMatrix(angleX, AXIS.X);
	Matrix rotateYM = Matrix.getRotationMatrix(angleY, AXIS.Y);
	Matrix rotateZM = Matrix.getRotationMatrix(angleZ, AXIS.Z);
	Matrix transM = Matrix.getTransferMatrix(dx, dy, dz);
	Matrix scaleM = Matrix.getScaleMatrix(scale, scale, scale);
	Matrix viewM = Matrix.getViewMatrix(ro, theta, phi);
	
	int size = vertexes.length;
	Point3DOdn[] res = new Point3DOdn[size];
	for (int i = 0; i < size; i++) {
	    Point3DOdn v = vertexes[i].getCopy();
	    v.multiply(rotateXM).multiply(rotateYM).multiply(rotateZM).
		    multiply(transM).multiply(scaleM);
	    if (isNeedPerspective) {
		v.multiply(viewM);
		v.setX(dist * v.getX() / v.getZ());
		v.setY(dist * v.getY() / v.getZ());
	    }
	    res[i] = v;
	}
	return res;
    }
    /*
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
	    //double xe = V.getMatrix()[0][0] * point.getX() + V.getMatrix()[1][0] * point.getY();
	    // double ye = V.getMatrix()[0][1] * point.getX() + V.getMatrix()[1][1] * point.getY() + V.getMatrix()[2][1] * point.getZ();
	    // double ze = V.getMatrix()[0][2] * point.getX() + V.getMatrix()[1][2] * point.getY() + V.getMatrix()[2][2] * point.getZ() + V.getMatrix()[3][2];
	     
	    vertex.multiply(V);
	    double xe = vertex.getX();
	    double ye = vertex.getY();
	    double ze = vertex.getZ();
	    vertex.setX(dist * xe / ze);
	    vertex.setY(dist * ye / ze);
	}
    }*/

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
    
    public String getName() {
	return name;
    }
}
