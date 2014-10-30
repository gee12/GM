package com.bondar.gm;

import com.bondar.geom.Point2D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DInds;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author truebondar
 */
public class DrawManager {
    
    private Graphics g;
    private int width, height;
    
    //////////////////////////////////////////////////
    public void setGraphics(Graphics g) {
	this.g = g;
	this.width = g.getClipBounds().width;
	this.height = g.getClipBounds().height;

	drawBackground(Color.WHITE);
    }
    
    public void setColor(Color color) {
	g.setColor(color);
    }  
    
    /////////////////////////////////////////////////////
    public void drawBackground(Color col) {
	g.setColor(col);
	g.fillRect(0, 0, width, height);
	g.setColor(Color.BLACK);
    }    
    
    /////////////////////////////////////////////////////
    // Алгоритм художника
    public void painterAlgorithm(Polygon3DInds[] polies) {
	Polygon3DInds[] sortPolies = sortTrianglesByZ(polies);
	if (sortPolies == null) {
	    return;
	}
	for (Polygon3DInds poly : sortPolies) {
	    drawFilledPolygon3D(poly);
	}
    }
    
    public void drawFilledPolygon3D(Polygon3D poly) {
	g.setColor(poly.getFillColor());
	switch(poly.getType()) {
	    case POINT:
		drawScreenPoint(poly.getVertex(0));
		break;
	    case LINE:
		drawScreenLine(poly.getVertex(0), poly.getVertex(1));
		break;
	    default:
		drawScreenFilledPolygon(poly.getVertexes());
		break;
	}
    }
    
    /////////////////////////////////////////////////////
    // draw point
    public void drawScreenPoint(Point3D p) {
	if (p == null) return;
	drawScreenPoint(p.getX(), p.getY());
    }

    public void drawScreenPoint(double x, double y) {
	g.drawLine((int)(x + 0.5), (int)(y + 0.5),
		(int)(x + 0.5), (int)(y + 0.5));	    
     }
    
    public void drawScreenPoint(Point3D p, Color col) {
	if (p == null) return;
	drawScreenPoint(p.getX(), p.getY(), col);
    }

    public void drawScreenPoint(double x, double y, Color col) {
	g.setColor(col);
	g.drawLine((int)(x + 0.5), (int)(y + 0.5),
		(int)(x + 0.5), (int)(y + 0.5));	    
    }
        
    /////////////////////////////////////////////////////
    // draw line
    public void drawScreenLine(Point3D p1, Point3D p2) {
	if (p1 == null || p2 == null) return;
	drawScreenLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public void drawScreenLine(Point2D p1, Point2D p2) {
	if (p1 == null || p2 == null) return;
	drawScreenLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }  
    
    public void drawScreenLine(double x1, double y1, double x2, double y2) {
	g.drawLine((int)(x1 + 0.5), (int)(y1 + 0.5), (int)(x2 + 0.5), (int)(y2 + 0.5));
    }
    
/////////////////////////////////////////////////////
    // draw filled polygon
    public void drawScreenFilledPolygon(Point3D[] verts) {
	if (verts == null) return;
	int size = verts.length;
	int xs[] = new int[size];
	int ys[] = new int[size];
	for (int i = 0; i < size; i++) {
	    if (verts[i] == null) continue;
	    xs[i] = (int)(verts[i].getX() + 0.5);
	    ys[i] = (int)(verts[i].getY() + 0.5);
	}
	g.fillPolygon(xs, ys, size);
    }
    public void drawScreenPolygonBorder(Point3D[] verts) {
	if (verts == null) return;
	int size = verts.length;
	for (int i = 0; i < size-1; i++) {
	    drawScreenLine(verts[i], verts[i+1]);
	}
	drawScreenLine(verts[size-1], verts[0]);
    }
    
   // Сортировка граней по координате Z
    public static Polygon3DInds[] sortTrianglesByZ(Polygon3DInds[] polies) {
	if (polies == null) {
	    return null;
	}
	int size = polies.length;
	double[] dists = new double[size];
	int[] indexes = new int[size];
	// нахождение средней величины Z - удаленности грани
	for (int i = 0; i < size; i++) {
	    Polygon3DInds poly = polies[i];
	    dists[i] = poly.averageZ();
	    indexes[i] = i;
	}
	Polygon3DInds[] res = new Polygon3DInds[size];
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
	    res[i] = polies[indexes[i]].getCopy();
	}
	return res;
    }
}
