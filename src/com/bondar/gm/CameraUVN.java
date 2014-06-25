package com.bondar.gm;

import java.awt.Dimension;

/**
 *
 * @author truebondar
 */
public class CameraUVN extends Camera{

    private Point3D target;	// look at target
    private Vector3D u;		// vectors to track the camera orientation
    private Vector3D v;
    
    public CameraUVN(int attr, Point3D pos, Vector3D dir,
	    float nearClipZ, float farClipZ, float fov, Dimension viewPort) {
	super(attr, pos, dir);
	
    }

    public Matrix builtMatrix(int camRotSeq) {
	Matrix res = new Matrix();
	/*Matrix transM = Matrix.getTransferMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
	Matrix rotateXM = Matrix.getRotationMatrix(-dir.getX(), Matrix.AXIS.X);
	Matrix rotateYM = Matrix.getRotationMatrix(-dir.getY(), Matrix.AXIS.Y);
	Matrix rotateZM = Matrix.getRotationMatrix(-dir.getZ(), Matrix.AXIS.Z);

	// now compute inverse camera rotation sequence
	switch (camRotSeq) {
	    case CAM_ROT_SEQ_XYZ:
		res = rotateXM.multiply(rotateYM).multiply(rotateZM);
		break;
	    case CAM_ROT_SEQ_YXZ:
		res = rotateYM.multiply(rotateXM).multiply(rotateZM);
		break;
	    case CAM_ROT_SEQ_XZY:
		res = rotateXM.multiply(rotateZM).multiply(rotateYM);
		break;
	    case CAM_ROT_SEQ_YZX:
		res = rotateYM.multiply(rotateZM).multiply(rotateXM);
		break;
	    case CAM_ROT_SEQ_ZYX:
		res = rotateZM.multiply(rotateYM).multiply(rotateXM);
		break;
	    case CAM_ROT_SEQ_ZXY:
		res = rotateZM.multiply(rotateXM).multiply(rotateYM);
		break;
	}*/
	return res;
    }
}
