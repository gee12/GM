package com.bondar.gm;

import com.bondar.geom.Solid3D;
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
    private static final String CUBE_TEXT = "Куб";
   
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
    
    public Solid3D[] getModels() {
	return models;
    }
}
