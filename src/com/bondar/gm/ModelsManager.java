package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Solid3D;
import static com.bondar.tasks.GM.ANGLE_UP;
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
   
    private Solid3D[] models;
    
    public ModelsManager() {

    }
    
    public void load() {
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
		for (int i = 0; i < 1000; i++) {
		    Solid3D newCube = new Solid3D(model);
		    newCube.updateTransfers(rand.nextInt(500)-250, 0, rand.nextInt(500)-250);
		    loaded.add(newCube);
		}
		break;
	    }
	}
	models = Types.toArray(loaded, Solid3D.class);
    }
    
    public void updateAndAnimate(Camera camera, boolean isNeedDefineBackfaces) {
	for (Solid3D model : models) {
	    animateModel(model);
	    updateModel(model, camera, isNeedDefineBackfaces);
	}
    }
    
    /////////////////////////////////////////////////////////
    private void updateModel(Solid3D model, Camera camera, boolean isNeedDefineBackfaces) {
	if (model == null) return;

	// culling solid if need
	if (!model.isSetAttribute(Solid3D.ATTR_NO_CULL))
	    model.setIsCulled(camera);
	if (model.getState() != Solid3D.States.VISIBLE)
	    return;

        // transfer local vertexes to world
	Point3D[] transVerts = TransferManager.transToWorld(model);
        
	// redefine normals, backfaces
        model.setVertexesPosition(transVerts);
        model.redefinePolygonsParams(transVerts, camera.getPosition(), isNeedDefineBackfaces);
        
	// transferFull world vertexes to camera
	transVerts = TransferManager.transToCamera(transVerts, camera);
	
	model.setVertexesPosition(transVerts);
	//model.setTransVertexes(transVerts);
        
        
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
    
    /////////////////////////////////////////////////////////
    private void animateModel(Solid3D model) {
	if (model == null || model.isSetAttribute(Solid3D.ATTR_FIXED)) return;
	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.Y);
    }
    
    // get
    public Solid3D[] getModels() {
	return models;
    }
}
