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
 *
 * @author Najem
 */
public interface RserveCallback {
    /**Transforms the data from the sending agent 
     * @param data 
     **/
    void transformation(Serializable data);

    /**Uses Rserve to run R libraries
     * @param c the RConnection object 
     * @param libs Array of strings containing specified libraries
     * @throws RserveException
     **/
    public void runLibraries(RConnection c ,String[] libs)throws RserveException;

    /**Creates vector (run in loops for multiple vectors) 
     * @param data list of your data
     * @param cName the column name of your vector
     * @return return a string containing the vector to be run with Rserve
     **/
    public String doVector(List<Student> data, String cName);

    /**Creates a data frame 
     * @param cNames array of column names
     * @param dName name of the data frame
     * @return returns string containing the data frame to be run with Rserve
     **/
    public String doDataFrame(String[] cNames, String dName);

    /**Creates a normalization function
     * @param c the RConnection object
     * @param functionName name of the function 
     **/
    public void createNormFunction(RConnection c ,String functionName);
    
    /**Normalizes a data frame
     * @param c the RConnection object
     * @param dName name of the data frame
     **/
    public void normalizeDataframe(RConnection c ,String dName);

    /**Creates an RConnection object
     * @return returns a an RConnection object
     **/
    public RConnection getRCon();

    /**Runs an eval through Rserve
     * @param c the RConnection object
     * @param eval string of the code to run in Rserve
     **/
    public void doEval(RConnection c ,String eval);

    /**Run an array of evals through Rserve
     * @param c the RConnection object
     * @param evals the array of codes to run in Rserve
     **/
    public void doEvalArray(RConnection c ,String[] evals);
    
    /**Begins the crossvalidation process by shuffling and creating folds
     * @param c the RConnection object
     * @param k integer of k
     * @param data string of the data to be shuffled and folded
     **/
    public void doKfoldCrossvalidation(RConnection c ,int k, String data);

    /**Bind separate data frames into one
     * @param c the RConnection object
     * @param dataFrames array of the data frames
     **/
    public void doBindData(RConnection c ,String[] dataFrames);
    
    /**Does computations (confusion matrix, Accuracy, Precision)
     * @param c the RConnection object
     * @param s string of the code to be run through Rserve
     **/
    public void doComputations(RConnection c ,String s);
      

}
