package com.bondar.geom;

import static com.bondar.geom.Polygon3D.ATTR_2_SIDES;
import java.awt.Color;

/**
 * Polygon with indexes for vertexes (no all-sufficient polygon).
 * @author truebondar
 */
public class Polygon3DInds extends Polygon3D {

    protected int indexes[];    // indexes of polygon's vertexes in all solid vertexes.

    /////////////////////////////////////////////////////////
    public Polygon3DInds(int[] inds, float transp, int attr, Color srcColor) {
        super(inds.length, transp, attr, srcColor);
	this.indexes = inds;
    }
    
    public Polygon3DInds(int[] inds, float transp, int attr, int textId, Point2D[] textPoints) {
        super(inds.length, transp, attr, textId, textPoints);
        this.indexes = inds;
    }

    /////////////////////////////////////////////////////////
    public boolean isBackFace(Point3D[] points, Point3D camPos) {
	if (isSetAttribute(ATTR_2_SIDES)) return false;
	return !isPointInHalfspace(points, camPos);
    }
    
    // normal has been updated !
    public boolean isPointInHalfspace(Point3D[] points, Point3D p) {
	if (points == null || p == null || size < 3) return false;
	Vector3D v = new Vector3D(points[indexes[0]], p);
	double res = Vector3D.dot(normal, v);
	return (res > 0.0); 
    }

    public double averageZ(Vertex3D[] verts) {
	if (verts == null) return 0;
        double sumZ = 0;
	for (Vertex3D v : verts) {
	    sumZ += v.getPosition().getZ();
	}
	return sumZ / size;
    }

    public double minZ(Vertex3D[] verts) {
        if (verts == null) return 0;
	double minZ = verts[0].getPosition().getZ();
	for (Vertex3D v : verts) {
	    if (minZ > v.getPosition().getZ())
		minZ = v.getPosition().getZ();
	}
	return minZ;
    }
 
    public double maxZ(Vertex3D[] verts) {
        if (verts == null) return 0;
	double maxZ = verts[0].getPosition().getZ();
	for (Vertex3D v : verts) {
	    if (maxZ < v.getPosition().getZ())
		maxZ = v.getPosition().getZ();
	}
	return maxZ;
    }

    public double zByXY(Vertex3D[] verts, double x, double y) {
 	if (verts == null || size < 3) return 0;
        return Triangle3D.zByXY(
                verts[0].getPosition(), 
                verts[1].getPosition(), 
                verts[2].getPosition(), x, y);
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setIsBackFace(Point3D[] points, Point3D camPos) {
	state = (isBackFace(points, camPos)) ? States.BACKFACE : States.VISIBLE;
    }
    
    /////////////////////////////////////////////////////////
    // reset
    public void resetNormal(Point3D[] points) {
 	if (size < 3) return;
        normal = normal(
                points[indexes[0]], 
                points[indexes[1]], 
                points[indexes[2]]);
    }

    /////////////////////////////////////////////////////////
    // get
    public Vertex3D[] getVertexes(Vertex3D[] allVerts) {
        if (allVerts == null) return null;
        Vertex3D[] res = new Vertex3D[size];
        for (int i = 0; i < size; i++) {
            res[i] = allVerts[indexes[i]].getCopy();
        }
        return res;
    }
    
    public Point3D getVertexPosition(Vertex3D[] verts, int i) {
	if (verts == null
		|| (i < 0 || i >= size) || (indexes[i] >= verts.length)) return null;
	return verts[indexes[i]].getPosition();
    }
    
    public int[] getIndexes() {
	return indexes;
    }
    
    // Return only polygon's vertexes (not all solid vertexes)
    public Point3D[] getVertexesPositions(Vertex3D[] verts) {
	if (verts == null) return null;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = verts[indexes[i]].getPosition();
	}
	return res;
    }
    
    public Polygon3DInds getCopy() {
        if (isSetAttribute(ATTR_TEXTURED))
            return new Polygon3DInds(indexes, transp, attributes, textureId, texturePoints);
        else return new Polygon3DInds(indexes, transp, attributes, colors[0]);
    }
    
    // To all-sufficient polygon
    public Polygon3DVerts toPolygon3DVerts(Vertex3D[] verts) {
        Polygon3DVerts res;
        if (isSetAttribute(ATTR_TEXTURED))
            res = new Polygon3DVerts(getVertexes(verts), transp, attributes, textureId, texturePoints);
        else res = new Polygon3DVerts(getVertexes(verts), transp, attributes, colors[0]);
        res.normal = normal;
        res.averageZ = averageZ;
        res.normalLength = normalLength;
        res.materialId = materialId;
        res.state = state;
        res.type = type;
        return res;
    }
}