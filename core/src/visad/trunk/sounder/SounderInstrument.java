//
// SounderInstrument.java
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

package visad.sounder;

import visad.*;
import visad.data.netcdf.units.Parser;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.NoSuchUnitException;
import visad.data.netcdf.StandardQuantityDB;

import java.rmi.RemoteException;

/**
   SoundingInstrument is the VisAD abstract class for atmospheric
   sounding instruments.<p>
*/
public abstract class SounderInstrument 
{
  double[] model_parms;
  RealType[] model_parm_types;
  DataReferenceImpl[] parm_refs;
  int n_parms;

  public SounderInstrument(String[] names, String[] units, double[] parms)
         throws VisADException, RemoteException
  {
    n_parms = parms.length;

    if ( names.length != n_parms || units.length != n_parms ) { 
      throw new VisADException("# of names, units, parms must be equal"); 
    }

    model_parm_types = new RealType[n_parms];
    model_parms = new double[n_parms];
    parm_refs = new DataReferenceImpl[n_parms];

    CellImpl update_cell = new CellImpl()
    {
      public void doAction() throws VisADException, RemoteException
      {
        for ( int ii = 0; ii < n_parms; ii++ ) {
          model_parms[ii] = ((Real)parm_refs[ii].getData()).getValue();
        }
      }
    };

    for ( int ii = 0; ii < n_parms; ii++ ) {
      Unit u = null;
      try {
        u = Parser.parse(units[ii]);
      }
      catch ( NoSuchUnitException e ) {
        e.printStackTrace();  
      }
      catch ( ParseException e ) {
        e.printStackTrace();
      }

      model_parm_types[ii] = new RealType(names[ii], u, null);
      model_parms[ii] = parms[ii];
      parm_refs[ii] = new DataReferenceImpl(names[ii]);
      parm_refs[ii].setData(new Real(model_parm_types[ii], model_parms[ii]));
    }

    for ( int ii = 0; ii < n_parms; ii++ ) {
      update_cell.addReference(parm_refs[ii]);
    }
  }

  public DataReferenceImpl[] getParamReferences()
  {
    return parm_refs;
  }

  public void add(DisplayImpl display) {

  }

  public void add(DisplayImpl[] display_s) {

  }

  public void remove() {

  }

  public abstract Sounding retrieval(Spectrum spectrum, Sounding firstGuess);

  public abstract Spectrum foward(Sounding sounding);
}
