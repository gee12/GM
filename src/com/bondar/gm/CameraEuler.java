package com.bondar.gm;

import com.bondar.geom.Point2D;
import com.bondar.geom.Vector3D;
import com.bondar.geom.Point3D;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;

/**
 *
 * @author truebondar
 */
public class CameraEuler extends Camera{

    public static final int CAM_ROT_SEQ_XYZ = 0;
    public static final int CAM_ROT_SEQ_YXZ = 1;
    public static final int CAM_ROT_SEQ_XZY = 2;
    public static final int CAM_ROT_SEQ_YZX = 3;
    public static final int CAM_ROT_SEQ_ZYX = 4;
    public static final int CAM_ROT_SEQ_ZXY = 5;
    
    private Point2D viewportCenter; // center of view port (final image destination)
    
    public CameraEuler(int attr, Point3D pos, Vector3D dir, double nearClipZ, 
	    double farClipZ, double dist, double fov, Dimension vp, int mode) {
	super(attr, pos, dir, nearClipZ, farClipZ, dist, fov, vp, new Point3D(), mode);
	this.fov = fov;
	viewportCenter = new Point2D(viewPort.getWidth() / 2., viewPort.getHeight() / 2.);
	
    }

    @Override
    public Matrix builtMatrix(int camRotSeq) {
	Matrix res = new Matrix();
	Matrix invM = Matrix.buildTranlateMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
	Matrix rotateXM = Matrix.buildRotationXMatrix(-dir.getX());
	Matrix rotateYM = Matrix.buildRotationYMatrix(-dir.getY());
	Matrix rotateZM = Matrix.buildRotationZMatrix(-dir.getZ());
	
	// now compute inverse camera rotation sequence
	switch (camRotSeq) {
	    case CAM_ROT_SEQ_XYZ:
		res = res.multiply(rotateXM).multiply(rotateYM).multiply(rotateZM);
		break;
	    case CAM_ROT_SEQ_YXZ:
		res = res.multiply(rotateYM).multiply(rotateXM).multiply(rotateZM);
		break;
	    case CAM_ROT_SEQ_XZY:
		res = res.multiply(rotateXM).multiply(rotateZM).multiply(rotateYM);
		break;
	    case CAM_ROT_SEQ_YZX:
		res = res.multiply(rotateYM).multiply(rotateZM).multiply(rotateXM);
		break;
	    case CAM_ROT_SEQ_ZYX:
		res = res.multiply(rotateZM).multiply(rotateYM).multiply(rotateXM);
		break;
	    case CAM_ROT_SEQ_ZXY:
		res = res.multiply(rotateZM).multiply(rotateXM).multiply(rotateYM);
		break;
	}
	//return invM.multiply(res);
	return res.multiply(invM);
    }
}
