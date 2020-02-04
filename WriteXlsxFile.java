package PhD3;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This program writes some data to an Excel file using the Apache POI library.
 * @author www.codejava.net & S.Davod.H
 */

class WriteXlsxFile {

    // Create a blank workbook
    private XSSFWorkbook workbook = new XSSFWorkbook();

    // Method 1-1
    WriteXlsxFile getDataAndsetSheet(int[][] a2DArrayData, String sheetName) {

        // Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(sheetName);

        int rowCount = 0;
        for (int[] aBracket : a2DArrayData) {

            //Create a blank row
            Row row = sheet.createRow(++rowCount);

            int columnCount = 0;
            for (Object field : aBracket) {
                //Create a blank cell
                Cell cell = row.createCell(++columnCount);
                //Put data in cell
                cell.setCellValue((Integer) field);
            }
        }
        return this;
    }

    // Method 1-2
    WriteXlsxFile getDataAndsetSheet(double[][] a2DArrayData, String sheetName) {

        // Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(sheetName);

        int rowCount = 0;
        for (double[] aBracket : a2DArrayData) {

            //Create a blank row
            Row row = sheet.createRow(++rowCount);

            int columnCount = 0;
            for (Object field : aBracket) {
                //Create a blank cell
                Cell cell = row.createCell(++columnCount);
                //Put data in cell
                cell.setCellValue((Double) field);
            }
        }
        return this;
    }

    // Method 1-3
    WriteXlsxFile getDataAndsetSheet(Object[][] a2DObjectData, String sheetName) {

        // An example of Object[][] a2DObjectData:
            //Object[][] bookData = {
                //{"Head First Java", "Kathy Serria", 79},
                //{"Effective Java", "Joshua Bloch", 36},
                //{"Clean Code", "Robert martin", 42},
                //{"Thinking in Java", "Bruce Eckel", 35},
            //};

        // Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(sheetName);

        int rowCount = 0;
        for (Object[] aBracket : a2DObjectData) {

            //Create a blank row
            Row row = sheet.createRow(++rowCount);

            int columnCount = 0;
            for (Object field : aBracket) {
                //Create a blank cell
                Cell cell = row.createCell(++columnCount);
                //Put data in cell
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }
        return this;
    }

    // Method 2: writes the file in the project's directory, so just enter the desired name, like "JavaBooks.xlsx"
    WriteXlsxFile setFileName(String fileName) {
        try {
            FileOutputStream output1 = new FileOutputStream(fileName);
            workbook.write(output1);
        } catch (IOException exc) {
            System.out.println("Error FileWriter (IOException):");
            exc.printStackTrace();
        }
        System.out.println("Excel file " + fileName + " is written successfully :)");
        return this;
    }

    // Method 3: writes the file in a specific path, like "C:\\Users\\hosses8\\Desktop\\JavaBooks.xlsx"
    // This method can be used interchangeably with Method 2
    WriteXlsxFile setFilePathName(String filePathName) {
        try {
            FileOutputStream output2 = new FileOutputStream(new File(filePathName));
            workbook.write(output2);
        } catch (IOException exc) {
            System.out.println("Error FileWriter (IOException):");
            exc.printStackTrace();
        }
        System.out.println("Excel file " + filePathName + " is written successfully :)");
        return this;
    }
}

