package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Solid3D;
import com.bondar.geom.Vertex3D;
import static com.bondar.tasks.Main.ANGLE_UP;
import com.bondar.tools.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author truebondar
 */
public class ModelsManager {
    
    private static final String MODELS_DIR = "models/";
    private static final String CUBE_TEXT = "Cube";
    
    private static final int CLONE_CUBE_NUM = 0;
   
    private static Solid3D[] models;
    
    public static void load() {
	List<Solid3D> loaded = new ArrayList<>();
	// load models from .GMX files
	try {
	    loaded = FileLoader.readGMXDir(MODELS_DIR);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	// clone models
        Random rand = new Random();
	for (Solid3D model : loaded) {
	    if (model.getName().equals(CUBE_TEXT)) {
		for (int i = 0; i < CLONE_CUBE_NUM; i++) {
		    Solid3D newCube = new Solid3D(model);
		    newCube.updateTransfers(rand.nextInt(500)-250, 0, rand.nextInt(500)-250);
		    loaded.add(newCube);
		}
		break;
	    }
	}
	models = Types.toArray(loaded, Solid3D.class);
    }
    
    public static void updateAndAnimate(Camera camera, boolean isAnimate, boolean isDefineBackfaces) {
	for (Solid3D model : models) {
            if (isAnimate) animateModel(model);
	    updateModel(model, camera, isDefineBackfaces);
	}
    }
    
    /////////////////////////////////////////////////////////
    private static void updateModel(Solid3D model, Camera cam, boolean isDefineBackfaces) {
	if (model == null) return;

	// culling solid if need
	if (!model.isSetAttribute(Solid3D.ATTR_NO_CULL))
	    model.setIsCulled(cam);
	if (model.getState() != Solid3D.States.VISIBLE)
	    return;

        // transfer local vertexes to world
	Point3D[] transVerts = TransferManager.transToWorld(model);
        // transferFull world vertexes to camera
        transVerts = TransferManager.transToCamera(transVerts, cam);
        
	// redefine normals, backfaces
        model.setVertexesPosition(transVerts);
        model.redefinePolygonsParams(transVerts, cam.getPosition(), isDefineBackfaces);
        
//      model.computeVertexesNormal();
//	model.setVertexesPosition(transVerts);

        
        /*if (model.getState() == Solid3D.States.VISIBLE) {
            // if object not fixed - redefine it's fields
            //model.resetBounds();
            if (!model.isSetAttribute(Solid3D.ATTR_FIXED)) {

                for (Polygon3D poly: model.getPolygons()) {
                    //poly.resetNormal();
                    poly.resetAverageZ();
                }
            }
        }*/
    }
    
    
    public static void onRotate(List<Solid3D> models, double angle, Matrix.AXIS axis) {
	for (Solid3D model : models) {
	    //if (model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    model.updateAngle(angle, axis);
	}
    }

    public static void onTransfer(List<Solid3D> models, double dx, double dy, double dz) {
	for (Solid3D model : models) {
	    //if (model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    model.updateTransfers(dx, dy, dz);
	}
    }

    public static void onScale(List<Solid3D> models, double scale) {
	for (Solid3D solid : models) {
	    //if (solid.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
	    solid.updateScale(scale);
	}
    }
    
    
    /////////////////////////////////////////////////////////
    private static void animateModel(Solid3D model) {
	if (model == null || model.isSetAttribute(Solid3D.ATTR_FIXED)) return;
//	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.X);
	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.Y);
//	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.Z);
    }
    
    // get
    public static Solid3D[] getModels() {
	return models;
    }
}
