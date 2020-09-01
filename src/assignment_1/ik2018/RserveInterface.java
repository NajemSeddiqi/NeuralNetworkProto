/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment_1.ik2018;

import java.io.Serializable;
import java.util.List;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * @author Najem
 */
public interface RserveInterface extends EvaluationInterface, InitRserveInterface {
    /**
     * Transforms the data from the sending agent
     *
     * @param data whatever data
     **/
    void transformation(Serializable data);

    /**
     * Creates a data frame
     *
     * @param cNames array of column names
     * @param dName  name of the data frame
     * @return returns string containing the data frame to be run with Rserve
     **/
    String doDataFrame(String[] cNames, String dName);

    /**
     * Creates a normalization function
     *
     * @param c            the RConnection object
     * @param functionName name of the function
     **/
    void createNormFunction(RConnection c, String functionName);

    /**
     * Normalizes a data frame
     *
     * @param c     the RConnection object
     * @param dName name of the data frame
     **/
    void normalizeDataframe(RConnection c, String dName);

    /**
     * Begins the crossvalidation process by shuffling and creating folds
     *
     * @param c    the RConnection object
     * @param k    integer of k
     * @param data string of the data to be shuffled and folded
     **/
    void doKfoldCrossvalidation(RConnection c, int k, String data);

    /**
     * Bind separate data frames into one
     *
     * @param c          the RConnection object
     * @param dataFrames array of the data frames
     **/
    void doBindData(RConnection c, String[] dataFrames);

    /**
     * Does computations (confusion matrix, Accuracy, Precision)
     *
     * @param c the RConnection object
     * @param s string of the code to be run through Rserve
     **/
    void doComputations(RConnection c, String s);

}
