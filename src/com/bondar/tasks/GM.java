package com.bondar.tasks;

import com.bondar.geom.Solid3D;
import com.bondar.geom.Point2D;
import com.bondar.geom.Vector3D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.panels.Application;
import com.bondar.gm.*;
import com.bondar.panels.OptionsPanelListener;
import com.bondar.tools.Types;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import jdk.net.Sockets;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class GM extends Application implements OptionsPanelListener {

    public static final int FPS = 60;
    public static final int MSEC_TICK = 1000/FPS;
    public static final int SCREEN_WIDTH = 1100;
    public static final int SCREEN_HEIGHT = 600;
    public static final String TITLE_TEXT = "GM";
    
    public static final double SHIFT_STEP = 0.008;
    public static final double SHIFT_BY_Z_STEP = 0.1;
    public static final double ANGLE_UP = Math.toRadians(6);
    public static final double ANGLE_DOWN = Math.toRadians(-6);
    public static final double SCALE_UP = 0.5;
    public static final double SCALE_DOWN = -SCALE_UP;
    public static final double CAMERA_SHIFT_STEP = 0.5;
    public static final double CAMERA_ANGLE = Math.toRadians(1) / 20;
    
    public static final int VIEW_MODE_KEY = KeyEvent.VK_TAB;
    
    public static final String GROUP_TITLE_OBJECTS_TEXT = "Объекты:";
    public static final String RADIO_ALL_MODELS_TEXT = "Все";
    public static final String GROUP_TITLE_OBJ_CHOISE_TEXT = "ВЫБОР ОБЪЕКТА:";
    public static final String RADIO_BY_MOUSE_PRESSED_TEXT = "по захвату мышей";
    public static final String RADIO_BY_LIST_SELECTION_TEXT = "по выбору из списка";
    public static final String GROUP_TITLE_OPERATIONS_TEXT = "ОПЕРАЦИИ:";
    public static final String RADIO_ROTATE_TEXT = "Поворот";
    public static final String RADIO_TRANSFER_TEXT = "Перемещение";
    public static final String RADIO_SCALE_TEXT = "Масштабирование";
    public static final String GROUP_TITLE_CAMERA_TEXT = "КАМЕРА:";
    public static final String RADIO_CAMERA_EULER_TEXT = "Эйлера";
    public static final String RADIO_CAMERA_UVN_TEXT = "UVN";
    public static final String GROUP_TITLE_CLIPPING_TEXT = "ОТСЕЧЕНИЕ:";
    public static final String RADIO_PAINTER_TEXT = "Алгритм художника";
    public static final String RADIO_BACKFACES_EJECTION_TEXT = "Отброс невидимых полигонов";
    public static final String GROUP_TITLE_VIEW_TEXT = "ВНЕШНИЙ ВИД:";
    public static final String RADIO_EDGES_TEXT = "Ребра";
    public static final String RADIO_FACES_TEXT = "Грани";
    public static final String RADIO_EDGES_FACES_TEXT = "Ребра и грани";
    public static final String GROUP_TITLE_SHADE_TEXT = "Затенение:";
    public static final String RADIO_SHADE_CONST_TEXT = "CONSTANT";
    public static final String RADIO_SHADE_FLAT_TEXT = "Плоское";
    public static final String RADIO_SHADE_GOURAD_TEXT = "Гуро";
    public static final String RADIO_SHADE_FONG_TEXT = "Фонг";

    public static final String CHECKBOX_SHIFT_IF_INTERSECT_TEXT = "Сдвигать при пересечении";
    
    private static Cursor EMPTY_CURSOR;
    private static final Point2D ZERO_POINT = new Point2D();
    private static final Color CROSSHAIR_COLOR_NORM = Color.WHITE;
    private static final Color CROSSHAIR_COLOR_ALLERT = Color.RED;
    private static Color crosshairColor = CROSSHAIR_COLOR_NORM;
    
    
    private final List<Solid3D> focusedModels = new ArrayList<>();
    private Solid3D selectedModel;
    private Solid3D allModel;
    private Camera camera;
    private CameraEuler cameraEuler;
    private CameraUVN cameraUVN;
    private boolean isMousePressed;
    private boolean isGameViewModeEnabled;
    private final ShadeManager shadeManager = new ShadeManager();
    private final ModelsManager modelsManager = new ModelsManager();
    private final RenderManager renderManager = new RenderManager();
	
    public static void main(String[] args) {
	new GM(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /////////////////////////////////////////////////////////
    public GM(int width, int height) {
	super(width, height);
	requestFocus();
	setFocusable(true);
	setResizable(true);
	setTitle(TITLE_TEXT);
	setLocation(50, 50);
        // for VK_TAB working
        setFocusTraversalKeysEnabled(false);
        //
	addListeners();
	addControls();
	start(MSEC_TICK);
    }
    
    private void addListeners() {
	//
	addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent evt) {
		onKeyPressed(evt);
	    }

            @Override
            public void keyReleased(KeyEvent evt) {
                onKeyReleased(evt);
            }
            
	});
	//
	addMouseMotionListener(new MouseMotionListener() {
	    // while be called mouseDragged() method, 
	    // mouseMoved() will not be called
	    private Point oldPoint = new Point();
            
	    @Override
	    public void mouseDragged(MouseEvent me) {
		Point curPoint = me.getPoint();
                onMouseDragged(curPoint, oldPoint);
                oldPoint = curPoint;
	    }
	    @Override
	    public void mouseMoved(MouseEvent me) {
                Point curPoint = me.getPoint();
                
                // return mouse cursor to center of screen (if need)
                if (isGameViewModeEnabled) {
                    onTurnSceneWithMouse(curPoint);
                    moveMouseToCenter();
                }
                else onCursorSwitch();
                
		oldPoint = curPoint;
	    }
	});
	//
	addMouseListener(new MouseListener() {
	    @Override
	    public void mousePressed(MouseEvent me) {
		onMousePressed(me.getPoint());
	    }
	    @Override
	    public void mouseReleased(MouseEvent me) {
		onMouseReleased();
	    }
	    @Override
	    public void mouseClicked(MouseEvent me) {
	    }
	    @Override
	    public void mouseEntered(MouseEvent me) {
	    }
	    @Override
	    public void mouseExited(MouseEvent me) {
	    }
	});
	//
	addMouseWheelListener(new MouseWheelListener() {
	    @Override
	    public void mouseWheelMoved(MouseWheelEvent mwe) {
		onMouseWheelMoved(mwe.getWheelRotation());
	    }
	});
	//
	addComponentListener(new ComponentListener() {
	    @Override
	    public void componentResized(ComponentEvent e) {
	    }
	    @Override
	    public void componentMoved(ComponentEvent e) {
	    }
	    @Override
	    public void componentShown(ComponentEvent e) {
	    }
	    @Override
	    public void componentHidden(ComponentEvent e) {
	    }
	});
    }
    
    private void addControls() {
	// radioButtons
 	addRadio(GROUP_TITLE_OBJECTS_TEXT, RADIO_ALL_MODELS_TEXT, this);

	addRadio(GROUP_TITLE_OBJ_CHOISE_TEXT, RADIO_BY_MOUSE_PRESSED_TEXT, this);
	addRadio(GROUP_TITLE_OBJ_CHOISE_TEXT, RADIO_BY_LIST_SELECTION_TEXT, this);

	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_ROTATE_TEXT, this);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_TRANSFER_TEXT, this);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_SCALE_TEXT, this);

	//addRadio(GROUP_TITLE_CAMERA_TEXT, RADIO_CAMERA_EULER_TEXT, this);
	//addRadio(GROUP_TITLE_CAMERA_TEXT, RADIO_CAMERA_UVN_TEXT, this);

	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_PAINTER_TEXT, this);
	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_BACKFACES_EJECTION_TEXT, this);

	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_FACES_TEXT, this);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_FACES_TEXT, this);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_TEXT, this);
	// checkBox
	//addCheckBox(CHECKBOX_SHIFT_IF_INTERSECT_TEXT, false, this);
        
        addRadio(GROUP_TITLE_SHADE_TEXT, RADIO_SHADE_CONST_TEXT, this);
        addRadio(GROUP_TITLE_SHADE_TEXT, RADIO_SHADE_FLAT_TEXT, this);
        addRadio(GROUP_TITLE_SHADE_TEXT, RADIO_SHADE_GOURAD_TEXT, this);
        addRadio(GROUP_TITLE_SHADE_TEXT, RADIO_SHADE_FONG_TEXT, this);
    }

    /////////////////////////////////////////////////////////
    @Override
    protected final void load() {
	modelsManager.load();
	//
	/*for (Solid3D model : modelsManager.getModels()) {
	    if (!model.isSetAttribute(Solid3D.ATTR_FIXED)) {
		addRadio(GROUP_TITLE_OBJECTS_TEXT, model.getName(), this);
	    }
	}*/
	//
	shadeManager.load();
    }

    @Override
    protected final void init() {
	isMousePressed = false;
        EMPTY_CURSOR = getToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),"null");
	// radio button to select all models to move
	allModel = new Solid3D(RADIO_ALL_MODELS_TEXT);
	// init camera
	Point3DOdn pos = new Point3DOdn(0,0,-10);
	Vector3D dir = new Vector3D(0,0,0);
	Point3D target = new Point3D(0,0,10);
	double near = 3, far = 1000, dist = 3, fov = 90;
	Dimension vp = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
	cameraEuler = new CameraEuler(0, pos, dir, near, far, dist, fov, vp, CameraEuler.CAM_ROT_SEQ_ZYX);
	cameraUVN = new CameraUVN(0, pos, dir, near, far, dist, fov, vp, target, CameraUVN.UVN_MODE_SIMPLE);
	camera = cameraEuler;
        
        /*// init fixed values in models
        for (Solid3D model : modelsManager.getModels()) {
            model.resetBounds();
            for (Polygon3D poly: model.getPolygons()) {
                poly.resetNormal();
                poly.resetAverageZ();
            }
        }*/
    }

    /////////////////////////////////////////////////////////
    @Override
    protected void update() {
        boolean isNeedDefineBackfaces = 
		getSelectedRadioText(GROUP_TITLE_CLIPPING_TEXT).equals(RADIO_BACKFACES_EJECTION_TEXT);
        
        // 1 - work with isolated objects
        modelsManager.updateAndAnimate(camera, isNeedDefineBackfaces);
	
	// 2 - work with render array (visible polygons)
	renderManager.buildRenderArray(modelsManager.getModels());
        
        onShading();
        
	renderManager.sortByZ(RenderManager.SortByZTypes.AVERAGE_Z);
        renderManager.transToPerspectAndScreen(camera);
        
	// 3 - 
	onCollision();
        
        //
        //onCrosshair();
    }
    
    /////////////////////////////////////////////////////////
    private void onShading() {
        switch(getSelectedRadioText(GROUP_TITLE_SHADE_TEXT)) {
            
            case RADIO_SHADE_CONST_TEXT:
                break;
            case RADIO_SHADE_FLAT_TEXT:
                renderManager.setRenderArray(
                        shadeManager.flatShade(renderManager.getRenderArray()));
                break;
            case RADIO_SHADE_GOURAD_TEXT:
                renderManager.setRenderArray(
                        shadeManager.gouradShade(renderManager.getRenderArray()));
                break;
            case RADIO_SHADE_FONG_TEXT:
                
                break;
        }
    }
    
    /////////////////////////////////////////////////////////
    private void onCollision() {
	// if collision check is off
	if (!isSelectedCheckBox(CHECKBOX_SHIFT_IF_INTERSECT_TEXT)
		|| isMousePressed) return;
	//InteractionManager.collision(modelsManager.getModels());
    }
  
    /////////////////////////////////////////////////////////
    private void onOperation(List<Solid3D> models, double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale) {
	// if selected all models
	if (models.contains(allModel)) {
	    models.clear();
	    models.addAll(Types.toList(modelsManager.getModels()));
	}
	// select operation
	switch (getSelectedRadioText(GROUP_TITLE_OPERATIONS_TEXT)) {
	    case RADIO_ROTATE_TEXT:
		onRotate(models, angle, axis);
		break;
	    case RADIO_TRANSFER_TEXT:
		onTransfer(models, dx, dy, dz);
		break;
	    case RADIO_SCALE_TEXT:
		onScale(models, scale);
		break;
	}
    }
    
    /////////////////////////////////////////////////////////
    private void onCameraOperation(double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double tx, double ty, double tz) {
	camera.updateDirection(angle, axis);
	camera.updatePosition(dx, dy, dz);
	//camera.updateTarget(tx, ty, tz);
    }
    
    private void onCameraSelection(String cameraType) {
	switch(cameraType) {
	    case RADIO_CAMERA_EULER_TEXT:
		camera = cameraEuler;
		break;
	    case RADIO_CAMERA_UVN_TEXT:
		camera = cameraUVN;
		break;
	}
    }

    private void onRotate(List<Solid3D> models, double angle, Matrix.AXIS axis) {
	for (Solid3D model : models) {
	    //if (model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    model.updateAngle(angle, axis);
	}
    }

    private void onTransfer(List<Solid3D> models, double dx, double dy, double dz) {
	for (Solid3D model : models) {
	    //if (model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    model.updateTransfers(dx, dy, dz);
	}
    }

    private void onScale(List<Solid3D> models, double scale) {
	for (Solid3D solid : models) {
	    //if (solid.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    solid.updateScale(scale);
	}
    }
    
    private void onSolidListSelection(String selectedRadioText) {
	for (Solid3D model : modelsManager.getModels()) {
	    if (model.getName().equals(selectedRadioText)) {
		selectedModel = model;
		return;
	    }
	}
	// if suddenly selected solid don't finded
	selectedModel = allModel;
    }

    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
	switch(groupTitle) {
	    case GROUP_TITLE_OBJECTS_TEXT:
		onSolidListSelection(radioText);
		break;
	    case GROUP_TITLE_CAMERA_TEXT:
		onCameraSelection(radioText);
		break;
	}
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
    }

    @Override
    public void onSliderChanged(String sliderName, int value) {
    }
    
    /////////////////////////////////////////////////////////
    public void onMouseDragged(Point curPoint, Point prevPoint) {
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
	onOperation(focusedModels, angle, axis, dx, dy, dz, scale);
    }
    
    /////////////////////////////////////////////////////////
    public void onTurnSceneWithMouse(Point curPoint) {
        if (curPoint == null) return;
        
        int dx = curPoint.x - SCREEN_WIDTH/2;
        int dy = curPoint.y - SCREEN_HEIGHT/2;
        camera.updateDirection(dx * CAMERA_ANGLE, Matrix.AXIS.Y);
        //camera.updateDirection(dy * CAMERA_ANGLE, Matrix.AXIS.X);
        //
        //moveMouseToCenter();
    }

    protected void onCrosshair() {
	for (Solid3D model : modelsManager.getModels()) {
	    if (model.getState() != Solid3D.States.VISIBLE
		    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (model.isCameraPointInto(ZERO_POINT, camera)) {
		crosshairColor = CROSSHAIR_COLOR_ALLERT;
		return;
	    }
        }
        crosshairColor = CROSSHAIR_COLOR_NORM;
    }
    
    /////////////////////////////////////////////////////////
    public void onCursorSwitch(/*Point curPoint*/) {
	if (/*curPoint == null || */modelsManager == null || modelsManager.getModels() == null) return;
	// set cursor type
	if (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE_TEXT).equals(RADIO_BY_LIST_SELECTION_TEXT)) {
	    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	    return;
	}
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	//Point2D p = getGraphicSystem().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
	for (Solid3D model : modelsManager.getModels()) {
	    if (model.getState() != Solid3D.States.VISIBLE
		    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (model.isCameraPointInto(ZERO_POINT, camera)) {
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		return;
	    }
        }
    }
    
    /////////////////////////////////////////////////////////
    public void onMouseWheelMoved(int notches) {
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
	onOperation(focusedModels, 0, axis, 0, 0, 0, scale);
    }

    /////////////////////////////////////////////////////////
    public void onMousePressed(Point curPoint) {
	if (curPoint == null) return;
	isMousePressed = true;
	switch (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE_TEXT)) {
	    //
	    case RADIO_BY_MOUSE_PRESSED_TEXT:
		//Point2D p = getDrawManager().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
		for (Solid3D model : modelsManager.getModels()) {
		    if (model.getState() != Solid3D.States.VISIBLE
			    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
		    boolean isPointInto = model.isCameraPointInto(ZERO_POINT, camera);
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
	    case RADIO_BY_LIST_SELECTION_TEXT:
		focusedModels.add(selectedModel);
		break;
	}
    }

    /////////////////////////////////////////////////////////
    public void onMouseReleased() {
	isMousePressed = false;
	focusedModels.clear();
        if (isGameViewModeEnabled)
            moveMouseToCenter();
    }
    
    public void moveMouseToCenter() {
        try {
            Robot r = new Robot();
            Point p = getScreenCenter();
            r.mouseMove(p.x, p.y);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
    
    public Point getScreenCenter() {
        Point p = getLocationOnScreen();
        int cx = (int) (p.x + SCREEN_WIDTH / 2);
        int cy = (int) (p.y + SCREEN_HEIGHT / 2);
        return new Point(cx, cy);
    }
    
    public boolean isCursonInScreenCenter(Point point) {
        if (point == null) return false;
        Point center = getScreenCenter();
        return (point.x == center.x && point.y == center.y);
    }

    /////////////////////////////////////////////////////////
    public void onKeyPressed(KeyEvent evt) {
	double dx = 0, dy = 0, dz = 0, angle = 0,
		tx = 0, ty = 0, tz = 0;
	Matrix.AXIS axis = Matrix.AXIS.None;
	int keyCode = evt.getKeyCode();
	switch(keyCode) {
            // position to the sides
	    case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
		dx = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
		dx = -CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_SPACE:
		dy = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_SHIFT:
		dy = -CAMERA_SHIFT_STEP;
		break;
	    // position to up/down
	    case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
		dz = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
		dz = -CAMERA_SHIFT_STEP;
		break;
	    // direction/target
	    case KeyEvent.VK_NUMPAD8:
		angle = -CAMERA_ANGLE;
		axis = Matrix.AXIS.X;
		ty = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_NUMPAD2:
		angle = CAMERA_ANGLE;
		axis = Matrix.AXIS.X;
		ty = -CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_NUMPAD4:
		angle = -CAMERA_ANGLE;
		axis = Matrix.AXIS.Y;
		tx = -CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_NUMPAD6:
		angle = CAMERA_ANGLE;
		axis = Matrix.AXIS.Y;
		tx = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_NUMPAD9:
		angle = CAMERA_ANGLE;
		axis = Matrix.AXIS.Z;
		tz = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_NUMPAD3:
		angle = -CAMERA_ANGLE;
		axis = Matrix.AXIS.Z;
		tz = -CAMERA_SHIFT_STEP;
		break;
        }
	onCameraOperation(angle, axis, 
		dx, dy, dz, 
		tx, ty, tz);
    }
    
    public void onKeyReleased(KeyEvent evt) {
	int keyCode = evt.getKeyCode();
	switch(keyCode) {
            // screen focus
            case VIEW_MODE_KEY:
                switchViewMode();
            break;
        }
    }
    public void switchViewMode() {
        if (isGameViewModeEnabled) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            setCursor(EMPTY_CURSOR);
            moveMouseToCenter();
        }
        setVisibleOptionsPanel(isGameViewModeEnabled);
        isGameViewModeEnabled = !isGameViewModeEnabled;
    }
    /////////////////////////////////////////////////////////
    @Override
    protected void paint(DrawManager g) {
	//g.drawBackground();
//        g.fillRectangle(new Rectangle(0,0,
//                SCREEN_WIDTH,SCREEN_HEIGHT/2), Color.DARK_GRAY);
//        g.fillRectangle(new Rectangle(0,SCREEN_HEIGHT/2,
//                SCREEN_WIDTH,SCREEN_HEIGHT/2), Color.BLACK);
	if (modelsManager == null ||
                modelsManager.getModels() == null) return;
	//
//	drawPolies(g, renderManager.getRenderArray());
        //
//        g.setColor(crosshairColor);
//        g.drawLine(CROSSHAIR[0], CROSSHAIR[1]);
//        g.drawLine(CROSSHAIR[2], CROSSHAIR[3]);
        
        g.drawScene(renderManager.getRenderArray(), 
                getSelectedRadioText(GROUP_TITLE_VIEW_TEXT),
                getSelectedRadioText(GROUP_TITLE_SHADE_TEXT),
                crosshairColor);
    }

    /////////////////////////////////////////////////////////
//    private void drawPolies(DrawManager g, Polygon3DVerts[] polies) {
//	if (g == null) return;
//	switch (getSelectedRadioText(GROUP_TITLE_VIEW_TEXT)) {
//	    case RADIO_FACES_TEXT:
//		g.drawPolies(polies);
//		break;
//	    case RADIO_EDGES_TEXT:
//		g.drawBorders(polies);
//		break;
//	    case RADIO_EDGES_FACES_TEXT:
//		g.drawBorderedPolies(polies);
//		break;
//	}
//    }
}
