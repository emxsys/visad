/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DInt16 variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class Int16VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private Int16VariableAdapter(DInt16 var, DAS das)
	throws VisADException, RemoteException
    {
	AttributeTable	table = attributeTable(das, var);
	realType = realType(var, table);
	valuator = Valuator.valuator(table, Attribute.INT16);
	repSets = new SimpleSet[] {valuator.getRepresentationalSet(realType)};
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link DInt16}.
     *
     * @param var		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static Int16VariableAdapter int16VariableAdapter(DInt16 var, DAS das)
	throws VisADException, RemoteException
    {
	return new Int16VariableAdapter(var, das);
    }

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public MathType getMathType()
    {
	return realType;
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent this
     * instances data values in the range of a VisAD {@link FlatField}.  The
     * same array is returned each time, so modifications to the array will
     * affect all subsequent invocations of this method.
     *
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.  WARNING: Modify
     *				only under duress.
     */
    public SimpleSet[] getRepresentationalSets()
    {
	return repSets;
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DInt16}.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be {@link Real}.
     */
    public DataImpl data(DInt16 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
