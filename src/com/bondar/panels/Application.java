package com.bondar.panels;

import com.bondar.gm.GraphicSystem;
import com.bondar.gm.Point2D;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author bondar
 */
public abstract class Application extends JFrame {

    protected final OptionsPanel optionsPanel;
    protected DrawablePanel drawablePanel;

    public Application(int width, int height) {
	setLocationByPlatform(true);
	setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

	drawablePanel = new DrawablePanel(this, width, height);
	add(drawablePanel);
	optionsPanel = new OptionsPanel(drawablePanel, width);
	add(optionsPanel);

	pack();
	setVisible(true);
    }

    public OptionsPanel getOptionsPanel() {
	return optionsPanel;
    }
    
    public DrawablePanel getDrawablePanel() {
	return drawablePanel;
    }
    
    public GraphicSystem getGraphicSystem() {
	return drawablePanel.getGraphicSystem();
    }

    public void setClipWindow(double xmin, double ymin, double xmax, double ymax) {
	drawablePanel.getGraphicSystem().setClipWindow(xmin,ymin,xmax,ymax);
    }
    public void setClipWindow(Point2D[] points) {
	drawablePanel.getGraphicSystem().setClipWindow(points);
    }
    
    public void setClip(boolean isNeedClip) {
	drawablePanel.getGraphicSystem().setClip(isNeedClip);
    }
    
    public void setScale(boolean isNeedScale) {
	drawablePanel.getGraphicSystem().setScale(isNeedScale);
    }
    
    public void addSlider(String text, int min, int max, int init) {
	optionsPanel.addSlider(text, min, max, init);
	revalidate();
    }
    
    public void addSlider(String text, int min, int max, int init, String[] values) {
	optionsPanel.addSlider(text, min, max, init, values);
	revalidate();
    }
    
    public void addRadio(final String titleText, final String text) {
	optionsPanel.addRadio(titleText, text);
	revalidate();
    }

    public int getSliderValue(String sliderName) {
	return optionsPanel.getSliderValue(sliderName);
    }
    
    public void setRadioGroupListeners(RadioGroupListener listener) {
	optionsPanel.setListeners(listener);
    }

    protected abstract void paint(GraphicSystem g);
}
