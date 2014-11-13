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
	Light ambient = new Light(0, "Ambient white", Light.Types.AMBIENT, 0, Light.States.ON,
		Color.BLACK, null, null,
		null, null,
		0, 0, 0, 0, 0, 0);
	//lights.put(ambient.getIndex(), ambient);
        //
	Light infinite = new Light(1, "Infinite yellow", Light.Types.INFINITE, 0, Light.States.ON,
		null, Color.GRAY, null,
		null, new Vector3D(1, 0, 0),
		0, 0, 0, 0, 0, 0);
	lights.put(infinite.getIndex(), infinite); 
        //
 	Light point = new Light(2, "Point yellow", Light.Types.POINT, 0, Light.States.ON,
		Color.YELLOW, null, null,
		null, new Vector3D(0, -1, 0),
		0, 1, 0, 0, 0, 0);
	//lights.put(point.getIndex(), point); 
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
            switch (light.getType()) {

                case AMBIENT:
                    rSum += ((light.getC_ambient().getRed() * rBase) / 256);
                    gSum += ((light.getC_ambient().getGreen() * gBase) / 256);
                    bSum += ((light.getC_ambient().getBlue() * bBase) / 256);

                    break;

                case INFINITE:
                    if (poly.getType() == Polygon3D.Types.LINE
                        || poly.getType() == Polygon3D.Types.POINT) return null;
                    
                    poly.resetNormal();
                    Vector3D n = poly.getNormal();
//                            Polygon3D.normal(poly.getVertexPosition(0), 
//                            poly.getVertexPosition(1), 
//                            poly.getVertexPosition(2));
                    double nl = n.length();
                    double dp = Vector3D.dot(n, light.getDirection());
                    if (dp > 0) {
                        double i = 128 * dp / nl;
                        rSum += ((light.getC_diffuse().getRed() * rBase * i) / (256 * 128));
                        gSum += ((light.getC_diffuse().getGreen() * gBase * i) / (256 * 128));
                        bSum += ((light.getC_diffuse().getBlue() * bBase * i) / (256 * 128));
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
        
        int rSum = 0, gSum = 0, bSum = 0;

        for (Light light : lights.values()) {
            switch (light.getType()) {

                case AMBIENT:
                    rSum += ((light.getC_ambient().getRed() * rBase) / 256);
                    gSum += ((light.getC_ambient().getGreen() * gBase) / 256);
                    bSum += ((light.getC_ambient().getBlue() * bBase) / 256);

                    break;

                case INFINITE:
                    if (poly.getType() == Polygon3D.Types.LINE
                        || poly.getType() == Polygon3D.Types.POINT) return null;
                    
                    poly.resetNormal();
                    Vector3D n = poly.getNormal();
//                            Polygon3D.normal(poly.getVertexPosition(0), 
//                            poly.getVertexPosition(1), 
//                            poly.getVertexPosition(2));
                    double nl = n.length();
                    double dp = Vector3D.dot(n, light.getDirection());
                    if (dp > 0) {
                        double i = 128 * dp / nl;
                        rSum += ((light.getC_diffuse().getRed() * rBase * i) / (256 * 128));
                        gSum += ((light.getC_diffuse().getGreen() * gBase * i) / (256 * 128));
                        bSum += ((light.getC_diffuse().getBlue() * bBase * i) / (256 * 128));
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
}
