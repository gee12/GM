package com.bondar.gm;

import static com.bondar.gm.Matrix.AXIS.X;
import static com.bondar.gm.Matrix.AXIS.Y;
import static com.bondar.gm.Matrix.AXIS.Z;

/**
 *
 * @author truebondar
 */
public class Camera {

    public static final int CAM_ROT_SEQ_XYZ = 0;
    public static final int CAM_ROT_SEQ_YXZ = 1;
    public static final int CAM_ROT_SEQ_XZY = 2;
    public static final int CAM_ROT_SEQ_YZX = 3;
    public static final int CAM_ROT_SEQ_ZYX = 4;
    public static final int CAM_ROT_SEQ_ZXY = 5;
    
    protected int state;
    protected int attr;
    protected Point3D pos;    // world position of camera used 
    protected Vector3D dir;   // angles or look at direction of camera 
   
    public Camera(int attr, Point3D pos, Vector3D dir) {
	this.attr = attr;
	this.pos = pos;
	this.dir = dir;
    }

   /////////////////////////////////////////////////////////
    // operations
    public void updateAngle(double a, Matrix.AXIS axis) {
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
    
    public void updateAngles(double ax, double ay, double az) {
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updateTransfers(double dx, double dy, double dz) {
	pos.add(new Point3D(dx, dy, dz));
    }

    // get
    public Point3D getPosition() {
	return pos;
    }

    public Vector3D getDirection() {
	return dir;
    }
    
}
