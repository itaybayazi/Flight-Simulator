package algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static algorithms.SimpleAnomalyDetector.*;

public class Commands {

	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);
	}

	// you may add default methods here

	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	// you may add other helper classes here

	
	
	// the shared state of all commands
	private class SharedState{
		// implement here whatever you need
		private TimeSeries TsTrain;
		private TimeSeries TsTest;
		private SimpleAnomalyDetector AnomalyDetector = new SimpleAnomalyDetector();
		private List<AnomalyReport> AnomalyReport = new ArrayList<>();
		private float FalsePositive;
		private float TruePositive;
		private float N;

		public TimeSeries getTs_train() {
			return TsTrain;
		}

		public void setTs_train(TimeSeries Ts_Train) {
			this.TsTrain = Ts_Train;
		}

		public TimeSeries GetTsTest() {
			return TsTest;
		}

		public void SetTsTest(TimeSeries Ts_Test) {
			this.TsTest = Ts_Test;
		}
	}
	
	private  SharedState sharedState=new SharedState();

	
	// Command abstract class
	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute() throws IOException;
	}
	
	// Command class for example:
	public class ExampleCommand extends Command{

		public ExampleCommand() {
			super("this is an example of command");
		}

		@Override
		public void execute() {
			dio.write(description);
		}		
	}

	public void PrintMenu() {
		dio.write(
				"Welcome to the Anomaly Detection Server.\n" +
						"Please choose an option:\n" +
						"1. upload a time series csv file\n" +
						"2. algorithm settings\n" +
						"3. detect anomalies\n" +
						"4. display results\n" +
						"5. upload anomalies and analyze results\n" +
						"6. exit\n"
		);
	}

	public class RunTrainFile extends Command {

		public RunTrainFile() {
			super("upload csv train files");
		}

		TimeSeries TrainTs;

		@Override
		public void execute() throws IOException {
			dio.write("Please upload your local train CSV file.\n");
			BufferedWriter pw = new BufferedWriter(new FileWriter("anomalyTrain.csv"));
			String s = dio.readText();
			while (true) {
				s = dio.readText();
				if (s.equals("done")) {
					break;
				}
				pw.write(s + "\n");
			}
			pw.close();
			TrainTs = new TimeSeries("anomalyTrain.csv");
			sharedState.setTs_train(TrainTs);
			dio.write("Upload complete.\n");
		}
	}

	public class RunTestFile extends Command {

		public RunTestFile() {
			super("upload csv test files");
		}
		TimeSeries TestTs;
		@Override
		public void execute() throws IOException {
			dio.write("Please upload your local test CSV file.\n");
			BufferedWriter pw = new BufferedWriter(new FileWriter("anomalyTest.csv"));
			String s;
			while (true) {
				s = dio.readText();
				if (s.equals("done")) {
					break;
				}
				sharedState.N++;
				pw.write(s + "\n");
			}
			pw.close();
			TestTs = new TimeSeries("anomalyTest.csv");
			sharedState.SetTsTest(TestTs);
			dio.write("Upload complete.\n");
		}
	}


	public class UpdateThreshold extends Command {
		public UpdateThreshold() {
			super("algorithm settings");
		}
		@Override
		public void execute() throws IOException {
			dio.write("The current correlation threshold is " + Threshold  + "\n");
			boolean flag = false;
			while (!flag) {
				dio.write("Type a new threshold\n");
				float newTH = dio.readVal();
				if ((newTH >= 0) && (newTH <= 1)) {
					Threshold = newTH;
					flag = true;
				} else {
					dio.write("Please choose a value between 0 and 1.\n");
				}
			}
		}
	}

	public class DetectAnomaly extends Command {
		public DetectAnomaly() {
			super("detect anomalies");
		}
		@Override
		public void execute() throws IOException {
			sharedState.AnomalyDetector.learnNormal(sharedState.getTs_train());
			sharedState.AnomalyReport = sharedState.AnomalyDetector.detect(sharedState.GetTsTest());
			dio.write("anomaly detection complete.\n");
		}
	}

	public class DisplayReport extends Command {
		public DisplayReport() {
			super("display results");
		}
		@Override
		public void execute() throws IOException {
			sharedState.AnomalyReport = sharedState.AnomalyDetector.detect(sharedState.GetTsTest());
			for (AnomalyReport ar : sharedState.AnomalyReport) {
				dio.write(ar.timeStep + "\t" + " " + ar.description + "\n");
			}
			dio.write("Done\n");
		}

	}


	float ThreeNumsafterDot(float num) {

		float x = num * 1000;
		int y = (int) x;
		x = y;

		return (x / 1000);

	}

	public class AnomanlyAnalyze extends Command {
		public AnomanlyAnalyze() {
			super("analyze results");
		}
		@Override
		public void execute() throws IOException {
			float Positive = 0;
			float Negative = sharedState.N - 1;
			sharedState.FalsePositive = 0;
			sharedState.TruePositive = 0;
			float B = 0;
			float count = 0;
			List<String> listT = new ArrayList<>();

			dio.write("Please upload your local anomalies file.\n" +
					"Upload complete.\n");
			while (true) {

				String s = dio.readText();
				if (s.equals("")) {
					s = dio.readText();
				}
				if (s.equals("done")) {
					break;
				}
				String[] arr = s.split(",");
				float Minus = Integer.parseInt(arr[1]) - Integer.parseInt(arr[0]) + 1;

				Negative = Negative - Minus;

				for (AnomalyReport ar : sharedState.AnomalyReport) {
					if ((ar.timeStep >= Integer.parseInt(arr[0])) && (ar.timeStep <= Integer.parseInt(arr[1]))) {

						if ((B + 1) == ar.timeStep) {
							B = ar.timeStep;
							continue;

						} else {
							if (listT.contains(ar.description)) {
								listT.remove(ar.description);
								sharedState.FalsePositive--;
							} else {
								sharedState.TruePositive++;
								B = ar.timeStep;
								listT.add(ar.description);
							}
						}
					} else {
						if (listT.contains(ar.description)) {
							continue;
						} else {
							if ((count + 1) == ar.timeStep) {
								count =  ar.timeStep;
								continue;
							}
							else
							{
								sharedState.FalsePositive++;
								listT.add(ar.description);
								count = ar.timeStep;
							}
						}
					}
				}
				Positive++;
			}
			float TruePos = sharedState.TruePositive / Positive;
			TruePos = ThreeNumsafterDot(TruePos);

			float FalsePos = sharedState.FalsePositive / Negative;
			FalsePos = ThreeNumsafterDot(FalsePos);

			dio.write("True Positive Rate: " + TruePos + "\n");
			dio.write("False Positive Rate: " + FalsePos + "\n");
		}
	}
}
