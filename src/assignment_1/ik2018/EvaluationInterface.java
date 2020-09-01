package assignment_1.ik2018;

import org.rosuda.REngine.Rserve.RConnection;

/**
 * @author Najem
 */
public interface EvaluationInterface {

    /**
     * Runs an eval through Rserve
     *
     * @param c    the RConnection object
     * @param eval string of the code to run in Rserve
     **/
    void doEval(RConnection c, String eval);

    /**
     * Run an array of evals through Rserve
     *
     * @param c     the RConnection object
     * @param evals the array of codes to run in Rserve
     **/
    void doEvalArray(RConnection c, String[] evals);
}
