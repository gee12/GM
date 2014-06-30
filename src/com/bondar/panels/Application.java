package com.bondar.panels;

import com.bondar.gm.GraphicSystem;
import com.bondar.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 *
 * @author bondar
 */
public abstract class Application extends JFrame {

    private final OptionsPanel optionsPanel;
    private final DrawablePanel drawablePanel;
    
    public Application(int width, int height) {
	setLocationByPlatform(true);
	setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

	drawablePanel = new DrawablePanel(this, width, height);
	add(drawablePanel);
	optionsPanel = new OptionsPanel();
	add(optionsPanel);

	pack();
	setVisible(true);
    }
    
    protected void start(int msec) {
	load();
	init();
	new Timer(msec, taskPerformer).start();
    }
    
    /////////////////////////////////////////////////////
    // add controls
    protected void addSlider(String text, int min, int max, int init, OptionsPanelListener listener) {
	optionsPanel.addSlider(text, min, max, init, listener);
	revalidate();
    }
    
    protected void addSlider(String text, int min, int max, int init, String[] values, OptionsPanelListener listener) {
	optionsPanel.addSlider(text, min, max, init, values, listener);
	revalidate();
    }
    
    protected void addRadio(final String titleText, final String text, OptionsPanelListener listener) {
	optionsPanel.addRadio(titleText, text, listener);
	revalidate();
    }
     
    protected void addCheckBox(final String text, final boolean isChecked, OptionsPanelListener listener) {
	optionsPanel.addCheckBox(text, isChecked, listener);
	revalidate();
    }
    
    /////////////////////////////////////////////////////
    // set
    protected void setClipWindow(double xmin, double ymin, double xmax, double ymax) {
	drawablePanel.getGraphicSystem().setClipWindow(xmin,ymin,xmax,ymax);
    }
    protected void setClipWindow(Point2D[] points) {
	drawablePanel.getGraphicSystem().setClipWindow(points);
    }
    
    protected void setClip(boolean isNeedClip) {
	drawablePanel.getGraphicSystem().setClip(isNeedClip);
    }
    
    protected void setScale(boolean isNeedScale) {
	drawablePanel.getGraphicSystem().setScale(isNeedScale);
    }
    
    // get
    protected OptionsPanel getOptionsPanel() {
	return optionsPanel;
    }
    
    protected DrawablePanel getDrawablePanel() {
	return drawablePanel;
    }
    
    protected GraphicSystem getGraphicSystem() {
	return drawablePanel.getGraphicSystem();
    }

    protected int getSliderValue(String sliderName) {
	return optionsPanel.getSliderValue(sliderName);
    }
    
    protected String getSelectedRadioText(final String titleText) {
	return optionsPanel.getGroupPanel(titleText).getSelectedRadioText();
    }
    
    protected boolean isSelectedCheckBox(String text) {
	return optionsPanel.isSelectedCheckBox(text);
    }

    //
    protected abstract void load();
    protected abstract void init();
    protected abstract void update();
    protected abstract void paint(GraphicSystem g);
    
    // timer for regular update and repaint
    ActionListener taskPerformer = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent evt) {
	    update();
	    drawablePanel.repaint();
	}
    };
}
