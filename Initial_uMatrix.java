package PhD3;

class Initial_uMatrix {
    public static void GenerateIuM () {

        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();

        double[][] ALPE; // Arc Length & Population Exposure
        ALPE = objReadXlsxFile.setFilePath("D:\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(1).setRow(1,123).setCell(1,167)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ALPE);

        double[][] initial_uYardsInTs; // initial u values for yards in train services
        initial_uYardsInTs = objReadXlsxFile.setFilePath("D:\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(6).setRow(1,25).setCell(0,13)
                .createArrayMatrix();

        double[][] initial_uArcsInTs; // initial u values for arcs in train services
        initial_uArcsInTs = objReadXlsxFile.setFilePath("D:\\0 PhD Dissertation\\3. CVaR + Equity\\Coding\\PhD3Data_2.xlsx")
                .setSheet(7).setRow(1,83).setCell(0,6) // for PhD3Data: rowEnd is 81 & for PhD3Data_2: rowEnd is 83 !!!!!!!
                .createArrayMatrix();

        // Start building matrix initial_uMatrix using ALPE, initial_uYardsInTs and initial_uArcsInTs  // initial_uMatrix: initial u values of arcs and yards
        double[][] initial_uMatrix = new double[ALPE[0].length + 2][ALPE[0].length + 2];
        for (int i = 0; i < initial_uMatrix.length; i++)
            for (int j = 0; j < initial_uMatrix.length; j++)
                initial_uMatrix[i][j] = -1; // initially placed -1 in each cell of initial_uMatrix
        for (int i = 0; i < 2; i++)
            System.arraycopy(ALPE[i], 0, initial_uMatrix[i], 2, initial_uMatrix.length - 2);
        // copied the first two rows of ALPE (yards and train services) into the first two rows of initial_uMatrix from column 2
        for (int i = 2; i < initial_uMatrix.length; i++)
            for (int j = 0; j < 2; j++)
                initial_uMatrix[i][j] = initial_uMatrix[j][i];
        // did a transpose copy of the first two rows of initial_uMatrix (from column 2) into the first two cloumns of initial_uMatrix (from row 2)

        // Fill the cells of matrix initial_uMatrix using initial_uYardsInTs and initial_uArcsInTs
        for (int i = 2; i < initial_uMatrix.length; i++) {
            for (int j = 2; j < initial_uMatrix.length; j++) {
                if ((initial_uMatrix[i][0] == initial_uMatrix[0][j]) && (initial_uMatrix[i][1] != initial_uMatrix[1][j])) { // the same yard but different TS; potential transferring yard
                    for (int k = 0; k < initial_uYardsInTs.length; k++) {
                        if (initial_uYardsInTs[k][1] == initial_uMatrix[i][0]) {
                            initial_uMatrix[i][j] = initial_uYardsInTs[k][0];
                            break;
                        }
                    }
                }
                if ((initial_uMatrix[i][0] != initial_uMatrix[0][j]) && (initial_uMatrix[i][1] == initial_uMatrix[1][j])) { // different nodes but the same TS; an arc in a TS
                    for (int k = 0; k < initial_uArcsInTs.length; k++) {
                        if ((initial_uArcsInTs[k][1] == initial_uMatrix[i][0]) && (initial_uArcsInTs[k][2] == initial_uMatrix[0][j])) {
                            for (int m = 3; m < initial_uArcsInTs[k].length; m++) {
                                if (initial_uArcsInTs[k][m] == initial_uMatrix[i][1]) { // the TS is in the list of the arc
                                    initial_uMatrix[i][j] = initial_uArcsInTs[k][0];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Building matrix initial_uMatrix using ALPE, initial_uYardsInTs and initial_uArcsInTs is done!
        //objReadXlsxFile.showArrayMatrix(initial_uMatrix);

        int counter_initial_uMatrix = 0;
        for (int i = 2; i < initial_uMatrix.length; i++) {
            for (int j = 2; j < initial_uMatrix.length; j++) {
                if (initial_uMatrix[i][j] >= 0) {
                    counter_initial_uMatrix ++;
                }
            }
        }

        System.out.println();
        System.out.println("counter uMatrix (u >= 0): " + counter_initial_uMatrix);

        WriteXlsxFile objWriteXlsxFile = new WriteXlsxFile();
        objWriteXlsxFile.getDataAndsetSheet(initial_uMatrix,"uMatrix").setFileName("uMatrix.xlsx");
    }
}