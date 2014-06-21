package com.bondar.gm;

import java.awt.Color;

/**
 * Polygon with indexes for vertexes.
 * @author truebondar
 */
public class Polygon3D {
    
    protected int state;
    protected int attr;
    protected Point3D[] vertexes;
    protected int indexes[];
    protected int size;
    protected Color color;

    public Polygon3D(Point3D[] verts, int[] inds, Color color) {
	this.vertexes = verts;
	this.indexes = inds;
	this.size = inds.length;
	this.color = color;
    }

    public int[] getIndexes() {
	return indexes;
    }
    
    public int getSize() {
	return size;
    }
    
    public Color getColor() {
	return color;
    }
    
    public Point3D[] getVertexes() {
	if (vertexes == null) return null;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[indexes[i]];
	}
	return res;
    }
}
