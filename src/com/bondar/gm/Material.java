package com.bondar.gm;

import java.awt.Color;

/**
 *
 * @author truebondar
 */
public class Material {
    
    public enum States {
	ACTIVE,
	INACTIVE
    }
    
    public static enum Types {
	CONSTANT,
	EMMISIVE,
	FLAT,
	GOURAUD,
	FASTPHONG,
	TEXTURE
    } 
    
    public static final int ATTR_2_SIDES = 1;
    public static final int ATTR_TRANSPARENT = 2;
    
    private States state;
    private int id;
    private String name;
    private Types type;
    private int  attributes; 
    private Color color;
    private float ka,	// ambient
	    kd,		// diffuse
	    ks,		// specular
	    power;	// specular power
    private Color ra,	// the reflectivities/colors premultiplied
	    rd,		// r = color*k
	    rs; 
    //private Bitmap texture;    // actual texture map (if any)
    private int   iaux1, iaux2;      // auxiliary vars for future expansion
    private float faux1, faux2;

    public Material(int id, String name, Types type, int attributes, States state, Color color, 
	    float ka, float kd, float ks, float power, 
	    Color ra, Color rd, Color rs, 
	    int iaux1, int iaux2, float faux1, float faux2) {
	this.id = id;
	this.name = name;
	this.type = type;
	this.attributes = attributes;
	this.state = state;
	this.color = color;
	this.ka = ka;
	this.kd = kd;
	this.ks = ks;
	this.power = power;
	this.ra = ra;
	this.rd = rd;
	this.rs = rs;
	this.iaux1 = iaux1;
	this.iaux2 = iaux2;
	this.faux1 = faux1;
	this.faux2 = faux2;
    }
    
    /////////////////////////////////////////////////////////
    // set
    public final void setAttributes(int attr) {
	attributes |= attr;
    }

    public final void unsetAttribute(int attr) {
	attributes &= ~attr;
    }
    
    /////////////////////////////////////////////////////////
    // get
    public boolean isSetAttribute(int attr) {
	return (attributes & attr) != 0;
    }   

    public int getAttributes() {
	return attributes;
    }
    
    public States getState() {
	return state;
    }

    public int getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public Color getColor() {
	return color;
    }
    
}
