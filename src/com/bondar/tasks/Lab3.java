package com.bondar.tasks;

import com.bondar.panels.Application;
import com.bondar.gm.GraphicSystem;
import static com.bondar.gm.GraphicSystem.X_MAX;
import static com.bondar.gm.GraphicSystem.Y_MAX;
import com.bondar.gm.Matrix.AXIS;
import com.bondar.gm.Point2D;
import java.awt.Color;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class Lab3 extends Application {

    private static final double RADIAN_PI = Math.toDegrees(Math.PI * 2);
    private final double ELLIPSES_NUM = 3;
    private final double TRANSLATE_ANGLE = Math.toRadians(RADIAN_PI / ELLIPSES_NUM);  // 120
    private final double R1 = 1.7;
    private final double R2 = 0.8;
    private enum DrawOrScale {
	Draw,
	Scale
    }
    private DrawOrScale curMode;
    
    public static void main(String[] args) {
	new Lab3(900, 600);
    }
    
    /////////////////////////////////////////////////////////
    public Lab3(int width, int height) {
	super(width, height);
	setResizable(true);
	setTitle("LR_3 v8");
	setLocation(0, 0);
	setClip(false);
	setScale(true);
	// 1 проход для масштабирвания (не рисуем)
	curMode = DrawOrScale.Scale;
	drawRecursiveEllipses(getDrawablePanel().getGraphicSystem());
	// далее - рисуем
 	curMode = DrawOrScale.Draw;
   }

    /////////////////////////////////////////////////////////
    @Override
    protected void paint(GraphicSystem g) {
	g.clear();
	g.setColor(Color.GRAY);
	drawRecursiveEllipses(g);
    }

    /////////////////////////////////////////////////////////
    private void drawRecursiveEllipses(GraphicSystem g) {
	final int depth = 5;
	final double scale = 1;
	final double x0 = X_MAX / 2;
	final double y0 = Y_MAX / 2;
	final Point2D center = new Point2D(x0, y0);
	final Point2D coord = new Point2D(x0, y0);

	drawRecursiveEllipses(g, scale, center, coord, depth);
    }
 
    private void drawRecursiveEllipses(GraphicSystem g, 
	    double scale, Point2D center, Point2D coord, int depth) {
	if (depth-- < 0) return;
	//
	draw3Ellipses(g, scale, center, coord);
	//
	scale *= 0.4;
	Point2D oldCenter = new Point2D(coord.getX(), coord.getY());
	double newCenterX = coord.getX();
	double newCenterY = coord.getY() - 5 * R1 * scale;
	for (int i = 0; i < ELLIPSES_NUM; i++) {
	    //
	    Point2D newCenter = new Point2D(newCenterX, newCenterY);
	    drawRecursiveEllipses(g, scale, oldCenter, newCenter, depth);

	    double dx = newCenterX - oldCenter.getX();
	    double dy = newCenterY - oldCenter.getY();
	    newCenterX = oldCenter.getX() + dx * Math.cos(TRANSLATE_ANGLE) - dy * Math.sin(TRANSLATE_ANGLE);
	    newCenterY = oldCenter.getY() + dx * Math.sin(TRANSLATE_ANGLE) + dy * Math.cos(TRANSLATE_ANGLE);
	}
    }
    
    private void draw3Ellipses(GraphicSystem g, double scale, Point2D center, Point2D coord) {
	double x0 = center.getX();
	double y0 = center.getY();
	double x = coord.getX();
	double y = coord.getY() + R1 * scale;
	double angle = Math.toRadians(90);
	for (int i = 0; i < ELLIPSES_NUM; i++) {
	    g.reset();
	    g.scale(scale);
	    g.translate(x, y);
	    g.rotate(angle, AXIS.Z);
	    angle -= TRANSLATE_ANGLE;

	    drawEllipse(g, R1, R2);

	    double dx = x - x0;
	    double dy = y - y0;
	    x = x0 + dx * Math.cos(TRANSLATE_ANGLE) - dy * Math.sin(TRANSLATE_ANGLE);
	    y = y0 + dx * Math.sin(TRANSLATE_ANGLE) + dy * Math.cos(TRANSLATE_ANGLE);
	}
    }

    private void drawEllipse(GraphicSystem g, double r1, double r2) {
	final double sides = 30;
	final double ca = Math.toRadians(RADIAN_PI / sides); // 12
	double x = r1 * Math.cos(ca);
	double y = r2 * Math.sin(ca);
	g.move(x, y);
	for (int j = 0; j < sides; j++) {
	    double angle = 0;
	    while (angle <= Math.PI * 2) {
		x = r1 * Math.cos(angle);
		y = r2 * Math.sin(angle);
		
		if (curMode == DrawOrScale.Scale)
		    g.setScale(x, y);
		else if (curMode == DrawOrScale.Draw){
		    g.draw(x, y);
		}
		angle += ca;
	    }
	}
    }
}
