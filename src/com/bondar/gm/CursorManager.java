package com.bondar.gm;

import com.bondar.geom.Point2D;
import com.bondar.geom.Solid2D;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author Иван
 */
public class CursorManager {
    
    private static Cursor EMPTY_CURSOR;
    private static Solid2D crosshair;
    
    public static void init(Toolkit toolkit, Color initColor, int width, int height) {
        EMPTY_CURSOR = toolkit.createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),"null");
        // init crosshair 
        Point2D cx = new Point2D(width/2,height/2);//getScreenCenter());
        //cx = cx.sub(new Point2D(50,50));
        final int SIZE = 10;
        Point2D[] points = new Point2D[4];
        points[0] = new Point2D(cx.getX(), cx.getY() + SIZE);
        points[1] = new Point2D(cx.getX(), cx.getY() - SIZE);
        points[2] = new Point2D(cx.getX() - SIZE, cx.getY());
        points[3] = new Point2D(cx.getX() + SIZE, cx.getY());
        crosshair = new Solid2D(points, initColor);
        
    }
    
    public static void setColor(Color col) {
        crosshair.setColor(col);
    }
    
    public static Cursor getEmptyCursor() {
        return EMPTY_CURSOR;
    }

    public static Solid2D getCrosshair() {
        return crosshair;
    }
}
