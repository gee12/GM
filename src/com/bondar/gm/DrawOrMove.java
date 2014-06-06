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
    private Point3DOdn p;
    private Operation oper;

    public DrawOrMove(Point3DOdn p, Operation oper) {
	this.p = p;
	this.oper = oper;
    }

    public Point3DOdn getPoint() {
	return p;
    }

    public Operation getOperation() {
	return oper;
    }
    
    public void setPoint(Point3DOdn p) {
	this.p = p;
    }
    
    public void setOperation(Operation op) {
	this.oper = op;
    }
}
