 
//
// PromiscuousUnit.java
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
   PromiscuousUnit is the VisAD class for units
   that are convertable with any other Unit.<P>
*/
public class PromiscuousUnit extends Unit {

  static final Unit promiscuous = new PromiscuousUnit();

  private PromiscuousUnit() {
    super("promiscuous");
  }

  /**
   * Clones this unit, changing the identifier.  This method always throws
   * an exception because promiscuous units may not be cloned.
   * @param identifier		The name or abbreviation for the cloned unit.
   *				May be <code>null</code> or empty.
   * @throws UnitException	Promiscuous units may not be cloned.  Always 
   *				thrown.
   */
  public Unit clone(String identifier)
    throws UnitException
  {
    throw new UnitException("Promiscuous units may not be cloned");
  }

  /**
   * Returns the definition of this unit.  For promiscuous units, this is the
   * same as the identifier.
   * @return		The definition of this unit.  Won't be <code>null
   *			</code> but may be empty.
   */
  public String getDefinition()
  {
    return getIdentifier();
  }

  public Unit pow(int power) 
	throws UnitException {
    return this;
  }

  public Unit pow(double power)
	throws UnitException {
    return this;
  }
 
  Unit multiply(BaseUnit that)
       throws UnitException {
    return that;
  }

  Unit multiply(DerivedUnit that)
       throws UnitException {
    return that;
  }

  Unit multiply(ScaledUnit that)
       throws UnitException {
    return that;
  }

  Unit multiply(PromiscuousUnit that)
       throws UnitException {
    return that;
  }

  Unit divide(BaseUnit that)
       throws UnitException {
    return CommonUnit.dimensionless.divide(that);
  }

  Unit divide(DerivedUnit that)
       throws UnitException {
    return CommonUnit.dimensionless.divide(that);
  }

  Unit divide(ScaledUnit that)
       throws UnitException {
    return CommonUnit.dimensionless.divide(that);
  }

  Unit divide(PromiscuousUnit that)
       throws UnitException {
    return that;
  }


  double[] toThis(double[] values, BaseUnit that)
           throws UnitException {
    return values;
  }

  double[] toThis(double[] values, DerivedUnit that)
           throws UnitException {
    return values;
  }

  double[] toThis(double[] values, ScaledUnit that)
           throws UnitException {
    return values;
  }

  double[] toThis(double[] values, OffsetUnit that)
           throws UnitException {
    return values;
  }

  double[] toThis(double[] values, PromiscuousUnit that)
           throws UnitException {
    return values;
  }


  double[] toThat(double[] values, BaseUnit that)
           throws UnitException {
    return values;
  }

  double[] toThat(double[] values, DerivedUnit that)
           throws UnitException {
    return values;
  }

  double[] toThat(double[] values, ScaledUnit that)
           throws UnitException {
    return values;
  }

  double[] toThat(double[] values, OffsetUnit that)
           throws UnitException {
    return values;
  }

  double[] toThat(double[] values, PromiscuousUnit that)
           throws UnitException {
    return values;
  }


  float[] toThis(float[] values, BaseUnit that)
           throws UnitException {
    return values;
  }

  float[] toThis(float[] values, DerivedUnit that)
           throws UnitException {
    return values;
  }

  float[] toThis(float[] values, ScaledUnit that)
           throws UnitException {
    return values;
  }

  float[] toThis(float[] values, OffsetUnit that)
           throws UnitException {
    return values;
  }

  float[] toThis(float[] values, PromiscuousUnit that)
           throws UnitException {
    return values;
  }


  float[] toThat(float[] values, BaseUnit that)
           throws UnitException {
    return values;
  }

  float[] toThat(float[] values, DerivedUnit that)
           throws UnitException {
    return values;
  }

  float[] toThat(float[] values, ScaledUnit that)
           throws UnitException {
    return values;
  }

  float[] toThat(float[] values, OffsetUnit that)
           throws UnitException {
    return values;
  }

  float[] toThat(float[] values, PromiscuousUnit that)
           throws UnitException {
    return values;
  }

  public boolean equals(Unit unit) {
    return (unit instanceof PromiscuousUnit);
  }

}

