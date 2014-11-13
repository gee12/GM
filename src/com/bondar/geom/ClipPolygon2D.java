/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bondar.geom;

/**
 *
 * @author Иван
 */
public class ClipPolygon2D {
    
    public static enum States {
	ERROR,		// ошибка
	OK,		// норма
	LESS_3_VERTS,	// вершин менее трех
	SEGMENT,	// многоугольник вырожден в отрезок
	NONCONVEX	// многоугольник невыпуклый
    }

    private States state;
    private int size;
    private Point2D[] points;
    private Point2D[] normals;
    
    public ClipPolygon2D() {
        this.state = States.ERROR;
    }
    
    
    public ClipPolygon2D(States state) {
        this.state = state;
    }
    
    public ClipPolygon2D(ClipPolygon2D clip) {
	if (clip == null) {
	    state = States.ERROR;
	    return;
	}
        this.size = clip.getSize();
	this.points = clip.getPoints();
	this.normals = clip.getNormals();
        this.state = clip.getState();
    }
    
    
    public ClipPolygon2D(Point2D[] points, Point2D[] normals, States state) {
	if (points == null) {
	    state = States.ERROR;
	    return;
	}
        this.size = points.length;
	this.points = points;
	this.normals = normals;
        this.state = state;
    }
    
    public ClipPolygon2D(Point2D[] points) {
	this(build(points));
    }
    
    /////////////////////////////////////////////////////////
    // Проверяет окно на выпуклость и невырожденность.
    // Если окно правильное, то вычисляет координаты перепендикуляров к ребрам.
    public static ClipPolygon2D build(Point2D[] points)
    {
        if (points == null) return null;
        int size = points.length;
	boolean sminus, splus, szero;	// Знак вект.произведений
	double r,
		vox, voy,   // Координаты (i-1) вершины
		vix, viy,   // Координаты i вершины
		vnx, vny;   // Координаты (i+1) вершины
        Point2D[] normals = new Point2D[size];

	/* Проверка на выпуклость - вычисляются векторные произведения
	 * смежных сторон и определяется знак.
	 * если все знаки == 0 то многоугольник вырожден
	 * если все знаки >= 0 то многоугольник выпуклый
	 * если все знаки <= 0 то многоугольник невыпуклый
	 */
	if (--size < 2) {
            return new ClipPolygon2D(States.LESS_3_VERTS);
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
	int i = 0;
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
	if (!splus && !sminus) {
            return new ClipPolygon2D(States.SEGMENT);
        }
	// Знакопеременность => Многоугольник невыпуклый
	if (splus && sminus) {
            return new ClipPolygon2D(States.NONCONVEX);
        }

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
            normals[i] = new Point2D(vnx, vny);
            vox = vix;
	    voy = viy;
	} while (i != 0);
        
        return new ClipPolygon2D(points, normals, States.OK);
    }

    /////////////////////////////////////////////////////////
    // Алгоритм отсечения Кирус-Бека
    public Line2D CBclip(double x0, double y0, double x1, double y1) {
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

	for (i = 0; i < size; i++) {
	    Qx = Vx - points[i].getX();	// Положения относительно ребра
	    Qy = Vy - points[i].getY();
	    Nx = normals[i].getX();	// Перпендикуляры к ребру
	    Ny = normals[i].getY();
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
	return new Line2D(x0, y0, x1, y1, visible);
    }
    
    // get
    public States getState() {
        return state;
    }

    public int getSize() {
        return size;
    }

    public Point2D[] getPoints() {
        return points;
    }

    public Point2D[] getNormals() {
        return normals;
    }
}
