//
// ShadowImageFunctionTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.bom;

import visad.*;
import visad.java3d.*;

import javax.media.j3d.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferInt;

/**
   The ShadowImageFunctionTypeJ3D class shadows the FunctionType class for
   BarbRendererJ3D, within a DataDisplayLink, under Java2D.<P>
*/
public class ShadowImageFunctionTypeJ3D extends ShadowFunctionTypeJ3D {

  private static final int MISSING1 = Byte.MIN_VALUE;      // least byte

  public ShadowImageFunctionTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public boolean doTransform(Group group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
 
    // return if data is missing or no ScalarMaps
    if (data.isMissing()) return false;
    if (getLevelOfDifficulty() == NOTHING_MAPPED) return false;

    boolean sequence = ((ImageRendererJ3D) renderer).getSequence();

    ShadowFunctionOrSetType adaptedShadowType =
      (ShadowFunctionOrSetType) getAdaptedShadowType();
    DisplayImpl display = getDisplay();
    GraphicsModeControl mode = (GraphicsModeControl)
      display.getGraphicsModeControl().clone();

    // get 'shape' flags
    boolean anyContour = adaptedShadowType.getAnyContour();
    boolean anyFlow = adaptedShadowType.getAnyFlow();
    boolean anyShape = adaptedShadowType.getAnyShape();
    boolean anyText = adaptedShadowType.getAnyText();

    if (anyContour || anyFlow || anyShape || anyText) {
      throw new BadMappingException("no contour, flow, shape or text allowed");
    }

    // get some precomputed values useful for transform
    // length of ValueArray
    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    // mapping from ValueArray to MapVector
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();
 
    // array to hold values for various mappings
    float[][] display_values = new float[valueArrayLength][];
 
    int[] inherited_values = adaptedShadowType.getInheritedValues();

    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }

    Set domain_set = ((Field) data).getDomainSet();
    Unit[] dataUnits = ((Function) data).getDomainUnits();
    CoordinateSystem dataCoordinateSystem =
      ((Function) data).getDomainCoordinateSystem();

    float[][] domain_values = null;
    double[][] domain_doubles = null;
    ShadowRealTupleType Domain = adaptedShadowType.getDomain();
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length;
    int domain_dimension;
    try {
      domain_length = domain_set.getLength();
      domain_dimension = domain_set.getDimension();
    }
    catch (SetException e) {
      return false;
    }

    // ShadowRealTypes of Domain
    ShadowRealType[] DomainComponents = adaptedShadowType.getDomainComponents();

    if (adaptedShadowType.getIsTerminal()) {
      // check that range is single RealType mapped to RGB only
      ShadowRealType[] RangeComponents = adaptedShadowType.getRangeComponents();
      Vector mvector = RangeComponents[0].getSelectedMapVector();     
      if (mvector.size() != 1) {
        throw new BadMappingException("image values must be mapped to RGB only");
      }
      ScalarMap cmap = (ScalarMap) mvector.elementAt(0);
      if (!Display.RGB.equals(cmap.getDisplayScalar())) {
        throw new BadMappingException("image values must be mapped to RGB");
      }

      // build texture colors in color_ints
      ColorControl control = (ColorControl) cmap.getControl();
      float[][] table = control.getTable();
      byte[][] bytes = null;
      Set[] rsets = null;
      if (data instanceof FlatField) {
        bytes = ((FlatField) data).getBytes();
        rsets = ((FlatField) data). getRangeSets();
      }
      int[] color_ints = new int[domain_length];
      if (bytes != null && bytes[0] != null && table != null &&
          rsets != null && rsets[0] != null &&
          rsets[0] instanceof Linear1DSet) {
        // fast since FlatField with bytes and range set is Linear1DSet
        // get "scale and offset" for Linear1DSet
        double first = ((Linear1DSet) rsets[0]).getFirst();
        double step = ((Linear1DSet) rsets[0]).getStep();
        // get scale and offset for ScalarMap
        double[] so = new double[2];
        double[] da = new double[2];
        double[] di = new double[2];
        cmap.getScale(so, da, di);
        double scale = so[0];
        double offset = so[1];
        // get scale for color table
        double table_scale = (double) table[0].length;
        // combine scales and offsets for Set, ScalarMap and color table
        float mult = (float) (table_scale * scale * step);
        float add = (float) (table_scale * (offset + scale * first));
        // combine color table RGB components into ints
        int[] itable = new int[table[0].length];
        int r, g, b, a = 255;
        int c;
        for (int j=0; j<table[0].length; j++) {
          c = (int) (255.0 * table[0][j]);
          r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          c = (int) (255.0 * table[1][j]);
          g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          c = (int) (255.0 * table[2][j]);
          b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          itable[j] = ((a << 24) | (r << 16) | (g << 8) | b);
        }
        // now do fast lookup from byte values to color ints
        int tblEnd = table[0].length - 1;
        byte[] bytes0 = bytes[0];
        for (int i=0; i<domain_length; i++) {
          int index = ((int) bytes0[i]) - MISSING1 - 1;
          if (index < 0) {
            color_ints[i] = 0; // missing
          }
          else {
            int j = (int) (add + mult * index);
            // clip to table
            color_ints[i] =
              (j < 0) ? itable[0] : ((j > tblEnd) ? itable[tblEnd] : itable[j]);
          }
        }
        bytes = null; // take out the garbage
      }
      else {
        // slower, more general way to build texture colors
        bytes = null; // take out the garbage
        float[][] values = ((Field) data).getFloats();
        values[0] = cmap.scaleValues(values[0]);
        float[][] color_values = control.lookupValues(values[0]);
        values = null; // take out the garbage
        // combine color RGB components into ints
        int r, g, b, a = 255;
        int c;
        for (int i=0; i<domain_length; i++) {
          c = (int) (255.0 * color_values[0][i]);
          r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          c = (int) (255.0 * color_values[1][i]);
          g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          c = (int) (255.0 * color_values[2][i]);
          b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          color_ints[i] = ((a << 24) | (r << 16) | (g << 8) | b);
        }
      }

      // check domain and determine whether it is square or curved texture
      if (!Domain.getAllSpatial() || Domain.getMultipleDisplayScalar()) {
        throw new BadMappingException("domain must be only spatial");
      }

      boolean isTextureMap = adaptedShadowType.getIsTextureMap() &&
                             (domain_set instanceof Linear2DSet ||
                              (domain_set instanceof LinearNDSet &&
                               domain_set.getDimension() == 2));
   
      int curved_size = display.getGraphicsModeControl().getCurvedSize();
      boolean curvedTexture = adaptedShadowType.getCurvedTexture() &&
                              !isTextureMap &&
                              curved_size > 0 &&
                              (domain_set instanceof Gridded2DSet ||
                               (domain_set instanceof GriddedSet &&
                                domain_set.getDimension() == 2));

      float[] coordinates = null;
      float[] texCoords = null;
      float[] normals = null;
      byte[] colors = null;
      int data_width = 0;
      int data_height = 0;
      int texture_width = 1;
      int texture_height = 1;
      float[] coordinatesX = null;
      float[] texCoordsX = null;
      float[] normalsX = null;
      byte[] colorsX = null;
      float[] coordinatesY = null;
      float[] texCoordsY = null;
      float[] normalsY = null;
      byte[] colorsY = null;
  
      if (isTextureMap) {
        Linear1DSet X = null;
        Linear1DSet Y = null;
        if (domain_set instanceof Linear2DSet) {
          X = ((Linear2DSet) domain_set).getX();
          Y = ((Linear2DSet) domain_set).getY();
        }
        else {
          X = ((LinearNDSet) domain_set).getLinear1DComponent(0);
          Y = ((LinearNDSet) domain_set).getLinear1DComponent(1);
        }
        float[][] limits = new float[2][2];
        limits[0][0] = (float) X.getFirst();
        limits[0][1] = (float) X.getLast();
        limits[1][0] = (float) Y.getFirst();
        limits[1][1] = (float) Y.getLast();
   
        // convert values to default units (used in display)
        limits = Unit.convertTuple(limits, dataUnits, domain_units);
   
        // get domain_set sizes
        data_width = X.getLength();
        data_height = Y.getLength();
        texture_width = textureWidth(data_width);
        texture_height = textureHeight(data_height);
   
        int[] tuple_index = new int[3];
        if (DomainComponents.length != 2) {
          throw new DisplayException("texture domain dimension != 2:" +
                                     "ShadowFunctionOrSetType.doTransform");
        }
        for (int i=0; i<DomainComponents.length; i++) {
          Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
          ScalarMap map = (ScalarMap) maps.nextElement();
          // scale values
          limits[i] = map.scaleValues(limits[i]);
          DisplayRealType real = map.getDisplayScalar();
          DisplayTupleType tuple = real.getTuple();
          if (tuple == null ||
              !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
            throw new DisplayException("texture with bad tuple: " +
                                       "ShadowFunctionOrSetType.doTransform");
          }
          // get spatial index
          tuple_index[i] = real.getTupleIndex();
          if (maps.hasMoreElements()) {
            throw new DisplayException("texture with multiple spatial: " +
                                       "ShadowFunctionOrSetType.doTransform");
          }
        } // end for (int i=0; i<DomainComponents.length; i++)
        // get spatial index not mapped from domain_set
        tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
        DisplayRealType real = (DisplayRealType)
          Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
        int value2_index = display.getDisplayScalarIndex(real);
        float value2 = default_values[value2_index];
        for (int i=0; i<valueArrayLength; i++) {
          if (inherited_values[i] > 0 &&
              real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
            value2 = value_array[i];
            break;
          }
        }
   
        coordinates = new float[12];
        // corner 0
        coordinates[tuple_index[0]] = limits[0][0];
        coordinates[tuple_index[1]] = limits[1][0];
        coordinates[tuple_index[2]] = value2;
        // corner 1
        coordinates[3 + tuple_index[0]] = limits[0][1];
        coordinates[3 + tuple_index[1]] = limits[1][0];
        coordinates[3 + tuple_index[2]] = value2;
        // corner 2
        coordinates[6 + tuple_index[0]] = limits[0][1];
        coordinates[6 + tuple_index[1]] = limits[1][1];
        coordinates[6 + tuple_index[2]] = value2;
        // corner 3
        coordinates[9 + tuple_index[0]] = limits[0][0];
        coordinates[9 + tuple_index[1]] = limits[1][1];
        coordinates[9 + tuple_index[2]] = value2;
   
        // move image back in Java3D 2-D mode
        adjustZ(coordinates);
   
        texCoords = new float[8];
        float ratiow = ((float) data_width) / ((float) texture_width);
        float ratioh = ((float) data_height) / ((float) texture_height);
        setTexCoords(texCoords, ratiow, ratioh);
   
        normals = new float[12];
        float n0 = ((coordinates[3+2]-coordinates[0+2]) *
                    (coordinates[6+1]-coordinates[0+1])) -
                   ((coordinates[3+1]-coordinates[0+1]) *
                    (coordinates[6+2]-coordinates[0+2]));
        float n1 = ((coordinates[3+0]-coordinates[0+0]) *
                    (coordinates[6+2]-coordinates[0+2])) -
                   ((coordinates[3+2]-coordinates[0+2]) *
                    (coordinates[6+0]-coordinates[0+0]));
        float n2 = ((coordinates[3+1]-coordinates[0+1]) *
                    (coordinates[6+0]-coordinates[0+0])) -
                   ((coordinates[3+0]-coordinates[0+0]) *
                    (coordinates[6+1]-coordinates[0+1]));
   
        float nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
        n0 = n0 / nlen;
        n1 = n1 / nlen;
        n2 = n2 / nlen;
   
        // corner 0
        normals[0] = n0;
        normals[1] = n1;
        normals[2] = n2;
        // corner 1
        normals[3] = n0;
        normals[4] = n1;
        normals[5] = n2;
        // corner 2
        normals[6] = n0;
        normals[7] = n1;
        normals[8] = n2;
        // corner 3
        normals[9] = n0;
        normals[10] = n1;
        normals[11] = n2;
   
        colors = new byte[12];
        for (int i=0; i<12; i++) colors[i] = (byte) 127;
  
        VisADQuadArray qarray = new VisADQuadArray();
        qarray.vertexCount = 4;
        qarray.coordinates = coordinates;
        qarray.texCoords = texCoords;
        qarray.colors = colors;
        qarray.normals = normals;

        BufferedImage image = createImage(data_width, data_height, texture_width,
                                          texture_height, color_ints);

        textureToGroup(group, qarray, image, mode, 1.0f, null,
                       texture_width, texture_height);
      } // end if (isTextureMap)
      else if (curvedTexture) {

        int[] lengths = ((GriddedSet) domain_set).getLengths();
        data_width = lengths[0];
        data_height = lengths[1];
        texture_width = textureWidth(data_width);
        texture_height = textureHeight(data_height);
   
        int size = (data_width + data_height) / 2;
        curved_size = Math.max(2, Math.min(curved_size, size / 32));
   
        int nwidth = 2 + (data_width - 1) / curved_size;
        int nheight = 2 + (data_height - 1) / curved_size;
  
        int nn = nwidth * nheight;
        int[] is = new int[nwidth];
        int[] js = new int[nheight];
        for (int i=0; i<nwidth; i++) {
          is[i] = Math.min(i * curved_size, data_width - 1);
        }
        for (int j=0; j<nheight; j++) {
          js[j] = Math.min(j * curved_size, data_height - 1);
        }

        int[] indices = new int[nn];
        int k=0;
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
            indices[k] = is[i] + data_width * js[j];
            k++;
          }
        }

        float[][] spline_domain = domain_set.indexToValue(indices);
        spline_domain = Unit.convertTuple(spline_domain, dataUnits, domain_units);

        ShadowRealTupleType domain_reference = Domain.getReference();

        ShadowRealType[] DC = DomainComponents;
        if (domain_reference != null &&
            domain_reference.getMappedDisplayScalar()) {
          RealTupleType ref = (RealTupleType) domain_reference.getType();
          renderer.setEarthSpatialData(Domain, domain_reference, ref,
                      ref.getDefaultUnits(), (RealTupleType) Domain.getType(),
                      new CoordinateSystem[] {dataCoordinateSystem},
                      domain_units);

          spline_domain =
            CoordinateSystem.transformCoordinates(
              ref, null, ref.getDefaultUnits(), null,
              (RealTupleType) Domain.getType(), dataCoordinateSystem,
              domain_units, null, spline_domain);
          // ShadowRealTypes of DomainReference
          DC = adaptedShadowType.getDomainReferenceComponents();
        }
        else {
          RealTupleType ref = (domain_reference == null) ? null :
                              (RealTupleType) domain_reference.getType();
          Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
          renderer.setEarthSpatialData(Domain, domain_reference, ref,
                      ref_units, (RealTupleType) Domain.getType(),
                      new CoordinateSystem[] {dataCoordinateSystem},
                      domain_units);
        }

        int[] tuple_index = new int[3];
        int[] spatial_value_indices = {-1, -1, -1};

        DisplayTupleType spatial_tuple = null;
        for (int i=0; i<DC.length; i++) {
          Enumeration maps =
            DC[i].getSelectedMapVector().elements();
          ScalarMap map = (ScalarMap) maps.nextElement();
          DisplayRealType real = map.getDisplayScalar();
          spatial_tuple = real.getTuple();
          if (spatial_tuple == null) {
            throw new DisplayException("texture with bad tuple: " +
                                       "ShadowImageFunctionTypeJ3D.doTransform");
          }
          // get spatial index
          tuple_index[i] = real.getTupleIndex();
          spatial_value_indices[tuple_index[i]] = map.getValueIndex();
          if (maps.hasMoreElements()) {
            throw new DisplayException("texture with multiple spatial: " +
                                       "ShadowImageFunctionTypeJ3D.doTransform");
          }
        } // end for (int i=0; i<DC.length; i++)
        // get spatial index not mapped from domain_set
        tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
        DisplayRealType real = (DisplayRealType)
          Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
        int value2_index = display.getDisplayScalarIndex(real);
        float value2 = default_values[value2_index];
        for (int i=0; i<valueArrayLength; i++) {
          if (inherited_values[i] > 0 &&
              real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
            value2 = value_array[i];
            break;
          }
        }

        float[][] spatial_values = new float[3][];
        spatial_values[tuple_index[0]] = spline_domain[0];
        spatial_values[tuple_index[1]] = spline_domain[1];
        spatial_values[tuple_index[2]] = new float[nn];
        for (int i=0; i<nn; i++) spatial_values[tuple_index[2]][i] = value2;

        if (spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
          renderer.setEarthSpatialDisplay(null, spatial_tuple, display,
                   spatial_value_indices, default_values, null);
        }
        else {
          CoordinateSystem coord = spatial_tuple.getCoordinateSystem();
          spatial_values = coord.toReference(spatial_values);
          renderer.setEarthSpatialDisplay(coord, spatial_tuple, display,
                   spatial_value_indices, default_values, null);
        }

        // break from ShadowFunctionOrSetType

        coordinates = new float[3 * nn];
        k = 0;
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
            int ij = is[i] + data_width * js[j];
            coordinates[k++] = spatial_values[0][ij];
            coordinates[k++] = spatial_values[1][ij];
            coordinates[k++] = spatial_values[2][ij];
          }
        }

        boolean spatial_all_select = true;
        for (int i=0; i<3*nn; i++) {
          if (coordinates[i] != coordinates[i]) spatial_all_select = false;
        }

        normals = Gridded3DSet.makeNormals(coordinates, nwidth, nheight);
        colors = new byte[3 * nn];
        for (int i=0; i<3*nn; i++) colors[i] = (byte) 127;
 
        float ratiow = ((float) data_width) / ((float) texture_width);
        float ratioh = ((float) data_height) / ((float) texture_height);
        int mt = 0;
        texCoords = new float[2 * nn];
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
            texCoords[mt++] = ratiow * is[i] / (data_width - 1.0f);
            texCoords[mt++] = 1.0f - ratioh * js[j] / (data_height - 1.0f);
          }
        }
 
        VisADTriangleStripArray tarray = new VisADTriangleStripArray();
        tarray.stripVertexCounts = new int[nheight - 1];
        for (int i=0; i<nheight - 1; i++) {
          tarray.stripVertexCounts[i] = 2 * nwidth;
        }
        int len = (nheight - 1) * (2 * nwidth);
        tarray.vertexCount = len;
        tarray.normals = new float[3 * len];
        tarray.coordinates = new float[3 * len];
        tarray.colors = new byte[3 * len];
        tarray.texCoords = new float[2 * len];
 
        // shuffle normals into tarray.normals, etc
        k = 0;
        int kt = 0;
        int nwidth3 = 3 * nwidth;
        int nwidth2 = 2 * nwidth;
        for (int i=0; i<nheight-1; i++) {
          int m = i * nwidth3;
          mt = i * nwidth2;
          for (int j=0; j<nwidth; j++) {
            tarray.coordinates[k] = coordinates[m];
            tarray.coordinates[k+1] = coordinates[m+1];
            tarray.coordinates[k+2] = coordinates[m+2];
            tarray.coordinates[k+3] = coordinates[m+nwidth3];
            tarray.coordinates[k+4] = coordinates[m+nwidth3+1];
            tarray.coordinates[k+5] = coordinates[m+nwidth3+2];
 
            tarray.normals[k] = normals[m];
            tarray.normals[k+1] = normals[m+1];
            tarray.normals[k+2] = normals[m+2];
            tarray.normals[k+3] = normals[m+nwidth3];
            tarray.normals[k+4] = normals[m+nwidth3+1];
            tarray.normals[k+5] = normals[m+nwidth3+2];
 
            tarray.colors[k] = colors[m];
            tarray.colors[k+1] = colors[m+1];
            tarray.colors[k+2] = colors[m+2];
            tarray.colors[k+3] = colors[m+nwidth3];
            tarray.colors[k+4] = colors[m+nwidth3+1];
            tarray.colors[k+5] = colors[m+nwidth3+2];
 
            tarray.texCoords[kt] = texCoords[mt];
            tarray.texCoords[kt+1] = texCoords[mt+1];
            tarray.texCoords[kt+2] = texCoords[mt+nwidth2];
            tarray.texCoords[kt+3] = texCoords[mt+nwidth2+1];
 
            k += 6;
            m += 3;
            kt += 4;
            mt += 2;
          }
        }

        if (!spatial_all_select) {
          tarray = (VisADTriangleStripArray) tarray.removeMissing();
        }

        tarray = (VisADTriangleStripArray) tarray.adjustLongitude(renderer);

        BufferedImage image = createImage(data_width, data_height, texture_width,
                                          texture_height, color_ints);

        textureToGroup(group, tarray, image, mode, 1.0f, null,
                       texture_width, texture_height);
      } // end if (curvedTexture)
      else { // !isTextureMap && !curvedTexture
        throw new BadMappingException("must be texture map or curved texture map");
      }
    }
    else { // !adaptedShadowType.getIsTerminal()
      Vector domain_maps = DomainComponents[0].getSelectedMapVector();
      ScalarMap amap = null;
      if (domain_set.getDimension() == 1 && domain_maps.size() == 1) {
        ScalarMap map = (ScalarMap) domain_maps.elementAt(0);
        if (Display.Animation.equals(map.getDisplayScalar())) {
          amap = map;
        }
      }
      if (amap == null) {
        throw new BadMappingException("time must be mapped to Animation");
      }
      AnimationControlJ3D control = (AnimationControlJ3D) amap.getControl();

      // get any usable frames from the old scene graph
      Switch old_swit = null;
      BranchGroup[] old_nodes = null;
      double[] old_times = null;
      boolean[] old_mark = null;
      int old_len = 0;
      if (((BranchGroup) group).numChildren() > 0) {
        Node g = ((BranchGroup) group).getChild(0);
        if (g instanceof Switch) {
          old_swit = (Switch) g;

          old_len = old_swit.numChildren();
          if (old_len > 0) {
            old_nodes = new BranchGroup[old_len];
            for (int i=0; i<old_len; i++) {
              old_nodes[i] = (BranchGroup) old_swit.getChild(i);
            }
            // remove old_nodes from old_swit
            for (int i=0; i<old_len; i++) {
              old_nodes[i].detach();
            }
            old_times = new double[old_len];
            old_mark = new boolean[old_len];
            for (int i=0; i<old_len; i++) {
              old_mark[i] = false;
              if (old_nodes[i] instanceof VisADBranchGroup) {
                old_times[i] = ((VisADBranchGroup) old_nodes[i]).getTime();
              }
              else {
                old_times[i] = Double.NaN;
              }
            }
          }
        }
      }

      // create frames for new scene graph
      Set aset = control.getSet();
      double[][] values = aset.getDoubles();
      double[] times = values[0];
      int len = times.length;
      double delta = Math.abs((times[len-1] - times[0]) / (1000.0 * len));

      // create new Switch and make live
      // control.clearSwitches(this); // already done in DataRenderer.doAction
      Switch swit = null;
      if (old_swit != null) {
        swit = old_swit;
        ((AVControlJ3D) control).addPair((Switch) swit, domain_set, renderer);
        ((AVControlJ3D) control).init();
      }
      else {
        swit = (Switch) makeSwitch();
        addSwitch(group, swit, control, domain_set, renderer);
      }

      // insert old frames into new scene graph, and make
      // new (blank) VisADBranchGroups for rendering new frames
      VisADBranchGroup[] nodes = new VisADBranchGroup[len];
      boolean[] mark = new boolean[len];
      for (int i=0; i<len; i++) {
        for (int j=0; j<old_len; j++) {
          if (!old_mark[j] && Math.abs(times[i] - old_times[j]) < delta) {
            old_mark[j] = true;
            nodes[i] = (VisADBranchGroup) old_nodes[j];
            break;
          }
        }
        if (nodes[i] != null) {
          mark[i] = true;
        }
        else {
          mark[i] = false;
          nodes[i] = new VisADBranchGroup(times[i]);
          nodes[i].setCapability(BranchGroup.ALLOW_DETACH);
          ensureNotEmpty(nodes[i]);
        }
        addToSwitch(swit, nodes[i]);
      }

      // make sure group is live
      ((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);

      // render new frames
      for (int i=0; i<len; i++) {
        if (!mark[i]) {
          // not necessary, but perhaps if this is modified
          // int[] lat_lon_indices = renderer.getLatLonIndices();
          recurseRange(nodes[i], ((Field) data).getSample(i),
                       value_array, default_values, renderer);
          // not necessary, but perhaps if this is modified
          // renderer.setLatLonIndices(lat_lon_indices);
        }
      }
    }


    ensureNotEmpty(group);
    return false;
  }

  public BufferedImage createImage(int data_width, int data_height,
                       int texture_width, int texture_height, int[] color_ints) {
    BufferedImage image = null;
    ColorModel colorModel = ColorModel.getRGBdefault();
    WritableRaster raster =
      colorModel.createCompatibleWritableRaster(texture_width, texture_height);
    image = new BufferedImage(colorModel, raster, false, null);
    int[] intData = ((DataBufferInt)raster.getDataBuffer()).getData();
    int k = 0;
    int m = 0;
    int r, g, b, a;
    for (int j=0; j<data_height; j++) {
      for (int i=0; i<data_width; i++) {
        intData[m++] = color_ints[k++];
      }
      for (int i=data_width; i<texture_width; i++) {
        intData[m++] = 0;
      }
    }
    for (int j=data_height; j<texture_height; j++) {
      for (int i=0; i<texture_width; i++) {
        intData[m++] = 0;
      }
    }
    return image;
  }

}

