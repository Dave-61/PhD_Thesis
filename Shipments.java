package PhD3;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Shipments {
    public static void main(String[] args) {

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();
        double[][] Dmnd; // Arc Length & Population Exposure
        Dmnd = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data.xlsx")
                .setSheet(0).setRow(0, 25).setCell(0, 25)
                .createArrayMatrix();
        // objReadXlsxFile.showArrayMatrix(Dmnd);

        List<List<Integer>> shipments_arrList = new ArrayList<>();
        for (int i = 1; i < Dmnd.length; i++) {
            for (int j = 1; j < Dmnd[i].length; j++) {
                if (Dmnd[i][j] != -1) {
                    List<Integer> newBracket = new ArrayList<>();
                    newBracket.add((int)Dmnd[i][0]);
                    newBracket.add((int)Dmnd[0][j]);
                    newBracket.add((int)Dmnd[i][j]);
                    shipments_arrList.add(newBracket);
                }
            }
        }
        // Write shipments_arrList into a text file
        try {
            FileWriter writer = new FileWriter("shipments.txt");
            writer.write("Shipments (the first two numbers show the origin and destination, " +
                    "and the last number shows the number of hazmat railcars):");
            writer.write(System.getProperty( "line.separator" ));
            for (List<Integer> bracket : shipments_arrList) {
                //System.out.println(bracket);
                writer.write(Arrays.toString(bracket.toArray()));
                writer.write(System.getProperty( "line.separator" ));
            }
            writer.close();
        } catch (IOException exc) {
            System.out.println("Error FileWriter (IOException):");
            exc.printStackTrace();
        }
        // Convert shipments_arrList from arrayList to array (shipments)
        int [][] shipments = new int[shipments_arrList.size()][];
        for (int k = 0; k < shipments_arrList.size(); k++) {
            List<Integer> currentBracket = new ArrayList<>(shipments_arrList.get(k));
            shipments[k] = currentBracket.stream().mapToInt(i -> i).toArray();
        }
        // Write shipments into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile1 = new WriteXlsxFile();
        objWriteXlsxFile1.getDataAndsetSheet(shipments,"Shipments").setFileName("shipments.xlsx");
    }
}
