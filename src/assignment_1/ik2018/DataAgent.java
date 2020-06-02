/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment_1.ik2018;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Najem
 */
public class DataAgent extends Agent {

    private Workbook workbook;

    @Override
    protected void setup() {
        try {
            System.out.println(getLocalName() + " has been created");
            getCellValues();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private OneShotBehaviour getCellValues() throws IOException {
        ArrayList ourData = new ArrayList();
        try {
            //We get our workbook
            workbook = getWorkBook();
            //Create two instances of our XlsToObject class with specified sheet numbers
            XlsToObject tr = new XlsToObject(workbook, 1);
            XlsToObject te = new XlsToObject(workbook, 2);
            System.out.println(tr.getData().toArray().length + "\n" + te.getData().toArray().length);
            //Add the data to our arraylist
            ourData.add(tr.getData());
            ourData.add(te.getData());
        } finally {
            workbook.close();
        }
        sendMsg(ourData);
        return null;
    }
    
    private void sendMsg(ArrayList ourData){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        //We add a receiver and specify a language
        msg.addReceiver(new AID("V", AID.ISLOCALNAME));
        msg.setLanguage("JavaSerialization");
        //We send our data as content object cast down as Serializable
        try {
            msg.setContentObject((Serializable) ourData);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        send(msg);
    }

    private Workbook getWorkBook() throws IOException {
        Workbook wb = WorkbookFactory.create(new File("Data_User_Modeling_Dataset_Hamdi Tolga KAHRAMAN.xls"));
        return wb;
    }

}
