package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DInds;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Solid3D;
import com.bondar.geom.Vector3D;
import com.bondar.geom.Vertex3D;
import com.bondar.tasks.Main;
import com.bondar.tools.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class RenderManager {
    
    public static enum SortByZTypes {
	AVERAGE_Z,
	NEAR_Z,
	FAR_Z
    }
    
    private static Polygon3DVerts[] renderArray = null;
    
    /////////////////////////////////////////////////////////
    public static void load() {
      	LightManager.load();
    }

    /////////////////////////////////////////////////////////
    //
    public static void buildRenderArray(List<Solid3D> models) {
	renderArray = toRenderArray(models);
    }
    
    public static Polygon3DVerts[] toRenderArray(List<Solid3D> models) {
	if (models == null) return null;
	List<Polygon3DVerts> res = new ArrayList<>();
	for (Solid3D model : models) {
            // check is culled
	    if (model.getState() != Solid3D.States.VISIBLE) continue;
            
	    for (Polygon3DInds poly : model.getPolygons()) {
                // check is backface
		if (poly.getState() != Polygon3D.States.VISIBLE) continue;
                // to all-sufficient polygon
		res.add(poly.toPolygon3DVerts(model.getVertexes()));
	    }
	}
	return Types.toArray(res, Polygon3DVerts.class);
    }
    
    /////////////////////////////////////////////////////////
    public static void update(Camera cam, String depthType, String shadingType, boolean isNormalsPoly, boolean isNormalsVert) {
	// NEED TO OPTIMATE !
        // cull, shade & transfer in one for()
        
        
        // cull polygons
        onCulling(cam);
        // shade
        onShading(shadingType);
        //
        if (depthType.equals(Main.RADIO_PAINTER)) {
            sortByZ(RenderManager.SortByZTypes.AVERAGE_Z);
        }
        transToPerspectAndScreen(cam);
        
        // transfer nolmals for drawing
                
        //
        if (isNormalsPoly) {
            for (Polygon3DVerts poly : renderArray) {
                if (poly.getSize() < 3) continue;
                Point3D n = poly.getNormal();
                // !!! transfer to camera for the SECOND TIME !
//                n = TransferManager.transToCamera(n, cam);
                
                n = TransferManager.transToPerspectAndScreen(n, cam);
//                n = TransferManager.transToPerspective(n, cam);
//                n = TransferManager.transPerspectToScreen(n, cam);
                Vector3D v =  n.toVector3D();
//                v = v.normalize();
                poly.setNormal(v);
            }
        }
        //
        if (isNormalsVert) {
            for (Polygon3DVerts poly : renderArray) {
                for (Vertex3D vert : poly.getVertexes()) {
                    Point3D nv = vert.getNormal();
                    // !!! transfer to camera for the SECOND TIME !
//                    nv = TransferManager.transToCamera(nv, cam);
                    
                    nv = TransferManager.transToPerspectAndScreen(nv, cam);
                    vert.setNormal(nv.toVector3D());
                }
            }
        }
        
    }
    
    /////////////////////////////////////////////////////////
    private static void onCulling(Camera cam) {
        for (Polygon3DVerts poly : renderArray) {
            poly.setIsCulled(cam);
        }
    }
     
    /////////////////////////////////////////////////////////
    private static void onShading(String shadingType) {
        switch(shadingType) {
            
            case Main.RADIO_SHADE_CONST:
                break;
            case Main.RADIO_SHADE_FLAT:
                LightManager.flatShade(renderArray);
                break;
            case Main.RADIO_SHADE_GOURAD:
                LightManager.gouradShade(renderArray);
                break;
            case Main.RADIO_SHADE_FONG:
                
                break;
        }
    }
       
    /////////////////////////////////////////////////////////
    // Need to optimized: sort poly indexes, but not polies!
    //
    public static void sortByZ(SortByZTypes sortType) {
	sortByZ(renderArray, sortType);
    }
    
    public static void sortByZ(Polygon3DVerts[] polies, SortByZTypes sortType) {
	switch (sortType) {
	    case AVERAGE_Z:
		Arrays.sort(polies, compAverageZ);
		break;
	    case NEAR_Z:
		Arrays.sort(polies, compNearZ);
		break;
	    case FAR_Z:
		Arrays.sort(polies, compFarZ);
		break;
	}
    }
    
    /////////////////////////////////////////////////////////
    // 
    public static void transToPerspectAndScreen(Camera cam) {
        TransferManager.transToPerspectAndScreen(renderArray, cam);
    }
    
    /////////////////////////////////////////////////////////
    // set
    public static void setRenderArray(Polygon3DVerts[] polies) {
	renderArray = polies;
    }
    
    /////////////////////////////////////////////////////////
    // get
    public static Polygon3DVerts[] getRenderArray() {
	return renderArray;
    }
    
    /////////////////////////////////////////////////////////
    // Sort polygons by average Z coordinate
    public static Polygon3DVerts[] sortPolygonsByZ(Polygon3DVerts[] polies) {
	if (polies == null) {
	    return null;
	}
	int size = polies.length;
	double[] dists = new double[size];
	int[] indexes = new int[size];
	// нахождение средней величины Z - удаленности грани
	for (int i = 0; i < size; i++) {
	    Polygon3DVerts poly = polies[i];
	    dists[i] = poly.averageZ();
	    indexes[i] = i;
	}
	Polygon3DVerts[] res = new Polygon3DVerts[size];
	// сортировка граней по удаленности
	for (int i = 0; i < size - 1; i++) {
	    for (int j = 0; j < size - 1; j++) {
		if (dists[j] < dists[j + 1]) {
		    double distTemp = dists[j];
		    dists[j] = dists[j + 1];
		    dists[j + 1] = distTemp;

		    int indTemp = indexes[j];
		    indexes[j] = indexes[j + 1];
		    indexes[j + 1] = indTemp;
		}
	    }
	}
	for (int i = 0; i < size; i++) {
	    res[i] = polies[indexes[i]].getCopy();
	}
	return res;
    }
    
    //
    public static Comparator<Polygon3DVerts> compAverageZ = new Comparator<Polygon3DVerts>() {
	@Override
	public int compare(Polygon3DVerts poly1, Polygon3DVerts poly2) {
            //final double averageZ1 = poly1.averageZ();
            //final double averageZ2 = poly2.averageZ();
//            final double averageZ1 = (poly1.isSetAttribute(Polygon3DVerts.ATTR_FIXED)) ? poly1.getAverageZ() : poly1.averageZ();
//            final double averageZ2 = (poly2.isSetAttribute(Polygon3DVerts.ATTR_FIXED)) ? poly2.getAverageZ() : poly2.averageZ();
            final double averageZ1 = poly1.averageZ();
            final double averageZ2 = poly2.averageZ();
	    return (averageZ1 < averageZ2) ? 1 :
		    (averageZ1 > averageZ2) ? -1 : 0;
	}
    };    
    public static Comparator<Polygon3DVerts> compNearZ = new Comparator<Polygon3DVerts>() {
	@Override
	public int compare(Polygon3DVerts poly1, Polygon3DVerts poly2) {
            final double minZ1 = poly1.minZ();
            final double minZ2 = poly2.minZ();
	    return (minZ1 < minZ2) ? 1 :
		    (minZ1 > minZ2) ? -1 : 0;
	}
    };    
    public static Comparator<Polygon3DVerts> compFarZ = new Comparator<Polygon3DVerts>() {
	@Override
	public int compare(Polygon3DVerts poly1, Polygon3DVerts poly2) {
            final double maxZ1 = poly1.maxZ();
            final double maxZ2 = poly2.maxZ();
	    return (maxZ1 < maxZ2) ? 1 :
		    (maxZ1 > maxZ2) ? -1 : 0;
	}
    };
    
    /////////////////////////////////////////////////////////
    // 
    public Comparator<Integer> compInds = new Comparator<Integer>() {
	@Override
	public int compare(Integer i1, Integer i2) {
            final double maxZ1 = renderArray[i1].averageZ();
            final double maxZ2 = renderArray[i2].averageZ();
	    return (maxZ1 < maxZ2) ? 1 :
		    (maxZ1 > maxZ2) ? -1 : 0;
	}
    };    
    
    public static int getSize() {
        return renderArray.length;
    }
}
