package algorithms;


public class StatLib {
	public static float avg(float[] x){
		float Average = 0;
		for (float v : x) {
			Average = Average + v;
		}
		Average = Average/x.length;
		return Average;
	}

	// returns the variance of X and Y
	public static float var(float[] x){
		float u , Var = 0;
		u =avg(x);
		for (int i = 0;i < x.length; i++ ) {
			Var = Var + x[i]*x[i];
		}
		Var = Var/x.length;
		Var = Var - u*u;
		return Var;
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		float AvgX = avg(x) ,AvgY = avg(y) , Cov = 0;
		for (int i = 0 ; i<x.length ; i++)
		{
			Cov =Cov +((x[i]-AvgX)*(y[i]-AvgY));
		}
		Cov = Cov / x.length;
		return Cov;
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		float sqrtX =(float)Math.sqrt(var(x));
		float sqrtY =(float)Math.sqrt(var(y));
		return cov(x,y)/(sqrtX*sqrtY);
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		float a,b;
		float[] x = new float[points.length];
		float[] y = new float[points.length];
		for(int i =0 ;i<points.length;i++) {
			x[i]=points[i].x;
			y[i]=points[i].y;
		}

		a = (cov(x,y) / var(x));
		b= (avg(y) - a*(avg (x)));
		return new Line(a,b);
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){return dev(p,linear_reg(points));}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){ return Math.abs(l.f(p.x) - p.y); }
}
