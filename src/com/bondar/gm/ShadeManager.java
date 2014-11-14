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
public class ShadeManager {
    private HashMap<Integer, Light> lights = new HashMap<>();

    public ShadeManager() {
	
    }
    
    public void load() {
        //
	Light ambient = new Light(0, "Ambient white", Light.Types.AMBIENT, 0, Light.States.OFF,
		Color.BLACK, null, null,
		null, null,
		0, 0, 0, 0, 0, 0);
	lights.put(ambient.getIndex(), ambient);
        //
	Light infinite = new Light(1, "Infinite yellow", Light.Types.INFINITE, 0, Light.States.ON,
		null, Color.GRAY, null,
		null, new Vector3D(1, 0, 0),
		0, 0, 0, 0, 0, 0);
	lights.put(infinite.getIndex(), infinite); 
        //
 	Light point = new Light(2, "Point yellow", Light.Types.POINT, 0, Light.States.OFF,
		Color.YELLOW, null, null,
		null, new Vector3D(0, -1, 0),
		0, 1, 0, 0, 0, 0);
	lights.put(point.getIndex(), point); 
    }
    
    public Polygon3DVerts[] flatShade(Polygon3DVerts[] polies) {
        if (polies == null) return null;
        for (Polygon3DVerts poly : polies) {
            flatShade(poly);
        }
        return polies;
    }
    
    public Polygon3D flatShade(Polygon3DVerts poly) {
        if (poly == null) return null;
        
        Color scrColor = poly.getColor();
        int rBase = scrColor.getRed();
        int gBase = scrColor.getGreen();
        int bBase = scrColor.getBlue();
        
        int rSum = 0, gSum = 0, bSum = 0;

        for (Light light : lights.values()) {
            if (light.getState() == Light.States.OFF) continue;
            
            switch (light.getType()) {

                case AMBIENT:
                    rSum += ((light.getC_ambient().getRed() * rBase) >> 8);
                    gSum += ((light.getC_ambient().getGreen() * gBase) >> 8);
                    bSum += ((light.getC_ambient().getBlue() * bBase) >> 8);

                    break;

                case INFINITE:
                    if (poly.getType() == Polygon3D.Types.LINE
                        || poly.getType() == Polygon3D.Types.POINT) return null;
                    
                    Vector3D n = poly.getNormal();
                    double dp = Vector3D.dot(n, light.getDirection());
                    double nl = n.length();
                    if (dp > 0) {
                        // WTF? Why need *128, and then /128. What the metter?
                        double i = /*128 **/ dp / nl;
                        rSum += ((light.getC_diffuse().getRed() * rBase * i) / (256/* * 128*/));
                        gSum += ((light.getC_diffuse().getGreen() * gBase * i) / (256/* * 128*/));
                        bSum += ((light.getC_diffuse().getBlue() * bBase * i) / (256/* * 128*/));
                    }
                    break;

                case POINT:
                    
                    break;
                    
                case DIRECTIONAL:
                    
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
        
        poly.setColor(new Color(rSum, gSum, bSum));
        return poly;
    }
    
    public Polygon3DVerts[] gouradShade(Polygon3DVerts[] polies) {
        if (polies == null) return null;
        for (Polygon3DVerts poly : polies) {
            gouradShade(poly);
        }
        return polies;
    }
    
    public Polygon3D gouradShade(Polygon3DVerts poly) {
        if (poly == null) return null;
        
        Color scrColor = poly.getColor();
        int rBase = scrColor.getRed();
        int gBase = scrColor.getGreen();
        int bBase = scrColor.getBlue();
        
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
                    if (poly.getType() == Polygon3D.Types.LINE
                        || poly.getType() == Polygon3D.Types.POINT) return null;
                    
                    // vertex 0
                    Vector3D n0 = poly.getVertexes()[0].getNormal();
                    double dp = Vector3D.dot(n0, light.getDirection());
                    if (dp > 0) {
                        double i = 128 * dp;
                        rSum0 += ((light.getC_diffuse().getRed() * rBase * i) / (256 * 128));
                        gSum0 += ((light.getC_diffuse().getGreen() * gBase * i) / (256 * 128));
                        bSum0 += ((light.getC_diffuse().getBlue() * bBase * i) / (256 * 128));
                    }
                    // vertex 1
                    Vector3D n1 = poly.getVertexes()[1].getNormal();
                    dp = Vector3D.dot(n1, light.getDirection());
                    if (dp > 0) {
                        double i = 128 * dp;
                        rSum1 += ((light.getC_diffuse().getRed() * rBase * i) / (256 * 128));
                        gSum1 += ((light.getC_diffuse().getGreen() * gBase * i) / (256 * 128));
                        bSum1 += ((light.getC_diffuse().getBlue() * bBase * i) / (256 * 128));
                    }
                    // vertex 2
                    Vector3D n2 = poly.getVertexes()[2].getNormal();
                    dp = Vector3D.dot(n2, light.getDirection());
                    if (dp > 0) {
                        double i = 128 * dp;
                        rSum2 += ((light.getC_diffuse().getRed() * rBase * i) / (256 * 128));
                        gSum2 += ((light.getC_diffuse().getGreen() * gBase * i) / (256 * 128));
                        bSum2 += ((light.getC_diffuse().getBlue() * bBase * i) / (256 * 128));
                    }
                    break;

                case POINT:
                    
                    break;
                    
                case DIRECTIONAL:
                    
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
        
        poly.setColor(new Color(rSum0, gSum0, bSum0), 0);
        poly.setColor(new Color(rSum1, gSum1, bSum1), 1);
        poly.setColor(new Color(rSum2, gSum2, bSum2), 2);
        return poly;
    }
}
