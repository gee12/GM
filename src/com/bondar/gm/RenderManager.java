package com.bondar.gm;

import com.bondar.geom.Polygon3D;
import com.bondar.geom.Solid3D;
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
    
    private Polygon3D[] renderArray = null;

    /////////////////////////////////////////////////////////
    //
    public void buildRenderArray(Solid3D[] models) {
	renderArray = toRenderArray(models);
    }
    
    public static Polygon3D[] toRenderArray(Solid3D[] models) {
	if (models == null) return null;
	List<Polygon3D> res = new ArrayList<>();
	for (Solid3D model : models) {
	    if (model.getState() != Solid3D.States.VISIBLE) continue;
	    for (Polygon3D poly : model.getPolygons()) {
		if (poly.getState() != Polygon3D.States.VISIBLE) continue;
		res.add(poly);
	    }
	    //polies.addAll(Types.toList(model.getPolygons()));
	}
	return Types.toArray(res, Polygon3D.class);
    }
    
    /////////////////////////////////////////////////////////
    // Need to optimized: sort poly indexes, but not polies!
    //
    public void sortByZ(SortByZTypes sortType) {
	sortByZ(renderArray, sortType);
    }
    
    public static void sortByZ(Polygon3D[] polies, SortByZTypes sortType) {
	if (polies == null) return;
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
    // set
    public void setRenderArray(Polygon3D[] renderArray) {
	this.renderArray = renderArray;
    }
    
    /////////////////////////////////////////////////////////
    // get
    public Polygon3D[] getRenderArray() {
	return renderArray;
    }
    
    /////////////////////////////////////////////////////////
    //
    public static Comparator<Polygon3D> compAverageZ= new Comparator<Polygon3D>() {
	@Override
	public int compare(Polygon3D poly1, Polygon3D poly2) {
	    return (poly1.averageZ() > poly2.averageZ()) ? 1 :
		    (poly1.averageZ() < poly2.averageZ()) ? -1 : 0;
	}
    };    
    public static Comparator<Polygon3D> compNearZ = new Comparator<Polygon3D>() {
	@Override
	public int compare(Polygon3D poly1, Polygon3D poly2) {
	    return (poly1.minZ() > poly2.minZ()) ? 1 :
		    (poly1.minZ() < poly2.minZ()) ? -1 : 0;
	}
    };    
    public static Comparator<Polygon3D> compFarZ = new Comparator<Polygon3D>() {
	@Override
	public int compare(Polygon3D poly1, Polygon3D poly2) {
	    return (poly1.maxZ() > poly2.maxZ()) ? 1 :
		    (poly1.maxZ() < poly2.maxZ()) ? -1 : 0;
	}
    };    
}
