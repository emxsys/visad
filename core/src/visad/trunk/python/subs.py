from visad import ScalarMap, Display, DataReferenceImpl, RealTupleType,\
          Gridded2DSet, DisplayImpl, RealType
from types import StringType
from visad.ss import BasicSSCell
from visad.java2d import DisplayImplJ2D

# try to get 3D
ok3d = 1
try:
  from visad.java3d import DisplayImplJ3D, TwoDDisplayRendererJ3D
except:
  ok3d = 0

# create (and return) a VisAD DisplayImplJ3D and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay3D(maps):
  disp = DisplayImplJ3D("Jython3D")
  maximizeBox(disp)
  if maps != None:  addMaps(disp, maps)
  return disp

# create (and return) a VisAD DisplayImplJ2D and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay2D(maps):
  disp = DisplayImplJ2D("Jython2D")
  maximizeBox(disp)
  if maps != None:  addMaps(disp, maps)
  return disp

# create (and return) a VisAD DisplayImpl and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay(maps):
  is3d = 0
  if maps == None:
    is3d = 1
  else:
    for m in maps:
      if m.getDisplayScalar().toString() == "DisplayZAxis": is3d = 1

  if is3d == 1 and ok3d == 1:
    disp = makeDisplay3D(maps)
  else:
    if ok3d:
      tdr = TwoDDisplayRendererJ3D() 
      disp = DisplayImplJ3D("Jython3D",tdr)
      maximizeBox(disp)
      addMaps(disp, maps)
    else:
      disp =  makeDisplay2D(maps)
  
  return disp


# add a Data object to a Display, and return a reference to the Data
def addData(name, data, disp, constantMaps=None):
  ref = DataReferenceImpl(name)
  temp = disp.addReference(ref, constantMaps)
  if data != None: temp = ref.setData(data)
  return ref

# a simple method for making the VisAD "box" 95% of the window size
def maximizeBox(display):
  pc=display.getProjectionControl()
  pcMatrix=pc.getMatrix()
  if len(pcMatrix) > 10:
    pcMatrix[0]=.95
    pcMatrix[5]=.95
    pcMatrix[10]=.95
  else:
    pcMatrix[0]=1.4
    pcMatrix[3]=-1.4
    
  pc.setMatrix(pcMatrix)

# make a 2D or 3D line, return a reference so it can be changed
def makeLine(domainType, points):
  return Gridded2DSet(RealTupleType(domainType), points, len(points[0]))

# draw a line directly into the display; also return reference
# drawLine(display, domainType, points[], color=Color, mathtype=domainType)
# drawLine(name|display, points[], color=Color)
# "Color" is java.awt.Color
def drawLine(display, points, color=None, mathtype=None):

  constmap = None
  # see if color should be handled
  if color is not None:
    from visad import ConstantMap
    from java.awt import Color
    red = float(color.getRed())/255.
    green = float(color.getGreen())/255.
    blue = float(color.getBlue())/255.

    constmap = ( ConstantMap(red,Display.Red), ConstantMap(green,Display.Green),
           ConstantMap(blue,Display.Blue) )


  # drawLine(display, domainType, points[])
  maps = None
  if mathtype is not None:
    linref = addData("linesegment",
       Gridded2DSet(RealTupleType(mathtype), points, len(points[0])), display)
    return linref

  # drawLine(name|display, points[])
  else:
    if type(display) == StringType:
      d = BasicSSCell.getSSCellByName(display)
      disp = d.getDisplay()
      maps = d.getMaps()
    elif isinstance(a, DisplayImpl):
      maps = display.getMapVector()
      disp = display
    else:
      maps = None
      disp = None

    if maps == None:
      x = RealType.getRealTypeByName("x")
      y = RealType.getRealTypeByName("y")
      z = RealType.getRealTypeByName("z")
    # if no maps, make them...
    else:
      for m in maps:
        if m.getDisplayScalar().toString() == "DisplayXAxis":
          x = m.getScalar()
        if m.getDisplayScalar().toString() == "DisplayYAxis":
          y = m.getScalar()
        if m.getDisplayScalar().toString() == "DisplayZAxis":
          z = m.getScalar()

    if len(points) == 2:
      dom = RealTupleType(x,y)
    else:
      dom = RealTupleType(x,y,z)

# this is a test
    linref = addData("linesegment", 
             Gridded2DSet(dom, points, len(points[0])), disp, constmap)
    return linref 

# add an array of ScalarMaps to a Display
def addMaps(display, maps):
  for map in maps:
    display.addMap(map)

# define ScalarMap(s) given pairs of (Type, name)
# where "name" is taken from the list, below.
def makeMaps(*a):
  dis = ("x","y","z","lat","lon","rad","list","red","green",
  "blue","rgb","rgba","hue","saturation","value","hsv","cyan",
  "magenta","yellow","cmy","alpha","animation","selectvalue",
  "selectrange","contour","flow1x","flow1y","flow1z",
  "flow2x","flow2y","flow2z","xoffset","yoffset","zoffset",
  "shape","text","shapescale","linewidth","pointsize",
  "cylradius","cylazimuth","cylzaxis",
  "flow1elev","flow1azimuth","flow1radial",
  "flow2elev","flow2azimuth","flow2radial")
# note this list is in the same order as Display.DisplayRealArray! 

  maps=[]
  for i in range(0,len(a),2):
    got = -1 

    for k in range(len(dis)):
      if dis[k] == a[i+1] : got=k

    if got != -1:
      maps.append(ScalarMap(a[i], Display.DisplayRealArray[got]))
    else:
      print "While making mappings, cannot match: ",a[i+1]

  return maps