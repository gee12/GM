package com.bondar.geom;

import com.bondar.gm.Camera;
import com.bondar.gm.Matrix;
import java.awt.Color;

/**
 *
 * @author Иван
 */
public class Polygon3DVerts extends Polygon3D {
    
    private Vertex3D[] vertexes;
    
    /////////////////////////////////////////////////////////
    public Polygon3DVerts(Vertex3D[] verts, Color src, /*Color shade,*/ Color border, int attr) {
        super(verts.length, src, /*shade,*/ border, attr);
	this.vertexes = verts;//Arrays.copyOf(verts, verts.length);
    }
    
    /////////////////////////////////////////////////////////
    public boolean isBackFace(Camera cam) {
	if (cam == null) return true;
	if (isSetAttribute(Polygon3D.ATTR_2_SIDES)
		|| type == Types.LINE
		|| type == Types.POINT) return false;
        Point3D p = cam.getPosition();
	return !isPointInHalfspace(p);
    }
    
    public boolean isPointInHalfspace(Point3D p) {
	if (p == null || type == Types.LINE
		|| type == Types.POINT) return false;
	Vector3D v = new Vector3D(vertexes[1].getPosition(), p);
	double res = Vector3D.dot(normal, v);
	return (res > 0.0); 
    }
        
    public double averageZ() {
	double sumZ = 0;
	for (Vertex3D v : vertexes) {
	    sumZ += v.getPosition().getZ();
	}
	return sumZ / size;
    }

    public double minZ() {
	double minZ = vertexes[0].getPosition().getZ();
	for (Vertex3D v : getVertexes()) {
	    if (minZ > v.getPosition().getZ())
		minZ = v.getPosition().getZ();
	}
	return minZ;
    }
 
    public double maxZ() {
	double maxZ = vertexes[0].getPosition().getZ();
	for (Vertex3D v : getVertexes()) {
	    if (maxZ < v.getPosition().getZ())
		maxZ = v.getPosition().getZ();
	}
	return maxZ;
    }

    public double zByXY(double x, double y) {
 	if (type == Types.LINE || type == Types.POINT) return 0;
        return Triangle3D.zByXY(
                vertexes[0].getPosition(), 
                vertexes[1].getPosition(), 
                vertexes[2].getPosition(), x, y);
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setVertexes(Vertex3D[] verts) {
	this.vertexes = verts;
    }
    
    public void setVertexesPosition(Point3D[] points) {
        if (points == null) return;
        for (int i = 0; i < vertexes.length; i++) {
            vertexes[i].setPosition(points[i]);
        }
    }
    
    public void setIsBackFace(Camera cam) {
	if (isBackFace(cam))
	    state = States.BACKFACE;
	else state = States.VISIBLE;
    }
        
    /////////////////////////////////////////////////////////
    // reset
    public void resetNormal() {
 	if (type == Types.LINE || type == Types.POINT) return;
        normal = normal(
                vertexes[0].getPosition(), 
                vertexes[1].getPosition(), 
                vertexes[2].getPosition());
    }
    
    public void resetAverageZ() {
        averageZ = averageZ();
    }

    /////////////////////////////////////////////////////////
    // get
    public Point3D getVertexPosition(int i) {
	if (i < 0 || i >= size) return null;
	return vertexes[i].getPosition();
    }

    public Vertex3D[] getVertexes() {
	return vertexes;
    }
    
    public Polygon3DVerts getCopy() {
	return new Polygon3DVerts(vertexes, colors[0], /*shadeColor, */borderColor, attributes);
    }
}
