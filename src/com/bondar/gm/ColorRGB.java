package com.bondar.gm;

import java.awt.Color;
/**
 *
 * @author truebondar
 */
public class ColorRGB extends Color {
    
    //private Color color;

    public ColorRGB(Color color) {
	super(color.getRGB());
    }
    
    public ColorRGB(int rgb) {
	super(rgb);
    } 
    
    public ColorRGB(int r, int g, int b) {
	super(r, g, b);
    }
    
    public ColorRGB(float r, float g, float b) {
	super(r, g, b);
    }  
    
    public ColorRGB(int r, int g, int b, int a) {
	super(r, g, b, a);
    }
    
    public ColorRGB(float r, float g, float b, float a) {
	super(r, g, b, a);
    }
    
    /////////////////////////////////////////////////////////
    // update
    public ColorRGB updateRed(int r) {
	return new ColorRGB(getRed() + r, getGreen(), getBlue(), getAlpha());
    }
    
    public ColorRGB updateGreen(int g) {
	return new ColorRGB(getRed(), getGreen() + g, getBlue(), getAlpha());
    }
    
    public ColorRGB updateBlue(int b) {
	return new ColorRGB(getRed(), getGreen(), getBlue() + b, getAlpha());
    }
    
    public ColorRGB updateRGB(int r, int g, int b) {
	return new ColorRGB(getRed() + r, getGreen() + g, getBlue() + b, getAlpha());
    }
             
    /////////////////////////////////////////////////////////
    // set
    public ColorRGB setRed(int r) {
	return new ColorRGB(r, getGreen(), getBlue(), getAlpha());
    }
    
    public ColorRGB setGreen(int g) {
	return new ColorRGB(getRed(), g, getBlue(), getAlpha());
    }
    
    public ColorRGB setBlue(int b) {
	return new ColorRGB(getRed(), getGreen(), b, getAlpha());
    }

    /*public ColorRGB(Color color) {
	this.color = color;
    }
    
    public ColorRGB(int rgb) {
	this.color = new Color(rgb);
    } 
    
    public ColorRGB(int r, int g, int b) {
	this.color = new Color(r, g, b);
    }
    
    public ColorRGB(float r, float g, float b) {
	this.color = new Color(r, g, b);
    }  
    
    public ColorRGB(int r, int g, int b, int a) {
	this.color = new Color(r, g, b, a);
    }
    
    public ColorRGB(float r, float g, float b, float a) {
	this.color = new Color(r, g, b, a);
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setRed(int r) {
	color = new Color(r, getGreen(), getBlue(), getAlpha());
    }
    
    public void setGreen(int g) {
	color = new Color(getRed(), g, getBlue(), getAlpha());
    }
    
    public void setBlue(int b) {
	color = new Color(getRed(), getGreen(), b, getAlpha());
    }
            
    /////////////////////////////////////////////////////////
    // get    
    public int getRed() {
	return color.getRed();
    }
    
    public int getGreen() {
	return color.getGreen();
    }
    
    public int getBlue() {
	return color.getBlue();
    }
    
    public int getAlpha() {
	return color.getAlpha();
    }*/
    
}
