package com.bondar.geom;

import com.bondar.gm.Camera;
import com.bondar.gm.Matrix;
import com.bondar.gm.TransferManager;
import static com.bondar.gm.Matrix.AXIS.X;
import static com.bondar.gm.Matrix.AXIS.Y;
import static com.bondar.gm.Matrix.AXIS.Z;

/**
 *
 * @author truebondar
 */
public class Solid3D {
    
    final double HALF_SCREEN_X = 0.5;
    final double HALF_SCREEN_Y = 0.5;
    
    public static enum States {
	VISIBLE,
	CULLED
    }
    
    public static final int ATTR_FIXED = 1;
    public static final int ATTR_NO_CULL = 2;
    
    private States state;
    private int attributes;
    private String name;
    private Point3D[] localVerts;
    //private Point3D[] transVerts;
    private Vertex3D[] vertexes;
    private Polygon3DInds[] polygons;
    private BoundingSphere3D bounds;
    
    private Point3D dir;
    private Point3D pos;
    private Point3D scale;

    /////////////////////////////////////////////////////////
    public Solid3D(Solid3D solid) {
	this(solid.getName(), solid.getAttributes(), solid.getLocalVertexes(), solid.getPolygons());
    }

    public Solid3D(String name, int attribs, Point3D[] verts, Polygon3DInds[] polies) {
	this.name = name;
	this.attributes = attribs;
	this.localVerts = verts;//getVertexesCopy(verts);
	this.polygons = getPolygonsCopy(polies); //Arrays.copyOf(polies, polies.length); - don't work (copy links)
	reInit();
    }
    public Solid3D(String name) {
	this.name = name;
	this.attributes = 0;
	this.localVerts = null;
	this.polygons = null;
	reInit();
    }
    /*
    public Solid3D(String name, int attribs, Point3D[] vertexes, int[][] indsToTrias, Color[] fills, Color[] borders, int attr) {
	this.name = name;
	this.attributes = attribs;
	this.localVerts = vertexes;
	this.polygons = buildPolygons(vertexes, indsToTrias, fills, borders, attr);
	reInit();
    }

    public Solid3D(String name, Point3D[] vertexes) {
	this.name = name;
	this.attributes = 0;
	this.localVerts = vertexes;
	this.polygons = buildPolygons(vertexes, buildIndexes(vertexes));
	reInit();
    }
    */
    private void reInit() {
	state = States.VISIBLE;
	//transVerts = localVerts;
	dir = new Point3D();
	pos = new Point3D();
	scale = new Point3D(1,1,1);
	bounds = new BoundingSphere3D(localVerts);
        vertexes = buildVertexes(localVerts);
    }

    /////////////////////////////////////////////////////////
    //
    public static Vertex3D[] buildVertexes(Point3D[] points) {
        if (points == null) return null;
        int size = points.length;
        Vertex3D[] res = new Vertex3D[size];
        for (int i = 0; i < size; i++) {
            res[i] = new Vertex3D(points[i]);
        }
        return res;
    }
    
    /*/////////////////////////////////////////////////////////
    // triangulation
    public static int[][] buildIndexes(Point3D[] vertexes) {
	if (vertexes == null) return null;
	int n = vertexes.length;
	// combination from n to 3
	int triasCount = n*(n-1)*(n-2)/6;
	int[][] res = new int[triasCount][3];
	// simple triangulation (with trias inside polygon & intersecting trias)
	int ind = 0;
	for (int i = 0; i < n-2; i++) {
	    for (int j = i+1; j < n-1; j++) {
		for (int k = j+1; k < n; k++,ind++) {
		    res[ind] = new int[] {i,j,k};
		}
	    }
	}
	return res;
    }
   
    private static Polygon3DInds[] buildPolygons(Point3D[] vertexes, int[][] indsToTrias) {
	if (indsToTrias == null) return null;
	int size = indsToTrias.length;
	Color[] fills = new Color[size];
	Color[] borders = new Color[size];
	Random rand = new Random();
	for (int i = 0; i < size; i++) {
	    fills[i] = new Color(
		    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
	    borders[i] = new Color(
		    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
	}
	return buildPolygons(vertexes, indsToTrias, fills, borders, 0);
    }
    
    private static Polygon3DInds[] buildPolygons(Point3D[] vertexes, int[][] inds, Color[] fills, Color[] borders, int attr) {
	if (vertexes == null || inds == null || fills == null || borders == null) return null;
	int size = inds.length;
	Polygon3DInds[] res = new Polygon3DInds[size];
	for (int i = 0; i < size; i++) {
	    res[i] = new Polygon3DInds(
		    vertexes,
		    inds[i],
		    fills[i],
		    borders[i],
		    attr);
	}
	return res;
    }*/

    /////////////////////////////////////////////////////////
    // operations
    public void updateAngle(double a, Matrix.AXIS axis) {
	double ax = 0, ay = 0, az = 0;
	switch (axis) {
	    case X: ax = a;
		break;
	    case Y: ay = a;
		break;
	    case Z: az = a;
		break;
	}
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updateAngles(double ax, double ay, double az) {
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updateTransfers(double dx, double dy, double dz) {
	pos.add(new Point3D(dx, dy, dz));
    }
    
    public void updateScale(double s) {
	scale.add(new Point3D(s, s, s));
    }
    
    public void updateScale(double sx, double sy, double sz) {
	scale.add(new Point3D(sx, sy, sz));
    }

    
    /////////////////////////////////////////////////////////
    // Cull solid, if it's fully out of clip bounds.
    public boolean isCulled(Camera cam) {
	if (cam == null) return false;
        // transfer object position (from world to camera coord's)
	Point3D spherePos = TransferManager.transToCamera(pos, cam);
        double maxRadius = bounds.getMaxRadius();
	// by z
	if (((spherePos.getZ() - maxRadius) > cam.getClipBox().getFarClipZ()) ||    // far side
	    ((spherePos.getZ() + maxRadius) < cam.getClipBox().getNearClipZ())) {   // near side
	    return true;
	}
	// by x
	double zTest = HALF_SCREEN_X * cam.getViewPlane().getWidth() * spherePos.getZ() / cam.getViewDist();
	if (((spherePos.getX() - maxRadius) > zTest)  || // right side
	    ((spherePos.getX() + maxRadius) < -zTest) ) { // left side, note sign change
	    return true;
	}
	// by y
	zTest = HALF_SCREEN_Y * cam.getViewPlane().getHeight()* spherePos.getZ() / (cam.getViewDist() * cam.getAspectRatio());
	if (((spherePos.getY() - maxRadius) > zTest)  || // down side
	    ((spherePos.getY() + maxRadius) < -zTest) ) { // up side, note sign change
	    return true;
	}
	return false;
    }
    
    /////////////////////////////////////////////////////////
    // Define backfaces triangles.
    public void defineBackfaces(Point3D[] points, Point3D camPos) {
	for (Polygon3DInds poly: polygons) {
	    poly.setIsBackFace(points, camPos);
	}
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setVertexesPosition(Point3D[] points) {
        if (points == null) return;
        for (int i = 0; i < vertexes.length; i++) {
            vertexes[i].setPosition(points[i]);
        }
    }
    
    public final void setAttributes(int attr) {
	attributes |= attr;
    }

    public final void unsetAttribute(int attr) {
	attributes &= ~attr;
    }   
    
    public void setIsCulled(Camera cam) {
	if (isCulled(cam))
	    state = States.CULLED;
	else state = States.VISIBLE;
    }
    
    // scale.getX() - need to correct
    public boolean isCameraPointInto(Point2D cp, Camera cam) {
        return bounds.isCameraPointInto(cp, cam, pos, scale.getX());
    }
    
    /////////////////////////////////////////////////////////
    // 
    // Polygons normals need to be updated
    public void computeVertexesNormal() {
        int[] touchVertex = new int[vertexes.length];
        for (Polygon3DInds poly : polygons) {
            // if (shadeMode != ShadeModes.Gourad)
            if (poly.getType() == Polygon3D.Types.POINT
                    || poly.getType() == Polygon3D.Types.LINE) return;
            
            int i0 = poly.getIndexes()[0];
            int i1 = poly.getIndexes()[1];
            int i2 = poly.getIndexes()[2];
            
            touchVertex[i0]++;
            touchVertex[i1]++;
            touchVertex[i2]++;
            
            // poly.getNormal() - need to be updated
            Vector3D n = poly.getNormal();
            vertexes[i0].getNormal().add(n);
            vertexes[i1].getNormal().add(n);
            vertexes[i2].getNormal().add(n);
        }
        
        for (int i = 0; i < vertexes.length; i++) {
            if (touchVertex[i] >= 1) {
                double inv = 1. / touchVertex[i];
                vertexes[i].getNormal().mul(inv);
                vertexes[i].getNormal().normalize();
            }
        }
    }
    
    /////////////////////////////////////////////////////////
    // reset
    // points - in camera coord's
    public void redefinePolygonsParams(Point3D[] points, Point3D camPos, boolean isNeedDefineBackfaces) {
        int[] touchVertex = new int[vertexes.length];
        
	for (Polygon3DInds poly: polygons) {
	    //poly.setVertexesPosition(points);
            poly.resetNormal(points);
            //poly.resetAverageZ(points);
            if (isNeedDefineBackfaces && poly.getSize() >= 3)
                poly.setIsBackFace(points, camPos);
            // restore backfaces if don't need to rejection
            else poly.setState(Polygon3D.States.VISIBLE);
            

            // compute vertexes normal
            if (/*poly.getShadeMode() != ShadeModes.Gourad &&*/ poly.getSize() >= 3) {
                int i0 = poly.getIndexes()[0];
                int i1 = poly.getIndexes()[1];
                int i2 = poly.getIndexes()[2];

                touchVertex[i0]++;
                touchVertex[i1]++;
                touchVertex[i2]++;

                // poly.getNormal() - need to be updated
                Vector3D n = poly.getNormal();
                vertexes[i0].getNormal().add(n);
                vertexes[i1].getNormal().add(n);
                vertexes[i2].getNormal().add(n);
            }
	}
        
        // for Gourad shading
        for (int i = 0; i < vertexes.length; i++) {
            if (touchVertex[i] >= 1) {
                double inv = 1. / touchVertex[i];
                vertexes[i].getNormal().mul(inv);
                vertexes[i].getNormal().normalize();
            }
        }
    }
    
    public void resetPoliesNormals(Point3D[] points) {
	for (Polygon3DInds poly : polygons) {
	    poly.resetNormal(points);
	}
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setPosition(Point3D pos) {
        this.pos = pos;
    }
     
    /////////////////////////////////////////////////////////
    // get
    public int getSize() {
	return localVerts.length;
    }

    public Point3D getLocalVertex(int i) {
	if (i >= localVerts.length || i < 0)
	    return null;
	return localVerts[i];
    }
    
    public Vertex3D[] getVertexes() {
        return vertexes;
    }
    
    public Point3D[] getLocalVertexes() {
	return localVerts;
    }
    
    public static Point2D[] getVertexes2D(Point3D[] vertexes) {
	int size = vertexes.length;
	Point2D[] res = new Point2D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[i].toPoint2D();
	}
	return res;
    }

    public Polygon3DInds[] getPolygons() {
	return polygons;
    }
    
    public String getName() {
	return name;
    }
    
    public int getAttributes() {
	return attributes;
    }
    
    public boolean isSetAttribute(int attr) {
	return (attributes & attr) != 0;
    }   
    
    public BoundingSphere3D getBounds() {
	return bounds;
    }

    /*public boolean isIntersect(BoundingBox3D bb) {
	return bounds.isIntersect(bb);
    }*/
    
    public States getState() {
	return state;
    }
    
    public Point3D getDirection() {
	return dir;
    }

    public Point3D getPosition() {
	return pos;
    }

    public Point3D getScale() {
	return scale;
    }
     
    public static Point3D[] getPointsCopy(Point3D[] points) {
	int size = points.length;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = points[i].getCopy();
	}
	return res;
    }
    
    public static Polygon3DInds[] getPolygonsCopy(Polygon3DInds[] polies) {
	int size = polies.length;
	Polygon3DInds[] res = new Polygon3DInds[size];
	for (int i = 0; i < size; i++) {
	    res[i] = polies[i].getCopy();
	}
	return res;
    }
        
    public Solid3D getCopy() {
	return new Solid3D(name, attributes, getPointsCopy(localVerts), getPolygonsCopy(polygons));
    }
}
