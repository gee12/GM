package com.bondar.panels;

import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

/**
 *
 * @author bondar
 */
public class OptionsPanel extends JPanel {

    private final JPanel drawablePanel;
    private final HashMap<String, Integer> slidersVal;
    private String[] sliderValues;
    private final HashMap<String, GroupPanel> radioGroups;

    public OptionsPanel(JPanel drawablePanel, int width) {
	this.drawablePanel = drawablePanel;
	slidersVal = new HashMap<>();
	radioGroups = new HashMap<>();
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setAlignmentY(TOP_ALIGNMENT);
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /////////////////////////////////////////////////////
    public int getSliderValue(String sliderName) {
	if (slidersVal == null) {
	    return 0;
	}
	return slidersVal.get(sliderName);
    }
    
    public void addSlider(final String text, int min, int max, int init) {
	addSlider(text, min, max, init, null);
    }

    public void addSlider(final String text, int min, int max, int init, String[] values) {
	slidersVal.put(text, init);
	this.sliderValues = values;
	final JPanel sliderPanel = new JPanel();

	final JLabel textLabel = new JLabel(text);
	sliderPanel.add(textLabel);

	final JLabel valueLabel = new JLabel();
	valueLabel.setText(Integer.toString(init));
	setSliderText(valueLabel, 0);
	
	final JSlider slider = new JSlider(min, max, init);
	slider.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent ce) {
		int value = slider.getValue();
		slidersVal.put(text, value);
		
		setSliderText(valueLabel, value);
		drawablePanel.repaint();
	    }
	});
	sliderPanel.add(slider);
	sliderPanel.add(valueLabel);

	add(sliderPanel);
    }

    private void setSliderText(JLabel label, int value) {
	if (sliderValues != null && sliderValues.length > value)
	    label.setText(sliderValues[value]);
	else label.setText(Integer.toString(value));
    }
 
    public HashMap<String, Integer> getSlidersValues() {
	return slidersVal;
    }
   
    /////////////////////////////////////////////////////
    public void addRadio(final String groupTitle, final String radioText) {
	GroupPanel group = null;
	if (radioGroups.containsKey(groupTitle)) {
	    group = radioGroups.get(groupTitle);
	}
	else {
	    group = new GroupPanel(groupTitle);
	    add(group);
	    radioGroups.put(groupTitle, group);
	}
	group.addRadio(radioText);
	// оставляем первый элемент выбранным
	group.getRadioGroup().getElements().nextElement().setSelected(true);
    }
    
    public GroupPanel getGroupPanel(final String groupTitle) {
	return radioGroups.get(groupTitle);
    }
    
    public void setListeners(RadioGroupListener listener) {
	for (GroupPanel group : radioGroups.values()) {
	    group.setListener(listener);
	}
    }
/*
    public String getSelectedRadioText(final String groupTitle) {
	GroupPanel group = groups.get(groupTitle);
	if (group == null) return null;
	for (Enumeration<AbstractButton> buttons = group.getRadioGroup().getElements(); buttons.hasMoreElements();) {
	    AbstractButton button = buttons.nextElement();
	    if (button.isSelected()) {
		return button.getText();
	    }
	}
	return null;
    }*/
}
