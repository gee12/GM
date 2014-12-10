package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DInds;
import com.bondar.geom.Solid3D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Иван
 */
public class ShadesManager {
    
    public static final float SHADE_TRANSPARENT_VALUE = 0.5f;
    public static final Color SHADE_COLOR = Color.BLACK;
    public static final double FLOOR = -10.0;
    
    private static List<Solid3D> shades = null;
    
    /////////////////////////////////////////////////////////
    //
    public static void buildShades(List<Solid3D> models) {
	shades = buildShades(models, LightManager.getLights(), CameraManager.getCam());
    }
    
    /////////////////////////////////////////////////////////
    public static List<Solid3D> buildShades(List<Solid3D> models, Collection<Light> lights, Camera cam) {
	if (models == null) return null;
        
	List<Solid3D> res = new ArrayList<>();
        
        for (Light light : lights) {
            if (light.getState() == Light.States.OFF
                    || light.getType() == Light.Types.AMBIENT
                    || light.getType() == Light.Types.INFINITE)
                continue;
            
            for (Solid3D model : models) {
                // check is culled
                if (model.getState() != Solid3D.States.VISIBLE) {
                    continue;
                }
                res.add(buildShade(model, light, cam));
            }
        }
        return res;
    }
    
    /////////////////////////////////////////////////////////
    protected static Solid3D buildShade(Solid3D model, Light light, Camera cam) {
        if (model == null) return null;
        
        Solid3D res = model.getCopy();
        
        // transfer vertexes to projection on XZ plane
//        Point3D[] localPoints = model.getLocalVertexes();
        Point3D[] worldPoints = TransferManager.transToWorld(res);
        Point3D[] shadePoints = new Point3D[worldPoints.length];
        Point3D lightPos = light.getPos();
        
        for (int i = 0; i < worldPoints.length; i++) {
            // to world coords
//            Point3D vi = localPoints[i].getCopy().add(model.getPosition());
            Point3D vi = worldPoints[i];//TransferManager.transToWorld(model);
            // 
            double t0 = (FLOOR - lightPos.getY()) / (vi.getY() - lightPos.getY());
            
            shadePoints[i] = new Point3D(
                    lightPos.getX() + t0 * (vi.getX() - lightPos.getX()),
                    FLOOR,
                    lightPos.getZ() + t0 * (vi.getZ() - lightPos.getZ()));
        }
        shadePoints = TransferManager.transToCamera(shadePoints, cam);
        res.setVertexesPosition(shadePoints);
	
        // reset parameters
        for (Polygon3DInds poly : res.getPolygons()) {
//            if (poly.getState() != Polygon3D.States.VISIBLE) {
//                continue;
//            }
            poly.setTransparent(SHADE_TRANSPARENT_VALUE);
            poly.setAttributes(Polygon3D.ATTR_FIXED | Polygon3D.ATTR_SHADE_CONST | Polygon3D.ATTR_TRANSPARENT);
            poly.setColor(SHADE_COLOR);
        }
        
        return res;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // get
    public static List<Solid3D> getShades() {
        return shades;
    }
    
    public static int getShadesNum() {
        return shades.size();
    }
    
}
