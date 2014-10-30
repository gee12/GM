package com.bondar.gm;

import com.bondar.geom.Solid3D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DInds;

/**
 *
 * Need to optimized:
 * 4X4 + div Z -> 3x3 ?
 * 
 * @author truebondar
 */
public class TransferManager {

    /////////////////////////////////////////////////////////
    //
    public static void transLocalToCamera(Solid3D model, Camera camera, boolean isNeedDefineBackfaces) {
	if (model == null) return;
	// transferFull local vertexes to world
	Point3D[] verts = TransferManager.transToWorld(model);
	// culling solid if need
	if (!model.isSetAttribute(Solid3D.ATTR_NO_CULL))
	    model.setIsNeedCulling(camera);
	if (model.getState() != Solid3D.States.VISIBLE)
	    return;
	// define backfaces triangles
	if (isNeedDefineBackfaces) {
	    model.reinitPoliesVertexes(verts);
	    model.defineBackfaces(camera);
	}
	// transferFull world vertexes to camera
	verts = TransferManager.transToCamera(verts, camera);
	/*// to perspective
	if (model.isNeedPerspective()) {
	    verts = TransferManager.transToPerspective(verts, camera);
	    //
	    verts = TransferManager.transPerspectToScreen(verts, camera);
	}
	else verts = TransferManager.transToScreen(verts, camera);*/
	
	model.reinitPoliesVertexes(verts);
	model.setTransVertexes(verts);
    }
    
    /*public static void transToPerspectAndScreen(Polygon3D[] polies, Camera camera, boolean isNeedPerspective) {
	if (polies == null) return;
	if (isNeedPerspective) {
	    for (Polygon3D poly: polies) {
		poly.setVertexes(transToPerspectAndScreen(poly.getVertexes(), camera));
	    }
	} else {
	    for (Polygon3D poly: polies) {
		poly.setVertexes(transToScreen(poly.getVertexes(), camera));
	    }
	}
    }*/
    public static void transToPerspectAndScreen(Polygon3DInds[] polies, Camera camera) {
	if (polies == null) return;
        for (Polygon3DInds poly : polies) {
            poly.setVertexes(transToPerspectAndScreen(poly.getVertexes(), camera));
        }
    }
    
    public static void transToPerspectAndScreen(Polygon3D[] polies, Camera camera) {
	if (polies == null) return;
        for (Polygon3D poly : polies) {
            poly.setVertexes(transToPerspectAndScreen(poly.getVertexes(), camera));
        }
    }   
    /*public static void transferFull(Solid3D model, Camera camera, boolean isNeedDefineBackfaces) {
	if (model == null) return;
	// transferFull local vertexes to world
	Point3D[] verts = TransferManager.transToWorld(model);
	// culling solid if need
	if (!model.isSetAttribute(Solid3D.ATTR_NO_CULL))
	    model.setIsNeedCulling(camera);
	if (model.getState() != Solid3D.States.VISIBLE)
	    return;
	// define backfaces triangles
	if (isNeedDefineBackfaces)
	{
	    model.reinitPoliesVertexes(verts);
	    model.defineBackfaces(camera);
	}
	// transferFull world vertexes to camera
	verts = TransferManager.transToCamera(verts, camera);
	// to perspective
	if (model.isNeedPerspective()) {
	    verts = TransferManager.transToPerspective(verts, camera);
	    //
	    verts = TransferManager.transPerspectToScreen(verts, camera);
	}
	else verts = TransferManager.transToScreen(verts, camera);
	
	if (verts == null) return;
	model.reinitPoliesVertexes(verts);
	model.setTransVertexes(verts);
    }*/
    
    /////////////////////////////////////////////////////////
    // transfer with all vertexes
    // local -> world -> camera -> perspective
    public static Point3D[] transferFull(Solid3D model, Camera cam) {
	if (model == null) return null;
	return transferFull(model.getLocalVertexes(), model.getDirection(), 
		model.getPosition(), model.getScale(), cam, model.isNeedPerspective());
    }
    
    public static Point3D[] transferFull(Point3D[] verts, Point3D dir, Point3D pos, Point3D scale,
	    Camera cam, boolean isNeedPerspective) {
	if (verts == null || dir == null || pos == null || scale == null || cam == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.rotationXMatrix(dir.getX());
	Matrix rotateYM = Matrix.rotationYMatrix(dir.getY());
	Matrix rotateZM = Matrix.rotationZMatrix(dir.getZ());
	Matrix transM = Matrix.tranlateMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.scaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix perspM = Matrix.perspectMatrix(cam.getViewDist(), cam.getAspectRatio());
	Matrix camM = cam.builtMatrix(cam.getBuildMode());
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
     public static Point3D[] transToWorld(Solid3D model) {
	if (model == null) return null;
	return transToWorld(model.getLocalVertexes(), model.getDirection(), 
		model.getPosition(), model.getScale());
     }
     
     public static Point3D[] transToWorld(Point3D[] verts, Point3D dir, Point3D pos, Point3D scale) {
	if (verts == null || dir == null || pos == null || scale == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.rotationXMatrix(dir.getX());
	Matrix rotateYM = Matrix.rotationYMatrix(dir.getY());
	Matrix rotateZM = Matrix.rotationZMatrix(dir.getZ());
	Matrix transM = Matrix.tranlateMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.scaleMatrix(scale.getX(), scale.getY(), scale.getZ());
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
    public static Point3D[] transToCamera(Point3D[] verts, Camera cam) {
	if (verts == null || cam == null) return null;
	// create camera matrix
	Matrix camM = cam.builtMatrix(cam.getBuildMode());
	// transform all world vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], camM);
	}
	return res;
    }

    // camera -> perspective
    public static Point3D[] transToPerspective(Point3D[] verts, Camera cam) {
	if (verts == null || cam == null) return null;
	// create perspective matrix
	Matrix perspM = Matrix.perspectMatrix(cam.getViewDist(), cam.getAspectRatio());
	// transform all camera vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], perspM);
	}
	return res;
    }
    
    // perspective -> screen
    public static Point3D[] transPerspectToScreen(Point3D[] verts, Camera cam) {
	if (verts == null || cam == null) return null;
	// create screen matrix
	Matrix scrM = Matrix.perspectToScreenMatrix(
		cam.getViewPort().getWidth(), cam.getViewPort().getHeight());
	// transform all perspective vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], scrM);
	}
	return res;
    }
    
    public static Point3D[] transToPerspectAndScreen(Point3D[] verts, Camera cam) {
	if (verts == null || cam == null) return null;
	// create matrixes
	Matrix perspM = Matrix.perspectMatrix(cam.getViewDist(), cam.getAspectRatio());
	Matrix scrM = Matrix.perspectToScreenMatrix(
		cam.getViewPort().getWidth(), cam.getViewPort().getHeight());
	Matrix[] ms = new Matrix[] {perspM,scrM};
	// transform all camera vertexes
	int size = verts.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = transVertex(verts[i], ms);
	}
	return res;
    }
    
    // camera -> screen
    public static Point3D[] transToScreen(Point3D[] verts, Camera cam) {
	if (verts == null || cam == null) return null;
	// create screen matrix
	Matrix scrM = Matrix.cameraToScreenMatrix(
		cam.getViewPort().getWidth(), cam.getViewPort().getHeight(), cam.getViewDist());
	// transform camera vertex
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
    public static Point3D transToWorld(Point3D vert, Point3D dir, Point3D pos, Point3D scale) {
	if (vert == null || dir == null || pos == null || scale == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.rotationXMatrix(dir.getX());
	Matrix rotateYM = Matrix.rotationYMatrix(dir.getY());
	Matrix rotateZM = Matrix.rotationZMatrix(dir.getZ());
	Matrix transM = Matrix.tranlateMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.scaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix[] ms = new Matrix[] {rotateXM,rotateYM,rotateZM,transM,scaleM};
	// transform local vertex
	return transVertex(vert, ms);
    }   
    // world -> camera
    public static Point3D transToCamera(Point3D vert, Camera cam) {
	if (vert == null || cam == null) return null;
	// create camera matrix
	Matrix camM = cam.builtMatrix(cam.getBuildMode());
	// transform world vertex
	return transVertex(vert, camM);
    }

    // camera -> perspective
    public static Point3D transToPerspective(Point3D vert, Camera cam) {
	if (vert == null || cam == null) return null;
	// create perspective matrix
	Matrix perspM = Matrix.perspectMatrix(cam.getViewDist(), cam.getAspectRatio());
	// transform camera vertex
	return transVertex(vert, perspM);
    }
    
    // perspective -> screen
    public static Point3D transPerspectToScreen(Point3D vert, Camera cam) {
	if (vert == null || cam == null) return null;
	// create screen matrix
	Matrix scrM = Matrix.perspectToScreenMatrix(
		cam.getViewPort().getWidth(), cam.getViewPort().getHeight());
	// transform perspective vertex
	return transVertex(vert, scrM);
    }   
     
    // camera -> screen
    public static Point3D transCameraToScreen(Point3D vert, Camera cam) {
	if (vert == null || cam == null) return null;
	// create screen matrix
	Matrix scrM = Matrix.cameraToScreenMatrix(
		cam.getViewPort().getWidth(), cam.getViewPort().getHeight(), cam.getViewDist());
	// transform camera vertex
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
