import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Task3 extends Task {
  int N, M, K;
  int[][] mat;
  int[][] y;
  int V;
  boolean oracleStatement;
  ArrayList<Integer> encodedVars = new ArrayList<>();

  public Task3() {
    N = 0;
    M = 0;
    K = 0;
    V = 0;
    oracleStatement = false;
  }

  @Override
  public void solve() throws IOException, InterruptedException {
    readProblemData();
    formulateOracleQuestion();
    askOracle();
    decipherOracleAnswer();
    writeAnswer();
  }

  @Override
  public void readProblemData() throws IOException {
    try {

      InputStreamReader in = new InputStreamReader(System.in);

      BufferedReader input = new BufferedReader(in);

      String str;

      int firstLine = 0;

      while ((str = input.readLine()) != null) {
        String[] splitStr = str.trim().split(" ");
        if (firstLine == 0) {
          N = Integer.parseInt(splitStr[0]);
          M = Integer.parseInt(splitStr[1]);
          K = Integer.parseInt(splitStr[2]);
          firstLine++;

          mat = new int[N][N];
          y = new int[N][K];

          for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
              mat[i][j] = 0; // intializam mat de adiacenta
            }
          }

          // matricea de clauze / variabile encodate
          int aux = 1;
          for (int i = 0; i < N; i++) {
            for (int j = 0; j < K; j++) {
              y[i][j] = aux; // y[0][0] ~~ y[1][1] -> primul nod = primul element din acoperire
              aux++; // si este codificat cu 1. y[1][2] = 2 (primul nod = al doilea elem din
              // acop)...
            }
          }

        } else {
          if (splitStr.length != 1) {

            int i = Integer.parseInt(splitStr[0]);
            int j = Integer.parseInt(splitStr[1]);

            mat[i - 1][j - 1] = 1; // umplem matricea de adiacenta
            mat[j - 1][i - 1] = 1; // in ambele sensuri deoarece lucram pe graf neorientat
          } else {
            break;
          }
        }
      }
    } catch (IOException io) {
      io.printStackTrace();
    }
  }

  @Override
  public void formulateOracleQuestion() throws IOException {
    int V = N * K;
    this.V = V;

    try {
      int nrClauses = 0;
      BufferedWriter writer = new BufferedWriter(new FileWriter("sat.cnf"));
      StringBuilder clauses = new StringBuilder();
      clauses.append("");

      nrClauses = buildFirstClauses(clauses, nrClauses);
      nrClauses = buildSecondClauses(clauses, nrClauses);
      nrClauses = buildThirdClauses(clauses, nrClauses);

      writer.write("p cnf " + V + " " + nrClauses + "\n");
      writer.write(String.valueOf(clauses));
      writer.close();
      //System.out.println(clauses);
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public int buildFirstClauses(StringBuilder clauses, int nrClauses) {
    for (int i = 0; i < N; i++) {
      for (int k = 0; k < K; k++) {
        clauses.append(y[i][k]).append(" "); // fiecare nod = element posibil al acoperirii
      }
      clauses.append("0\n");
      nrClauses++;
    }
    return nrClauses;
  }

  public int buildSecondClauses(StringBuilder clauses, int nrCLauses) {
    for (int i = 0; i < N; i++) {
      for (int c = 0; c < K - 1; c++) {
        for (int d = c + 1; d < K; d++) {
          clauses.append(-y[i][c]).append(" ").append(-y[i][d]).append(" 0\n");
          nrCLauses++;
        }
      }
    }
    return nrCLauses;
  }

  public int buildThirdClauses(StringBuilder clauses, int nrClauses) {
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
        if (j > i && mat[i][j] == 1) {
          for (int c = 0; c < K; c++) {
            clauses.append(-y[i][c]).append(" ").append(-y[j][c]).append(" 0\n");
            nrClauses++;
          }
        }
      }
    }
    return nrClauses;
  }

  @Override
  public void decipherOracleAnswer() throws IOException {
    String currentLine = null;
    BufferedReader input = new BufferedReader(new FileReader("sat.sol"));
    encodedVars.clear();
    currentLine = input.readLine();

    if (currentLine.compareTo("True") == 0) {
      oracleStatement = true;

      String aux = input.readLine();
      String vars = input.readLine();

      String[] splitVars = vars.split(" ");
      for (int i = 0; i < V; i++) {
        int num = Integer.parseInt(splitVars[i]);
        if (num > 0) {
          encodedVars.add(num); // adaugam un vectorul de variabile encodate, doar var pozitive
        }
      }
    } else {
      oracleStatement = false;
    }
    input.close();
  }

  @Override
  public void writeAnswer() throws IOException, InterruptedException {
    if (oracleStatement) {
      System.out.println("True");

      for (int i = 0; i < N; i++) {
        for (int j = 0; j < K; j++) {
          if (encodedVars.contains(y[i][j])) { // variabila encodata corespunde (ca nod) cu linia
            System.out.print((j + 1) + " "); // pe care se afla (linia din matricea de var encodate)
          }
        }
      }
      System.out.print("\n");
    } else {
      System.out.println("False");
    }
  }
}
