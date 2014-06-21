package com.bondar.gm;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

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
    int state;      // state of camera
    int attr;       // camera attributes
    Point3D pos;    // world position of camera used by both camera models
    Vector3D dir;   // angles or look at direction of camera for simple 
    // euler camera models, elevation and heading for
    // uvn model
    Vector3D u;     // extra vectors to track the camera orientation
    Vector3D v;     // for more complex UVN camera model
    Vector3D n;
    Point3D target; // look at target
    double viewDist;  // focal length 
    double fov;          // field of view for both horizontal and vertical axes
    Dimension2D viewPlane;
    //float viewplaneWidth;     // width and height of view plane to project onto
    //float viewplaneHeight;    // usually 2x2 for normalized projection or 
    // the exact same size as the viewport or screen window
    ClipBox clipBox;
    // remember screen and viewport are synonomous
    Dimension viewPort;
    //float viewport_width;     // size of screen/viewport
    //float viewport_height;
    Point2D viewportCenter;
    //float viewport_center_x;  // center of view port (final image destination)
    //float viewport_center_y;
    // aspect ratio
    double aspectRatio;
    // these matrices are not necessarily needed based on the method of
    // transformation, for example, a manual perspective or screen transform
    // and or a concatenated perspective/screen, however, having these 
    // matrices give us more flexibility         
    //MATRIX4X4 mcam;   // storage for the world to camera transform matrix
    //MATRIX4X4 mper;   // storage for the camera to perspective transform matrix
    //MATRIX4X4 mscr;   // storage for the perspective to screen transform matrix

    public Camera(int attr, Point3D pos, Vector3D dir,
	    float nearClipZ, float farClipZ, float fov, Dimension viewPort) {
	this.attr = attr;
	this.pos = pos;
	this.dir = dir;
	this.fov = fov;
	this.viewPort = viewPort;
	viewportCenter = new Point2D(viewPort.getWidth() / 2, viewPort.getHeight() / 2);
	aspectRatio = viewPort.getWidth() / viewPort.getHeight();
	viewPlane = new Dimension();
	viewPlane.setSize(2., 2. / aspectRatio);
	clipBox = new ClipBox();
	clipBox.setNearClipZ(nearClipZ);
	clipBox.setFarClipZ(farClipZ);
	Point3D origin = new Point3DOdn(0, 0, 0);
	clipBox.setRtPlane(origin, new Vector3D(1, 0, -1), true);
	clipBox.setLtPlane(origin, new Vector3D(-1, 0, -1), true);
	clipBox.setTpPlane(origin, new Vector3D(0, 1, -1), true);
	clipBox.setBtPlane(origin, new Vector3D(0, -1, -1), true);
    }

    public Matrix builtMatrix(int camRotSeq) {
	Matrix res = new Matrix();
	Matrix transM = Matrix.getTransferMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
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
	}
	return res.multiply(transM);
    }
}
