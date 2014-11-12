package com.bondar.gm;

import com.bondar.geom.ClipBox3D;
import com.bondar.geom.Vector3D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import static com.bondar.gm.Matrix.AXIS.X;
import static com.bondar.gm.Matrix.AXIS.Y;
import static com.bondar.gm.Matrix.AXIS.Z;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;

/**
 *
 * @author truebondar
 */
public abstract class Camera {

    protected int state;
    protected int attr;
    protected int buildMode;
    protected Point3DOdn pos;    // world position of camera used 
    protected Vector3D dir;   // angles or look at direction of camera 
    protected double viewDist;	    // focal length 
    protected double aspectRatio;
    protected Dimension viewPort;	    // size of viewport (screen window)
    protected Dimension2D viewPlane;  // width and height of view plane to project onto
    protected ClipBox3D clipBox;	    // 3d clipping planes
    protected double fov;		    // field of view for both horizontal and vertical axes
    protected Point3D target;	// look at target
   
    public Camera(int attr, Point3DOdn pos, Vector3D dir, double nearClipZ, double farClipZ, 
	    double dist, double fov, Dimension vp, Point3D target, int mode) {
	this.attr = attr;
	this.pos = pos;
	this.dir = dir;
	this.viewPort = vp;
	this.buildMode = mode;
	this.target = target;
	// usually 2x2 for normalized projection or 
	// the exact same size as the viewport (screen window)
	viewPlane = new Dimension();
	viewPlane.setSize(2., 2. / aspectRatio);
	aspectRatio = viewPort.getWidth() / viewPort.getHeight();
	viewDist = dist;
	//viewDist = 0.5 * viewPlane.getWidth() * Math.tan(Math.toRadians(fov / 2.));
	clipBox = new ClipBox3D();
	clipBox.setNearClipZ(nearClipZ);
	clipBox.setFarClipZ(farClipZ);
	Point3D origin = new Point3DOdn(0, 0, 0);
	if (fov == 90.0) {
	    clipBox.setRtPlane(origin, new Vector3D(1, 0, -1), true);
	    clipBox.setLtPlane(origin, new Vector3D(-1, 0, -1), true);
	    clipBox.setTpPlane(origin, new Vector3D(0, 1, -1), true);
	    clipBox.setBtPlane(origin, new Vector3D(0, -1, -1), true);
	} else {
	    double z = -viewPlane.getWidth()/2.0;
	    clipBox.setRtPlane(origin, new Vector3D(viewDist,0,z), true);
	    clipBox.setLtPlane(origin, new Vector3D(-viewDist,0,z), true);
	    clipBox.setTpPlane(origin, new Vector3D(0,viewDist,z), true);
	    clipBox.setBtPlane(origin, new Vector3D(0,-viewDist,z), true);
	}
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
        pos.mul(Matrix.rotationMatrix(-a, axis));
    }
    
    public void updateDirection(double ax, double ay, double az) {
	dir.add(new Point3D(ax, ay, az));
        if (ax != 0.0) pos.mul(Matrix.rotationMatrix(-ax, X));
        if (ay != 0.0) pos.mul(Matrix.rotationMatrix(-ay, Y));
        if (az != 0.0) pos.mul(Matrix.rotationMatrix(-az, Z));
    }
    
    public void updatePosition(double dx, double dy, double dz) {
	pos.add(new Point3D(dx, dy, dz));
    }
    
    public void updateTarget(double dx, double dy, double dz) {
	target.add(new Point3D(dx, dy, dz));
    }
    
    // set
    public void setBuildMode(int mode) {
	this.buildMode = mode;
    }

    // get
    public int getBuildMode() {
	return buildMode;
    }
 
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
    
    public ClipBox3D getClipBox() {
	return clipBox;
    }
    
    public Dimension2D getViewPlane() {
	return viewPlane;
    }
    
    public Dimension2D getViewPort() {
	return viewPort;
    }  
    
    //
    public abstract Matrix builtMatrix(int mode);
}
