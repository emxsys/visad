
//
// Function.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;

/**
   Function is the interface for approximate implmentations
   of mathematical function.<P>
*/
public interface Function extends Data {

  /** get dimension of Function domain */
  public abstract int getDomainDimension()
         throws VisADException, RemoteException;

  /** get Units of domain Real components */
  public abstract Unit[] getDomainUnits()
         throws VisADException, RemoteException;

  /** get domain CoordinateSystem */
  public abstract CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException;

  /** evaluate this Function at domain, for 1-D domains;
      use default modes for resampling (Data.NEAREST_NEIGHBOR) and
      errors (Data.NO_ERRORS) */
  public abstract Data evaluate(Real domain)
         throws VisADException, RemoteException;

  /** evaluate this Function, for 1-D domains, with non-default modes for
      resampling and errors */
  public abstract Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;

  /** evaluate this Function at domain; first check that types match;
      use default modes for resampling (Data.NEAREST_NEIGHBOR) and
      errors (Data.NO_ERRORS) */
  public abstract Data evaluate(RealTuple domain)
         throws VisADException, RemoteException;

  /** evaluate this Function with non-default modes for resampling and errors */
  public abstract Data evaluate(RealTuple domain, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /** return a Field of Function values at the samples in set;
      this combines unit conversions, coordinate transforms,
      resampling and interpolation */
  public abstract Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /** return the derivative of this Function with respect to d_partial;
      d_partial may occur in this Function's domain RealTupleType, or,
      if the domain has a CoordinateSystem, in its Reference
      RealTupleType; propogate errors accoridng to error_mode;
      propogate errors according to error_mode */
  public abstract Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException;
 
  /** return the derivative of this Function with respect to d_partial;
      set result MathType to derivType; d_partial may occur in this
      Function's domain RealTupleType, or, if the domain has a
      CoordinateSystem, in its Reference RealTupleType;
      propogate errors accoridng to error_mode;
      propogate errors according to error_mode */
  public abstract Function derivative( RealType d_partial, MathType derivType,
                                       int error_mode)
         throws VisADException, RemoteException;

  /** return the tuple of derivatives of this Function with respect to
      all RealType components of its domain RealTuple;
      propogate errors according to error_mode */
  public abstract Data derivative( int error_mode )
         throws VisADException, RemoteException;

  /** return the tuple of derivatives of this Function with respect to
      all RealType components of its domain RealTuple;
      set result MathTypes of tuple components to derivType_s;
      propogate errors according to error_mode */
  public abstract Data derivative( MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException;

  /** return the tuple of derivatives of this Function with respect
      to the RealTypes in d_partial_s; the RealTypes in d_partial_s
      may occur in this Function's domain RealTupleType, or, if the
      domain has a CoordinateSystem, in its Reference RealTupleType;
      set result MathTypes of tuple components to derivType_s;
      propogate errors according to error_mode */
  public abstract Data derivative( RealTuple location, RealType[] d_partial_s,
                                   MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException;

}

