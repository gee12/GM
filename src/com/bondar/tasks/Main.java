package com.bondar.tasks;

import com.bondar.geom.Solid3D;
import com.bondar.geom.Point2D;
import com.bondar.panels.Application;
import com.bondar.gm.*;
import com.bondar.panels.OptionsPanelListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bondar
 */
public class Main extends Application implements OptionsPanelListener {

    public static final int FPS = 30;
    public static final int MSEC_TICK = 1000/FPS;
    public static final int SRC_WIDTH = 1100;
    public static final int SRC_HEIGHT = 600;
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
    public static final String GROUP_TITLE_DEPTH = "ГЛУБИНА:";
    public static final String RADIO_PAINTER = "Алгритм художника";
    public static final String RADIO_Z_BUFFER = "Z буффер";
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
    public static final String CHECKBOX_TEXTURE = "Отобразить текстуры";
    
    public static final Color FONT_COLOR = Color.LIGHT_GRAY;
    public static final Font FONT = new Font("Tahoma", Font.PLAIN, 15);

    public static final Point2D ZERO_POINT = new Point2D();
    
    private final List<Solid3D> focusedModels = new ArrayList<>();
    public static boolean isGameViewModeEnabled;
	
    public static void main(String[] args) {
	new Main(SRC_WIDTH, SRC_HEIGHT);
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
    
    @Override
    protected void setDrawPanelDimension(int width, int height) {
        super.setDrawPanelDimension(width, height);
//        DrawManager.setDimension(getWidth(), getHeight());
        CameraManager.setViewPort(getWidth(), getHeight());
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
                MouseManager.onMouseDragged(curPoint, oldPoint, focusedModels, 
                        getSelectedRadioText(GROUP_TITLE_OPERATIONS));
                oldPoint = curPoint;
	    }
	    @Override
	    public void mouseMoved(MouseEvent me) {
                Point curPoint = me.getPoint();
                
                // return mouse cursor to center of screen (if need)
                if (isGameViewModeEnabled) {
                    MouseManager.onTurnSceneWithMouse(curPoint, getWindowLocalCenter());
                    MouseManager.moveMouseToWindowCenter(getWindowCenterOnScreen());
                }
                else setCursor(Cursor.getPredefinedCursor(CursorManager.onCursorSwitch(getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE),
                        ModelsManager.getModels())));
                
		oldPoint = curPoint;
	    }
	});
	//
	addMouseListener(new MouseListener() {
	    @Override
	    public void mousePressed(MouseEvent me) {
		MouseManager.onMousePressed(getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE), focusedModels);
	    }
	    @Override
	    public void mouseReleased(MouseEvent me) {
		MouseManager.onMouseReleased(focusedModels, getWindowCenterOnScreen());
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
		MouseManager.onMouseWheelMoved(focusedModels, mwe.getWheelRotation(),
                        getSelectedRadioText(GROUP_TITLE_OPERATIONS));
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

	addRadio(GROUP_TITLE_DEPTH, RADIO_Z_BUFFER, this);
	addRadio(GROUP_TITLE_DEPTH, RADIO_PAINTER, this);

	addRadio(GROUP_TITLE_VIEW, RADIO_FACES, this);
	addRadio(GROUP_TITLE_VIEW, RADIO_EDGES, this);
	addRadio(GROUP_TITLE_VIEW, RADIO_EDGES_FACES, this);
        
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_FLAT, this);
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_GOURAD, this);
        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_CONST, this);
//        addRadio(GROUP_TITLE_SHADE, RADIO_SHADE_FONG, this);
	// checkBox
	//addCheckBox(CHECKBOX_SHIFT_IF_INTERSECT, false, this);
	addCheckBox(CHECKBOX_TEXTURE, true, this);
	addCheckBox(CHECKBOX_BACKFACES_EJECTION, true, this);
	addCheckBox(CHECKBOX_NORMALS_POLY, false, this);
	addCheckBox(CHECKBOX_NORMALS_VERT, false, this);
	addCheckBox(CHECKBOX_ANIMATE, true, this);
    }

    /////////////////////////////////////////////////////////
    @Override
    protected final void load() {
	ModelsManager.load();
	//
	for (Solid3D model : ModelsManager.getModels()) {
	    if (!model.isSetAttribute(Solid3D.ATTR_FIXED)) {
		addRadio(GROUP_TITLE_OBJECTS, model.getName(), this);
	    }
	}
        ModelsManager.clone("Cube", 1000);
        RenderManager.load();
        TextureManager.load();
    }

    @Override
    protected final void init() {
	MouseManager.init();
        CursorManager.init(getToolkit(), getWindowLocalCenter());
        CameraManager.init(drawPanelWidth, drawPanelHeight);
    }

    /////////////////////////////////////////////////////////
    @Override
    protected void update() {
        boolean isAnimate = isSelectedCheckBox(CHECKBOX_ANIMATE);
        boolean isDefineBackfaces = isSelectedCheckBox(CHECKBOX_BACKFACES_EJECTION);
        
        // 1 - work with isolated objects
        ModelsManager.updateAndAnimate(CameraManager.getCam(), isAnimate, isDefineBackfaces);
	// 2 - work with render array (visible polygons)
	RenderManager.buildRenderArray(ModelsManager.getModels());
        RenderManager.update(CameraManager.getCam(),
                getSelectedRadioText(GROUP_TITLE_DEPTH),
                getSelectedRadioText(GROUP_TITLE_SHADE),
                isSelectedCheckBox(CHECKBOX_NORMALS_POLY),
                isSelectedCheckBox(CHECKBOX_NORMALS_VERT));
	// 3 - 
//	onCollision();
        //
        CursorManager.onHit(ModelsManager.getModels());
    }

    /////////////////////////////////////////////////////////
    private void onCollision() {
	// if collision check is off
	if (!isSelectedCheckBox(CHECKBOX_SHIFT_IF_INTERSECT)
		|| MouseManager.isMousePressed) return;
	InteractionManager.collision(ModelsManager.getModels());
    }


    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
	switch(groupTitle) {
	    case GROUP_TITLE_OBJECTS:
		MouseManager.onSolidListSelection(radioText);
		break;
	    case GROUP_TITLE_CAMERA:
		CameraManager.onSwitch(radioText);
		break;
	}
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
    }

    @Override
    public void onSliderChanged(String sliderName, int value) {
    }
    
    public Point getWindowCenterOnScreen() {
        Point p = getLocationOnScreen();
        int cx = (int) (p.x + getWidth() / 2);
        int cy = (int) (p.y + getHeight() / 2);
        return new Point(cx, cy);
    }
    
    public Point getWindowLocalCenter() {
        return new Point(getWidth() / 2, getHeight() / 2);
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
	CameraManager.onOperation(angle, axis, 
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
            MouseManager.moveMouseToWindowCenter(getWindowCenterOnScreen());
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
                getSelectedRadioText(GROUP_TITLE_DEPTH),
                isSelectedCheckBox(CHECKBOX_TEXTURE),
                isSelectedCheckBox(CHECKBOX_NORMALS_POLY),
                isSelectedCheckBox(CHECKBOX_NORMALS_VERT),
                CursorManager.getCrosshair());
        g.drawMultilineText(infoText(), FONT, FONT_COLOR, 10, 20);
    }
    
    private String infoText() {
        StringBuilder res = new StringBuilder();
        try {
            res.append("Камера: ");
            res.append(CameraManager.getCam().getPosition().toString());
            //
            res.append("\nМоделей: ");
            int modelsNum = ModelsManager.getSize();
            res.append(modelsNum);
            //
            res.append("  Видимых: ");
            res.append(ModelsManager.getVisibleNum());
            //
            res.append("\nПолигонов: ");
            res.append(RenderManager.getSize());
            //
            res.append("\nИсточников света (активных): ");
            res.append(LightManager.getActiveLightsNum());
        } catch(Exception ex) {}
        return res.toString();
    }
    

}
