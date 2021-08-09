package algorithms;

import java.io.*;
import java.util.ArrayList;
import algorithms.Commands.Command;
import algorithms.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	
	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio); 
		commands=new ArrayList<>();
		// example: commands.add(c.new ExampleCommand());
		// implement
	}

	public void start() {
		while (true) {
			c.PrintMenu();
			float Input = this.dio.readVal();
			switch ((int) Input) {
				case 1 :
					Command TrainTs = c.new RunTrainFile();
					try {
						TrainTs.execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
					commands.add(TrainTs);
					Command TestTs = c.new RunTestFile();
					try {
						TestTs.execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
					commands.add(TestTs);
					break;

				case 2 :
					Command ThresHold = c.new UpdateThreshold();
					try {
						ThresHold.execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
					commands.add(ThresHold);
					break;

				case 3 :
					Command Anomaly = c.new DetectAnomaly();
					try {
						Anomaly.execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
					commands.add(Anomaly);
					break;

				case 4:
					Command AnomalyReport = c.new DisplayReport();
					try {
						AnomalyReport.execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
					commands.add(AnomalyReport);
					break;

				case 5 :
					Command Analyze = c.new AnomanlyAnalyze();
					try {
						Analyze.execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
					commands.add(Analyze);
					break;

				case 6 :
					return;
			}
		}
		}
	}

