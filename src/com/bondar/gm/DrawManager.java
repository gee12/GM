package com.bondar.gm;

import com.bondar.geom.ClipBox2D;
import com.bondar.geom.Point2D;
import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3D;
import com.bondar.geom.Polygon3DInds;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Vertex3D;
import com.bondar.tools.Mathem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author truebondar
 */
public class DrawManager {
    
    private static int TONE = 0;
    public static Color BACK_COLOR = new Color(TONE,TONE,TONE);
    public static enum RasterizerModes {
        ACCURATE,
        FAST,
        FASTEST
    }

    private Graphics g;
    private int width, height;
    private ClipBox2D clip;
    private RasterizerModes rasterMode;

    //////////////////////////////////////////////////
    public DrawManager() {
        rasterMode = RasterizerModes.FAST;
    }
    
    //////////////////////////////////////////////////
    public void setGraphics(Graphics g) {
	this.g = g;
        
        // ?????!!!!
	this.width = g.getClipBounds().width;
	this.height = g.getClipBounds().height;

	drawBackground(BACK_COLOR);
        
        //BufferedImage bufferedImage = new BufferedImage(1,1,BufferedImage.TYPE_INT_BGR);
        //bufferedImage.setR
    }
    
    public void setClipBox(Dimension dim) {
        if (dim == null) return;
        clip = new ClipBox2D(0, 0, dim.getWidth(), dim.getHeight());
    }
    
    public void setColor(Color color) {
	g.setColor(color);
    }  
    
    /////////////////////////////////////////////////////
    public void drawBackground(Color col) {
	g.setColor(col);
	g.fillRect(0, 0, width, height);
	g.setColor(Color.BLACK);
    }    
    
    /////////////////////////////////////////////////////
    // Алгоритм художника
    public void painterAlgorithm(Polygon3DVerts[] polies) {
	Polygon3DVerts[] sortPolies = sortTrianglesByZ(polies);
	if (sortPolies == null) {
	    return;
	}
	for (Polygon3DVerts poly : sortPolies) {
	    drawFilledPolygon3D(poly);
	}
    }
    
    public void drawFilledPolygon3D(Polygon3DVerts poly) {
	g.setColor(poly.getShadeColor());
	switch(poly.getType()) {
	    case POINT:
		drawPoint(poly.getVertexPosition(0));
		break;
	    case LINE:
		drawLine(poly.getVertexPosition(0), poly.getVertexPosition(1));
		break;
	    default:
		drawFilledPolygon(poly.getVertexes());
		break;
	}
    }
    
    /////////////////////////////////////////////////////
    // draw point
    public void drawPoint(Point3D p) {
	if (p == null) return;
	drawPoint(p.getX(), p.getY());
    }

    public void drawPoint(double x, double y) {
	g.drawLine((int)(x + 0.5), (int)(y + 0.5),
		(int)(x + 0.5), (int)(y + 0.5));	    
     }
    
    public void drawPoint(Point3D p, Color col) {
	if (p == null) return;
	drawPoint(p.getX(), p.getY(), col);
    }

    public void drawPoint(double x, double y, Color col) {
	g.setColor(col);
	g.drawLine((int)(x + 0.5), (int)(y + 0.5),
		(int)(x + 0.5), (int)(y + 0.5));	    
    }

    /////////////////////////////////////////////////////
    // draw line
    public void drawLine(Point3D p1, Point3D p2) {
	if (p1 == null || p2 == null) return;
	drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public void drawLine(Point2D p1, Point2D p2) {
	if (p1 == null || p2 == null) return;
	drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }  
    
    public void drawLine(double x1, double y1, double x2, double y2) {
	g.drawLine((int)(x1 + 0.5), (int)(y1 + 0.5), (int)(x2 + 0.5), (int)(y2 + 0.5));
    }
    
    /////////////////////////////////////////////////////
    // draw filled polygon
    public void drawFilledPolygon(Vertex3D[] verts) {
	if (verts == null) return;
	int size = verts.length;
	int xs[] = new int[size];
	int ys[] = new int[size];
	for (int i = 0; i < size; i++) {
	    if (verts[i] == null) continue;
	    xs[i] = (int)(verts[i].getPosition().getX() + 0.5);
	    ys[i] = (int)(verts[i].getPosition().getY() + 0.5);
	}
	g.fillPolygon(xs, ys, size);
    }
    
    public void drawPolygonBorder(Polygon3DVerts poly) {
        if (poly == null) return;
        g.setColor(poly.getBorderColor());
        drawPolygonBorder(poly.getVertexes());
    }
    
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
    // Сортировка граней по координате Z
    public static Polygon3DVerts[] sortTrianglesByZ(Polygon3DVerts[] polies) {
	if (polies == null) {
	    return null;
	}
	int size = polies.length;
	double[] dists = new double[size];
	int[] indexes = new int[size];
	// нахождение средней величины Z - удаленности грани
	for (int i = 0; i < size; i++) {
	    Polygon3DVerts poly = polies[i];
	    dists[i] = poly.averageZ();
	    indexes[i] = i;
	}
	Polygon3DVerts[] res = new Polygon3DVerts[size];
	// сортировка граней по удаленности
	for (int i = 0; i < size - 1; i++) {
	    for (int j = 0; j < size - 1; j++) {
		if (dists[j] < dists[j + 1]) {
		    double distTemp = dists[j];
		    dists[j] = dists[j + 1];
		    dists[j + 1] = distTemp;

		    int indTemp = indexes[j];
		    indexes[j] = indexes[j + 1];
		    indexes[j + 1] = indTemp;
		}
	    }
	}
	for (int i = 0; i < size; i++) {
	    res[i] = polies[indexes[i]].getCopy();
	}
	return res;
    }
    
    /////////////////////////////////////////////////////
    // draw a triangle it decomposes all triangles into a pair of flat top, flat bottom
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
        /*if ( y3 < clip.getYMin() || y1 > clip.getYMax() ||
             (x1 < clip.getXMin() && x2 < clip.getXMin() && x3 < clip.getXMin()) ||
             (x1 > clip.getXMax() && x2 > clip.getXMax() && x3 > clip.getXMax()) )
             return;*/
        
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
                         double x3, double y3, Color col)
    {
        int iy1 = 0, iy3 = 0;

// cast dest buffer to ushort
//USHORT *dest_buffer = (USHORT *)_dest_buffer;
// destination address of next scanline
//USHORT  *dest_addr = NULL;
// recompute mempitch in 16-bit words
//mempitch = (mempitch >> 1);
// test order of x1 and x2
        if (x3 < x2) {
            x2 = Mathem.returnFirst(x3, x3 = x2);
        }

        // compute delta's
        double height = y3 - y1;    // the height of the triangle

        double dx_left = (x2 - x1) / height;  // the dx/dy ratio of the left edge of line
        double dx_right = (x3 - x1) / height;  // the dx/dy ratio of the right edge of line

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

        // compute starting address in video memory
        //dest_addr = dest_buffer + iy1*mempitch;
        // test if x clipping is needed
        if (x1 >= clip.getXMin() && x1 <= clip.getXMax()
                && x2 >= clip.getXMin() && x2 <= clip.getXMax()
                && x3 >= clip.getXMin() && x3 <= clip.getXMax()) {
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++/*, dest_addr+=mempitch*/) {
                // draw the line
                //Mem_Set_WORD(dest_addr+(unsigned int)(xs),color,(unsigned int)((int)xe-(int)xs+1));

                // adjust starting point and ending point
                xs += dx_left;
                xe += dx_right;
            } // end for

        } // end if no x clipping needed
        else {
            // clip x axis with slower version
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++/*,dest_addr+=mempitch*/) {
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
                //Mem_Set_WORD(dest_addr+(unsigned int)(left),color,(unsigned int)((int)right-(int)left+1));
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
        double height = y3 - y1; // the height of the triangle

        double dx_left = (x3 - x1) / height;   // the dx/dy ratio of the right edge of line
        double dx_right = (x3 - x2) / height;   // the dx/dy ratio of the left edge of line

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

        // compute starting address in video memory
        //dest_addr = dest_buffer+iy1*mempitch;
        // test if no x clipping is needed
        if (x1 >= clip.getXMin() && x1 <= clip.getXMax()
                && x2 >= clip.getXMin() && x2 <= clip.getXMax()
                && x3 >= clip.getXMin() && x3 <= clip.getXMax()) {
            // draw the triangle
            for (int loop_y = iy1; loop_y <= iy3; loop_y++/*,dest_addr+=mempitch*/) {
                // draw the line
                //memset((UCHAR *)dest_addr+(unsigned int)xs, color,(unsigned int)((int)xe-(int)xs+1));

                // adjust starting point and ending point
                xs += dx_left;
                xe += dx_right;
            }

        } else {
            // clip x axis with slower version

            // draw the triangle
            for (int temp_y = iy1; temp_y <= iy3; temp_y++/*,dest_addr+=mempitch*/) {
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
            }
        }
    }
    
}
