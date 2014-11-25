package com.bondar.gm;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 * @author Иван
 */
public class TextureManager {
    
    private static final String TEXTURES_DIR = "textures/";
    private static final String TEXTURES_MAP = "map.txt";

    private static HashMap<Integer, Texture> textures = new HashMap<>();
    
    /////////////////////////////////////////////////////////
    // load textures
    public static void load() {
	try {
	    textures = FileLoader.readTexturesDir(TEXTURES_DIR, TEXTURES_MAP);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    public static Texture getTexture(int id) {
        return textures.get(id);
    }
    
    public static BufferedImage getImage(int id) {
        Texture texture;
        return ((texture = textures.get(id)) != null) ? texture.getImage() : null;
    }
}
