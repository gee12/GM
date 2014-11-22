package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import com.bondar.geom.Vector3D;
import com.bondar.tasks.Main;
import java.awt.Dimension;

/**
 *
 * @author Иван
 */
public class CameraManager {
    private Camera camera;
    private CameraEuler cameraEuler;
    private CameraUVN cameraUVN;
    
    /////////////////////////////////////////////////////////
    public CameraManager(int width, int height) {
        Point3DOdn pos = new Point3DOdn(0,0,-10);
	Vector3D dir = new Vector3D(0,0,0);
	Point3D target = new Point3D(0,0,10);
	double near = 3, far = 1000, dist = 3, fov = 90;
	Dimension vp = new Dimension(width, height);
	cameraEuler = new CameraEuler(0, pos, dir, near, far, dist, fov, vp, CameraEuler.CAM_ROT_SEQ_ZYX);
	cameraUVN = new CameraUVN(0, pos, dir, near, far, dist, fov, vp, target, CameraUVN.UVN_MODE_SIMPLE);
	camera = cameraEuler;
    }
    
    /////////////////////////////////////////////////////////
    public void onSwitch(String cameraType) {
	switch(cameraType) {
	    case Main.RADIO_CAMERA_EULER_TEXT:
		camera = cameraEuler;
		break;
	    case Main.RADIO_CAMERA_UVN_TEXT:
		camera = cameraUVN;
		break;
	}
    }
    
    /////////////////////////////////////////////////////////
    public void onOperation(double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double tx, double ty, double tz) {
	camera.updateDirection(angle, axis);
	camera.updatePosition(dx, dy, dz);
	//cameraManager.getCamera().updateTarget(tx, ty, tz);
    }
    
    /////////////////////////////////////////////////////////
    // get
    public Camera getCam() {
        return camera;
    }
}
