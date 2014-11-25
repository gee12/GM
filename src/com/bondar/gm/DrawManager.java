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
    public static final Color POLY_NORMAL_COLOR = Color.RED;
    public static final Color VERT_NORMAL_COLOR = Color.GREEN;
    
    public static final Color EDGES_COLOR = Color.WHITE;

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
    
    public void drawScene(Polygon3DVerts[] polies, String viewType, String shadeType, 
            boolean isTextured, boolean isNormalsPoly, boolean isNormalsVert, 
            Solid2D crosshair) {
        // background
        drawBackground();
//        fillRectangle(new Rectangle(0,0,
//                width,height/2), Color.DARK_GRAY);
//        fillRectangle(new Rectangle(0,height/2,
//                width,height/2), Color.DARK_GRAY);
        // polygons
	switch (viewType) {
	    case Main.RADIO_FACES:
		drawPolies(polies, shadeType, isTextured, isNormalsPoly, isNormalsVert);
		break;
	    case Main.RADIO_EDGES:
		drawEdges(polies, isNormalsPoly, isNormalsVert);
		break;
	    case Main.RADIO_EDGES_FACES:
		drawBorderedPolies(polies, shadeType, isTextured, isNormalsPoly, isNormalsVert);
		break;
	}
        //crosshair
        if (crosshair != null) {
            setColor(crosshair.getColor());
            Point2D[] points = crosshair.getPoints();
            drawLine(points[0], points[1]);
            drawLine(points[2], points[3]);
        }
        // image
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
    public void drawFlatTriangle2D(double x1, double y1, double x2, double y2,
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
                drawNoClipLine(left, loop_y, right, loop_y, col);
            }
        }
    }
    

    /////////////////////////////////////////////////////
    // Draw a gouraud shaded polygon, based on the affine texture mapper, instead
    // of interpolating the texture coordinates, we simply interpolate the (R,G,B) values across
    // the polygons, I simply needed at another interpolant, I have mapped u->red, v->green, w->blue
    // note that this is the 8-bit version, and I have decided to throw caution at the wind and see
    // what happens if we do a full RGB interpolation and then at the last minute use the color lookup
    // to find the appropriate color
    public void drawGouraudTriangle2D(Vertex3D[] verts, Color[] colors) {
        if (verts == null || colors == null
            || colors[1] == null || colors[2] == null) {
            return;
        }
        final int TRI_TYPE_NONE = 0;
        final int TRI_TYPE_FLAT_TOP = 1;
        final int TRI_TYPE_FLAT_BOTTOM = 2;
        final int TRI_TYPE_FLAT_MASK = 3;
        final int TRI_TYPE_GENERAL = 4;
        
        final int INTERP_LHS = 0;
        final int INTERP_RHS  = 1;
        
        final int FIXP16_SHIFT = 16;
        final int FIXP16_ROUND_UP = 0x00008000;
        
        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                du, dv, dw,
                xi, yi, // the current interpolated x,y
                ui, vi, wi, // the current interpolated u,v
                x, y, // hold general x,y
                xStart,
                xEnd,
                yStart,
                yRestart,
                yEnd,
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
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v0].getPosition().getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v1].getPosition().getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }

        if (Mathem.toInt(verts[v0].getPosition().getY()) == Mathem.toInt(verts[v1].getPosition().getY())) {
            if (Mathem.toInt(verts[v0].getPosition().getY()) == Mathem.toInt(verts[v2].getPosition().getY())) {
                // draw line ?!
                return;
            }
            type = TRI_TYPE_FLAT_TOP;
            if (verts[v1].getPosition().getX() < verts[v0].getPosition().getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (Mathem.toInt(verts[v1].getPosition().getY()) == Mathem.toInt(verts[v2].getPosition().getY())) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (verts[v2].getPosition().getX() < verts[v1].getPosition().getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }
        
        Point3D p0 = verts[v0].getPosition(),
                p1 = verts[v1].getPosition(),
                p2 = verts[v2].getPosition();

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
        x0 = Mathem.toInt(p0.getX());
        y0 = Mathem.toInt(p0.getY());

        tu0 = rBase0;
        tv0 = gBase0;
        tw0 = bBase0;

        x1 = Mathem.toInt(p1.getX());
        y1 = Mathem.toInt(p1.getY());

        tu1 = rBase1;
        tv1 = gBase1;
        tw1 = bBase1;

        x2 = Mathem.toInt(p2.getX());
        y2 = Mathem.toInt(p2.getY());

        tu2 = rBase2;
        tv2 = gBase2;
        tw2 = bBase2;

        // set interpolation restart value
        yRestart = y1;
        // what kind of triangle
        if ((type & TRI_TYPE_FLAT_MASK) > 0) {
            ////////////////////////////////////////////////////////////////////
            // FLAT TOP TRIANGLE
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
                ////////////////////////////////////////////////////////////////
                // FLAT BOTTOM TRIANGLE
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
            if ((yEnd = y2) > clip.getYMax()) {
                yEnd = clip.getYMax();
            }
            ////////////////////////////////////////////////////////////////
            // DRAW TOP/BOTTOM TRIANGLE
            
            // CLIP HORISONTAL?
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {
                // CLIP VERSION
                for (yi = yStart; yi <= yEnd; yi++) {
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

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
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
                    // CLIP RIGHT
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT));
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
                }
            }
            else {
                ////////////////////////////////////////////////////////////////
                // NON-CLIP VERSION
                for (yi = yStart; yi <= yEnd; yi++) {
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

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT));
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
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////
        // GENERAL TRIANGLE
        else if (type == TRI_TYPE_GENERAL) {

            // CLIP BOTTOM
            if ((yEnd = y2) > clip.getYMax()) {
                yEnd = clip.getYMax();
            }

            // INITIAL TOP CLIP (Y1)
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
            // INITIAL TOP CLIP (Y0)
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
                ////////////////////////////////////////////////////////////////
                // NO INITIAL TOP CLIP
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
            ////////////////////////////////////////////////////////////////////
            // NEED HORISONTAL CLIP?
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {

                // DRAW CLIPPED GENERAL TRIANGLE
                for (yi = yStart; yi <= yEnd; yi++) {
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

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
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
                    // CLIP RIGHT
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

                    ///////////////////////////////////////////////////////////////////////
                    // DRAW CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT));
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

                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
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
                ////////////////////////////////////////////////////////////////
                // DRAW NON-CLIPPING GENERAL TRIANGLE
                for (yi = yStart; yi <= yEnd; yi++) {
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
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT));
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
                    
                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
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
    
    /////////////////////////////////////////////////////
    public void drawTexturedConstTriangle(Vertex3D[] verts, BufferedImage texture, Point2D[] texturePoints) {
        if (verts == null || texture == null || texturePoints == null) {
            return;
        }
        final int TRI_TYPE_NONE = 0;
        final int TRI_TYPE_FLAT_TOP = 1;
        final int TRI_TYPE_FLAT_BOTTOM = 2;
        final int TRI_TYPE_FLAT_MASK = 3;
        final int TRI_TYPE_GENERAL = 4;
        
        final int INTERP_LHS = 0;
        final int INTERP_RHS  = 1;
        
        final int FIXP16_SHIFT = 16;
        final int FIXP16_ROUND_UP = 0x00008000;

        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                du, dv,
                xi, yi, // the current interpolated x,y
                ui, vi, // the current interpolated u,v
                xStart,
                xEnd,
                yStart,
                yRestart,
                yEnd,
                xl,
                dxdyl,
                xr,
                dxdyr,
                dudyl,
                ul,
                dvdyl,
                vl,
                dudyr,
                ur,
                dvdyr,
                vr;

        int x0, y0, tu0, tv0, // cached vertices
                x1, y1, tu1, tv1,
                x2, y2, tu2, tv2;
        
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
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v0].getPosition().getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v1].getPosition().getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }
        
        if (Mathem.toInt(verts[v0].getPosition().getY()) == Mathem.toInt(verts[v1].getPosition().getY())) {
            if (Mathem.toInt(verts[v0].getPosition().getY()) == Mathem.toInt(verts[v2].getPosition().getY())) {
                // draw line ?!
                return;
            }
            type = TRI_TYPE_FLAT_TOP;
            if (verts[v1].getPosition().getX() < verts[v0].getPosition().getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (Mathem.toInt(verts[v1].getPosition().getY()) == Mathem.toInt(verts[v2].getPosition().getY())) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (verts[v2].getPosition().getX() < verts[v1].getPosition().getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }
        
        Point3D p0 = verts[v0].getPosition(),
                p1 = verts[v1].getPosition(),
                p2 = verts[v2].getPosition();
        
        Point2D tp0 = texturePoints[v0],
                tp1 = texturePoints[v1],
                tp2 = texturePoints[v2];

        // extract vertices for processing, now that we have order
        x0 = Mathem.toInt(p0.getX());
        y0 = Mathem.toInt(p0.getY());
        tu0 = (int)tp0.getX();
        tv0 = (int)tp0.getY();

        x1 = Mathem.toInt(p1.getX());
        y1 = Mathem.toInt(p1.getY());
        tu1 = (int)tp1.getX();
        tv1 = (int)tp1.getY();

        x2 = Mathem.toInt(p2.getX());
        y2 = Mathem.toInt(p2.getY());
        tu2 = (int)tp2.getX();
        tv2 = (int)tp2.getY();
        
        // set interpolation restart value
        yRestart = y1;
        // what kind of triangle
        if ((type & TRI_TYPE_FLAT_MASK) > 0) {
            ////////////////////////////////////////////////////////////////////
            // FLAT TOP TRIANGLE
            if (type == TRI_TYPE_FLAT_TOP) {
                // compute all deltas
                dy = (y2 - y0);

                dxdyl = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyl = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyl = ((tv2 - tv0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu1) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv1) << FIXP16_SHIFT) / dy;
                
                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu1 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv1 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();

                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x1 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);

                    ur = (tu1 << FIXP16_SHIFT);
                    vr = (tv1 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            } else {
                ////////////////////////////////////////////////////////////////
                // FLAT BOTTOM TRIANGLE
                // compute all deltas
                dy = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dy;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dy;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dy;

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();
                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x0 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);

                    ur = (tu0 << FIXP16_SHIFT);
                    vr = (tv0 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            }
            // test for bottom clip, always
            if ((yEnd = y2) > clip.getYMax()) {
                yEnd = clip.getYMax();
            }
            
            ////////////////////////////////////////////////////////////////
            // HORISONTAL CLIP TOP/BOTTOM TRIANGLE
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {
                // CLIP VERSION
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    } else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }
                    
                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;
                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        // reset vars
                        xStart = clip.getXMin();
                    }
                    // CLIP RIGHT
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, col);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                }
            }
            else {
                ////////////////////////////////////////////////////////////////
                // NON-CLIP VERSION
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, col);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////
        // GENERAL TRIANGLE
        else if (type == TRI_TYPE_GENERAL) {

            // CLIP BOTTOM
            if ((yEnd = y2) > clip.getYMax()) {
                yEnd = clip.getYMax();
            }

            // INITIAL TOP CLIP (Y1)
            if (y1 < clip.getYMin()) {
		// compute all deltas
                // LHS
                dyl = (y2 - y1);

                dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);
                ul = dudyl * dyl + (tu1 << FIXP16_SHIFT);
                vl = dvdyl * dyl + (tv1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);
                ur = dudyr * dyr + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dyr + (tv0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);

                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            }
            // INITIAL TOP CLIP (Y0)
            else if (y0 < clip.getYMin()) {
		// compute all deltas
                // LHS
                dyl = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);

                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            } else {
                ////////////////////////////////////////////////////////////////
                // NO INITIAL TOP CLIP
		// compute all deltas
                // LHS
                dyl = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                ul = (tu0 << FIXP16_SHIFT);
                vl = (tv0 << FIXP16_SHIFT);

                ur = (tu0 << FIXP16_SHIFT);
                vr = (tv0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            }
            ////////////////////////////////////////////////////////////////////
            // NEED HORISONTAL CLIP?
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {

                // DRAW CLIPPED GENERAL TRIANGLE
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;

                        // set x to left clip edge
                        xStart = clip.getXMin();

                    }
                    // CLIP RIGHT
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

                    ///////////////////////////////////////////////////////////////////////
                    // DRAW CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, col);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;

                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                            dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                        }
                    }
                }
            }
            else {
                ////////////////////////////////////////////////////////////////
                // DRAW NON-CLIPPING GENERAL TRIANGLE
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, col);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    
                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                            dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                        }
                    }
                }
            }
        }
    }            

    /////////////////////////////////////////////////////
    public void drawTexturedFlatTriangle(Vertex3D[] verts, BufferedImage texture, Point2D[] texturePoints, Color lightCol) {
        if (verts == null || texture == null || texturePoints == null || lightCol == null) {
            return;
        }
        final int TRI_TYPE_NONE = 0;
        final int TRI_TYPE_FLAT_TOP = 1;
        final int TRI_TYPE_FLAT_BOTTOM = 2;
        final int TRI_TYPE_FLAT_MASK = 3;
        final int TRI_TYPE_GENERAL = 4;
        
        final int INTERP_LHS = 0;
        final int INTERP_RHS  = 1;
        
        final int FIXP16_SHIFT = 16;
        final int FIXP16_ROUND_UP = 0x00008000;

        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                du, dv,
                xi, yi, // the current interpolated x,y
                ui, vi, // the current interpolated u,v
                xStart,
                xEnd,
                yStart,
                yRestart,
                yEnd,
                xl,
                dxdyl,
                xr,
                dxdyr,
                dudyl,
                ul,
                dvdyl,
                vl,
                dudyr,
                ur,
                dvdyr,
                vr;

        int x0, y0, tu0, tv0, // cached vertices
                x1, y1, tu1, tv1,
                x2, y2, tu2, tv2;
        
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
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v0].getPosition().getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (verts[v2].getPosition().getY() < verts[v1].getPosition().getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }
        
        if (Mathem.toInt(verts[v0].getPosition().getY()) == Mathem.toInt(verts[v1].getPosition().getY())) {
            if (Mathem.toInt(verts[v0].getPosition().getY()) == Mathem.toInt(verts[v2].getPosition().getY())) {
                // draw line ?!
                return;
            }
            type = TRI_TYPE_FLAT_TOP;
            if (verts[v1].getPosition().getX() < verts[v0].getPosition().getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (Mathem.toInt(verts[v1].getPosition().getY()) == Mathem.toInt(verts[v2].getPosition().getY())) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (verts[v2].getPosition().getX() < verts[v1].getPosition().getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }
        
        Point3D p0 = verts[v0].getPosition(),
                p1 = verts[v1].getPosition(),
                p2 = verts[v2].getPosition();
        
        Point2D tp0 = texturePoints[v0],
                tp1 = texturePoints[v1],
                tp2 = texturePoints[v2];
        
        // extract base color of lit poly, so we can modulate texture a bit
//        int rBase = intensCol.getRed();
//        int gBase = intensCol.getGreen();
//        int bBase = intensCol.getBlue();

        // build 4.4.4 intensity for color modulation
//        base_rgb444 = ( (b_base >> 4) + ((g_base >> 4) << 4) + ((r_base >> 4) << 8) );

        // extract vertices for processing, now that we have order
        x0 = Mathem.toInt(p0.getX());
        y0 = Mathem.toInt(p0.getY());
        tu0 = (int)tp0.getX();
        tv0 = (int)tp0.getY();

        x1 = Mathem.toInt(p1.getX());
        y1 = Mathem.toInt(p1.getY());
        tu1 = (int)tp1.getX();
        tv1 = (int)tp1.getY();

        x2 = Mathem.toInt(p2.getX());
        y2 = Mathem.toInt(p2.getY());
        tu2 = (int)tp2.getX();
        tv2 = (int)tp2.getY();
        
        // set interpolation restart value
        yRestart = y1;
        // what kind of triangle
        if ((type & TRI_TYPE_FLAT_MASK) > 0) {
            ////////////////////////////////////////////////////////////////////
            // FLAT TOP TRIANGLE
            if (type == TRI_TYPE_FLAT_TOP) {
                // compute all deltas
                dy = (y2 - y0);

                dxdyl = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyl = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyl = ((tv2 - tv0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu1) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv1) << FIXP16_SHIFT) / dy;
                
                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu1 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv1 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();

                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x1 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);

                    ur = (tu1 << FIXP16_SHIFT);
                    vr = (tv1 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            } else {
                ////////////////////////////////////////////////////////////////
                // FLAT BOTTOM TRIANGLE
                // compute all deltas
                dy = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dy;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dy;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dy;

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();
                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x0 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);

                    ur = (tu0 << FIXP16_SHIFT);
                    vr = (tv0 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            }
            // test for bottom clip, always
            if ((yEnd = y2) > clip.getYMax()) {
                yEnd = clip.getYMax();
            }
            
            ////////////////////////////////////////////////////////////////
            // HORISONTAL CLIP TOP/BOTTOM TRIANGLE
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {
                // CLIP VERSION
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    } else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }
                    
                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;
                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        // reset vars
                        xStart = clip.getXMin();
                    }
                    // CLIP RIGHT
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, LightManager.toLight(lightCol, col));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                }
            }
            else {
                ////////////////////////////////////////////////////////////////
                // NON-CLIP VERSION
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, LightManager.toLight(lightCol, col));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////
        // GENERAL TRIANGLE
        else if (type == TRI_TYPE_GENERAL) {

            // CLIP BOTTOM
            if ((yEnd = y2) > clip.getYMax()) {
                yEnd = clip.getYMax();
            }

            // INITIAL TOP CLIP (Y1)
            if (y1 < clip.getYMin()) {
		// compute all deltas
                // LHS
                dyl = (y2 - y1);

                dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);
                ul = dudyl * dyl + (tu1 << FIXP16_SHIFT);
                vl = dvdyl * dyl + (tv1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);
                ur = dudyr * dyr + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dyr + (tv0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);

                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            }
            // INITIAL TOP CLIP (Y0)
            else if (y0 < clip.getYMin()) {
		// compute all deltas
                // LHS
                dyl = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);

                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            } else {
                ////////////////////////////////////////////////////////////////
                // NO INITIAL TOP CLIP
		// compute all deltas
                // LHS
                dyl = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dyl;
                dudyl = ((tu1 - tu0) << FIXP16_SHIFT) / dyl;
                dvdyl = ((tv1 - tv0) << FIXP16_SHIFT) / dyl;

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                ul = (tu0 << FIXP16_SHIFT);
                vl = (tv0 << FIXP16_SHIFT);

                ur = (tu0 << FIXP16_SHIFT);
                vr = (tv0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    // set interpolation restart
                    iRestart = INTERP_RHS;
                }
            }
            ////////////////////////////////////////////////////////////////////
            // NEED HORISONTAL CLIP?
            if ((x0 < clip.getXMin()) || (x0 > clip.getXMax())
                    || (x1 < clip.getXMin()) || (x1 > clip.getXMax())
                    || (x2 < clip.getXMin()) || (x2 > clip.getXMax())) {

                // DRAW CLIPPED GENERAL TRIANGLE
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;

                        // set x to left clip edge
                        xStart = clip.getXMin();

                    }
                    // CLIP RIGHT
                    if (xEnd > clip.getXMax()) {
                        xEnd = clip.getXMax();
                    }

                    ///////////////////////////////////////////////////////////////////////
                    // DRAW CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, LightManager.toLight(lightCol, col));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;

                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                            dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                        }
                    }
                }
            }
            else {
                ////////////////////////////////////////////////////////////////
                // DRAW NON-CLIPPING GENERAL TRIANGLE
                for (yi = yStart; yi <= yEnd; yi++) {
                    // compute span endpoints
                    xStart = ((xl + FIXP16_ROUND_UP) >> FIXP16_SHIFT);
                    xEnd = ((xr + FIXP16_ROUND_UP) >> FIXP16_SHIFT);

                    // compute starting points for u,v,w interpolants
                    ui = ul + FIXP16_ROUND_UP;
                    vi = vl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                    }
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        Color col = new Color(texture.getRGB(ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT));
                        drawNoClipPoint(xi, yi, LightManager.toLight(lightCol, col));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    
                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dudyl = ((tu2 - tu1) << FIXP16_SHIFT) / dyl;
                            dvdyl = ((tv2 - tv1) << FIXP16_SHIFT) / dyl;

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                        }
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DRAW POLYGONS
    
    /////////////////////////////////////////////////////
    // Draw filled polygon (awt.Graphics)
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
        drawFlatTriangle2D(p1.getX(), p1.getY(),
                        p2.getX(), p2.getY(),
                        p3.getX(), p3.getY(), col);
    }

    public void drawPolies(Polygon3DVerts[] polies, String shadeType, boolean isTextured, boolean isNormalsPoly, boolean isNormalsVert) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    drawPolygon3D(poly, shadeType, isTextured);
            //
            if (isNormalsPoly) {
                drawPolygonNormal(poly);
            }
            if (isNormalsVert) {
                drawVertexNormals(poly);
            }
	}
    }
    
    public void drawPolygon3D(Polygon3DVerts poly, String shadeType, boolean isTextured) {
	switch(poly.getType()) {
	    case POINT:
		drawPoint(poly.getVertexPosition(0), poly.getColor());
		break;
	    case LINE:
		drawLine(poly.getVertexPosition(0), poly.getVertexPosition(1), poly.getColor());
		break;
	    default:
                if (!isTextured) {
                    switch(shadeType) {
                        case Main.RADIO_SHADE_CONST:
                        case Main.RADIO_SHADE_FLAT:
                            drawFlatPolygon(poly.getVertexes(), poly.getColor());
                            break;
                        case Main.RADIO_SHADE_GOURAD:
                            drawGouraudTriangle2D(poly.getVertexes(), poly.getColors());
                            break;
                    }
                } else {
                    BufferedImage texture = TextureManager.getImage(poly.getTextureId());
                    switch(shadeType) {
                        case Main.RADIO_SHADE_CONST:
                            drawTexturedConstTriangle(poly.getVertexes(), texture, 
                                    poly.getTexturePoints());
                            break;
                        case Main.RADIO_SHADE_FLAT:
                            drawTexturedFlatTriangle(poly.getVertexes(), texture, 
                                    poly.getTexturePoints(), poly.getColor());
                            break;
                        case Main.RADIO_SHADE_GOURAD:
                            // textures with gourad shading NOT SUPPORTED yet
                            drawGouraudTriangle2D(poly.getVertexes(), poly.getColors());
                            break;
                    }
                }
                
		break;
	}
    }
    
    //////////////////////////////////////////////////
    // Draw edges
    public void drawEdges(Polygon3DVerts[] polies, boolean isNormalsPoly, boolean isNormalsVert) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    drawEdges(poly);
            //
            if (isNormalsPoly) {
                drawPolygonNormal(poly);
            }
            if (isNormalsVert) {
                drawVertexNormals(poly);
            }
	}
    }
    
    public void drawEdges(Polygon3DVerts poly) {
        if (poly == null) return;
        setColor(poly.getColor());
        drawEdges(poly.getVertexes());
    }
    
    public void drawEdges(Polygon3DVerts poly, Color col) {
        if (poly == null) return;
        setColor(col);
        drawEdges(poly.getVertexes());
    }
    
    public void drawEdges(Vertex3D[] verts) {
	if (verts == null) return;
	int size = verts.length;
        if (size == 0) return;
	for (int i = 0; i < size-1; i++) {
	    drawLine(verts[i].getPosition(), verts[i+1].getPosition());
	}
	drawLine(verts[size-1].getPosition(), verts[0].getPosition());
    }
    
    //////////////////////////////////////////////////        
    // Draw polygons and edges
    public void drawBorderedPolies(Polygon3DVerts[] polies, String shadeType,
            boolean isTextured, boolean isNormalsPoly, boolean isNormalsVert) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    // shading
	    drawPolygon3D(poly, shadeType, isTextured);
	    // edges
            if (poly.getSize() >= 3) {
                drawEdges(poly, EDGES_COLOR);
            }
            if (isNormalsPoly) {
                drawPolygonNormal(poly);
            }
            if (isNormalsVert) {
                drawVertexNormals(poly);
            }
	}
    }
    
    //////////////////////////////////////////////////
    // Draw normals
    public void drawPolygonNormal(Polygon3DVerts poly) {
        if (poly == null || poly.getSize() < 3) return;
        
        Point3D n = poly.getNormal();
        
        Point3D c = poly.getVertexPosition(0).getCopy();
        c.add(poly.getVertexPosition(1));
        c.add(poly.getVertexPosition(2));
        c.mul(0.333);
        
//        drawLine(poly.getVertexPosition(0), n, POLY_NORMAL_COLOR);
        drawLine(c, n, POLY_NORMAL_COLOR);
    }

    public void drawVertexNormals(Polygon3DVerts poly) {
        if (poly == null || poly.getSize() < 3) return;
        
        Point3D n0 = poly.getVertexes()[0].getNormal();
        Point3D n1 = poly.getVertexes()[1].getNormal();
        Point3D n2 = poly.getVertexes()[2].getNormal();

        drawLine(poly.getVertexPosition(0), n0, VERT_NORMAL_COLOR);
        drawLine(poly.getVertexPosition(1), n1, VERT_NORMAL_COLOR);
        drawLine(poly.getVertexPosition(2), n2, VERT_NORMAL_COLOR);
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
        curColor = col;
    }

    public void setBackColor(Color col) {
        if (col == null) return;
        backColor = col;
    }
    
}
