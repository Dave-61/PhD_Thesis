package PhD3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class InitialSltn1_SnglShpmt {
    static List<Object> initialSolution1_SingleShipment (double alpha, int numTrainServices, double[][] ALPE, double[][] YPE, double[][] AP_N1, double[][] AC_N1, double Ov, double Dv, double Nv, double[][] delta, double[][] CR) {

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
        // did a transpose copy of the first two rows of ALPTY (from column 2) into the first two columns of ALPTY (from row 2)

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
        System.out.println("Building matrices AP and AC for this shipment (Nv) is done :)");

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

        List<Double> C_rPlusfBar_r = new ArrayList<>();
        List<List<Double>> route_r = new ArrayList<>();
        double CVaRstar_alpha_vBar = 0;

        System.out.println();
        System.out.println("------------------------------------- ORIGINAL ROUTE -------------------------------------");
        //System.out.println();
        for (int r = 0; r < sortedAC.length; r++) { // For r = 0 to M do:
            double[][] wBarMatrix = new double[AC.length][]; // weights of arcs and yards: modifications in (11) and (12)
            for (int i = 0; i < AC[0].length; i++)
                wBarMatrix[i] = AC[i].clone();
            for (int i = 2; i < AC.length; i++) {
                for (int j = 2; j < AC.length; j++) {
                    if (AC[i][j] > 0) {
                        if (AC[i][j] > sortedAC[r])
                            wBarMatrix[i][j] = AP[i][j]*(AC[i][j] - sortedAC[r])/(1-alpha);
                        else
                            wBarMatrix[i][j] = 0;
                    }
                }
            }
            double[][] adjacencyMatrix = new double[wBarMatrix.length - 2][wBarMatrix.length - 2];
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                for (int j = 0; j < adjacencyMatrix.length; j++) { // adjacencyMatrix doesn't have the first two rows and columns of wBarMatrix
                    adjacencyMatrix[i][j] = -1;
                    if (wBarMatrix[i+2][j+2] >= 0)
                        adjacencyMatrix[i][j] = wBarMatrix[i+2][j+2]; // the other elements of adjacencyMatrix are -1
                }
            }
            List<List<Double>> allRoutes = new ArrayList<>();  // All routes found between the origin and destination using Dijkstra's Algorithm
            // (from the origin with different TS to the destination with different TS !!!)
            for (int i = 2; i < wBarMatrix.length; i++) {
                for (int j = 2; j < wBarMatrix.length; j++) {
                    if (wBarMatrix[i][0] == Ov && wBarMatrix[0][j] == Dv) {
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
            double fBar_r = bestRoute.get(0); // the first element of route shows fBar_r and the rest shows the route itself
            C_rPlusfBar_r.add(sortedAC[r] + fBar_r);

//            System.out.println("r = " + r + ", C_r = " + sortedAC[r] + ", fBar_r = " + fBar_r +
//                    " => C_r + fBar_r = " + (sortedAC[r]+fBar_r));
//
//            List<Integer> bestRoute_display = new ArrayList<>();
//            for (int i = 1; i < bestRoute.size(); i++) {
//                bestRoute_display.add((int) ALPTY[(int) (bestRoute.get(i) + 2)][0]);
//                bestRoute_display.add((int) ALPTY[(int) (bestRoute.get(i) + 2)][1]);
//            }
//            for (int i = 0; i < bestRoute_display.size(); i += 2) {
//                System.out.print(bestRoute_display.get(i));
//                System.out.print("{");
//                System.out.print(bestRoute_display.get(i+1));
//                System.out.print("}");
//                if (i != bestRoute_display.size()-2)
//                    System.out.print(" -> ");
//                else
//                    System.out.println();
//            }
        }
        int minIndex = C_rPlusfBar_r.indexOf(Collections.min(C_rPlusfBar_r));
        System.out.println();
        System.out.println("r* = " + minIndex + ", CVaR*_alpha(vBar) = C_r* + fBar_r* = " + C_rPlusfBar_r.get(minIndex));
        CVaRstar_alpha_vBar = C_rPlusfBar_r.get(minIndex);

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

        List<Double> route_noTS = new ArrayList<>();
        for (int i = 1; i < route_r.get(minIndex).size(); i++) {
            route_noTS.add(ALPTY[(int) (route_r.get(minIndex).get(i) + 2)][0]);
        }
        System.out.println("The ORIGINAL route with no TS: ");
        System.out.println(route_noTS);



        boolean anyRemoval = true; // If a yard and/or arc is temporarily removed from the network for the routing of current shipment
        int numAdjustment = 0; // Number of adjustments occured for the shipment
        int numElementsRemoved = 0; // Number of yards and arcs removed from the network
        boolean DijkstraInfeasible = false;

        while (anyRemoval) {
            System.out.println();
            for (int n = 0; n < route_noTS.size() - 1; n++) {
                if (route_noTS.get(n).equals(route_noTS.get(n + 1))) { // a yard
                    for (int i = 2; i < CR.length; i++) {
                        if (CR[i][0] == route_noTS.get(n)) {
                            for (int j = 2; j < CR.length; j++) {
                                if (CR[0][j] == route_noTS.get(n) && CR[i][1] != CR[1][j]) { // the same yard but different TS
                                    if (CR[i][j] == -1) {
                                        System.out.println("i = " + i + ", j = " + j);
                                        System.out.println("Error: INCOMPATIBILITY BETWEEN MATRICES!");
                                        System.out.println("The program is terminating ...");
                                        System.exit(1);
                                    } else {
                                        CR[i][j] += Nv * AP_N1[i][j] * AC_N1[i][j]; // Update CR_(IV) of this yard by setting IV up to the current shipment, as the summation of CR_(IV-1) and the risk added to the yard by the current shipment
                                        if (CR[i][j] > delta[i][j]) {
                                            System.out.println();
                                            System.out.println(CR[i][j] + " > " + delta[i][j]);
                                            AC[i][j] = -1;
                                            AP[i][j] = -1;
                                            System.out.println("Yard " + (int) ((double) route_noTS.get(n)) + "{" + (int) CR[i][1] + "} -> " + (int) ((double) route_noTS.get(n+1)) + "{" + (int) CR[1][j] + "} " +
                                                    "will be removed for finding the next adjusted route");
                                            numElementsRemoved += 1;
                                            CR[i][j] -= Nv * AP_N1[i][j] * AC_N1[i][j]; // This yard will be removed from the route, so its risk shouldn't be added
                                            System.out.println("CR returns to the previous value: " + CR[i][j] + " < delta: " + delta[i][j]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else { // an arc
                    for (int i = 2; i < CR.length; i++) {
                        if (CR[i][0] == route_noTS.get(n) || CR[i][0] == route_noTS.get(n+1)) { // || is to make it undirected: i.e. if there is arc (6,2) in the route_noTS, both service legs 6->2 and 2->6 's CR will be added
                            for (int j = 2; j < CR.length; j++) {
                                if ((CR[0][j] == route_noTS.get(n+1) || CR[0][j] == route_noTS.get(n)) && CR[i][1] == CR[1][j]) { // two nodes (the arc) with the same TS
                                    if (CR[i][j] != -1) { // where delta >= 0: two nodes (the arc) with the same TS, when the TS is in the list of the arc. Also it assures that a yard's CR won't be added: i.e. 2{10}->2{10}
                                        CR[i][j] += Nv * AP_N1[i][j] * AC_N1[i][j];; // Update CR_(IV) of this arc by setting IV up to the current shipment, as the summation of CR_(IV-1) and the risk added to the arc by the current shipment
                                        if (CR[i][j] > delta[i][j]) {
                                            System.out.println();
                                            System.out.println(CR[i][j] + " > " + delta[i][j]);
                                            AC[i][j] = -1;
                                            AP[i][j] = -1;
                                            if (CR[i][0] == route_noTS.get(n))
                                                System.out.println("Service leg " + (int) ((double) route_noTS.get(n)) + "{" + (int) CR[i][1] + "} -> " + (int) ((double) route_noTS.get(n+1)) + "{" + (int) CR[1][j] + "} " +
                                                        "will be removed for finding the next adjusted route");
                                            if (CR[i][0] == route_noTS.get(n+1))
                                                System.out.println("Service Leg " + (int) ((double) route_noTS.get(n+1)) + "{" + (int) CR[i][1] + "} -> " + (int) ((double) route_noTS.get(n)) + "{" + (int) CR[1][j] + "} " +
                                                        "will be removed for finding the next adjusted route");
                                            numElementsRemoved += 1;
                                            CR[i][j] -= Nv * AP_N1[i][j] * AC_N1[i][j]; // This arc will be removed from the route, so its risk shouldn't be added
                                            System.out.println("CR returns to the previous value: " + CR[i][j] + " < delta: " + delta[i][j]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (numElementsRemoved > 0) {
                anyRemoval = true;
                numElementsRemoved = 0;
                numAdjustment += 1;
                System.out.println();
                System.out.println("---------------------------------- ADJUSTED ROUTE #" + numAdjustment + " ----------------------------------");

                sortedAC_withDuplicates_arrList = new ArrayList<>();  // Sorted AC (resulting from Nv hazmat railcars) using MODIFIED AC
                sortedAC_withDuplicates_arrList.add(0.0);
                for (int i = 2; i < AC.length; i++)
                    for (int j = 2; j < AC.length; j++)
                        if (AC[i][j] > 0)
                            sortedAC_withDuplicates_arrList.add(AC[i][j]);

                sortedAC_arrList = sortedAC_withDuplicates_arrList
                        .stream().distinct().sorted().collect(Collectors.toList());
                // removed duplicates from sortedAC_withDuplicates_arrList and sorted it (using Java 8 Lambdas)

                // Convert sortedAC_arrList to an array (sortedAC)
                sortedAC= sortedAC_arrList.stream().mapToDouble(i -> i).toArray();

                C_rPlusfBar_r = new ArrayList<>();
                route_r = new ArrayList<>();


                System.out.println();
                for (int r = 0; r < sortedAC.length; r++) { // For r = 0 to M do:

                    double[][] wBarMatrix = new double[AC.length][]; // weights of arcs and yards: modifications in (11) and (12)
                    for (int i = 0; i < AC[0].length; i++)
                        wBarMatrix[i] = AC[i].clone();
                    for (int i = 2; i < AC.length; i++) {
                        for (int j = 2; j < AC.length; j++) {
                            if (AC[i][j] > 0) {
                                if (AC[i][j] > sortedAC[r])
                                    wBarMatrix[i][j] = AP[i][j]*(AC[i][j] - sortedAC[r])/(1-alpha);
                                else
                                    wBarMatrix[i][j] = 0;
                            }
                        }
                    }
                    double[][] adjacencyMatrix = new double[wBarMatrix.length - 2][wBarMatrix.length - 2];
                    for (int i = 0; i < adjacencyMatrix.length; i++) {
                        for (int j = 0; j < adjacencyMatrix.length; j++) { // adjacencyMatrix doesn't have the first two rows and columns of wBarMatrix
                            adjacencyMatrix[i][j] = -1;
                            if (wBarMatrix[i+2][j+2] >= 0)
                                adjacencyMatrix[i][j] = wBarMatrix[i+2][j+2]; // the other elements of adjacencyMatrix are -1
                        }
                    }
                    List<List<Double>> allRoutes = new ArrayList<>();  // All routes found between the origin and destination using Dijkstra's Algorithm
                    // (from the origin with different TS to the destination with different TS !!!)
                    for (int i = 2; i < wBarMatrix.length; i++) {
                        for (int j = 2; j < wBarMatrix.length; j++) {
                            if (wBarMatrix[i][0] == Ov && wBarMatrix[0][j] == Dv) {
                                List<Double> route;
                                try {
                                    route = DijkstrasAlg.dijkstra(adjacencyMatrix, i - 2, j - 2);
                                } catch (ArrayIndexOutOfBoundsException exc) {
                                        System.out.println("Dijkstra's Algorithm Infeasible!!!");
                                        exc.printStackTrace();
                                        DijkstraInfeasible = true;
                                        return Arrays.asList(CVaRstar_alpha_vBar, CR, DijkstraInfeasible);
                                }
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
                    double fBar_r = bestRoute.get(0); // the first element of route shows fBar_r and the rest shows the route itself
                    C_rPlusfBar_r.add(sortedAC[r] + fBar_r);

//                    System.out.println("r = " + r + ", C_r = " + sortedAC[r] + ", fBar_r = " + fBar_r +
//                            " => C_r + fBar_r = " + (sortedAC[r]+fBar_r));
//
//                    List<Integer> bestRoute_display = new ArrayList<>();
//                    for (int i = 1; i < bestRoute.size(); i++) {
//                        bestRoute_display.add((int) ALPTY[(int) (bestRoute.get(i) + 2)][0]);
//                        bestRoute_display.add((int) ALPTY[(int) (bestRoute.get(i) + 2)][1]);
//                    }
//                    for (int i = 0; i < bestRoute_display.size(); i += 2) {
//                        System.out.print(bestRoute_display.get(i));
//                        System.out.print("{");
//                        System.out.print(bestRoute_display.get(i+1));
//                        System.out.print("}");
//                        if (i != bestRoute_display.size()-2)
//                            System.out.print(" -> ");
//                        else
//                            System.out.println();
//                    }
                }
                minIndex = C_rPlusfBar_r.indexOf(Collections.min(C_rPlusfBar_r));
                System.out.println();
                System.out.println("r* = " + minIndex + ", CVaR*_alpha(vBar) = C_r* + fBar_r* = " + C_rPlusfBar_r.get(minIndex));
                CVaRstar_alpha_vBar = C_rPlusfBar_r.get(minIndex);

                // and the corresponding route to r* is route_r.get(minIndex):
                route_display = new ArrayList<>();
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

                route_noTS = new ArrayList<>();
                for (int i = 1; i < route_r.get(minIndex).size(); i++) {
                    route_noTS.add(ALPTY[(int) (route_r.get(minIndex).get(i) + 2)][0]);
                }
                System.out.println("The ADJUSTED route with no TS: ");
                System.out.println(route_noTS);


            } else { // numElementsRemoved = 0
                anyRemoval = false;
            }
        }

        return Arrays.asList(CVaRstar_alpha_vBar, CR, DijkstraInfeasible);
    }
}
