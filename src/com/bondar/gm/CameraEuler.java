package com.bondar.gm;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

/**
 *
 * @author truebondar
 */
public class CameraEuler extends Camera{

    private double viewDist;	    // focal length 
    private double fov;		    // field of view for both horizontal and vertical axes
    private Dimension2D viewPlane;  // width and height of view plane to project onto
    private ClipBox3D clipBox;	    // 3d clipping planes
    private Dimension viewPort;	    // size of viewport (screen window)
    private Point2D viewportCenter; // center of view port (final image destination)
    private double aspectRatio;
    
    public CameraEuler(int attr, Point3D pos, Vector3D dir,
	    float nearClipZ, float farClipZ, float fov, Dimension vp) {
	super(attr, pos, dir);
	this.fov = fov;
	this.viewPort = vp;
	viewportCenter = new Point2D(viewPort.getWidth() / 2., viewPort.getHeight() / 2.);
	aspectRatio = viewPort.getWidth() / viewPort.getHeight();
	// usually 2x2 for normalized projection or 
	// the exact same size as the viewport (screen window)
	viewPlane = new Dimension();
	viewPlane.setSize(2., 2. / aspectRatio);
	
	// now we know fov and we know the viewplane dimensions plug into formula and
	// solve for view distance parameters
	viewDist = 2;
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

    public Matrix builtMatrix(int camRotSeq) {
	Matrix res = new Matrix();
	Matrix transM = Matrix.buildTransferMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
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
	return res.multiply(transM);
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
}
