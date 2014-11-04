/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bondar.geom;

import java.awt.Color;

/**
 *
 * @author Иван
 */
public class Polygon3DExt extends Polygon3D {

    protected Color[] shadeVertsColors;
    //protected Bitmap texture;
    protected int materialId;
    protected Vector3D normal;
    protected double normalLength;
    
    public Polygon3DExt(Point3D[] verts, Color fill, Color border, int attr) {
        super(verts, fill, border, attr);
    }
    
}
