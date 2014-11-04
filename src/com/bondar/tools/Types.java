package com.bondar.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author truebondar
 */
public class Types {
    
    public static <T> T[] toArray(List<T> list, Class<T> clazz) {
	return list.toArray((T[])Array.newInstance(clazz, list.size()));
    }
    
    public static <T> List<T> toList(T[] array) {
	return Arrays.asList(array);
    }
    
    public static String[] getClassFieldsTypesValues(Object o) throws IllegalArgumentException, IllegalAccessException {
	Field[] fields = o.getClass().getFields();
	String[] res = new String[fields.length];
	for (int i = 0; i < fields.length; i++) {
	    res[i] = String.format("%s $s = $s", fields[i].getGenericType(), fields[i].getName(),  fields[i].get(o));
	}
	return res;
    }
    
    public static boolean isEquals(double d1, double d2) {
        return ((Math.abs(d1 - d2) < Mathem.EPSILON_E5));
    }
}
