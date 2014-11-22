package com.bondar.gm;

import com.bondar.geom.ClipRectangle2D;
import com.bondar.geom.Point2D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Solid2D;
import com.bondar.geom.Vertex3D;
import com.bondar.tasks.Main;
import com.bondar.tools.Mathem;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 *
 * @author truebondar
 */
public class DrawManager {
    
    public static enum RasterizerModes {
        ACCURATE,
        FAST,
        FASTEST
    }

    private Graphics graphic;
    private Graphics2D imageGraphic;
    private int width, height;
    private ClipRectangle2D clip;
    private RasterizerModes rasterMode;
    private BufferedImage image;
    private WritableRaster raster;
    private Color curColor;
    private Color backColor;
    
    //////////////////////////////////////////////////
    public DrawManager(int width, int height) {
        this.width = width;
	this.height = height;
        
        setClipBox(new ClipRectangle2D(10,10,width-10,height-10));

        rasterMode = RasterizerModes.ACCURATE;
        image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        imageGraphic = image.createGraphics();
        raster = image.getRaster();
        curColor = Color.BLACK;
        final int TONE = 10;
        backColor = new Color(TONE,TONE,TONE);
    }
    
    public void drawScene(Polygon3DVerts[] polies, String viewType, String shadeType, Solid2D crosshair) {
        //
        fillRectangle(new Rectangle(0,0,
                width,height/2), Color.DARK_GRAY);
        fillRectangle(new Rectangle(0,height/2,
                width,height/2), backColor);
        //
	switch (viewType) {
	    case Main.RADIO_FACES_TEXT:
		drawPolies(polies, shadeType);
		break;
	    case Main.RADIO_EDGES_TEXT:
		drawBorders(polies);
		break;
	    case Main.RADIO_EDGES_FACES_TEXT:
		drawBorderedPolies(polies, shadeType);
		break;
	}
        //
        setColor(crosshair.getColor());
        Point2D[] points = crosshair.getPoints();
        drawLine(points[0], points[1]);
        drawLine(points[2], points[3]);
        //
        graphic.drawImage(image, 0,0,width, height, null);
    }

    /////////////////////////////////////////////////////
    // DRAW RECTANGLES
    public void drawBackground() {
        fillScreen(backColor);
    }
    
    public void fillScreen(Color col) {
        fillRectangle(new Rectangle(0, 0, image.getWidth(), image.getHeight()), col);
    }
    
    public void fillRectangle(Rectangle rect, Color col) {
        imageGraphic.setPaint(col);
        imageGraphic.fillRect(rect.x, rect.y, rect.x+rect.width, rect.x+rect.height);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DRAW POINTS
        
    public void drawPoint(Point3D p) {
	if (p == null) return;
        drawPoint(p.getX(), p.getY(), curColor);
    }

    public void drawPoint(Point3D p, Color col) {
	if (p == null) return;
	drawPoint(p.getX(), p.getY(), col);
    }

    public void drawPoint(double x, double y, Color col) {
        setPixel(Mathem.toInt(x), Mathem.toInt(y), col);
    }
   
    public void drawNoClipPoint(int x, int y, Color col) {
        if (col == null) return;
	image.setRGB(x, y, col.getRGB());	    
    }    
    public void drawPoint(int x, int y, Color col, boolean isNeedClip) {
        if (isNeedClip) setPixel(x, y, col);
        else drawNoClipPoint(x, y, col);
    }    

    public void setPixel(int x, int y, Color col) {
        if (col == null || x < clip.getXMin() || x > clip.getXMax()
                || y < clip.getYMin() || y > clip.getYMax()) return;
	image.setRGB(x, y, col.getRGB());	    
    }    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DRAW LINES
    
    public void drawLine(Point3D p1, Point3D p2) {
	if (p1 == null || p2 == null) return;
	drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public void drawLine(Point3D p1, Point3D p2, Color col) {
	if (p1 == null || p2 == null) return;
	drawClipLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), col);
    }
        
    public void drawLine(Point2D p1, Point2D p2) {
	if (p1 == null || p2 == null) return;
	drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }  
    
    public void drawLine(double x1, double y1, double x2, double y2) {
        drawClipLine(x1, y1, x2, y2, curColor);
    }
    
    public void drawClipLine(double x1, double y1, double x2, double y2, Color col) {
        int[] line = clip.clipLine(Mathem.toInt(x1), Mathem.toInt(y1), 
                Mathem.toInt(x2), Mathem.toInt(y2));
        if (line != null) {
            drawLine(line[0], line[1], line[2], line[3], col, false);
        }
    }
    
    public void drawNoClipLine(double x1, double y1, double x2, double y2, Color col) {
        drawLine(Mathem.toInt(x1), Mathem.toInt(y1), 
                Mathem.toInt(x2), Mathem.toInt(y2), col, false);
    }
    
    public void drawLineSimple(int x1, int y1, int x2, int y2, Color col) {
        if (x1 > x2) {
            x1 = Mathem.returnFirst(x2, x2 = x1);
            y1 = Mathem.returnFirst(y2, y2 = y1);
        }
        int dx = x2 - x1;
        int dy = y2 - y1;
        for (int x = x1; x < x2; x++) {
            int y = y1 + dy * (x - x1) / dx;
            setPixel(x, y, col);
        }
    }
    
    
    /////////////////////////////////////////////////////
    // Draw a line from xo,yo to x1,y1 using differential error terms
    // (based on Bresenahams work)
    private void drawLine(int x0, int y0, int x1, int y1, Color col, boolean isNeedClip) {
        int x_inc, y_inc,   // amount in pixel space to move during drawing
                error;      // the discriminant i.e. error i.e. decision variable
        // compute horizontal and vertical deltas
        int dx = x1 - x0;
        int dy = y1 - y0;
        // test which direction the line is going in i.e. slope angle
        if (dx >= 0) {
            x_inc = 1;
        }
        else {
            x_inc = -1;
            dx = -dx;  // absolute value
        }
        // test y component of slope
        if (dy >= 0) {
            y_inc = 1;
        }
        else {
            y_inc = -1;
            dy = -dy;  // absolute value
        }
        // compute (dx,dy) * 2
        int dx2 = dx << 1;
        int dy2 = dy << 1;
        // start pixels
        int x = x0;
        int y = y0;
        
        // if |slope| <= 1
        if (dx > dy) {
            // initialize error term
            error = dy2 - dx;
            // draw the line
            for (int index = 0; index <= dx; index++) {
                // set the pixel
                 drawPoint(x, y, col, isNeedClip);
                // test if error has overflowed
                if (error >= 0) {
                    error -= dx2;
                    // move to next line
                    y += y_inc;
                }
                // adjust the error term
                error += dy2;
                // move to the next pixel
                x += x_inc;
            }
        }
        // if |slope| > 1
        else {
            error = dx2 - dy;
            // draw the line
            for (int index = 0; index <= dy; index++) {
                // set the pixel
                drawPoint(x, y, col, isNeedClip);
                if (error >= 0) {
                    error -= dy2;
                    x += x_inc;
                }
                error += dx2;
                y += y_inc;
            }
        }
    }
    

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DRAW TRIANGLES
    
    /////////////////////////////////////////////////////
    // Draw a triangle
    // by means of decomposes triangle into a pair of flat top, flat bottom.
    public void drawTriangle2D(double x1, double y1, double x2, double y2,
            double x3, double y3, Color col) {

        // test for h lines and v lines
        if ((Mathem.isEquals5(x1, x2) && Mathem.isEquals5(x2, x3))
                || (Mathem.isEquals5(y1, y2) && Mathem.isEquals5(y2, y3)))
            return;

        // sort p1,p2,p3 in ascending y order
        if (y2 < y1) {
            x1 = Mathem.returnFirst(x2, x2 = x1);
            y1 = Mathem.returnFirst(y2, y2 = y1);
        }
        // now we know that p1 and p2 are in order
        if (y3 < y1) {
            x1 = Mathem.returnFirst(x3, x3 = x1);
            y1 = Mathem.returnFirst(y3, y3 = y1);
        }
        // finally test y3 against y2
        if (y3 < y2) {
            x2 = Mathem.returnFirst(x3, x3 = x2);
            y2 = Mathem.returnFirst(y3, y3 = y2);
        }

        // do trivial rejection tests for clipping
        if ( y3 < clip.getYMin() || y1 > clip.getYMax() ||
             (x1 < clip.getXMin() && x2 < clip.getXMin() && x3 < clip.getXMin()) ||
             (x1 > clip.getXMax() && x2 > clip.getXMax() && x3 > clip.getXMax()) )
             return;
        
        // test if top of triangle is flat
        if (Mathem.isEquals5(y1, y2)) {
            drawTopTringle2D(x1, y1, x2, y2, x3, y3, col);
        } else if (Mathem.isEquals5(y2, y3)) {
            drawBottomTriangle2D(x1, y1, x2, y2, x3, y3, col);
        } else {
            // general triangle that's needs to be broken up along long edge
            double new_x = x1 + (y2 - y1) * (x3 - x1) / (y3 - y1);
            // draw each sub-triangle
            drawBottomTriangle2D(x1, y1, new_x, y2, x2, y2, col);
            drawTopTringle2D(x2, y2, new_x, y2, x3, y3, col);
        }
    }
    
    /////////////////////////////////////////////////////
    // Draw a triangle that has a flat bottom
    public void drawBottomTriangle2D(double x1, double y1, double x2, double y2, 
                         double x3, double y3, Color col) {
        int iy1 = 0, iy3 = 0;
        // test order of x1 and x2
        if (x3 < x2) {
            x2 = Mathem.returnFirst(x3, x3 = x2);
        }
        // compute delta's
        double h = y3 - y1;    // the height of the triangle
        double dx_left = (x2 - x1) / h;  // the dx/dy ratio of the left edge of line
        double dx_right = (x3 - x1) / h;  // the dx/dy ratio of the right edge of line
        // set starting points
        // the starting and ending points of the edges
        double xs = x1;
        double xe = x1;

        if (rasterMode == RasterizerModes.ACCURATE) {
            // perform y clipping
            //if top is off screen
            if (y1 < clip.getYMin()) {
                // compute new xs and ys
                xs = xs + dx_left * (-y1 + clip.getYMin());
                xe = xe + dx_right * (-y1 + clip.getYMin());
                // reset y1
                y1 = clip.getYMin();
                // make sure top left fill convention is observed
                //iy1 = y1;
                iy1 = (int) y1;
            } else {
                // make sure top left fill convention is observed
                iy1 = Mathem.ceil(y1);
                // bump xs and xe appropriately
                xs = xs + dx_left * (iy1 - y1);
                xe = xe + dx_right * (iy1 - y1);
            }
            if (y3 > clip.getYMax()) {
                // clip y
                y3 = clip.getYMax();
                // make sure top left fill convention is observed
                //iy3 = y3-1;
                iy3 = (int) (y3 - 1);
            } else {
                // make sure top left fill convention is observed
                iy3 = Mathem.ceil(y3) - 1;
            }
        }
        else if (rasterMode == RasterizerModes.FAST || rasterMode == RasterizerModes.FASTEST) {
        // perform y clipping
        // if top is off screen
            if (y1 < clip.getYMin()) {
                // compute new xs and ys
                xs = xs + dx_left * (-y1 + clip.getYMin());
                xe = xe + dx_right * (-y1 + clip.getYMin());
                // reset y1
                y1 = clip.getYMin();
            }
            if (y3 > clip.getYMax()) {
                y3 = clip.getYMax();
            }
            // make sure top left fill convention is observed
            iy1 = Mathem.ceil(y1);
            iy3 = Mathem.ceil(y3) - 1;
        }
        // test if x clipping is needed
        if (x1 >= clip.getXMin() && x1 <= clip.getXMax()
                && x2 >= clip.getXMin() && x2 <= clip.getXMax()
                && x3 >= clip.getXMin() && x3 <= clip.getXMax()) {
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++) {
                // draw the line
                drawNoClipLine(xs, loop_y, xe, loop_y, col);
                // adjust starting point and ending point
                xs += dx_left;
                xe += dx_right;
            }
        }
        else {
            // clip x axis with slower version
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++) {
                // do x clip
                double left = xs;
                double right = xe;
                // adjust starting point and ending point
                xs += dx_left;
                xe += dx_right;
                // clip line
                if (left < clip.getXMin()) {
                    left = clip.getXMin();
                    if (right < clip.getXMin()) {
                        continue;
                    }
                }
                if (right > clip.getXMax()) {
                    right = clip.getXMax();
                    if (left > clip.getXMax()) {
                        continue;
                    }
                }
                // draw the line
                drawNoClipLine(left, loop_y, right, loop_y, col);
            }
        }
    }
    
    
    /////////////////////////////////////////////////////
    // this function draws a triangle that has a flat top
    public void drawTopTringle2D(double x1,double y1, double x2,double y2,
            double x3,double y3, Color col)
    {
        int iy1 = 0, iy3 = 0;

        // test order of x1 and x2
        if (x2 < x1) {
            x1 = Mathem.returnFirst(x2, x2 = x1);
        }
        // compute delta's
        double h = y3 - y1; // the height of the triangle

        double dx_left = (x3 - x1) / h;   // the dx/dy ratio of the right edge of line
        double dx_right = (x3 - x2) / h;   // the dx/dy ratio of the left edge of line

        // set the starting and ending points of the edges
        double xs = x1;
        double xe = x2;

        if (rasterMode == RasterizerModes.ACCURATE) {
        // perform y clipping
            //if top is off screen
            if (y1 < clip.getYMin()) {
                // compute new xs and ys
                xs = xs + dx_left * (-y1 + clip.getYMin());
                xe = xe + dx_right * (-y1 + clip.getYMin());
                // reset y1
                y1 = clip.getYMin();
            // make sure top left fill convention is observed
                //iy1 = y1;
                iy1 = (int) y1;
            } else {
                // make sure top left fill convention is observed
                iy1 = Mathem.ceil(y1);

                // bump xs and xe appropriately
                xs = xs + dx_left * (iy1 - y1);
                xe = xe + dx_right * (iy1 - y1);
            }
            if (y3 > clip.getYMax()) {
                // clip y
                y3 = clip.getYMax();
            // make sure top left fill convention is observed
                //iy3 = y3-1;
                iy3 = (int) (y3 - 1);
            } else {
                // make sure top left fill convention is observed
                iy3 = Mathem.ceil(y3) - 1;
            }
        }
        if (rasterMode == RasterizerModes.FAST || rasterMode == RasterizerModes.FASTEST) {
            // perform y clipping
            // if top is off screen
            if (y1 < clip.getYMin()) {
                // compute new xs and ys
                xs = xs + dx_left * (-y1 + clip.getYMin());
                xe = xe + dx_right * (-y1 + clip.getYMin());
                // reset y1
                y1 = clip.getYMin();
            }
            if (y3 > clip.getYMax()) {
                y3 = clip.getYMax();
            }
            // make sure top left fill convention is observed
            iy1 = Mathem.ceil(y1);
            iy3 = Mathem.ceil(y3) - 1;
        }
        // test if no x clipping is needed
        if (x1 >= clip.getXMin() && x1 <= clip.getXMax()
                && x2 >= clip.getXMin() && x2 <= clip.getXMax()
                && x3 >= clip.getXMin() && x3 <= clip.getXMax()) {
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++) {
                // draw the line
                drawNoClipLine(xs, loop_y, xe, loop_y, col);
                // adjust starting point and ending point
                xs += dx_left;
                xe += dx_right;
            }
        } else {
            // clip x axis with slower version
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++) {
                // do x clip
                double left = xs;
                double right = xe;
                // adjust starting point and ending point
                xs += dx_left;
                xe += dx_right;
                // clip line
                if (left < clip.getXMin()) {
                    left = clip.getXMin();
                    if (right < clip.getXMin()) {
                        continue;
                    }
                }
                if (right > clip.getXMax()) {
                    right = clip.getXMax();

                    if (left > clip.getXMax()) {
                        continue;
                    }
                }
                // draw the line
                //memset((UCHAR  *)dest_addr+(unsigned int)left, color,(unsigned int)((int)right-(int)left+1));
                drawNoClipLine(left, loop_y, right, loop_y, col);
            }
        }
    }
    

    /////////////////////////////////////////////////////
    // this function draws a gouraud shaded polygon, based on the affine texture mapper, instead
    // of interpolating the texture coordinates, we simply interpolate the (R,G,B) values across
    // the polygons, I simply needed at another interpolant, I have mapped u->red, v->green, w->blue
    // note that this is the 8-bit version, and I have decided to throw caution at the wind and see
    // what happens if we do a full RGB interpolation and then at the last minute use the color lookup
    // to find the appropriate color
    public void drawGouraudTriangle2D(Vertex3D[] verts, Color[] colors) {
        if (verts == null || colors == null
            || colors[1] == null || colors[2] == null    
                ) {
            return;
        }
        
        final int TRI_TYPE_NONE = 0;
        final int TRI_TYPE_FLAT_TOP = 1;
        final int TRI_TYPE_FLAT_BOTTOM = 2;
        final int TRI_TYPE_FLAT_MASK = 3;
        final int TRI_TYPE_GENERAL = 4;
        final int INTERP_LHS = 0;
        final int INTERP_RHS  = 1;
        
        final int FIXP16_SHIFT = 0;//16;
        final int FIXP16_ROUND_UP = 0;//0x00008000;  
        
        int v0 = 0,
                v1 = 1,
                v2 = 2,
                temp = 0,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                u, v, w,
                du, dv, dw,
                xi, yi, // the current interpolated x,y
                ui, vi, wi, // the current interpolated u,v
                index_x, index_y, // looping vars
                x, y, // hold general x,y
                xStart,
                xEnd,
                yStart,
                yRestart,
                yend,
                xl,
                dxdyl,
                xr,
                dxdyr,
                dudyl,
                ul,
                dvdyl,
                vl,
                dwdyl,
                wl,
                dudyr,
                ur,
                dvdyr,
                vr,
                dwdyr,
                wr;

        int x0, y0, tu0, tv0, tw0, // cached vertices
                x1, y1, tu1, tv1, tw1,
                x2, y2, tu2, tv2, tw2;

        int rBase0, gBase0, bBase0,
                rBase1, gBase1, bBase1,
                rBase2, gBase2, bBase2;
//UCHAR  *screen_ptr  = NULL,
//	   *screen_line = NULL,
//	   *textmap     = NULL;

// first trivial clipping rejection tests 
        if (((verts[0].getPosition().getY() < clip.getYMin())
                && (verts[1].getPosition().getY() < clip.getYMin())
                && (verts[2].getPosition().getY() < clip.getYMin()))
                
                || ((verts[0].getPosition().getY() > clip.getYMax())
                && (verts[1].getPosition().getY() > clip.getYMax())
                && (verts[2].getPosition().getY() > clip.getYMax()))
                
                || ((verts[0].getPosition().getX() < clip.getXMin())
                && (verts[1].getPosition().getX() < clip.getXMin())
                && (verts[2].getPosition().getX() < clip.getXMin()))
                
                || ((verts[0].getPosition().getX() > clip.getXMax())
                && (verts[1].getPosition().getX() > clip.getXMax())
                && (verts[2].getPosition().getX() > clip.getXMax()))) {
            return;
        }

        // degenerate triangle
        if (Mathem.isEquals1(verts[0].getPosition().getX(), verts[1].getPosition().getX()) 
                && Mathem.isEquals1(verts[1].getPosition().getX(), verts[2].getPosition().getX())
                || Mathem.isEquals1(verts[0].getPosition().getY(), verts[1].getPosition().getY()) 
                && Mathem.isEquals1(verts[1].getPosition().getY(), verts[2].getPosition().getY())) {
            return;
        }

        // sort vertices
        if (verts[v1].getPosition().getY() < verts[v0].getPosition().getY()) {
//            SWAP(v0, v1, temp);
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v0].getPosition().getY()) {
//            SWAP(v0, v2, temp);
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v1].getPosition().getY()) {
//            SWAP(v1, v2, temp);
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }

// now test for trivial flat sided cases
        if (Mathem.isEquals1(verts[v0].getPosition().getY(), verts[v1].getPosition().getY())) {
            type = TRI_TYPE_FLAT_TOP;
            if (verts[v1].getPosition().getX() < verts[v0].getPosition().getX()) {
//		{SWAP(v0,v1,temp);}
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (Mathem.isEquals1(verts[v1].getPosition().getY(), verts[v2].getPosition().getY())) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (verts[v2].getPosition().getX() < verts[v1].getPosition().getX()) {
//		{SWAP(v1,v2,temp);}
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }
        
        Point3D p0 = verts[v0].getPosition(),
                p1 = verts[v1].getPosition(),
                p2 = verts[v2].getPosition();

//_RGB565FROM16BIT(face->lit_color[v0], &r_base0, &g_base0, &b_base0);
//_RGB565FROM16BIT(face->lit_color[v1], &r_base1, &g_base1, &b_base1);
//_RGB565FROM16BIT(face->lit_color[v2], &r_base2, &g_base2, &b_base2);
//// scale to 8 bit 
//rBase0 <<= 3;
//gBase0 <<= 2;
//bBase0 <<= 3;
//// scale to 8 bit 
//rBase1 <<= 3;
//gBase1 <<= 2;
//bBase1 <<= 3;
//// scale to 8 bit 
//rBase2 <<= 3;
//gBase2 <<= 2;
//bBase2 <<= 3;
        rBase0 = colors[v0].getRed();
        gBase0 = colors[v0].getGreen();
        bBase0 = colors[v0].getBlue();

        rBase1 = colors[v1].getRed();
        gBase1 = colors[v1].getGreen();
        bBase1 = colors[v1].getBlue();

        rBase2 = colors[v2].getRed();
        gBase2 = colors[v2].getGreen();
        bBase2 = colors[v2].getBlue();

        // extract vertices for processing, now that we have order
//        x0  = (int)(p0.getX()+0.5);
//        y0  = (int)(p0.getY()+0.5);
        x0 = Mathem.toInt(p0.getX());
        y0 = Mathem.toInt(p0.getY());

        tu0 = rBase0;
        tv0 = gBase0;
        tw0 = bBase0;

//        x1  = (int)(p1.getX()+0.5);
//        y1  = (int)(p1.getY()+0.5);
        x1 = Mathem.toInt(p1.getX());
        y1 = Mathem.toInt(p1.getY());

        tu1 = rBase1;
        tv1 = gBase1;
        tw1 = bBase1;

//        x2  = (int)(p2.getX()+0.5);
//        y2  = (int)(p2.getY()+0.5);
        x2 = Mathem.toInt(p2.getX());
        y2 = Mathem.toInt(p2.getY());

        tu2 = rBase2;
        tv2 = gBase2;
        tw2 = bBase2;

// set interpolation restart value
        yRestart = y1;
// what kind of triangle
        if ((type & TRI_TYPE_FLAT_MASK) > 0) {
            if (type == TRI_TYPE_FLAT_TOP) {
                // compute all deltas
                dy = (y2 - y0);

                dxdyl = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyl = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyl = ((tv2 - tv0) << FIXP16_SHIFT) / dy;
                dwdyl = ((tw2 - tw0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu1) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv1) << FIXP16_SHIFT) / dy;
                dwdyr = ((tw2 - tw1) << FIXP16_SHIFT) / dy;

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    wl = dwdyl * dy + (tw0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu1 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv1 << FIXP16_SHIFT);
                    wr = dwdyr * dy + (tw1 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();

                } else {
		// no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x1 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);
                    wl = (tw0 << FIXP16_SHIFT);

                    ur = (tu1 << FIXP16_SHIFT);
                    vr = (tv1 << FIXP16_SHIFT);
                    wr = (tw1 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            } else {
	// must be flat bottom

                // compute all deltas
                dy = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dy;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dy;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dy;
                dwdyl = ((tw1 - tw0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dy;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dy;

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    wl = dwdyl * dy + (tw0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                    wr = dwdyr * dy + (tw0 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();
                } else {
		// no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x0 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);
                    wl = (tw0 << FIXP16_SHIFT);

                    ur = (tu0 << FIXP16_SHIFT);
                    vr = (tv0 << FIXP16_SHIFT);
                    wr = (tw0 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            }
            // test for bottom clip, always
            if ((yend = y2) > clip.getYMax()) {
                yend = clip.getYMax();
            }
            // test for horizontal clipping
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {
    // clip version

	// point screen ptr to starting line
//	screen_ptr = dest_buffer + (yStart * mem_pitch);
                for (yi = yStart; yi <= yend; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;
                    wi = wl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                    } else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                    }

		///////////////////////////////////////////////////////////////////////
                    // test for x clipping, LHS
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        wi += dx * dw;

                        // reset vars
                        xStart = clip.getXMin();

                    }
                    // test for x clipping RHS
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

		///////////////////////////////////////////////////////////////////////
                    // draw span
                    for (xi = xStart; xi <= xEnd; xi++) {
                        // write textel assume 5.6.5
//   		    screen_ptr[xi] = rgblookup[( ((ui >> (FIXP16_SHIFT+3)) << 11) + 
//                                         ((vi >> (FIXP16_SHIFT+2)) << 5) + 
//                                          (wi >> (FIXP16_SHIFT+3)) ) ];  
//                        drawNoClipPoint(xi, yi, new Color(ui, vi, wi));

                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                    }

                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;

		// advance screen ptr
//		screen_ptr+=mem_pitch;
                }

            }
            else {
	// non-clip version

	// point screen ptr to starting line
//	screen_ptr = dest_buffer + (yStart * mem_pitch);
                for (yi = yStart; yi <= yend; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;
                    wi = wl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                    }

                    // draw span
                    for (xi = xStart; xi <= xEnd; xi++) {
                        // write textel 5.6.5
//            screen_ptr[xi] = rgblookup[( ((ui >> (FIXP16_SHIFT+3)) << 11) + 
//                                         ((vi >> (FIXP16_SHIFT+2)) << 5) + 
//                                          (wi >> (FIXP16_SHIFT+3)) ) ];  
//                        drawNoClipPoint(xi, yi, new Color(ui, vi, wi));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                    }

                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;
		// advance screen ptr
//		screen_ptr+=mem_pitch;
                }
            }
        }
        else if (type == TRI_TYPE_GENERAL) {

            // first test for bottom clip, always
            if ((yend = y2) > clip.getYMax()) {
                yend = clip.getYMax();
            }

            // pre-test y clipping status
            if (y1 < clip.getYMin()) {
		// compute all deltas
                // LHS
                dyl = (y2 - y1);

                dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;
                dwdyl = ((tw2 - tw1) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dyr;

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);

                ul = dudyl * dyl + (tu1 << FIXP16_SHIFT);
                vl = dvdyl * dyl + (tv1 << FIXP16_SHIFT);
                wl = dwdyl * dyl + (tw1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);

                ur = dudyr * dyr + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dyr + (tv0 << FIXP16_SHIFT);
                wr = dwdyr * dyr + (tw0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dwdyl = Mathem.returnFirst(dwdyr, dwdyr = dwdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    wl = Mathem.returnFirst(wr, wr = wl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tw1 = Mathem.returnFirst(tw2, tw2 = tw1);

                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            }
            else if (y0 < clip.getYMin()) {
		// compute all deltas
                // LHS
                dyl = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dyl;
                dwdyl = ((tw1 - tw0) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dyr;

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                wl = dwdyl * dy + (tw0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                wr = dwdyr * dy + (tw0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dwdyl = Mathem.returnFirst(dwdyr, dwdyr = dwdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    wl = Mathem.returnFirst(wr, wr = wl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tw1 = Mathem.returnFirst(tw2, tw2 = tw1);

                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            } else {
		// no initial y clipping

		// compute all deltas
                // LHS
                dyl = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dyl;
                dwdyl = ((tw1 - tw0) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                ul = (tu0 << FIXP16_SHIFT);
                vl = (tv0 << FIXP16_SHIFT);
                wl = (tw0 << FIXP16_SHIFT);

                ur = (tu0 << FIXP16_SHIFT);
                vr = (tv0 << FIXP16_SHIFT);
                wr = (tw0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dwdyl = Mathem.returnFirst(dwdyr, dwdyr = dwdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    wl = Mathem.returnFirst(wr, wr = wl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tw1 = Mathem.returnFirst(tw2, tw2 = tw1);
                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            }
            // test for horizontal clipping
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {
    // clip version
                // x clipping	

	// point screen ptr to starting line
//	screen_ptr = dest_buffer + (yStart * mem_pitch);
                for (yi = yStart; yi <= yend; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;
                    wi = wl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                    }

		///////////////////////////////////////////////////////////////////////
                    // test for x clipping, LHS
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        wi += dx * dw;

                        // set x to left clip edge
                        xStart = clip.getXMin();

                    }
                    // test for x clipping RHS
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

		///////////////////////////////////////////////////////////////////////
                    // draw span
                    for (xi = xStart; xi <= xEnd; xi++) {
                        // write textel assume 5.6.5
//   		    screen_ptr[xi] = rgblookup[( ((ui >> (FIXP16_SHIFT+3)) << 11) + 
//                                         ((vi >> (FIXP16_SHIFT+2)) << 5) + 
//                                          (wi >> (FIXP16_SHIFT+3)) ) ];   
//                        drawNoClipPoint(xi, yi, new Color(ui, vi, wi));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                    }

                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;

		// advance screen ptr
//		screen_ptr+=mem_pitch;
                    // test for yi hitting second region, if so change interpolant
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                            dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;
                            dwdyl = ((tw2 - tw1) << FIXP16_SHIFT) / dyl;

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            wl = (tw1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            wl += dwdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dwdyr = ((tw1 - tw2) << FIXP16_SHIFT) / dyr;

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            wr = (tw2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            wr += dwdyr;
                        }
                    }
                }
            }
            else {
	// no x clipping
                // point screen ptr to starting line
//	screen_ptr = dest_buffer + (yStart * mem_pitch);

                for (yi = yStart; yi <= yend; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;
                    wi = wl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                    }

                    // draw span
                    for (xi = xStart; xi <= xEnd; xi++) {
                        // write textel assume 5.6.5
//   		    screen_ptr[xi] = rgblookup[( ((ui >> (FIXP16_SHIFT+3)) << 11) + 
//                                         ((vi >> (FIXP16_SHIFT+2)) << 5) + 
//                                          (wi >> (FIXP16_SHIFT+3)) ) ];  
                        if (ui > 255) ui = 255;
                        if (vi > 255) vi = 255;
                        if (wi > 255) wi = 255;
//                        drawNoClipPoint(xi, yi, new Color(ui, vi, wi));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                    }

                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;

		// advance screen ptr
//		screen_ptr+=mem_pitch;
                    // test for yi hitting second region, if so change interpolant
                    if (yi == yRestart) {
                        // test interpolation side change flag

                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                            dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;
                            dwdyl = ((tw2 - tw1) << FIXP16_SHIFT) / dyl;

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            wl = (tw1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            wl += dwdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dwdyr = ((tw1 - tw2) << FIXP16_SHIFT) / dyr;

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            wr = (tw2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            wr += dwdyr;
                        }
                    }
                }
            }
        }
}
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DRAW POLYGONS
    
    /////////////////////////////////////////////////////
    //
    public void drawPolygon3D(Polygon3DVerts poly, String shadeType) {
	switch(poly.getType()) {
	    case POINT:
		drawPoint(poly.getVertexPosition(0), poly.getColor());
		break;
	    case LINE:
		drawLine(poly.getVertexPosition(0), poly.getVertexPosition(1), poly.getColor());
		break;
	    default:
                switch(shadeType) {
                    case Main.RADIO_SHADE_CONST_TEXT:
                    case Main.RADIO_SHADE_FLAT_TEXT:
                        drawFlatPolygon(poly.getVertexes(), poly.getColor());
                        break;
                    case Main.RADIO_SHADE_GOURAD_TEXT:
                        drawGouraudTriangle2D(poly.getVertexes(), poly.getColors());
                }
                
		break;
	}
    }
    
    /////////////////////////////////////////////////////
    // Draw filled polygon
    // (with awt.Graphics.fillPolygon())
    public void drawAwtPolygon(Vertex3D[] verts) {
	if (verts == null) return;
	int size = verts.length;
	int xs[] = new int[size];
	int ys[] = new int[size];
	for (int i = 0; i < size; i++) {
	    if (verts[i] == null) continue;
	    xs[i] = (int)(verts[i].getPosition().getX() + 0.5);
	    ys[i] = (int)(verts[i].getPosition().getY() + 0.5);
	}
	graphic.fillPolygon(xs, ys, size);
    }
        
    // Draw flat polygon
    public void drawFlatPolygon(Vertex3D[] verts, Color col) {
        if (verts == null || verts.length < 3) return;
        Point3D p1 = verts[0].getPosition();
        Point3D p2 = verts[1].getPosition();
        Point3D p3 = verts[2].getPosition();
        drawTriangle2D(p1.getX(), p1.getY(),
                        p2.getX(), p2.getY(),
                        p3.getX(), p3.getY(), col);
    }
    
    // Draw polygon border (edges/wire)
    public void drawPolygonBorder(Polygon3DVerts poly) {
        if (poly == null) return;
        //graphic.setColor(poly.getBorderColor());
        setColor(poly.getBorderColor());
        drawPolygonBorder(poly.getVertexes());
    }
    
    // Draw polygon border (edges/wire)
    public void drawPolygonBorder(Vertex3D[] verts) {
	if (verts == null) return;
	int size = verts.length;
        if (size == 0) return;
	for (int i = 0; i < size-1; i++) {
	    drawLine(verts[i].getPosition(), verts[i+1].getPosition());
	}
	drawLine(verts[size-1].getPosition(), verts[0].getPosition());
    }
    
    /////////////////////////////////////////////////////
    // 
    public void drawBorderedPolies(Polygon3DVerts[] polies, String shadeType) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    // shading
	    drawPolygon3D(poly, shadeType);
	    // border
	    drawPolygonBorder(poly);
	}
    }
    
    public void drawPolies(Polygon3DVerts[] polies, String shadeType) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    // shading
	    drawPolygon3D(poly, shadeType);
	}
    }
        
    public void drawBorders(Polygon3DVerts[] polies) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    // border
	    drawPolygonBorder(poly);
	}
    }
    
    //////////////////////////////////////////////////
    // set
    public void setGraphics(Graphics g) {
	this.graphic = g;
    }
    
    public void setDimension(int width, int height) {
        this.width = width;
	this.height = height;
    }
    
    public void setClipBox(ClipRectangle2D clipBox) {
        this.clip = clipBox;
    }
    
    public void setColor(Color col) {
        if (col == null) return;
	//g.setColor(color);
        curColor = col;
    }

    public void setBackColor(Color col) {
        if (col == null) return;
        backColor = col;
    }
    
}
