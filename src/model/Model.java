package model;

import test.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.chart.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import static java.lang.Float.isNaN;

public class Model extends Observable{
	public double PlaySpeed;
	public IntegerProperty timeStep,Index;
	public Timer T;
	protected Thread SimThread;
	public String DataToMySim,ListFeatures,Notifications;
	public volatile boolean IfPlugInRunning = false,IsGraphsRunning = false , ListDone = false , ChangeGraph = false;
	public TimeSeries ts,anomaly;
	public LineChart<String,Number> LeftGraph,RightGraph;
	public LineChart<Number,Number>BottomGraph;
	protected XYChart.Series<String, Number> RSeries,LSeries;
	protected XYChart.Series<Number, Number>BSeries,GreySeries,GreenSeries,RedSeries,ZScoreSeries;
	public NumberAxis X,y;
	public CategoryAxis x;
	public int CorrelatedNum;
	public HybridAlgo Hybrid;
	float Pearson;

	public Model() { // Constructor
		T = null;
		PlaySpeed = 1.0;
		CorrelatedNum = 0;
		DataToMySim = null;
		Notifications = "";
		timeStep = new SimpleIntegerProperty(1);
		Index = new SimpleIntegerProperty();
		x = new CategoryAxis();
		y = new NumberAxis();
		X = new NumberAxis();
		Hybrid= new HybridAlgo();
		ts = new TimeSeries("reg_flight.csv");
		RightGraph = new LineChart<>(x,y);
		LeftGraph = new LineChart<>(x,y);
		BottomGraph=new LineChart<>(X,y);
		RSeries = new XYChart.Series<>();
		LSeries = new XYChart.Series<>();
		BSeries=new XYChart.Series<>();
		GreySeries=new XYChart.Series<>();
		RedSeries=new XYChart.Series<>();
		GreenSeries=new XYChart.Series<>();
		ZScoreSeries=new XYChart.Series<>();
		RightGraph.getData().add(RSeries);
		LeftGraph.getData().add(LSeries);
		BottomGraph.getData().addAll(BSeries,GreySeries,GreenSeries,RedSeries,ZScoreSeries);
	}

	public void setTs(String path){  // Set the TimeSeries by the chosen CSV file.
		this.anomaly = new TimeSeries(path);
		Hybrid.train = ts;
		Hybrid.test = anomaly;
		Hybrid.AD.learnNormal(this.ts);
		Hybrid.AD.detect(this.anomaly);
		SetFeaturesToList();
	}

	public boolean CheckCSV(String path) {// Function to check the CSV file.
		try {
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(path)));
			String Features = scanner.nextLine();
			for (int i = 0; i < ts.Features.length; i++)
				if (!Features.contains(ts.Features[i]))
					return false;
			scanner.close();
			return true;
		}catch (Exception e){return false;}
	}

	public boolean CheckAttributes(String path) { // Function to check the Attributes file.
		try {
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(path)));
			HashMap<String, Boolean> map = new HashMap<>();
			List<String> attribute = new ArrayList<>();
			int i =0;
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				String[] split = line.split(" ");
				attribute.add(split[0]);
				i++;
			}
			scanner.close();
			for ( i = 0; i < attribute.size(); i++) {
				if(attribute.get(i)!=null) {
					if (attribute.get(i).contains("aileron") || attribute.get(i).contains("elevator") || attribute.get(i).contains("rudder") || attribute.get(i).contains("throttle") || attribute.get(i).contains("latitude-deg") || attribute.get(i).contains("longitude-deg") || attribute.get(i).contains("roll-deg") || attribute.get(i).contains("pitch-deg") || attribute.get(i).contains("heading-deg") || attribute.get(i).contains("side-slip-deg") || attribute.get(i).contains("airspeed-kt") || attribute.get(i).contains("altimeter_indicated-altitude-ft"))
						map.put(attribute.get(i), true);
				}
			}
				if (map.containsKey("aileron") && map.containsKey("elevator") && map.containsKey("rudder") && map.containsKey("throttle") && map.containsKey("latitude-deg") && map.containsKey("longitude-deg") && map.containsKey("roll-deg") && map.containsKey("pitch-deg") && map.containsKey("heading-deg") && map.containsKey("side-slip-deg") && map.containsKey("airspeed-kt") && map.containsKey("altimeter_indicated-altitude-ft"))
					return true;
		} catch (FileNotFoundException e) {return false;}
		return false;
	}

		public void setBottomGraph(int TimeStep , int CorrelatedIndex) {  // Update the bottom according to the time step and send notifications if anomaly has been detected.
		Platform.runLater(() -> {
			if(Pearson < 0.95 && Pearson >= 0.5) { // Welzl.
				if(TimeStep % 20 == 0)
					BottomGraph.setStyle("-fx-background-color: transparent");
				if(TimeStep%20 == 0)
						GreenSeries.getData().add(new XYChart.Data<>(ts.Data[Index.getValue()][TimeStep], ts.Data[CorrelatedNum][TimeStep]));
				for (int i = 0; i < Hybrid.Report.size(); i++){
					if (Hybrid.Report.get(i).description.contains(ts.Features[Index.getValue()]) && (Hybrid.Report.get(i).timeStep == TimeStep)){
						BottomGraph.setStyle("-fx-background-color: red");
						RedSeries.getData().add(new XYChart.Data<>(anomaly.getData()[Index.getValue()][TimeStep],anomaly.getData()[CorrelatedNum][TimeStep]));
						setNotifications("Alert ! Anomaly detected - "+ Hybrid.Report.get(i).description + " On line "+ TimeStep);
					}}}
			else if ((CorrelatedIndex != -1) && (TimeStep % 20 == 0) && (Pearson>=0.95)) { // Linear regression.
				BottomGraph.setStyle("-fx-background-color: transparent");
				float Dev = anomaly.ReturnDev(new Point(TimeStep, this.anomaly.getData()[Index.getValue()][TimeStep]), Hybrid.AD.getNormalModel().get(CorrelatedIndex).lin_reg);
				GreenSeries.getData().add(new XYChart.Data<>(TimeStep, Dev));
					for (int i = 0; i < Hybrid.Report.size(); i++)
						if (Hybrid.Report.get(i).description.contains(Hybrid.AD.getNormalModel().get(CorrelatedIndex).feature1) && (Hybrid.Report.get(i).timeStep == TimeStep)){
							BottomGraph.setStyle("-fx-background-color: red");
							RedSeries.getData().add(new XYChart.Data<>( TimeStep, Dev));
							setNotifications("Alert ! Anomaly detected - "+ Hybrid.Report.get(i).description + " between lines " + (TimeStep - 20) + " - " + TimeStep);
				}
			}
			else if (Pearson<0.5 || isNaN(Pearson)) { // ZScore.
				ZScoreSeries.getData().add(new XYChart.Data<>(TimeStep, this.ts.getData()[Index.getValue()][TimeStep]));
				for (int i = 0; i < Hybrid.Report.size(); i++){
					if (Hybrid.Report.get(i).description.contains(ts.Features[Index.getValue()]) && (Hybrid.Report.get(i).timeStep == TimeStep)) {
						BottomGraph.setStyle("-fx-background-color: red");
						RedSeries.getData().add(new XYChart.Data<>(anomaly.getData()[Index.getValue()][TimeStep], anomaly.getData()[CorrelatedNum][TimeStep]));
						setNotifications("Alert ! Anomaly detected - " + Hybrid.Report.get(i).description + " On line " + TimeStep);
					}
				}
			}
		});
	}

	public void DisplayLineReg(int CorrelatedIndex){ // Init the bottom graph by the regular flight details
		if(CorrelatedIndex != -1 && (Pearson >= 0.95)) {  // Only if correlated to other feature.
			for (int i = 0; i < ts.Data[0].length; i++) {
				BSeries.getData().add(new XYChart.Data<>( i, Hybrid.AD.getNormalModel().get(CorrelatedIndex).lin_reg.f(i)));
				if(i%20==0) {
					float Dev = ts.ReturnDev(new Point(i, this.ts.Data[Index.getValue()][i]), Hybrid.AD.getNormalModel().get(CorrelatedIndex).lin_reg);
					GreySeries.getData().add(new XYChart.Data<>(i, Dev)); // point the current time step by the deviation.
				}
			}
		}
		else if(Pearson>= 0.5 && Pearson < 0.95) { // Welzl
			for (int i = 0; i < 360; i++)
				BSeries.getData().add(new XYChart.Data<>(Hybrid.LearnNormalCircle.center.x + (Hybrid.LearnNormalCircle.radius * Math.cos(Math.toRadians(i))), Hybrid.LearnNormalCircle.center.y + (Hybrid.LearnNormalCircle.radius * Math.sin(Math.toRadians(i)))));
			for (int i = 0; i < ts.Data[0].length; i++) {
				if(i%20 == 0)
					GreySeries.getData().add(new XYChart.Data<>(ts.Data[Index.getValue()][i], ts.Data[CorrelatedNum][i]));
			}
		}
	}

	public int FindOnCFList(int FeatureIndex){  // Function to locate the feature on the CF list(if excited).
		int index = -1;
		for (int i = 0; i < Hybrid.AD.getNormalModel().size(); i++)
			if(Hybrid.AD.getNormalModel().get(i).feature1.equals(ts.Features[FeatureIndex]))
				index=i;
		return index;
	}

	public void setRightGraph(int TimeStep , int FeatureNumber){Platform.runLater(()->  RSeries.getData().add(new XYChart.Data<>("" + TimeStep, this.anomaly.getData()[FeatureNumber][TimeStep])));}

	public void setLeftGraph(int TimeStep , int FeatureNumber){Platform.runLater(()-> LSeries.getData().add(new XYChart.Data<>("" + TimeStep, this.anomaly.getData()[FeatureNumber][TimeStep])));}

	public void setAd(TimeSeriesAnomalyDetector ad) {
		ad.learnNormal(this.ts);
		ad.detect(this.anomaly);
	}

	public interface TimeSeriesAnomalyDetector {
		void learnNormal(TimeSeries ts);
		List<AnomalyReport> detect(TimeSeries ts);
	}

	public void SetFeaturesToList(){  // Init the feature list by the CSV attributes.
		for (int i = 0; i < this.anomaly.Features.length; i++) {
			ListFeatures = (this.anomaly.Features[i]);
			IfPlugInRunning = true;
			setChanged();
			notifyObservers();
		}
		ListDone = true;
		setChanged();
		notifyObservers();
		IfPlugInRunning = false;
	}

	public void setIndex(int index) {
		this.Index.set(index);
		this.CorrelatedNum= this.ts.CheckCorrelation(Index.getValue());
		if(CorrelatedNum == -1) // if not correlated
			this.CorrelatedNum = Index.getValue();
		IsGraphsRunning = true;
		Pearson = ts.GetPearson(ts.Data[Index.getValue()],ts.Data[CorrelatedNum]);
		Hybrid.Hybrid(Pearson,Index.getValue(),CorrelatedNum);
		if(BSeries.getData().size() == 0) // if haven't init.
			DisplayLineReg(FindOnCFList(index));
	}

	public void setChangeGraph(boolean changeGraph) {  // Init changes if a different feature has been chosen, clear the graphs and add the new data.
		ChangeGraph = changeGraph;
			if(ChangeGraph && IsGraphsRunning){
				LSeries.getData().clear();
				RSeries.getData().clear();
				BSeries.getData().clear();
				ZScoreSeries.getData().clear();
				BottomGraph.getData().clear();
				BottomGraph.getData().addAll(BSeries,GreySeries,GreenSeries,RedSeries,ZScoreSeries);
				GreySeries.getData().clear();
				GreenSeries.getData().clear();
				RedSeries.getData().clear();
				ZScoreSeries.getData().clear();
				DisplayLineReg(FindOnCFList(Index.getValue()));
				for (int i = 0; i < timeStep.getValue(); i++) {
					setLeftGraph(timeStep.getValue(),Index.getValue());
					setRightGraph(timeStep.getValue(),this.CorrelatedNum);
					setBottomGraph(i,FindOnCFList(Index.getValue()));
				}
			}
	}

	public void play() { // Play has been clicked.
		if (T == null) {
			T = new Timer();
			T.scheduleAtFixedRate(new TimerTask(){
				@Override
				public void run() {
					if(timeStep.getValue()<2174) {
						timeStep.setValue(timeStep.getValue() + 1);
						if(IsGraphsRunning) {
							setLeftGraph(timeStep.getValue(),Index.getValue());
							setRightGraph(timeStep.getValue(),CorrelatedNum);
							setBottomGraph(timeStep.getValue(),FindOnCFList(Index.getValue()));
						}
						setChanged();
						notifyObservers();
					}
				}
			}, 0, 100);
		}
	}

	public void pause(){ // Pause has been clicked.
		if(T!=null) {T.cancel();}
		T= null;
	}

	public void stop(){if(T!=null){   // Stop has been clicked.
		T.cancel();}
		T= null;
		timeStep.setValue(1);
		this.setChanged();
		this.notifyObservers();
	}

	public void Rewind(){ // Rewind has been clicked.
		timeStep.setValue(1);
		this.setChanged();
		this.notifyObservers();
	}

	public void End(){ // End has been clicked.
		pause();
		timeStep.setValue(2174);
		this.setChanged();
		this.notifyObservers();
	}

	public void FastForwardOrBackwards() { // Fast forward or backwards has been clicked.
		if(T!=null) {
			T.cancel();
			T = new Timer();
			T.scheduleAtFixedRate(new TimerTask(){ @Override
				public void run() {
					if(timeStep.getValue()<2174) {
						timeStep.setValue(timeStep.getValue() + 1);
						if(IsGraphsRunning) {
							setLeftGraph(timeStep.getValue(),Index.getValue());
							setRightGraph(timeStep.getValue(),CorrelatedNum);
							setBottomGraph(timeStep.getValue(),FindOnCFList(Index.getValue()));
						}
						setChanged();
						notifyObservers();
					}
				}
			}, 0, (long) (100 / PlaySpeed));
		}
	}

	public void setPlaySpeed(double playSpeed) {PlaySpeed = playSpeed;}

	public void setDataToMySim(String dataToMySim) {DataToMySim = dataToMySim;}

	public void setNotifications(String notifications) {Notifications = notifications;}

	public void StartSim() {  // Function to connect and send data to FlightGear simulator.
		SimThread=new Thread(()-> {
		try {
			Socket fg = new Socket("localhost", 5400);
			PrintWriter out = new PrintWriter(fg.getOutputStream());
			while (timeStep.getValue()< 2175) {
				if((DataToMySim != null))
					out.println(DataToMySim);
					out.flush();
					if(PlaySpeed != 0)
						Thread.sleep((long) (100/ PlaySpeed));
					else
						Thread.sleep(100);
				}
			out.close();
			fg.close();
			} catch (IOException | InterruptedException e) { Notifications = "FlightGear isn't connected.";}
		});
		SimThread.start();
	}

	public String GetCurrentTime(){ // Function to init and run the video timer.
		int minutes = 0;
		int Seconds = (timeStep.getValue()/10);
		while(Seconds>60) {
			minutes++;
			Seconds = Seconds - 60;
		}
		if(Seconds<10)
			return ("0"+ minutes + ":" + "0" + Seconds);
		else
			return ("0"+ minutes + ":" + "" + Seconds);
	}
}
