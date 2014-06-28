package com.bondar.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 *
 * @author truebondar
 */
public class GroupPanel extends JPanel {
    
    private final MyButtonGroup group;
    private String selectedRadioText = "";
    private OptionsPanelListener listener = null;
  
    public GroupPanel(final String titleText) {
	
	group = new MyButtonGroup();
	//
	group.addActionListener(new ActionListener() {
	    private String lastText = "";
	    @Override
	    public void actionPerformed(ActionEvent e) {
		selectedRadioText = e.getActionCommand();
		lastText = selectedRadioText;
		if (listener == null) return;
		listener.onRadioSelected(titleText, selectedRadioText);
	    }
	});
	setBorder(BorderFactory.createTitledBorder(titleText));
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    public void setListener(OptionsPanelListener listener) {
	this.listener = listener;
    }
    
    public void addRadio(final String radioText) {
	final JRadioButton radio = new JRadioButton(radioText);
	radio.setActionCommand(radioText);
	// setFocusable(false) -> for work KeyListener in main JFrame
	radio.setFocusable(false);
	group.add(radio);
	add(radio);
 	selectedRadioText = group.getElements().nextElement().getText();
   }
    
    public ButtonGroup getRadioGroup() {
	return group;
    }
    
    public String getSelectedRadioText() {
	return selectedRadioText;
    }
}
