/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment_1.ik2018;

import java.io.Serializable;

/**
 * @author Najem
 */
public class Student implements Serializable {

    private String STG;
    private String SCG;
    private String STR;
    private String LPR;
    private String PEG;
    private String UNS;

    Student() {
    }

    String getSTG() {
        return STG;
    }

    void setSTG(String STG) {
        this.STG = STG;
    }

    String getSCG() {
        return SCG;
    }

    void setSCG(String SCG) {
        this.SCG = SCG;
    }

    String getSTR() {
        return STR;
    }

    void setSTR(String STR) {
        this.STR = STR;
    }

    String getLPR() {
        return LPR;
    }

    void setLPR(String LPR) {
        this.LPR = LPR;
    }

    String getPEG() {
        return PEG;
    }

    void setPEG(String PEG) {
        this.PEG = PEG;
    }

    String getUNS() {
        return UNS;
    }

    void setUNS(String UNS) {
        this.UNS = UNS;
    }

    @Override
    public String toString() {
        return "" + "STG=" + STG + ", SCG=" + SCG + ", STR=" + STR + ", LPR=" + LPR + ", PEG=" + PEG + ", UNS=" + UNS;
    }


}
