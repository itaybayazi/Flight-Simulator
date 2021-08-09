package algorithms;

import java.util.ArrayList;
import java.util.List;
import static algorithms.StatLib.*;

public class SimpleAnomalyDetector/* implements TimeSeriesAnomalyDetector*/ {
	public List <CorrelatedFeatures> CorrelatedList = new ArrayList<>();;
	public List<AnomalyReport> Report  = new ArrayList<>();
	public static float Threshold =(float)0.9;
	public float[] dev;

	public void learnNormal(TimeSeries ts) {
		int len=ts.Features.length;
		int [] max=new int[len];
		float [] pearson=new float[len];
		Line[] reg=new Line[len];
		this.dev=new float[len];
		for(int i=0;i<len;i++)
		{
			max[i]=Maxthreshold(i,ts.Data);
			if (max[i] != -1) {
				pearson[i] = StatLib.pearson(ts.Data[i], ts.Data[max[i]]);
				reg[i] = linear_reg(ts.CreateNewPointArray(i, max[i], ts.Data));
				this.dev[i] = ts.CheckDev(ts.CreateNewPointArray(i, max[i], ts.Data));
				this.CorrelatedList.add(new CorrelatedFeatures(ts.Features[i], ts.Features[max[i]], pearson[i], reg[i], dev[i]));
			}
		}
	}


	public List<AnomalyReport> detect(TimeSeries ts) {
		this.Report = new ArrayList<>();
		float[] x,y;
		for(CorrelatedFeatures cr: CorrelatedList)
		{
			x=ts.Data[getline(cr.feature1,ts)];
			y=ts.Data[getline(cr.feature2,ts)];
			for(int j =0;j<ts.Data[0].length - 1;j++)
			{
				Point point=new Point(x[j],y[j]);
				if(dev(point,cr.lin_reg)> cr.threshold + 0.1)
				{
					Report.add(new AnomalyReport(cr.feature1+"-"+cr.feature2,j+1));
				}
			}
		}
		return this.Report;
	}

	public int getline(String name, TimeSeries  ts)
	{
		for(int i=0;i<ts.Features.length;i++)
		{
			if(name.equals(ts.Features[i]))
				return i;
		}
		return -1;
	}

	public List<CorrelatedFeatures> getNormalModel(){
		return this.CorrelatedList;
	}

	public int Maxthreshold(int main, float[][] data)
	{
		float max = Threshold;
		int index=-1;
		for(int j= main+1;j<data.length;j++)
		{
			float res=0;
			if(main!=j) {
				res = Math.abs(pearson(data[main], data[j]));
				if (res > max) {
					max = res;
					index = j;
				}
			}
		}
		return index;
	}
}
