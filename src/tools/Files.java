package tools;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author truebondar
 */
public class Files {
    
    // get file name without path & extension
    public static String withOutExtAndPath(String fullFileName) {
	Path path = Paths.get(fullFileName);
	String withoutPath = path.getFileName().toString();
	return withoutPath.replaceFirst("[.][^.]+$", "");
    }
    // get file's names by mask in directory
    public static String[] getMaskedFilesInDir(String dirName, String filesMask) {
	File path = new File(dirName);
	return path.list(new FileFilter(filesMask));
    }
    
    static class FileFilter implements FilenameFilter {
	String endWith;

	public FileFilter(String endWith) {
	    this.endWith = endWith;
	}

	@Override
	public boolean accept(File dir, String name) {
	    return name.toLowerCase().endsWith(endWith);
	}
    }
}
