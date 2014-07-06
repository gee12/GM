package com.bondar.tools;

import java.util.Comparator;

/**
 *
 * @author truebondar
 */
public class Algorithms {
    
    
    public class TComparator<T extends Comparable> implements Comparator<T> {
	
	//private String compareField;
	private boolean isAscending;
 
	public TComparator(/*String field, */boolean isAscending) {
	    //this.compareField = field;
	    this.isAscending = isAscending;
	}
	
	@Override
	public int compare(T obj1, T obj2) {
	    /*try {
		Object value1 = obj1.getClass().getDeclaredField(compareField).get(obj1);
		Object value2 = obj2.getClass().getDeclaredField(compareField).get(obj2);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }*/
	    if (isAscending)
		return obj1.compareTo(obj2);
	    else return obj2.compareTo(obj1);
	}
    };
}
