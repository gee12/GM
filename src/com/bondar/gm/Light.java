package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Vector3D;
import java.awt.Color;

/**
 *
 * @author truebondar
 */
public class Light {
    
    public static enum States {
	ON("ON"),
	OFF("OFF");
        
        private String text;

        States(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static States fromString(String text) {
            if (text != null) {
                for (States b : States.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    
    public static enum Types {
	AMBIENT("AMBIENT"),
	INFINITE("INFINITE"), // DIRECTIONAL
	POINT("POINT"),          
	SPOTLIGHT1("SPOTLIGHT1"),
	SPOTLIGHT2("SPOTLIGHT2");
        
        private String text;

        Types(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static Types fromString(String text) {
            if (text != null) {
                for (Types b : Types.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    
    private States state;
    private int id;
    private String name;
    private Types type;
 
    private Color c_ambient;   // ambient light intensity
    private Color c_diffuse;   // diffuse light intensity
    private Color c_specular;  // specular light intensity

    private Point3D  pos, transPos;       // position of light
    private Vector3D dir, transDir;       // direction of light
    private float kc, kl, kq;   // attenuation factors
    private float spot_inner;   // inner angle for spot light
    private float spot_outer;   // outer angle for spot light
    private float pf;           // power factor/falloff for spot lights

    private int   iaux1, iaux2; // auxiliary vars for future expansion
    private float faux1, faux2;

    public Light(int id, String name, Types type, States state,
	    Color c_ambient, Color c_diffuse, Color c_specular, 
	    Point3D pos, Vector3D dir, 
	    float kc, float kl, float kq,
	    float spot_inner, float spot_outer, float pf) {
	this.id = id;
	this.name = name;
	this.type = type;
	this.state = state;
	this.c_ambient = c_ambient;
	this.c_diffuse = c_diffuse;
	this.c_specular = c_specular;
	this.pos = pos;
	this.dir = dir;
	this.kc = kc;
	this.kl = kl;
	this.kq = kq;
	this.spot_inner = spot_inner;
	this.spot_outer = spot_outer;
	this.pf = pf;
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setTransPos(Point3D pos) {
        this.transPos = pos;
    }
    
    public void setTransDir(Vector3D dir) {
        this.transDir = dir;
    }
        
    /////////////////////////////////////////////////////////
    // get
    public States getState() {
	return state;
    }

    public int getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public Types getType() {
	return type;
    }

    public Point3D getPos() {
	return pos;
    }

    public Point3D getTransPos() {
	return transPos;
    }

    public Vector3D getDirection() {
	return dir;
    }

    public Vector3D getTransDirection() {
	return transDir;
    }

    public Color getC_ambient() {
        return c_ambient;
    }

    public Color getC_diffuse() {
        return c_diffuse;
    }

    public Color getC_specular() {
        return c_specular;
    }

    public float getKc() {
        return kc;
    }

    public float getKl() {
        return kl;
    }

    public float getKq() {
        return kq;
    }
    
}
