package PhD3;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class LinearRelaxation {
    public static void main(String[] args) {
        ReadXlsxFile objReadXlsxFile = new ReadXlsxFile();

        double[][] YardsInTS;
        YardsInTS = objReadXlsxFile.setFilePath("C:\\IntelliJProjects\\SDH\\yardsInTS.xlsx")
                .setSheet(0).setRow(1,25).setCell(1,13)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(YardsInTS);

        double[][] ArcsInTS;
        ArcsInTS = objReadXlsxFile.setFilePath("C:\\IntelliJProjects\\SDH\\arcsInTS.xlsx")
                .setSheet(0).setRow(1,81).setCell(1,6)
                .createArrayMatrix();
        //objReadXlsxFile.showArrayMatrix(ArcsInTS);

        int noYards = YardsInTS.length; // = 25
        int noArcs = ArcsInTS.length; // = 81
        int V = 600; // Number of hazmat shipments (between yards: 24*25=600)

        try {
            // Create the modeler/solver
            IloCplex cplex = new IloCplex();

            // Variable 1: xYard[v][k][s]; k shows the yard, and s is the corresponding train service
            IloNumVar[][][] xYard = new IloNumVar[V][noYards][];
            for (int v = 0; v < V; v++) {
                for (int k = 0; k < noYards; k++) {
                    xYard[v][k] = cplex.numVarArray(YardsInTS[k].length - 1, 0, Double.MAX_VALUE);
                }
            }

            // Variable 2: xArc[v][i][s]; i shows the arc, and s is the corresponding train service
            IloNumVar[][][] xArc = new IloNumVar[V][noArcs][];
            for (int v = 0; v < V; v++) {
                for (int i = 0; i < noArcs; i++) {
                    xArc[v][i] = cplex.numVarArray(ArcsInTS[i].length - 2, 0, Double.MAX_VALUE);
                }
            }




        }
        catch (IloException exc) {
            exc.printStackTrace();
        }

    }
}
