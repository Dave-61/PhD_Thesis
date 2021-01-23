package PhD3;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InitialSltn2_AllShpmt {
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        double alpha = 0.999999;
        int numTrainServices = 31;
        System.out.println("alpha = " + alpha);
        System.out.println("Number of train services = " + numTrainServices);
        System.out.println();

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
        YPE = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
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

        double[][] CR = new double[ALPE[0].length + 2][ALPE[0].length + 2]; // Matrix CR (cumulative risk) is adopted from matrix NvTimesX
        for (int i = 0; i < CR.length; i++)
            for (int j = 0; j < CR.length; j++)
                CR[i][j] = -1; // initially placed -1 in each cell of matrix CR
        for (int i = 0; i < 2; i++)
            System.arraycopy(ALPE[i], 0, CR[i], 2, CR.length - 2);
        // copied the first two rows of ALPE (yards and train services) into the first two rows of delta from column 2
        for (int i = 2; i < CR.length; i++)
            for (int j = 0; j < 2; j++)
                CR[i][j] = CR[j][i];
        // did a transpose copy of the first two rows of delta (from column 2) into the first two columns of delta (from row 2)
        for (int i = 2; i < CR.length; i++)
            for (int j = 2; j < CR.length; j++)
                if (delta[i][j] >= 0)
                    CR[i][j] = 0; // replaced -1 with 0 in the potential yards and arcs of a route

        List<Pair<Double, Pair<Double, Double>>> sortedDmnd = new ArrayList<>(); // (Nv, (Ov, Dv))
        for (int i = 1; i < 14; i++)    // i < Dmnd.length !!!!!!!!!!!!!!!!!     OR     i < 2  // original values: i < Dmnd.length
            for (int j = 1; j < 26; j++)                                                       // original values: j < Dmnd.length
                if (Dmnd[i][j] != -1 && Dmnd[i][j] != 0)
                    sortedDmnd.add(new Pair<>(Dmnd[i][j], new Pair<>(Dmnd[i][0], Dmnd[0][j]))); // (Nv, (Ov, Dv))

        sortedDmnd.sort(Comparator.comparing((p -> -p.getKey())));

        boolean DijkstraInfeasible = false;

        double sumCVaRstar_alpha_vBar = 0;
        int counter = 0;
        for (int i = 0; i < sortedDmnd.size(); i++) {
            counter++;
            double Nv = sortedDmnd.get(i).getKey();
            Pair<Double, Double> OvDv = sortedDmnd.get(i).getValue();
            double Ov = OvDv.getKey();
            double Dv = OvDv.getValue();

            System.out.println();
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ SHIPMENT " + counter + " @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("********** Origin: " + (int) Ov + ", Destination: " + (int) Dv + ", Hazmat Volume: " + (int) Nv + " **********");

            List<Object> initialSolution1_output = InitialSltn1_SnglShpmt.initialSolution1_SingleShipment(alpha, numTrainServices, ALPE, YPE, AP_N1, AC_N1, Ov, Dv, Nv, delta, CR);
            sumCVaRstar_alpha_vBar += (double) initialSolution1_output.get(0);
            CR = (double[][]) initialSolution1_output.get(1);

            DijkstraInfeasible = (boolean) initialSolution1_output.get(2);
            if (DijkstraInfeasible)
                break;

        }

        if (!DijkstraInfeasible) {
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ALL SHIPMENTS ARE DONE! @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("SUM CVaR*_alpha(vBar) = " + sumCVaRstar_alpha_vBar);
            System.out.println();
        }

        List<List<Double>> list_CRofYards = new ArrayList<>();
        for (int i = 0; i < deltaYardsInTs.length; i++) {
            list_CRofYards.add(new ArrayList<>());
            list_CRofYards.get(i).add(deltaYardsInTs[i][1]);
        }

        List<List<Double>> list_CRofArcs = new ArrayList<>();
        for (int i = 0; i < deltaArcsInTs.length; i++) {
            list_CRofArcs.add(new ArrayList<>());
            list_CRofArcs.get(i).add(deltaArcsInTs[i][1]);
            list_CRofArcs.get(i).add(deltaArcsInTs[i][2]);
        }

        for (int i = 2; i < CR.length; i++) {
            for (int j = 2; j < CR.length; j++) {
                if ((CR[i][0] == CR[0][j]) && (CR[i][1] != CR[1][j])) { // the same yard but different TS; potential transferring yard
                    for (int k = 0; k < list_CRofYards.size(); k++) {
                        if ((list_CRofYards.get(k).get(0) == CR[i][0])) {
                            if (list_CRofYards.get(k).size() == 1) { // the load is not added to the yard yet!
                                list_CRofYards.get(k).add(CR[i][j]);
                            }
                            else { // the load is already added to the yard
                                if (list_CRofYards.get(k).get(1) != CR[i][j]) {
                                    System.out.println("Error: CR of the yard are not the same!!!");
                                    System.out.println("The program is terminating ...");
                                    System.exit(1);
                                }
                            }
                        }
                    }
                }
                if ((CR[i][0] != CR[0][j]) && (CR[i][1] == CR[1][j]) && delta[i][j] >= 0) { // different nodes but the same TS; an arc  // delta[i][j] >= 0: the TS is in the list of the arc
                    for (int k = 0; k < list_CRofArcs.size(); k++) {
                        if ((list_CRofArcs.get(k).get(0) == CR[i][0]) && (list_CRofArcs.get(k).get(1) == CR[0][j])) {
                            if (list_CRofArcs.get(k).size() == 2) { // the load is not added to the arc yet!
                                list_CRofArcs.get(k).add(CR[i][j]);
                            }
                            else { // the load is already added to the arc
                                if (list_CRofArcs.get(k).get(2) != (CR[i][j])) {
                                    System.out.println("Error: CR of arc " + CR[i][0] + "{" + CR[i][1] + "} -> " + CR[0][j] + "{" + CR[1][j] + "} are not the same!!!");
                                    System.out.println("The program is terminating ...");
                                    System.exit(1);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Convert list_CRofYards from arrayList to array (CRofYards)
        double [][] CRofYards = new double[list_CRofYards.size()][];
        for (int k = 0; k < list_CRofYards.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(list_CRofYards.get(k));
            CRofYards[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }
        // Write CRofYards into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
        objWriteXlsxFile.getDataAndsetSheet(CRofYards,"CRofYards").setFileName("CRofArcYard.xlsx");

        // Convert list_CRofArcs from arrayList to array (CRofArcs)
        double [][] CRofArcs = new double[list_CRofArcs.size()][];
        for (int k = 0; k < list_CRofArcs.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(list_CRofArcs.get(k));
            CRofArcs[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }
        // Write CRofArcs into a xlsx (Excel) file
        objWriteXlsxFile.getDataAndsetSheet(CRofArcs,"CRofArcs").setFileName("CRofArcYard.xlsx");


        if (DijkstraInfeasible)
            System.exit(1);

        System.out.println();
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime; // in nanoseconds
        int totalTime_m = (int) ((totalTime/1000000000)/60);
        int totalTime_s = (int) ((totalTime/1000000000)%60);
        System.out.println("Running time of the program: " + totalTime_m + " min, " + totalTime_s + " secs.");
    }
}
