package PhD3;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class CVaR_Alg { // "myRoute" is shown based on matrix ALPTY
    static void cvarAlg (double alpha, int numTrainServices, int[] myRoute, double Nv) {

        System.out.println("alpha = " + alpha);
        System.out.println("Number of train services = " + numTrainServices);
        System.out.println("myRoute based on matrix ALPTY: " + Arrays.toString(myRoute));
        System.out.println("Number of hazmat railcars in this shipment = " + Nv);

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();

        double[][] ALPE; // Arc Length & Population Exposure
        ALPE = objReadXlsxFile.setFilePath("D:\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data.xlsx")
                .setSheet(1).setRow(1,123).setCell(1,167)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ALPE);

        double[][] YPE; // Yard Population Exposure
        YPE = objReadXlsxFile.setFilePath("D:\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data.xlsx")
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

        //System.out.println(AP[21][22]);
        //System.out.println(AC[21][22]);
        //System.out.println(AC[15][45]);
        //System.out.println(AP[24][67]);


        List<Integer> route_display = new ArrayList<>();
        for (int i = 0; i < myRoute.length; i++) {
            route_display.add((int) ALPTY[myRoute[i]][0]);
            route_display.add((int) ALPTY[myRoute[i]][1]);
        }
        System.out.println();
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

        // Calculate the CVaR of myRoute
        List<Pair<Double, Double>> ACandAPofRoute = new ArrayList<>();
        for (int i = 0; i < myRoute.length - 1; i++) {
            ACandAPofRoute.add(new Pair<>(AC[myRoute[i]][myRoute[i+1]], AP[myRoute[i]][myRoute[i+1]]));
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

    }
}
