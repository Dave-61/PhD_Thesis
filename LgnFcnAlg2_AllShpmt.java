package PhD3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LgnFcnAlg2_AllShpmt {        // Pair<double[], double[][]>   // Object[] // List<Object>
    static List<Object> lgnFcnAll(double alpha, int numTrainServices, double[][] Dmnd, double[][] ALPE, double[][] YPE, double[][] AP_N1, double[][] AC_N1, double[][] delta, double[][] uMatrix) {

        List<Double> sumC_rPlusf_r = new ArrayList<>();
        double sumCVaR = 0;
        List<List<Double>> sum_route_noTS = new ArrayList<>();
        List<Double> all_Nv = new ArrayList<>();

        int counter = 0;
        for (int i = 1; i < Dmnd.length; i++) {   // i < Dmnd.length !!!!!!!!!!!!!!!!!     OR     i < 3  // original values: i < Dmnd.length
            for (int j = 1; j < Dmnd.length; j++) {                                                      // original values: j < Dmnd.length
                if (Dmnd[i][j] != -1 && Dmnd[i][j] != 0) {
                    counter ++;
                    double Ov = Dmnd[i][0];
                    double Dv = Dmnd[0][j];
                    double Nv = Dmnd[i][j];
                    System.out.println();
                    System.out.println("------------------------------------------------ SHIPMENT " + counter + " ------------------------------------------------");
                    System.out.println("********** Origin: " + (int)Ov + ", Destination: " + (int)Dv + ", Hazmat Volume: " + (int)Nv + " **********");

                    List<Double> LgnFcnAlg1_output = new ArrayList<>(LgnFcnAlg1_SnglShpmt.lgnFcnSngl(alpha, numTrainServices, ALPE, YPE, AP_N1, AC_N1, uMatrix, Ov, Dv, Nv));

                    sumC_rPlusf_r.add(LgnFcnAlg1_output.get(0));
                    sumCVaR += LgnFcnAlg1_output.get(1);

                    List<Double> current_route_noTS = new ArrayList<>();
                    for (int k = 2; k < LgnFcnAlg1_output.size(); k++) {
                        current_route_noTS.add(LgnFcnAlg1_output.get(k));
                    }
                    sum_route_noTS.add(current_route_noTS);

                    all_Nv.add(Nv);
                }
            }
        }
        System.out.println();
        System.out.println("------------------------------------------------ ALL SHIPMENTS ARE DONE! ------------------------------------------------");
        Double totalC_rPlusf_r = 0.0;
        for (Double index : sumC_rPlusf_r) {
            totalC_rPlusf_r += index;
        }
        System.out.println("Summation of C_r* + f_r* over all shipments = " + totalC_rPlusf_r);

        System.out.println();
        WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
        objWriteXlsxFile.getDataAndsetSheet(delta,"deltaMatrix").setFileName("deltaMatrix.xlsx");
//        WriteXlsxFile objWriteXlsxFile2 = new WriteXlsxFile();
//        objWriteXlsxFile2.getDataAndsetSheet(uMatrix,"uMatrix").setFileName("uMatrix_used.xlsx");

        double sum_deltaTIMESu = 0.0;
        for (int i = 2; i < delta.length; i++)
            for (int j = 2; j < delta.length; j++)
                if (delta[i][j] >= 0)
                    sum_deltaTIMESu += delta[i][j] * uMatrix[i][j];

        System.out.println();
        System.out.println("Summation of deltaXu over all yards and arcs = " + sum_deltaTIMESu);

        double Lu = totalC_rPlusf_r - sum_deltaTIMESu;
        System.out.println();
        System.out.println("Lagrangian function (Lu) = " + Lu); // Lu = Sum_v (C_r* + f_r*) - Sum_k&(i,j) u*delta

        //System.out.println(sum_route_noTS);
        //System.out.println(all_Nv);
        //System.out.println(sum_route_noTS.size());
        //System.out.println(all_Nv.size());

        double[][] NvTimesX = new double[ALPE[0].length + 2][ALPE[0].length + 2];
        for (int i = 0; i < NvTimesX.length; i++)
            for (int j = 0; j < NvTimesX.length; j++)
                NvTimesX[i][j] = -1; // initially placed -1 in each cell of matrix NvTimesX
        for (int i = 0; i < 2; i++)
            System.arraycopy(ALPE[i], 0, NvTimesX[i], 2, NvTimesX.length - 2);
        // copied the first two rows of ALPE (yards and train services) into the first two rows of delta from column 2
        for (int i = 2; i < NvTimesX.length; i++)
            for (int j = 0; j < 2; j++)
                NvTimesX[i][j] = NvTimesX[j][i];
        // did a transpose copy of the first two rows of delta (from column 2) into the first two cloumns of delta (from row 2)

        for (int i = 2; i < NvTimesX.length; i++)
            for (int j = 2; j < NvTimesX.length; j++)
                if (delta[i][j] >= 0)
                    NvTimesX[i][j] = 0; // replaced -1 with 0 in the potential yards and arcs of a route

        for (int k = 0; k < sum_route_noTS.size(); k++) {
            List<Double> current_route_noTS = sum_route_noTS.get(k);
            for (int n = 0; n < current_route_noTS.size() - 1; n++) {
                if (current_route_noTS.get(n).equals(current_route_noTS.get(n + 1))) { // a yard
                    for (int i = 2; i < NvTimesX.length; i++) {
                        if (NvTimesX[i][0] == current_route_noTS.get(n)) {
                            for (int j = 2; j < NvTimesX.length; j++) {
                                if (NvTimesX[0][j] == current_route_noTS.get(n) && NvTimesX[i][1] != NvTimesX[1][j]) { // the same yard but different TS
                                    if (NvTimesX[i][j] == -1) {
                                        System.out.println("i = " + i + ", j = " + j);
                                        System.out.println("Error: INCOMPATIBILITY BETWEEN MATRICES!");
                                        System.out.println("The program is terminating ...");
                                        System.exit(1);
                                    }
                                    else
                                        NvTimesX[i][j] += all_Nv.get(k);
                                }
                            }
                        }
                    }
                }
                else { // an arc
                    for (int i = 2; i < NvTimesX.length; i++) {
                        if (NvTimesX[i][0] == current_route_noTS.get(n) || NvTimesX[i][0] == current_route_noTS.get(n+1)) { // || is to make it undirected: i.e. if there is arc (6,2) in the route_noTS, both service legs 6->2 and 2->6 's CR will be added
                            for (int j = 2; j < NvTimesX.length; j++) {
                                if ((NvTimesX[0][j] == current_route_noTS.get(n+1) || NvTimesX[0][j] == current_route_noTS.get(n)) && NvTimesX[i][1] == NvTimesX[1][j]) { // two nodes (the arc) with the same TS
                                    if (NvTimesX[i][j] != -1) { // where delta >= 0: two nodes (the arc) with the same TS, when the TS is in the list of the arc. Also it assures that a yard's CR won't be added: i.e. 2{10}->2{10}
                                        NvTimesX[i][j] += all_Nv.get(k);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //objReadXlsxFile.showArrayMatrix(NvTimesX);
        //System.out.println();
        //System.out.println("Building matrix NvTimesX is done :)");

        double[][] gamma = new double[ALPE[0].length + 2][ALPE[0].length + 2];
        double[][] load = new double[ALPE[0].length + 2][ALPE[0].length + 2]; // load on yards and arcs: NvTimesX[i][j] * AP_N1[i][j] * AC_N1[i][j]
        for (int i = 0; i < gamma.length; i++) {
            gamma[i] = delta[i].clone();
            load[i] = delta[i].clone();
        }

        double square_norm_gamma = 0.0;

        for (int i = 2; i < gamma.length; i++) {
            for (int j = 2; j < gamma.length; j++) {
                if (gamma[i][j] >= 0) { // where delta >= 0
                    gamma[i][j] = -delta[i][j] + (NvTimesX[i][j] * AP_N1[i][j] * AC_N1[i][j]);
                    square_norm_gamma += Math.pow(gamma[i][j], 2);

                    load[i][j] = NvTimesX[i][j] * AP_N1[i][j] * AC_N1[i][j]; // resulting from all shipments (in the current iteration)
                }
            }
        }
        //objReadXlsxFile.showArrayMatrix(gamma);
        System.out.println();
        System.out.println("Building matrix gamma is done :)");
        System.out.println("Square of norm of gamma = " + square_norm_gamma);

        //WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
        //objWriteXlsxFile.getDataAndsetSheet(load,"load").setFileName("load.xlsx");

        return Arrays.asList(sumCVaR, Lu, square_norm_gamma, gamma, load); //return new Pair<>(returnCategory1, gamma) //return new Object[]{Lu, square_norm_gamma, gamma, load}

    }
}