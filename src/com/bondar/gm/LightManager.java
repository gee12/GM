package com.bondar.gm;

import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Vector3D;
import java.awt.Color;
import java.util.HashMap;

/**
 *
 * @author truebondar
 */
public class LightManager {
    
    private static final String TEXTURES_DIR = "lights/";
    private static final String LIGHTS_EXTENSION = ".gml";

    private static HashMap<Integer, Light> lights = new HashMap<>();
    private static int activeLights;

    ////////////////////////////////////////////////////////
    public static void load() {
	try {
	    lights = FileLoader.readLightsDir(TEXTURES_DIR, LIGHTS_EXTENSION);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
        // 
        activeLights = 0;
        for (Light light : lights.values()) {
            if (light.getState() == Light.States.ON) {
                activeLights++;
            }
        }
    }
    
    ////////////////////////////////////////////////////////
    public static void flatShade(Polygon3DVerts[] polies) {
        if (polies == null) return;
        for (Polygon3DVerts poly : polies) {
            flatShade(poly);
        }
    }
    
    public static Polygon3D flatShade(Polygon3DVerts poly) {
        if (poly == null) return null;
        
        Color scrColor = poly.getColor();
        int rBase = scrColor.getRed();
        int gBase = scrColor.getGreen();
        int bBase = scrColor.getBlue();
        
        int rSum = 0, gSum = 0, bSum = 0;

        for (Light light : lights.values()) {
            if (light.getState() == Light.States.OFF) continue;
            
            Vector3D n = poly.getNormal();
            double nLength = n.length();
            double dp = 0;
            
            switch (light.getType()) {

                case AMBIENT:
                    rSum += ((light.getC_ambient().getRed() * rBase) >> 8);
                    gSum += ((light.getC_ambient().getGreen() * gBase) >> 8);
                    bSum += ((light.getC_ambient().getBlue() * bBase) >> 8);

                    break;

                case INFINITE:
                    if (poly.getSize() < 3) return null;
                    
                    dp = Vector3D.dot(n, light.getTransDirection());
                    if (dp > 0) {
                        // WTF? Why need *128, and then /128. What the metter?
                        double i = /*128 **/ dp / nLength;
                        rSum += ((light.getC_diffuse().getRed() * rBase * i) / (256/* * 128*/));
                        gSum += ((light.getC_diffuse().getGreen() * gBase * i) / (256/* * 128*/));
                        bSum += ((light.getC_diffuse().getBlue() * bBase * i) / (256/* * 128*/));
                    }
                    break;

                case POINT:
                    Vector3D l = new Vector3D(poly.getVertexPosition(0), light.getTransPos());
                    double dist = l.length();
                    dp = Vector3D.dot(n, l);
                    if (dp > 0) {
                        double atten = (light.getKc() + light.getKl() * dist + light.getKq() * dist * dist);
                        double i = /*128 **/ dp / (nLength * dist * atten ); 
                        rSum += ((light.getC_diffuse().getRed() * rBase * i) / (256/* * 128*/));
                        gSum += ((light.getC_diffuse().getGreen() * gBase * i) / (256/* * 128*/));
                        bSum += ((light.getC_diffuse().getBlue() * bBase * i) / (256/* * 128*/));
                    }
                    break;
                    
                case SPOTLIGHT1:
                    
                    break;
                    
                case SPOTLIGHT2:
                    
                    break;
            }
        }
        // make sure colors aren't out of range
        if (rSum > 255) rSum = 255;
        if (gSum > 255) gSum = 255;
        if (bSum > 255) bSum = 255;
        
        poly.setColor(rSum, gSum, bSum);
        return poly;
    }
    
    ////////////////////////////////////////////////////////
    public static void gouradShade(Polygon3DVerts[] polies) {
        if (polies == null) return;
        for (Polygon3DVerts poly : polies) {
            gouradShade(poly);
        }
    }
    
    public static Polygon3D gouradShade(Polygon3DVerts poly) {
        if (poly == null || poly.getSize() < 3) return null;
        
        Color scrColor = poly.getColor();
        int rBase = scrColor.getRed();
        int gBase = scrColor.getGreen();
        int bBase = scrColor.getBlue();
        
        Vector3D n0 = poly.getVertexes()[0].getNormal();
        Vector3D n1 = poly.getVertexes()[1].getNormal();
        Vector3D n2 = poly.getVertexes()[2].getNormal();

        int rSum0 = 0, gSum0 = 0, bSum0 = 0,
                rSum1 = 0, gSum1 = 0, bSum1 = 0,
                rSum2 = 0, gSum2 = 0, bSum2 = 0;

        for (Light light : lights.values()) {
            if (light.getState() == Light.States.OFF) continue;

            switch (light.getType()) {

                case AMBIENT:
                    // >>8 = /256
                    int ri = ((light.getC_ambient().getRed() * rBase) >> 8);
                    int gi = ((light.getC_ambient().getGreen() * gBase) >> 8);
                    int bi = ((light.getC_ambient().getBlue() * bBase) >> 8);
            
                    // ambient light has the same affect on each vertex
                    rSum0 += ri;
                    gSum0 += gi;
                    bSum0 += bi;

                    rSum1 += ri;
                    gSum1 += gi;
                    bSum1 += bi;

                    rSum2 += ri;
                    gSum2 += gi;
                    bSum2 += bi;
                    break;

                case INFINITE:
                    // vertex 0
                    double dp = Vector3D.dot(n0, light.getTransDirection());
                    if (dp > 0) {
                        double i = /*128 **/ dp;
                        rSum0 += ((light.getC_diffuse().getRed() * rBase * i) / (256 /** 128*/));
                        gSum0 += ((light.getC_diffuse().getGreen() * gBase * i) / (256 /** 128*/));
                        bSum0 += ((light.getC_diffuse().getBlue() * bBase * i) / (256 /** 128*/));
                    }
                    // vertex 1
                    dp = Vector3D.dot(n1, light.getTransDirection());
                    if (dp > 0) {
                        double i = /*128 **/ dp;
                        rSum1 += ((light.getC_diffuse().getRed() * rBase * i) / (256 /** 128*/));
                        gSum1 += ((light.getC_diffuse().getGreen() * gBase * i) / (256 /** 128*/));
                        bSum1 += ((light.getC_diffuse().getBlue() * bBase * i) / (256 /** 128*/));
                    }
                    // vertex 2
                    dp = Vector3D.dot(n2, light.getTransDirection());
                    if (dp > 0) {
                        double i = /*128 **/ dp;
                        rSum2 += ((light.getC_diffuse().getRed() * rBase * i) / (256 /** 128*/));
                        gSum2 += ((light.getC_diffuse().getGreen() * gBase * i) / (256 /** 128*/));
                        bSum2 += ((light.getC_diffuse().getBlue() * bBase * i) / (256 /** 128*/));
                    }
                    break;

                case POINT:
                    Vector3D l = new Vector3D(poly.getVertexPosition(0), light.getTransPos());
                    double dist = l.length();
                    double atten = (light.getKc() + light.getKl() * dist + light.getKq() * dist * dist);
                    // vertex 0
                    dp = Vector3D.dot(n0, l);
                    if (dp > 0) {
                        double i = /*128 **/ dp / (dist * atten); 
                        rSum0 += ((light.getC_diffuse().getRed() * rBase * i) / (256/* * 128*/));
                        gSum0 += ((light.getC_diffuse().getGreen() * gBase * i) / (256/* * 128*/));
                        bSum0 += ((light.getC_diffuse().getBlue() * bBase * i) / (256/* * 128*/));
                    }
                    // vertex 1
                    dp = Vector3D.dot(n1, l);
                    if (dp > 0) {
                        double i = /*128 **/ dp / (dist * atten); 
                        rSum1 += ((light.getC_diffuse().getRed() * rBase * i) / (256/* * 128*/));
                        gSum1 += ((light.getC_diffuse().getGreen() * gBase * i) / (256/* * 128*/));
                        bSum1 += ((light.getC_diffuse().getBlue() * bBase * i) / (256/* * 128*/));
                    }
                    // vertex 2
                    dp = Vector3D.dot(n2, l);
                    if (dp > 0) {
                        double i = /*128 **/ dp / (dist * atten); 
                        rSum2 += ((light.getC_diffuse().getRed() * rBase * i) / (256/* * 128*/));
                        gSum2 += ((light.getC_diffuse().getGreen() * gBase * i) / (256/* * 128*/));
                        bSum2 += ((light.getC_diffuse().getBlue() * bBase * i) / (256/* * 128*/));
                    }
                    break;
                    
                case SPOTLIGHT1:
                    
                    break;
                    
                case SPOTLIGHT2:
                    
                    break;
            }
        }
        
        // make sure colors aren't out of range
        if (rSum0 > 255) rSum0 = 255;
        if (gSum0 > 255) gSum0 = 255;
        if (bSum0 > 255) bSum0 = 255;
        
        if (rSum1 > 255) rSum1 = 255;
        if (gSum1 > 255) gSum1 = 255;
        if (bSum1 > 255) bSum1 = 255;
        
        if (rSum2 > 255) rSum2 = 255;
        if (gSum2 > 255) gSum2 = 255;
        if (bSum2 > 255) bSum2 = 255;
        
        poly.setColor(rSum0, gSum0, bSum0, 0);
        poly.setColor(rSum1, gSum1, bSum1, 1);
        poly.setColor(rSum2, gSum2, bSum2, 2);
        return poly;
    }
    
    
    ////////////////////////////////////////////////////////
    public static void individualShade(Polygon3DVerts[] polies) {
        if (polies == null) return;
        for (Polygon3DVerts poly : polies) {
            if (poly.isSetAttribute(Polygon3D.ATTR_SHADE_GOURAD))
                gouradShade(poly);
            else if (poly.isSetAttribute(Polygon3D.ATTR_SHADE_FLAT))
                flatShade(poly);
        }
    }
    
    ////////////////////////////////////////////////////////
    public static void transLights(Camera cam) {
        for (Light light : lights.values()) {
            light.setTransPos(TransferManager.transToCamera(light.getPos(), cam));
            light.setTransDir(TransferManager.transToCamera(light.getDirection(), cam).toVector3D());
        }
    }
        
    ////////////////////////////////////////////////////////
    public static Color light(Color src, Color light) {
        if (src == null || light == null) return null;
        
        int r = ((light.getRed() * src.getRed()) >> 8);
        int g = ((light.getGreen() * src.getGreen()) >> 8);
        int b = ((light.getBlue() * src.getBlue()) >> 8);
        
        return new Color(r, g, b, src.getAlpha());
    }
    
    ////////////////////////////////////////////////////////
    // get
    public static HashMap<Integer, Light> getLights() {
        return lights;
    }
    
    public static int getSize() {
        return lights.size();
    }
    
    public static int getActiveLightsNum() {
        return activeLights;
    }
}
