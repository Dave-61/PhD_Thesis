package PhD3;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class LgnFcnAlg1_SnglShpmt {
    static List<Double> lgnFcnSngl(double alpha, int numTrainServices, double[][] ALPE, double[][] YPE, double[][] AP_N1, double[][] AC_N1, double[][] uMatrix, double Ov, double Dv, double Nv) {

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

        double[][] AP = new double[ALPTY.length][]; // Start building matrix AP by copying ALPTY
        for (int i = 0; i < ALPTY[0].length; i++) {  // AP: Accident Probability
            AP[i] = ALPTY[i].clone();

        }

        double[][] AC = new double[ALPTY.length][]; // Start building matrix AC by copying ALPTY
        for (int i = 0; i < ALPTY[0].length; i++) { // AC: Accident Consequence
            AC[i] = ALPTY[i].clone();

        }

        for (int k = 1; k <= numTrainServices; k++) {
            for (int j = 0; j < ALPE[0].length; j++) {
                if (ALPE[1][j] == k && ALPE[2][j] >= 0) {
                    ALPTY[j + 2][j + 3] = ALPE[2][j]; // placed arc length in ALPTY
                    AP[j+2][j+3] = ALPTY[j + 2][j + 3] * 7.35 * Math.pow(10, -11) * Nv; // calculated AP of the arc
                    AC[j + 2][j + 3] = ALPE[(int) (Nv + 2)][j]; // placed AC of the arc (resulting from Nv hazmat railcars) using ALPE
                }
            }
        }

        for (int i = 2; i < ALPTY.length; i++) {
            for (int j = 2; j < ALPTY.length; j++) {
                if (ALPTY[i][0] == ALPTY[0][j] && ALPTY[i][1] != ALPTY[1][j]) { // same yard but different train service
                    ALPTY[i][j] = 0; // placed zero for potential transferring yards
                    AP[i][j] = 6.42 * Math.pow(10, -10) * Nv; // calculated AP of the yard
                    AC[i][j] = YPE[(int) Nv][(int) ALPTY[0][j]-1]; // placed AC of the yard (resulting from Nv hazmat railcars) using YPE
                }
            }
        }
        // Building matrices ALPTY, AP, and AC are done!
        //objReadXlsxFile.showArrayMatrix(AP);
        System.out.println();
        System.out.println("Building matrices AP and AC for this shipment is done :)");

        //System.out.println(AP[21][22]);
        //System.out.println(AC[21][22]);
        //System.out.println(AC[15][45]);
        //System.out.println(AP[24][67]);

        List<Double> sortedAC_withDuplicates_arrList = new ArrayList<>();  // Sorted AC (resulting from Nv hazmat railcars) using AC
        sortedAC_withDuplicates_arrList.add(0.0);
        for (int i = 2; i < AC.length; i++)
            for (int j = 2; j < AC.length; j++)
                if (AC[i][j] > 0)
                    sortedAC_withDuplicates_arrList.add(AC[i][j]);

        List<Double> sortedAC_arrList = sortedAC_withDuplicates_arrList
                .stream().distinct().sorted().collect(Collectors.toList());
        // removed duplicates from sortedAC_withDuplicates_arrList and sorted it (using Java 8 Lambdas)

        // Convert sortedAC_arrList to an array (sortedAC)
        double[] sortedAC= sortedAC_arrList.stream().mapToDouble(i -> i).toArray();

        List<Double> C_rPlusf_r = new ArrayList<>();
        List<List<Double>> route_r = new ArrayList<>();

        System.out.println();
        for (int r = 0; r < sortedAC.length; r++) { // For r = 0 to M do:
            double[][] wMatrix = new double[AC.length][]; // weights of arcs and yards: modifications in (11) and (12)
            for (int i = 0; i < AC[0].length; i++)
                wMatrix[i] = AC[i].clone();
            for (int i = 2; i < AC.length; i++) {
                for (int j = 2; j < AC.length; j++) {
                    if (AC[i][j] > 0) {
                        if (AC[i][j] > sortedAC[r])
                            wMatrix[i][j] = AP[i][j]*(AC[i][j] - sortedAC[r])/(1-alpha)
                                    + Nv*uMatrix[i][j]*AP_N1[i][j]*AC_N1[i][j];
                        else
                            wMatrix[i][j] = Nv*uMatrix[i][j]*AP_N1[i][j]*AC_N1[i][j];
                    }
                }
            }
            double[][] adjacencyMatrix = new double[wMatrix.length - 2][wMatrix.length - 2];
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                for (int j = 0; j < adjacencyMatrix.length; j++) { // adjacencyMatrix doesn't have the first two rows and columns of wMatrix
                    adjacencyMatrix[i][j] = -1;
                    if (wMatrix[i+2][j+2] >= 0)
                        adjacencyMatrix[i][j] = wMatrix[i+2][j+2]; // the other elements of adjacencyMatrix are -1
                }
            }
            List<List<Double>> allRoutes = new ArrayList<>();  // All routes found between the origin and destination using Dijkstra's Algorithm
            // (from the origin with different TS to the destination with different TS !!!)
            for (int i = 2; i < wMatrix.length; i++) {
                for (int j = 2; j < wMatrix.length; j++) {
                    if (wMatrix[i][0] == Ov && wMatrix[0][j] == Dv) {
                        List<Double> route = DijkstrasAlg.dijkstra(adjacencyMatrix, i-2, j-2);
                        //System.out.println(route);
                        allRoutes.add(route);
                    }
                }
            }
            //System.out.println(allRoutes);
            int bestRouteIndex = 0;
            for (int i = 1; i < allRoutes.size(); i++) {
                List<Double> bestRoute = allRoutes.get(bestRouteIndex);
                List<Double> route = allRoutes.get(i);
                if (route.get(0) < bestRoute.get(0)) {
                    bestRouteIndex = i;
                }
            }

            List<Double> bestRoute = allRoutes.get(bestRouteIndex);
            route_r.add(bestRoute);
            double f_r = bestRoute.get(0); // the first element of route shows f_r and the rest shows the route itself
            C_rPlusf_r.add(sortedAC[r] + f_r);

            System.out.println("r = " + r + ", C_r = " + sortedAC[r] + ", f_r = " + f_r +
                    " => C_r + f_r = " + (sortedAC[r]+f_r));

            List<Integer> bestRoute_display = new ArrayList<>();
            for (int i = 1; i < bestRoute.size(); i++) {
                bestRoute_display.add((int) ALPTY[(int) (bestRoute.get(i) + 2)][0]);
                bestRoute_display.add((int) ALPTY[(int) (bestRoute.get(i) + 2)][1]);
            }
            for (int i = 0; i < bestRoute_display.size(); i += 2) {
                System.out.print(bestRoute_display.get(i));
                System.out.print("{");
                System.out.print(bestRoute_display.get(i+1));
                System.out.print("}");
                if (i != bestRoute_display.size()-2)
                    System.out.print(" -> ");
                else
                    System.out.println();
            }
        }
        int minIndex = C_rPlusf_r.indexOf(Collections.min(C_rPlusf_r));
        System.out.println();
        System.out.println("r* = " + minIndex + ", C_r* + f_r* = " + C_rPlusf_r.get(minIndex));

        // and the corresponding route to r* is route_r.get(minIndex):
        List<Integer> route_display = new ArrayList<>();
        for (int i = 1; i < route_r.get(minIndex).size(); i++) {
            route_display.add((int) ALPTY[(int) (route_r.get(minIndex).get(i) + 2)][0]);
            route_display.add((int) ALPTY[(int) (route_r.get(minIndex).get(i) + 2)][1]);
        }
        for (int i = 0; i < route_display.size(); i += 2) {
            System.out.print(route_display.get(i));
            System.out.print("{");
            System.out.print(route_display.get(i+1));
            System.out.print("}");
            if (i != route_display.size()-2)
                System.out.print(" -> ");
            else
                System.out.println();
        }

        // Corresponding route to r* (the best route for this shipment): route_r.get(minIndex), from the SECOND element
        // Calculate the CVaR of this route
        List<Pair<Double, Double>> ACandAPofRoute = new ArrayList<>();
        for (int i = 1; i < route_r.get(minIndex).size() - 1; i++) {  // the first element of route shows f_r and the rest shows the route itself
            ACandAPofRoute.add(new Pair<>(AC[(int) (route_r.get(minIndex).get(i) + 2)][(int) (route_r.get(minIndex).get(i+1) + 2)], AP[(int) (route_r.get(minIndex).get(i) + 2)][(int) (route_r.get(minIndex).get(i+1) + 2)]));
        }
        double sumAPofRoute = 0;
        for (Pair<Double, Double> pair : ACandAPofRoute) {
            sumAPofRoute += pair.getValue();
        }
        ACandAPofRoute.add(new Pair<>(0.0, 1 - sumAPofRoute));

        List<Pair<Double, Double>> sortedACandAPofRoute = new ArrayList<>(ACandAPofRoute);
        sortedACandAPofRoute.sort(Comparator.comparing(Pair::getKey));

        int T = 0;
        double cumulativeAP = 0;
        for (int i = 0; i < sortedACandAPofRoute.size(); i++) {
            if (i == 0) {
                if (alpha <= sortedACandAPofRoute.get(i).getValue()) {
                    T = 0;
                    break;
                } else {
                    cumulativeAP += sortedACandAPofRoute.get(i).getValue();
                }
            } else {
                if ((cumulativeAP < alpha) && (alpha <= (cumulativeAP + sortedACandAPofRoute.get(i).getValue()))) {
                    T = i;
                    break;
                } else
                    cumulativeAP += sortedACandAPofRoute.get(i).getValue();
            }
        }
        double insideBracketCVaR = 0;
        for (int i = T+1; i < sortedACandAPofRoute.size(); i++) {
            insideBracketCVaR += sortedACandAPofRoute.get(i).getValue() * (sortedACandAPofRoute.get(i).getKey() - sortedACandAPofRoute.get(T).getKey());
        }
        double CVaR = sortedACandAPofRoute.get(T).getKey() + (insideBracketCVaR/(1-alpha));
        System.out.println("CVaR_alpha(v) = " + CVaR);


        List<Double> route_noTS = new ArrayList<>();
        for (int i = 1; i < route_r.get(minIndex).size(); i++) {
            route_noTS.add(ALPTY[(int) (route_r.get(minIndex).get(i) + 2)][0]);
        }
        System.out.println("The route with no TS: ");
        System.out.println(route_noTS);

        List<Double> returnValues = new ArrayList<>(route_noTS);
        returnValues.add(0, CVaR);  // CVaR for the current shipment (correspondent to r*), added to the beginning of route_noTS
        returnValues.add(0, C_rPlusf_r.get(minIndex)); // C_r* + f_r* for the current shipment, added to the beginning of route_noTS

        return returnValues; // returns C_r* + f_r* for the current shipment AND route_noTS
    }
}