/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.26 2001-12-19 16:47:05 steve Exp $
 */

package visad.data.netcdf.in;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import visad.*;
import visad.data.BadFormException;
import visad.data.netcdf.*;


/**
 * The NetcdfAdapter class adapts a netCDF dataset to a VisAD API.  It is
 * useful for importing a netCDF dataset.
 *
 * @author Steven R. Emmerson
 */
public class
NetcdfAdapter
{
    /**
     * The name of the import-strategy Java property.  (NOTE: A Java property is
     * not a JavaBean property.)
     */
    public static final String  IMPORT_STRATEGY_PROPERTY =
        "visad.data.netcdf.in.Strategy";

    /**
     * The view of the netCDF datset.
     */
    private View        view;

    /**
     * The top-level VisAD data object corresponding to the netCDF datset.
     */
    private DataImpl    data;


    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf            The netCDF dataset to be adapted.
     * @param quantityDB        A quantity database to be used to map netCDF
     *                          variables to VisAD {@link Quantity}s.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws RemoteException  Remote data access failure.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException Non-conforming netCDF dataset.
     */
    public
    NetcdfAdapter(Netcdf netcdf, QuantityDB quantityDB)
        throws VisADException, RemoteException, IOException, BadFormException
    {
        this(View.getInstance(netcdf, quantityDB));
    }


    /**
     * Constructs from a view of a netCDF dataset.
     *
     * @param view              The view of the netCDF dataset to be adapted.
     */
    public
    NetcdfAdapter(View view)
    {
        this.view = view;
    }


    /**
     * <p>Gets the VisAD data object corresponding to the netCDF dataset.  This
     * is a potentially expensive method in either time or space.</p>
     *
     * <p>This method uses the Java (not JavaBean) property
     * <em>visad.data.netcdf.in.Strategy</em> to determine the strategy with
     * which to import the netCDF dataset.  If the property is not set,
     * then the default is to use {@link Strategy#DEFAULT}; otherwise, the
     * value of the property is used as a class name to instantiate the
     * strategy for importing the netCDF dataset.  The import strategy
     * can be set by the user of an application by means of the property
     * <em>visad.data.netcdf.in.Strategy</em>:
     * <blockquote><code><pre>
     * java -Dvisad.data.netcdf.in.Strategy=<em>SomeClassName</em> ...</pre>
     * </code></blockquote></p>
     *
     * <p>This implementation invokes method {@link
     * #getData(NetcdfAdapter.Strategy)} with the {@link NetcdfAdapter.Strategy}
     * determined from the above procedure.</p>
     *
     * @return                  The top-level, VisAD data object in the netCDF
     *                          dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in the View that was passed to the
     *                          constructor.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @throws RemoteException  if a Java RMI failure occurs.
     * @see #getData(Strategy)
     * @see #IMPORT_STRATEGY_PROPERTY
     */
    public synchronized DataImpl
    getData()
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        if (data == null)
        {
            String      strategyName =
                System.getProperty(IMPORT_STRATEGY_PROPERTY);
            Strategy    strategy;

            try
            {
                strategy =
                    strategyName == null
                        ? Strategy.DEFAULT
                        : (Strategy)Class.forName(strategyName).getMethod(
                            "instance", new Class[0])
                            .invoke(null, new Object[0]);
            }
            catch (NoSuchMethodException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): " +
                    "Import strategy \"" + strategyName + "\" doesn't have an "
                    + "\"instance()\" method");
            }
            catch (ClassNotFoundException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): " +
                    "Import strategy \"" + strategyName + "\" not found");
            }
            catch (IllegalAccessException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): " +
                    "Permission to access import strategy \"" + strategyName +
                    "\" denied");
            }
            catch (java.lang.reflect.InvocationTargetException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): Import strategy's \"" +
                    strategyName + "\" \"instance()\" method threw exception: "
                    + e.getMessage());
            }

            data = getData(strategy);
        }

        return data;
    }

    /**
     * Gets the VisAD data object corresponding to the netCDF dataset using a
     * given strategy.  This is a potentially expensive method in either time or
     * space.</p>
     *
     * @param strategy          The strategy to use for importing the data.
     * @return                  The top-level, VisAD data object in the netCDF
     *                          dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in the View that was passed to the
     *                          constructor.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @throws RemoteException  if a Java RMI failure occurs.
     */
    public synchronized DataImpl
    getData(Strategy strategy)
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        if (data == null)
        {
            data = strategy.getData(this);
        }

        return data;
    }


    /**
     * Returns a proxy for the VisAD data object corresponding to the netCDF 
     * dataset.  Because of the way import strategies are used, this just
     * invokes the <em>getData()</em> method.
     *
     * @return                  A proxy for the top-level, VisAD data object in
     *                          the netCDF dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in constructing View.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @throws RemoteException  if a Java RMI failure occurs.
     * @see Strategy#getData
     */
    public DataImpl
    getProxy()
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        return getData();
    }


    /**
     * Returns the VisAD data object corresponding to the netCDF dataset.  This
     * is a potentially expensive method in either time or space.  This method
     * is designed to be used by a <em>Strategy</em>.
     *
     * @param view              The view of the netCDF dataset.
     * @param merger            The object that merges the data objects in the
     *                          netCDF dataset.
     * @param dataFactory       The factory that creates VisAD data objects from
     *                          virtual data objects.
     * @return                  The VisAD data object corresponding to the
     *                          netCDF dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in constructing View.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @throws RemoteException  if a Java RMI failure occurs.
     * @see Strategy
     */
    protected static DataImpl
    importData(View view, Merger merger, DataFactory dataFactory)
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        VirtualTuple    topTuple = new VirtualTuple();

        for (VirtualDataIterator iter = view.getVirtualDataIterator();
            iter.hasNext(); )
        {
            merger.merge(topTuple, iter.next());
        }

        topTuple.setDataFactory(dataFactory);

        return topTuple.getData();
    }


    /**
     * Gets the view of the netCDF dataset.
     *
     * @return                  The view of the netCDF dataset.
     */
    protected View
    getView()
    {
        return view;
    }


    /**
     * Tests this class.
     *
     * @param args              File pathnames.
     * @throws Exception        Something went wrong.
     */
    public static void
    main(String[] args)
        throws Exception
    {
        String[]        pathnames;

        if (args.length == 0)
            pathnames = new String[] {"test.nc"};
        else
            pathnames = args;

        for (int i = 0; i < pathnames.length; ++i)
        {
            NetcdfFile  file;
            try
            {
                URL     url = new URL(pathnames[i]);
                file = new NetcdfFile(url);
            }
            catch (MalformedURLException e)
            {
                file = new NetcdfFile(pathnames[i], /*readonly=*/true);
            }
            NetcdfAdapter       adapter =
                new NetcdfAdapter(file, QuantityDBManager.instance());
            DataImpl            data = adapter.getData();

            System.out.println("data.getClass().getName() = " +
                data.getClass().getName());

            System.out.println("data.getType().prettyString():\n" +
                data.getType().prettyString());
            // System.out.println("Domain set:\n" +
                // ((FieldImpl)data).getDomainSet());
            // System.out.println("Data:\n" + data);
        }
    }
}
