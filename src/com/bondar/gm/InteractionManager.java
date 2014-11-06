package com.bondar.gm;

import com.bondar.geom.BoundingBox3D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Solid3D;
import com.bondar.tasks.GM;

/**
 *
 * @author truebondar
 */
public class InteractionManager {
    
    public InteractionManager() {
	
    }
    
    /*public static void collision(Solid3D[] models) {
	if (models == null) return;
	for (Solid3D model1 : models) {
	    for (Solid3D model2 : models) {
		if (model2.equals(model1)) continue;
		// if solids are intersect, then shift them to opposite directions
		if (model2.isIntersect(model1.getBounds())) {
		    boolean isFixed1 = model1.isSetAttribute(Solid3D.ATTR_FIXED);
		    boolean isFixed2 = model2.isSetAttribute(Solid3D.ATTR_FIXED);
		    if (isFixed1 && isFixed2) continue;
		    //
		    double dx1=0,dy1=0,dz1=0,
			    dx2=0,dy2=0,dz2=0;
		    Point3D cp1 = BoundingBox3D.center(model1.getBounds());
		    Point3D cp2 = BoundingBox3D.center(model2.getBounds());
		    // shift to X axis
		    if (cp2.getX() > cp1.getX()) {
			dx2 = GM.SHIFT_STEP;
		    } else {
			dx1 = -GM.SHIFT_STEP;
		    }
		    // shift to Y axis
		    if (cp2.getY() > cp1.getY()) {
			dy2 = GM.SHIFT_STEP;
		    } else {
			dy1 = -GM.SHIFT_STEP;
		    }
		    // shift to Z axis
		    if (cp2.getZ() > cp1.getZ()) {
			dz2 = GM.SHIFT_STEP;
		    } else {
			dz1 = -GM.SHIFT_STEP;
		    }
		    //
		    if (!isFixed1) model1.updateTransfers(dx1,dy1,dz1);
		    if (!isFixed2) model2.updateTransfers(dx2,dy2,dz2);
		}
	    }
	}
    }*/
}
