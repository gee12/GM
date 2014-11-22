package com.bondar.geom;

import com.bondar.geom.Point2D;
import com.bondar.geom.Line3D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class Solid2D {

    private final Point2D[] points;
    private Color color;

    public Solid2D(Point2D[] points) {
	this.points = points;
        this.color = Color.BLACK;
    }
    
    public Solid2D(Point2D[] points, Color col) {
	this.points = points;
        this.color = col;
    }
    
    /*
     * Триангуляция Делоне.
     * Последовательно просматриваются вершины многоугольника. 
     * Если угол при вершине меньше 180 градусов (вершина выпуклая), 
     * то делается проверка для всех несоседних рёбер - 
     * попадают ли они внутрь треугольника образованного 
     * этой вершиной и двумя соседними. Если да, то переходим к следующей вершине. 
     * Если нет, то убираем эту вершину из многоугольника, 
     * а полученный треугольник записывем в результат. 
     * С оставшимся многоугольником повторяем процедуру пока в нём больше трёх вершин.
     * Вариантов триангуляции много и алгоритм находит какой-то из них. 
     */
    public Line2D[] getDecompositionLines(boolean isCounterClockWise) {
	int polySize = getSize();
	if (polySize <= 3) {
	    return getLines();
	}
	//
	List<Line2D> res = new ArrayList(polySize - 3);
	int[] verts = new int[polySize];
	for (int i = 0; i < polySize; i++) {
	    verts[i] = i;
	}
	int m = polySize;

	while (m > 3) {
	    double diag, minDist;
	    int vPrev, vCur, vNext, vMin = 0;
	    minDist = Double.MAX_VALUE;

	    for (vCur = 0; vCur < m; vCur++) {
		vPrev = (vCur == 0) ? m - 1 : vCur - 1;
		vNext = (vCur == m - 1) ? 0 : vCur + 1;
		
		// если внутрь треугольника, образованного выбранными
		// тремя вершинами, попадает одна из остальных вершин - идем дальше
		if (isPointsInTriangle(vPrev, vCur, vNext, verts)) {
		    continue;
		}
		DetermDistance dd = counterClock(vPrev, vCur, vNext, verts);
		double det = dd.getDeterm();
		double dist = dd.getDistance();
		// наименьшее расстояние между vPrev и vNext
		if (((det > 0 && isCounterClockWise)
			|| (det < 0 && !isCounterClockWise))
			&& dist < minDist) {
		    minDist = dist;
		    vMin = vCur;
		}
	    }
	    vCur = vMin;
	    vPrev = (vCur == 0) ? m - 1 : vCur - 1;
	    vNext = (vCur == m - 1) ? 0 : vCur + 1;
	    
	    // не в том направлении заданы точки
	    if (minDist == Double.MAX_VALUE) {
		return null;
	    }
	    // "делим" многоугольник линией
	    res.add(new Line2D(points[verts[vPrev]], points[verts[vNext]]));
	    // убираем текущую вершину
	    m--;
	    for (int k = vCur; k < m; k++) {
		verts[k] = verts[k+1];
	    }
	}

	Line2D[] array = new Line2D[res.size()];
	return res.toArray(array);
    }
    
    /////////////////////////////////////////////////////////
    // 
    public boolean isPointInto(Point2D point) {
	if (points == null || point == null) {
	    return false;
	}
	int size = points.length;
	if (size <= 1) {
	    return false;
	}
	int intersections_num = 0;
	int prev = size - 1;
	boolean prev_under = points[prev].getY() < point.getY();

	for (int i = 0; i < size; ++i) {
	    boolean cur_under = points[i].getY() < point.getY();

	    Point2D a = points[prev].sub(point);
	    Point2D b = points[i].sub(point);

	    double t = (a.getX() * (b.getY() - a.getY()) - a.getY() * (b.getX() - a.getX()));
	    if (cur_under && !prev_under) {
		if (t > 0) {
		    intersections_num += 1;
		}
	    }
	    if (!cur_under && prev_under) {
		if (t < 0) {
		    intersections_num += 1;
		}
	    }
	    prev = i;
	    prev_under = cur_under;
	}
	return (intersections_num & 1) != 0;
    }
   
    private boolean isPointsInTriangle(int a, int b, int c, int[] verts) {
	Point2D pa = points[verts[a]];
	Point2D pb = points[verts[b]];
	Point2D pc = points[verts[c]];
	final double e = 1e-6;
	double abcSquare = square(pa,pb,pc);
	
	for (Point2D pd : points) {
	    // если точки совпадают
	    if (pd.equals(pa) || pd.equals(pb) || pd.equals(pc))
		continue;
	    double abdSquare = square(pa, pb, pd);
	    double adcSquare = square(pa, pd, pc);
	    double dbcSquare = square(pd, pb, pc);
	    
	    // если треугольник выржден в линию
	    if (abdSquare == 0 || adcSquare == 0 || dbcSquare == 0)
		continue;
	    
	    double sum = abdSquare + adcSquare + dbcSquare;
	    if (sum - abcSquare < e)
		return true;
	}
	return false;
    }
    
    private double square(Point2D a, Point2D b, Point2D c) {
	double temp1 = (b.getX() - a.getX()) * (c.getY() - a.getY());
	double temp2 = (c.getX() - a.getX()) * (b.getY() - a.getY());
	return Math.abs(temp1 - temp2) / 2d;
    }

    /////////////////////////////////////////////////////////
    private DetermDistance counterClock(int vPrev, int vCur, int vNext, int[] verts) {
	Point2D pPrev = points[verts[vPrev]];
	Point2D pCur = points[verts[vCur]];
	Point2D pNext = points[verts[vNext]];

	Point2D pCurPrev = pCur.sub(pPrev);
	Point2D pNextPrev = pNext.sub(pPrev);

	DetermDistance res = new DetermDistance();
	res.setDistance(pNextPrev.getX() * pNextPrev.getX() + pNextPrev.getY() * pNextPrev.getY());
	res.setDeterm(pCurPrev.getX() * pNextPrev.getY() - pNextPrev.getX() * pCurPrev.getY());
	return res;
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setColor(Color col) {
        this.color = col;
    }
    
    /////////////////////////////////////////////////////////
    // get
    public Line2D[] getLines() {
	int polySize = getSize();
	Line2D[] res = new Line2D[polySize - 1];
	for (int i = 0; i < polySize - 1; i++) {
	    res[i] = new Line2D(points[i], points[i + 1]);
	}
	return res;
    }

    public Point2D[] getPoints() {
	return points;
    }

    public int getSize() {
	return points.length;
    }
    
    public Color getColor() {
        return color;
    }

    /////////////////////////////////////////////////////////
    private static class DetermDistance {

	private double determinant;
	private double distance;

	public DetermDistance() {
	}

	public DetermDistance(double determ, double dist) {
	    this.determinant = determ;
	    this.distance = dist;
	}

	public void setDeterm(double determ) {
	    this.determinant = determ;
	}

	public void setDistance(double diagonal) {
	    this.distance = diagonal;
	}

	public double getDeterm() {
	    return determinant;
	}

	public double getDistance() {
	    return distance;
	}
    }
}
