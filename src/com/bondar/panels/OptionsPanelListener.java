package com.bondar.panels;

import java.awt.event.ItemListener;

/**
 *
 * @author truebondar
 */
public interface OptionsPanelListener extends ItemListener {
    public abstract void onRadioSelected(final String groupTitle, final String radioText);
    public abstract void onSliderChanged(final String sliderName, final int value);
}
