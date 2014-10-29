package com.bondar.geom;

import java.awt.Color;

/**
 * Polygon with indexes for vertexes.
 * @author truebondar
 */
public class Polygon3DInds extends Polygon3D {

    protected int indexes[];

    /////////////////////////////////////////////////////////
    public Polygon3DInds(Point3D[] verts, int[] inds, Color fill, Color border, int attr) {
        super(verts,fill,border,attr);
	this.indexes = inds;
        this.size = inds.length;
    }
    
    /////////////////////////////////////////////////////////
    // set

    /////////////////////////////////////////////////////////
    // get
    @Override
    public Point3D getVertex(int i) {
	if (vertexes == null || indexes == null
		|| (i < 0 || i >= size) || (indexes[i] >= vertexes.length)) return null;
	return vertexes[indexes[i]];
    }
    
    public int[] getIndexes() {
	return indexes;
    }
    
    @Override
    public Point3D[] getVertexes() {
	if (vertexes == null) return null;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[indexes[i]];
	}
	return res;
    }
    
    @Override
    public Polygon3DInds getCopy() {
	return new Polygon3DInds(vertexes, indexes, srcColor, borderColor, attributes);
    }
    
    // 
    public Polygon3D toPolygon3D() {
        Polygon3D res = new Polygon3D(vertexes, srcColor, borderColor, size);
        Point3D[] ps = getVertexes();
        res.setVertexes(ps);
        return res;
    }
}
