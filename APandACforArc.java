package PhD3;

import java.util.ArrayList;
import java.util.List;

class APandACforArc {
    public static void main(String[] args) {

        int numTrainServices = 31; // Number of train services in the network
        int Nv = 1; // Number of hazmat railcars in shipment v

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();

        double[][] ALPE; // Arc Length & Population Exposure
        ALPE = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data.xlsx")
                .setSheet(1).setRow(1,123).setCell(1,167)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ALPE);

        double[][] YPE; // Yard Population Exposure
        YPE = objReadXlsxFile.setFilePath("E:\\PhD & MSc\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data.xlsx")
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
        for (int i = 0; i < ALPTY[0].length; i++)  // AP: Accident Probability
            AP[i] = ALPTY[i].clone();

        double[][] AC = new double[ALPTY.length][]; // Start building matrix AC by copying ALPTY
        for (int i = 0; i < ALPTY[0].length; i++)  // AC: Accident Consequence
            AC[i] = ALPTY[i].clone();


        for (int k = 1; k <= numTrainServices; k++) {
            for (int j = 0; j < ALPE[0].length; j++) {
                if (ALPE[1][j] == k && ALPE[2][j] >= 0) {
                    ALPTY[j+2][j+3] = ALPE[2][j]; // placed arc length in ALPTY
                    AP[j+2][j+3] = ALPTY[j+2][j+3] * 7.35 * Math.pow(10, -11) * Nv; // calculated AP of the arc
                    AC[j+2][j+3] = ALPE[Nv+2][j]; // placed AC of the arc (resulting from Nv hazmat railcars) using ALPE
                }
            }
        }

        for (int i = 2; i < ALPTY.length; i++) {
            for (int j = 2; j < ALPTY.length; j++) {
                if (ALPTY[i][0] == ALPTY[0][j] && ALPTY[i][1] != ALPTY[1][j]) { // same yard but different train service
                    ALPTY[i][j] = 0; // placed zero for potential transferring yards
                    AP[i][j] = 6.42 * Math.pow(10, -10) * Nv; // calculated AP of the yard
                    AC[i][j] = YPE[Nv][(int) ALPTY[0][j]-1]; // placed AC of the yard (resulting from Nv hazmat railcars) using YPE
                }
            }
        }
        // Building matrices ALPTY, AP, and AC are done!
        //objReadXlsxFile.showArrayMatrix(AP);

        double[][] ArcsInTS;
        ArcsInTS = objReadXlsxFile.setFilePath("C:\\IntelliJProjects\\SDH\\arcsInTS.xlsx")
                .setSheet(0).setRow(1,81).setCell(1,6)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ArcsInTS);
        
        // Start building matrix AP_Arc and AC_Arc using ArcsInTS, AP, and AC
        List<List<Double>> AP_Arc_arrList = new ArrayList<>();  // AP of arcs in a 2D arrayList
        List<List<Double>> AC_Arc_arrList = new ArrayList<>();  // AC of arcs in a 2D arrayList
        for (double[] crntArc : ArcsInTS) {
            List<Double> arcProbAttribute = new ArrayList<>();
            List<Double> arcConsAttribute = new ArrayList<>();
            arcProbAttribute.add(crntArc[0]); // arc starting node
            arcProbAttribute.add(crntArc[1]); // arc finish node
            arcConsAttribute.add(crntArc[0]); // arc starting node
            arcConsAttribute.add(crntArc[1]); // arc finish node
            List<Double> arcProbability = new ArrayList<>();
            List<Double> arcConsequence = new ArrayList<>();
            for (int i = 2; i < ALPTY.length; i++) {
                for (int j = 2; j < ALPTY.length; j++) {
                    if (AP[i][j] != -1) {
                        if (AP[i][0] == crntArc[0] && AP[0][j] == crntArc[1] && AP[i][1] == AP[1][j]) { // Two nodes of the arc have the same TS
                            arcProbability.add(AP[i][j]); // found the AP of the arc in all TS
                        }
                    }
                    if (AC[i][j] != -1) {
                        if (AC[i][0] == crntArc[0] && AC[0][j] == crntArc[1] && AC[i][1] == AC[1][j]) { // Two nodes of the arc have the same TS
                            arcConsequence.add(AC[i][j]); // found the AC of the arc in all TS
                        }
                    }
                }
            }
            for (int i = 0; i < arcProbability.size(); i++) {
                for (int j = i + 1; j < arcProbability.size(); j++) {
                    if (arcProbability.get(i).doubleValue() != arcProbability.get(j).doubleValue()) {
                        System.out.println("Error: AP of the arc in different TS is not the same!");
                        System.out.println("The program is terminating ...");
                        System.exit(1);
                    }
                }
            }
            for (int i = 0; i < arcConsequence.size(); i++) {
                for (int j = i + 1; j < arcConsequence.size(); j++) {
                    if (arcConsequence.get(i).doubleValue() != arcConsequence.get(j).doubleValue()) {
                        System.out.println("Error: AC of the arc in different TS is not the same!");
                        System.out.println("The program is terminating ...");
                        System.exit(1);
                    }
                }
            }
            arcProbAttribute.add(arcProbability.get(0)); // arc AP; AP of the arc (resulting from 1 hazmat railcar) in all TS is the same :)
            AP_Arc_arrList.add(arcProbAttribute);
            arcConsAttribute.add(arcConsequence.get(0)); // arc AC; AC of the arc (resulting from 1 hazmat railcar) in all TS is the same :)
            AC_Arc_arrList.add(arcConsAttribute);
        }

// For Nv >= 2 ---------------------------------------------------------------------------------------------------------

        for (int n = 2; n <= 120; n++) {
            Nv = n;
            for (int k = 1; k <= numTrainServices; k++) {
                for (int j = 0; j < ALPE[0].length; j++) {
                    if (ALPE[1][j] == k && ALPE[2][j] >= 0) {
                        ALPTY[j+2][j+3] = ALPE[2][j]; // placed arc length in ALPTY
                        AP[j+2][j+3] = ALPTY[j+2][j+3] * 7.35 * Math.pow(10, -11) * Nv; // calculated AP of the arc
                        AC[j+2][j+3] = ALPE[Nv+2][j]; // placed AC of the arc (resulting from Nv hazmat railcars) using ALPE
                    }
                }
            }

            for (int i = 2; i < ALPTY.length; i++) {
                for (int j = 2; j < ALPTY.length; j++) {
                    if (ALPTY[i][0] == ALPTY[0][j] && ALPTY[i][1] != ALPTY[1][j]) { // same yard but different train service
                        ALPTY[i][j] = 0; // placed zero for potential transferring yards
                        AP[i][j] = 6.42 * Math.pow(10, -10) * Nv; // calculated AP of the yard
                        AC[i][j] = YPE[Nv][(int) ALPTY[0][j]-1]; // placed AC of the yard (resulting from Nv hazmat railcars) using YPE
                    }
                }
            }

            for (List<Double> crntArc : AP_Arc_arrList) {
                for (int i = 2; i < AP.length; i++) {
                    boolean breakForLoop = false;
                    for (int j = 2; j < AP.length; j++) {
                        if (AP[i][j] != -1) {
                            if (AP[i][0] == crntArc.get(0) && AP[0][j] == crntArc.get(1) && AP[i][1] == AP[1][j]) { // Two nodes of the arc have the same TS
                                crntArc.add(AP[i][j]);
                                breakForLoop = true;
                                break;
                            }
                        }
                    }
                    if (breakForLoop) break;
                }
            }
            for (List<Double> crntArc : AC_Arc_arrList) {
                for (int i = 2; i < AC.length; i++) {
                    boolean breakForLoop = false;
                    for (int j = 2; j < AC.length; j++) {
                        if (AC[i][j] != -1) {
                            if (AC[i][0] == crntArc.get(0) && AC[0][j] == crntArc.get(1) && AC[i][1] == AC[1][j]) { // Two nodes of the arc have the same TS
                                crntArc.add(AC[i][j]);
                                breakForLoop = true;
                                break;
                            }
                        }
                    }
                    if (breakForLoop) break;
                }
            }
        }

        // Convert AP_Arc_arrList from arrayList to array (AP_Arc)
        double [][] AP_Arc = new double[AP_Arc_arrList.size()][];
        for (int k = 0; k < AP_Arc_arrList.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(AP_Arc_arrList.get(k));
            AP_Arc[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }
        // Convert AC_Arc_arrList from arrayList to array (AC_Arc)
        double [][] AC_Arc = new double[AC_Arc_arrList.size()][];
        for (int k = 0; k < AC_Arc_arrList.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(AC_Arc_arrList.get(k));
            AC_Arc[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }

        System.out.println();
        // Write AP_Arc and AC_Arc into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
        objWriteXlsxFile.getDataAndsetSheet(AP_Arc,"AP_Arc").
                getDataAndsetSheet(AC_Arc,"AC_Arc").setFileName("APandACforArc.xlsx");

        System.out.println("AP_Arc: The first two numbers show the arc, " +
                "and the rest show the accident probability for Nv = 1, 2, 3, 4, ...");
        System.out.println("AC_Arc: The first two numbers show the arc, " +
                "and the rest show the accident consequence for Nv = 1, 2, 3, 4, ...");

    }
}