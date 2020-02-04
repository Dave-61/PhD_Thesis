package PhD3;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This program first checks if there is any NULL rows or cells in a sheet of an Excel file (xlsx),
 * then puts the NUMBERS exist in each row into a 2D Array.
 * @ S.Davod.H
 */

class ReadXlsxFile {
    private InputStream fileToRead;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private int rowStart;
    private int rowEnd;
    private int cellStart;
    private int cellMax;

    // Method 1
    ReadXlsxFile setFilePath(String filePath) {
        try {
            fileToRead = new FileInputStream(filePath);
            // Construct a XSSFWorkbook object from the given file
            workbook = new XSSFWorkbook(fileToRead);
        } catch (Throwable exc) {
            System.out.println("Error_1 (Throwable):");
            System.out.println(exc);
        }
        return this;
    }

    // Method 2-1: Get the sheet from the workbook using sheet name
    ReadXlsxFile setSheet(String sheetName) {
        sheet = workbook.getSheet(sheetName);
        return this;
    }
    // Method 2-2: We can also pass the index of the sheet which starts from '0'
    ReadXlsxFile setSheet(int sheetNumber) {
        try {
            sheet = workbook.getSheetAt(sheetNumber);
        } catch (IllegalArgumentException exc) {
            System.out.println("Error_2: The sheet index is out of range!");
        }
        return this;
    }

    // Method 3
    ReadXlsxFile setRow(int rowStart, int rowEnd) {
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        return this;
    }
    // Method 4
    ReadXlsxFile setCell(int cellStart, int cellMax) {
        this.cellStart = cellStart;
        this.cellMax = cellMax;
        return this;
    }

    // Method 5
    double[][] createArrayMatrix() {

        List<List<Number>> arrLisMatrix = new ArrayList<>();

        // Iterating the selected rows of the sheet
        for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {

            XSSFRow row = sheet.getRow(rowNum);

            if (row == null) {
                System.out.println("Row #" + rowNum + " is NULL!");
                System.out.println("The program is terminating ...");
                System.exit(0);
            }

            arrLisMatrix.add(new ArrayList<>());

            int rowIndex = rowNum - rowStart; // rowIndex = (rowNum - number of first undesired rows)
            //System.out.println("rowIndex " + rowIndex +" -------------------------------------------");

            // Iterating the selected cells of the current row
            for (int cellNum = cellStart; cellNum < Math.min(cellMax+1, row.getLastCellNum()); cellNum++) {

                XSSFCell cell = row.getCell(cellNum);

                if (cell == null || cell.getCellTypeEnum() == CellType.BLANK) {
                    System.out.println("Cell #" + cellNum + " in row #" + rowNum + " is EMPTY!");
                    System.out.println("The program is terminating ...");
                    System.exit(0);
                }
                else if(cell.getCellTypeEnum() == CellType.STRING) {
                    //System.out.printf("%-15s", cell.getStringCellValue());
                    System.out.println("Cell #" + cellNum + " in row #" + rowNum + " is STRING!");
                    System.out.println("The program is terminating ...");
                    System.exit(0);
                }
                else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                    //System.out.printf("%-15.0f", cell.getNumericCellValue());
                    arrLisMatrix.get(rowIndex).add(cell.getNumericCellValue());
                }
                else if (cell.getCellTypeEnum() == CellType.FORMULA) {
                    if (cell.getCachedFormulaResultTypeEnum() == CellType.STRING) {
                        //System.out.printf("%-15s", cell.getStringCellValue());
                        System.out.println("Cell #" + cellNum + " in row #" + rowNum + " is STRING!");
                        System.out.println("The program is terminating ...");
                        System.exit(0);
                    }
                    else if (cell.getCachedFormulaResultTypeEnum() == CellType.NUMERIC) {
                        //System.out.printf("%-15.0f", cell.getNumericCellValue());
                        arrLisMatrix.get(rowIndex).add(cell.getNumericCellValue());
                    }
                }
                // Here if required, we can also add below methods to read the cell content:
                // CellType.BOOLEAN, CellType.ERROR
            }
        }
        System.out.println();
        //System.out.println("NUMBER cells of each row in a 2D ArrayList:");
        //System.out.println(arrLisMatrix);

        //Remove all the empty elements of the ArrayList Matrix
        int i=0;
        while (i<arrLisMatrix.size()) {
            if (arrLisMatrix.get(i).isEmpty())
                arrLisMatrix.remove(arrLisMatrix.get(i));
            else
                i++;
        }

        // Now convert the ArrayList Matrix to an Array Matrix
        double[][] arrayMatrix = new double[arrLisMatrix.size()][];

        for (i=0; i<arrLisMatrix.size(); i++) {
            arrayMatrix[i] = new double[arrLisMatrix.get(i).size()];
            for (int j=0; j<arrLisMatrix.get(i).size(); j++) {
                arrayMatrix[i][j] = (arrLisMatrix.get(i).get(j)).doubleValue();
            }
        }
        System.out.println("Excel data from sheet " + sheet.getSheetName() + " is imported and saved as an array matrix :)");

        try {
             fileToRead.close();
        } catch (IOException exc) {
            System.out.println("Error_3 (IOException):");
            System.out.println(exc);
        }

        return arrayMatrix;
    }

    // Method 6
    ReadXlsxFile showArrayMatrix(double[][] arrayMatrix) {
        System.out.println();
        System.out.println("Array Matrix:");
        System.out.println(Arrays.deepToString(arrayMatrix));
        System.out.println();
        // Show the arrayMatrix like a matrix
        for (double[] x : arrayMatrix) {
            for (double y : x) {
                System.out.printf("%-10.1f", y);
            }
            System.out.println();
        }
        return this;
    }
}