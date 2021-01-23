package PhD3;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ArcYardInTS {
    public static void main(String[] args) {

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();
        double[][] ALPE; // Arc Length & Population Exposure
        ALPE = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data.xlsx")
                .setSheet(1).setRow(1,2).setCell(1,167)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ALPE);

        List<List<Integer>> arcsInTS = new ArrayList<>();  // arcs in train services (TS)
        arcsInTS.add(Arrays.asList(-1,-1)); // it will be removed at the end

        for (int j = 0; j < ALPE[0].length-1; j++) {
            if (ALPE[1][j] == ALPE[1][j+1]) { // only if two nodes (the arc) are in the same TS
                boolean arcIncluded = false;
                int K = -1;
                for (int k = 0; k < arcsInTS.size(); k++) {
                    if (ALPE[0][j] == arcsInTS.get(k).get(0) && ALPE[0][j + 1] == arcsInTS.get(k).get(1)) {
                        arcIncluded = true; // the arc is already added to the matrix (arcsInTS) -
                        K = k;                                // - at the Kth bracket
                        break;                                    // - no action needed
                    }
                }
                if (arcIncluded) {
                    boolean TSIncluded = false;
                    List<Integer> currentBracket = new ArrayList<>(arcsInTS.get(K));
                    for (int q = 2; q < currentBracket.size(); q++) {
                        if (ALPE[1][j] == currentBracket.get(q)) {
                            TSIncluded = true; // the TS of the arc is already added to the matrix (arcsInTS) -
                            break;                             // - no action needed
                        }
                    }
                    if (!TSIncluded) arcsInTS.get(K).add((int)ALPE[1][j]); // added the new TS of the arc -
                }                                                   // - to the current bracket of the matrix (arcsInTS)
                else {
                    List<Integer> newBracket = new ArrayList<>();
                    newBracket.add((int)ALPE[0][j]);
                    newBracket.add((int)ALPE[0][j+1]);
                    newBracket.add((int)ALPE[1][j]);
                    arcsInTS.add(newBracket); // the new arc (two nodes) and its TS are added to the matrix (arcsInTS)
                }
            }
        }
        arcsInTS.remove(0);

        // Write arcsInTS into a text file
        try {
            FileWriter writer = new FileWriter("arcsInTS.txt");
            writer.write("Arcs in TS (the first two numbers show the arc, and the rest show the TS):");
            writer.write(System.getProperty( "line.separator" ));
            for (List<Integer> bracket : arcsInTS) {
                //System.out.println(bracket);
                writer.write(Arrays.toString(bracket.toArray()));
                writer.write(System.getProperty( "line.separator" ));
            }
            writer.close();
        } catch (IOException exc) {
            System.out.println("Error FileWriter (IOException):");
            exc.printStackTrace();
        }
        // Convert arcsInTS from arrayList to array (arcsInTS_array)
        int [][] arcsInTS_array = new int[arcsInTS.size()][];
        for (int k = 0; k < arcsInTS.size(); k++) {
            List<Integer> currentBracket = new ArrayList<>(arcsInTS.get(k));
            arcsInTS_array[k] = currentBracket.stream().mapToInt(i -> i).toArray();
        }
        // Write arcsInTS_array into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile1 = new WriteXlsxFile();
        objWriteXlsxFile1.getDataAndsetSheet(arcsInTS_array,"ArcsInTS").setFileName("arcsInTS.xlsx");

        //--------------------------------------------------------------------------------------------------------------

        List<List<Integer>> yardsInTS = new ArrayList<>();  // yards in train services (TS)
        yardsInTS.add(Arrays.asList(-1,-1)); // it will be removed at the end

        for (int j = 0; j < ALPE[0].length; j++) {
                boolean yardIncluded = false;
                int K = -1;
                for (int k = 0; k < yardsInTS.size(); k++) {
                    if (ALPE[0][j] == yardsInTS.get(k).get(0)) {
                        yardIncluded = true; // the yard is already added to the matrix (yardsInTS) -
                        K = k;                                // - at the Kth bracket
                        break;                                    // - no action needed
                    }
                }
                if (yardIncluded) {
                    boolean TSIncluded = false;
                    List<Integer> currentBracket = new ArrayList<>(yardsInTS.get(K));
                    for (int q = 1; q < currentBracket.size(); q++) {
                        if (ALPE[1][j] == currentBracket.get(q)) {
                            TSIncluded = true; // the TS of the yard is already added to the matrix (yardsInTS) -
                            break;                             // - no action needed
                        }
                    }
                    if (!TSIncluded) yardsInTS.get(K).add((int)ALPE[1][j]); // added the new TS of the yard -
                }                                                   // - to the current bracket of the matrix (yardsInTS)
                else {
                    List<Integer> newBracket = new ArrayList<>();
                    newBracket.add((int)ALPE[0][j]);
                    newBracket.add((int)ALPE[1][j]);
                    yardsInTS.add(newBracket); // the new yard and its TS are added to the matrix (yardsInTS)
                }
        }
        yardsInTS.remove(0);

        // Write yardsInTS into a text file
        try {
            FileWriter writer = new FileWriter("yardsInTS.txt");
            writer.write("Yards in TS (the first number shows the yard, and the rest show the TS):");
            writer.write(System.getProperty( "line.separator" ));
            for (List<Integer> bracket : yardsInTS) {
                //System.out.println(bracket);
                writer.write(Arrays.toString(bracket.toArray()));
                writer.write(System.getProperty( "line.separator" ));
            }
            writer.close();
        } catch (IOException exc) {
            System.out.println("Error FileWriter (IOException):");
            exc.printStackTrace();
        }
        // Convert yardsInTS from arrayList to array (yardsInTS_array)
        int [][] yardsInTS_array = new int[yardsInTS.size()][];
        for (int k = 0; k < yardsInTS.size(); k++) {
            List<Integer> currentBracket = new ArrayList<>(yardsInTS.get(k));
            yardsInTS_array[k] = currentBracket.stream().mapToInt(i -> i).toArray();
        }
        // Write yardsInTS_array into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile2 = new WriteXlsxFile();
        objWriteXlsxFile2.getDataAndsetSheet(yardsInTS_array,"YardsInTS").setFileName("yardsInTS.xlsx");
    }
}