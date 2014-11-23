package com.bondar.tasks;

import com.bondar.geom.Solid3D;
import com.bondar.geom.Point2D;
import com.bondar.panels.Application;
import com.bondar.gm.*;
import com.bondar.panels.OptionsPanelListener;
import com.bondar.tools.Types;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class Main extends Application implements OptionsPanelListener {

    public static final int FPS = 30;
    public static final int MSEC_TICK = 1000/FPS;
    public static final int WINDOW_WIDTH = 1100;
    public static final int WINDOW_HEIGHT = 600;
    public static final String TITLE = "GM";
    
    public static final double SHIFT_STEP = 0.008;
    public static final double SHIFT_BY_Z_STEP = 0.1;
    public static final double ANGLE_UP = Math.toRadians(6);
    public static final double ANGLE_DOWN = Math.toRadians(-6);
    public static final double SCALE_UP = 0.5;
    public static final double SCALE_DOWN = -SCALE_UP;
    public static final double CAMERA_SHIFT_STEP = 0.5;
    public static final double CAMERA_ANGLE = Math.toRadians(1) / 20;
    
    public static final int VIEW_MODE_KEY = KeyEvent.VK_TAB;
    
    public static final String GROUP_TITLE_OBJECTS = "Объекты:";
    public static final String RADIO_ALL_MODELS = "Все";
    public static final String GROUP_TITLE_OBJ_CHOISE = "ВЫБОР ОБЪЕКТА:";
    public static final String RADIO_BY_MOUSE_PRESSED = "по захвату мышей";
    public static final String RADIO_BY_LIST_SELECTION = "по выбору из списка";
    public static final String GROUP_TITLE_OPERATIONS = "ОПЕРАЦИИ:";
    public static final String RADIO_ROTATE = "Поворот";
    public static final String RADIO_TRANSFER = "Перемещение";
    public static final String RADIO_SCALE = "Масштабирование";
    public static final String GROUP_TITLE_CAMERA = "КАМЕРА:";
    public static final String RADIO_CAMERA_EULER = "Эйлера";
    public static final String RADIO_CAMERA_UVN = "UVN";
    public static final String GROUP_TITLE_RENDER = "РЕНДЕРИНГ:";
    public static final String RADIO_PAINTER = "Алгритм художника";
    public static final String GROUP_TITLE_VIEW = "ВНЕШНИЙ ВИД:";
    public static final String RADIO_EDGES = "Ребра";
    public static final String RADIO_FACES = "Грани";
    public static final String RADIO_EDGES_FACES = "Ребра и грани";
    public static final String GROUP_TITLE_SHADE = "Затенение:";
    public static final String RADIO_SHADE_CONST = "CONSTANT";
    public static final String RADIO_SHADE_FLAT = "Плоское";
    public static final String RADIO_SHADE_GOURAD = "Гуро";
    public static final String RADIO_SHADE_FONG = "Фонг";

    public static final String CHECKBOX_SHIFT_IF_INTERSECT = "Сдвигать при пересечении";
    public static final String CHECKBOX_BACKFACES_EJECTION = "Отброс невидимых полигонов";
    public static final String CHECKBOX_NORMALS_POLY = "Нормали плоскостей";
    public static final String CHECKBOX_NORMALS_VERT = "Нормали вершин";
    public static final String CHECKBOX_ANIMATE = "Анимировать";
    
    private static final Color CROSSHAIR_COLOR_NORM = Color.WHITE;
    private static final Color CROSSHAIR_COLOR_ALLERT = Color.RED;
    
    private static final Point2D ZERO_POINT = new Point2D();
    private final List<Solid3D> focusedModels = new ArrayList<>();
    private Solid3D allModel;
    private Solid3D selectedModel;
    private boolean isMousePressed;
    private boolean isGameViewModeEnabled;
    private final CameraManager cameraManager = new CameraManager(WINDOW_WIDTH, WINDOW_HEIGHT);
	
    public static void main(String[] args) {
	new Main(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /////////////////////////////////////////////////////////
    public Main(int width, int height) {
	super(width, height);
	requestFocus();
	setFocusable(true);
	setResizable(true);
	setTitle(TITLE);
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
 	addRadio(GROUP_TITLE_OBJECTS, RADIO_ALL_MODELS, this);

	addRadio(GROUP_TITLE_OBJ_CHOISE, RADIO_BY_MOUSE_PRESSED, this);
	addRadio(GROUP_TITLE_OBJ_CHOISE, RADIO_BY_LIST_SELECTION, this);

	addRadio(GROUP_TITLE_OPERATIONS, RADIO_ROTATE, this);
	addRadio(GROUP_TITLE_OPERATIONS, RADIO_TRANSFER, this);
	addRadio(GROUP_TITLE_OPERATIONS, RADIO_SCALE, this);

	//addRadio(GROUP_TITLE_CAMERA, RADIO_CAMERA_EULER, this);
	//addRadio(GROUP_TITLE_CAMERA, RADIO_CAMERA_UVN, this);

	addRadio(GROUP_TITLE_RENDER, RADIO_PAINTER, this);

	addRadio(GROUP_TITLE_VIEW, RADIO_EDGES_FACES, this);
	addRadio(GROUP_TITLE_VIEW, RADIO_FACES, this);
	addRadio(GROUP_TITLE_VIEW, RADIO_EDGES, this);
        
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_CONST, this);
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_FLAT, this);
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_GOURAD, this);
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_FONG, this);
	// checkBox
	//addCheckBox(CHECKBOX_SHIFT_IF_INTERSECT, false, this);
	addCheckBox(CHECKBOX_BACKFACES_EJECTION, false, this);
	addCheckBox(CHECKBOX_NORMALS_POLY, false, this);
	addCheckBox(CHECKBOX_NORMALS_VERT, false, this);
	addCheckBox(CHECKBOX_ANIMATE, false, this);
    }

    /////////////////////////////////////////////////////////
    @Override
    protected final void load() {
	ModelsManager.load();
	//
	/*for (Solid3D model : ModelsManager.getModels()) {
	    if (!model.isSetAttribute(Solid3D.ATTR_FIXED)) {
		addRadio(GROUP_TITLE_OBJECTS, model.getName(), this);
	    }
	}*/
	//
        RenderManager.load();
    }

    @Override
    protected final void init() {
	isMousePressed = false;
        CursorManager.init(getToolkit(), CROSSHAIR_COLOR_NORM, WINDOW_WIDTH, WINDOW_HEIGHT);
	// radio button to select all models to move
	allModel = new Solid3D(RADIO_ALL_MODELS);
        selectedModel = allModel;
        
        /*// init fixed values in models
        for (Solid3D model : ModelsManager.getModels()) {
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
        boolean isAnimate = isSelectedCheckBox(CHECKBOX_ANIMATE);
        boolean isDefineBackfaces = isSelectedCheckBox(CHECKBOX_BACKFACES_EJECTION);
        
        // 1 - work with isolated objects
        ModelsManager.updateAndAnimate(cameraManager.getCam(), isAnimate, isDefineBackfaces);
	// 2 - work with render array (visible polygons)
	RenderManager.buildRenderArray(ModelsManager.getModels());
        RenderManager.update(cameraManager.getCam(), 
                getSelectedRadioText(GROUP_TITLE_SHADE),
                isSelectedCheckBox(CHECKBOX_NORMALS_POLY),
                isSelectedCheckBox(CHECKBOX_NORMALS_VERT));
	// 3 - 
	onCollision();
        //
        //onCrosshair();
    }

    /////////////////////////////////////////////////////////
    private void onCollision() {
	// if collision check is off
	if (!isSelectedCheckBox(CHECKBOX_SHIFT_IF_INTERSECT)
		|| isMousePressed) return;
	//InteractionManager.collision(ModelsManager.getModels());
    }
  
    /////////////////////////////////////////////////////////
    private void onOperation(List<Solid3D> models, double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale) {
	// if selected all models
	if (models.contains(allModel)) {
	    models.clear();
	    //models.addAll(Types.toList(ModelsManager.getModels()));
            for (Solid3D model : ModelsManager.getModels()) {
                if (model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
                models.add(model);
            }
	}
	// select operation
	switch (getSelectedRadioText(GROUP_TITLE_OPERATIONS)) {
	    case RADIO_ROTATE:
		ModelsManager.onRotate(models, angle, axis);
		break;
	    case RADIO_TRANSFER:
		ModelsManager.onTransfer(models, dx, dy, dz);
		break;
	    case RADIO_SCALE:
		ModelsManager.onScale(models, scale);
		break;
	}
    }

    private void onSolidListSelection(String selectedRadioText) {
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

    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
	switch(groupTitle) {
	    case GROUP_TITLE_OBJECTS:
		onSolidListSelection(radioText);
		break;
	    case GROUP_TITLE_CAMERA:
		cameraManager.onSwitch(radioText);
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
        
        int dx = curPoint.x - WINDOW_WIDTH/2;
        int dy = curPoint.y - WINDOW_HEIGHT/2;
        cameraManager.getCam().updateDirection(dx * CAMERA_ANGLE, Matrix.AXIS.Y);
        cameraManager.getCam().updateDirection(dy * CAMERA_ANGLE, Matrix.AXIS.X);
        //
        //moveMouseToCenter();
    }

    protected void onCrosshair() {
	for (Solid3D model : ModelsManager.getModels()) {
	    if (model.getState() != Solid3D.States.VISIBLE
		    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (model.isCameraPointInto(ZERO_POINT, cameraManager.getCam())) {
		CursorManager.setColor(CROSSHAIR_COLOR_ALLERT);
		return;
	    }
        }
        CursorManager.setColor(CROSSHAIR_COLOR_NORM);
    }
    
    /////////////////////////////////////////////////////////
    public void onCursorSwitch(/*Point curPoint*/) {
	if (/*curPoint == null || */ModelsManager.getModels() == null) return;
	// set cursor type
	if (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE).equals(RADIO_BY_LIST_SELECTION)) {
	    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	    return;
	}
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	//Point2D p = getGraphicSystem().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
	for (Solid3D model : ModelsManager.getModels()) {
	    if (model.getState() != Solid3D.States.VISIBLE
		    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (model.isCameraPointInto(ZERO_POINT, cameraManager.getCam())) {
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
	switch (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE)) {
	    //
	    case RADIO_BY_MOUSE_PRESSED:
		//Point2D p = getDrawManager().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
		for (Solid3D model : ModelsManager.getModels()) {
		    if (model.getState() != Solid3D.States.VISIBLE
			    || model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
		    boolean isPointInto = model.isCameraPointInto(ZERO_POINT, cameraManager.getCam());
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
    public void onMouseReleased() {
	isMousePressed = false;
	focusedModels.clear();
        if (isGameViewModeEnabled)
            moveMouseToCenter();
    }
    
    public void moveMouseToCenter() {
        try {
            Robot r = new Robot();
            Point p = getWindowCenterOnScreen();
            r.mouseMove(p.x, p.y);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
    
    public Point getWindowCenterOnScreen() {
        Point p = getLocationOnScreen();
        int cx = (int) (p.x + WINDOW_WIDTH / 2);
        int cy = (int) (p.y + WINDOW_HEIGHT / 2);
        return new Point(cx, cy);
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
	cameraManager.onOperation(angle, axis, 
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
            setCursor(CursorManager.getEmptyCursor());
            moveMouseToCenter();
        }
        setVisibleOptionsPanel(isGameViewModeEnabled);
        isGameViewModeEnabled = !isGameViewModeEnabled;
    }
    
    /////////////////////////////////////////////////////////
    @Override
    protected void paint(DrawManager g) {
        g.drawScene(RenderManager.getRenderArray(), 
                getSelectedRadioText(GROUP_TITLE_VIEW),
                getSelectedRadioText(GROUP_TITLE_SHADE),
                isSelectedCheckBox(CHECKBOX_NORMALS_POLY),
                isSelectedCheckBox(CHECKBOX_NORMALS_VERT),
                CursorManager.getCrosshair());
    }
}