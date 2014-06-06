package com.bondar.tasks;

import com.bondar.panels.Application;
import com.bondar.gm.GraphicSystem;
import com.bondar.gm.Line;
import com.bondar.gm.Point2D;
import com.bondar.gm.Polygon2D;
import java.awt.Color;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class Lab4 extends Application {

    private Polygon2D polygon;
    private Line[] decompLines;
    
    public static void main(String[] args) {
	new Lab4(900, 600);
    }

    /////////////////////////////////////////////////////////
    public Lab4(int width, int height) {
	super(width, height);
	setResizable(true);
	setTitle("LR_4 v8");
	setLocation(0, 0);
	setClip(false);
	setScale(false);
	
	final double indent = GraphicSystem.BORDER;
	final double xMax = GraphicSystem.X_MAX;
	final double yMax = GraphicSystem.Y_MAX;
	final double thikness = 1d;
	// обход точек ПРОТИВ часовой стрелки
	polygon = new Polygon2D(new Point2D[] {
	    /*new Point2D(indent,indent),
	    new Point2D(xMax - indent,indent),
	    new Point2D(xMax - indent,yMax - indent),
	    new Point2D(xMax/2 + thikness,yMax - indent),
	    new Point2D(xMax/2 + thikness,yMax - indent - thikness),
	    new Point2D(xMax - indent - thikness,yMax - indent - thikness),
	    
	    new Point2D(xMax - indent - thikness,indent + thikness),
	    new Point2D(indent + thikness,indent + thikness),
	    new Point2D(indent + thikness,yMax - indent - thikness),
	    new Point2D(xMax/2 - thikness,yMax - indent - thikness),
	    new Point2D(xMax/2 - thikness,yMax - indent),
	    new Point2D(indent,yMax - indent),*/
	    
	    new Point2D(1,1),
	    new Point2D(3,2),
	    new Point2D(3.5,5),
	    new Point2D(4,2),
	    new Point2D(6.5,1),
	    new Point2D(6.5,5),
	    new Point2D(4.5,4.5),
	    new Point2D(3.5,6),
	    new Point2D(3,4.5),
	    new Point2D(1,5)
	});
	
	// true -> обход точек ПРОТИВ часовой стрелки
	decompLines = polygon.getDecompositionLines(true);
    }

    /////////////////////////////////////////////////////////
    @Override
    protected void paint(GraphicSystem g) {
	g.clear();
	g.setColor(Color.GRAY);
	drawPolygon(g, polygon);
	drawLines(g, decompLines);
    }

    private void drawPolygon(GraphicSystem g, Polygon2D poly) {
	if (poly == null) return;
	int size = poly.getSize();
	if (size == 0) return;
	g.reset();
	g.setColor(Color.BLACK);
	
	g.move(poly.getPoints()[size - 1]);
	for (int i = 0; i < size; i++) {
	    g.draw(poly.getPoints()[i]);
	}
    }
    
    private void drawLines(GraphicSystem g, Line[] lines) {
	if (lines == null) return;
	g.reset();
	g.setColor(Color.RED);
	
	for (int i = 0; i < lines.length; i++) {
	    g.move(lines[i].getP1());
	    g.draw(lines[i].getP2());
	}	
    }
}
