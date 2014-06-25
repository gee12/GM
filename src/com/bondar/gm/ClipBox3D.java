package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class ClipBox3D {
    
    // 3d clipping planes
    // if view volume is NOT 90 degree then general 3d clipping
    // must be employed
    float nearClipZ;
    float farClipZ; 
    Plane3D rtPlane;
    Plane3D ltPlane;
    Plane3D tpPlane;
    Plane3D btPlane;

    public ClipBox3D() {
	nearClipZ = farClipZ = 0;
	rtPlane = new Plane3D();
	ltPlane = new Plane3D();
	tpPlane = new Plane3D();
	btPlane = new Plane3D();
    }

    // set
    public void setNearClipZ(float nearClipZ) {
	this.nearClipZ = nearClipZ;
    }

    public void setFarClipZ(float farClipZ) {
	this.farClipZ = farClipZ;
    }

    public void setRtPlane(Point3D p, Vector3D v, boolean needNormalize) {
	rtPlane.setPlane3D(p, v, needNormalize);
    }

    public void setLtPlane(Point3D p, Vector3D v, boolean needNormalize) {
	ltPlane.setPlane3D(p, v, needNormalize);
    }

    public void setTpPlane(Point3D p, Vector3D v, boolean needNormalize) {
	tpPlane.setPlane3D(p, v, needNormalize);
    }

    public void setBtPlane(Point3D p, Vector3D v, boolean needNormalize) {
	btPlane.setPlane3D(p, v, needNormalize);
    }

    // get
    public float getNearClipZ() {
	return nearClipZ;
    }

    public float getFarClipZ() {
	return farClipZ;
    }

    public Plane3D getRtPlane() {
	return rtPlane;
    }

    public Plane3D getLtPlane() {
	return ltPlane;
    }

    public Plane3D getTpPlane() {
	return tpPlane;
    }

    public Plane3D getBtPlane() {
	return btPlane;
    }
}
