package com.bondar.panels;

import java.awt.Cursor;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

/**
 *
 * @author bondar
 */
public class OptionsPanel extends JPanel {

    private final HashMap<String, JSlider> sliders = new HashMap<>();
    private String[] sliderValues;
    private final HashMap<String, GroupPanel> radioGroups = new HashMap<>();
    private final HashMap<String, JCheckBox> checkBoxes = new HashMap<>();

    public OptionsPanel() {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setAlignmentY(TOP_ALIGNMENT);
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    /////////////////////////////////////////////////////
    // Sliders
    public void addSlider(final String text, int min, int max, int init, final OptionsPanelListener listener) {
	addSlider(text, min, max, init, null, listener);
    }

    public void addSlider(final String text, int min, int max, int init, String[] values, final OptionsPanelListener listener) {
	this.sliderValues = values;
	final JPanel sliderPanel = new JPanel();

	final JLabel textLabel = new JLabel(text);
	sliderPanel.add(textLabel);

	final JLabel valueLabel = new JLabel();
	valueLabel.setText(Integer.toString(init));
	setSliderValueText(valueLabel, 0);
	
	final JSlider slider = new JSlider(min, max, init);
	slider.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent ce) {
		int value = slider.getValue();
		setSliderValueText(valueLabel, value);
		listener.onSliderChanged(text, value);
	    }
	});
	sliders.put(text, slider);
	sliderPanel.add(slider);
	sliderPanel.add(valueLabel);

	add(sliderPanel);
    }
    
    public int getSliderValue(String sliderName) {
	JSlider slider = sliders.get(sliderName);
	return (slider != null) ? slider.getValue() : 0;
    }
    
    private void setSliderValueText(JLabel label, int value) {
	if (sliderValues != null && sliderValues.length > value)
	    label.setText(sliderValues[value]);
	else label.setText(Integer.toString(value));
    }
   
    /////////////////////////////////////////////////////
    // Radio buttons
    public void addRadio(final String groupTitle, final String radioText, OptionsPanelListener listener) {
	GroupPanel group = null;
	if (radioGroups.containsKey(groupTitle)) {
	    group = radioGroups.get(groupTitle);
	}
	else {
	    group = new GroupPanel(groupTitle);
	    group.setListener(listener);
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

    /////////////////////////////////////////////////////
    // CheckBoxes
    public void addCheckBox(String text, boolean isChecked, OptionsPanelListener listener) {
	final JCheckBox check = new JCheckBox(text, isChecked);
	check.setFocusable(false);
	check.addItemListener(listener);
	add(check);
	checkBoxes.put(text, check);
    }
    
    public boolean isSelectedCheckBox(String text) {
	return checkBoxes.get(text).isSelected();
    }
}
