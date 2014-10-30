/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bondar.gm;

/**
 *
 * @author Иван
 */
public class MatrixMathematics {
 
    public static void main(String[] args) {
        double[][] a = {{2,3,4},{4,5,6},{6,7,8}};
        Matrix m = new Matrix(a);
        Matrix m1 = inverse(m);

        System.out.println(toString(m));
        System.out.println(toString(m1));
    }
    
    public static String toString(Matrix m) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < m.getRows(); i++) {
            for (int j = 0; j < m.getCols(); j++) {
                res.append(m.getAt(i, j));
                res.append("\t");
            }
            res.append("\n");
        }
        return res.toString();
    }
     
     /**
     * This class a matrix utility class and cannot be instantiated.
     */
    private MatrixMathematics(){}
    /**
     * Transpose of a matrix - Swap the columns with rows
     * @param matrix
     * @return
     */
    public static Matrix transpose(Matrix matrix) {
        Matrix transposedMatrix = new Matrix(matrix.getCols(), matrix.getRows());
        for (int i=0;i<matrix.getRows();i++) {
            for (int j=0;j<matrix.getCols();j++) {
                transposedMatrix.setAt(j, i, matrix.getAt(i, j));
            }
        }
        return transposedMatrix;
    }
    
    /**
     * Inverse of a matrix - A-1 * A = I where I is the identity matrix
     * A matrix that have inverse is called non-singular or invertible. If the matrix does not have inverse it is called singular.
     * For a singular matrix the values of the inverted matrix are either NAN or Infinity
     * Only square matrices have inverse and the following method will throw exception if the matrix is not square.
     * @param matrix
     * @return
     * @throws NoSquareException
     */
    public static Matrix inverse(Matrix matrix) /*throws NoSquareException*/ {
        return (transpose(cofactor(matrix)).multiplyConst(1.0/determinant(matrix)));
    }
    
    /**
     * Determinant of a square matrix
     * The following function find the determinant in a recursively. 
     * @param matrix
     * @return
     * @throws NoSquareException
     */
    public static double determinant(Matrix matrix) /*throws NoSquareException*/ {
        /*if (!matrix.isSquare())
            throw new NoSquareException("matrix need to be square.");*/
        
        //if (matrix.size()==2) {
        if (matrix.getCols() == 2 && matrix.getRows() == 2) {
            return (matrix.getAt(0, 0) * matrix.getAt(1, 1)) - ( matrix.getAt(0, 1) * matrix.getAt(1, 0));
        }
        double sum = 0.0;
        for (int i=0; i<matrix.getCols(); i++) {
            sum += sign(i) * matrix.getAt(0, i) * determinant(createSubMatrix(matrix, 0, i));
        }
        return sum;
    }
 
    /**
     * Determine the sign - even numbers have sign + and odds -
     * @param i
     * @return
     */
    private static int sign(int i) {
        if (i%2==0)
            return 1;
        return -1;
    }
    /**
     * Creates a submatrix excluding the given row and column
     * @param matrix
     * @param excluding_row
     * @param excluding_col
     * @return
     */
    public static Matrix createSubMatrix(Matrix matrix, int excluding_row, int excluding_col) {
        Matrix mat = new Matrix(matrix.getRows()-1, matrix.getCols()-1);
        int r = -1;
        for (int i=0;i<matrix.getRows();i++) {
            if (i==excluding_row)
                continue;
                r++;
                int c = -1;
            for (int j=0;j<matrix.getCols();j++) {
                if (j==excluding_col)
                    continue;
                mat.setAt(r, ++c, matrix.getAt(i, j));
            }
        }
        return mat;
    }
    
    /**
     * The cofactor of a matrix
     * @param matrix
     * @return
     * @throws NoSquareException
     */
    public static Matrix cofactor(Matrix matrix) /*throws NoSquareException*/ {
        Matrix mat = new Matrix(matrix.getRows(), matrix.getCols());
        for (int i=0;i<matrix.getRows();i++) {
            for (int j=0; j<matrix.getCols();j++) {
                mat.setAt(i, j, sign(i) * sign(j) * determinant(createSubMatrix(matrix, i, j)));
            }
        }
        
        return mat;
    }
    
    /**
     * Adds two matrices of the same dimension
     * @param matrix1
     * @param matrix2
     * @return
     * @throws IllegalDimensionException
     */
    public static Matrix add(Matrix matrix1, Matrix matrix2) /*throws IllegalDimensionException */{
        /*if (matrix1.getCols() != matrix2.getCols() || matrix1.getRows() != matrix2.getRows())
            throw new IllegalDimensionException("Two matrices should be the same dimension.");*/
        Matrix sumMatrix = new Matrix(matrix1.getRows(), matrix1.getCols());
        for (int i=0; i<matrix1.getRows();i++) {
            for (int j=0;j<matrix1.getCols();j++) 
                sumMatrix.setAt(i, j, matrix1.getAt(i, j) + matrix2.getAt(i,j));
            
        }
        return sumMatrix;
    }
    
    /**
     * subtract two matrices using the above addition method. Matrices should be the same dimension.
     * @param matrix1
     * @param matrix2
     * @return
     * @throws IllegalDimensionException
     */
    public static Matrix subtract(Matrix matrix1, Matrix matrix2) /*throws IllegalDimensionException */{
        return add(matrix1,matrix2.multiplyConst(-1));
    }
    
    /**
     * Multiply two matrices
     * 
     * @param matrix1
     * @param matrix2
     * @return 
     */
    public static Matrix multiply(Matrix matrix1, Matrix matrix2)  {
        Matrix multipliedMatrix = new Matrix(matrix1.getRows(), matrix2.getCols());
        
        for (int i=0;i<multipliedMatrix.getRows();i++) {
            for (int j=0;j<multipliedMatrix.getCols();j++) {
                double sum = 0.0;
                for (int k=0;k<matrix1.getCols();k++) {
                    sum += matrix1.getAt(i, k) * matrix2.getAt(k, j);
                }
                multipliedMatrix.setAt(i, j, sum);
            }
        }
        return multipliedMatrix;
    }
}