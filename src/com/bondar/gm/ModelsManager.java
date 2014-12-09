package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Solid3D;
import com.bondar.tasks.Main;
import static com.bondar.tasks.Main.ANGLE_UP;
import static com.bondar.tasks.Main.GROUP_TITLE_OPERATIONS;
import static com.bondar.tasks.Main.RADIO_ROTATE;
import static com.bondar.tasks.Main.RADIO_SCALE;
import static com.bondar.tasks.Main.RADIO_TRANSFER;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author truebondar
 */
public class ModelsManager {
    
    private static final String MODELS_DIR = "models/";
    private static final String MODELS_EXTENSION = ".gmm";
    private static final double ROTATE_ANGLE = ANGLE_UP/100;
   
    private static List<Solid3D> models;
    private static int visible = 0;
    
    /////////////////////////////////////////////////////////
    // load models from .gmx file
    public static void load() {
	try {
	    models = FileLoader.readModelsDir(MODELS_DIR, MODELS_EXTENSION);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    /////////////////////////////////////////////////////////
    // clone models
    public static void clone(String modelName, int num) {
	List<Solid3D> clones = new ArrayList<>();
        Random rand = new Random();
	for (Solid3D model : models) {
	    if (model.getName().equals(modelName)) {
		for (int i = 0; i < num; i++) {
		    Solid3D clone = new Solid3D(model);
		    clone.updateTransfers(rand.nextInt(500)-250, rand.nextInt(200)-100, rand.nextInt(500)-250);
		    clone.updateAngles(Math.toRadians(rand.nextInt(360)), 
                            Math.toRadians(rand.nextInt(360)), 
                                    Math.toRadians(rand.nextInt(360)));
		    clones.add(clone);
		}
		break;
	    }
	}
	models.addAll(clones);
    }
    
    /////////////////////////////////////////////////////////
    //
    public static void updateAndAnimate(Camera camera, boolean isAnimate, boolean isDefineBackfaces) {
        visible = 0;
	for (Solid3D model : models) {
            if (isAnimate) animateModel(model);
	    updateModel(model, camera, isDefineBackfaces);
            //
            if (model.getState() == Solid3D.States.VISIBLE)
                visible++;
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
    
    /////////////////////////////////////////////////////////
    public static void onOperation(List<Solid3D> models, double angle, Matrix.AXIS axis,
	    double dx, double dy, double dz, double scale, String operationType) {
	// if selected all models
	if (models.contains(MouseManager.allModel)) {
	    models.clear();
	    //models.addAll(Types.toList(ModelsManager.getModels()));
            for (Solid3D model : ModelsManager.getModels()) {
                if (model.isSetAttribute(Solid3D.ATTR_FIXED)) continue;
                models.add(model);
            }
	}
	// select operation
	switch (operationType) {
	    case RADIO_ROTATE:
		ModelsManager.onRotate(models, angle, axis);
		break;
	    case RADIO_TRANSFER:
		ModelsManager.onTransfer(models, dx, dy, dz);
		break;
	    case RADIO_SCALE:
		ModelsManager.onScale(models, scale);
		break;
	}
    }    
    
    public static void onRotate(List<Solid3D> models, double angle, Matrix.AXIS axis) {
	for (Solid3D model : models) {
	    model.updateAngle(angle, axis);
	}
    }

    public static void onTransfer(List<Solid3D> models, double dx, double dy, double dz) {
	for (Solid3D model : models) {
	    model.updateTransfers(dx, dy, dz);
	}
    }

    public static void onScale(List<Solid3D> models, double scale) {
	for (Solid3D solid : models) {
	    solid.updateScale(scale);
	}
    }
    
    
    /////////////////////////////////////////////////////////
    private static void animateModel(Solid3D model) {
	if (model == null || model.isSetAttribute(Solid3D.ATTR_FIXED)) return;
	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.X);
	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.Y);
	model.updateAngle(ANGLE_UP/5, Matrix.AXIS.Z);
        Point3D pos = model.getPosition();
        
        model.setPosition(new Point3D(Math.cos(ROTATE_ANGLE)*pos.getX() + Math.sin(ROTATE_ANGLE)*pos.getZ(), 
                pos.getY(), 
                -Math.sin(ROTATE_ANGLE)*pos.getX() + Math.cos(ROTATE_ANGLE)*pos.getZ()));
    }
    
    // get
    public static List<Solid3D> getModels() {
	return models;
    }
    
    public static int getSize() {
        return models.size();
    }
    
    public static int getVisibleNum() {
        return visible;
    }
}
