package com.bondar.geom;

import com.bondar.gm.Camera;
import com.bondar.gm.TransferManager;

/**
 *
 * @author Иван
 */
public class BoundingSphere3D {
    
    private double maxRadius;
    private double avgRadius;

    public BoundingSphere3D(Point3D[] verts) {
        maxRadius = BoundingBox3D.maxRadius(verts);
        avgRadius = BoundingBox3D.avgRadius(verts);
    }
    
    /////////////////////////////////////////////////////////
    // Is 3D point in camera coord's into object.
    // cp - point in CAMERA coord's
    // pos - object position in WORLD coord's
    // scale - object scale (1.0 - normal scale)
    public boolean isCameraPointInto(Point3D cp, Camera cam, Point3D pos, double scale) {
	if (cp == null || pos == null) return false;
        // center - object position in CAMERA coord's
        Point3D center = TransferManager.transToCamera(pos, cam);
        // scalled object radius
        double sRadius = maxRadius * scale;
	double px = cp.getX(), py = cp.getY(), pz = cp.getZ();
        
	return ((px >= center.getX() - sRadius) && (px <= center.getX() + sRadius)
		&& (py >= center.getY() - sRadius) && (py <= center.getY() + sRadius)
		&& (pz >= center.getZ() - sRadius) && (pz <= center.getZ() + sRadius));
    }
    
    /////////////////////////////////////////////////////////
    // Is 2D point in camera coord's into object.
    // cp - point in CAMERA coord's
    // pos - object position in WORLD coord's
    // scale - object scale (1.0 - normal scale)
    public boolean isCameraPointInto(Point2D cp, Camera cam, Point3D pos, double scale) {
	if (cp == null || cam == null || pos == null) return false;
        // center - object position in CAMERA coord's
        Point3D center = TransferManager.transToCamera(pos, cam);
        // scalled object radius
        double sRadius = maxRadius * scale;
	double px = cp.getX(), py = cp.getY();
        
	return ((px >= center.getX() - sRadius) && (px <= center.getX() + sRadius)
		&& (py >= center.getY() - sRadius) && (py <= center.getY() + sRadius));
    }  
    
    public double getMaxRadius() {
        return maxRadius;
    }

    public double getAvgRadius() {
        return avgRadius;
    }
}
