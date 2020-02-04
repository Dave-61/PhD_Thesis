package PhD3;

import java.util.ArrayList;
import java.util.List;

class APandACforYard {
    public static void main(String[] args) {

        int numTrainServices = 31; // Number of train services in the network
        int Nv = 1; // Number of hazmat railcars in shipment v

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

        double[][] YarsInTS;
        YarsInTS = objReadXlsxFile.setFilePath("C:\\IntelliJProjects\\SDH\\yardsInTS.xlsx")
                .setSheet(0).setRow(1,25).setCell(1,13)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(YarsInTS);
        
        // Start building matrix AP_Yard and AC_Yard using YarsInTS, AP, and AC
        List<List<Double>> AP_Yard_arrList = new ArrayList<>();  // AP of yards in a 2D arrayList
        List<List<Double>> AC_Yard_arrList = new ArrayList<>();  // AC of yards in a 2D arrayList
        for (double[] crntYard : YarsInTS) {
            List<Double> yardProbAttribute = new ArrayList<>();
            List<Double> yardConsAttribute = new ArrayList<>();
            yardProbAttribute.add(crntYard[0]); // yard
            yardConsAttribute.add(crntYard[0]); // yard
            List<Double> yardProbability = new ArrayList<>();
            List<Double> yardConsequence = new ArrayList<>();
            for (int i = 2; i < ALPTY.length; i++) {
                for (int j = 2; j < ALPTY.length; j++) {
                    if (AP[i][j] != -1) {
                        if (AP[i][0] == crntYard[0] && AP[0][j] == crntYard[0] && AP[i][1] != AP[1][j]) { // Yard in two different TS
                            yardProbability.add(AP[i][j]); // found the AP of the yard in all TS
                        }
                    }
                    if (AC[i][j] != -1) {
                        if (AC[i][0] == crntYard[0] && AC[0][j] == crntYard[0] && AC[i][1] != AC[1][j]) { // Yard in two different TS
                            yardConsequence.add(AC[i][j]); // found the AC of the yard in all TS
                        }
                    }
                }
            }
            for (int i = 0; i < yardProbability.size(); i++) {
                for (int j = i + 1; j < yardProbability.size(); j++) {
                    if (yardProbability.get(i).doubleValue() != yardProbability.get(j).doubleValue()) {
                        System.out.println("Error: AP of the yard in different TS is not the same!");
                        System.out.println("The program is terminating ...");
                        System.exit(0);
                    }
                }
            }
            for (int i = 0; i < yardConsequence.size(); i++) {
                for (int j = i + 1; j < yardConsequence.size(); j++) {
                    if (yardConsequence.get(i).doubleValue() != yardConsequence.get(j).doubleValue()) {
                        System.out.println("Error: AC of the yard in different TS is not the same!");
                        System.out.println("The program is terminating ...");
                        System.exit(0);
                    }
                }
            }
            yardProbAttribute.add(yardProbability.get(0)); // yard AP; AP of the yard (resulting from 1 hazmat railcar) in all TS is the same :)
            AP_Yard_arrList.add(yardProbAttribute);
            yardConsAttribute.add(yardConsequence.get(0)); // yard AC; AC of the yard (resulting from 1 hazmat railcar) in all TS is the same :)
            AC_Yard_arrList.add(yardConsAttribute);
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

            for (List<Double> crntYard : AP_Yard_arrList) {
                for (int i = 2; i < AP.length; i++) {
                    boolean breakForLoop = false;
                    for (int j = 2; j < AP.length; j++) {
                        if (AP[i][j] != -1) {
                            if (AP[i][0] == crntYard.get(0) && AP[0][j] == crntYard.get(0) && AP[i][1] != AP[1][j]) { // Yard in two different TS
                                crntYard.add(AP[i][j]);
                                breakForLoop = true;
                                break;
                            }
                        }
                    }
                    if (breakForLoop) break;
                }
            }
            for (List<Double> crntYard : AC_Yard_arrList) {
                for (int i = 2; i < AC.length; i++) {
                    boolean breakForLoop = false;
                    for (int j = 2; j < AC.length; j++) {
                        if (AC[i][j] != -1) {
                            if (AC[i][0] == crntYard.get(0) && AC[0][j] == crntYard.get(0) && AC[i][1] != AC[1][j]) { // Yard in two different TS
                                crntYard.add(AC[i][j]);
                                breakForLoop = true;
                                break;
                            }
                        }
                    }
                    if (breakForLoop) break;
                }
            }
        }

        // Convert AP_Yard_arrList from arrayList to array (AP_Yard)
        double [][] AP_Yard = new double[AP_Yard_arrList.size()][];
        for (int k = 0; k < AP_Yard_arrList.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(AP_Yard_arrList.get(k));
            AP_Yard[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }

        // Convert AC_Yard_arrList from arrayList to array (AC_Yard)
        double [][] AC_Yard = new double[AC_Yard_arrList.size()][];
        for (int k = 0; k < AC_Yard_arrList.size(); k++) {
            List<Double> currentBracket = new ArrayList<>(AC_Yard_arrList.get(k));
            AC_Yard[k] = currentBracket.stream().mapToDouble(i -> i).toArray();
        }

        System.out.println();
        // Write AP_Yard and AC_Yard into a xlsx (Excel) file
        WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
        objWriteXlsxFile.getDataAndsetSheet(AP_Yard,"AP_Yard").
                getDataAndsetSheet(AC_Yard,"AC_Yard").setFileName("APandACforYard.xlsx");

        System.out.println("AP_Yard: The first number shows the yard, " +
                "and the rest show the accident probability for Nv = 1, 2, 3, 4, ...");
        System.out.println("AC_Yard: The first number shows the yard, " +
                "and the rest show the accident consequence for Nv = 1, 2, 3, 4, ...");

    }
}