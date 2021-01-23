package PhD3;

import java.util.ArrayList;
import java.util.List;

class A_Exe {
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        // ---------------------------------------------------------- SET DATA ---------------------------------------------------------------------
        double alpha = 0.999999;
        int numTrainServices = 31;

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();

        double[][] Dmnd; // This matrix (FROM/TO) shows the origin yard, destination yard, and the number of hazmat rails to be shipped between them in the network
        Dmnd = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(0).setRow(0,25).setCell(0,25)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(Dmnd);

        double[][] ALPE; // Arc Length & Population Exposure
        ALPE = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(1).setRow(1,123).setCell(1,167)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ALPE);

        double[][] YPE; // Yard Population Exposure
        YPE = objReadXlsxFile.setFilePath("D:\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(4).setRow(1,121).setCell(1,25)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(YPE);

        // Start building matrix ALPTY using ALPE  // ALPTY: Arc Length & Potential Transferring Yards
        double[][] ALPTY = new double[ALPE[0].length + 2][ALPE[0].length + 2];
        for (int i = 0; i < ALPTY.length; i++)
            for (int j = 0; j < ALPTY.length; j++)
                ALPTY[i][j] = -1; // initially placed -1 in each cell of ALPTY
        for (int i = 0; i < 2; i++)
            System.arraycopy(ALPE[i], 0, ALPTY[i], 2, ALPTY.length - 2);
        // copied the first two rows of ALPE (yards and train services) into the first two rows of ALPTY from column 2
        for (int i = 2; i < ALPTY.length; i++)
            for (int j = 0; j < 2; j++)
                ALPTY[i][j] = ALPTY[j][i];
        // did a transpose copy of the first two rows of ALPTY (from column 2) into the first two cloumns of ALPTY (from row 2)

        double[][] AP_N1 = new double[ALPTY.length][]; // Start building matrix AP_N1: matrix AP when Nv = 1, AP: Accident Probability
        double[][] AC_N1 = new double[ALPTY.length][]; // Start building matrix AC_N1: matrix AC when Nv = 1, AC: Accident Consequence
        for (int i = 0; i < ALPTY[0].length; i++) {
            AP_N1[i] = ALPTY[i].clone();
            AC_N1[i] = ALPTY[i].clone();
        }

        for (int k = 1; k <= numTrainServices; k++) {
            for (int j = 0; j < ALPE[0].length; j++) {
                if (ALPE[1][j] == k && ALPE[2][j] >= 0) {
                    ALPTY[j + 2][j + 3] = ALPE[2][j]; // placed arc length in ALPTY
                    AP_N1[j+2][j+3] = ALPTY[j + 2][j + 3] * 7.35 * Math.pow(10, -11) * 1; // calculated AP_N1 of the arc
                    AC_N1[j + 2][j + 3] = ALPE[1 + 2][j]; // placed AC of the arc (resulting from 1 hazmat railcars) using ALPE
                }
            }
        }

        for (int i = 2; i < ALPTY.length; i++) {
            for (int j = 2; j < ALPTY.length; j++) {
                if (ALPTY[i][0] == ALPTY[0][j] && ALPTY[i][1] != ALPTY[1][j]) { // same yard but different train service
                    ALPTY[i][j] = 0; // placed zero for potential transferring yards
                    AP_N1[i][j] = 6.42 * Math.pow(10, -10) * 1; // calculated AP_N1 of the yard
                    AC_N1[i][j] = YPE[1][(int) ALPTY[0][j]-1]; // placed AC of the yard (resulting from 1 hazmat railcars) using YPE
                }
            }
        }
        // Building matrices ALPTY, AP_N1, and AC_N1 are done!
        //objReadXlsxFile.showArrayMatrix(AP_N1);
        System.out.println();
        System.out.println("Building matrices AP_N1 and AC_N1 is done :)");

        double[][] deltaYardsInTs; // delta values for yards in train services
        deltaYardsInTs = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(11).setRow(1,25).setCell(0,13)
                .createArrayMatrix();

        double[][] deltaArcsInTs; // delta values for arcs in train services
        deltaArcsInTs = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(12).setRow(1,83).setCell(0,6) // for PhD3Data: rowEnd is 81 & for PhD3Data_2: rowEnd is 83 !!!!!!!
                .createArrayMatrix();

        // Start building matrix delta using ALPE, deltaYardsInTs and deltaArcsInTs  // delta: threshold values of arcs and yards
        double[][] delta = new double[ALPE[0].length + 2][ALPE[0].length + 2];
        for (int i = 0; i < delta.length; i++)
            for (int j = 0; j < delta.length; j++)
                delta[i][j] = -1; // initially placed -1 in each cell of matrix delta
        for (int i = 0; i < 2; i++)
            System.arraycopy(ALPE[i], 0, delta[i], 2, delta.length - 2);
        // copied the first two rows of ALPE (yards and train services) into the first two rows of delta from column 2
        for (int i = 2; i < delta.length; i++)
            for (int j = 0; j < 2; j++)
                delta[i][j] = delta[j][i];
        // did a transpose copy of the first two rows of delta (from column 2) into the first two cloumns of delta (from row 2)

        // Fill the cells of matrix delta using deltaYardsInTs and deltaArcsInTs
        for (int i = 2; i < delta.length; i++) {
            for (int j = 2; j < delta.length; j++) {
                if ((delta[i][0] == delta[0][j]) && (delta[i][1] != delta[1][j])) { // the same yard but different TS; potential transferring yard
                    for (int k = 0; k < deltaYardsInTs.length; k++) {
                        if (deltaYardsInTs[k][1] == delta[i][0]) {
                            delta[i][j] = deltaYardsInTs[k][0];
                            break;
                        }
                    }
                }
                if ((delta[i][0] != delta[0][j]) && (delta[i][1] == delta[1][j])) { // different nodes but the same TS; an arc in a TS
                    for (int k = 0; k < deltaArcsInTs.length; k++) {
                        if ((deltaArcsInTs[k][1] == delta[i][0]) && (deltaArcsInTs[k][2] == delta[0][j])) {
                            for (int m = 3; m < deltaArcsInTs[k].length; m++) {
                                if (deltaArcsInTs[k][m] == delta[i][1]) { // the TS is in the list of the arc
                                    delta[i][j] = deltaArcsInTs[k][0];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Building matrix delta using ALPE, deltaYardsInTs and deltaArcsInTs is done!
        //objReadXlsxFile.showArrayMatrix(delta);
        System.out.println();
        System.out.println("Building matrix delta is done :)");

        List<List<Double>> list_loadYards = new ArrayList<>();
        for (int i = 0; i < deltaYardsInTs.length; i++) {
            list_loadYards.add(new ArrayList<>());
            list_loadYards.get(i).add(deltaYardsInTs[i][1]);
        }

        List<List<Double>> list_loadArcs = new ArrayList<>();
        for (int i = 0; i < deltaArcsInTs.length; i++) {
            list_loadArcs.add(new ArrayList<>());
            list_loadArcs.get(i).add(deltaArcsInTs[i][1]);
            list_loadArcs.get(i).add(deltaArcsInTs[i][2]);
        }


        // ---------------------------------------------------------- EXE ---------------------------------------------------------------------
        SubGradientAlg.subgradient(alpha, numTrainServices, Dmnd, ALPE, YPE, AP_N1, AC_N1, delta, list_loadYards, list_loadArcs);


        System.out.println();
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime; // in nanoseconds
        int totalTime_m = (int) ((totalTime/1000000000)/60);
        int totalTime_s = (int) ((totalTime/1000000000)%60);
        System.out.println("Running time of the program: " + totalTime_m + " mins, " + totalTime_s + " secs.");

    }
}
