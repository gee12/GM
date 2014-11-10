package com.bondar.tools;

/**
 *
 * @author truebondar
 */
public class Mathem {
    public static final double EPSILON_E5 = 1e-5;
    public static final double EPSILON_E6 = 1e-6;
    public static final double EPSILON_E7 = 1e-7;
    
    /////////////////////////////////////////////////////
    public static int factorial(int n) {
	if (n > 1) {
	    n *= factorial(n - 1);
	}
	return n;
    }

    /////////////////////////////////////////////////////
    public static int summ(int n) {
	return (n * (n + 1) / 2);
    }
    
    /////////////////////////////////////////////////////
    public static double abs(double value) {
        return ((value >= 0) ? value : -value);
    }
    
    public static int abs(int value) {
        return ((value >= 0) ? value : -value);
    }
    
    /////////////////////////////////////////////////////
    public static boolean isEquals5(double d1, double d2) {
        return ((Mathem.abs(d1 - d2) < Mathem.EPSILON_E5));
    }
    
    /////////////////////////////////////////////////////
    public static double returnFirst(double a, double b) {
        return a;
    }
    
    /////////////////////////////////////////////////////
    public static int ceil(double x) {
        /*if (x < 0) return (int)x;
        else if ((int)x < x) return ((int)x + 1);
        return x;*/
        if (x >= 0 && (int)x < x) return ((int)x + 1);
        else return (int)x;
    }
}
