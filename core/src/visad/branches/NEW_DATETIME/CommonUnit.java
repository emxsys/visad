
//
// CommonUnit.java
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

/**
   CommonUnit is a class for commonly used Units
*/
public class CommonUnit extends Object {

  /** CommonUnit for plane angle, not temperature */
  public static Unit degree;
  public static Unit radian = SI.radian;
  public static Unit second = SI.second;
  public static Unit meterPerSecond =
    new DerivedUnit(new BaseUnit[] {SI.meter, SI.second},
                    new int[] {1, -1});
  /** CommonUnit for seconds since the Epoch (i.e. 1970-01-01 00:00:00Z) */
  public static Unit secondsSinceTheEpoch =
        new OffsetUnit(
            visad.data.netcdf.units.UnitParser.encodeTimestamp(
                1970, 1, 1, 0, 0, 0, 0),
            SI.second);

  /** all BaseUnits have exponent zero in dimensionless */
  public static Unit dimensionless = new DerivedUnit();
  /** promiscuous is compatible with any Unit; useful for constants;
      not the same as null Unit, which is only compatible with
      other null Units; not the same as dimensionless, which is not
      compatible with other Units for addition and subtraction */
  public static Unit promiscuous = PromiscuousUnit.promiscuous;

  static {
    try {
      degree = SI.radian.scale(Math.PI/180.0, true).clone("deg");
    }
    catch (UnitException e) {}		// can't happen
  }

}

