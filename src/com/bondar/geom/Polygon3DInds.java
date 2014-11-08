package com.bondar.geom;

import java.awt.Color;

/**
 * Polygon with indexes for vertexes (no all-sufficient polygon).
 * Field 'vertexes' - all solid vertexes (not only polygon).
 * Field 'indexes' - array of indexes of polygon's vertexes in all solid vertexes.
 * @author truebondar
 */
public class Polygon3DInds extends Polygon3D {

    protected int indexes[];

    /////////////////////////////////////////////////////////
    public Polygon3DInds(Vertex3D[] verts, int[] inds, Color src, Color shade, Color border, int attr) {
        super(verts,src,shade,border,attr);
	this.indexes = inds;
        this.size = inds.length;
        this.type = type(size);
    }
    
    public Polygon3DInds(Point3D[] verts, int[] inds, Color src, Color shade, Color border, int attr) {
        super(verts,src,shade,border,attr);
	this.indexes = inds;
        this.size = inds.length;
        this.type = type(size);
    }
    
    /////////////////////////////////////////////////////////
    // set
    
    // normal has been updated !
    public boolean isPointInHalfspace(Point3D p) {
	if (p == null || type == Types.LINE
		|| type == Types.POINT) return false;
	Vector3D v = new Vector3D(vertexes[indexes[1]].getPosition(), p);
	double res = Vector3D.dot(normal, v);
	return (res > 0.0); 
    }
    
    public void resetNormal() {
 	if (type == Types.LINE || type == Types.POINT) return;
        normal = normal(
                vertexes[indexes[0]].getPosition(), 
                vertexes[indexes[1]].getPosition(), 
                vertexes[indexes[2]].getPosition());
    }
    /*public void setVertexesPosition(Point3D[] points) {
        if (points == null) return;
        int i = 0;
        for (Vertex3D v : vertexes) {
            v.setPosition(points[i]);
            i++;
        }
    }*/

    /////////////////////////////////////////////////////////
    // get
    @Override
    public Point3D getVertexPosition(int i) {
	if (vertexes == null || indexes == null
		|| (i < 0 || i >= size) || (indexes[i] >= vertexes.length)) return null;
	return vertexes[indexes[i]].getPosition();
    }
    
    public int[] getIndexes() {
	return indexes;
    }
    
    // Return only polygon's vertexes (not all solid vertexes)
    public Point3D[] getVertexesPositions() {
	if (vertexes == null) return null;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[indexes[i]].getPosition();
	}
	return res;
    }
    
    @Override
    public Vertex3D[] getVertexes() {
	if (vertexes == null) return null;
	Vertex3D[] res = new Vertex3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[indexes[i]];
	}
	return res;
    }
    
    @Override
    public Polygon3DInds getCopy() {
	return new Polygon3DInds(vertexes, indexes, srcColor, shadeColor, borderColor, attributes);
    }
    
    // To all-sufficient polygon
    public Polygon3D toPolygon3D() {
        return new Polygon3D(getVertexes(), srcColor, shadeColor, borderColor, attributes);
    }
}
