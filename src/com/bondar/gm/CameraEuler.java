package com.bondar.gm;

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
    
    private double fov;		    // field of view for both horizontal and vertical axes
    private Dimension2D viewPlane;  // width and height of view plane to project onto
    private ClipBox3D clipBox;	    // 3d clipping planes
    private Point2D viewportCenter; // center of view port (final image destination)
    
    public CameraEuler(int attr, Point3D pos, Vector3D dir,
	    double nearClipZ, double farClipZ, double dist, double fov, Dimension vp) {
	super(attr, pos, dir, dist, vp);
	this.fov = fov;
	viewportCenter = new Point2D(viewPort.getWidth() / 2., viewPort.getHeight() / 2.);
	// usually 2x2 for normalized projection or 
	// the exact same size as the viewport (screen window)
	viewPlane = new Dimension();
	viewPlane.setSize(2., 2. / aspectRatio);
	
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

    @Override
    public Matrix builtMatrix(int camRotSeq) {
	Matrix res = new Matrix();
	Matrix invM = Matrix.buildTransferMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
	Matrix rotateXM = Matrix.buildRotationMatrix(-dir.getX(), Matrix.AXIS.X);
	Matrix rotateYM = Matrix.buildRotationMatrix(-dir.getY(), Matrix.AXIS.Y);
	Matrix rotateZM = Matrix.buildRotationMatrix(-dir.getZ(), Matrix.AXIS.Z);

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
	}
	return res.multiply(invM);
    }

    // get
    public ClipBox3D getClipBox() {
	return clipBox;
    }
    
    public Dimension2D getViewPlane() {
	return viewPlane;
    }
    
    public Dimension2D getViewPort() {
	return viewPort;
    }
}
