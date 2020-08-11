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

    public String STG;
    public String SCG;
    public String STR;
    public String LPR;
    public String PEG;
    public String UNS;

    public Student() {
    }

    public String getSTG() {
        return STG;
    }

    public void setSTG(String STG) {
        this.STG = STG;
    }

    public String getSCG() {
        return SCG;
    }

    public void setSCG(String SCG) {
        this.SCG = SCG;
    }

    public String getSTR() {
        return STR;
    }

    public void setSTR(String STR) {
        this.STR = STR;
    }

    public String getLPR() {
        return LPR;
    }

    public void setLPR(String LPR) {
        this.LPR = LPR;
    }

    public String getPEG() {
        return PEG;
    }

    public void setPEG(String PEG) {
        this.PEG = PEG;
    }

    public String getUNS() {
        return UNS;
    }

    public void setUNS(String UNS) {
        this.UNS = UNS;
    }

    @Override
    public String toString() {
        return "" + "STG=" + STG + ", SCG=" + SCG + ", STR=" + STR + ", LPR=" + LPR + ", PEG=" + PEG + ", UNS=" + UNS;
    }


}
