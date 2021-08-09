package algorithms;

import java.util.ArrayList;
import java.util.List;

public class ZScore {
    public float  ZThreshold; // Threshold for each feature.
    List<AnomalyReport> Report; // List of Anomaly reports.

    public static float [] CalcZScore(float [] Column) {
        float[] Results = new float[Column.length];// Array results of z-scores
        for (int i = 0; i <Column.length - 1 ; i++) {
        float[] FixedColumn = new float[i + 1];
        System.arraycopy(Column, 0, FixedColumn, 0, FixedColumn.length); // Copy the column without the last index.
        float Avg = avg(FixedColumn); // Calc The Avg.
        Results[i] = ((Math.abs(Column[i] - Avg))/ ((float) Math.sqrt(var(FixedColumn)))); // Z-score calculation.
        }
        return Results;
    }

    public static float FindTH(float[] Results) { // Function that find the maximum z-score result.
        float  max =0;
        for (float result : Results)if (max < result) max = result;
        return max;
    }

    public static float avg(float[] x) {
        float Sum = 0;
        for (float v : x)
            Sum += v;
        return (Sum/x.length);
    }

    public static float var(float[] x) {
        float u = avg(x), Var = 0;
        for (int i = 0;i < x.length; i++ ) {
            Var = Var + x[i]*x[i];
        }
        Var = Var/x.length;
        Var = Var - u*u;
        return Var;
    }


    public void learnNormal(TimeSeries ts, int index) { // A Machine learning function.
        float[] ZScoreResults=CalcZScore(ts.Data[index]);
        ZThreshold=FindTH(ZScoreResults); // set a threshold for each feature.
    }

    public List<AnomalyReport> detect(TimeSeries ts,int index) { // Function that detects data error and insert those errors to an anomaly report.
        this.Report = new ArrayList<>();
        float[] ZScoreResults= CalcZScore(ts.Data[index]);
        for(int j =0; j < ZScoreResults.length;j++)
                if(ZThreshold< ZScoreResults[j]) // check if there is a result above the current threshold.
                    Report.add(new AnomalyReport(ts.Features[index], j));
        return Report;
    }
}


