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

    public static final int FPS = 60;
    public static final int MSEC_TICK = 1000/FPS;
    public static final int WINDOW_WIDTH = 1100;
    public static final int WINDOW_HEIGHT = 600;
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
    private static final Color CROSSHAIR_COLOR_NORM = Color.WHITE;
    private static final Color CROSSHAIR_COLOR_ALLERT = Color.RED;
    
    private static final Point2D ZERO_POINT = new Point2D();
    private final List<Solid3D> focusedModels = new ArrayList<>();
    private Solid3D selectedModel;
    private Solid3D allModel;
    private boolean isMousePressed;
    private boolean isGameViewModeEnabled;
    private final ModelsManager modelsManager = new ModelsManager();
    private final RenderManager renderManager = new RenderManager();
    private final CameraManager cameraManager = new CameraManager(WINDOW_WIDTH, WINDOW_HEIGHT);
    private final CursorManager cursorManager = new CursorManager();
	
    public static void main(String[] args) {
	new Main(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /////////////////////////////////////////////////////////
    public Main(int width, int height) {
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
        renderManager.load();
    }

    @Override
    protected final void init() {
	isMousePressed = false;
        cursorManager.init(getToolkit(), CROSSHAIR_COLOR_NORM, WINDOW_WIDTH, WINDOW_HEIGHT);
	// radio button to select all models to move
	allModel = new Solid3D(RADIO_ALL_MODELS_TEXT);
        
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
        modelsManager.updateAndAnimate(cameraManager.getCam(), isNeedDefineBackfaces);
	
	// 2 - work with render array (visible polygons)
	renderManager.buildRenderArray(modelsManager.getModels());
        renderManager.update(cameraManager.getCam(), 
                getSelectedRadioText(GROUP_TITLE_SHADE_TEXT));
        
	// 3 - 
	onCollision();
        
        //
        //onCrosshair();
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
		ModelsManager.onRotate(models, angle, axis);
		break;
	    case RADIO_TRANSFER_TEXT:
		ModelsManager.onTransfer(models, dx, dy, dz);
		break;
	    case RADIO_SCALE_TEXT:
		ModelsManager.onScale(models, scale);
		break;
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
        //cameraManager.getCamera().updateDirection(dy * CAMERA_ANGLE, Matrix.AXIS.X);
        //
        //moveMouseToCenter();
    }

    protected void onCrosshair() {
	for (Solid3D model : modelsManager.getModels()) {
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
	switch (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE_TEXT)) {
	    //
	    case RADIO_BY_MOUSE_PRESSED_TEXT:
		//Point2D p = getDrawManager().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
		for (Solid3D model : modelsManager.getModels()) {
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
                CursorManager.getCrosshair());
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
