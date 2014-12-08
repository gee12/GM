package com.bondar.gm;

import com.bondar.geom.Point2D;
import com.bondar.geom.Solid2D;
import com.bondar.geom.Solid3D;
import com.bondar.tasks.Main;
import static com.bondar.tasks.Main.FONT_COLOR;
import static com.bondar.tasks.Main.GROUP_TITLE_OBJ_CHOISE;
import static com.bondar.tasks.Main.RADIO_BY_LIST_SELECTION;
import static com.bondar.tasks.Main.ZERO_POINT;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Иван
 */
public class CursorManager {
    
    public static final Color CROSSHAIR_COLOR_NORM = FONT_COLOR;
    public static final Color CROSSHAIR_COLOR_ALLERT = Color.RED;
    
    private static Cursor EMPTY_CURSOR;
    private static Solid2D crosshair;
    
    public static void init(Toolkit toolkit, Point cx) {
        EMPTY_CURSOR = toolkit.createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),"null");
        //cx = cx.sub(new Point2D(50,50));
        final int SIZE = 10;
        Point2D[] points = new Point2D[4];
        points[0] = new Point2D(cx.getX(), cx.getY() + SIZE);
        points[1] = new Point2D(cx.getX(), cx.getY() - SIZE);
        points[2] = new Point2D(cx.getX() - SIZE, cx.getY());
        points[3] = new Point2D(cx.getX() + SIZE, cx.getY());
        crosshair = new Solid2D(points, CROSSHAIR_COLOR_NORM);
        
    }
    
    public static void onHit(List<Solid3D> models) {
	for (Solid3D model : models) {
	    if (model.getState() != Solid3D.States.VISIBLE
		    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (model.isCameraPointInto(Main.ZERO_POINT, CameraManager.getCam())) {
		setColor(CROSSHAIR_COLOR_ALLERT);
		return;
	    }
        }
        setColor(CROSSHAIR_COLOR_NORM);
    }
    
    /////////////////////////////////////////////////////////
    public static int onCursorSwitch(String objChoise, List<Solid3D> models) {
        if (models == null) return Cursor.DEFAULT_CURSOR;
	// set cursor type
	if (objChoise.equals(RADIO_BY_LIST_SELECTION)) {
	    return Cursor.MOVE_CURSOR;
	}
	for (Solid3D model : models) {
	    if (model.getState() != Solid3D.States.VISIBLE
		    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (model.isCameraPointInto(ZERO_POINT, CameraManager.getCam())) {
		return Cursor.MOVE_CURSOR;
	    }
        }
        return Cursor.DEFAULT_CURSOR;
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
