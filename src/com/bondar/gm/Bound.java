package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class Bound {

    private double min;
    private double max;

    public Bound() {
	this.min = 0;
	this.max = 0;
    }
    
    public Bound(double min, double max) {
	this.min = min;
	this.max = max;
    }

    public void setMin(double min) {
	this.min = min;
    }

    public void setMax(double max) {
	this.max = max;
    }

    public double getMin() {
	return min;
    }

    public double getMax() {
	return max;
    }
}
