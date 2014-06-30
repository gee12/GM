package com.bondar.tasks;

import com.bondar.geom.Solid3D;
import com.bondar.geom.Point2D;
import com.bondar.geom.Vector3D;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import com.bondar.panels.Application;
import com.bondar.gm.*;
import com.bondar.panels.OptionsPanelListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class GM extends Application implements OptionsPanelListener {

    public static final int FPS = 30;
    public static final int MSEC_TICK = 1000/FPS;
    private static final double SHIFT_STEP = 0.008;
    private static final double SHIFT_BY_Z_STEP = 0.1;
    private static final double ANGLE_UP = Math.toRadians(6);
    private static final double ANGLE_DOWN = Math.toRadians(-6);
    private static final double SCALE_UP = 0.05;
    private static final double SCALE_DOWN = -SCALE_UP;
    private static final double CAMERA_SHIFT_STEP = 0.5;
    private static final double CAMERA_ANGLE_UP = ANGLE_UP/3;
    private static final double CAMERA_ANGLE_DOWN = ANGLE_DOWN/3;
    
    private static final int SCREEN_WIDTH = 1100;
    private static final int SCREEN_HEIGHT = 600;
    private static final double DX = 5;
    private static final double DY = 3.5;
    private static final String TITLE_TEXT = "GM";
    private static final String MODELS_DIR = "models/";
    private static final String AXIS_TEXT = "Оси";
    private static final String CUBE_TEXT = "Куб";
    private static final String GROUP_TITLE_OBJECTS_TEXT = "Объекты:";
    private static final String RADIO_ALL_MODELS_TEXT = "Все";
    private static final String GROUP_TITLE_OBJ_CHOISE_TEXT = "ВЫБОР ОБЪЕКТА:";
    private static final String RADIO_BY_MOUSE_PRESSED_TEXT = "по захвату мышей";
    private static final String RADIO_BY_LIST_SELECTION_TEXT = "по выбору из списка";
    private static final String GROUP_TITLE_OPERATIONS_TEXT = "ОПЕРАЦИИ:";
    private static final String RADIO_ROTATE_TEXT = "Поворот";
    private static final String RADIO_TRANSFER_TEXT = "Перемещение";
    private static final String RADIO_SCALE_TEXT = "Масштабирование";
    private static final String GROUP_TITLE_PROJECTION_TEXT = "ПРОЕКЦИЯ:";
    private static final String RADIO_ORTOGON_TEXT = "Ортогональная";
    private static final String RADIO_CENTER_TEXT = "Центральная";
    private static final String GROUP_TITLE_CAMERA_TEXT = "КАМЕРА:";
    private static final String RADIO_CAMERA_EULER_TEXT = "Эйлера";
    private static final String RADIO_CAMERA_UVN_TEXT = "UVN";
    private static final String GROUP_TITLE_CLIPPING_TEXT = "ОТСЕЧЕНИЕ:";
    private static final String RADIO_PAINTER_TEXT = "Алгритм художника";
    private static final String RADIO_BACKFACES_EJECTION_TEXT = "Отброс невидимых полигонов";
    private static final String RADIO_Z_BUFFER_TEXT = "Z-Буффер";
    private static final String GROUP_TITLE_VIEW_TEXT = "ВНЕШНИЙ ВИД:";
    private static final String RADIO_EDGES_TEXT = "Ребра";
    private static final String RADIO_FACES_TEXT = "Грани";
    private static final String RADIO_EDGES_FACES_TEXT = "Ребра и грани";
    
    private static final String CHECKBOX_SHIFT_IF_INTERSECT_TEXT = "Сдвигать при пересечении";
    
    private List<Solid3D> solids = new ArrayList<>();;
    private List<Solid3D> focusedSolids = new ArrayList<>();;
    private Solid3D allSolid;
    private Camera camera;
    private CameraEuler cameraEuler;
    private CameraUVN cameraUVN;
    private Solid3D selectedSolid;
    private boolean isMousePressed;
	
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
	//
	setClip(false);
	setScale(false);
	addListeners();
	addControls();
	//
	start(MSEC_TICK);  
    }
    
    private void addListeners() {
	//
	addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent evt) {
		onKeyPressed(evt);
	    }
	});
	//
	addMouseMotionListener(new MouseMotionListener() {
	    // while be called mouseDragged() method, 
	    // mouseMoved() will not be called
	    private Point p = new Point();
	    @Override
	    public void mouseDragged(MouseEvent me) {
		Point curPoint = me.getPoint();
		onMouseDragged(curPoint, p);
		p = curPoint;
	    }
	    @Override
	    public void mouseMoved(MouseEvent me) {
		p = me.getPoint();
		onMouseMoved(p);
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

	addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_CENTER_TEXT, this);
	addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_ORTOGON_TEXT, this);

	addRadio(GROUP_TITLE_CAMERA_TEXT, RADIO_CAMERA_EULER_TEXT, this);
	addRadio(GROUP_TITLE_CAMERA_TEXT, RADIO_CAMERA_UVN_TEXT, this);

	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_PAINTER_TEXT, this);
	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_BACKFACES_EJECTION_TEXT, this);
	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_Z_BUFFER_TEXT, this);

	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_FACES_TEXT, this);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_FACES_TEXT, this);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_TEXT, this);
	// checkBox
	addCheckBox(CHECKBOX_SHIFT_IF_INTERSECT_TEXT, false, this);
    }

    /////////////////////////////////////////////////////////
    @Override
    protected final void load() {
	// load models from .GMX files
	try {
	    solids = GMXFile.readGMXDir(MODELS_DIR);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	Random rand = new Random();
	for (Solid3D solid : solids) {
	    // axis solid need save to individual variable 
	    // and remove from list
	    if (solid.isSetAttribute(Solid3D.ATTR_FIXED)) {
		continue;
	    }
	    addRadio(GROUP_TITLE_OBJECTS_TEXT, solid.getName(), this);
	}
	// clone models
	/*List<Solid3D> cubes = new ArrayList<>();
	for (Solid3D solid : solids) {
	    if (solid.getName().equals(CUBE_TEXT)) {
		for (int i = 0; i < 1000; i++) {
		    Solid3D newCube = new Solid3D(solid);
		    newCube.updateTransfers(rand.nextInt(500)-250, 0, rand.nextInt(500)-250);
		    cubes.add(newCube);
		}
		break;
	    }
	}
	solids.addAll(cubes);*/
    }

    @Override
    protected final void init() {
	isMousePressed = false;
	// radio button to select all models to move
	allSolid = new Solid3D(RADIO_ALL_MODELS_TEXT, null);	
	// camera
	Point3D pos = new Point3D(0,0,-10);
	Vector3D dir = new Vector3D(0,0,0);
	Point3D target = new Point3D(0,0,10);
	double near = 4, far = 1000, dist = 10, fov = 90;
	Dimension vp = new Dimension((int)GraphicSystem.X_MAX, (int)GraphicSystem.Y_MAX);
	cameraEuler = new CameraEuler(0, pos, dir, near, far, dist, fov, vp, CameraEuler.CAM_ROT_SEQ_ZYX);
	cameraUVN = new CameraUVN(0, pos, dir, near, far, dist, fov, vp, target, CameraUVN.UVN_MODE_SIMPLE);
	camera = cameraEuler;
	//
 	onProjection(getSelectedRadioText(GROUP_TITLE_PROJECTION_TEXT));
   }
    
    /////////////////////////////////////////////////////////
    @Override
    protected void update() {
	// if there are not collisions OR objects were not moved ->
	// objects are not changed ->
	// then don't need to update the solid's vertexes
	// if (!isCollisions && !isObjectsMoved) return;
	
	for (Solid3D solid : solids) {
	    animateSolid(solid);
	    updateSolid(solid);
	}
	//
	onCollision();
    }
    
    private void animateSolid(Solid3D solid) {
	if (solid == null || solid.isSetAttribute(Solid3D.ATTR_FIXED)) return;
	solid.updateAngle(ANGLE_UP, Matrix.AXIS.Y);
    }
   
    private void updateSolid(Solid3D solid) {
	if (solid == null) return;
	// transferFull local vertexes to world
	Point3D[] verts = Transfer.transToWorld(solid);
	// culling solid if need
	if (!solid.isSetAttribute(Solid3D.ATTR_NO_CULL))
	    solid.setIsNeedCulling(camera);
	if (solid.getState() != Solid3D.States.VISIBLE)
	    return;
	// define backfaces triangles
	if (getSelectedRadioText(GROUP_TITLE_CLIPPING_TEXT).equals(RADIO_BACKFACES_EJECTION_TEXT))
	{
	    solid.reinitPoliesVertexes(verts);
	    solid.defineBackfaces(camera);
	}
	// transferFull world vertexes to camera
	verts = Transfer.transToCamera(verts, camera);
	if (solid.isNeedPerspective())
	    verts = Transfer.transToPerspective(verts, camera);
	if (verts == null) return;
	solid.reinitPoliesVertexes(verts);
	solid.setTransVertexes(verts);
	// 
	solid.setBounds(verts);
    }
     
    private void onCollision() {
	// if collision check is off
	if (!isSelectedCheckBox(CHECKBOX_SHIFT_IF_INTERSECT_TEXT)
		|| isMousePressed) return;
	for (Solid3D solid1 : solids) {
	    for (Solid3D solid2 : solids) {
		if (solid2.equals(solid1)) continue;
		// if solids are intersect, then shift them to opposite directions
		if (solid2.isIntersect(solid1.getBounds())) {
		    boolean isFixed1 = solid1.isSetAttribute(Solid3D.ATTR_FIXED);
		    boolean isFixed2 = solid2.isSetAttribute(Solid3D.ATTR_FIXED);
		    if (isFixed1 && isFixed2) continue;
		    //
		    double dx1=0,dy1=0,dz1=0,
			    dx2=0,dy2=0,dz2=0;
		    Point3D cp1 = Solid3D.getCenter(solid1.getBounds());
		    Point3D cp2 = Solid3D.getCenter(solid2.getBounds());
		    // shift to X axis
		    if (cp2.getX() > cp1.getX()) {
			dx2 = SHIFT_STEP;
		    } else {
			dx1 = -SHIFT_STEP;
		    }
		    // shift to Y axis
		    if (cp2.getY() > cp1.getY()) {
			dy2 = SHIFT_STEP;
		    } else {
			dy1 = -SHIFT_STEP;
		    }
		    // shift to Z axis
		    if (cp2.getZ() > cp1.getZ()) {
			dz2 = SHIFT_STEP;
		    } else {
			dz1 = -SHIFT_STEP;
		    }
		    //
		    if (!isFixed1) solid1.updateTransfers(dx1,dy1,dz1);
		    if (!isFixed2) solid2.updateTransfers(dx2,dy2,dz2);
		}
	    }
	}
    }
  
    /////////////////////////////////////////////////////////
    private void onOperation(List<Solid3D> solids, double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale) {
	// if selected all solids
	if (solids.contains(allSolid)) {
	    solids.clear();
	    solids.addAll(this.solids);
	}
	// select operation
	switch (getSelectedRadioText(GROUP_TITLE_OPERATIONS_TEXT)) {
	    case RADIO_ROTATE_TEXT:
		onRotate(solids, angle, axis);
		break;
	    case RADIO_TRANSFER_TEXT:
		onTransfer(solids, dx, dy, dz);
		break;
	    case RADIO_SCALE_TEXT:
		onScale(solids, scale);
		break;
	}
    }
    
    /////////////////////////////////////////////////////////
    private void onCameraOperation(double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz) {
	camera.updateDirection(angle, axis);
	camera.updatePosition(dx, dy, dz);
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

    private void onRotate(List<Solid3D> solids, double angle, Matrix.AXIS axis) {
	for (Solid3D solid : solids) {
	    if (solid.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    solid.updateAngle(angle, axis);
	}
    }

    private void onTransfer(List<Solid3D> solids, double dx, double dy, double dz) {
	for (Solid3D solid : solids) {
	    if (solid.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    solid.updateTransfers(dx, dy, dz);
	}
    }

    private void onScale(List<Solid3D> solids, double scale) {
	for (Solid3D solid : solids) {
	    if (solid.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    solid.updateScale(scale);
	}
    }
    
    private void onProjection(String selectedRadioText) {
	boolean isPerspective = false;
	switch (selectedRadioText) {
	    case RADIO_ORTOGON_TEXT:
		isPerspective = false;
		break;
	    case RADIO_CENTER_TEXT:
		isPerspective = true;
		break;
	}
	for (Solid3D solid : solids) {
	    solid.setPerspective(isPerspective);
	}
    }
    
    private void onSolidListSelection(String selectedRadioText) {
	for (Solid3D solid : solids) {
	    if (solid.getName().equals(selectedRadioText)) {
		selectedSolid = solid;
		return;
	    }
	}
	// if suddenly selected solid don't finded
	selectedSolid = allSolid;
    }

    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
	switch(groupTitle) {
	    case GROUP_TITLE_PROJECTION_TEXT:
		onProjection(radioText);
		break;
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
	onOperation(focusedSolids, angle, axis, dx, dy, dz, scale);
    }
    
    /////////////////////////////////////////////////////////
    public void onMouseMoved(Point curPoint) {
	if (curPoint == null) return;
	// set cursor type
	if (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE_TEXT).equals(RADIO_BY_LIST_SELECTION_TEXT)) {
	    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	    return;
	}
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	Point2D p = getGraphicSystem().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
	for (Solid3D solid : solids) {
	    if (solid.getState() != Solid3D.States.VISIBLE
		    || solid.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    // find object borders & check the cursor hit into borders
	    if (solid.getBounds().isPointInto(p)) {
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
	onOperation(focusedSolids, angle, axis, 0, 0, dz, scale);
    }

    /////////////////////////////////////////////////////////
    public void onMousePressed(Point curPoint) {
	if (curPoint == null) return;
	isMousePressed = true;
	switch (getSelectedRadioText(GROUP_TITLE_OBJ_CHOISE_TEXT)) {
	    //
	    case RADIO_BY_MOUSE_PRESSED_TEXT:
		Point2D p = getGraphicSystem().convPToWorld(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
		for (Solid3D solid : solids) {
		    boolean isPointInto = solid.getBounds().isPointInto(p);
		    if (solid.getState() != Solid3D.States.VISIBLE
			    /*|| solid.getName().equals(AXIS_TEXT)*/
			    /*|| solid.isSetAttribute(Solid3D.ATTR_FIXED)*/) continue;
		    // find object borders & check the cursor hit into borders
		    if (isPointInto) {
			focusedSolids.add(solid);
		    }
		}
		// if the mouse is pressed in an empty area,
		// then making operations with all solids
		if (focusedSolids.isEmpty()) {
		    focusedSolids.add(allSolid);
		}
		break;
	    //
	    case RADIO_BY_LIST_SELECTION_TEXT:
		focusedSolids.add(selectedSolid);
		break;
	}
    }

    /////////////////////////////////////////////////////////
    public void onMouseReleased() {
	isMousePressed = false;
	focusedSolids.clear();
    }
    
    /////////////////////////////////////////////////////////
    public void onKeyPressed(KeyEvent evt) {
	double dx = 0, dy = 0, dz = 0, angle = 0;
	Matrix.AXIS axis = Matrix.AXIS.X;
	int keyCode = evt.getKeyCode();
	switch(keyCode) {
	    case KeyEvent.VK_RIGHT:
		dx = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_LEFT:
		dx = -CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_SPACE:
		dy = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_SHIFT:
		dy = -CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_UP:
		dz = CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_DOWN:
		dz = -CAMERA_SHIFT_STEP;
		break;
	    case KeyEvent.VK_NUMPAD8:
		angle = CAMERA_ANGLE_DOWN;
		axis = Matrix.AXIS.X;
		break;
	    case KeyEvent.VK_NUMPAD2:
		angle = CAMERA_ANGLE_UP;
		axis = Matrix.AXIS.X;
		break;
	    case KeyEvent.VK_NUMPAD4:
		angle = CAMERA_ANGLE_DOWN;
		axis = Matrix.AXIS.Y;
		break;
	    case KeyEvent.VK_NUMPAD6:
		angle = CAMERA_ANGLE_UP;
		axis = Matrix.AXIS.Y;
		break;
	}
	onCameraOperation(angle, axis, dx, dy, dz);
    }
    
    /////////////////////////////////////////////////////////
    @Override
    protected void paint(GraphicSystem g) {
	g.clear();
	g.reset();
	// shift the coordinates system to the center
	g.translate(DX, DY);

	//List<Triangle3D> trias = new ArrayList<>();
	for (Solid3D solid : solids) {
	    if (solid.getState() != Solid3D.States.VISIBLE) continue;
	    drawSolid(g, solid);
	    //trias.addAll(Types.toList(solid.getPolygons()));
	}
	//g.painterAlgorithm(Types.toArray3(trias, Triangle3D.class));
   }
    /////////////////////////////////////////////////////////
    private void drawSolid(GraphicSystem g, Solid3D solid) {
	if (solid == null) return;
	Polygon3D[] polies = solid.getPolygons();
	
	switch (getSelectedRadioText(GROUP_TITLE_VIEW_TEXT)) {
	    case RADIO_FACES_TEXT:
		fillSolid(g, polies);
		break;
	    case RADIO_EDGES_TEXT:
		drawEdges(g, polies);
		break;
	    case RADIO_EDGES_FACES_TEXT:
		fillSolid(g, polies);
		drawEdges(g, polies);
		break;
	}
    }

    /////////////////////////////////////////////////////////
    private void drawEdges(GraphicSystem g, Polygon3D[] polies) {
	if (g == null || polies == null) return;
	for (Polygon3D poly : polies) {
	    if (poly.getState() == Polygon3D.States.VISIBLE) {
		g.drawPolygonBorder(poly.getVertexes(), poly.getBorderColor());
	    }
	}
    }

    /////////////////////////////////////////////////////////
    private void fillSolid(GraphicSystem g, Polygon3D[] polies) {
	if (g == null || polies == null)  return;
	switch (getSelectedRadioText(GROUP_TITLE_CLIPPING_TEXT)) {
	    case RADIO_PAINTER_TEXT:
		g.painterAlgorithm(polies);
		break;
	    case RADIO_BACKFACES_EJECTION_TEXT:
		for (Polygon3D poly : polies) {
		    if (poly.getState() == Polygon3D.States.VISIBLE) {
			g.setColor(poly.getFillColor());
			g.fillPolygon(poly.getVertexes());
		    }
		}
		break;
	    case RADIO_Z_BUFFER_TEXT:
		g.zBufferAlgorithm(polies);
		break;
	}
    }
}
