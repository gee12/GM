package com.bondar.gm;

/**
 *
 * @author truebondar
 */
public class DrawOrMove {

    public enum Operation {
	MOVE,
	DRAW
    }
    private int index;
    private Operation oper;

    public DrawOrMove(int i, Operation oper) {
	this.index = i;
	this.oper = oper;
    }

    public int getPoint() {
	return index;
    }

    public Operation getOperation() {
	return oper;
    }
    
    public void setPoint(int i) {
	this.index = i;
    }
    
    public void setOperation(Operation op) {
	this.oper = op;
    }
    
    public Point3DOdn getPoint3DOdn(Point3DOdn[] points) {
	return points[index];
    }
}
