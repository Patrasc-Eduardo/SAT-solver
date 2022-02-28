import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Task2 extends Task {
  int N, M, K;
  int[][] mat;
  int[][] y;
  int V;
  boolean oracleStatement;
  ArrayList<Integer> encodedVars = new ArrayList<>();

  public Task2() {
    N = 0;
    M = 0;
    V = 0;
    K = 0;
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
          firstLine++;
          K = 1;
          mat = new int[N][N];

          for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
              mat[i][j] = 0; // intializam mat de adiacenta
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

    // matricea de clauze / variabile encodate
    y = new int[N][K];
    int aux = 1;
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < K; j++) {
        y[i][j] = aux; // y[0][0] ~~ y[1][1] -> primul nod = primul element din acoperire
        aux++; // si este codificat cu 1. y[1][2] = 2 (primul nod = al doilea elem din acop)..
      }
    }

    try {
      int nrClauses = 0;
      BufferedWriter writer = new BufferedWriter(new FileWriter("sat.cnf"));
      StringBuilder clauses = new StringBuilder();
      clauses.append("");

      nrClauses = buildFirstClauses(clauses, nrClauses);
      nrClauses = buildSecondClauses(clauses, nrClauses);
      nrClauses = buildThirdClauses(clauses, nrClauses);
      nrClauses = buildFourthClauses(clauses, nrClauses);

      writer.write("p cnf " + V + " " + nrClauses + "\n");
      writer.write(String.valueOf(clauses));
      writer.close();

    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public int buildFirstClauses(StringBuilder clauses, int nrClauses) {
    for (int i = 0; i < K; i++) {
      for (int j = 0; j < N; j++) {
        clauses.append(y[j][i]).append(" "); // fiecare nod = element posibil al acoperirii
      }
      clauses.append("0\n");
      nrClauses++;
    }
    return nrClauses;
  }

  public int buildSecondClauses(StringBuilder clauses, int nrClauses) {
    for (int i = 0; i < N; i++) {
      for (int k = 0; k < K; k++) {
        clauses.append(-y[i][k]).append(" ");
      }
      clauses.append("0\n");
      nrClauses++;
    }
    return nrClauses;
  }

  public int buildThirdClauses(StringBuilder clauses, int nrClauses) {
    for (int v = 0; v < N; v++) {
      for (int w = 0; w < N; w++) {
        if (w > v && mat[v][w] == 1) {
          for (int k = 0; k < K; k++) {
            clauses.append(y[v][k]).append(" ").append(y[w][k]).append(" ");
          }
          clauses.append("0\n");
          nrClauses++;
        }
      }
    }
    return nrClauses;
  }

  public int buildFourthClauses(StringBuilder clauses, int nrClauses) {
    for (int i = 0; i < K; i++) {
      for (int v = 0; v < N - 1; v++) {
        for (int w = v + 1; w < N; w++) {
          if (v != w) { // 2 noduri nu pot fi ambele al i-lea element din acoperire
            clauses.append(-y[v][i]).append(" ").append(-y[w][i]).append(" 0\n");
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
    }
    input.close();
  }

  @Override
  public void writeAnswer() throws IOException, InterruptedException {
    if (oracleStatement) {
      for (int i = 0; i < N; i++) {
        for (int j = 0; j < K; j++) {
          if (encodedVars.contains(y[i][j])) { // variabila encodata corespunde (ca nod) cu linia
            System.out.print((i + 1) + " "); // pe care se afla (linia din matricea de var encodate)
          }
        }
      }
      System.out.print("\n");
    } else {
      if (K == N) {
        System.out.println("False");
        return;
      }
      K++;
      formulateOracleQuestion();
      askOracle();
      decipherOracleAnswer();
      writeAnswer();
    }
  }
}
