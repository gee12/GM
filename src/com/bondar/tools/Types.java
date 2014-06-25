package com.bondar.tools;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class Types {
    /*public static <T> Object[] toArray(List<T> list) {
	return list.toArray(new Object[list.size()]);
    }*/
    
    public static <T> T[] toArray(List<T> list, Class<T> clazz) {
	return list.toArray((T[])Array.newInstance(clazz, list.size()));
    }
    
    public static <T> List<T> toList(T[] array) {
	return Arrays.asList(array);
    }
}
