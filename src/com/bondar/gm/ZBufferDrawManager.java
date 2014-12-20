package com.bondar.gm;

import com.bondar.geom.Point2D;
import com.bondar.geom.Point3DInt;
import com.bondar.geom.Polygon3DVerts;
import com.bondar.geom.Solid2D;
import com.bondar.geom.Vertex3D;
import com.bondar.tasks.Main;
import com.bondar.tools.Mathem;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class ZBufferDrawManager extends DrawManager {
    
    public static final float SCR_VALUE = Float.MAX_VALUE;
    
    private float[][] buffer;
    private boolean isZBuffer;

    public ZBufferDrawManager(int width, int height) {
        super(width, height);
        this.buffer = new float[width][height];
    }
    
    public void drawScene(List<Polygon3DVerts> polies, String viewType, String shadeType, 
            String depthType, boolean isTextured, boolean isNormalsPoly, boolean isNormalsVert,
            boolean isGameViewModeEnabled, Solid2D crosshair) {
        isZBuffer = false;
        if (depthType.equals(Main.RADIO_Z_BUFFER)) {
            isZBuffer = true;
            // clear depth buffer
            clear();
        }
        super.drawScene(polies, viewType, shadeType, depthType, isTextured, isNormalsPoly, isNormalsVert, 
                isGameViewModeEnabled, crosshair);
    }
    
//    public void reset(int width, int height) {
//        this.buffer = new float[width][height];
//    }
    
    public void clear() {
        for (int i = 0; i < buffer.length; i++) {
            for (int j = 0; j < buffer[0].length; j++) {
                buffer[i][j] = SCR_VALUE;
            }
        }
    }

    public void setDepth(int x, int y, float depth) {
        float old = buffer[x][y];
        if (old > depth)
            buffer[x][y] = depth;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // PIXELS
    public void setNoClipBufferedPixel(int x, int y, float z, int u, int v, int w) {
        setNoClipBufferedPixel(x, y, z, new Color(u, v, w));
    }
    
    public void setNoClipBufferedPixel(int x, int y, float z, Color col) {
        if (z < buffer[x][y]) {
            setNoClipPixel(x, y, col);
            buffer[x][y] = z;
        } else {
            return;
        }
    }
    
    public void setBufferedTextel(int x, int y, int z, int u, int v, BufferedImage texture) {
        Color textel = new Color(texture.getRGB(u, v));
        setNoClipBufferedPixel(x, y, z, textel);
    }    
    
    public void setBufferedLightTextel(int x, int y, int z, int u, int v, BufferedImage texture, Color light) {
        Color textel = new Color(texture.getRGB(u, v));
        setNoClipBufferedPixel(x, y, z, LightManager.light(textel, light));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // TRIANGLES
    @Override
    public void drawFlatTriangle2D(Vertex3D[] verts, Color col) {
        
        if (!isZBuffer) {
            // draw without z buffer
            super.drawFlatTriangle2D(verts, col);
            return;
        }
        
        if (verts == null || col == null || verts.length < 3) {
            return;
        }
        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                dz,
                xi, yi, // the current interpolated x,y
                zi, // the current interpolated u,v,w,z
                xStart,
                xEnd,
                yStart,
                yRestart,
                yEnd,
                xl,                 
                dxdyl,              
                xr,
                dxdyr,             
                zl,
                dzdyl,   
                zr,
                dzdyr;

        int x0, y0, tz0,
                x1, y1, tz1,
                x2, y2, tz2;

        Point3DInt[] ints = {
            new Point3DInt(verts[v0].getPosition()),
            new Point3DInt(verts[v1].getPosition()),
            new Point3DInt(verts[v2].getPosition()),
        };

        // first trivial clipping rejection tests 
        if (((ints[v0].getY() < clip.getYMin())
                && (ints[v1].getY() < clip.getYMin())
                && (ints[v2].getY() < clip.getYMin()))
                
                || ((ints[v0].getY() > clip.getYMax())
                && (ints[v1].getY() > clip.getYMax())
                && (ints[v2].getY() > clip.getYMax()))
                
                || ((ints[v0].getX() < clip.getXMin())
                && (ints[v1].getX() < clip.getXMin())
                && (ints[v2].getX() < clip.getXMin()))
                
                || ((ints[v0].getX() > clip.getXMax())
                && (ints[v1].getX() > clip.getXMax())
                && (ints[v2].getX() > clip.getXMax()))) {
            return;
        }

        // sort vertices
        if (ints[v1].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (ints[v2].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (ints[v2].getY() < ints[v1].getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }
        
        // if same
        if (ints[v0].getY() == ints[v1].getY()) {
//            if (ints[v0].getY() == ints[v2].getY()) {
//                // draw line ?!
//                return;
//            }
            type = TRI_TYPE_FLAT_TOP;
            if (ints[v1].getX() < ints[v0].getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (ints[v1].getY() == ints[v2].getY()) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (ints[v2].getX() < ints[v1].getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }

        // extract vertices
        // 0
        x0 = ints[v0].getX();
        y0 = ints[v0].getY();
        tz0 = ints[v0].getZ();
        // 1
        x1 = ints[v1].getX();
        y1 = ints[v1].getY();
        tz1 = ints[v1].getZ();
        // 2
        x2 = ints[v2].getX();
        y2 = ints[v2].getY();
        tz2 = ints[v2].getZ();

        // degenerate triangle
        if (((x0 == x1) && (x1 == x2)) || ((y0 ==  y1) && (y1 == y2))) {
            return;
        }
        
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
                dzdyl = ((tz2 - tz0) << FIXP16_SHIFT) / dy;
                
                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz1) << FIXP16_SHIFT) / dy;

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz1 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();

                } else {
		// no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x1 << FIXP16_SHIFT);

                    zl = (tz0 << FIXP16_SHIFT);
                    zr = (tz1 << FIXP16_SHIFT);

                    // set starting y
                    yStart = y0;
                }
            } else {
                ////////////////////////////////////////////////////////////////
                // FLAT BOTTOM TRIANGLE
                // compute all deltas
                dy = (y1 - y0);

                dxdyl = ((x1 - x0) << FIXP16_SHIFT) / dy;
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dy; 

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dy; 

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();
                } else {
		// no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x0 << FIXP16_SHIFT);

                    zl = (tz0 << FIXP16_SHIFT);
                    zr = (tz0 << FIXP16_SHIFT);

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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        dz = (zr - zl)/dx;
                    } else {
                        dz = (zr - zl);
                    }

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;
                        // slide interpolants over
                        zi += dx * dz;
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
                        setNoClipBufferedPixel(xi, yi, zi, col);
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        dz = (zr - zl) / dx;
                    }
                    else {
                        dz = (zr - zl);
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setNoClipBufferedPixel(xi, yi, zi, col);
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    zr += dzdyr;
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
                dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);
                zl = dzdyl * dyl + (tz1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);
                zr = dzdyr * dyr + (tz0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                zl = (tz0 << FIXP16_SHIFT);
                zr = (tz0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        dz = (zr - zl)/dx;
                    }
                    else {
                        dz = (zr - zl);
                    }

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        zi += dx * dz;

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
                        setNoClipBufferedPixel(xi, yi, zi, col);
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    zr += dzdyr;

                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;  

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            zr+=dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        dz = (zr - zl)/dx;
                    }
                    else {
                        dz = (zr - zl);
                    }
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setNoClipBufferedPixel(xi, yi, zi, col);
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    zr += dzdyr;
                    
                    ////////////////////////////////////////////////////////////
                    // TEST FOR yi HIT IN SECOND REGION (SO CHANGE INTERPOLANT KOEFFICIENTS)
                    if (yi == yRestart) {
                        // test interpolation side change flag
                        if (iRestart == INTERP_LHS) {
                            // LHS
                            dyl = (y2 - y1);

                            dxdyl = ((x2 - x1) << FIXP16_SHIFT) / dyl;
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;   

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            zl+=dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            zr+=dzdyr;
                        }
                    }
                }
            }
        }
    }
 
    @Override
    public void drawGouraudTriangle2D(Vertex3D[] verts, Color[] colors) {
        
        if (!isZBuffer) {
            // draw without z buffer
            super.drawGouraudTriangle2D(verts, colors);
            return;
        }
        
        if (verts == null || colors == null 
                || verts.length < 3 || colors.length < 3) {
            return;
        }
        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                du, dv, dw, dz,
                xi, yi, // the current interpolated x,y
                ui, vi, wi, zi, // the current interpolated u,v,w,z
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
                dzdyl,   
                zl,
                dudyr,
                ur,
                dvdyr,
                vr,
                dwdyr,
                wr,
                dzdyr,
                zr;

        Point3DInt[] ints = {
            new Point3DInt(verts[v0].getPosition()),
            new Point3DInt(verts[v1].getPosition()),
            new Point3DInt(verts[v2].getPosition()),
        };

        // first trivial clipping rejection tests 
        if (((ints[v0].getY() < clip.getYMin())
                && (ints[v1].getY() < clip.getYMin())
                && (ints[v2].getY() < clip.getYMin()))
                
                || ((ints[v0].getY() > clip.getYMax())
                && (ints[v1].getY() > clip.getYMax())
                && (ints[v2].getY() > clip.getYMax()))
                
                || ((ints[v0].getX() < clip.getXMin())
                && (ints[v1].getX() < clip.getXMin())
                && (ints[v2].getX() < clip.getXMin()))
                
                || ((ints[v0].getX() > clip.getXMax())
                && (ints[v1].getX() > clip.getXMax())
                && (ints[v2].getX() > clip.getXMax()))) {
            return;
        }

        // sort vertices
        if (ints[v1].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (ints[v2].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (ints[v2].getY() < ints[v1].getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }
        
        // if same
        if (ints[v0].getY() == ints[v1].getY()) {
            type = TRI_TYPE_FLAT_TOP;
            if (ints[v1].getX() < ints[v0].getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (ints[v1].getY() == ints[v2].getY()) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (ints[v2].getX() < ints[v1].getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }

        // extract colors
        int rBase0 = colors[v0].getRed();
        int gBase0 = colors[v0].getGreen();
        int bBase0 = colors[v0].getBlue();

        int rBase1 = colors[v1].getRed();
        int gBase1 = colors[v1].getGreen();
        int bBase1 = colors[v1].getBlue();

        int rBase2 = colors[v2].getRed();
        int gBase2 = colors[v2].getGreen();
        int bBase2 = colors[v2].getBlue();

        // extract vertices
        // 0
        int x0 = ints[v0].getX();
        int y0 = ints[v0].getY();
        int tu0 = rBase0;
        int tv0 = gBase0;
        int tw0 = bBase0;
        int tz0 = ints[v0].getZ();
        // 1
        int x1 = ints[v1].getX();
        int y1 = ints[v1].getY();
        int tu1 = rBase1;
        int tv1 = gBase1;
        int tw1 = bBase1;
        int tz1 = ints[v1].getZ();
        // 2
        int x2 = ints[v2].getX();
        int y2 = ints[v2].getY();
        int tu2 = rBase2;
        int tv2 = gBase2;
        int tw2 = bBase2;
        int tz2 = ints[v2].getZ();

        // degenerate triangle
        if (((x0 == x1) && (x1 == x2)) || ((y0 ==  y1) && (y1 == y2))) {
            return;
        }
        
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
                dzdyl = ((tz2 - tz0) << FIXP16_SHIFT) / dy;
                
                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu1) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv1) << FIXP16_SHIFT) / dy;
                dwdyr = ((tw2 - tw1) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz1) << FIXP16_SHIFT) / dy;

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    wl = dwdyl * dy + (tw0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu1 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv1 << FIXP16_SHIFT);
                    wr = dwdyr * dy + (tw1 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz1 << FIXP16_SHIFT);

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
                    zl = (tz0 << FIXP16_SHIFT);

                    ur = (tu1 << FIXP16_SHIFT);
                    vr = (tv1 << FIXP16_SHIFT);
                    wr = (tw1 << FIXP16_SHIFT);
                    zr = (tz1 << FIXP16_SHIFT);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dy; 

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dy;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dy; 

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    wl = dwdyl * dy + (tw0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                    wr = dwdyr * dy + (tw0 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);

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
                    zl = (tz0 << FIXP16_SHIFT);

                    ur = (tu0 << FIXP16_SHIFT);
                    vr = (tv0 << FIXP16_SHIFT);
                    wr = (tw0 << FIXP16_SHIFT);
                    zr = (tz0 << FIXP16_SHIFT);

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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                        dz = (zr - zl)/dx;
                    } else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                        dz = (zr - zl);
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
                        zi += dx * dz;
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
                        setNoClipBufferedPixel(xi, yi, zi, ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT);
//                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
//                                vi >> FIXP16_SHIFT, 
//                                wi >> FIXP16_SHIFT));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;
                    zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                        dz = (zr - zl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                        dz = (zr - zl);
                    }

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setNoClipBufferedPixel(xi, yi, zi, ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT);
//                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
//                                vi >> FIXP16_SHIFT, 
//                                wi >> FIXP16_SHIFT));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;
                    zr += dzdyr;
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
                dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);

                ul = dudyl * dyl + (tu1 << FIXP16_SHIFT);
                vl = dvdyl * dyl + (tv1 << FIXP16_SHIFT);
                wl = dwdyl * dyl + (tw1 << FIXP16_SHIFT);
                zl = dzdyl * dyl + (tz1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);

                ur = dudyr * dyr + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dyr + (tv0 << FIXP16_SHIFT);
                wr = dwdyr * dyr + (tw0 << FIXP16_SHIFT);
                zr = dzdyr * dyr + (tz0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dwdyl = Mathem.returnFirst(dwdyr, dwdyr = dwdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    wl = Mathem.returnFirst(wr, wr = wl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tw1 = Mathem.returnFirst(tw2, tw2 = tw1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                wl = dwdyl * dy + (tw0 << FIXP16_SHIFT);
                zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                wr = dwdyr * dy + (tw0 << FIXP16_SHIFT);
                zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dwdyl = Mathem.returnFirst(dwdyr, dwdyr = dwdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    wl = Mathem.returnFirst(wr, wr = wl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tw1 = Mathem.returnFirst(tw2, tw2 = tw1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dwdyr = ((tw2 - tw0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                ul = (tu0 << FIXP16_SHIFT);
                vl = (tv0 << FIXP16_SHIFT);
                wl = (tw0 << FIXP16_SHIFT);
                zl = (tz0 << FIXP16_SHIFT);

                ur = (tu0 << FIXP16_SHIFT);
                vr = (tv0 << FIXP16_SHIFT);
                wr = (tw0 << FIXP16_SHIFT);
                zr = (tz0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dwdyl = Mathem.returnFirst(dwdyr, dwdyr = dwdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    wl = Mathem.returnFirst(wr, wr = wl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tw1 = Mathem.returnFirst(tw2, tw2 = tw1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                        dz = (zr - zl)/dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                        dz = (zr - zl);
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
                        zi += dx * dz;

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
                        setNoClipBufferedPixel(xi, yi, zi, ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT);
//                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
//                                vi >> FIXP16_SHIFT, 
//                                wi >> FIXP16_SHIFT));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;
                    zr += dzdyr;

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
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;  

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            wl = (tw1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            wl += dwdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dwdyr = ((tw1 - tw2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            wr = (tw2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            wr += dwdyr;
                            zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dw = (wr - wl) / dx;
                        dz = (zr - zl)/dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dw = (wr - wl);
                        dz = (zr - zl);
                    }
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setNoClipBufferedPixel(xi, yi, zi, ui >> FIXP16_SHIFT, 
                                vi >> FIXP16_SHIFT, 
                                wi >> FIXP16_SHIFT);
//                        drawNoClipPoint(xi, yi, new Color(ui >> FIXP16_SHIFT, 
//                                vi >> FIXP16_SHIFT, 
//                                wi >> FIXP16_SHIFT));
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        wi += dw;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    wl += dwdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    wr += dwdyr;
                    zr += dzdyr;
                    
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
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;   

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            wl = (tw1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            wl += dwdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dwdyr = ((tw1 - tw2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            wr = (tw2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            wr += dwdyr;
                            zr+=dzdyr;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void drawTexturedConstTriangle(Vertex3D[] verts, BufferedImage texture, Point2D[] texturePoints) {
        
        if (!isZBuffer) {
            // draw without z buffer
            super.drawTexturedConstTriangle(verts, texture, texturePoints);
            return;
        }
        
        if (verts == null || verts.length < 3
                || texture == null || texturePoints == null) {
            return;
        }

        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                du, dv, dz,
                xi, yi, // the current interpolated x,y
                ui, vi, zi, // the current interpolated u,v,z
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
                vr,
                dzdyl,   
                zl,
                dzdyr,
                zr;
        
        int x0, y0, tu0, tv0, tz0, // cached vertices
                x1, y1, tu1, tv1, tz1,
                x2, y2, tu2, tv2, tz2;
        
                Point3DInt[] ints = {
            new Point3DInt(verts[v0].getPosition()),
            new Point3DInt(verts[v1].getPosition()),
            new Point3DInt(verts[v2].getPosition()),
        };

        // first trivial clipping rejection tests 
        if (((ints[v0].getY() < clip.getYMin())
                && (ints[v1].getY() < clip.getYMin())
                && (ints[v2].getY() < clip.getYMin()))
                
                || ((ints[v0].getY() > clip.getYMax())
                && (ints[v1].getY() > clip.getYMax())
                && (ints[v2].getY() > clip.getYMax()))
                
                || ((ints[v0].getX() < clip.getXMin())
                && (ints[v1].getX() < clip.getXMin())
                && (ints[v2].getX() < clip.getXMin()))
                
                || ((ints[v0].getX() > clip.getXMax())
                && (ints[v1].getX() > clip.getXMax())
                && (ints[v2].getX() > clip.getXMax()))) {
            return;
        }

        // sort vertices
        if (ints[v1].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (ints[v2].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (ints[v2].getY() < ints[v1].getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }
        
        // if same
        if (ints[v0].getY() == ints[v1].getY()) {
//            if (ints[v0].getY() == ints[v2].getY()) {
//                // draw line ?!
//                return;
//            }
            type = TRI_TYPE_FLAT_TOP;
            if (ints[v1].getX() < ints[v0].getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (ints[v1].getY() == ints[v2].getY()) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (ints[v2].getX() < ints[v1].getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }
        
        Point2D tp0 = texturePoints[v0],
                tp1 = texturePoints[v1],
                tp2 = texturePoints[v2];

        // extract vertices for processing, now that we have order
        x0 = ints[v0].getX();
        y0 = ints[v0].getY();
        tu0 = (int)tp0.getX();
        tv0 = (int)tp0.getY();
        tz0 = ints[v0].getZ();

        x1 = ints[v1].getX();
        y1 = ints[v1].getY();
        tu1 = (int)tp1.getX();
        tv1 = (int)tp1.getY();
        tz1 = ints[v1].getZ();

        x2 = ints[v2].getX();
        y2 = ints[v2].getY();
        tu2 = (int)tp2.getX();
        tv2 = (int)tp2.getY();
        tz2 = ints[v2].getZ();

        // degenerate triangle
        if (((x0 == x1) && (x1 == x2)) || ((y0 == y1) && (y1 == y2))) {
            return;
        }

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
                dzdyl = ((tz2 - tz0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu1) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv1) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz1) << FIXP16_SHIFT) / dy;
                
                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu1 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv1 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz1 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();

                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x1 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);
                    zl = (tz0 << FIXP16_SHIFT);

                    ur = (tu1 << FIXP16_SHIFT);
                    vr = (tv1 << FIXP16_SHIFT);
                    zr = (tz1 << FIXP16_SHIFT);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dy; 

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dy; 

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                  xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();
                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x0 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);
                    zl = (tz0 << FIXP16_SHIFT);

                    ur = (tu0 << FIXP16_SHIFT);
                    vr = (tv0 << FIXP16_SHIFT);
                    zr = (tz0 << FIXP16_SHIFT);

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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl)/dx;
                    } else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                    }
                    
                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;
                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        zi += dx * dz;
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
                        setBufferedTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                   }

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setBufferedTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;
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
                dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);
                ul = dudyl * dyl + (tu1 << FIXP16_SHIFT);
                vl = dvdyl * dyl + (tv1 << FIXP16_SHIFT);
                zl = dzdyl * dyl + (tz1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);
                ur = dudyr * dyr + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dyr + (tv0 << FIXP16_SHIFT);
                zr = dzdyr * dyr + (tz0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);


                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                ul = (tu0 << FIXP16_SHIFT);
                vl = (tv0 << FIXP16_SHIFT);
                zl = (tz0 << FIXP16_SHIFT);

                ur = (tu0 << FIXP16_SHIFT);
                vr = (tv0 << FIXP16_SHIFT);
                zr = (tz0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl)/dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                    }

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        zi += dx * dz;

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
                        setBufferedTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;

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
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;  

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl)/dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                    }
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setBufferedTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;
                    
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
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;   

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            zr+=dzdyr;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void drawTexturedFlatTriangle(Vertex3D[] verts, BufferedImage texture, Point2D[] texturePoints, Color lightColor) {
        
        if (!isZBuffer) {
            // draw without z buffer
            super.drawTexturedFlatTriangle(verts, texture, texturePoints, lightColor);
            return;
        }
        
        if (verts == null || verts.length < 3
                || texture == null || texturePoints == null) {
            return;
        }

        int v0 = 0,
                v1 = 1,
                v2 = 2,
                type = TRI_TYPE_NONE,
                iRestart = INTERP_LHS;

        int dx, dy, dyl, dyr, // general deltas
                du, dv, dz,
                xi, yi, // the current interpolated x,y
                ui, vi, zi, // the current interpolated u,v,z
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
                vr,
                dzdyl,   
                zl,
                dzdyr,
                zr;
        
        int x0, y0, tu0, tv0, tz0, // cached vertices
                x1, y1, tu1, tv1, tz1,
                x2, y2, tu2, tv2, tz2;
        
        Point3DInt[] ints = {
            new Point3DInt(verts[v0].getPosition()),
            new Point3DInt(verts[v1].getPosition()),
            new Point3DInt(verts[v2].getPosition()),
        };

        // first trivial clipping rejection tests 
        if (((ints[v0].getY() < clip.getYMin())
                && (ints[v1].getY() < clip.getYMin())
                && (ints[v2].getY() < clip.getYMin()))
                
                || ((ints[v0].getY() > clip.getYMax())
                && (ints[v1].getY() > clip.getYMax())
                && (ints[v2].getY() > clip.getYMax()))
                
                || ((ints[v0].getX() < clip.getXMin())
                && (ints[v1].getX() < clip.getXMin())
                && (ints[v2].getX() < clip.getXMin()))
                
                || ((ints[v0].getX() > clip.getXMax())
                && (ints[v1].getX() > clip.getXMax())
                && (ints[v2].getX() > clip.getXMax()))) {
            return;
        }

        // sort vertices
        if (ints[v1].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v1, v1 = v0);
        }
        if (ints[v2].getY() < ints[v0].getY()) {
            v0 = Mathem.returnFirst(v2, v2 = v0);
        }
        if (ints[v2].getY() < ints[v1].getY()) {
            v1 = Mathem.returnFirst(v2, v2 = v1);
        }
        
        // if same
        if (ints[v0].getY() == ints[v1].getY()) {
//            if (ints[v0].getY() == ints[v2].getY()) {
//                // draw line ?!
//                return;
//            }
            type = TRI_TYPE_FLAT_TOP;
            if (ints[v1].getX() < ints[v0].getX()) {
                v0 = Mathem.returnFirst(v1, v1 = v0);
            }
        } else // now test for trivial flat sided cases
        if (ints[v1].getY() == ints[v2].getY()) {
            type = TRI_TYPE_FLAT_BOTTOM;
            if (ints[v2].getX() < ints[v1].getX()) {
                v1 = Mathem.returnFirst(v2, v2 = v1);
            }
        } else {
            type = TRI_TYPE_GENERAL;
        }
        
        Point2D tp0 = texturePoints[v0],
                tp1 = texturePoints[v1],
                tp2 = texturePoints[v2];

        // extract vertices for processing, now that we have order
        x0 = ints[v0].getX();
        y0 = ints[v0].getY();
        tu0 = (int)tp0.getX();
        tv0 = (int)tp0.getY();
        tz0 = ints[v0].getZ();

        x1 = ints[v1].getX();
        y1 = ints[v1].getY();
        tu1 = (int)tp1.getX();
        tv1 = (int)tp1.getY();
        tz1 = ints[v1].getZ();

        x2 = ints[v2].getX();
        y2 = ints[v2].getY();
        tu2 = (int)tp2.getX();
        tv2 = (int)tp2.getY();
        tz2 = ints[v2].getZ();

        // degenerate triangle
        if (((x0 == x1) && (x1 == x2)) || ((y0 == y1) && (y1 == y2))) {
            return;
        }

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
                dzdyl = ((tz2 - tz0) << FIXP16_SHIFT) / dy;

                dxdyr = ((x2 - x1) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu1) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv1) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz1) << FIXP16_SHIFT) / dy;
                
                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                    xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x1 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu1 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv1 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz1 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();

                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x1 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);
                    zl = (tz0 << FIXP16_SHIFT);

                    ur = (tu1 << FIXP16_SHIFT);
                    vr = (tv1 << FIXP16_SHIFT);
                    zr = (tz1 << FIXP16_SHIFT);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dy; 

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dy;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dy;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dy;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dy; 

                // test for y clipping
                if (y0 < clip.getYMin()) {
                    // compute overclip
                    dy = (clip.getYMin() - y0);

                    // computer new LHS starting values
                  xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                    ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                    vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                    zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                    // compute new RHS starting values
                    xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                    ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                    vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                    zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);

                    // compute new starting y
                    yStart = clip.getYMin();
                } else {
                    // no clipping
                    // set starting values
                    xl = (x0 << FIXP16_SHIFT);
                    xr = (x0 << FIXP16_SHIFT);

                    ul = (tu0 << FIXP16_SHIFT);
                    vl = (tv0 << FIXP16_SHIFT);
                    zl = (tz0 << FIXP16_SHIFT);

                    ur = (tu0 << FIXP16_SHIFT);
                    vr = (tv0 << FIXP16_SHIFT);
                    zr = (tz0 << FIXP16_SHIFT);

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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl)/dx;
                    } else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                    }
                    
                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;
                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        zi += dx * dz;
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
                        setBufferedLightTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture, lightColor);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl) / dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                   }

                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN FLAT TOP/BOTTOM TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setBufferedLightTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture, lightColor);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;
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
                dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dyr = (clip.getYMin() - y0);
                dyl = (clip.getYMin() - y1);

                // computer new LHS starting values
                xl = dxdyl * dyl + (x1 << FIXP16_SHIFT);
                ul = dudyl * dyl + (tu1 << FIXP16_SHIFT);
                vl = dvdyl * dyl + (tv1 << FIXP16_SHIFT);
                zl = dzdyl * dyl + (tz1 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dyr + (x0 << FIXP16_SHIFT);
                ur = dudyr * dyr + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dyr + (tv0 << FIXP16_SHIFT);
                zr = dzdyr * dyr + (tz0 << FIXP16_SHIFT);

                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr > dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;  

                // compute overclip
                dy = (clip.getYMin() - y0);

                // computer new LHS starting values
                xl = dxdyl * dy + (x0 << FIXP16_SHIFT);
                ul = dudyl * dy + (tu0 << FIXP16_SHIFT);
                vl = dvdyl * dy + (tv0 << FIXP16_SHIFT);
                zl = dzdyl * dy + (tz0 << FIXP16_SHIFT);

                // compute new RHS starting values
                xr = dxdyr * dy + (x0 << FIXP16_SHIFT);
                ur = dudyr * dy + (tu0 << FIXP16_SHIFT);
                vr = dvdyr * dy + (tv0 << FIXP16_SHIFT);
                zr = dzdyr * dy + (tz0 << FIXP16_SHIFT);


                // compute new starting y
                yStart = clip.getYMin();

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);

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
                dzdyl = ((tz1 - tz0) << FIXP16_SHIFT) / dyl; 

                // RHS
                dyr = (y2 - y0);

                dxdyr = ((x2 - x0) << FIXP16_SHIFT) / dyr;
                dudyr = ((tu2 - tu0) << FIXP16_SHIFT) / dyr;
                dvdyr = ((tv2 - tv0) << FIXP16_SHIFT) / dyr;
                dzdyr = ((tz2 - tz0) << FIXP16_SHIFT) / dyr;

		// no clipping y
                // set starting values
                xl = (x0 << FIXP16_SHIFT);
                xr = (x0 << FIXP16_SHIFT);

                ul = (tu0 << FIXP16_SHIFT);
                vl = (tv0 << FIXP16_SHIFT);
                zl = (tz0 << FIXP16_SHIFT);

                ur = (tu0 << FIXP16_SHIFT);
                vr = (tv0 << FIXP16_SHIFT);
                zr = (tz0 << FIXP16_SHIFT);

                // set starting y
                yStart = y0;

                // test if we need swap to keep rendering left to right
                if (dxdyr < dxdyl) {
                    dxdyl = Mathem.returnFirst(dxdyr, dxdyr = dxdyl);
                    dudyl = Mathem.returnFirst(dudyr, dudyr = dudyl);
                    dvdyl = Mathem.returnFirst(dvdyr, dvdyr = dvdyl);
                    dzdyl = Mathem.returnFirst(dzdyr, dzdyr = dzdyl);
                    xl = Mathem.returnFirst(xr, xr = xl);
                    ul = Mathem.returnFirst(ur, ur = ul);
                    vl = Mathem.returnFirst(vr, vr = vl);
                    zl = Mathem.returnFirst(zr, zr = zl);
                    x1 = Mathem.returnFirst(x2, x2 = x1);
                    y1 = Mathem.returnFirst(y2, y2 = y1);
                    tu1 = Mathem.returnFirst(tu2, tu2 = tu1);
                    tv1 = Mathem.returnFirst(tv2, tv2 = tv1);
                    tz1 = Mathem.returnFirst(tz2, tz2 = tz1);
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl)/dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                    }

                    ////////////////////////////////////////////////////////////
                    // CLIP LEFT
                    if (xStart < clip.getXMin()) {
                        // compute x overlap
                        dx = clip.getXMin() - xStart;

                        // slide interpolants over
                        ui += dx * du;
                        vi += dx * dv;
                        zi += dx * dz;

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
                        setBufferedLightTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture, lightColor);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;

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
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;  

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            zr += dzdyr;
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
                    zi = zl + FIXP16_ROUND_UP;

                    // compute u,v interpolants
                    if ((dx = (xEnd - xStart)) > 0) {
                        du = (ur - ul) / dx;
                        dv = (vr - vl) / dx;
                        dz = (zr - zl)/dx;
                    }
                    else {
                        du = (ur - ul);
                        dv = (vr - vl);
                        dz = (zr - zl);
                    }
                    ////////////////////////////////////////////////////////////
                    // DRAW NON-CLIPPED LINE IN GENERAL TRIANGLE
                    for (xi = xStart; xi <= xEnd; xi++) {
                        setBufferedLightTextel(xi, yi, zi, ui >> FIXP16_SHIFT, vi >> FIXP16_SHIFT, texture, lightColor);
                        // interpolate u,v
                        ui += du;
                        vi += dv;
                        zi += dz;
                    }
                    // interpolate u,v,w,x along right and left edge
                    xl += dxdyl;
                    ul += dudyl;
                    vl += dvdyl;
                    zl += dzdyl;

                    xr += dxdyr;
                    ur += dudyr;
                    vr += dvdyr;
                    zr += dzdyr;
                    
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
                            dzdyl = ((tz2 - tz1) << FIXP16_SHIFT) / dyl;   

                            // set starting values
                            xl = (x1 << FIXP16_SHIFT);
                            ul = (tu1 << FIXP16_SHIFT);
                            vl = (tv1 << FIXP16_SHIFT);
                            zl = (tz1 << FIXP16_SHIFT);

                            // interpolate down on LHS to even up
                            xl += dxdyl;
                            ul += dudyl;
                            vl += dvdyl;
                            zl += dzdyl;
                        }
                        else {
                            // RHS
                            dyr = (y1 - y2);

                            dxdyr = ((x1 - x2) << FIXP16_SHIFT) / dyr;
                            dudyr = ((tu1 - tu2) << FIXP16_SHIFT) / dyr;
                            dvdyr = ((tv1 - tv2) << FIXP16_SHIFT) / dyr;
                            dzdyr = ((tz1 - tz2) << FIXP16_SHIFT) / dyr;   

                            // set starting values
                            xr = (x2 << FIXP16_SHIFT);
                            ur = (tu2 << FIXP16_SHIFT);
                            vr = (tv2 << FIXP16_SHIFT);
                            zr = (tz2 << FIXP16_SHIFT);

                            // interpolate down on RHS to even up
                            xr += dxdyr;
                            ur += dudyr;
                            vr += dvdyr;
                            zr+=dzdyr;
                        }
                    }
                }
            }
        }
    }
    
}
