package com.bondar.gm;

import com.bondar.geom.ClipPolygon2D;
import com.bondar.geom.ClipRectangle2D;
import com.bondar.geom.Point2D;
import com.bondar.geom.Line2D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.gm.Matrix.AXIS;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author bondar
 */
public class GraphicSystem2D {
    
    public static enum ClipTypes {
        None,
        Rect,
        Poly
    }

    private static int TONE = 240;
    public static Color BACK_COLOR = new Color(TONE,TONE,TONE);
    public static Color DEF_COLOR = Color.BLACK;
    public static final double MAX_POINT = 10;
    public static final double X_MAX = 10;
    public static final double Y_MAX = 7;
    public static final double X_MIN = 0;
    public static final double Y_MIN = 0;
    public static final double BORDER = 1;
    private Graphics g;
    private int width, height;
    private Point2D old;
    private Matrix transMatrix;
    private ClipRectangle2D clipRect;
    private ClipPolygon2D clipPoly;
    private ClipTypes clipType;
    private boolean isNeedScale;
    private double xMin, xMax, yMin, yMax;
    private double xc, yc, Xc, Yc;
    private double f;
    private double c1, c2;
    private ZBufferDrawManager zBuffer;

    public GraphicSystem2D() {
	old = new Point2D();
	transMatrix = new Matrix();
	clipType = ClipTypes.None;
	resetScale();
    }

    //////////////////////////////////////////////////
    public void setGraphics(Graphics g) {
	this.g = g;
	this.width = g.getClipBounds().width;
	this.height = g.getClipBounds().height;

	drawBackground(Color.WHITE);
    }
    
    public void clear() {
	g.setColor(BACK_COLOR);
	g.fillRect(convXToScreen(0), convYToScreen(Y_MAX), convXToScreen(X_MAX), convYToScreen(0));
	g.setColor(DEF_COLOR);
    }
    
    public void drawBackground(Color col) {
	g.setColor(col);
	g.fillRect(0, 0, width, height);
	g.setColor(Color.BLACK);
    }
    
    //////////////////////////////////////////////////
    // Установка прямоугольного окна отсечения (в координатах X_MAX и Y_MAX)
    public void setClipRectangle(int xmin, int ymin, int xmax, int ymax) {
	if (!isCorrectXCoord(xmin) || !isCorrectXCoord(xmax)
		|| !isCorrectYCoord(ymin) || !isCorrectYCoord(ymax)
		|| xmin > xmax || ymin > ymax) {
	    return;
	}
	clipRect = new ClipRectangle2D(xmin, ymin, xmax, ymax);
    }

    // Установка поточечного окна отсечения (в координатах X_MAX и Y_MAX)
    public void setClipPolygon(Point2D[] points) {
	if (points == null) {
	    return;
	}
	for (int i = 0; i < points.length; i++) {
	    if (!isCorrectPoint(points[i])) {
		return;
	    }
	}
	clipPoly = new ClipPolygon2D(points);
    }

    //////////////////////////////////////////////////
    // Установка масштабирования
    public void setScale(boolean isNeedScale) {
	this.isNeedScale = isNeedScale;
	resetScale();
    }

    public void setScale(double x, double y) {
	double[] vector4 = transMatrix.applyTransform(new Point2D(x, y).toArray4Odn());
	x = vector4[0];
	y = vector4[1];
	if (x < xMin) {
	    xMin = x;
	}
	if (x > xMax) {
	    xMax = x;
	}
	if (y < yMin) {
	    yMin = y;
	}
	if (y > yMax) {
	    yMax = y;
	}
	//
	double dx = xMax - xMin;
	double dy = yMax - yMin;
	double Dx = X_MAX - X_MIN;
	double Dy = Y_MAX - Y_MIN;
	double fx = (dx != 0) ? (Dx / dx) : Double.MAX_VALUE;
	double fy = (dy != 0) ? (Dy / dy) : Double.MAX_VALUE;
	f = (fx < fy) ? fx : fy;
	//
	xc = 0.5 * (xMax + xMin);
	yc = 0.5 * (yMax + yMin);
	Xc = 0.5 * (X_MAX + X_MIN);
	Yc = 0.5 * (Y_MAX + Y_MIN);
	//
	c1 = Xc - f * xc;
	c2 = Yc - f * yc;
    }

    public void resetScale() {
	xMin = yMin = Double.MAX_VALUE;
	xMax = yMax = Double.MIN_VALUE;
    }

    //////////////////////////////////////////////////
    // Работа с матрицей преобразования
    public void reset() {
	transMatrix.reset();
    }

    public void rotate(double angle, AXIS axis) {
	transMatrix = transMatrix.multiply(Matrix.rotationMatrix(angle, axis));
    }

    public void translate(double tx, double ty) {
	transMatrix.translate(tx, ty);
    }

    public void scale(double value) {
	transMatrix.multiply(value);
    }

    public void setColor(Color color) {
	g.setColor(color);
    }

    //////////////////////////////////////////////////
    // Перемещение точки-указателя
    public void move(double x, double y) {
	old.setX(x);
	old.setY(y);
    }

    public void move(final Point3D p) {
	move(p.toPoint2D());
    }

    public void move(final Point2D p) {
	old = new Point2D(p);
    }

    //////////////////////////////////////////////////
    // Отрисовка линии из точки-указателя в указанную
    public void draw(double x, double y) {
	Point2D p = new Point2D(x, y);
	line(old, p);
	old = p;
    }

    public void draw(final Point3D p) {
	draw(p.toPoint2D());
    }

    public void draw(final Point2D p) {
	line(old, p);
	old = new Point2D(p);
    }
    
    //////////////////////////////////////////////////
    // Отрисовка строки
    public void drawString(String text, double x, double y) {
	Point3DOdn p = new Point3DOdn(transMatrix.applyTransform(new Point2D(x, y).toArray4Odn()));
	g.drawString(text, convXToScreen(p.getX()), convYToScreen(p.getY()));
    }

    //////////////////////////////////////////////////
    // Отрисовка линии по двум точкам
    public void line(double x1, double y1, double x2, double y2) {
	line(new Point2D(x1, x2), new Point2D(x2, y2));
    }

    public void line(Point3D from, Point3D to) {
	line(from.toPoint2D(), to.toPoint2D());
    }

    public void line(Point2D from, Point2D to) {
	Point2D p1 = new Point2D(transMatrix.applyTransform(from.toArray4Odn()));
	Point2D p2 = new Point2D(transMatrix.applyTransform(to.toArray4Odn()));
	Line2D line = new Line2D(p1, p2);
	// is need scale?
	if (isNeedScale) {
	    line = getScaleLine(line);
	}
	// is need clip?
	if (clipType != ClipTypes.None) {
	    line = getClipLine(line);
	}
	// is line visible?
	if (line.isVisible()) {
	    g.drawLine(convXToScreen(line.getP1().getX()), convYToScreen(line.getP1().getY()),
		    convXToScreen(line.getP2().getX()), convYToScreen(line.getP2().getY()));
	}
    }

    //////////////////////////////////////////////////
    // Отрисовка многоугольника
    public void fillPolygon(Point3D[] verts) {
	if (verts == null) {
	    return;
	}
	int size = verts.length;
	int xs[] = new int[size];
	int ys[] = new int[size];
	for (int i = 0; i < size; i++) {
	    double[] p = transMatrix.applyTransform(verts[i].toPoint3DOdn().toArray3());
	    xs[i] = convXToScreen(p[0]);
	    ys[i] = convYToScreen(p[1]);
	}
	g.fillPolygon(xs, ys, size);
    }
    
    /////////////////////////////////////////////////////
    //
    public void drawPolygonBorder(Point3D[] verts, Color color) {
	if (verts == null) return;
	setColor(color);
	int size = verts.length;
	for (int i = 0; i < size-1; i++) {
	    line(verts[i], verts[i+1]);
	}
	line(verts[size-1], verts[0]);
    }

    //////////////////////////////////////////////////
    // Пребразование линии с учетом масштабирования
    private Line2D getScaleLine(Line2D line) {
	return getScaleLine(line.getP1().getX(), line.getP1().getY(),
		line.getP2().getX(), line.getP2().getY());
    }

    private Line2D getScaleLine(Point2D p1, Point2D p2) {
	return new Line2D(getScalePoint(p1), getScalePoint(p2));
    }

    private Line2D getScaleLine(double x0, double y0, double x1, double y1) {
	return new Line2D(getScalePoint(x0, y0), getScalePoint(x1, y1));
    }

    private Point2D getScalePoint(Point2D p) {
	return getScalePoint(p.getX(), p.getY());
    }

    private Point2D getScalePoint(double oldX, double oldY) {
	double x = f * oldX + c1;
	double y = f * oldY + c2;
	return new Point2D(x, y);
    }

    //////////////////////////////////////////////////
    // Пребразование линии с учетом отсечения
    private Line2D getClipLine(double x0, double y0, double x1, double y1) {
	Line2D line = null;
	if (clipType == ClipTypes.Rect) {
	    line = clipRect.CSclip(x0, y0, x1, y1);
	} else if (clipType == ClipTypes.Poly) {
	    line = clipPoly.CBclip(x0, y0, x1, y1);
	} else {
	    line = new Line2D();
	}
	return line;
    }

    private Line2D getClipLine(Point2D p1, Point2D p2) {
	return getClipLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    private Line2D getClipLine(Line2D line) {
	return getClipLine(line.getP1().getX(), line.getP1().getY(),
		line.getP2().getX(), line.getP2().getY());
    }

    /////////////////////////////////////////////////////
    // Алгоритм отсечения невидимых граней с использованием z-буффера
    public void zBufferAlgorithm(Polygon3DVerts[] polies) {
	if (polies == null) {
	    return;
	}
	for (Polygon3DVerts poly : polies) {
	    switch(poly.getType()) {
		case POINT:
		    line(poly.getVertexPosition(0), poly.getVertexPosition(0));
		    break;
		case LINE:
		    line(poly.getVertexPosition(0), poly.getVertexPosition(1));
		    break;
		default:
//		    drawBufferedPolygon(poly);
		    break;
	    }
	}
    }


	
    public void drawScreenPoint(double x, double y, Color col) {
	g.setColor(col);
	g.drawLine((int)(x + 0.5), (int)(y + 0.5),
		(int)(x + 0.5), (int)(y + 0.5));	    
    }
    
    /////////////////////////////////////////////////////
    // проверки и преобразования
    public static double[] decartToSpherical(double x, double y, double z) {
	/*double ro = Math.sqrt(x * x + y * y + z * z);
	double theta = /*Math.atan(Math.sqrt(x*x+y*y)/z);//*/ /*Math.atan2(z, x);
	double phi = /*Math.atan(y/x);//*/ /*Math.atan2(y, Math.sqrt(x * x + z * z));*/
	/*double ro = Math.atan2(x,z)/6.28318f; 
	double theta = 0;
	if(ro < 0.0f) 
	    theta += 1.0f; 
	double phi = Math.atan2(Math.sqrt(x*x+z*z),y)/3.14159f; */
	double ro = (Math.sqrt(x*x + y*y + z*z)); 
	//double theta = Math.toDegrees(Math.asin(z/ro));
	double theta = Math.toDegrees(Math.atan(y/x)); // Find the value of 'θ'
	if(y<0 && x<0) { // Correct the value of 'θ' depending upon the quadrant.
	    theta += 180;
	}
	if(y>0 && x<0) { // Correct the value of 'θ' depending upon the quadrant.
	    theta += 180;
	}
	//double phi = Math.toDegrees(Math.atan2(y, x));
	double phi = Math.toDegrees(Math.acos(z/ro)); // Find the value of 'β'
	return new double[]{ro, theta, phi};
    }

    public boolean isCorrectXCoord(double x) {
	return (x >= 0 && x <= X_MAX);
    }

    public boolean isCorrectYCoord(double y) {
	return (y >= 0 && y <= Y_MAX);
    }

    public boolean isCorrectPoint(Point2D p) {
	return (isCorrectXCoord(p.getX())
		&& isCorrectYCoord(p.getY()));
    }

    /////////////////////////////////////////////////////
    // Convert to screen coordinates.
    public static int convXToScreen(double width, double x) {
	return (int) (x * (width / X_MAX) + BORDER);
    }

    private int convXToScreen(double x) {
	return GraphicSystem2D.convXToScreen(width, x);
    }

    public static int convYToScreen(double height, double y) {
	return (int) (height - (y * (height / Y_MAX) + BORDER));
    }

    private int convYToScreen(double y) {
	return GraphicSystem2D.convYToScreen(height, y);
    }

    public static Point3D convPToScreen(double width, double height, Point3D p) {
	return new Point3D(
		GraphicSystem2D.convXToScreen(width, p.getX()),
		GraphicSystem2D.convYToScreen(height, p.getY()),
		p.getZ());
    }
    
    public Point3D convPToScreen(Point3D p) {
	if (p == null) return null;
	Point3D trans = transMatrix.getTranslate();
	return new Point3D(
		(int) ((p.getX() + trans.getX()) * (width / X_MAX) + BORDER),
		(int) (height - ((p.getY() + trans.getY()) * (height / Y_MAX) + BORDER)),
		p.getZ());
    }

    /////////////////////////////////////////////////////
    // Convert back to world coordinates.
    public static Point3D convPToWorld(double width, double height, Point3D p) {
	if (width == 0 || height == 0) {
	    return null;
	}
	return new Point3D(
		(p.getX() - BORDER) * X_MAX / width,
		(BORDER + height - p.getY()) * Y_MAX / height,
		p.getZ());
    }

    public Point3D convPToWorld(Point3DOdn p) {
	Point3D trans = transMatrix.getTranslate();
	return new Point3D(
		(p.getX() - BORDER) * X_MAX / width - trans.getX() ,
		(BORDER + height - p.getY()) * Y_MAX / height - trans.getY(),
		p.getZ());
    }

    /////////////////////////////////////////////////////
    // get
    public ClipRectangle2D getClipWindow() {
	return clipRect;
    }

    public double getxMin() {
	return xMin;
    }

    public double getxMax() {
	return xMax;
    }

    public double getyMin() {
	return yMin;
    }

    public double getyMax() {
	return yMax;
    }
    
    public Graphics getGraphics() {
	return g;
    }
}
