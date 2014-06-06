package com.bondar.gm;

/**
 * a - left bottom b - left top c - right top d - right botton
 *
 * @author bondar
 */
public class ClipWindow {

    public enum WINDOW_TYPE {
	None,
	Rectangle,
	Polygon
    }
    private WINDOW_TYPE type;
    // for rectangle
    private double xMin, xMax, yMin, yMax;
    // for polygon
    private int count;
    private Point2D[] points;
    private Point2D[] normals;

    public ClipWindow() {
	setRectangle(0,0,0,0);
    }
    
    public int setPolygon(Point2D[] points) {
	if (points == null) {
	    return -1;
	}
	type = WINDOW_TYPE.Polygon;
	this.count = points.length;
	this.points = points;
	normals = new Point2D[count];
	for (int i = 0; i < count; i++) {
	    normals[i] = new Point2D();
	}
	int res = setNormals(points);
	return res;
    }

    //-------------------------------------------------
    // Устанавливает многоугольное окно отсечения;
    // Проверяет окно на выпуклость и невырожденность;
    // Если окно правильное, то вычисляет координаты перепендикуляров к ребрам.
    //
    // Возвращает:
    // -1 - ошибка
    // 0 - норма
    // 1 - вершин менее трех
    // 2 - многоугольник вырожден в отрезок
    // 3 - многоугольник невыпуклый
    private int setNormals(Point2D[] points)
    {
	if (points == null) return -1;
	int n = points.length;
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
	if (--n < 2) {
	    return 1;
	}
	sminus = false;
	splus = false;
	szero = false;
	// (i-1)вершина = последняя
	vox = points[n].getX();
	voy = points[n].getY();
	// (i)вершина = первая
	vix =  points[0].getX();
	viy =  points[0].getY();
	i = 0;
	do {
	    if (++i > n) i= 0;
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

	// Все векторные произведения равны нулю => Многоугольник вырожден
	if (!splus && !sminus)
	    return 2;
	// Знакопеременность => Многоугольник невыпуклый
	if (splus && sminus)
	    return 3;

	// Вычисление координат нормалей к сторонам
	vox =  points[0].getX();
	voy =  points[0].getY();
	i = 0;
	do {
	    if (++i > n) i= 0;
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

	return 0;
    }

    public int setRectangle(double xmin, double ymin, double xmax, double ymax) {
	type = WINDOW_TYPE.Rectangle;
	this.xMin = xmin;
	this.yMin = ymin;
	this.xMax = xmax;
	this.yMax = ymax;
	return 0;
    }

    public Point2D[] getPoints() {
	return points;
    }

    public Point2D[] getNormals() {
	return normals;
    }

    public WINDOW_TYPE getType() {
	return type;
    }

    public int getCount() {
	return count;
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

    public double getXMin() {
	return xMin;
    }

    public double getXMax() {
	return xMax;
    }

    public double getYMin() {
	return yMin;
    }

    public double getYMax() {
	return yMax;
    }
}
