/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment_1.ik2018;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
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
 * @author Najem
 */
public class ClassificationAgent extends Agent implements RserveInterface {

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
     */
    @Override
    public void transformation(Serializable data) {
        @SuppressWarnings("unchecked")
        List<List<Student>> ourData = (List<List<Student>>) data;
        List<Student> trainingData = ourData.get(0);
        List<Student> testingData = ourData.get(1);
        String[] labelNames = {"STG", "SCG", "STR", "LPR", "PEG", "UNS"};
        var train_Vectors = new String[6];
        var test_Vectors = new String[6];
        //for each loop, we run doVector which creates our vectors (training vectors)/////
        for (var i = 0; i < labelNames.length; i++) {
            train_Vectors[i] = doVector(trainingData, labelNames[i]);
        }
        //Testing vectors
        for (var i = 0; i < labelNames.length; i++) {
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

        //Each method has to receive a connection otherwise it doesn't work
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
            for (var lib : libs) {
                doEval(c, "library(\"" + lib + "\")");
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
        var st = new StringBuilder(cName + "<-c(");
        String comma = ", ";
        for (var dt : data) {
            switch (cName) {
                case "STG":
                    st.append(dt.getSTG()).append(comma);
                    break;
                case "SCG":
                    st.append(dt.getSCG()).append(comma);
                    break;
                case "STR":
                    st.append(dt.getSTR()).append(comma);
                    break;
                case "LPR":
                    st.append(dt.getLPR()).append(comma);
                    break;
                case "PEG":
                    st.append(dt.getPEG()).append(comma);
                    break;
                case "UNS":
                    st.append(dt.getUNS()).append(comma);
                    break;
            }
        }
        //We need to substring the last value otherwise a comma follows it and that returns an error
        st.append(")");
        return st.substring(0, st.length() - 3) + "" + st.substring(st.length() - 2);
    }

    //For each loop, we add the vectors to the our data frame
    @Override
    public String doDataFrame(String[] cNames, String dName) {
        var dataFrame = new StringBuilder(dName + "<-data.frame(");
        for (var i = 0; i < cNames.length; i++) {
            if (i != 5) {
                dataFrame.append(cNames[i]).append(", ");
            } else {
                dataFrame.append(cNames[i]).append(")");
            }
        }
        return dataFrame.toString();
    }

    //We create our normalize function through Rserve
    @Override
    public void createNormFunction(RConnection c, String functionName
    ) {
        String function = "" + functionName + "<-function(x) {\n"
                + "  return ((x - min(x)) / (max(x) - min(x)))}";
        System.out.println("createNormFunction");
        doEval(c, function);
        //c.eval(function);
    }

    //We normalize our data frame using the previously created normalize function
    //We also make sure to add our nominal data back, i.e. the target (UNS)
    @Override
    public void normalizeDataframe(RConnection c, String dName) {
        String ourTargets = "temp <-" + dName + "$UNS";
        String nTrainingData = dName + "<-as.data.frame(lapply(" + dName + "[1:5],normalize_i))";
        String tempTargets = dName + "$UNS<-temp";
        String[] evals = new String[]{ourTargets, nTrainingData, tempTargets};
        doEvalArray(c, evals);
        System.out.println("NormalizeDataFrame");
    }

    //Simple eval that we run through Rserve
    @Override
    public void doEval(RConnection c, String eval) {
        try {
            c.eval(eval);
        } catch (RserveException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Runs an array of evals through Rserve
    //REXP can be used to capture error message when you're debugging
    @Override
    public void doEvalArray(RConnection c, String[] evals) {
        for (var eval : evals) {
            try {
                REXP response = c.parseAndEval("try(eval(" + eval + "))");
                if (response.inherits("try-error")) {
                    System.out.println(response.asString());
                    c.eval(eval);
                    return;
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
        System.out.println("doBindData");
        doEval(c, "allData<-rbind(" + dataFrames[0] + "," + dataFrames[1] + ")");
    }

    //Method that runs our kFold crossvalidation
    @Override
    public void doKfoldCrossvalidation(RConnection c, int k, String data
    ) {
        System.out.println("kFoldCrossvalidComputing");
        var s = new StringBuilder();
        if (k <= 1) {
            System.out.println("You can't fold by one. kfold requires at least two folds");
        }
        var ifs = new String[k + 1];
        matrixNames = new String[k + 1];
        //https://stats.stackexchange.com/questions/61090/how-to-split-a-data-set-to-do-10-fold-cross-validation
        //We run our shuffling and folds creation 
        doEval(c, "theData<-allData[sample(nrow(" + data + ")),]");
        doEval(c, "folds<-cut(seq(1,nrow(" + data + ")),breaks=" + k + ",labels=FALSE)");
        //For what ever number the user has entered, we create the appropriate string containing the necessary
        //amount of matrixes
        //We also add to matrixNames array for later use
        for (var i = 1; i < k + 1; i++) {
            if (i == 1) {
                ifs[i] = "if (i == 1)\n"
                        + "confusionMatrix_" + i + "<-table(Target = testingData$UNS, Predicted = result)";
                matrixNames[i] = "confusionMatrix_" + i + "";
            } else {
                ifs[i] = " else if(i == " + i + ")\n"
                        + "confusionMatrix_" + i + "<-table(Target = testingData$UNS, Predicted = result)\n";
                matrixNames[i] = "confusionMatrix_" + i + "";
            }
            s.append(ifs[i]);
        }
        //loop amount is based on the user input stored in kf
        String crossvalidation = "for(i in 1:" + k + "){\n"
                + "testIdx<-which(folds==i,arr.ind=TRUE)\n"
                + "testingData<-theData[testIdx, ]\n"
                + "trainingData<-theData[-testIdx, ]\n"
                + "result<-knn(trainingData[1:5], testingData[1:5], trainingData$UNS)\n"
                + s
                + "}";
        //We send the string to our computation method to be run
        doComputations(c, crossvalidation);
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
                for (var i = 1; i < matrixNames.length; i++) {
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
                    doEval(c, precision);
                    String pResult = c.eval("paste(capture.output(precision_" + i + "), collapse='\\n')").asString();
                    System.out.println("\n" + pResult);
                }
            } else {
                System.out.println(x.asString());
            }
        } catch (REXPMismatchException | REngineException ex) {
            Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            c.close();
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
