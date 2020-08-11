/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment_1.ik2018;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author Najem
 */
class XlsToObject {

    private Workbook wb;
    private int sheetAt;
    private ArrayList<Student> studentList;

    /**
     * Creates an instance of this class
     *
     * @param workbook    the workbook of our data
     * @param sheetNumber specify the sheet number of said said
     **/
    XlsToObject(Workbook workbook, int sheetNumber) {
        this.wb = workbook;
        this.sheetAt = sheetNumber;
        studentList = new ArrayList<>();
    }

    /**
     * Creates an arraylist of student type by mapping the cells to student fields
     *
     * @return the arraylist of student
     * @throws java.io.IOException
     **/
    ArrayList<Student> getData() throws IOException {
        try {
            Sheet sheet = wb.getSheetAt(sheetAt);
            DataFormatter formatter = new DataFormatter();
            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
                Student student = new Student();
                Row row = sheet.getRow(i);
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (j == 0) {
                        String stg = formatter.formatCellValue(cell);
                        student.setSTG(stg);
                    }
                    if (j == 1) {
                        String scg = formatter.formatCellValue(cell);
                        student.setSCG(scg);
                    }
                    if (j == 2) {
                        String str = formatter.formatCellValue(cell);
                        student.setSTR(str);
                    }
                    if (j == 3) {
                        String lpr = formatter.formatCellValue(cell);
                        student.setLPR(lpr);
                    }
                    if (j == 4) {
                        String peg = formatter.formatCellValue(cell);
                        student.setPEG(peg);
                    }
                    if (j == 5) {
                        String u = cell.getStringCellValue();
                        String uns = '"' + u + '"';
                        student.setUNS(uns);
                    }
                }
                studentList.add(student);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            wb.close();
        }
        return studentList;
    }

}
