package com.bondar.geom;

import com.bondar.geom.Point2D;

/**
 * a - left bottom
 * b - left top
 * c - right top
 * d - right botton
 * @author bondar
 */
public class ClipBox2D {
    
    // internal clipping codes
    public final static int CODE_C = 0x0000;
    public final static int CODE_N = 0x0008;
    public final static int CODE_S = 0x0004;
    public final static int CODE_E = 0x0002;
    public final static int CODE_W = 0x0001;
 
    public final static int CODE_NE = 0x000a;
    public final static int CODE_SE = 0x0006;
    public final static int CODE_NW = 0x0009;
    public final static int CODE_SW = 0x0005;
        
    public enum Type {
	None,
	Rectangle,
	Polygon
    }
    public enum State {
	ERROR,		// ошибка
	OK,		// норма
	LESS_3_VERTS,	// вершин менее трех
	SEGMENT,	// многоугольник вырожден в отрезок
	NONCONVEX	// многоугольник невыпуклый
    }
    private Type type;
    private State state;
    // for rectangle
    private int xMin, xMax, yMin, yMax;
    private int width, height;
    // for polygon
    private int size;
    private Point2D[] points;
    private Point2D[] normals;

 
    public ClipBox2D(int xmin, int ymin, int xmax, int ymax) {
	type = Type.Rectangle;
	this.xMin = xmin;
	this.yMin = ymin;
	this.xMax = xmax;
	this.yMax = ymax;
        this.width = xmax - xmin;
        this.height = ymax - ymin;
	state = State.OK;
    }
    
    public ClipBox2D(Point2D[] points) {
	type = Type.Polygon;
	if (points == null) {
	    state = State.ERROR;
	    return;
	}
	this.size = points.length;
	this.points = points;
	normals = new Point2D[size];
	for (int i = 0; i < size; i++) {
	    normals[i] = new Point2D();
	}
	state = setNormals();
    }

    /////////////////////////////////////////////////////////
    // Проверяет окно на выпуклость и невырожденность.
    // Если окно правильное, то вычисляет координаты перепендикуляров к ребрам.
    private State setNormals()
    {
	int i;
	boolean sminus, splus, szero;	// Знак вект.произведений
	double r,
		vox, voy,   // Координаты (i-1) вершины
		vix, viy,   // Координаты i вершины
		vnx, vny;   // Координаты (i+1) вершины

	/* Проверка на выпуклость - вычисляются векторные произведения
	 * смежных сторон и определяется знак.
	 * если все знаки == 0 то многоугольник вырожден
	 * если все знаки >= 0 то многоугольник выпуклый
	 * если все знаки <= 0 то многоугольник невыпуклый
	 */
	if (--size < 2) {
	    return State.LESS_3_VERTS;
	}
	sminus = false;
	splus = false;
	szero = false;
	// (i-1)вершина = последняя
	vox = points[size].getX();
	voy = points[size].getY();
	// (i)вершина = первая
	vix =  points[0].getX();
	viy =  points[0].getY();
	i = 0;
	do {
	    if (++i > size) i= 0;
	    vnx = points[i].getX();	// Следующая (i+1) вершина
	    vny = points[i].getX(); 
	    r = (vix - vox) * (vny - viy) -	// Векторное произведение ребер
		    (viy - voy) * (vnx - vix);	// смежных с i-й вершиной
	    if (r < 0) {
		sminus = true;
	    } else if (r > 0) {
		splus = true;
	    } else {
		szero = true;
	    }
	    // Обновление координат 
	    vox = vix;
	    voy = viy;
	    vix = vnx;
	    viy = vny;
	} while (i != 0);

	// Все векторные произведения равны нулю => Многоугольник вырожден в линию
	if (!splus && !sminus)
	    return State.SEGMENT;
	// Знакопеременность => Многоугольник невыпуклый
	if (splus && sminus)
	    return State.NONCONVEX;

	// Вычисление координат нормалей к сторонам
	vox =  points[0].getX();
	voy =  points[0].getY();
	i = 0;
	do {
	    if (++i > size) i= 0;
	    // Текущая вершина
	    vix = points[i].getX();
	    viy = points[i].getY();
	    // Поворот по часовой  
	    vnx = viy - voy;
	    vny = vox - vix;
	    // Внутр нормали влево
	    if (splus) {
		vnx = -vnx;
		vny = -vny;
	    }
	    // Обновление координат нормалей
	    normals[i].setX(vnx);
	    normals[i].setY(vny);
	    vox = vix;
	    voy = viy;
	} while (i != 0);
	return State.OK;
    }

    /////////////////////////////////////////////////////////
    // get
    public Point2D[] getPoints() {
	return points;
    }

    public Point2D[] getNormals() {
	return normals;
    }

    public Type getType() {
	return type;
    }
    
    public State getState() {
	return state;
    }

    public int getCount() {
	return size;
    }

    public Point2D getA() {
	return new Point2D(xMin,yMin);
    }

    public Point2D getB() {
	return new Point2D(xMin,yMax);
    }

    public Point2D getC() {
	return new Point2D(xMax,yMax);
    }

    public Point2D getD() {
	return new Point2D(xMax,yMin);
    }

    public int getXMin() {
	return xMin;
    }

    public int getXMax() {
	return xMax;
    }

    public int getYMin() {
	return yMin;
    }

    public int getYMax() {
	return yMax;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
