package algorithms;

import java.io.*;
import java.util.*;
import static algorithms.StatLib.*;

public class TimeSeries {

	public float[][] Data;
	public String[] Features;

	public TimeSeries(String csvFileName) {
	try {
		int counter ;
		int RowLen = 0;
		BufferedReader LineReader = new BufferedReader(new FileReader(csvFileName));
		while ((LineReader.readLine()) != null) {
			RowLen++;
		}
		LineReader.close();
		Scanner InputStream = new Scanner(new BufferedReader(new FileReader(csvFileName)));
		String data = InputStream.next();
		this.Features = data.split(",");
		counter = Features.length;
		 this.Data = new float[counter][RowLen];
		for (int i = 0; InputStream.hasNext(); i++) {
			data = InputStream.next();
			String[] strData = data.split(",");
			for (int j = 0; j < counter; j++) {
				this.Data[j][i] = Float.parseFloat(strData[j]);
			}
		}
		InputStream.close();
	}
	catch (IOException ignored) {}
	}

	public Point[] CreateNewPointArray(int XIndex,int YIndex,float[][] data)
	{
		Point[] point = new Point[data[0].length-1];
		for (int i = 0; i < data[0].length-1; i++) {
			point[i] = new Point(data[XIndex][i],data[YIndex][i]);
		}
		return point;
	}

	public float CheckDev(Point[] P)
	{
		float Max = 0, DevRes;
		for (Point point : P) {
			DevRes = dev(point, P);
			if (DevRes > Max) {
				Max = DevRes;
			}
		}
		return Max;
	}
	public float[][] getData(){return Data;}

	public int CheckCorrelation(int Correlated){ // Function that check the correlation between the flight features.
		float Threshold = -1;
		int CorrelatedIndex = -1;
		for (int j = 0; j < this.Features.length; j++) {
			if (Correlated != j) {
				float Pearson = StatLib.pearson(this.Data[Correlated], this.Data[j]);
				if(Pearson > Threshold) {
					Threshold = Pearson;
					CorrelatedIndex = j;
				}
			}
		}
		return CorrelatedIndex;
	}

	public float GetPearson(float[] feature1,float[] feature2){ return pearson(feature1,feature2);}

	public float ReturnDev(Point P , Line l){return dev(P,l);}
}
