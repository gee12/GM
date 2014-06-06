package com.bondar.gm;

import java.awt.Color;

/**
 *
 * @author truebondar
 */
public class ZBuffer {

    public class Cell {
	private final double z;
	private final Color color;

	public Cell() {
	    this.z = 0;
	    this.color = Color.BLACK;
	}

	public Cell(double z, Color col) {
	    this.z = z;
	    this.color = col;
	}

	public double getZ() {
	    return z;
	}

	public Color getColor() {
	    return color;
	}
    }
    public static final Color DEF_COLOR = Color.WHITE;
    public static final double MAX_DIST = 1000;
    private final double[][] buff;
    //private final double maxX, maxY;
    //private final double xStep, yStep;
    private final int maxX, maxY;

    public ZBuffer(int maxX, int maxY) {
	this.maxX = maxX;
	this.maxY = maxY;
	buff = new double[maxY][maxX];
	clearBuffer();
    }
    
    /*public ZBuffer(double maxX, double maxY, double xStep, double yStep) {
	this.maxX = maxX;
	this.maxY = maxY;
	this.xStep = xStep;
	this.yStep = yStep;
	int rows = (int) (maxY / yStep);
	int cols = (int) (maxX / xStep);
	buff = new Cell[rows][cols];
	clearBuffer();
    }*/

    /*public void putTriangle(Triangle3D tria) {
	if (tria == null) {
	    return;
	}
	double ymax, ymin;
	double[] x = new double[3], y = new double[3];
	final Point3DOdn[] vertexes = tria.getVertexes();
	//Заносим x,y из t в массивы для последующей работы с ними
	for (int i = 0; i < 3; i++) {
	    x[i] = vertexes[i].getX();
	    y[i] = vertexes[i].getY();
	}
	//Определяем максимальный и минимальный y
	ymax = ymin = y[0];
	if (ymax < y[1]) {
	    ymax = y[1];
	} else if (ymin > y[1]) {
	    ymin = y[1];
	}
	if (ymax < y[2]) {
	    ymax = y[2];
	} else if (ymin > y[2]) {
	    ymin = y[2];
	}
	ymin = (ymin < 0) ? 0 : ymin;
	ymax = (ymax < this.maxY) ? ymax : this.maxY;
	double x1 = 0, x2 = 0, z1 = 0, z2 = 0, xsc1, xsc2;
	//Следующий участок кода перебирает все строки сцены
	//и определяет глубину каждого пикселя
	//для соответствующего треугольника
	for (double ysc = ymin; ysc < ymax; ysc += this.yStep) {
	    int ne = 0;
	    for (int e = 0; e < 3; e++) {
		int e1 = (e == 2) ? 0 : e + 1;
		if (y[e] < y[e1]) {
		    if (y[e1] <= ysc || ysc < y[e]) {
			continue;
		    }
		} else if (y[e] > y[e1]) {
		    if (y[e1] > ysc || ysc >= y[e]) {
			continue;
		    }
		} else {
		    continue;
		}
		double tc = (y[e] - ysc) / (y[e] - y[e1]);
		if (ne == 1) {
		    x2 = x[e] + tc * (x[e1] - x[e]);
		    z2 = vertexes[e].getZ() + tc * (vertexes[e1].getZ() - vertexes[e].getZ());
		} else {
		    x1 = x[e] + (tc * (x[e1] - x[e]));
		    z1 = vertexes[e].getZ() + tc * (vertexes[e1].getZ() - vertexes[e].getZ());
		    ne = 1;
		}
	    }
	    if (x2 < x1) {
		double temp = x1;
		x1 = x2;
		x2 = temp;
		temp = z1;
		z1 = z2;
		z2 = temp;
	    }
	    xsc1 = (x1 < 0) ? 0 : x1;
	    xsc2 = (x2 > this.maxX) ? this.maxX : x2;
	    for (double xsc = xsc1; xsc < xsc2; xsc += this.xStep) {
		double temp = (x1 - xsc) / (x1 - x2);
		double z = z1 + temp * (z2 - z1);
		//Если полученная глубина пиксела меньше той,
		//что находится в Z-Буфере - заменяем храняшуюся на новую.
		if (z < buff[(int) ysc][(int) xsc].getZ()) {
		    buff[(int) ysc][(int) xsc] = new Cell(z, tria.getColor());
		}
	    }
	}
    }*/

    public void clearBuffer() {
	for (int i = 0; i < buff.length; i++) {
	    for (int j = 0; j < buff[i].length; j++) {
		buff[i][j] = 0;//new Cell(MAX_DIST, DEF_COLOR);
	    }
	}
    }

    public double[][] getBuffer() {
	return buff;
    }
    
    public double getBufferAt(int col, int row) {
	if (col < 0 || row < 0 || col > maxX || row > maxY)
	    throw new RuntimeException("Индекс за пределами массива");
	return buff[row][col];
    }
    
    public void setBufferAt(int col, int row, double value) {
	if (col < 0 || row < 0 || col > maxX || row > maxY)
	    throw new RuntimeException("Индекс за пределами массива");
	buff[row][col] = value;
    }
}
