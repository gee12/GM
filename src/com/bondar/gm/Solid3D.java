package com.bondar.gm;

import com.bondar.gm.Matrix.AXIS;
import static com.bondar.gm.Matrix.AXIS.X;
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
    private Color edgesColor;
    
    private double angleX, angleY, angleZ;
    private double dx, dy, dz;
    private double scale;
    private boolean isNeedPerspective = false;
    private double dist, ro, theta, phi;
    
    public void resetTransformations() {
	angleX = angleY = angleZ = 0;
	dx = dy = dz = 0;
	scale = 1;
    }
    
    public void updateAngle(double a, Matrix.AXIS axis) {
	switch (axis) {
	    case X: this.angleX += a;
		break;
	    case Y: this.angleY += a;
		break;
	    case Z: this.angleZ += a;
		break;
	}
    }
    
    public void updateAngles(double ax, double ay, double az) {
	this.angleX += ax;
	this.angleY += ay;
	this.angleZ += az;
    }
    
    public void updateTransfers(double dx, double dy, double dz) {
	this.dx += dx;
	this.dy += dy;
	this.dz += dz;
    }
    
    public void updateScale(double scale) {
	this.scale += scale;
    }
    
    public void setPerspective(boolean isNeedPerspective) {
	this.isNeedPerspective = isNeedPerspective;
    }
    
    public void setPerspective(double d, double ro, double theta, double phi) {
	this.dist += d;
	this.ro += ro;
	this.theta = theta;
	this.phi += phi;
    }

    /////////////////////////////////////////////////////////
    public Solid3D(String name, Point3DOdn[] vertexes, int[][] indsToTrias, DrawOrMove[] doms) {
	this.name = name;
	this.vertexes = vertexes;
	this.indsToTrias = indsToTrias;
	this.triangles = createTriangles(vertexes, indsToTrias);
	this.doms = doms;
	resetTransformations();
    }

    public Solid3D(String name, Point3DOdn[] vertexes, int[][] indsToTrias) {
	this.name = name;
	this.vertexes = vertexes;
	this.indsToTrias = indsToTrias;
	this.triangles = createTriangles(vertexes, indsToTrias);
	this.doms = createDrawOrMoves(vertexes);
	resetTransformations();
    }

    public Solid3D(String name, Point3DOdn[] vertexes) {
	this.name = name;
	this.vertexes = vertexes;
	this.indsToTrias = createIndsForTrias(vertexes);
	this.triangles = createTriangles(vertexes, indsToTrias);
	this.doms = createDrawOrMoves(vertexes);
	resetTransformations();
    }

    /////////////////////////////////////////////////////////
    // triangulation
    public static int[][] createIndsForTrias(Point3DOdn[] vertexes) {
	if (vertexes == null) return null;
	int n = vertexes.length;
	// combination from n to 3
	int triasCount = n*(n-1)*(n-2)/6;
	int[][] res = new int[triasCount][3];
	// simple triangulation (with trias inside polygon & intersecting trias)
	int ind = 0;
	for (int i = 0; i < n-2; i++) {
	    for (int j = i+1; j < n-1; j++) {
		for (int k = j+1; k < n; k++,ind++) {
		    res[ind] = new int[] {i,j,k};
		}
	    }
	}
	return res;
    }
   
    private static Triangle3D[] createTriangles(Point3DOdn[] vertexes, int[][] indsToTrias) {
	return createTriasFromVerts(vertexes, indsToTrias, null);
    }
    
    public static Triangle3D[] createTriasFromVerts(Point3DOdn[] vertexes, int[][] indsToTrias, Triangle3D[] oldTrias) {
	if (vertexes == null || indsToTrias == null) return null;
	int size = indsToTrias.length;
	Triangle3D[] res = new Triangle3D[size];
	for (int i = 0; i < size; i++) {
	    Color color = (oldTrias != null && oldTrias.length >= size) ? 
		    oldTrias[i].getColor() : Color.BLACK;
	    res[i] = new Triangle3D(
		    vertexes[indsToTrias[i][0]],
		    vertexes[indsToTrias[i][1]],
		    vertexes[indsToTrias[i][2]],
		    color);
	}
	return res;
    }
    
    public  Triangle3D[] setTrianglesVertexes(Point3DOdn[] vertexes) {
	if (vertexes == null) return null;
	for (int i = 0; i < indsToTrias.length; i++) {
	    triangles[i].setTriangle(
		    vertexes[indsToTrias[i][0]],
		    vertexes[indsToTrias[i][1]],
		    vertexes[indsToTrias[i][2]]);
	}
	return triangles;
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
		res[ind] = new DrawOrMove(i, DrawOrMove.Operation.MOVE);
		res[ind+1] = new DrawOrMove(j, DrawOrMove.Operation.DRAW);
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
	    throws Exception {
	BufferedReader reader = new BufferedReader(
		new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
	// get file name without path & extension
	Path path = Paths.get(fileName);
	String withoutPath = path.getFileName().toString();
	String withoutExt = withoutPath.replaceFirst("[.][^.]+$", "");
	/////////////////////
	// vertexes
	int vertCount = Integer.parseInt(reader.readLine());
	Point3DOdn[] vertexes = new Point3DOdn[vertCount];
	String line = null;
	int lineNumber = 0;
	while (lineNumber < vertCount) {
	    if ((line = reader.readLine()) == null) {
		throw new RuntimeException("Неверное количество кординат в " + fileName);
	    }
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
	/////////////////////
	// triangles vertex indexes
	if ((line = reader.readLine()) == null) {
	    return new Solid3D(withoutExt, vertexes);
	}
	int triasCount = Integer.parseInt(line);
	int[][] indsToTrias = new int[triasCount][];
	lineNumber = 0;
	while (lineNumber < triasCount) {
	    if ((line = reader.readLine()) == null) {
		throw new RuntimeException("Неверное количество индексов координат треугольников в " + fileName);
	    }
	    String[] pointsStr = line.split(",");
	    int pointsCount = pointsStr.length;
	    if (pointsCount < 3) {
		throw new RuntimeException("Необходимо 3 вершины");
	    }
	    indsToTrias[lineNumber] = new int[pointsCount];
	    for (int i = 0; i < pointsCount; i++) {
		indsToTrias[lineNumber][i] = Integer.parseInt(pointsStr[i]);
	    }
	    lineNumber++;
	}
	/////////////////////
	// strtucture for edges drawing
	if ((line = reader.readLine()) == null) {
	    return new Solid3D(withoutExt, vertexes, indsToTrias);
	}
	int domCount = Integer.parseInt(line);
	DrawOrMove[] domPoints = new DrawOrMove[domCount];
	lineNumber = 0;
	while (lineNumber < domCount) {
	    if ((line = reader.readLine()) == null) {
		throw new RuntimeException("Неверное количество DrawOrMove элементов в " + fileName);
	    }
	    String[] domStr = line.split(",");
	    int elemCount = domStr.length;
	    if (elemCount < 2) {
		throw new RuntimeException("Необходимо 2 числа (идентификатор точки и тип операции)");
	    }
	    int pointIndex = Integer.parseInt(domStr[0]);
	    int domOper = Integer.parseInt(domStr[1]);
	    domPoints[lineNumber] = new DrawOrMove(
		    pointIndex,
		    DrawOrMove.Operation.values()[domOper]);
	    lineNumber++;
	}
	reader.close();
	//
	return new Solid3D(withoutExt, vertexes, indsToTrias, domPoints);
    }
 
    /////////////////////////////////////////////////////////
    // operations
    public Point3DOdn[] makeTransformations() {
	// create matrixes
	Matrix rotateXM = Matrix.getRotationMatrix(angleX, AXIS.X);
	Matrix rotateYM = Matrix.getRotationMatrix(angleY, AXIS.Y);
	Matrix rotateZM = Matrix.getRotationMatrix(angleZ, AXIS.Z);
	Matrix transM = Matrix.getTransferMatrix(dx, dy, dz);
	Matrix scaleM = Matrix.getScaleMatrix(scale, scale, scale);
	Matrix viewM = Matrix.getViewMatrix(ro, theta, phi);
	
	int size = vertexes.length;
	Point3DOdn[] res = new Point3DOdn[size];
	// transform all vertexes
	for (int i = 0; i < size; i++) {
	    Point3DOdn v = vertexes[i].getCopy();
	    v.multiply(rotateXM).multiply(rotateYM).multiply(rotateZM).
		    multiply(transM).multiply(scaleM);
	    // perspective
	    if (isNeedPerspective) {
		v.multiply(viewM);
		v.setX(dist * v.getX() / v.getZ());
		v.setY(dist * v.getY() / v.getZ());
	    }
	    res[i] = v;
	}
	return res;
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setEdgesColor(Color col) {
	edgesColor = col;
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

    public static Point2D[] getVertexes2D(Point3DOdn[] vertexes) {
	int size = vertexes.length;
	Point2D[] res = new Point2D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[i].toPoint2D();
	}
	return res;
    }
    
    public Point2D[] getVertexes2D() {
	return getVertexes2D(vertexes);
    }

    public Triangle3D[] getTriangles() {
	return triangles;
    }
    
    public DrawOrMove[] getDoms() {
	return doms;
    }
    
    public int[][] getIndsToTrias() {
	return indsToTrias;
    }
    
    public String getName() {
	return name;
    }
    
    public Color getEdgesColor() {
	return (edgesColor != null) ? edgesColor : Color.BLACK;
    }
    
    public boolean isPerspective() {
	return isNeedPerspective;
    }
}
