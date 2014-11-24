package com.bondar.gm;

import java.awt.image.BufferedImage;

/**
 *
 * @author Иван
 */
public class Texture {
    
    private final int id;
    private final BufferedImage image;

    public Texture(int id, BufferedImage image) {
        this.id = id;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public BufferedImage getImage() {
        return image;
    }
}
