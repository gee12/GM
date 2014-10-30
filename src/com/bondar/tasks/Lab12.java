package com.bondar.tasks;

import com.bondar.panels.Application;
import com.bondar.gm.GraphicSystem2D;
import com.bondar.geom.ClipBox2D;
import static com.bondar.gm.GraphicSystem2D.X_MAX;
import static com.bondar.gm.GraphicSystem2D.Y_MAX;
import static com.bondar.gm.GraphicSystem2D.BORDER;
import com.bondar.gm.Matrix.AXIS;
import com.bondar.geom.Point2D;
import com.bondar.panels.OptionsPanelListener;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.util.HashMap;

/**
 * Вариант 8
 *
 * @author bondar
 */
public class Lab12 extends Application implements OptionsPanelListener {

    private static final double ARROW_WIDTH = 0.6;
    private static final double ARROW_HEIGHT = 0.2;
    private static final double[][] ARROW_PTS = {
	// x, y
	{0.0, 0.0},
	{ARROW_WIDTH, 0.0},
	{4d * ARROW_WIDTH / 7d, ARROW_HEIGHT},
	{4d * ARROW_WIDTH / 7d, -ARROW_HEIGHT},
	{ARROW_WIDTH, 0.0},};
    private static final String ARROW_COUNT_TEXT = "Количество:";
    private static final String FREQ_TEXT = "Частота:";
    private static final String AMPLITUDE_TEXT = "Амплитуда: ";
    private static final String SCALE_TEXT = "Масштаб:";
    
    public static void main(String[] args) {
	new Lab12(1000, 600);
    }
    
    /////////////////////////////////////////////////////////
    public Lab12(int width, int height) {
	super(width, height);
	setResizable(true);
	setLocationByPlatform(false);
	setTitle("LR_1_2 v8");
	addSlider(ARROW_COUNT_TEXT, 0, 80, 30, this);
	addSlider(FREQ_TEXT, 1, 200, 3, this);
	addSlider(AMPLITUDE_TEXT, 1, 50, 30, this);
	addSlider(SCALE_TEXT, 1, 300, 300, this);
	
	setClipWindow();
   }

    private void setClipWindow() {
	setClip(true);
	setClipWindow(
		BORDER,
		BORDER,
		X_MAX - BORDER,
		Y_MAX - BORDER);
	Point2D[] paral = new Point2D[]{
	    new Point2D(BORDER, BORDER / 2),
	    new Point2D(BORDER, Y_MAX - BORDER * 2),
	    new Point2D(X_MAX - BORDER, Y_MAX - BORDER / 2),
	    new Point2D(X_MAX - BORDER, BORDER * 2)};
	Point2D[] rect = new Point2D[]{
	    new Point2D(0, 0),
	    new Point2D(0, Y_MAX),
	    new Point2D(X_MAX, Y_MAX),
	    new Point2D(X_MAX, 0)};

	//setClipWindow(paral);
	System.out.println(getGraphicSystem().getClipWindow().getState());
    }

    /////////////////////////////////////////////////////////
    @Override
    protected void paint(GraphicSystem2D g) {
	g.clear();
	drawSinArrows(g);
	drawClipWindow(g);
	repaint();
    }

    /////////////////////////////////////////////////////////
    private void drawSinArrows(GraphicSystem2D g) {
	final double xShift = 1.5d, yShift = 0d;
	double scale;
	final double count = getSliderValue(ARROW_COUNT_TEXT);
	final double freq = getSliderValue(FREQ_TEXT);
	final double yScale = getSliderValue(AMPLITUDE_TEXT) / 100d;
	final double mScale = getSliderValue(SCALE_TEXT) / 100d;
		
	double oldX = 2, oldY = 2;
	for (int i = 0; i < count; i++) {
	    scale = (i / freq);
	    double curX = oldX + ARROW_WIDTH * scale;
	    double curY = oldY + Math.sin(curX) * yScale;

	    double angl1 = -Math.atan2(curY - oldY, curX - oldX);
	    double angl2 = -Math.sin(curX);
//            g.rotate(-Math.sin(curX));
//            drawArrow(g, scale);

	    g.setColor(Color.BLUE);
	    g.reset();
	    g.scale(mScale);
	    g.translate(curX - xShift, curY - yShift);
	    g.rotate(angl1, AXIS.Z);
	    drawArrow(g, scale);

	    g.setColor(Color.RED);
	    g.reset();
	    g.scale(mScale);
	    g.translate(curX - xShift, curY - yShift);
	    g.rotate(angl2, AXIS.Z);
	    drawArrow(g, scale);

	    oldX = curX;
	    oldY = curY;
	}
    }

    private void drawArrow(GraphicSystem2D g, double scale) {
	final int length = ARROW_PTS.length;
	double[][] points = new double[length][2];
	for (int i = 0; i < length; i++) {
	    points[i][0] = ARROW_PTS[i][0] * scale;
	    points[i][1] = ARROW_PTS[i][1] * scale;
	}
	double oldX = points[0][0], oldY = points[0][1];
	g.move(oldX, oldY);
	for (int i = 1; i < length; i++) {
	    g.draw(points[i][0], points[i][1]);
	}
    }

    /////////////////////////////////////////////////////////
    private void drawClipWindow(GraphicSystem2D g) {
	g.reset();
	g.setColor(Color.BLACK);
	final ClipBox2D cw = g.getClipWindow();
	setClip(false);
	if (cw.getType() == ClipBox2D.Type.Rectangle) {
	    g.move(cw.getA());
	    g.draw(cw.getB());
	    g.draw(cw.getC());
	    g.draw(cw.getD());
	    g.draw(cw.getA());
	} else if (cw.getType() == ClipBox2D.Type.Polygon) {
	    g.move(cw.getPoints()[0]);
	    for (int i = 1; i < cw.getCount(); i++) {
		g.draw(cw.getPoints()[i]);
	    }
	    g.draw(cw.getPoints()[0]);
	}
	setClip(true);
    }

    @Override
    protected void load() {
    }

    @Override
    protected void init() {
    }

    @Override
    protected void update() {
    }

    @Override
    public void onRadioSelected(String groupTitle, String radioText) {
    }

    @Override
    public void onSliderChanged(String sliderName, int value) {
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
    }
}
