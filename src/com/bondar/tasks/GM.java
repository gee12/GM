package com.bondar.tasks;

import com.bondar.panels.Application;
import com.bondar.gm.*;
import com.bondar.panels.RadioGroupListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;
import javax.swing.Timer;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class GM extends Application implements RadioGroupListener {

    private static final int MSEC_TICK = 30;
    private static final double SHIFT_STEP = 0.008;
    private static final double ANGLE_UP = Math.toRadians(6);
    private static final double ANGLE_DOWN = Math.toRadians(-6);
    private static final double SCALE_UP = 0.05;
    private static final double SCALE_DOWN = -SCALE_UP;
    private static final double PERSPECT_VALUE = 0.1;
    private static final int SCREEN_WIDTH = 1100;
    private static final int SCREEN_HEIGHT = 600;
    private static final double DX = 5;
    private static final double DY = 3.5;
    private static final String TITLE_TEXT = "GM";
    private static final String MODELS_DIR = "models/";
    private static final String AXIS_TEXT = "Оси";
    private static final String GROUP_TITLE_OBJECTS_TEXT = "Объекты:";
    private static final String RADIO_CAMERA_TEXT = "Камера";
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
    private static final String GROUP_TITLE_CLIPPING_TEXT = "ОТСЕЧЕНИЕ:";
    private static final String RADIO_PAINTER_TEXT = "Алгритм художника";
    private static final String RADIO_Z_BUFFER_TEXT = "Z-Буффер";
    private static final String GROUP_TITLE_INTERSECT_TEXT = "ПЕРЕСЕЧЕНИЕ ОБЪЕКТОВ:";
    private static final String RADIO_INTERSECT_ON_TEXT = "Допускается";
    private static final String RADIO_INTERSECT_OFF_TEXT = "Не допускается";
    private static final String GROUP_TITLE_VIEW_TEXT = "ВНЕШНИЙ ВИД:";
    private static final String RADIO_EDGES_TEXT = "Ребра";
    private static final String RADIO_FACES_TEXT = "Грани";
    private static final String RADIO_EDGES_FACES_TEXT = "Ребра и грани";
    
    private List<Solid3D> solids = new ArrayList<>();
    private List<Solid3D> focusedSolids = new ArrayList<>();
    private final Solid3D axis;
    private final Solid3D camera;
    private final GraphicSystem g;
    private HashMap<String, String> radiosMap = new HashMap<>();
    private boolean isPerspective = false;
    private Solid3D selectedSolid;
    private boolean isMousePressed = false;
    
    public static void main(String[] args) {
	new GM(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /////////////////////////////////////////////////////////
    public GM(int width, int height) {
	super(width, height);
	setResizable(true);
	setTitle(TITLE_TEXT);
	setLocation(50, 50);
	setClip(false);
	setScale(false);
	// обработчики
	addListeners();
	// переключателей
	createRadioButtons();
	setRadioGroupListeners(this);
	// оси
	axis = new Solid3D(AXIS_TEXT, new Point3DOdn[]{
	    new Point3DOdn(-5, 0, 0),
	    new Point3DOdn(5, 0, 0),
	    new Point3DOdn(0, 3.5, 0),
	    new Point3DOdn(0, -3.5, 0)});
	// точка наблюдения
	camera = new Solid3D(RADIO_CAMERA_TEXT, new Point3DOdn[]{
	    new Point3DOdn(2, 2, 2)});
	selectedSolid = camera;
	// объекты
	loadSolids();
	//
	g = getDrawablePanel().getGraphicSystem();
	// таймер для регулярной перерисовки
	ActionListener taskPerformer = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		update();
		paint(g);
	    }
	};
	new Timer(MSEC_TICK, taskPerformer).start();   
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
    
    private void createRadioButtons() {
	addRadio(GROUP_TITLE_OBJECTS_TEXT, RADIO_CAMERA_TEXT);
	
	addRadio(GROUP_TITLE_OBJ_CHOISE_TEXT, RADIO_BY_MOUSE_PRESSED_TEXT);
	addRadio(GROUP_TITLE_OBJ_CHOISE_TEXT, RADIO_BY_LIST_SELECTION_TEXT);

	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_ROTATE_TEXT);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_TRANSFER_TEXT);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_SCALE_TEXT);

	addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_ORTOGON_TEXT);
	addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_CENTER_TEXT);

	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_PAINTER_TEXT);
	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_Z_BUFFER_TEXT);

	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_FACES_TEXT);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_FACES_TEXT);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_TEXT);

	addRadio(GROUP_TITLE_INTERSECT_TEXT, RADIO_INTERSECT_ON_TEXT);
	addRadio(GROUP_TITLE_INTERSECT_TEXT, RADIO_INTERSECT_OFF_TEXT);
    }
    
    @Override
    public void addRadio(final String titleText, final String text) {
	super.addRadio(titleText, text);
	radiosMap.put(titleText, 
		optionsPanel.getGroupPanel(titleText).getSelectedRadioText());
    }

    /////////////////////////////////////////////////////////
    private void loadSolids() {
	try {
	    solids = GMXFile.readGMXDir(MODELS_DIR);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	Random rand = new Random();
	for (Solid3D solid : solids) {
	    solid.setEdgesColor(new Color(
			rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255));
	    addRadio(GROUP_TITLE_OBJECTS_TEXT, solid.getName());
	}
    }

    /////////////////////////////////////////////////////////
    private void update() {
	// if there are not collisions OR objects were not moved ->
	// objects are not changed ->
	// then don't need to update the solid's vertexes
	// if (!isCollisions && !isObjectsMoved) return;
	
	for (Solid3D solid : solids) {
	    updateSolid(solid);
	}
	onCollision();
    }
   
    private void updateSolid(Solid3D solid) {
	if (solid == null) return;
	// make transformations to solid vertexes
	// and triangles
	Point3D[] verts = solid.makeTransformations();
	solid.setWorldVertexes(verts);
	//solid.setTrianglesVertexes(verts);
	solid.resetTrianglesVertexes();
	//
	solid.setBounds(verts);
    }
     
    private boolean onCollision() {
	boolean res = false;
	// if collision check is off
	if (radiosMap.get(GROUP_TITLE_INTERSECT_TEXT).equals(RADIO_INTERSECT_ON_TEXT)
		|| isMousePressed) {
	    return res;
	}
	for (Solid3D solid1 : solids) {
	    //if (solid1.equals(camera)) continue;
	    for (Solid3D solid2 : solids) {
		if (/*solid2.equals(camera) ||*/ solid2.equals(solid1)) continue;
		// if solids are intersect, then shift them to opposite directions
		if (solid2.isIntersect(solid1.getBounds())) {
		    Point3D cp1 = solid1.getCenter(solid1.getBounds());
		    Point3D cp2 = solid2.getCenter(solid2.getBounds());
		    // shift to X axis
		    if (cp2.getX() > cp1.getX()) {
			solid2.updateTransfers(SHIFT_STEP,0,0);
		    } else {
			solid1.updateTransfers(-SHIFT_STEP,0,0);
		    }
		    // shift to Y axis
		    if (cp2.getY() > cp1.getY()) {
			solid2.updateTransfers(0,SHIFT_STEP,0);
		    } else {
			solid1.updateTransfers(0,-SHIFT_STEP,0);
		    }
		    // shift to Z axis ??
		    /*
		     */
		    res = true;
		}
	    }
	}
	return res;
    }
  
    /////////////////////////////////////////////////////////
    private void onOperation(List<Solid3D> solids, double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale) {
	// if the camera was moved, then need to transfer all solids
	// with inverse direction
	if (solids.contains(camera)) {
	    angle = (angle == ANGLE_UP) ? ANGLE_DOWN : ANGLE_UP;
	    dx = -dx; dy = -dy; dz = -dz;
	    solids.clear();
	    solids.addAll(this.solids);
	}
	// choose operation
	String selectedRadioText = radiosMap.get(GROUP_TITLE_OPERATIONS_TEXT);
	switch (selectedRadioText) {
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

    private void onRotate(List<Solid3D> solids, double angle, Matrix.AXIS axis) {
	for (Solid3D solid : solids) {
	    solid.updateAngle(angle, axis);
	}
    }

    private void onTransfer(List<Solid3D> solids, double dx, double dy, double dz) {
	for (Solid3D solid : solids) {
	    solid.updateTransfers(dx, dy, dz);
	}
    }

    private void onScale(List<Solid3D> solids, double scale) {
	for (Solid3D solid : solids) {
	    solid.updateScale(scale);
	}
    }
    
    private void onProjection(String selectedRadioText) {
	switch (selectedRadioText) {
	    case RADIO_ORTOGON_TEXT:
		isPerspective = false;
		break;
	    case RADIO_CENTER_TEXT:
		if (isPerspective) return;
		isPerspective = true;
		// get decart camera coordinates
		Point3D cameraP = camera.getVertex(0);
		// convert to spherical coordinates
		double[] sphereCoords = GraphicSystem.getSphericalCoordinates(
			cameraP.getX(), cameraP.getY(), cameraP.getZ());
		//sphereCoords = new double[] {2.44, 0.61, 0.78};
		for (Solid3D solid : solids) {
		    solid.setPerspective(cameraP.getZ(), sphereCoords[0], sphereCoords[1], sphereCoords[2]);
		}
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
	selectedSolid = camera;
    }

    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
	radiosMap.put(groupTitle, radioText);
	switch(groupTitle) {
	    case GROUP_TITLE_PROJECTION_TEXT:
		onProjection(radioText);
		break;
	    case GROUP_TITLE_OBJECTS_TEXT:
		onSolidListSelection(radioText);
		break;
	}
    }

    /////////////////////////////////////////////////////////
    public void onMouseDragged(Point curPoint, Point prevPoint) {
	//Point curPoint = me.getPoint();
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
    
    public void onMouseMoved(Point curPoint) {
	if (g == null) return;
	// set cursor type
	if (radiosMap.get(GROUP_TITLE_OBJ_CHOISE_TEXT).equals(RADIO_BY_LIST_SELECTION_TEXT)) {
	    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	    return;
	}
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	Point2D p = g.convPToGraphic(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
	for (Solid3D solid : solids) {
	    // find object borders & check the cursor hit into borders
	    double[] borders = GraphicSystem.getBorders(solid.makeTransformations());
	    if (GraphicSystem.isPointInRect(borders, p)) {
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		return;
	    }
	}	    
    }
    
    /////////////////////////////////////////////////////////
    public void onMouseWheelMoved(int notches) {
	double dx = 0, dy = 0, dz = 0, angle = 0, scale = 1;
	Matrix.AXIS axis = Matrix.AXIS.X;

	if (notches < 0) {
	    angle = ANGLE_UP;
	    axis = Matrix.AXIS.Z;
	    dz = SHIFT_STEP;
	    scale = SCALE_UP;
	} else {
	    angle = ANGLE_DOWN;
	    axis = Matrix.AXIS.Z;
	    dz = -SHIFT_STEP;
	    scale = SCALE_DOWN;
	}
	onOperation(focusedSolids, angle, axis, dx, dy, dz, scale);
    }

    /////////////////////////////////////////////////////////s
    public void onMousePressed(Point curPoint) {
	isMousePressed = true;
	String selectedRadioText = radiosMap.get(GROUP_TITLE_OBJ_CHOISE_TEXT);
	switch (selectedRadioText) {
	    //
	    case RADIO_BY_MOUSE_PRESSED_TEXT:
		Point2D p = g.convPToGraphic(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
		for (Solid3D solid : solids) {
		    // find object borders & check the cursor hit into borders
		    double[] borders = GraphicSystem.getBorders(solid.makeTransformations());
		    if (GraphicSystem.isPointInRect(borders, p)) {
			focusedSolids.add(solid);
		    }
		}
		// if the mouse is pressed in an empty area,
		// then making operations with camera
		if (focusedSolids.isEmpty()) {
		    focusedSolids.add(camera);
		}
		break;
	    //
	    case RADIO_BY_LIST_SELECTION_TEXT:
		focusedSolids.add(selectedSolid);
		break;
	}
    }

    public void onMouseReleased() {
	isMousePressed = false;
	focusedSolids.clear();
    }
    /////////////////////////////////////////////////////////
    public void onKeyPressed(KeyEvent evt) {
	int keyCode = evt.getKeyCode();

    }
    
    /////////////////////////////////////////////////////////
    @Override
    protected void paint(GraphicSystem g) {
	g.clear();
	g.reset();
	// shift the coordinates system to the center
	g.translate(DX, DY);

	drawAxis(g, axis);
	for (Solid3D solid : solids) {
	    drawSolid(g, solid);
	}
 	repaint();
   }

    /////////////////////////////////////////////////////////
    private void drawAxis(GraphicSystem g, Solid3D axis) {
	if (axis == null || axis.getLocalVertexes().length < 4) {
	    return;
	}
	g.setColor(Color.BLACK);
	g.line(axis.getVertex(0), axis.getVertex(1));
	g.line(axis.getVertex(2), axis.getVertex(3));
     }

    /////////////////////////////////////////////////////////
    private void drawSolid(GraphicSystem g, Solid3D solid) {
	if (solid == null) return;
	Point3D[] verts = solid.getWorldVertexes();
	Triangle3D[] trias = solid.getTriangles();
	
	String selectedRadioText = radiosMap.get(GROUP_TITLE_VIEW_TEXT);
	switch (selectedRadioText) {
	    case RADIO_FACES_TEXT:
		fillSolid(g, trias);
		break;
	    case RADIO_EDGES_TEXT:
		drawEdges(g, verts, solid.getDoms(), solid.getEdgesColor());
		break;
	    case RADIO_EDGES_FACES_TEXT:
		fillSolid(g, trias);
		drawEdges(g, verts, solid.getDoms(), solid.getEdgesColor());
		break;
	}
  }

    /////////////////////////////////////////////////////////
    private void drawEdges(GraphicSystem g, Point3D[] verts, DrawOrMove[] doms, Color col) {
	if (g == null || verts == null || doms == null) return;
	
	g.setColor(col);
	for (int i = 0; i < doms.length; i++) {
	    Point3D p = doms[i].getPoint3D(verts);
	    DrawOrMove.Operation op = doms[i].getOperation();
	    if (op == DrawOrMove.Operation.MOVE) {
		g.move(p);
	    } else {
		g.draw(p);
	    }
	}
    }

    /////////////////////////////////////////////////////////
    private void fillSolid(GraphicSystem g, Triangle3D[] trias) {
	if (g == null || trias == null)  return;
	
	String selectedRadioText = radiosMap.get(GROUP_TITLE_CLIPPING_TEXT);
	switch (selectedRadioText) {
	    case RADIO_PAINTER_TEXT:
		g.painterAlgorithm(trias);
		break;
	    case RADIO_Z_BUFFER_TEXT:
		g.zBufferAlgorithm(trias);
		break;
	}
    }
}
