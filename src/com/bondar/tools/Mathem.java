package com.bondar.tools;

/**
 *
 * @author truebondar
 */
public class Mathem {
    public static final double EPSILON_E5 = 1e-5;
    public static final double EPSILON_E6 = 1e-6;
    
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
}
