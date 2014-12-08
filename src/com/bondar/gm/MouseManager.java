package com.bondar.gm;

import com.bondar.geom.Solid3D;
import com.bondar.tasks.Main;
import static com.bondar.tasks.Main.ANGLE_DOWN;
import static com.bondar.tasks.Main.ANGLE_UP;
import static com.bondar.tasks.Main.CAMERA_ANGLE;
import static com.bondar.tasks.Main.RADIO_ALL_MODELS;
import static com.bondar.tasks.Main.RADIO_BY_LIST_SELECTION;
import static com.bondar.tasks.Main.RADIO_BY_MOUSE_PRESSED;
import static com.bondar.tasks.Main.SCALE_DOWN;
import static com.bondar.tasks.Main.SCALE_UP;
import static com.bondar.tasks.Main.SHIFT_BY_Z_STEP;
import static com.bondar.tasks.Main.SHIFT_STEP;
import static com.bondar.tasks.Main.ZERO_POINT;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.util.List;

/**
 *
 * @author Иван
 */
public class MouseManager {
    
    public static Solid3D allModel;
    private static Solid3D selectedModel;
    public static boolean isMousePressed;
    
    public static void init() {
	// radio button to select all models to move
	allModel = new Solid3D(RADIO_ALL_MODELS);
        selectedModel = allModel;
        isMousePressed = false;
    }

    /////////////////////////////////////////////////////////
    public static void onMouseDragged(Point curPoint, Point prevPoint, List<Solid3D> focusedModels, String operationType) {
	double dx = 0, dy = 0, dz = 0, angle = 0, scale = 0;
	Matrix.AXIS axis = Matrix.AXIS.X;
	int diffX = curPoint.x - prevPoint.x;
	int diffY = curPoint.y - prevPoint.y;
	
	dx = diffX * SHIFT_STEP;
	dy = -diffY * SHIFT_STEP;
	if (diffX > 0) {
	    angle = ANGLE_UP;
	    axis = Matrix.AXIS.Y;
	    
	} else if (diffX < 0) {
	    angle = ANGLE_DOWN;
	    axis = Matrix.AXIS.Y;
	}
	if (diffY > 0) {
	    angle = ANGLE_UP;
	    axis = Matrix.AXIS.X;
	} else if (diffY < 0) {
	    angle = ANGLE_DOWN;
	    axis = Matrix.AXIS.X;
	}
	ModelsManager.onOperation(focusedModels, angle, axis, dx, dy, dz, scale, operationType);
    }
  
    public static void onSolidListSelection(String selectedRadioText) {
        if (selectedRadioText.equals(RADIO_ALL_MODELS)) {
            // if suddenly selected solid don't finded
            selectedModel = allModel;
            return;
        }
	for (Solid3D model : ModelsManager.getModels()) {
	    if (model.getName().equals(selectedRadioText)) {
		selectedModel = model;
		return;
	    }
	}
    }
    
    /////////////////////////////////////////////////////////
    public static void onTurnSceneWithMouse(Point curPoint, Point center) {
        if (curPoint == null) return;
        
        int dx = curPoint.x - center.x;
        int dy = curPoint.y - center.y;
        CameraManager.getCam().updateDirection(dx * CAMERA_ANGLE, Matrix.AXIS.Y);
        CameraManager.getCam().updateDirection(dy * CAMERA_ANGLE, Matrix.AXIS.X);
    }
    
    /////////////////////////////////////////////////////////
    public static void onMouseWheelMoved(List<Solid3D> focusedModels, int notches, String operationType) {
	double dz, angle, scale;
	Matrix.AXIS axis;

	if (notches < 0) {
	    angle = ANGLE_UP;
	    axis = Matrix.AXIS.Z;
	    dz = notches * SHIFT_BY_Z_STEP;
	    scale = SCALE_UP;
	} else {
	    angle = ANGLE_DOWN;
	    axis = Matrix.AXIS.Z;
	    dz = notches * SHIFT_BY_Z_STEP;
	    scale = SCALE_DOWN;
	}
	ModelsManager.onOperation(focusedModels, 0, axis, 0, 0, 0, scale, operationType);
    }    
    
    /////////////////////////////////////////////////////////
    public static void onMousePressed(String objChoise, List<Solid3D> focusedModels) {
	isMousePressed = true;
	switch (objChoise) {
	    case RADIO_BY_MOUSE_PRESSED:
		for (Solid3D model : ModelsManager.getModels()) {
		    if (model.getState() != Solid3D.States.VISIBLE
			    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
		    boolean isPointInto = model.isCameraPointInto(ZERO_POINT, CameraManager.getCam());
		    // find object borders & check the cursor hit into borders
		    if (isPointInto) {
			focusedModels.add(model);
		    }
		}
		// if the mouse is pressed in an empty area,
		// then making operations with all solids
		if (focusedModels.isEmpty()) {
		    focusedModels.add(allModel);
		}
		break;
	    //
	    case RADIO_BY_LIST_SELECTION:
		focusedModels.add(selectedModel);
		break;
	}
    }
    /////////////////////////////////////////////////////////
    public static void onMouseReleased(List<Solid3D> focusedModels, Point windowCenterOnScreen) {
	isMousePressed = false;
	focusedModels.clear();
        if (Main.isGameViewModeEnabled)
            moveMouseToWindowCenter(windowCenterOnScreen);
    }
    
    public static void moveMouseToWindowCenter(Point windowCenterOnScreen) {
        try {
            Robot r = new Robot();
            r.mouseMove(windowCenterOnScreen.x, windowCenterOnScreen.y);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
}
