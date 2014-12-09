package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Solid3D;

/**
 *
 * @author Иван
 */
public class CullManager {
    
    final static double HALF_SCREEN_X = 0.5;
    final static double HALF_SCREEN_Y = 0.5;
        
    /////////////////////////////////////////////////////////
    // Cull solid, if it's fully out of clip bounds.
    public static boolean isCulled(Solid3D model, Camera cam) {
	if (model == null || cam == null) return false;
        // transfer object position (from world to camera coord's)
	Point3D spherePos = TransferManager.transToCamera(model.getPosition(), cam);
        double maxRadius = model.getBounds().getMaxRadius();
	// by z
	if (((spherePos.getZ() - maxRadius) > cam.getClipBox().getFarClipZ()) ||    // far side
	    ((spherePos.getZ() + maxRadius) < cam.getClipBox().getNearClipZ())) {   // near side
	    return true;
	}
	// by x
	double zTest = HALF_SCREEN_X * cam.getViewPlane().getWidth() * spherePos.getZ() / cam.getViewDist();
	if (((spherePos.getX() - maxRadius) > zTest)  || // right side
	    ((spherePos.getX() + maxRadius) < -zTest) ) { // left side, note sign change
	    return true;
	}
	// by y
	zTest = HALF_SCREEN_Y * cam.getViewPlane().getHeight()* spherePos.getZ() / (cam.getViewDist() * cam.getAspectRatio());
	if (((spherePos.getY() - maxRadius) > zTest)  || // down side
	    ((spherePos.getY() + maxRadius) < -zTest) ) { // up side, note sign change
	    return true;
	}
	return false;
    }
    
    /////////////////////////////////////////////////////////
    // Cull polygon (in camera coordinates)
    public static boolean isCulled(Polygon3DVerts poly, Camera cam) {
        if (poly == null || poly.getSize() < 3 || cam == null) return false;
        Point3D p0 = poly.getVertexPosition(0),
                p1 = poly.getVertexPosition(1),
                p2 = poly.getVertexPosition(2);
        // by z
        double z0 = p0.getZ(),
                z1 = p1.getZ(),
                z2 = p2.getZ();
        
	if ((z0 > cam.getClipBox().getFarClipZ()           // far side
                && z1 > cam.getClipBox().getFarClipZ()
                && z2 > cam.getClipBox().getFarClipZ()) ||    
	    (z0 < cam.getClipBox().getNearClipZ()           // near side
                && z1 < cam.getClipBox().getNearClipZ()
                && z2 < cam.getClipBox().getNearClipZ())) {
	    return true;
	}
        // by x
        double x0 = p0.getX(),
                x1 = p1.getX(),
                x2 = p2.getX();
        double zFactor = HALF_SCREEN_Y * cam.getViewPlane().getWidth() / cam.getViewDist();
        double zTest0 = zFactor * z0;
        double zTest1 = zFactor * z1;
        double zTest2 = zFactor * z2;

        if ((x0 > zTest0 && x1 > zTest1 && x2 > zTest2)
                || (x0 < -zTest0 && x1 < -zTest1 && x2 < -zTest2)) {
                return true;
        }        
        // by y
        double y0 = p0.getY(),
                y1 = p1.getY(),
                y2 = p2.getY();
        zFactor = HALF_SCREEN_Y * cam.getViewPlane().getHeight() / (cam.getViewDist() * cam.getAspectRatio());
        zTest0 = zFactor * z0;
        zTest1 = zFactor * z1;
        zTest2 = zFactor * z2;

        if ((y0 > zTest0 && y1 > zTest1 && y2 > zTest2)
                || (y0 < -zTest0 && y1 < -zTest1 && y2 < -zTest2)) {
            return true;
        }
        return false;
    }
    
//    public static void cullAndClip(Polygon3DVerts poly, Camera cam) {
//        
//    }
}
