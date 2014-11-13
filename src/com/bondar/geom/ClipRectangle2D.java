package com.bondar.geom;

import com.bondar.tools.Mathem;

/**
 * a - left bottom
 * b - left top
 * c - right top
 * d - right botton
 * @author bondar
 */
public class ClipRectangle2D {
    
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
        
    private int xMin, xMax, yMin, yMax;
    private int width, height;

 
    public ClipRectangle2D(int xmin, int ymin, int xmax, int ymax) {
	this.xMin = xmin;
	this.yMin = ymin;
	this.xMax = xmax;
	this.yMax = ymax;
        this.width = xmax - xmin;
        this.height = ymax - ymin;
    }
    

    ///////////////////////////////////////////////////////////
    // 
    public int[] clipLine(int x1, int y1, int x2, int y2) {
        double xc1 = x1,
                yc1 = y1,
                xc2 = x2,
                yc2 = y2;
        int p1_code = 0,
                p2_code = 0;
        // determine codes
        // p1
        if (y1 < yMin) {
            p1_code |= ClipRectangle2D.CODE_N;
        } else if (y1 > yMax) {
            p1_code |= ClipRectangle2D.CODE_S;
        }

        if (x1 < xMin) {
            p1_code |= ClipRectangle2D.CODE_W;
        } else if (x1 > xMax) {
            p1_code |= ClipRectangle2D.CODE_E;
        }
        // p2
        if (y2 < yMin) {
            p2_code |= ClipRectangle2D.CODE_N;
        } else if (y2 > yMax) {
            p2_code |= ClipRectangle2D.CODE_S;
        }

        if (x2 < xMin) {
            p2_code |= ClipRectangle2D.CODE_W;
        } else if (x2 > xMax) {
            p2_code |= ClipRectangle2D.CODE_E;
        }
        // try and trivially reject
        if ((p1_code & p2_code) > 0) {
            return null;
        }
        // test for totally visible, if so leave points untouched
        if (p1_code == 0 && p2_code == 0) {
            return new int[] {x1, y1, x2, y2};
        }
        // determine end clip point for p1
        switch (p1_code) {
            case ClipRectangle2D.CODE_C:
                break;

            case ClipRectangle2D.CODE_N: {
                yc1 = yMin;
                xc1 = x1 + 0.5 + (yMin - y1) * (x2 - x1) / (y2 - y1);
            }
            break;
            case ClipRectangle2D.CODE_S: {
                yc1 = yMax;
                xc1 = x1 + 0.5 + (yMax - y1) * (x2 - x1) / (y2 - y1);
            }
            break;
            case ClipRectangle2D.CODE_W: {
                xc1 = xMin;
                yc1 = y1 + 0.5 + (xMin - x1) * (y2 - y1) / (x2 - x1);
            }
            break;
            case ClipRectangle2D.CODE_E: {
                xc1 = xMax;
                yc1 = y1 + 0.5 + (xMax - x1) * (y2 - y1) / (x2 - x1);
            }
            break;
            // these cases are more complex, must compute 2 intersections
            case ClipRectangle2D.CODE_NE: {
                // north hline intersection
                yc1 = yMin;
                xc1 = x1 + 0.5 + (yMin - y1) * (x2 - x1) / (y2 - y1);
                // test if intersection is valid, of so then done, else compute next
                if (xc1 < xMin || xc1 > xMax) {
                    // east vline intersection
                    xc1 = xMax;
                    yc1 = y1 + 0.5 + (xMax - x1) * (y2 - y1) / (x2 - x1);
                }
            }
            break;
            case ClipRectangle2D.CODE_SE: {
                // south hline intersection
                yc1 = yMax;
                xc1 = x1 + 0.5 + (yMax - y1) * (x2 - x1) / (y2 - y1);
                // test if intersection is valid, of so then done, else compute next
                if (xc1 < xMin || xc1 > xMax) {
                    // east vline intersection
                    xc1 = xMax;
                    yc1 = y1 + 0.5 + (xMax - x1) * (y2 - y1) / (x2 - x1);
                }
            }
            break;
            case ClipRectangle2D.CODE_NW: {
                // north hline intersection
                yc1 = yMin;
                xc1 = x1 + 0.5 + (yMin - y1) * (x2 - x1) / (y2 - y1);
                // test if intersection is valid, of so then done, else compute next
                if (xc1 < xMin || xc1 > xMax) {
                    xc1 = xMin;
                    yc1 = y1 + 0.5 + (xMin - x1) * (y2 - y1) / (x2 - x1);
                }
            }
            break;
            case ClipRectangle2D.CODE_SW: {
                // south hline intersection
                yc1 = yMax;
                xc1 = x1 + 0.5 + (yMax - y1) * (x2 - x1) / (y2 - y1);
                // test if intersection is valid, of so then done, else compute next
                if (xc1 < xMin || xc1 > xMax) {
                    xc1 = xMin;
                    yc1 = y1 + 0.5 + (xMin - x1) * (y2 - y1) / (x2 - x1);
                }
            }
            break;

            default:
                break;
        }
        // determine clip point for p2
        switch (p2_code) {
            case ClipRectangle2D.CODE_C:
                break;
            case ClipRectangle2D.CODE_N: {
                yc2 = yMin;
                xc2 = x2 + (yMin - y2) * (x1 - x2) / (y1 - y2);
            }
            break;
            case ClipRectangle2D.CODE_S: {
                yc2 = yMax;
                xc2 = x2 + (yMax - y2) * (x1 - x2) / (y1 - y2);
            }
            break;
            case ClipRectangle2D.CODE_W: {
                xc2 = xMin;
                yc2 = y2 + (xMin - x2) * (y1 - y2) / (x1 - x2);
            }
            break;
            case ClipRectangle2D.CODE_E: {
                xc2 = xMax;
                yc2 = y2 + (xMax - x2) * (y1 - y2) / (x1 - x2);
            }
            break;
            // these cases are more complex, must compute 2 intersections
            case ClipRectangle2D.CODE_NE: {
                // north hline intersection
                yc2 = yMin;
                xc2 = x2 + 0.5 + (yMin - y2) * (x1 - x2) / (y1 - y2);
                // test if intersection is valid, of so then done, else compute next
                if (xc2 < xMin || xc2 > xMax) {
                    // east vline intersection
                    xc2 = xMax;
                    yc2 = y2 + 0.5 + (xMax - x2) * (y1 - y2) / (x1 - x2);
                }
            }
            break;

            case ClipRectangle2D.CODE_SE: {
                // south hline intersection
                yc2 = yMax;
                xc2 = x2 + 0.5 + (yMax - y2) * (x1 - x2) / (y1 - y2);
                // test if intersection is valid, of so then done, else compute next
                if (xc2 < xMin || xc2 > xMax) {
                    // east vline intersection
                    xc2 = xMax;
                    yc2 = y2 + 0.5 + (xMax - x2) * (y1 - y2) / (x1 - x2);
                }
            }
            break;
            case ClipRectangle2D.CODE_NW: {
                // north hline intersection
                yc2 = yMin;
                xc2 = x2 + 0.5 + (yMin - y2) * (x1 - x2) / (y1 - y2);
                // test if intersection is valid, of so then done, else compute next
                if (xc2 < xMin || xc2 > xMax) {
                    xc2 = xMin;
                    yc2 = y2 + 0.5 + (xMin - x2) * (y1 - y2) / (x1 - x2);
                }
            }
            break;
            case ClipRectangle2D.CODE_SW: {
                // south hline intersection
                yc2 = yMax;
                xc2 = x2 + 0.5 + (yMax - y2) * (x1 - x2) / (y1 - y2);
                // test if intersection is valid, of so then done, else compute next
                if (xc2 < xMin || xc2 > xMax) {
                    xc2 = xMin;
                    yc2 = y2 + 0.5 + (xMin - x2) * (y1 - y2) / (x1 - x2);
                }
            }
            break;
            default:
                break;
        }
        // do bounds check
        if ((xc1 < xMin) || (xc1 > xMax)
                || (yc1 < yMin) || (yc1 > yMax)
                || (xc2 < xMin) || (xc2 > xMax)
                || (yc2 < yMin) || (yc2 > yMax)) {
            return null;
        }
        // store vars back
        x1 = Mathem.toInt(xc1);
        y1 = Mathem.toInt(yc1);
        x2 = Mathem.toInt(xc2);
        y2 = Mathem.toInt(yc2);
        return new int[] {x1,y1,x2,y2};
    }
    
   ///////////////////////////////////////////////////////////
    // Алгоритм отсечения Коэна-Сазерленда
    public Line2D CSclip(double x0, double y0, double x1, double y1) {
	boolean visible = false;	// не видим/видим
	int cn, ck, /* Коды концов отрезка */
		ii = 4, s;      /* Рабочие переменные  */
	double dx, dy, /* Приращения координат*/
		dxdy = 0, dydx = 0, /* Наклоны отрезка к сторонам */
		r;            /* Рабочая переменная  */

	Line2D res = new Line2D(x0, y0, x1, y1, visible);
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
		y0 = y0 + dydx * (xMin - x0);
		x0 = xMin;
	    } else if ((cn & 2) != 0) {  /* Пересечение с правой стороной*/
		y0 = y0 + dydx * (xMax - x0);
		x0 = xMax;
	    } else if ((cn & 4) != 0) {  /* Пересечение в нижней стороной*/
		x0 = x0 + dxdy * (yMin - y0);
		y0 = yMin;
	    } else if ((cn & 8) != 0) {  /*Пересечение с верхней стороной*/
		x0 = x0 + dxdy * (yMax - y0);
		y0 = yMax;
	    }
	    cn = code(x0, y0);        /* Перевычисление кода точки Pn */
	} while (--ii >= 0);

	if (visible) {
	    return new Line2D(x0, y0, x1, y1, visible);
	} else {
	    return res;
	}
    }

    /////////////////////////////////////////////////////
    // Область попадания точки
    private int code(double x, double y) {
	int i = 0;
	if (x < xMin + Mathem.EPSILON_E7) {
	    ++i;
	} else if (x > xMax - Mathem.EPSILON_E7) {
	    i += 2;
	}
	if (y < yMin) {
	    i += 4;
	} else if (y > yMax) {
	    i += 8;
	}
	return i;
    }    
    /////////////////////////////////////////////////////////
    // get
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
