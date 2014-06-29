package com.bondar.gm;

import static com.bondar.gm.Matrix.AXIS.X;
import static com.bondar.gm.Matrix.AXIS.Y;
import static com.bondar.gm.Matrix.AXIS.Z;
import java.awt.Dimension;

/**
 *
 * @author truebondar
 */
public abstract class Camera {

    protected int state;
    protected int attr;
    protected Point3D pos;    // world position of camera used 
    protected Vector3D dir;   // angles or look at direction of camera 
    protected double viewDist;	    // focal length 
    protected double aspectRatio;
    protected Dimension viewPort;	    // size of viewport (screen window)
    
    public Camera(int attr, Point3D pos, Vector3D dir, double dist, Dimension vp) {
	this.attr = attr;
	this.pos = pos;
	this.dir = dir;
	this.viewPort = vp;
	aspectRatio = viewPort.getWidth() / viewPort.getHeight();
	viewDist = dist;
	//viewDist = 0.5 * viewPlane.getWidth() * Math.tan(Math.toRadians(fov / 2.));
    }

    /////////////////////////////////////////////////////////
    // operations
    public void updateDirection(double a, Matrix.AXIS axis) {
	double ax = 0, ay = 0, az = 0;
	switch (axis) {
	    case X: ax = a;
		break;
	    case Y: ay = a;
		break;
	    case Z: az = a;
		break;
	}
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updateDirection(double ax, double ay, double az) {
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updatePosition(double dx, double dy, double dz) {
	pos.add(new Point3D(dx, dy, dz));
    }

    // get
    public Point3D getPosition() {
	return pos;
    }

    public Vector3D getDirection() {
	return dir;
    }
    
    public double getViewDist() {
	return viewDist;
    }
    
    public double getAspectRatio() {
	return aspectRatio;
    }
        
    //
    public abstract Matrix builtMatrix(int mode);
}
