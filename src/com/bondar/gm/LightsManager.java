package com.bondar.gm;

import com.bondar.geom.Vector3D;
import java.awt.Color;
import java.util.HashMap;

/**
 *
 * @author truebondar
 */
public class LightsManager {
    private HashMap<Integer, Light> lights = new HashMap<>();

    public LightsManager() {
	
    }
    
    public void load() {
	Light ambient = new Light(0, "Ambient white", Light.Types.AMBIENT, 0, Light.States.ON,
		Color.WHITE, null, null,
		null, null,
		0, 0, 0, 0, 0, 0);
	lights.put(ambient.getIndex(), ambient);
	Light infinite = new Light(0, "Infinite yellow", Light.Types.DIRECTIONAL, 0, Light.States.ON,
		Color.YELLOW, null, null,
		null, new Vector3D(0, -1, 0),
		0, 0, 0, 0, 0, 0);
	lights.put(infinite.getIndex(), infinite);   
 	Light point = new Light(0, "Point yellow", Light.Types.POINT, 0, Light.States.ON,
		Color.YELLOW, null, null,
		null, new Vector3D(0, -1, 0),
		0, 1, 0, 0, 0, 0);
	lights.put(point.getIndex(), point); 
    }
}
