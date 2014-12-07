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
    private static Camera camera;
    private static CameraEuler cameraEuler;
    private static CameraUVN cameraUVN;
    
    /////////////////////////////////////////////////////////
//    public CameraManager(int width, int height) {
//        Point3DOdn pos = new Point3DOdn(0,0,-15);
//	Vector3D dir = new Vector3D(0.5,0.78,0);//-0.78,0.78,0);
//	Point3D target = new Point3D(0,0,10);
//	double near = 3, far = 1000, dist = 3, fov = 90;
//	Dimension vp = new Dimension(width, height);
//	cameraEuler = new CameraEuler(0, pos, dir, near, far, dist, fov, vp, CameraEuler.CAM_ROT_SEQ_ZYX);
//	cameraUVN = new CameraUVN(0, pos, dir, near, far, dist, fov, vp, target, CameraUVN.UVN_MODE_SIMPLE);
//	camera = cameraEuler;
//    }
    
    public static void init(int width, int height) {
        Point3DOdn pos = new Point3DOdn(0,0,-15);
	Vector3D dir = new Vector3D(0.5,0.78,0);//-0.78,0.78,0);
	Point3D target = new Point3D(0,0,10);
	double near = 3, far = 1000, dist = 3, fov = 90;
	Dimension vp = new Dimension(width, height);
	cameraEuler = new CameraEuler(0, pos, dir, near, far, dist, fov, vp, CameraEuler.CAM_ROT_SEQ_ZYX);
	cameraUVN = new CameraUVN(0, pos, dir, near, far, dist, fov, vp, target, CameraUVN.UVN_MODE_SIMPLE);
	camera = cameraEuler;
    }
    
    //
    public static void onSwitch(String cameraType) {
	switch(cameraType) {
	    case Main.RADIO_CAMERA_EULER:
		camera = cameraEuler;
		break;
	    case Main.RADIO_CAMERA_UVN:
		camera = cameraUVN;
		break;
	}
    }
    
    //
    public static void onOperation(double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double tx, double ty, double tz) {
	camera.updateDirection(angle, axis);
	camera.updatePosition(dx, dy, dz);
	//cameraManager.getCamera().updateTarget(tx, ty, tz);
    }
    
    /////////////////////////////////////////////////////////
    // set
    public static void setViewPort(int width, int height) {
        if (camera == null) return;
        camera.setViewPort(new Dimension(width, height));
    }
    
    /////////////////////////////////////////////////////////
    // get
    public static Camera getCam() {
        return camera;
    }
}
