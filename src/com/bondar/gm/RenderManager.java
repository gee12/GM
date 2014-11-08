package com.bondar.gm;

import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DInds;
import com.bondar.geom.Solid3D;
import com.bondar.geom.Vertex3D;
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
        int i = 0;
	for (Solid3D model : models) {
	    if (model.getState() != Solid3D.States.VISIBLE) continue;
            i++;
	    for (Polygon3DInds poly : model.getPolygons()) {
		if (poly.getState() != Polygon3D.States.VISIBLE) continue;
                // to all-sufficient polygon
		res.add(poly.toPolygon3D());
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
    public void transToPerspectAndScreen(Camera camera) {
        //TransferManager.transToPerspectAndScreen(renderArray, camera);
        for (Polygon3D poly : renderArray) {
            //poly.setVertexes(transToPerspectAndScreen(poly.getVertexes(), camera));
            int i = 1;
            for (Vertex3D v : poly.getVertexes()) {
                Point3D p = v.getPosition();
                double[] d = new double[] {p.getX(), p.getY(), p.getZ()};
                System.out.println(String.format("poly%d = [%f],[%f],[%f]",i++,p.getX(), p.getY(), p.getZ()));
            }
            poly.transToPerspectAndScreen(camera);
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
    public static Comparator<Polygon3D> compAverageZ = new Comparator<Polygon3D>() {
	@Override
	public int compare(Polygon3D poly1, Polygon3D poly2) {
            //final double averageZ1 = poly1.averageZ();
            //final double averageZ2 = poly2.averageZ();
            final double averageZ1 = (poly1.isSetAttribute(Polygon3D.ATTR_FIXED)) ? poly1.getAverageZ() : poly1.averageZ();
            final double averageZ2 = (poly2.isSetAttribute(Polygon3D.ATTR_FIXED)) ? poly2.getAverageZ() : poly2.averageZ();
	    return (averageZ1 < averageZ2) ? 1 :
		    (averageZ1 > averageZ2) ? -1 : 0;
	}
    };    
    public static Comparator<Polygon3D> compNearZ = new Comparator<Polygon3D>() {
	@Override
	public int compare(Polygon3D poly1, Polygon3D poly2) {
            final double minZ1 = poly1.minZ();
            final double minZ2 = poly2.minZ();
	    return (minZ1 < minZ2) ? 1 :
		    (minZ1 > minZ2) ? -1 : 0;
	}
    };    
    public static Comparator<Polygon3D> compFarZ = new Comparator<Polygon3D>() {
	@Override
	public int compare(Polygon3D poly1, Polygon3D poly2) {
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
}
