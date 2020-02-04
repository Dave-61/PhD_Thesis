package PhD3;

import java.util.ArrayList;
import java.util.List;

class SubGradientAlg {
    static void subgradient(double alpha, int numTrainServices, double[][] Dmnd, double[][] ALPE, double[][] YPE, double[][] AP_N1, double[][] AC_N1, double[][] delta, List<List<Double>> list_loadYards, List<List<Double>> list_loadArcs) {

        Initial_uMatrix.GenerateIuM();
        System.out.println();
        System.out.println("Initial uMatrix is Built :)");

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();
        List<Double> list_sumCVaR = new ArrayList<>();
        List<Double> list_Lu = new ArrayList<>();
        List<Double> list_theta = new ArrayList<>();

        for (int q = 0; q < 20; q++) {
            System.out.println("############################################################### ITERATION " + (q+1) + " ###############################################################");

            double[][] uMatrix; // u_k≥0,∀k and u_ij≥0,∀(i,j) are the vectors of dual variables for constraint sets (9) and (10), respectively
            uMatrix = objReadXlsxFile.setFilePath("C:\\IntelliJProjects\\SDH\\uMatrix.xlsx")
                    .setSheet(0).setRow(1,169).setCell(1,169)
                    .createArrayMatrix();
            //objReadXlsxFile.showArrayMatrix(uMatrix);

            List<Object> LgnFcnAlg2_output = LgnFcnAlg2_AllShpmt.lgnFcnAll(alpha, numTrainServices, Dmnd, ALPE, YPE, AP_N1, AC_N1, delta, uMatrix);
            double sumCVaR = (double) LgnFcnAlg2_output.get(0);
            list_sumCVaR.add(sumCVaR);
            double Lu = (double) LgnFcnAlg2_output.get(1);
            double square_norm_gamma = (double) LgnFcnAlg2_output.get(2);
            double[][] gamma = (double[][]) LgnFcnAlg2_output.get(3);
            double[][] load = (double[][]) LgnFcnAlg2_output.get(4);

            for (int i = 2; i < load.length; i++) {
                for (int j = 2; j < load.length; j++) {
                    if ((load[i][0] == load[0][j]) && (load[i][1] != load[1][j])) { // the same yard but different TS; potential transferring yard
                        for (int k = 0; k < list_loadYards.size(); k++) {
                            if ((list_loadYards.get(k).get(0) == load[i][0])) {
                                if (list_loadYards.get(k).size() == q+1) { // the load is not added to the yard yet!
                                    list_loadYards.get(k).add(load[i][j]);
                                }
                                else { // the load is already added to the yard
                                    if (list_loadYards.get(k).get(q+1) != load[i][j]) {
                                        System.out.println("Error: Loads of the yard are not the same!!!");
                                        System.out.println("The program is terminating ...");
                                        System.exit(1);
                                    }
                                }
                            }
                        }
                    }
                    if ((load[i][0] != load[0][j]) && (load[i][1] == load[1][j]) && delta[i][j] >= 0) { // different nodes but the same TS; an arc  // delta[i][j] >= 0: the TS is in the list of the arc
                        for (int k = 0; k < list_loadArcs.size(); k++) {
                            if ((list_loadArcs.get(k).get(0) == load[i][0]) && (list_loadArcs.get(k).get(1) == load[0][j])) {
                                if (list_loadArcs.get(k).size() == q+2) { // the load is not added to the arc yet!
                                    list_loadArcs.get(k).add(load[i][j]);
                                    //System.out.println("Load of arc " + load[i][0] + "{" + load[i][1] + "} -> " + load[0][j] + "{" + load[1][j] + "} is added.");
                                }
                                else { // the load is already added to the arc
                                    if (list_loadArcs.get(k).get(q+2) != (load[i][j])) {
                                        System.out.println("Error: Loads of arc " + load[i][0] + "{" + load[i][1] + "} -> " + load[0][j] + "{" + load[1][j] + "} are not the same!!!");
                                        System.out.println("The program is terminating ...");
                                        System.exit(1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            list_Lu.add(Lu);

            double theta;
            if (q == 0) {
                theta = 2.0; // theta = list_theta.get(0)
                list_theta.add(theta);
            }
            else {
                if (list_Lu.get(q) < list_Lu.get(q-1)) {
                    theta = (list_theta.get(q-1))/2;
                    list_theta.add(theta);
                }
                else {
                    theta = list_theta.get(q-1); // theta = 2.0;
                    list_theta.add(theta);
                }
            }

            double L_ub = 151244;
            double t = (theta*(L_ub - Lu))/square_norm_gamma;

            for (int i = 2; i < uMatrix.length; i++) {
                for (int j = 2; j < uMatrix.length; j++) {
                    if (uMatrix[i][j] >= 0) {
                        uMatrix[i][j] = Math.max(0, uMatrix[i][j] + t*gamma[i][j]);
                    }
                }
            }
            System.out.println();
            WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
            objWriteXlsxFile.getDataAndsetSheet(uMatrix,"uMatrix").setFileName("uMatrix.xlsx");
        }
        System.out.println();
        System.out.println("############################################################### ALL ITERATIONS ARE DONE! ###############################################################");


        // Convert list_loadYards from arrayList to array (loadYards)
        double [][] loadYards = new double[list_loadYards.size()][];
        for (int k = 0; k < list_loadYards.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(list_loadYards.get(k));
            loadYards[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }
        // Write loadYards into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile2 = new WriteXlsxFile();
        objWriteXlsxFile2.getDataAndsetSheet(loadYards,"YardsLoad").setFileName("ArcYardLoad.xlsx");

        // Convert list_loadArcs from arrayList to array (loadArcs)
        double [][] loadArcs = new double[list_loadArcs.size()][];
        for (int k = 0; k < list_loadArcs.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(list_loadArcs.get(k));
            loadArcs[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }
        // Write loadArcs into a xlsx (Excel) file
        objWriteXlsxFile2.getDataAndsetSheet(loadArcs,"ArcsLoad").setFileName("ArcYardLoad.xlsx");

        System.out.println();
        System.out.println("List of sumCVaR: " + list_sumCVaR);

        System.out.println();
        System.out.println("List of L(u): " + list_Lu);
        System.out.println("List of theta: " + list_theta);

    }
}