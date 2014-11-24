package com.bondar.gm;

import com.bondar.geom.Point2D;
import com.bondar.geom.Solid3D;
import com.bondar.geom.Polygon3DInds;
import com.bondar.geom.Point3D;
import com.bondar.geom.Polygon3D;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.bondar.tools.Files;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 *
 * @author truebondar
 */
public class FileLoader {
    
    private static final String MODELS_EXTENSION = ".gmx";
    private static final String SEPARATOR = " ";
    private static final String TEXTURE_POINTS_SEPARATOR = ",";
    private static final String NAME_PARAM = "name:";
    private static final String POSITION_PARAM = "pos:";
    private static final String ANGLES_PARAM = "angles:";
    private static final String SCALE_PARAM = "scale:";
    private static final String VERTEXES_PARAM = "verts:";
    private static final String POLYGONS_PARAM = "polyns:";
    private static final String ATTRIBUTES_PARAM = "attr:";
    private static final String ATTRIBUTES_MATERIAL = "mater:";
    
    
    /////////////////////////////////////////////////////////
    public static HashMap<Integer, Texture> readTexturesDir(String dirName, String listFileName) throws Exception {
	HashMap<Integer, Texture> res = new HashMap<>();
	BufferedReader reader = new BufferedReader(
		new InputStreamReader(new FileInputStream(dirName + listFileName), "UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
	    String[] words = line.split(SEPARATOR);
            if (words.length < 2) continue;
            
	    int id = Integer.parseInt(words[0]);
            String imageName = words[1];
            try {
                BufferedImage image = ImageIO.read(new File(dirName + imageName));
                Texture texture = new Texture(id, image);
                res.put(id, texture);
            } catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }
        }
        reader.close();
	return res;
    }
    
    /////////////////////////////////////////////////////////
    public static List<Solid3D> readModelsDir(String dirName) throws Exception {
	List<Solid3D> res = new ArrayList<>();
	String[] solidsFiles = Files.getMaskedFilesInDir(dirName, MODELS_EXTENSION);
	for (String fileName : solidsFiles) {
	    Solid3D solid = FileLoader.readGMXFile(dirName + fileName);
	    res.add(solid);
	}
	return res;
    }

    /////////////////////////////////////////////////////////
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
	Point3D[] points = null;
	Polygon3DInds[] polies = null;
	
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
		    points = new Point3D[vertCount];
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
			points[lineNum] = new Point3D(coords[0], coords[1], coords[2]);
			lineNum++;
		    }
		    break;
		// polygons vertex indexes
		case POLYGONS_PARAM:
		    int poliesNum= Integer.parseInt(words[1]);
		    polies = new Polygon3DInds[poliesNum];
		    lineNum = 0;
		    while (lineNum < poliesNum) {
			if ((line = reader.readLine()) == null) {
			    throw new RuntimeException(buildErrorText(
				    "Wrong indexes number",fileName, fileLineNum + lineNum));
			}
			String[] polyStr = line.split(SEPARATOR);
			int polyStrSize = polyStr.length;
                        // min parameters = indsNum(1),indexes(1),attributes(1),color(1)
			if (polyStrSize < 4) {
			    throw new RuntimeException(buildErrorText(
				    "Wrong index format",fileName, fileLineNum + lineNum));
			}
			// indexes
			int indexesNum = Integer.parseInt(polyStr[0]);
			int[] indexes = new int[indexesNum];
			for (int i = 1; i < indexesNum + 1; i++) {
			    indexes[i-1] = Integer.parseInt(polyStr[i]);
			}
			// transparency
			float transparency = 0;
			if (polyStrSize > indexesNum + 1) {
			    transparency = Float.parseFloat(polyStr[indexesNum + 1]);
			}
			// attributes
			int polyAttribs = 0;
			if (polyStrSize > indexesNum + 2) {
			    polyAttribs = Integer.parseInt(polyStr[indexesNum + 2]);
			}
                        if (Polygon3D.isSetAttribute(polyAttribs, Polygon3D.ATTR_TEXTURED)) {
                            // texture id
                            int textureId = 0;
                            if (polyStrSize > indexesNum + 3) {
                                textureId = Integer.parseInt(polyStr[indexesNum + 3]);
                            }
                            // texture points
                            Point2D[] texturePoints = new Point2D[indexesNum];
                            if (polyStrSize > indexesNum + 4) {
                                String[] pointsStr = polyStr[indexesNum + 4].split(TEXTURE_POINTS_SEPARATOR);
                                if (pointsStr.length < indexesNum*2) {
                                    throw new RuntimeException(buildErrorText(
                                        "Wrong texture points format",fileName, fileLineNum + lineNum));
                                }
                                for (int i = 0; i < indexesNum*2; i += 2) {
                                    int u = Integer.parseInt(pointsStr[i]);
                                    int v = Integer.parseInt(pointsStr[i + 1]);
                                    texturePoints[i/2] = new Point2D(u, v);
                                }
                            }
                            polies[lineNum] = new Polygon3DInds(indexes, transparency, polyAttribs, textureId, texturePoints);
                        } else {
                            // fill color
                            Color fillColor = Color.LIGHT_GRAY;
                            if (polyStrSize > indexesNum + 3) {
                                fillColor = new Color(Integer.parseInt(polyStr[indexesNum + 3], 16));
                            }
                            polies[lineNum] = new Polygon3DInds(indexes, transparency, polyAttribs, fillColor);
                        }
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
	Solid3D res = new Solid3D(name, attribs, points, polies);
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
    
    public static String buildErrorText(String text, String fileName, int line) {
	return String.format("%s in file '%s' (line %d)", text, fileName, line);
    }
}
