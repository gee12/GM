package com.bondar.gm;

import com.bondar.geom.Vector3D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Point3DOdn;
import java.awt.Dimension;

/**
 *
 * @author truebondar
 */
public class CameraUVN extends Camera{

    public static final int UVN_MODE_SIMPLE = 0;
    public static final int UVN_MODE_SPHERICAL = 1;
    
    private Vector3D rightU;		// vectors to track the camera orientation
    private Vector3D upV;
    private Vector3D lookN;
    
    public CameraUVN(int attr, Point3DOdn pos, Vector3D dir, double nearClipZ, double farClipZ, 
	    double dist, double fov, Dimension vp, Point3D target, int mode) {
	super(attr, pos, dir, nearClipZ, farClipZ, dist, fov, vp, target, mode);
	this.rightU = new Vector3D(1, 0, 0);
	this.upV = new Vector3D(0, 1, 0);
	this.lookN = new Vector3D(0, 0, 1);
    }

    @Override
    public Matrix builtMatrix(int mode) {
	Matrix invM = Matrix.tranlateMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
	if (mode == UVN_MODE_SPHERICAL) {
	    double phi = dir.getX();	// elevation
	    double theta = dir.getY();	// heading
	    target = new Vector3D(
		    -1 * Math.sin(phi) * Math.sin(theta), 
		    Math.cos(phi), 
		    Math.sin(phi) * Math.cos(theta));
	}
	// Step 1: n = <target position - view reference point>
	lookN = new Vector3D(pos, target);
	lookN.normalize();
	// Step 2: Let v = <0,1,0>
	upV = new Vector3D(0, 1, 0);
	// Step 3: u = (v x n)
	rightU = upV.mul(lookN);
	rightU.normalize();
	// Step 4: v = (n x u)
	upV = lookN.mul(rightU);
	upV.normalize();
	Matrix uvnM = Matrix.UVNMatrix(rightU, upV, lookN);
	return invM.multiply(uvnM);
        
        
        /*Matrix invM = Matrix.tranlateMatrix(-pos.getX(), -pos.getY(), -pos.getZ());
        
        Matrix uvnM = Matrix.UVNMatrix(rightU, upV, lookN);
        return invM.multiply(uvnM);*/
    }
    
    /*
        private void calc_cam_axes()
        {
            _cam_dir.X = (float)Math.Cos(radFromDeg(_cam_pitch)) * (float)Math.Cos(radFromDeg(_cam_yaw));
            _cam_dir.Y = (float)Math.Cos(radFromDeg(_cam_pitch)) * (float)Math.Sin(radFromDeg(_cam_yaw));
            _cam_dir.Z = (float)Math.Sin(radFromDeg(_cam_pitch));

            _cam_right.X = (float)Math.Cos(radFromDeg(_cam_yaw - 90));
            _cam_right.Y = (float)Math.Sin(radFromDeg(_cam_yaw - 90));
            _cam_right.Z = 0;

            _cam_up = Vector3.Cross(_cam_right, _cam_dir);
        }
    */
    /*
    double camPitch = 0.0;
    double camYaw = 0.0;
    
    public void buildAxes() {
        /*lookN.normalize();
        upV = lookN.mul(rightU);
        upV.normalize();
        rightU = upV.mul(lookN);
        rightU.normalize(); */
        /*lookN = new Vector3D(
                Math.cos(camPitch)*Math.cos(camYaw),
                Math.cos(camPitch)*Math.sin(camYaw),
                Math.sin(camPitch));
        rightU = new Vector3D(
                Math.cos(camYaw),
                Math.sin(camYaw),
                0);
        upV = rightU.mul(lookN);
    }
    
    public Vector3D rotate(Vector3D point_on_axis, Vector3D dir, double a, Vector3D point) {
        Vector3D r = point.sub(point_on_axis).toVector3D();
        dir.normalize();
        return (point_on_axis.
                add(r.mul(Math.cos(a))).
                add(dir.mul(dir.dot(r)*(1-Math.cos(a))).
                add(dir.mul(r).mul(Math.sin(a)))).toVector3D());
    }*/
    /*
        private Vector3 rotate(Vector3 point_on_axis, Vector3 dir, float angle, Vector3 point) {
            Vector3 r = point - point_on_axis;
            dir.Normalize();
            float a = angle * (float)(Math.PI / 180.0f);
            return point_on_axis + 
                (float)Math.Cos(a) * r + 
                ((1 - (float)Math.Cos(a)) * Vector3.Dot(dir, r)) * dir + 
                (float)Math.Sin(a) * Vector3.Cross(dir, r);
        ИЛИ
        static Vector3 rotate(Vector3 point_on_axis, Vector3 axis_dir, float angle, Vector3 point) {
            return point_on_axis + Vector3.TransformCoordinate(point - point_on_axis, Matrix.RotationAxis(axis_dir, rad_from_deg(-angle)));
        }
    */
    /*@Override
    public void updateDirection(double angle, Matrix.AXIS axis) {
        if (angle == 0.0) return;
	switch (axis) {
	    case X: pitch(angle);
		break;
	    case Y: yaw(angle);
		break;
	    case Z: roll(angle);
		break;
	}
        buildAxes();
    }*/
    
    /*
        public void arcRotate(int move_x, int move_y) {
            if (move_x != 0) {
                float yaw_diff = move_x;
                _cam_pos = rotate(_arc_rotate_center, new Vector3(0, 0, 1), yaw_diff, _cam_pos);
                _cam_yaw += yaw_diff;
                calc_cam_axes();
            }
            if (move_y != 0) {
                float pitch_diff = -move_y;
                _cam_pos = rotate(_arc_rotate_center, _cam_right, pitch_diff, _cam_pos);
                _cam_pitch += pitch_diff;
                calc_cam_axes();
            }
        }
    */
    
    
        /*Vector3D _arc_rotate_center = new Vector3D(0, 0, 0);
    // around right
    public void pitch(double angle) {
        /*Matrix rotation = Matrix.rotationXMatrix(angle);
        upV = upV.mul(rotation);
        lookN = lookN.mul(rotation);*/
        /*pos = rotate(_arc_rotate_center, rightU, -angle, pos.toVector3D());
        camPitch -= angle;
    }
    // around up
    public void yaw(double angle) {
        /*Matrix rotation = Matrix.rotationYMatrix(angle);
        rightU = rightU.mul(rotation);
        lookN = lookN.mul(rotation);*/
        /*pos = rotate(_arc_rotate_center, new Vector3D(0, 0, 1), angle, pos.toVector3D());
        camYaw += angle;
    }
    // around look
    public void roll(double angle) {
        /*Matrix rotation = Matrix.rotationZMatrix(angle);
        rightU = rightU.mul(rotation);
        upV = upV.mul(rotation);*/
    /*}*/
}
