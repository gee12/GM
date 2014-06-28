package com.bondar.gm;

/**
 *
 * Need to optimized:
 * 4X4 + div Z -> 3x3 ?
 * 
 * @author truebondar
 */
public class Transfer {

    /////////////////////////////////////////////////////////
    // transfer with all vertexes
    // local -> world -> camera -> perspective
    public static Point3D[] transferFull(Solid3D solid, CameraEuler cam) {
	if (solid == null) return null;
	return transferFull(solid.getLocalVertexes(), solid.getDirection(), 
		solid.getPosition(), solid.getScale(), cam, solid.isNeedPerspective());
    }
    
    public static Point3D[] transferFull(Point3D[] verts, Point3D dir, Point3D pos, Point3D scale,
	    CameraEuler cam, boolean isNeedPerspective) {
	if (verts == null || dir == null || pos == null || scale == null || cam == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.buildRotationMatrix(dir.getX(), Matrix.AXIS.X);
	Matrix rotateYM = Matrix.buildRotationMatrix(dir.getY(), Matrix.AXIS.Y);
	Matrix rotateZM = Matrix.buildRotationMatrix(dir.getZ(), Matrix.AXIS.Z);
	Matrix transM = Matrix.buildTransferMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.buildScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix perspM = Matrix.buildPerspectiveMatrix(cam.getViewDist(), cam.getAspectRatio());
	//Matrix viewM = Matrix.getViewMatrix(ro, theta, phi);
	//Matrix perspM = Matrix.getPerspectiveMatrix(dist);
	Matrix camM = cam.builtMatrix(Camera.CAM_ROT_SEQ_ZYX);
	Matrix[] ms = new Matrix[] {rotateXM,rotateYM,rotateZM,transM,scaleM,perspM,camM};
	// transform all local vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], ms);
	}
	return res;
    }

    // local -> world
     public static Point3D[] transToWorld(Solid3D solid) {
	if (solid == null) return null;
	return transToWorld(solid.getLocalVertexes(), solid.getDirection(), 
		solid.getPosition(), solid.getScale());
     }
     
     public static Point3D[] transToWorld(Point3D[] verts, Point3D dir, Point3D pos, Point3D scale) {
	if (verts == null || dir == null || pos == null || scale == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.buildRotationMatrix(dir.getX(), Matrix.AXIS.X);
	Matrix rotateYM = Matrix.buildRotationMatrix(dir.getY(), Matrix.AXIS.Y);
	Matrix rotateZM = Matrix.buildRotationMatrix(dir.getZ(), Matrix.AXIS.Z);
	Matrix transM = Matrix.buildTransferMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.buildScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix[] ms = new Matrix[] {rotateXM,rotateYM,rotateZM,transM,scaleM};
	// transform all local vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], ms);
	}
	return res;
    }

    // world -> camera
    public static Point3D[] transToCamera(Point3D[] verts, CameraEuler cam) {
	if (verts == null || cam == null)return null;
	// create camera matrix
	Matrix camM = cam.builtMatrix(Camera.CAM_ROT_SEQ_ZYX);
	// transform all world vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], camM);
	}
	return res;
    }

    // camera -> perspective
    public static Point3D[] transToPerspective(Point3D[] verts, CameraEuler cam) {
	if (verts == null || cam == null) return null;
	// create perspective matrix
	Matrix perspM = Matrix.buildPerspectiveMatrix(cam.getViewDist(), cam.getAspectRatio());
	// transform all camera vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], perspM);
	}
	return res;
    }
    
    // perspective -> screen
    public static Point3D[] transToScreen(Point3D[] verts, CameraEuler cam) {
	if (verts == null || cam == null) return null;
	// create screen matrix
	Matrix scrM = Matrix.buildScreenMatrix(cam.getViewPort().getWidth(), cam.getViewPort().getHeight());
	// transform all perspective vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], scrM);
	}
	return res;
    }

    /////////////////////////////////////////////////////////
    // transfer with 1 vertex 
    // local -> world
    public static Point3D transToWorld(Point3D vert, Point3D dir, Point3D pos, Point3D scale,
	    CameraEuler cam) {
	if (vert == null || dir == null || pos == null || scale == null || cam == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.buildRotationMatrix(dir.getX(), Matrix.AXIS.X);
	Matrix rotateYM = Matrix.buildRotationMatrix(dir.getY(), Matrix.AXIS.Y);
	Matrix rotateZM = Matrix.buildRotationMatrix(dir.getZ(), Matrix.AXIS.Z);
	Matrix transM = Matrix.buildTransferMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.buildScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix[] ms = new Matrix[] {rotateXM,rotateYM,rotateZM,transM,scaleM};
	// transform local vertex
	return transVertex(vert, ms);
    }   
    // world -> camera
    public static Point3D transToCamera(Point3D vert, CameraEuler cam) {
	if (vert == null || cam == null) return null;
	// create camera matrix
	Matrix camM = cam.builtMatrix(Camera.CAM_ROT_SEQ_ZYX);
	// transform world vertex
	return transVertex(vert, camM);
    }

    // camera -> perspective
    public static Point3D transToPerspective(Point3D vert, CameraEuler cam) {
	if (vert == null || cam == null) return null;
	// create perspective matrix
	Matrix perspM = Matrix.buildPerspectiveMatrix(cam.getViewDist(), cam.getAspectRatio());
	// transform camera vertex
	return transVertex(vert, perspM);
    }
    
    // perspective -> screen
    public static Point3D transToScreen(Point3D vert, CameraEuler cam) {
	if (vert == null || cam == null) return null;
	// create screen matrix
	Matrix scrM = Matrix.buildScreenMatrix(cam.getViewPort().getWidth(), cam.getViewPort().getHeight());
	// transform perspective vertex
	return transVertex(vert, scrM);
    }   
    
    /////////////////////////////////////////////////////////
    public static Point3D transVertex(Point3D v, Matrix m) {
	if (v == null || m == null) return null;
	Point3DOdn res = v.toPoint3DOdn();
	res.mul(m);
	return (Point3D) res.divByW();
    }

    public static Point3D transVertex(Point3D v, Matrix[] ms) {
	if (v == null || ms == null) return null;
	Point3DOdn res = v.toPoint3DOdn();
	for (Matrix m : ms) {
	    res.mul(m);
	}
	return (Point3D) res.divByW();
    }
    
  
}
