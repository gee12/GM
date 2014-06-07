package com.bondar.tasks;

import com.bondar.panels.Application;
import com.bondar.gm.*;
import com.bondar.panels.RadioGroupListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.Timer;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class Lab5678 extends Application implements RadioGroupListener {

    private static final double DISP_STEP = 0.12;
    private static final double ANGLE_UP = Math.toRadians(6);
    private static final double ANGLE_DOWN = Math.toRadians(-6);
    private static final double SCALE_UP = 0.05;
    private static final double SCALE_DOWN = -SCALE_UP;
    private static final double PERSPECT_VALUE = 0.1;
    private static final int SCREEN_WIDTH = 1100;
    private static final int SCREEN_HEIGHT = 600;
    private static final double DX = 5;
    private static final double DY = 3.5;
    private static final String FILES_EXTENSION = ".gm";
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
    private static final String GROUP_TITLE_VIEW_TEXT = "ВНЕШНИЙ ВИД:";
    private static final String RADIO_EDGES_TEXT = "Ребра";
    private static final String RADIO_FACES_TEXT = "Грани";
    private static final String RADIO_EDGES_FACES_TEXT = "Ребра и грани";

    private ArrayList<Solid3D> models = new ArrayList<>();
    private ArrayList<Solid3D> focusedSolids = new ArrayList<>();
    private final Solid3D axis;
    private final Solid3D camera;
    private final GraphicSystem g;
    private HashMap<String, String> radiosMap = new HashMap<>();
 
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
	_addRadio(GROUP_TITLE_OBJECTS_TEXT, RADIO_CAMERA_TEXT);

	_addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_ROTATE_TEXT);
	_addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_TRANSFER_TEXT);
	_addRadio(GROUP_TITLE_OPERATIONS_TEXT, RADIO_SCALE_TEXT);

	_addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_ORTOGON_TEXT);
	_addRadio(GROUP_TITLE_PROJECTION_TEXT, RADIO_CENTER_TEXT);

	_addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_PAINTER_TEXT);
	_addRadio(GROUP_TITLE_CLIPPING_TEXT, RADIO_Z_BUFFER_TEXT);

	_addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_FACES_TEXT);
	_addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_FACES_TEXT);
	_addRadio(GROUP_TITLE_VIEW_TEXT, RADIO_EDGES_TEXT);

	_addRadio(GROUP_TITLE_OBJ_CHOISE_TEXT, RADIO_BY_MOUSE_PRESSED_TEXT);
	_addRadio(GROUP_TITLE_OBJ_CHOISE_TEXT, RADIO_BY_LIST_SELECTION_TEXT);
	
	setListeners(this);
	
	// оси
	axis = new Solid3D("Оси", new Point3DOdn[]{
	    new Point3DOdn(-5, 0, 0),
	    new Point3DOdn(5, 0, 0),
	    new Point3DOdn(0, 3.5, 0),
	    new Point3DOdn(0, -3.5, 0)});
	// точка наблюдения
	camera = new Solid3D("Камера", new Point3DOdn[]{
	    new Point3DOdn(2, 2, 2)});
	// объекты
	initModels();

	//
	g = getDrawablePanel().getGraphicSystem();

	// таймер для регулярной перерисовки
	ActionListener taskPerformer = new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		paint(g);
	    }
	};
	new Timer(500, taskPerformer).start();    
    }
    
    private void _addRadio(final String titleText, final String text) {
	addRadio(titleText, text);
	radiosMap.put(titleText, 
		optionsPanel.getGroupPanel(titleText).getSelectedRadioText());
    }

    /////////////////////////////////////////////////////////
    private void initModels() {
	File path = new File("models");
	String[] objFileNames = path.list(new FileFilter(FILES_EXTENSION));
	for (String fileName : objFileNames) {
	    try {
		Solid3D solid = Solid3D.readFromFile("models//" + fileName);
		Random rand = new Random();
		int size = solid.getTriangles().length;
		for (int i = 0; i < size; i++) {
		    solid.getTriangles()[i].setColor(new Color(
			    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255));
		}
		solid.setEdgesColor(new Color(
			    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255));
		
		models.add(solid);
		addRadio(GROUP_TITLE_OBJECTS_TEXT, solid.getName());
		
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
	//pyramid.rotate(Math.toRadians(25), Matrix.AXIS.X);
	//pyramid.rotate(Math.toRadians(40), Matrix.AXIS.Y);*/
	//
	/*cube.translate(-5, 3, 0);
	 cube.rotate(Math.toRadians(45), Matrix.AXIS.X);
	 cube.rotate(Math.toRadians(45), Matrix.AXIS.Y);*/
    }

    /////////////////////////////////////////////////////////
    private void onOperation(double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale) {
	String selectedRadioText = radiosMap.get(GROUP_TITLE_OPERATIONS_TEXT);
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
	}
    }

    public void onRotate(double angle, Matrix.AXIS axis) {
	for (Solid3D solid : focusedSolids) {
	    solid.updateAngle(angle, axis);
	}
    }

    public void onTransfer(double dx, double dy, double dz) {
	for (Solid3D solid : focusedSolids) {
	    solid.updateTransfers(dx, dy, dz);
	}
    }

    public void onScale(double scale) {
	for (Solid3D solid : focusedSolids) {
	    solid.updateScale(scale);
	}
    }
    
    private void onProjection() {
	String selectedRadioText = radiosMap.get(GROUP_TITLE_PROJECTION_TEXT);
	boolean isNeedPerspective = false;
	
	switch (selectedRadioText) {
	    case RADIO_ORTOGON_TEXT:
		break;
	    case RADIO_CENTER_TEXT:
		isNeedPerspective = true;
		Point3DOdn cameraP = camera.getVertex(0);
		// получение сферических координат камеры
		double[] sphereCoords = GraphicSystem.getSphericalCoordinates(
			cameraP.getX(), cameraP.getY(), cameraP.getZ());
		//sphereCoords = new double[] {2.44, 0.61, 0.78};
		for (Solid3D solid : models) {
		    solid.setPerspective(cameraP.getZ(), sphereCoords[0], sphereCoords[1], sphereCoords[2]);
		}
		break;
	}
	for (Solid3D solid : models) {
	    solid.setPerspective(isNeedPerspective);
	}
    }

    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
	radiosMap.put(groupTitle, radioText);
	onProjection();
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
	    String selectedRadioText = radiosMap.get(GROUP_TITLE_OBJ_CHOISE_TEXT);
	    switch (selectedRadioText) {
		//
		case RADIO_BY_MOUSE_PRESSED_TEXT:
		    Point curPoint = me.getPoint();
		    Point2D p = g.convPToGraphic(new Point3DOdn(curPoint.x, curPoint.y, 0)).toPoint2D();
		    for (Solid3D solid : models) {
			// находим границы объекта и проверяем попадание курсора
			double[] borders = GraphicSystem.getBorders(solid.makeTransformations());
			if (GraphicSystem.isPointInRect(borders, p)) {
			    focusedSolids.add(solid);
			}
			// if 
			if (focusedSolids.isEmpty()) {
			    focusedSolids.add(camera);
			}
		    }
		    break;
		//
		case RADIO_BY_LIST_SELECTION_TEXT:
		    
			break;
	    }
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	    focusedSolids.clear();
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

	drawAxis(g, axis);
	if (models == null) return;
	for (Solid3D solid : models) {
	    drawSolid(g, solid);
	}
   }

    /////////////////////////////////////////////////////////
    private void drawAxis(GraphicSystem g, Solid3D axis) {
	if (axis == null || axis.getVertexes().length < 4) {
	    return;
	}
	g.setColor(Color.BLACK);
	g.line(axis.getVertex(0), axis.getVertex(1));
	g.line(axis.getVertex(2), axis.getVertex(3));
     }

    /////////////////////////////////////////////////////////
    private void drawSolid(GraphicSystem g, Solid3D solid) {
	// make transformations to solid vertexes
	// and create DrawOrMove array for drawing
	Point3DOdn[] verts = solid.makeTransformations();
	Triangle3D[] trias = solid.setTriasFromVerts(verts);
	
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
	repaint();
    }

    /////////////////////////////////////////////////////////
    private void drawEdges(GraphicSystem g, Point3DOdn[] verts, DrawOrMove[] doms, Color col) {
	if (g == null || verts == null || doms == null) {
	    return;
	}
	g.setColor(col);
	for (int i = 0; i < doms.length; i++) {
	    Point3DOdn p = doms[i].getPoint3DOdn(verts);
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
	if (g == null || trias == null) {
	    return;
	}
	String selectedRadioText = radiosMap.get(GROUP_TITLE_CLIPPING_TEXT);
	switch (selectedRadioText) {
	    case RADIO_PAINTER_TEXT:
		// !!! models.get(0)
		Triangle3D[] sortTrias = GraphicSystem.painterAlgorithm(trias);
		if (sortTrias == null) {
		    return;
		}
		for (Triangle3D tria : sortTrias) {
		    g.setColor(tria.getColor());
		    g.fillPolygon(tria.getVertexes());
		}
		break;
	    case RADIO_Z_BUFFER_TEXT:
		// !!! models.get(0)
		/*if (solid.equals(models.get(0))) {
		    g.zBufferAlgorithm(models.get(0));
		}*/
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

    class FileFilter implements FilenameFilter {
	String endWith;

	public FileFilter(String endWith) {
	    this.endWith = endWith;
	}

	@Override
	public boolean accept(File dir, String name) {
	    return name.toLowerCase().endsWith(endWith);
	}
    }
}
