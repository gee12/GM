package com.bondar.gm;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.bondar.tools.Files;

/**
 *
 * @author truebondar
 */
public class GMXFile {
    
    private static final String MODELS_EXTENSION = ".gmx";
    private static final String SEPARATOR = " ";
    private static final String NAME_PARAM = "name:";
    private static final String POSITION_PARAM = "pos:";
    private static final String ANGLES_PARAM = "angles:";
    private static final String SCALE_PARAM = "scale:";
    private static final String VERTEXES_PARAM = "verts:";
    private static final String POLYGONS_PARAM = "polyns:";
    private static final String ATTRIBUTES_PARAM = "attr:";
    
    /////////////////////////////////////////////////////////
    public static List<Solid3D> readGMXDir(String dirName) throws Exception {
	List<Solid3D> res = new ArrayList<>();
	String[] solidsFiles = Files.getMaskedFilesInDir(dirName, MODELS_EXTENSION);
	for (String fileName : solidsFiles) {
	    Solid3D solid = GMXFile.readGMXFile(dirName + fileName);
	    res.add(solid);
	}
	return res;
    }
    
    public static Solid3D readGMXFile(String fileName)
	    throws Exception {
	BufferedReader reader = new BufferedReader(
		new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
	//
	String name = Files.withOutExtAndPath(fileName);
	int attribs = 0;
	Point3D pos = new Point3D();
	Point3D angles = new Point3D();
	Point3D scale = new Point3D();
	Point3D[] vertexes = null;
	/*int[][] indexes = null;
	Color[] fillColors = null;
	Color[] borderColors = null;
	int[] polyAttribs = null;*/
	Polygon3D[] polies = null;
	
	String line = null;
	int fileLineNum = 0;
	while ((line = reader.readLine()) != null) {
	    fileLineNum++;
	    String[] words = line.split(SEPARATOR);
	    switch (words[0]) {
		//
		case NAME_PARAM:
		    name = words[1];
		    break;
		case ATTRIBUTES_PARAM:
		    attribs = Integer.parseInt(words[1]);
		    break;
		case POSITION_PARAM:
		    pos = readPoint3D(words);
		    break;
		case ANGLES_PARAM:
		    angles = readPoint3D(words);
		    break;
		case SCALE_PARAM:
		    scale = readPoint3D(words);
		    break;
		//
		case VERTEXES_PARAM:
		    int vertCount = Integer.parseInt(words[1]);
		    vertexes = new Point3D[vertCount];
		    int lineNum = 0;
		    while (lineNum < vertCount) {
			if ((line = reader.readLine()) == null) {
			    throw new RuntimeException(buildErrorText(
				    "Wrong vertexes number",fileName, fileLineNum + lineNum));
			}
			String[] coordsStr = line.split(SEPARATOR);
			int coordsCount = coordsStr.length;
			if (coordsCount < 3) {
			    throw new RuntimeException(buildErrorText(
				    "Need 3 coordinates",fileName, fileLineNum + lineNum));
			}
			// vertexes
			double[] coords = new double[3];
			for (int i = 0; i < 3; i++) {
			    coords[i] = Double.parseDouble(coordsStr[i]);
			}
			vertexes[lineNum] = new Point3D(coords[0], coords[1], coords[2]);
			lineNum++;
		    }
		    break;
		// polygons vertex indexes
		case POLYGONS_PARAM:
		    int poliesNum= Integer.parseInt(words[1]);
		    /*indexes = new int[poliesNum][];
		    fillColors = new Color[poliesNum];
		    borderColors = new Color[poliesNum];
		    polyAttribs = new int[poliesNum];*/
		    polies = new Polygon3D[poliesNum];
		    lineNum = 0;
		    while (lineNum < poliesNum) {
			if ((line = reader.readLine()) == null) {
			    throw new RuntimeException(buildErrorText(
				    "Wrong indexes number",fileName, fileLineNum + lineNum));
			}
			String[] indsStr = line.split(SEPARATOR);
			int indsStrSize = indsStr.length;
			if (indsStrSize < 4) {
			    throw new RuntimeException(buildErrorText(
				    "Wrong index format",fileName, fileLineNum + lineNum));
			}
			// indexes
			int indsNum = Integer.parseInt(indsStr[0]);
			int[] indexes = new int[indsNum];
			for (int i = 1; i < indsNum + 1; i++) {
			    indexes[i-1] = Integer.parseInt(indsStr[i]);
			}
			// fill color
			Color fillColor = Color.LIGHT_GRAY;
			if (indsStrSize > indsNum + 1) {
			    fillColor = new Color(Integer.parseInt(indsStr[indsNum + 1], 16));
			}
			//fillColors[lineNum] = fillColor;
			// border color
			Color borderColor = Color.BLACK;
			if (indsStrSize > indsNum + 2) {
			    borderColor = new Color(Integer.parseInt(indsStr[indsNum + 2], 16));
			}
			//borderColors[lineNum] = borderColor;
			// attributes
			int attr = 0;
			if (indsStrSize > indsNum + 3) {
			    attr = Integer.parseInt(indsStr[indsNum + 3]);
			}
			//polyAttribs[lineNum] = attr;
			polies[lineNum] = new Polygon3D(vertexes, indexes, fillColor, borderColor, attr);
			lineNum++;
		    }		    
		    break;
		default:
		    // comment in file
		    break;
	    }
	}
	reader.close();
	//
	for (Polygon3D poly : polies) {
	    poly.setVertexes(vertexes);
	}
	Solid3D res = new Solid3D(name, vertexes, polies);
	res.updateTransfers(pos.getX(), pos.getY(), pos.getZ());
	res.updateAngles(angles.getX(), angles.getY(), angles.getZ());
	res.updateScale(scale.getX(), scale.getY(), scale. getZ());
	res.setAttribute(attribs);

	return res;
    }
    
    public static Point3D readPoint3D(String[] words) {
	if (words.length < 3) return null;
	double[] coords = new double[3];
	for (int i = 0; i < 3; i++) {
	    coords[i] = Double.parseDouble(words[i+1]);
	}
	return new Point3D(coords[0], coords[1], coords[2]);
    }
    
    public static String buildErrorText(String text, String fileName, int line) {
	return String.format("%s in file '%s' (line %d)", text, fileName, line);
    }
}
