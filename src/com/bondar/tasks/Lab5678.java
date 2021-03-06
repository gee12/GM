package com.bondar.tasks;

import com.bondar.panels.Application;
import com.bondar.panels.GroupPanel;
import com.bondar.gm.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.Timer;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class Lab5678 extends Application {

    private static final double DISP_STEP = 0.1;
    private static final double ANGLE_UP = Math.toRadians(7);
    private static final double ANGLE_DOWN = Math.toRadians(-7);
    private static final double SCALE_UP = 1.05;
    private static final double SCALE_DOWN = 0.95;
    private static final double PERSPECT_VALUE = 0.1;
    private static final int SCREEN_WIDTH = 1100;
    private static final int SCREEN_HEIGHT = 600;
    private static final double DX = 5;
    private static final double DY = 3.5;
    private static final String GROUP_TITLE_OPERATIONS_TEXT = "ОПЕРАЦИИ:";
    private static final String RADIO_ROTATE_TEXT = "Поворот объекта";
    private static final String RADIO_TRANSFER_TEXT = "Перемещение объекта";
    private static final String RADIO_SCALE_TEXT = "Масштабирование объекта";
    private static final String RADIO_CAMERA_TEXT = "Расположение камеры";
    private static final String GROUP_TITLE_PROJECTION_TEXT = "ПРОЕКЦИЯ:";
    private static final String RADIO_ORTOGON_TEXT = "Ортогональная";
    private static final String RADIO_CENTER_TEXT = "Центральная";
    private static final String GROUP_TITLE_CLIPPING_TEXT = "ОТСЕЧЕНИЕ:";
    private static final String RADIO_PAINTER_TEXT = "Алгритм художника";
    private static final String RADIO_Z_BUFFER_TEXT = "Z-Буффер";
    private static final String GROUP_TITLE_VIEW_TEXT = "ВНЕШНИЙ ВИД:";
    private static final String RADIO_EDGES_TEXT = "Ребра";
    private static final String RADIO_FACES_TEXT = "Грани";
    private static final String RADIO_EDGES_FACES_TEXT = "Ребра и грани";

    private enum POINTS {

	PEAK,
	BASE1,
	BASE2,
	BASE3,
	BASE4
    }
    private Solid3D pyramid;
    private Solid3D cube;
    private boolean isFocusOnPyramid = false;
    private boolean isFocusOnCube = false;
    private final Solid3D axis;
    private Solid3D camera;
    GraphicSystem g;

    public static void main(String[] args) {
	new Lab5678(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /////////////////////////////////////////////////////////
    public Lab5678(int width, int height) {
	super(width, height);
	setResizable(true);
	setTitle("LR_5678 v8");
	setLocation(50, 50);
	setClip(false);
	setScale(false);

	// таймер для регулярной перерисовки
	ActionListener taskPerformer = new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		paint(getDrawablePanel().getGraphicSystem());
	    }
	};
	new Timer(500, taskPerformer).start();
	// обработка клавиш
	addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent evt) {
		onKeyPressed(evt);
	    }
	});
	addMouseMotionListener(new MMListener());
	addMouseListener(new MListener());
	addMouseWheelListener(new MWListener());
	// добавление переключателей
	addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_ORTOGON_TEXT);
	addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_CENTER_TEXT);

	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_ROTATE_TEXT);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_TRANSFER_TEXT);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_SCALE_TEXT);
	addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_CAMERA_TEXT);

	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_PAINTER_TEXT);
	addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_Z_BUFFER_TEXT);

	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_FACES_TEXT);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_FACES_TEXT);
	addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_TEXT);
	// оси
	axis = new Solid3D(new Point3DOdn[]{
	    new Point3DOdn(-5, 0, 0),
	    new Point3DOdn(5, 0, 0),
	    new Point3DOdn(0, 3.5, 0),
	    new Point3DOdn(0, -3.5, 0)});
	// точка наблюдения
	camera = new Solid3D(new Point3DOdn[]{
	    new Point3DOdn(1, 1, 1)});
	// объекты
	initSolids();
	
	g = getDrawablePanel().getGraphicSystem();
    }

    /////////////////////////////////////////////////////////
    private void initSolids() {
	pyramid = new Solid3D(new Point3DOdn[]{
	    new Point3DOdn(0, 0, 3),
	    new Point3DOdn(-1, -1, 0),
	    new Point3DOdn(-1, 1, 0),
	    new Point3DOdn(1, 1, 0),
	    new Point3DOdn(1, -1, 0)
	});
	pyramid.rotate(Math.toRadians(25), Matrix.AXIS.X);
	pyramid.rotate(Math.toRadians(40), Matrix.AXIS.Y);
	//
	try {
	    cube = Solid3D.readFromFile("cube.csv");
	} catch (Exception ex) {
	    ex.printStackTrace();
	    cube = new Solid3D(new Point3DOdn[]{
		new Point3DOdn(-1, -1, 0),
		new Point3DOdn(-1, 1, 0),
		new Point3DOdn(1, 1, 0),
		new Point3DOdn(1, -1, 0),
		new Point3DOdn(-1, -1, 1),
		new Point3DOdn(-1, 1, 1),
		new Point3DOdn(1, 1, 1),
		new Point3DOdn(1, -1, 1)
	    });
	}
	
	Random rand = new Random();
	int size = cube.getTriangles().length;
	for (int i = 0; i< size; i++) {
	    cube.getTriangles()[i].setColor(new Color(
		    rand.nextInt(255),rand.nextInt(255),rand.nextInt(255),255));
	}
	/*cube.translate(-5, 3, 0);
	cube.rotate(Math.toRadians(45), Matrix.AXIS.X);
	cube.rotate(Math.toRadians(45), Matrix.AXIS.Y);*/
    }

    /////////////////////////////////////////////////////////
    private void onOperation(double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale) {
	GroupPanel group = optionsPanel.getGroupPanel(GROUP_TITLE_OPERATIONS_TEXT);
	if (group == null) {
	    return;
	}
	String selectedRadioText = group.getSelectedRadioText();
	switch (selectedRadioText) {
	    case RADIO_ROTATE_TEXT:
		onRotate(angle, axis);
		break;
	    case RADIO_TRANSFER_TEXT:
		onTransfer(dx, dy, dz);
		break;
	    case RADIO_SCALE_TEXT:
		onScale(scale);
		break;
	    case RADIO_CAMERA_TEXT:
		onCameraTransfer(dx, dy, dz);
		break;
	}
    }

    private void onProjection() {
	GroupPanel group = optionsPanel.getGroupPanel(GROUP_TITLE_PROJECTION_TEXT);
	if (group == null || !group.isChanged()) {
	    return;
	}
	group.setChanged(false);
	String selectedRadioText = group.getSelectedRadioText();
	switch (selectedRadioText) {
	    case RADIO_ORTOGON_TEXT:
		initSolids();
		break;
	    case RADIO_CENTER_TEXT:
		Point3DOdn cameraP = camera.getVertex(0);
		// получение сферических координат камеры
		double[] sphereCoords = GraphicSystem.getSphericalCoordinates(
			cameraP.getX(), cameraP.getY(), cameraP.getZ());
		//sphereCoords = new double[] {2.44, 0.61, 0.78};
		cube.perspective(cameraP.getZ(), sphereCoords[0], sphereCoords[1], sphereCoords[2]);
		break;
	}
	repaint();
    }

    public void onRotate(double angle, Matrix.AXIS axis) {
	/*if (isFocusOnPyramid) {
	    pyramid.rotate(angle, axis);
	} else */if (isFocusOnCube) {
	    cube.rotate(angle, axis);
	}
    }

    public void onTransfer(double dx, double dy, double dz) {
	if (isFocusOnPyramid) {
	    pyramid.translate(dx, dy, dz);
	}
	if (isFocusOnCube) {
	    cube.translate(dx, dy, dz);
	}
    }

    public void onScale(double scale) {
	if (isFocusOnPyramid) {
	    pyramid.scale(scale, scale, scale);
	}
	if (isFocusOnCube) {
	    cube.scale(scale, scale, scale);
	}
    }

    public void onCameraTransfer(double dx, double dy, double dz) {
	camera.translate(dx, dy, dz);
	repaint();
    }

    /////////////////////////////////////////////////////////
    public class MMListener implements MouseMotionListener {
	private Point prevPoint = new Point();
	@Override
	public void mouseDragged(MouseEvent me) {
	    Point curPoint = me.getPoint();
	    double dx = 0, dy = 0, dz = 0, angle = 0, scale = 1;
	    Matrix.AXIS axis = Matrix.AXIS.X;

	    if (curPoint.x > prevPoint.x) {
		angle = ANGLE_UP;
		axis = Matrix.AXIS.Y;
		dx = DISP_STEP;
	    } else if (curPoint.x < prevPoint.x) {
		angle = ANGLE_DOWN;
		axis = Matrix.AXIS.Y;
		dx = -DISP_STEP;
	    }
	    if (curPoint.y > prevPoint.y) {
		angle = ANGLE_UP;
		axis = Matrix.AXIS.X;
		dy = -DISP_STEP;
	    } else if (curPoint.y < prevPoint.y) {
		angle = ANGLE_DOWN;
		axis = Matrix.AXIS.X;
		dy = DISP_STEP;
	    }
	    prevPoint = curPoint;

	    onOperation(angle, axis, dx, dy, dz, scale);
	    repaint();
	}

	@Override
	public void mouseMoved(MouseEvent me) {
	}
    }

    /////////////////////////////////////////////////////////
    public class MWListener implements MouseWheelListener {
	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
	    int notches = mwe.getWheelRotation();
	    double dx = 0, dy = 0, dz = 0, angle = 0, scale = 1;
	    Matrix.AXIS axis = Matrix.AXIS.X;

	    if (notches < 0) {
		angle = ANGLE_UP;
		axis = Matrix.AXIS.Z;
		dz = DISP_STEP;
		scale = SCALE_UP;
	    } else {
		angle = ANGLE_DOWN;
		axis = Matrix.AXIS.Z;
		dz = -DISP_STEP;
		scale = SCALE_DOWN;
	    }
	    onOperation(angle, axis, dx, dy, dz, scale);
	    repaint();
	}
    }

    /////////////////////////////////////////////////////////s
    public class MListener implements MouseListener {

	@Override
	public void mousePressed(MouseEvent me) {
	    Point curPoint = me.getPoint();
	    Point2D p = g.convPToGraphic(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
	    // находим границы объекта и проверяем попадание курсора
	    double[] pyramidBorders = GraphicSystem.getBorders(pyramid.getVertexes2D());
	    if (GraphicSystem.isPointInRect(pyramidBorders, p)) {
		isFocusOnPyramid = true;
	    } else {
		isFocusOnPyramid = false;
	    }
	    //
	    double[] cubeBorders = GraphicSystem.getBorders(cube.getVertexes2D());
	    if (GraphicSystem.isPointInRect(cubeBorders, p)) {
		isFocusOnCube = true;
	    } else {
		isFocusOnCube = false;
	    }
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	    isFocusOnPyramid = false;
	    isFocusOnCube = false;
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
	g.translate(DX, DY);
	onProjection();

	drawAxis(g, axis);
	drawSolid(g, cube, Color.BLUE);
	drawSolid(g, pyramid, Color.RED);
    }
 
    /////////////////////////////////////////////////////////
    private void drawAxis(GraphicSystem g, Solid3D axis) {
	if (cube == null) {
	    return;
	}
	int size = cube.getSize();
	if (size == 0) {
	    return;
	}
	g.setColor(Color.BLACK);
	g.line(axis.getVertex(0), axis.getVertex(1));
	g.line(axis.getVertex(2), axis.getVertex(3));
    }
    
    /////////////////////////////////////////////////////////
    private String getClippingMethod() {
	GroupPanel group = optionsPanel.getGroupPanel(GROUP_TITLE_CLIPPING_TEXT);
	if (group == null) {
	    return null;
	}
	return group.getSelectedRadioText();
    }

    /////////////////////////////////////////////////////////
    private void drawSolid(GraphicSystem g, Solid3D solid, Color col) {
	GroupPanel group = optionsPanel.getGroupPanel(GROUP_TITLE_VIEW_TEXT);
	if (group == null) {
	    return;
	}
	String selectedRadioText = group.getSelectedRadioText();
	switch (selectedRadioText) {
	    case RADIO_FACES_TEXT:
		fillSolid(g, solid, col);
		break;
	    case RADIO_EDGES_TEXT:
		drawEdges(g, solid, col);
		break;
	    case RADIO_EDGES_FACES_TEXT:
		fillSolid(g, solid, col);
		drawEdges(g, solid, Color.BLACK);
		break;
	}
	repaint();
    }
    
    /////////////////////////////////////////////////////////
    private void drawEdges(GraphicSystem g, Solid3D solid, Color col) {
	if (g == null || solid == null) return;
	g.setColor(col);
	for (int i = 0; i < solid.getDoms().length; i++) {
	    Point3DOdn p = solid.getDoms()[i].getPoint();
	    DrawOrMove.Operation op = solid.getDoms()[i].getOperation();
	    if (op == DrawOrMove.Operation.MOVE)
		g.move(p);
	    else g.draw(p);
	}
    }
    
    /////////////////////////////////////////////////////////
    private void fillSolid(GraphicSystem g, Solid3D solid, Color col) {
	if (g == null || solid == null) return;
	g.setColor(col);
	switch (getClippingMethod()) {
	    case RADIO_PAINTER_TEXT:
		// !!! cube
		Triangle3D[] trias = cube.getTriangles();
		Triangle3D[] sortTrias = GraphicSystem.painterAlgorithm(trias);
		if (sortTrias == null) return;
		for (Triangle3D tria : sortTrias) {
		    g.setColor(tria.getColor());
		    g.fillPolygon(tria.getVertexes());
		}
		break;
	    case RADIO_Z_BUFFER_TEXT:
		// !!! cube
		if (solid.equals(cube))
		    g.zBufferAlgorithm(cube);
		//drawZBuffer(g);
		break;
	}	
    }
   
    /////////////////////////////////////////////////////////
    private void drawZBuffer(GraphicSystem g) {
	//g.reset();
	/*Triangle3D[] trias = cube.getTriangles();
	ZBuffer.Cell[][] buff = g.zBufferAlgorithm(trias);
	for (int i = 0; i < buff.length; i++) {
	    for (int j = 0; j < buff[i].length; j++) {
		g.setColor(buff[i][j].getColor());
		g.line(new Point2D(i,j), new Point2D(i,j));
	    }
	}*/
    }
}
