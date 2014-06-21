package com.bondar.gm;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.bondar.tools.Files;

/**
 *
 * @author truebondar
 */
public class GMXFile {
    
    private static final String MODELS_EXTENSION = ".gmx";
    private static final String SPLIT = " ";
    private static final String NAME_PARAM = "name:";
    private static final String POSITION_PARAM = "pos:";
    private static final String ANGLES_PARAM = "angles:";
    private static final String SCALE_PARAM = "scale:";
    private static final String VERTEXES_PARAM = "verts:";
    private static final String TRIANGLES_PARAM = "trias:";
    private static final String DOMS_PARAM = "doms:";
    
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
	Point3D pos = new Point3D();
	Point3D angles = new Point3D();
	Point3D scale = new Point3D();
	Point3D[] vertexes = null;
	int[][] indsToTrias = null;
	Color[] colors = null;
	DrawOrMove[] domPoints = null;
	
	String line = null;
	while ((line = reader.readLine()) != null) {
	    String[] words = line.split(SPLIT);
	    switch (words[0]) {
		//
		case NAME_PARAM:
		    name = words[1];
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
		    int lineNumber = 0;
		    while (lineNumber < vertCount) {
			if ((line = reader.readLine()) == null) {
			    throw new RuntimeException("Неверное количество кординат в " + fileName);
			}
			String[] coordsStr = line.split(SPLIT);
			int coordsCount = coordsStr.length;
			if (coordsCount < 3) {
			    throw new RuntimeException("Необходимо 3 координаты");
			}
			// vertexes
			double[] coords = new double[3];
			for (int i = 0; i < 3; i++) {
			    coords[i] = Double.parseDouble(coordsStr[i]);
			}
			vertexes[lineNumber] = new Point3D(coords[0], coords[1], coords[2]);
			lineNumber++;
		    }
		    break;
		// triangles vertex indexes
		case TRIANGLES_PARAM:
		    int triasCount = Integer.parseInt(words[1]);
		    indsToTrias = new int[triasCount][];
		    colors = new Color[triasCount];
		    lineNumber = 0;
		    while (lineNumber < triasCount) {
			if ((line = reader.readLine()) == null) {
			    throw new RuntimeException("Неверное количество индексов координат треугольников в " + fileName);
			}
			String[] pointsStr = line.split(SPLIT);
			int pointsCount = pointsStr.length;
			if (pointsCount < 3) {
			    throw new RuntimeException("Необходимо 3 вершины");
			}
			// indexes
			indsToTrias[lineNumber] = new int[3];
			for (int i = 0; i < 3; i++) {
			    indsToTrias[lineNumber][i] = Integer.parseInt(pointsStr[i]);
			}
			// colors
			Color color = Color.BLACK;
			if (pointsCount > 3) {
			    color = new Color(Integer.parseInt(pointsStr[3], 16));
			}
			colors[lineNumber] = color;

			lineNumber++;
		    }		    
		    break;
		// strtucture for edges drawing
		case DOMS_PARAM:
		    int domCount = Integer.parseInt(words[1]);
		    domPoints = new DrawOrMove[domCount];
		    lineNumber = 0;
		    while (lineNumber < domCount) {
			if ((line = reader.readLine()) == null) {
			    throw new RuntimeException("Неверное количество DrawOrMove элементов в " + fileName);
			}
			String[] domStr = line.split(SPLIT);
			int elemCount = domStr.length;
			if (elemCount < 2) {
			    throw new RuntimeException("Необходимо 2 числа (идентификатор точки и тип операции)");
			}
			// draw or moves
			int pointIndex = Integer.parseInt(domStr[0]);
			int domOper = Integer.parseInt(domStr[1]);
			domPoints[lineNumber] = new DrawOrMove(
				pointIndex,
				DrawOrMove.Operation.values()[domOper]);
			lineNumber++;
		    }
		    break;
	    }
	}
	reader.close();
	//
	Solid3D res = null;
	if (domPoints == null && indsToTrias == null)
		res = new Solid3D(name, vertexes);
	else if (domPoints == null)
	    res = new Solid3D(name, vertexes, indsToTrias, colors);
	else 
	    res = new Solid3D(name, vertexes, indsToTrias, colors, domPoints);
	//
	res.updateTransfers(pos.getX(), pos.getY(), pos.getZ());
	res.updateAngles(angles.getX(), angles.getY(), angles.getZ());
	res.updateScale(scale.getX(), scale.getY(), scale. getZ());
	
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
}
