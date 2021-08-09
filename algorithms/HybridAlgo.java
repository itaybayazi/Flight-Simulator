package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HybridAlgo implements TimeSeriesAnomalyDetector {
    public TimeSeries train;
    public TimeSeries test;
    public SimpleAnomalyDetector AD = new SimpleAnomalyDetector();
    public List<AnomalyReport> Report = new ArrayList<>();
    public Circle LearnNormalCircle;
    public HybridAlgo() {}

    public void Hybrid(float Correlation, int index1, int index2) // Function the determine which algorithm to use according to the correlation.
    {
        if ((Correlation < 0.95) && (Correlation >= 0.5)) {       //use Welzl algorithm.
            List<Point> ListOfPoints = makeListOfPoints(train.Data[index1], train.Data[index2]);
            LearnNormalCircle = Welzl.RunWelzl(ListOfPoints);
            Point[] DetectPoints = CreatePointsArray(test.Data[index1], test.Data[index2]);
            Report = CheckIfContainsPoints(LearnNormalCircle, DetectPoints, train.Features[index1], train.Features[index2]);
        } else {
            if (Correlation >= 0.95)        // use linear regression algorithm.
                Report = AD.Report;
             else {          // use Z-score algorithm.
                ZScore Z_Score = new ZScore();
                Z_Score.learnNormal(train,index1);
                Report = Z_Score.detect(test, index1);
            }
        }
    }

    Point[] CreatePointsArray(float[] Feature1, float[] Feature2) {
        Point[] NewPointArray = new Point[Feature1.length];
        for (int i = 0; i < NewPointArray.length; i++) {
            NewPointArray[i] = new Point(Feature1[i], Feature2[i]);
        }
        return NewPointArray;
    }

    public List<AnomalyReport> CheckIfContainsPoints(Circle circle, Point[] points, String feature1, String feature2) { //Function to check if the circle contains the points
        List<AnomalyReport> Report = new ArrayList<>();
        for (int i = 0; i < points.length; i++)
            if (points[i] != circle.center && !circle.containsPoint(points[i]))
                Report.add(new AnomalyReport(feature1 + "-" + feature2, i));
        return Report;
    }

    public List<Point> makeListOfPoints(float[] Feature1, float[] Feature2) {
        Point[] points = CreatePointsArray(Feature1, Feature2);
        return Arrays.asList(points);
    }

    @Override
    public void learnNormal(TimeSeries ts) {AD.learnNormal(train);}

    @Override
    public List<AnomalyReport> detect(TimeSeries ts) {return AD.detect(test);}
}

