package com.bondar.gm;

import com.bondar.gm.Matrix.AXIS;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author bondar
 */
public class GraphicSystem {

    public static double EPS = 0.0000001;
    public static final double MAX_POINT = 10;
    public static final double X_MAX = 10d;
    public static final double Y_MAX = 7d;
    public static final double X_MIN = 0d;
    public static final double Y_MIN = 0d;
    public static final double BORDER = 1d;
    private Graphics g;
    private int width, height;
    private Point2D old;
    private Matrix transMatrix;
    private ClipBox2D clipWindow;
    private boolean isNeedClip;
    private boolean isNeedScale;
    private double xMin, xMax, yMin, yMax;
    private double xc, yc, Xc, Yc;
    private double f;
    private double c1, c2;
    private ZBuffer zBuffer;

    public GraphicSystem() {
	old = new Point2D();
	transMatrix = new Matrix();
	clipWindow = new ClipBox2D(0,0,0,0);
	resetScale();
    }

    //////////////////////////////////////////////////
    public void setGraphics(Graphics g) {
	this.g = g;
	this.width = g.getClipBounds().width;
	this.height = g.getClipBounds().height;
	zBuffer = new ZBuffer(width, height);

	g.setColor(Color.WHITE);
	g.fillRect(0, 0, width, height);
	g.setColor(Color.BLACK);
    }
    
    public void clear() {
	g.setColor(Color.WHITE);
	g.fillRect(convXToScreen(0), convYToScreen(Y_MAX), convXToScreen(X_MAX), convYToScreen(0));
	g.setColor(Color.BLACK);
    }

    //////////////////////////////////////////////////
    // Устанвка окна отсечения
    public void setClip(boolean isNeedClip) {
	this.isNeedClip = isNeedClip;
    }
    
    // Установка прямоугольного окна отсечения (в координатах X_MAX и Y_MAX)
    public void setClipWindow(double xmin, double ymin, double xmax, double ymax) {
	if (!isCorrectXCoord(xmin) || !isCorrectXCoord(xmax)
		|| !isCorrectYCoord(ymin) || !isCorrectYCoord(ymax)
		|| xmin > xmax || ymin > ymax) {
	    return;
	}
	clipWindow = new ClipBox2D(xmin, ymin, xmax, ymax);
    }

    // Установка поточечного окна отсечения (в координатах X_MAX и Y_MAX)
    public void setClipWindow(Point2D[] points) {
	if (points == null) {
	    return;
	}
	for (int i = 0; i < points.length; i++) {
	    if (!isCorrectPoint(points[i])) {
		return;
	    }
	}
	clipWindow = new ClipBox2D(points);
    }

    //////////////////////////////////////////////////
    // Установка масштабирования
    public void setScale(boolean isNeedScale) {
	this.isNeedScale = isNeedScale;
	resetScale();
    }

    public void setScale(double x, double y) {
	double[] vector4 = transMatrix.applyTransform(new Point2D(x, y).toArrayOdn());
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
	transMatrix = transMatrix.multiply(Matrix.getRotationMatrix(angle, axis));
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
	Point3DOdn p = new Point3DOdn(transMatrix.applyTransform(new Point2D(x, y).toArrayOdn()));
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
	Point3D p1 = new Point3D(transMatrix.applyTransform(from.toArrayOdn()));
	Point3D p2 = new Point3D(transMatrix.applyTransform(to.toArrayOdn()));
	Line line = new Line(p1, p2);
	// is need scale?
	if (isNeedScale) {
	    line = getScaleLine(line);
	}
	// is need clip?
	if (isNeedClip) {
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
    public void fillPolygon(Point3D[] points) {
	if (points == null) {
	    return;
	}
	int size = points.length;
	int xs[] = new int[size];
	int ys[] = new int[size];
	for (int i = 0; i < size; i++) {
	    double[] p = transMatrix.applyTransform(points[i].toPoint3DOdn().toArray3());
	    xs[i] = convXToScreen(p[0]);
	    ys[i] = convYToScreen(p[1]);
	}
	g.fillPolygon(xs, ys, size);
    }

    //////////////////////////////////////////////////
    // Пребразование линии с учетом масштабирования
    private Line getScaleLine(Line line) {
	return getScaleLine(line.getP1().getX(), line.getP1().getY(),
		line.getP2().getX(), line.getP2().getY());
    }

    private Line getScaleLine(Point2D p1, Point2D p2) {
	return new Line(getScalePoint(p1), getScalePoint(p2));
    }

    private Line getScaleLine(double x0, double y0, double x1, double y1) {
	return new Line(getScalePoint(x0, y0), getScalePoint(x1, y1));
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
    private Line getClipLine(double x0, double y0, double x1, double y1) {
	Line line = null;
	if (clipWindow.getType() == ClipBox2D.Type.Rectangle) {
	    line = CSclip(x0, y0, x1, y1);
	} else if (clipWindow.getType() == ClipBox2D.Type.Polygon) {
	    line = CBclip(x0, y0, x1, y1);
	} else {
	    line = new Line();
	}
	return line;
    }

    private Line getClipLine(Point2D p1, Point2D p2) {
	return getClipLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    private Line getClipLine(Line line) {
	return getClipLine(line.getP1().getX(), line.getP1().getY(),
		line.getP2().getX(), line.getP2().getY());
    }

    /////////////////////////////////////////////////////
    // Алгоритм отсечения Коэна-Сазерленда
    public Line CSclip(double x0, double y0, double x1, double y1) {
	boolean visible = false;	// не видим/видим
	int cn, ck, /* Коды концов отрезка */
		ii = 4, s;      /* Рабочие переменные  */
	double dx, dy, /* Приращения координат*/
		dxdy = 0, dydx = 0, /* Наклоны отрезка к сторонам */
		r;            /* Рабочая переменная  */

	Line res = new Line(x0, y0, x1, y1, visible);
	ck = code(x1, y1);
	cn = code(x0, y0);
	/* Определение приращений координат и наклонов отрезка
	 * к осям. Заодно сразу на построение передается отрезок,
	 * состоящий из единственной точки, попавшей в окно
	 */
	dx = x1 - x0;
	dy = y1 - y0;
	if (dx != 0) {
	    dydx = dy / dx;
	} else if (dy == 0) {
	    return res;
	}
	if (dy != 0) {
	    dxdy = dx / dy;
	}
	/* Основной цикл отсечения */
	do {
	    if ((cn & ck) != 0) {
		break;       /* Целиком вне окна    */
	    }
	    if (cn == 0 && ck == 0) { /* Целиком внутри окна */
		visible = true;
		break;
	    }
	    if (cn == 0) { /* Если Pn внутри окна, то */
		s = cn;
		cn = ck;
		ck = s;  /* перестить точки Pn,Pk и */

		r = x1;
		x0 = x1;
		x1 = r;  /* их коды, чтобы Pn  */

		r = y0;
		y0 = y1;
		y1 = r;  /* оказалась вне окна */
	    }
	    /* Теперь отрезок разделяется. Pn помещается в точку
	     * пересечения отрезка со стороной окна.
	     */
	    if ((cn & 1) != 0) {         /* Пересечение с левой стороной */
		y0 = y0 + dydx * (clipWindow.getXMin() - x0);
		x0 = clipWindow.getXMin();
	    } else if ((cn & 2) != 0) {  /* Пересечение с правой стороной*/
		y0 = y0 + dydx * (clipWindow.getXMax() - x0);
		x0 = clipWindow.getXMax();
	    } else if ((cn & 4) != 0) {  /* Пересечение в нижней стороной*/
		x0 = x0 + dxdy * (clipWindow.getYMin() - y0);
		y0 = clipWindow.getYMin();
	    } else if ((cn & 8) != 0) {  /*Пересечение с верхней стороной*/
		x0 = x0 + dxdy * (clipWindow.getYMax() - y0);
		y0 = clipWindow.getYMax();
	    }
	    cn = code(x0, y0);        /* Перевычисление кода точки Pn */
	} while (--ii >= 0);

	if (visible) {
	    return new Line(x0, y0, x1, y1, visible);
	} else {
	    return res;
	}
    }

    /////////////////////////////////////////////////////
    // Область попадания точки
    private int code(double x, double y) {
	int i = 0;
	if (x < clipWindow.getXMin() + EPS) {
	    ++i;
	} else if (x > clipWindow.getXMax() - EPS) {
	    i += 2;
	}
	if (y < clipWindow.getYMin()) {
	    i += 4;
	} else if (y > clipWindow.getYMax()) {
	    i += 8;
	}
	return i;
    }

    /////////////////////////////////////////////////////
    // Алгоритм отсечения Кирус-Бека
    private Line CBclip(double x0, double y0, double x1, double y1) {
	int i;
	boolean visible;
	double Vx, Vy;
	double dx, dy, // Директриса отрезка (V1 - V0)
		t0, t1, // Параметры начальной и конечной точек видимой части отрезка
		Qx, Qy, // Вектор от начальной точки i-го ребра к точке V
		Nx, Ny, // Перпендикуляр к i-тому ребру
		Pi, Qi, // Вектора для нахождения t = -Qi/Pi
		t;	    // Для вычисления параметров t0 и t1 (>,<)
	visible = true;
	t0 = 0;
	t1 = 1;
	Vx = x0;
	Vy = y0;
	dx = x1 - x0;
	dy = y1 - y0;

	for (i = 0; i < clipWindow.getCount(); i++) {
	    Qx = Vx - clipWindow.getPoints()[i].getX();	// Положения относительно ребра
	    Qy = Vy - clipWindow.getPoints()[i].getY();
	    Nx = clipWindow.getNormals()[i].getX();	// Перпендикуляры к ребру
	    Ny = clipWindow.getNormals()[i].getY();
	    // Ориентация отрезка относительно i-й стороны окна 
	    // определяется знаком скалярного произведения Pi = Ni * (V1 - V0).
	    Pi = dx * Nx + dy * Ny;
	    // Для вычисления значений параметров, соответствующих 
	    // начальной и конечной точкам видимой части отрезка. Qi = Ni * (V - Li).
	    Qi = Qx * Nx + Qy * Ny;

	    // Анализ расположения
	    if (Pi == 0) {  // Отрезок параллелен ребру или вырожден в точку
		if (Qi < 0) {	// Точка V лежит  с   внешней  стороны  границы
		    visible = false;
		    break;
		}
	    } else {
		// Вычисляем значение параметра t 
		// для точки пересечения отсекаемого отрезка с i-тым ребром
		t = -Qi / Pi;
		if (Pi < 0) {
		    // Отрезок направлен с внутренней на внешнюю сторону i-й граничной линии;
		    // Поиск значения параметра для КОНЕЧНОЙ точки видимой части отрезка
		    // (верхнего предела t -> t1).
		    // Ищется МИНИМАЛЬНОЕ значение из всех получаемых решений.
		    if (t < t0) {
			visible = false;
			break;
		    }
		    if (t < t1) {
			t1 = t;
		    }
		} else if (Pi > 0) {
		    // Отрезок направлен с внешней на внутреннюю сторону i-й граничной линии;
		    // Поиск значения параметра для НАЧАЛЬНОЙ точки видимой части отрезка
		    // (нижнего предела t -> t0).
		    // Ищется МАКСИМАЛЬНОЕ значение.
		    if (t > t1) {
			visible = false;
			break;
		    }
		    if (t > t0) {
			t0 = t;
		    }
		}
	    }
	}
	// Вычисление координат точек пересечения отрезка с окном: 
	// параметрическое представление отсекаемого отрезка:
	// V(t) = V0 + (V1 - V0)*t, (0 <= t <= 1)
	if (visible) {
	    if (t0 > t1) {
		visible = false;
	    } else {
		if (t0 > 0) {
		    // Меняем координату P0
		    x0 = Vx + t0 * dx;
		    y0 = Vy + t0 * dy;
		}
		if (t1 < 1) {
		    // Меняем координату P1
		    x1 = Vx + t1 * dx;
		    y1 = Vy + t1 * dy;
		}
	    }
	}
	return new Line(x0, y0, x1, y1, visible);
    }

    /////////////////////////////////////////////////////
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

    /////////////////////////////////////////////////////
    // Алгоритм художника
    public void painterAlgorithm(Triangle3D[] trias) {
	Triangle3D[] sortTrias = sortTrianglesByZ(trias);
	if (sortTrias == null) {
	    return;
	}
	for (Triangle3D tria : sortTrias) {
	    setColor(tria.getColor());
	    fillPolygon(tria.getVertexes());
	}
    }

    // Сортировка граней по координате Z
    public static Triangle3D[] sortTrianglesByZ(Triangle3D[] trias) {
	if (trias == null) {
	    return null;
	}
	int size = trias.length;
	double[] dists = new double[size];
	int[] indexes = new int[size];
	// нахождение средней величины Z - удаленности грани
	for (int i = 0; i < size; i++) {
	    Triangle3D tria = trias[i];
	    dists[i] = (tria.getV1().getZ()
		    + tria.getV2().getZ()
		    + tria.getV3().getZ()) / 3;
	    indexes[i] = i;
	}
	Triangle3D[] res = new Triangle3D[size];
	// сортировка граней по удаленности
	for (int i = 0; i < size - 1; i++) {
	    for (int j = 0; j < size - 1; j++) {
		if (dists[j] < dists[j + 1]) {
		    double distTemp = dists[j];
		    dists[j] = dists[j + 1];
		    dists[j + 1] = distTemp;

		    int indTemp = indexes[j];
		    indexes[j] = indexes[j + 1];
		    indexes[j + 1] = indTemp;
		}
	    }
	}
	for (int i = 0; i < size; i++) {
	    res[i] = trias[indexes[i]].getCopy();
	}
	return res;
    }

    /////////////////////////////////////////////////////
    // Алгоритм отсечения невидимых граней с использованием z-буффера
    public void zBufferAlgorithm(Triangle3D[] trias) {
	if (trias == null) {
	    return;
	}
	for (Triangle3D tria : trias) {
	    drawBufferedTriangle(tria);
	}
    }

    public void setScreenPixel(Point3D p, Color col) {
	g.setColor(col);
	g.drawLine((int)p.getX(),height - (int)p.getY(), 
		(int)p.getX(),height - (int)p.getY());
    }

    public void setScreenPixel(double x, double y, Color col) {
	Point3D p = new Point3D(x, y, 0);
	setScreenPixel(p,col);
    }

    /////////////////////////////////////////////////////
    // Отрисовка треугольника (полинейно)
    private void drawBufferedTriangle(Triangle3D tria) {
	if (tria == null) return;
	Point3D a = convPToScreen(tria.getV1());
	Point3D b = convPToScreen(tria.getV2());
	Point3D c = convPToScreen(tria.getV3());
	int sy, x1, x2;
	boolean flag = true;
	// здесь сортируем вершины (A,B,C)
	while (flag) {
	    flag = false;
	    if (b.getY() < a.getY()) {
		Point3D d = b;
		b = a;
		a = d;
		flag = true;
	    }
	    if (c.getY() < b.getY()) {
		Point3D d = c;
		c = b;
		b = d;
		flag = true;
	    }
	}
	//закраска треугольника
	for (sy = (int) (a.getY()); sy < c.getY(); sy++) {
	    x1 = (int) (a.getX()) + (sy - (int) (a.getY())) * ((int) (c.getX())
		    - (int) (a.getX())) / ((int) (c.getY()) - (int) (a.getY()));
	    if (sy < b.getY()) {
		x2 = (int) (a.getX()) + (sy - (int) (a.getY())) * ((int) (b.getX())
			- (int) (a.getX())) / ((int) (b.getY()) - (int) (a.getY()));
	    } else {
		if (c.getY() == b.getY()) {
		    x2 = (int) (b.getX());
		} else {
		    x2 = (int) (b.getX()) + (sy - (int) (b.getY())) * ((int) (c.getX())
			    - (int) (b.getX())) / ((int) (c.getY()) - (int) (b.getY()));
		}
	    }
	    if (x1 > x2) {
		int tmp = x1;
		x1 = x2;
		x2 = tmp;
	    }
	    Point3DOdn p1 = new Point3DOdn(x1, sy, 0);
	    Point3DOdn p2 = new Point3DOdn(x2, sy, 0);
	    drawBufferedLine(tria, p1, p2);
	}
    }

    /////////////////////////////////////////////////////
    // Алгритм Брезинхема
    // (поточечная отрисовка линии треугольника с использованием Z-буффера)
    private void drawBufferedLine(Triangle3D tria, Point3DOdn p1, Point3DOdn p2) {
	float d, d1, d2;
	int dx = (int) (Math.abs(p2.getX() - p1.getX()));
	int dy = (int) (Math.abs(p2.getY() - p1.getY()));
	int sx = p2.getX() >= p1.getX() ? 1 : -1;
	int sy = p2.getY() >= p1.getY() ? 1 : -1;
	if (dy <= dx) // проверка угла наклона линии
	{
	    d = (dy >> 1) - dx; // деление на два для определения углового коэффициента
	    d1 = dy >> 1;
	    d2 = (dy - dx) >> 1;

	    int xPix = (int) p1.getX();
	    int yPix = (int) (height - p1.getY());
	    if (xPix >= 0 && yPix >= 0 && xPix < width && yPix < height) {
		double z_b = tria.getZbyXY(xPix, yPix);
		if (z_b < zBuffer.getBufferAt(xPix,yPix)) {
		    setScreenPixel(xPix, yPix, tria.getColor()); //вывод первой точки на экране
		    zBuffer.setBufferAt(xPix, yPix, z_b);
		}
	    }
	    for (int x = (int)(p1.getX()) + sx, y = (int)(p1.getY()), i = 1; i <= dx; i++, x += sx) // цикл вывода линии на экран
	    {
		if (d > 0) // Если d < 0 значение y не меняется по сравнению с предыдущей точкой, иначе y увеличивается
		{
		    d += d2;
		    y += sy;
		} else {
		    d += d1;
		}
		yPix = (int) (height - y);

		if ((x >= 0) && (yPix >= 0) && (x < width) && (yPix < height)) {
		    double z_b = tria.getZbyXY(xPix, yPix);
		    if (z_b < zBuffer.getBufferAt(x,yPix)) {
			setScreenPixel(x, yPix, tria.getColor());
			zBuffer.setBufferAt(x, yPix, z_b);
		    }
		}
	    }
	} else {
	    d = (dx >> 1) - dy;
	    d1 = dx >> 1;
	    d2 = (dx - dy) >> 1;

	    int xPix = (int) p1.getX();
	    int yPix = (int) (height - p1.getY());
	    if (xPix >= 0 && yPix >= 0 && xPix < width && yPix < height) {
		double z_b = tria.getZbyXY(xPix, yPix);
		if (z_b < zBuffer.getBufferAt(xPix,yPix)) {
		    setScreenPixel(xPix, yPix, tria.getColor());
		    zBuffer.setBufferAt(xPix, yPix, z_b);
		}
	    }
	    for (int x = (int) (p1.getX()), y = (int) (p1.getY()) + sy, i = 1; i <= dy; i++, y += sy) {
		if (d > 0) {
		    d += d2;
		    x += sx;
		} else {
		    d += d1;
		}
		yPix = (int) (height - y);

		if ((x >= 0) && (yPix >= 0) && (x < width) && (yPix < height)) {
		    double z_b = tria.getZbyXY(x, yPix);
		    if (z_b < zBuffer.getBufferAt(x,yPix)) {
			setScreenPixel(x, yPix, tria.getColor());
			zBuffer.setBufferAt(x, yPix, z_b);
		    }
		}
	    }
	}
    }

    /////////////////////////////////////////////////////
    // проверки и преобразования
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

    public static int convXToScreen(double width, double x) {
	return (int) (x * (width / X_MAX) + BORDER);
    }

    private int convXToScreen(double x) {
	return GraphicSystem.convXToScreen(width, x);
    }

    public static int convYToScreen(double height, double y) {
	return (int) (height - (y * (height / Y_MAX) + BORDER));
    }

    private int convYToScreen(double y) {
	return GraphicSystem.convYToScreen(height, y);
    }

    public static Point3D convPToScreen(double width, double height, Point3D p) {
	return new Point3D(
		GraphicSystem.convXToScreen(width, p.getX()),
		GraphicSystem.convYToScreen(height, p.getY()),
		p.getZ());
    }
    
    public Point3D convPToScreen(Point3D p) {
	Point3D trans = transMatrix.getTranslate();
	return new Point3D(
		(int) ((p.getX() + trans.getX()) * (width / X_MAX) + BORDER),
		(int) (height - ((p.getY() + trans.getY()) * (height / Y_MAX) + BORDER)),
		p.getZ());
    }

    public static Point3D convPToGraphic(double width, double height, Point3D p) {
	if (width == 0 || height == 0) {
	    return null;
	}
	return new Point3D(
		(p.getX() - BORDER) * X_MAX / width,
		(BORDER + height - p.getY()) * Y_MAX / height,
		p.getZ());
    }

    public Point3D convPToGraphic(Point3DOdn p) {
	Point3D trans = transMatrix.getTranslate();
	return new Point3D(
		(p.getX() - BORDER) * X_MAX / width - trans.getX() ,
		(BORDER + height - p.getY()) * Y_MAX / height - trans.getY(),
		p.getZ());
    }

    /////////////////////////////////////////////////////
    // get
    public ClipBox2D getClipWindow() {
	return clipWindow;
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
}
