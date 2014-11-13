package com.bondar.gm;

import com.bondar.geom.ClipRectangle2D;
import com.bondar.geom.Point2D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Vertex3D;
import com.bondar.tasks.GM;
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

    private static final Point2D[] CROSSHAIR = new Point2D[4];
    
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
        final int TONE = 20;
        backColor = new Color(TONE,TONE,TONE);
        // init crosshair 
        Point2D cx = new Point2D(width/2,height/2);//getScreenCenter());
        //cx = cx.sub(new Point2D(50,50));
        final int SIZE = 10;
        CROSSHAIR[0] = new Point2D(cx.getX(), cx.getY() + SIZE);
        CROSSHAIR[1] = new Point2D(cx.getX(), cx.getY() - SIZE);
        CROSSHAIR[2] = new Point2D(cx.getX() - SIZE, cx.getY());
        CROSSHAIR[3] = new Point2D(cx.getX() + SIZE, cx.getY());
    }
    
    public void drawScene(Polygon3DVerts[] polies, String viewType, Color crossCol) {
        //
        fillRectangle(new Rectangle(0,0,
                width,height/2), Color.DARK_GRAY);
        fillRectangle(new Rectangle(0,height/2,
                width,height/2), Color.BLACK);
        //
	switch (viewType) {
	    case GM.RADIO_FACES_TEXT:
		drawPolies(polies);
		break;
	    case GM.RADIO_EDGES_TEXT:
		drawBorders(polies);
		break;
	    case GM.RADIO_EDGES_FACES_TEXT:
		drawBorderedPolies(polies);
		break;
	}
        //
        setColor(crossCol);
        drawLine(CROSSHAIR[0], CROSSHAIR[1]);
        drawLine(CROSSHAIR[2], CROSSHAIR[3]);
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
        setPixel(p.getX(), p.getY(), curColor);
    }

    public void drawPoint(Point3D p, Color col) {
	if (p == null) return;
	setPixel(p.getX(), p.getY(), col);
    }

    public void setPixel(double x, double y, Color col) {
        setPixel(Mathem.toInt(x), Mathem.toInt(y), col);
    }
   
    public void setNoClipPixel(int x, int y, Color col) {
        if (col == null) return;
	image.setRGB(x, y, col.getRGB());	    
    }    
    public void setPixel(int x, int y, Color col, boolean isNeedClip) {
        if (isNeedClip) setPixel(x, y, col);
        else setNoClipPixel(x, y, col);
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
                 setPixel(x, y, col, isNeedClip);
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
                setPixel(x, y, col, isNeedClip);
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
    // this function draws a triangle that has a flat bottom
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
    

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DRAW POLYGONS
    
    /////////////////////////////////////////////////////
    //
    public void drawPolygon3D(Polygon3DVerts poly) {
	setColor(poly.getColor());
	switch(poly.getType()) {
	    case POINT:
		drawPoint(poly.getVertexPosition(0));
		break;
	    case LINE:
		drawLine(poly.getVertexPosition(0), poly.getVertexPosition(1));
		break;
	    default:
                drawFlatPolygon(poly.getVertexes());
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
    public void drawFlatPolygon(Vertex3D[] verts) {
        drawFlatPolygon(verts, curColor);
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
    public void drawBorderedPolies(Polygon3DVerts[] polies) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    // shading
	    drawPolygon3D(poly);
	    // border
	    drawPolygonBorder(poly);
	}
    }
    
    public void drawPolies(Polygon3DVerts[] polies) {
	if (polies == null) return;
	for (Polygon3DVerts poly : polies) {
	    // shading
	    drawPolygon3D(poly);
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
