/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment_1.ik2018;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author Najem
 */
public class ClassificationAgent extends Agent implements RserveCallback {

    //Store the matrix names here for easier solutions, "see below"
    private String[] matrixNames;
    private RConnection c;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " has been created");
        c = getRCon();
        try {
            REXP x = c.eval("R.version.string");
            System.out.println(x.asString());
        } catch (RserveException | REXPMismatchException ex) {
            System.out.println(ex.getMessage());
        }
        //Our behaviour that waits until something happens, i.e it receives a message from DataAgent
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //We receive our message
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("Attempting to retrieve message");
                    try {
                        //We call on transformation which takes the data from the DataAgent and begins it's processes
                        transformation(msg.getContentObject());
                    } catch (UnreadableException ex) {
                        System.out.println(ex.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    /**
     * This method runs every necessary function needed to complete our task
     *
     * @param data the data from DataAgent
     *
     */
    @Override
    public void transformation(Serializable data) {
        List<List<Student>> ourData = ((List<List<Student>>) data);
        List<Student> trainingData = ourData.get(0);
        List<Student> testingData = ourData.get(1);
        String[] labelNames = {"STG", "SCG", "STR", "LPR", "PEG", "UNS"};
        String[] train_Vectors = new String[6];
        String[] test_Vectors = new String[6];
        //for each loop, we run doVector which creates our vectors (training vectors)
        for (int i = 0; i < labelNames.length; i++) {
            train_Vectors[i] = doVector(trainingData, labelNames[i]);
        }
        //Testing vectors
        for (int i = 0; i < labelNames.length; i++) {
            test_Vectors[i] = doVector(testingData, labelNames[i]);
        }

        doVoidAndForm(labelNames, train_Vectors, test_Vectors);
    }

    private void doVoidAndForm(String[] labelNames, String[] train_Vectors, String[] test_Vectors) {
        String[] libs = {"mlbench", "class"};
        //doDataFrame creates our dataframes with the specified label names
        String trainData = doDataFrame(labelNames, "trainingData");
        String testData = doDataFrame(labelNames, "testingData");
        String[] bindData = {"trainingData", "testingData"};
//        String[] dFName = {"trainingData", "testingData"};
//        String[] bData = new String[2];

        try {
            runLibraries(c, libs);
            doEvalArray(c, train_Vectors);
            doEval(c, trainData);
            doEvalArray(c, test_Vectors);
            doEval(c, testData);
            doBindData(c, bindData);
            //https://stats.stackexchange.com/questions/61090/how-to-split-a-data-set-to-do-10-fold-cross-validation
            createNormFunction(c, "normalize_i");
            normalizeDataframe(c, "allData");
            doKfoldCrossvalidation(c, 5, "allData");
        } finally {
            c.close();
        }
    }

    //For each loop, we run our specified libraries through Rserve 
    @Override
    public void runLibraries(RConnection c, String[] libs
    ) {
        try {
            System.out.println("runLibraries");
            for (String lib : libs) {
                c.voidEval("library(\"" + lib + "\")");
                System.out.println(lib);
            }
            c.eval("set.seed(524)");
        } catch (RserveException ex) {
            System.out.println(ex.getMessage());
        }

    }

    //Create vectors based on case names being the column names
    @Override
    public String doVector(List<Student> data, String cName) {
        String st = cName + "<-c(";
        String comma = ", ";
        for (Student dt : data) {
            switch (cName) {
                case "STG":
                    st = st + dt.getSTG() + comma;
                    break;
                case "SCG":
                    st = st + dt.getSCG() + comma;
                    break;
                case "STR":
                    st = st + dt.getSTR() + comma;
                    break;
                case "LPR":
                    st = st + dt.getLPR() + comma;
                    break;
                case "PEG":
                    st = st + dt.getPEG() + comma;
                    break;
                case "UNS":
                    st = st + dt.getUNS() + comma;
                    break;
            }
        }
        //We need to substring the last value otherwise a comma follows it and that returns an error
        st = st + ")";
        String ss = st.substring(0, st.length() - 3) + "" + st.substring(st.length() - 2);
        return ss;
    }

    //For each loop, we add the vectors to the our data frame
    @Override
    public String doDataFrame(String[] cNames, String dName) {
        String dataFrame = dName + "<-data.frame(";
        for (int i = 0; i < cNames.length; i++) {
            if (i != 5) {
                dataFrame = dataFrame + cNames[i] + ", ";
            } else {
                dataFrame = dataFrame + cNames[i] + ")";
            }
        }
        return dataFrame;
    }

    //We create our normalize function through Rserve
    @Override
    public void createNormFunction(RConnection c, String functionName
    ) {
        String function = "" + functionName + "<-function(x) {\n"
                + "  return ((x - min(x)) / (max(x) - min(x)))}";
        try {
            System.out.println("createNormFunction");
            c.eval(function);
        } catch (RserveException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //We normalize our data frame using the previously created normalize function
    //We also make sure to add our nominal data back, i.e. the target (UNS)
    @Override
    public void normalizeDataframe(RConnection c, String dName) {
        try {
            String ourTargets = "temp <-" + dName + "$UNS";
            String nTrainingData = dName + "<-as.data.frame(lapply(" + dName + "[1:5],normalize_i))";
            String tempTargets = dName + "$UNS<-temp";
            c.eval(ourTargets);
            c.eval(nTrainingData);
            c.eval(tempTargets);
            System.out.println("NormalizeDataFrame");
        } catch (RserveException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Simple eval that we run through Rserve
    @Override
    public void doEval(RConnection c, String eval) {
        try {
            c.eval(eval);
            System.out.println(eval);
        } catch (RserveException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Runs an array of evals through Rserve
    //REXP can be used to capture error message when you're debugging
    @Override
    public void doEvalArray(RConnection c, String[] evals) {
        for (String eval : evals) {
            try {
                REXP response = c.parseAndEval("try(eval(" + eval + "))");
                if (response.inherits("try-error")) {
                    System.out.println(response.asString());
                    c.eval(eval);
                } else {
                    System.out.println(eval);
                }
            } catch (REngineException | REXPMismatchException ex) {
                Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //Method that bings our two data sets 
    @Override
    public void doBindData(RConnection c, String[] dataFrames
    ) {
        try {
            System.out.println("doBindData");
            c.eval("allData<-rbind(" + dataFrames[0] + "," + dataFrames[1] + ")");
        } catch (RserveException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //Method that runs our kFold crossvalidation
    @Override
    public void doKfoldCrossvalidation(RConnection c, int k, String data
    ) {
        try {
            System.out.println("kFoldCrossvalidComputing");
            String s = "";
            int kf = k;
            if (kf <= 1) {
                System.out.println("You can't fold by one. kfold requires at least two folds");
            }
            String[] ifs = new String[kf + 1];
            matrixNames = new String[kf + 1];
            //https://stats.stackexchange.com/questions/61090/how-to-split-a-data-set-to-do-10-fold-cross-validation
            //We run our shuffling and folds creation 
            c.eval("theData<-allData[sample(nrow(" + data + ")),]");
            c.eval("folds<-cut(seq(1,nrow(" + data + ")),breaks=" + k + ",labels=FALSE)");
            //For what ever number the user has entered, we create the appropriate string containing the necessary
            //amount of matrixes
            //We also add to matrixNames array for later use
            for (int i = 1; i < kf + 1; i++) {
                if (i == 1) {
                    ifs[i] = "if (i == 1)\n"
                            + "confusionMatrix_" + i + "<-table(Target = testingData$UNS, Predicted = result)";
                    matrixNames[i] = "confusionMatrix_" + i + "";
                } else {
                    ifs[i] = " else if(i == " + i + ")\n"
                            + "confusionMatrix_" + i + "<-table(Target = testingData$UNS, Predicted = result)\n";
                    matrixNames[i] = "confusionMatrix_" + i + "";
                }
                s = s + ifs[i];
            }
            //loop amount is based on the user input stored in kf
            String crossvalidation = "for(i in 1:" + kf + "){\n"
                    + "testIdx<-which(folds==i,arr.ind=TRUE)\n"
                    + "testingData<-theData[testIdx, ]\n"
                    + "trainingData<-theData[-testIdx, ]\n"
                    + "result<-knn(trainingData[1:5], testingData[1:5], trainingData$UNS)\n"
                    + s
                    + "}";
            //We send the string to our computation method to be run
            doComputations(c, crossvalidation);
        } catch (RserveException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

        }

    }

    @Override
    public void doComputations(RConnection c, String s
    ) {
        REXP x;
        System.out.println("doComputations");
        try {
//            String res = c.eval("paste(capture.output(" + s + "),collapse='\\n')").asString();
//            System.out.println(res);
            x = c.parseAndEval("try(" + s + ",silent=TRUE)");
            if (!x.inherits("try-error")) {
                //For each loop of the matrixNames length
                for (int i = 1; i < matrixNames.length; i++) {
                    //We print out each matrix
                    String result = c.eval("paste(capture.output(" + matrixNames[i] + "), collapse='\\n')").asString();
                    System.out.println("\n" + result);
                    //Create our accuracy variables and print them under the matrixes
                    String ACC = "ACC<-(sum(diag(" + matrixNames[i] + "))/sum(" + matrixNames[i] + "))*100";
                    double ACCresult = c.eval(ACC).asDouble();
                    System.out.println("Accuracy: " + ACCresult);
                    //https://stackoverflow.com/questions/33081702/accuracy-precision-and-recall-for-multi-class-model?rq=1
                    //Create our precision variables and print them under the Accuracy
                    String precision = "precision_" + i + " <- diag(" + matrixNames[i] + ")/colSums(" + matrixNames[i] + ")*100";
                    c.eval(precision);
                    String pResult = c.eval("paste(capture.output(precision_" + i + "), collapse='\\n')").asString();
                    System.out.println("\n" + pResult);
                }
            } else {
                System.out.println(x.asString());
            }
        } catch (RserveException | REXPMismatchException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REngineException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Creates and returns an instance of an RConnection 
    @Override
    public RConnection getRCon() {
        RConnection c;
        try {
            c = new RConnection();
            return c;
        } catch (RserveException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
