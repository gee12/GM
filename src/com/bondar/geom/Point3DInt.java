package com.bondar.geom;

import com.bondar.tools.Mathem;

/**
 *
 * @author Иван
 */
public class Point3DInt {
    
    protected int x, y, z;

    public Point3DInt(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point3DInt(Point3D p3D) {
        this.x = Mathem.toInt(p3D.getX());
        this.y = Mathem.toInt(p3D.getY());
        this.z = Mathem.toInt(p3D.getZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    
}
