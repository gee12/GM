package com.bondar.geom;

/**
 *
 * @author truebondar
 */
public class ClipBox3D {
    
    // 3d clipping planes
    // if view volume is NOT 90 degree then general 3d clipping
    // must be employed
    double nearClipZ;
    double farClipZ; 
    Plane3D rtPlane;
    Plane3D ltPlane;
    Plane3D tpPlane;
    Plane3D btPlane;

    public ClipBox3D() {
	nearClipZ = farClipZ = 0;
	rtPlane = new Plane3D();
	ltPlane = new Plane3D();
	tpPlane = new Plane3D();
	btPlane = new Plane3D();
    }

    // set
    public void setNearClipZ(double nearClipZ) {
	this.nearClipZ = nearClipZ;
    }

    public void setFarClipZ(double farClipZ) {
	this.farClipZ = farClipZ;
    }

    public void setRtPlane(Point3D p, Vector3D v, boolean needNormalize) {
	rtPlane.setPlane3D(p, v, needNormalize);
    }

    public void setLtPlane(Point3D p, Vector3D v, boolean needNormalize) {
	ltPlane.setPlane3D(p, v, needNormalize);
    }

    public void setTpPlane(Point3D p, Vector3D v, boolean needNormalize) {
	tpPlane.setPlane3D(p, v, needNormalize);
    }

    public void setBtPlane(Point3D p, Vector3D v, boolean needNormalize) {
	btPlane.setPlane3D(p, v, needNormalize);
    }

    // get
    public double getNearClipZ() {
	return nearClipZ;
    }

    public double getFarClipZ() {
	return farClipZ;
    }

    public Plane3D getRtPlane() {
	return rtPlane;
    }

    public Plane3D getLtPlane() {
	return ltPlane;
    }

    public Plane3D getTpPlane() {
	return tpPlane;
    }

    public Plane3D getBtPlane() {
	return btPlane;
    }
    
    /*
void Clip_Polys_RENDERLIST4DV2(RENDERLIST4DV2_PTR rend_list, CAM4DV1_PTR cam, int clip_flags)
{
// this function clips the polygons in the list against the requested clipping planes
// and sets the clipped flag on the poly, so it's not rendered
// note the function ONLY performs clipping on the near and far clipping plane
// but will perform trivial tests on the top/bottom, left/right clipping planes
// if a polygon is completely out of the viewing frustrum in these cases, it will
// be culled, however, this test isn't as effective on games based on objects since
// in most cases objects that are visible have polygons that are visible, but in the
// case where the polygon list is based on a large object that ALWAYS has some portion
// visible, testing for individual polys is worthwhile..
// the function assumes the polygons have been transformed into camera space

// internal clipping codes
#define CLIP_CODE_GZ   0x0001    // z > z_max
#define CLIP_CODE_LZ   0x0002    // z < z_min
#define CLIP_CODE_IZ   0x0004    // z_min < z < z_max

#define CLIP_CODE_GX   0x0001    // x > x_max
#define CLIP_CODE_LX   0x0002    // x < x_min
#define CLIP_CODE_IX   0x0004    // x_min < x < x_max

#define CLIP_CODE_GY   0x0001    // y > y_max
#define CLIP_CODE_LY   0x0002    // y < y_min
#define CLIP_CODE_IY   0x0004    // y_min < y < y_max

#define CLIP_CODE_NULL 0x0000

int vertex_ccodes[3]; // used to store clipping flags
int num_verts_in;     // number of vertices inside
int v0, v1, v2;       // vertex indices

float z_factor,       // used in clipping computations
      z_test;         // used in clipping computations

float xi, yi, x01i, y01i, x02i, y02i, // vertex intersection points
      t1, t2,                         // parametric t values
      ui, vi, u01i, v01i, u02i, v02i; // texture intersection points

int last_poly_index,            // last valid polygon in polylist
    insert_poly_index;          // the current position new polygons are inserted at

VECTOR4D u,v,n;                 // used in vector calculations

POLYF4DV2 temp_poly;            // used when we need to split a poly into 2 polys

// set last, current insert index to end of polygon list
// we don't want to clip poly's two times
insert_poly_index = last_poly_index = rend_list->num_polys;

// traverse polygon list and clip/cull polygons
for (int poly = 0; poly < last_poly_index; poly++)
    {
    // acquire current polygon
    POLYF4DV2_PTR curr_poly = rend_list->poly_ptrs[poly];

    // is this polygon valid?
    // test this polygon if and only if it's not clipped, not culled,
    // active, and visible and not 2 sided. Note we test for backface in the event that
    // a previous call might have already determined this, so why work
    // harder!
    if ((curr_poly==NULL) || !(curr_poly->state & POLY4DV2_STATE_ACTIVE) ||
        (curr_poly->state & POLY4DV2_STATE_CLIPPED ) || 
        (curr_poly->state & POLY4DV2_STATE_BACKFACE) )
        continue; // move onto next poly
           
       // clip/cull to x-planes       
       if (clip_flags & CLIP_POLY_X_PLANE)
           {
           // clip/cull only based on x clipping planes
           // for each vertice determine if it's in the clipping region or beyond it and
           // set the appropriate clipping code
           // we do NOT clip the final triangles, we are only trying to trivally reject them 
           // we are going to clip polygons in the rasterizer to the screen rectangle
           // but we do want to clip/cull polys that are totally outside the viewfrustrum

           // since we are clipping to the right/left x-planes we need to use the FOV or
           // the plane equations to find the z value that at the current x position would
           // be outside the plane
           z_factor = (0.5)*cam->viewplane_width/cam->view_dist;  

           // vertex 0

           z_test = z_factor*curr_poly->tvlist[0].z;

           if (curr_poly->tvlist[0].x > z_test)
              vertex_ccodes[0] = CLIP_CODE_GX;
           else
           if (curr_poly->tvlist[0].x < -z_test)
              vertex_ccodes[0] = CLIP_CODE_LX;
           else
              vertex_ccodes[0] = CLIP_CODE_IX;
          
           // vertex 1

           z_test = z_factor*curr_poly->tvlist[1].z;         

           if (curr_poly->tvlist[1].x > z_test)
              vertex_ccodes[1] = CLIP_CODE_GX;
           else
           if (curr_poly->tvlist[1].x < -z_test)
              vertex_ccodes[1] = CLIP_CODE_LX;
           else
              vertex_ccodes[1] = CLIP_CODE_IX;

           // vertex 2

           z_test = z_factor*curr_poly->tvlist[2].z;              

           if (curr_poly->tvlist[2].x > z_test)
              vertex_ccodes[2] = CLIP_CODE_GX;
           else
           if (curr_poly->tvlist[2].x < -z_test)
              vertex_ccodes[2] = CLIP_CODE_LX;
           else
              vertex_ccodes[2] = CLIP_CODE_IX;
           
          // test for trivial rejections, polygon completely beyond right or left
          // clipping planes
          if ( ((vertex_ccodes[0] == CLIP_CODE_GX) && 
                (vertex_ccodes[1] == CLIP_CODE_GX) && 
                (vertex_ccodes[2] == CLIP_CODE_GX) ) ||

               ((vertex_ccodes[0] == CLIP_CODE_LX) && 
                (vertex_ccodes[1] == CLIP_CODE_LX) && 
                (vertex_ccodes[2] == CLIP_CODE_LX) ) )

             {
             // clip the poly completely out of frustrum
             SET_BIT(curr_poly->state, POLY4DV2_STATE_CLIPPED);
             
             // move on to next polygon
             continue;
             } // end if
                           
          } // end if x planes

       // clip/cull to y-planes       
       if (clip_flags & CLIP_POLY_Y_PLANE)
           {
           // clip/cull only based on y clipping planes
           // for each vertice determine if it's in the clipping region or beyond it and
           // set the appropriate clipping code
           // we do NOT clip the final triangles, we are only trying to trivally reject them 
           // we are going to clip polygons in the rasterizer to the screen rectangle
           // but we do want to clip/cull polys that are totally outside the viewfrustrum

           // since we are clipping to the top/bottom y-planes we need to use the FOV or
           // the plane equations to find the z value that at the current y position would
           // be outside the plane
           z_factor = (0.5)*cam->viewplane_width/cam->view_dist;  

           // vertex 0
           z_test = z_factor*curr_poly->tvlist[0].z;

           if (curr_poly->tvlist[0].y > z_test)
              vertex_ccodes[0] = CLIP_CODE_GY;
           else
           if (curr_poly->tvlist[0].y < -z_test)
              vertex_ccodes[0] = CLIP_CODE_LY;
           else
              vertex_ccodes[0] = CLIP_CODE_IY;
          
           // vertex 1
           z_test = z_factor*curr_poly->tvlist[1].z;         

           if (curr_poly->tvlist[1].y > z_test)
              vertex_ccodes[1] = CLIP_CODE_GY;
           else
           if (curr_poly->tvlist[1].y < -z_test)
              vertex_ccodes[1] = CLIP_CODE_LY;
           else
              vertex_ccodes[1] = CLIP_CODE_IY;

           // vertex 2
           z_test = z_factor*curr_poly->tvlist[2].z;              

           if (curr_poly->tvlist[2].y > z_test)
              vertex_ccodes[2] = CLIP_CODE_GY;
           else
           if (curr_poly->tvlist[2].x < -z_test)
              vertex_ccodes[2] = CLIP_CODE_LY;
           else
              vertex_ccodes[2] = CLIP_CODE_IY;
           
          // test for trivial rejections, polygon completely beyond top or bottom
          // clipping planes
          if ( ((vertex_ccodes[0] == CLIP_CODE_GY) && 
                (vertex_ccodes[1] == CLIP_CODE_GY) && 
                (vertex_ccodes[2] == CLIP_CODE_GY) ) ||

               ((vertex_ccodes[0] == CLIP_CODE_LY) && 
                (vertex_ccodes[1] == CLIP_CODE_LY) && 
                (vertex_ccodes[2] == CLIP_CODE_LY) ) )

             {
             // clip the poly completely out of frustrum
             SET_BIT(curr_poly->state, POLY4DV2_STATE_CLIPPED);
             
             // move on to next polygon
             continue;
             } // end if
                           
          } // end if y planes

        // clip/cull to z planes
        if (clip_flags & CLIP_POLY_Z_PLANE)
           {
           // clip/cull only based on z clipping planes
           // for each vertice determine if it's in the clipping region or beyond it and
           // set the appropriate clipping code
           // then actually clip all polygons to the near clipping plane, this will result
           // in at most 1 additional triangle

           // reset vertex counters, these help in classification
           // of the final triangle 
           num_verts_in = 0;

           // vertex 0
           if (curr_poly->tvlist[0].z > cam->far_clip_z)
              {
              vertex_ccodes[0] = CLIP_CODE_GZ;
              } 
           else
           if (curr_poly->tvlist[0].z < cam->near_clip_z)
              {
              vertex_ccodes[0] = CLIP_CODE_LZ;
              }
           else
              {
              vertex_ccodes[0] = CLIP_CODE_IZ;
              num_verts_in++;
              } 
          
           // vertex 1
           if (curr_poly->tvlist[1].z > cam->far_clip_z)
              {
              vertex_ccodes[1] = CLIP_CODE_GZ;
              } 
           else
           if (curr_poly->tvlist[1].z < cam->near_clip_z)
              {
              vertex_ccodes[1] = CLIP_CODE_LZ;
              }
           else
              {
              vertex_ccodes[1] = CLIP_CODE_IZ;
              num_verts_in++;
              }     

           // vertex 2
           if (curr_poly->tvlist[2].z > cam->far_clip_z)
              {
              vertex_ccodes[2] = CLIP_CODE_GZ;
              } 
           else
           if (curr_poly->tvlist[2].z < cam->near_clip_z)
              {
              vertex_ccodes[2] = CLIP_CODE_LZ;
              }
           else
              {
              vertex_ccodes[2] = CLIP_CODE_IZ;
              num_verts_in++;
              } 
           
          // test for trivial rejections, polygon completely beyond far or near
          // z clipping planes
          if ( ((vertex_ccodes[0] == CLIP_CODE_GZ) && 
                (vertex_ccodes[1] == CLIP_CODE_GZ) && 
                (vertex_ccodes[2] == CLIP_CODE_GZ) ) ||

               ((vertex_ccodes[0] == CLIP_CODE_LZ) && 
                (vertex_ccodes[1] == CLIP_CODE_LZ) && 
                (vertex_ccodes[2] == CLIP_CODE_LZ) ) )

             {
             // clip the poly completely out of frustrum
             SET_BIT(curr_poly->state, POLY4DV2_STATE_CLIPPED);
             
             // move on to next polygon
             continue;
             } // end if

          // test if any vertex has protruded beyond near clipping plane?
          if ( ( (vertex_ccodes[0] | vertex_ccodes[1] | vertex_ccodes[2]) & CLIP_CODE_LZ) )
          {
          // at this point we are ready to clip the polygon to the near 
          // clipping plane no need to clip to the far plane since it can't 
          // possible cause problems. We have two cases: case 1: the triangle 
          // has 1 vertex interior to the near clipping plane and 2 vertices 
          // exterior, OR case 2: the triangle has two vertices interior of 
          // the near clipping plane and 1 exterior

          // step 1: classify the triangle type based on number of vertices
          // inside/outside
          // case 1: easy case :)
          if (num_verts_in == 1)
             {
             // we need to clip the triangle against the near clipping plane
             // the clipping procedure is done to each edge leading away from
             // the interior vertex, to clip we need to compute the intersection
             // with the near z plane, this is done with a parametric equation of 
             // the edge, once the intersection is computed the old vertex position
             // is overwritten along with re-computing the texture coordinates, if
             // there are any, what's nice about this case, is clipping doesn't 
             // introduce any added vertices, so we can overwrite the old poly
             // the other case below results in 2 polys, so at very least one has
             // to be added to the end of the rendering list -- bummer
 
             // step 1: find vertex index for interior vertex
             if ( vertex_ccodes[0] == CLIP_CODE_IZ)
                { v0 = 0; v1 = 1; v2 = 2; }
             else 
             if (vertex_ccodes[1] == CLIP_CODE_IZ)
                { v0 = 1; v1 = 2; v2 = 0; }
             else
                { v0 = 2; v1 = 0; v2 = 1; }

            // step 2: clip each edge
            // basically we are going to generate the parametric line p = v0 + v01*t
            // then solve for t when the z component is equal to near z, then plug that
            // back into to solve for x,y of the 3D line, we could do this with high
            // level code and parametric lines, but to save time, lets do it manually

            // clip edge v0->v1
            VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v1].v, &v);                          

            // the intersection occurs when z = near z, so t = 
            t1 = ( (cam->near_clip_z - curr_poly->tvlist[v0].z) / v.z );
      
            // now plug t back in and find x,y intersection with the plane
            xi = curr_poly->tvlist[v0].x + v.x * t1;
            yi = curr_poly->tvlist[v0].y + v.y * t1;

            // now overwrite vertex with new vertex
            curr_poly->tvlist[v1].x = xi;
            curr_poly->tvlist[v1].y = yi;
            curr_poly->tvlist[v1].z = cam->near_clip_z; 
         
            // clip edge v0->v2
            VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v2].v, &v);                          

            // the intersection occurs when z = near z, so t = 
            t2 = ( (cam->near_clip_z - curr_poly->tvlist[v0].z) / v.z );
      
            // now plug t back in and find x,y intersection with the plane
            xi = curr_poly->tvlist[v0].x + v.x * t2;
            yi = curr_poly->tvlist[v0].y + v.y * t2;

            // now overwrite vertex with new vertex
            curr_poly->tvlist[v2].x = xi;
            curr_poly->tvlist[v2].y = yi;
            curr_poly->tvlist[v2].z = cam->near_clip_z; 

            // now that we have both t1, t2, check if the poly is textured, if so clip
            // texture coordinates
            if (curr_poly->attr & POLY4DV2_ATTR_SHADE_MODE_TEXTURE)
               {
               ui = curr_poly->tvlist[v0].u0 + (curr_poly->tvlist[v1].u0 - curr_poly->tvlist[v0].u0)*t1;
               vi = curr_poly->tvlist[v0].v0 + (curr_poly->tvlist[v1].v0 - curr_poly->tvlist[v0].v0)*t1;
               curr_poly->tvlist[v1].u0 = ui;
               curr_poly->tvlist[v1].v0 = vi;

               ui = curr_poly->tvlist[v0].u0 + (curr_poly->tvlist[v2].u0 - curr_poly->tvlist[v0].u0)*t2;
               vi = curr_poly->tvlist[v0].v0 + (curr_poly->tvlist[v2].v0 - curr_poly->tvlist[v0].v0)*t2;
               curr_poly->tvlist[v2].u0 = ui;
               curr_poly->tvlist[v2].v0 = vi;
               } // end if textured

               // finally, we have obliterated our pre-computed normal length
               // it needs to be recomputed!!!!
 
              // build u, v
              VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v1].v, &u);
              VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v2].v, &v);

              // compute cross product
              VECTOR4D_Cross(&u, &v, &n);

              // compute length of normal accurately and store in poly nlength
              // +- epsilon later to fix over/underflows
              curr_poly->nlength = VECTOR4D_Length_Fast(&n); 

             } // end if
          else
          if (num_verts_in == 2)
             { // num_verts = 2

             // must be the case with num_verts_in = 2 
             // we need to clip the triangle against the near clipping plane
             // the clipping procedure is done to each edge leading away from
             // the interior vertex, to clip we need to compute the intersection
             // with the near z plane, this is done with a parametric equation of 
             // the edge, however unlike case 1 above, the triangle will be split
             // into two triangles, thus during the first clip, we will store the 
             // results into a new triangle at the end of the rendering list, and 
             // then on the last clip we will overwrite the triangle being clipped

             // step 0: copy the polygon
             memcpy(&temp_poly, curr_poly, sizeof(POLYF4DV2) );

             // step 1: find vertex index for exterior vertex
             if ( vertex_ccodes[0] == CLIP_CODE_LZ)
                { v0 = 0; v1 = 1; v2 = 2; }
             else 
             if (vertex_ccodes[1] == CLIP_CODE_LZ)
                { v0 = 1; v1 = 2; v2 = 0; }
             else
                { v0 = 2; v1 = 0; v2 = 1; }

             // step 2: clip each edge
             // basically we are going to generate the parametric line p = v0 + v01*t
             // then solve for t when the z component is equal to near z, then plug that
             // back into to solve for x,y of the 3D line, we could do this with high
             // level code and parametric lines, but to save time, lets do it manually

             // clip edge v0->v1
             VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v1].v, &v);                          

             // the intersection occurs when z = near z, so t = 
             t1 = ( (cam->near_clip_z - curr_poly->tvlist[v0].z) / v.z );
      
             // now plug t back in and find x,y intersection with the plane
             x01i = curr_poly->tvlist[v0].x + v.x * t1;
             y01i = curr_poly->tvlist[v0].y + v.y * t1;
         
             // clip edge v0->v2
             VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v2].v, &v);                          

             // the intersection occurs when z = near z, so t = 
             t2 = ( (cam->near_clip_z - curr_poly->tvlist[v0].z) / v.z );
      
             // now plug t back in and find x,y intersection with the plane
             x02i = curr_poly->tvlist[v0].x + v.x * t2;
             y02i = curr_poly->tvlist[v0].y + v.y * t2; 

             // now we have both intersection points, we must overwrite the inplace
             // polygon's vertex 0 with the intersection point, this poly 1 of 2 from
             // the split

             // now overwrite vertex with new vertex
             curr_poly->tvlist[v0].x = x01i;
             curr_poly->tvlist[v0].y = y01i;
             curr_poly->tvlist[v0].z = cam->near_clip_z; 

             // now comes the hard part, we have to carefully create a new polygon
             // from the 2 intersection points and v2, this polygon will be inserted
             // at the end of the rendering list, but for now, we are building it up
             // in  temp_poly

             // so leave v2 alone, but overwrite v1 with v01, and overwrite v0 with v02
             temp_poly.tvlist[v1].x = x01i;
             temp_poly.tvlist[v1].y = y01i;
             temp_poly.tvlist[v1].z = cam->near_clip_z;              

             temp_poly.tvlist[v0].x = x02i;
             temp_poly.tvlist[v0].y = y02i;
             temp_poly.tvlist[v0].z = cam->near_clip_z;    

            // now that we have both t1, t2, check if the poly is textured, if so clip
            // texture coordinates
            if (curr_poly->attr & POLY4DV2_ATTR_SHADE_MODE_TEXTURE)
               {
               // compute poly 1 new texture coordinates from split
               u01i = curr_poly->tvlist[v0].u0 + (curr_poly->tvlist[v1].u0 - curr_poly->tvlist[v0].u0)*t1;
               v01i = curr_poly->tvlist[v0].v0 + (curr_poly->tvlist[v1].v0 - curr_poly->tvlist[v0].v0)*t1;

               // compute poly 2 new texture coordinates from split
               u02i = curr_poly->tvlist[v0].u0 + (curr_poly->tvlist[v2].u0 - curr_poly->tvlist[v0].u0)*t2;
               v02i = curr_poly->tvlist[v0].v0 + (curr_poly->tvlist[v2].v0 - curr_poly->tvlist[v0].v0)*t2;

               // write them all at the same time         
               // poly 1
               curr_poly->tvlist[v0].u0 = u01i;
               curr_poly->tvlist[v0].v0 = v01i;

               // poly 2
               temp_poly.tvlist[v0].u0 = u02i;
               temp_poly.tvlist[v0].v0 = v02i;
               temp_poly.tvlist[v1].u0 = u01i;
               temp_poly.tvlist[v1].v0 = v01i;

               } // end if textured


               // finally, we have obliterated our pre-computed normal lengths
               // they need to be recomputed!!!!
 
               // poly 1 first, in place
 
               // build u, v
               VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v1].v, &u);
               VECTOR4D_Build(&curr_poly->tvlist[v0].v, &curr_poly->tvlist[v2].v, &v);

               // compute cross product
               VECTOR4D_Cross(&u, &v, &n);

               // compute length of normal accurately and store in poly nlength
               // +- epsilon later to fix over/underflows
               curr_poly->nlength = VECTOR4D_Length_Fast(&n); 

               // now poly 2, temp_poly
               // build u, v
               VECTOR4D_Build(&temp_poly.tvlist[v0].v, &temp_poly.tvlist[v1].v, &u);
               VECTOR4D_Build(&temp_poly.tvlist[v0].v, &temp_poly.tvlist[v2].v, &v);

               // compute cross product
               VECTOR4D_Cross(&u, &v, &n);

               // compute length of normal accurately and store in poly nlength
               // +- epsilon later to fix over/underflows
               temp_poly.nlength = VECTOR4D_Length_Fast(&n); 

             // now we are good to go, insert the polygon into list
             // if the poly won't fit, it won't matter, the function will
             // just return 0
             Insert_POLYF4DV2_RENDERLIST4DV2(rend_list, &temp_poly);

             } // end else
        
           } // end if near_z clipping has occured
             
          } // end if z planes

    } // end for poly

}
    */
}
