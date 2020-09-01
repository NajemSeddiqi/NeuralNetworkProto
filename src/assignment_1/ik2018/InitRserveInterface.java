package assignment_1.ik2018;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public interface InitRserveInterface {
    /**
     * Uses Rserve to run R libraries
     *
     * @param c    the RConnection object
     * @param libs Array of strings containing specified libraries
     * @throws RserveException throws an RserveException
     **/
    void runLibraries(RConnection c, String[] libs) throws RserveException;

    /**
     * Creates an RConnection object
     *
     * @return returns a an RConnection object
     **/
    RConnection getRCon();
}
