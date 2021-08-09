package viewModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import model.Model;

public class ViewModel  implements Observer {
	protected Model model;
	protected DoubleProperty PlaySpeed;
	protected StringProperty CSVPath,AttributesPath,PlugInPath,Timer,DataToSim,ListFeatures,Notifications;
	protected IntegerProperty TimeStep,ChosenIndex;
	protected HashMap<String,DoubleProperty> Features;
	public float[][] Data;
	public final Runnable play,pause,stop,fastForward,Backwards,Rewind,EndButton;
	int[] FeaturesIndexes;
	protected BooleanProperty OkToBind, SendToList,SliderChange;
	protected LineChart<String,Number> Right, Left;
	protected LineChart<Number,Number> Bottom;

	public ViewModel(Model model){  // Constructor.
		this.TimeStep=new SimpleIntegerProperty();
		this.ChosenIndex = new SimpleIntegerProperty();
		this.Timer = new SimpleStringProperty("00:00");
		this.CSVPath=new SimpleStringProperty();
		this.Notifications = new SimpleStringProperty("");
		this.AttributesPath=new SimpleStringProperty();
		this.PlugInPath = new SimpleStringProperty();
		this.PlaySpeed=new SimpleDoubleProperty();
		this.DataToSim = new SimpleStringProperty();
		this.ListFeatures = new SimpleStringProperty();
		this.OkToBind = new SimpleBooleanProperty(false);
		this.SendToList = new SimpleBooleanProperty(false);
		this.SliderChange = new SimpleBooleanProperty(false);
		this.model = model;
		this.Features = new HashMap<>();
		this.Data = null;
		this.FeaturesIndexes = new int[15];
		this.model.addObserver(this);
		this.Right = new LineChart<>(this.model.x,this.model.y);
		this.Left = new LineChart<>(this.model.x,this.model.y);
		this.Bottom=new LineChart<>(this.model.X,this.model.y);
		play = ()->{
			if(this.model.anomaly != null)
			    this.model.play();
			else
				Notifications.setValue("Please choose a correct CSV flight file first.");
		};
		pause = ()-> this.model.pause();
		stop = ()-> this.model.stop();
		fastForward = ()-> this.model.FastForwardOrBackwards();
		Backwards = ()-> this.model.FastForwardOrBackwards();
		Rewind = ()-> this.model.Rewind();
		EndButton = ()-> this.model.End();

		ChosenIndex.addListener((obj,oldValue,newValue)->this.model.setIndex(ChosenIndex.getValue()));
		TimeStep.addListener((obj,oldValue,newValue)->this.model.timeStep.setValue(newValue));
		SliderChange.addListener((obj,oldValue,newValue)->this.model.setChangeGraph(this.SliderChange.getValue()));
		PlaySpeed.addListener((obj,oldValue,newValue)->this.model.setPlaySpeed(PlaySpeed.doubleValue()));
		AttributesPath.addListener((obj,oldValue,newValue)-> SetFeatures());
		CSVPath.addListener((obj,oldValue,newValue)->this.runTS());
		PlugInPath.addListener((obj,oldValue,newValue)->this.RunPlugIn());
		DataToSim.addListener((obj,oldValue,newValue)->this.model.setDataToMySim(DataToSim.getValue()));
	}

	public DoubleProperty GetProperty(String name) {return Features.get(name);}

	public DoubleProperty getPlaySpeed(){return this.PlaySpeed;}

	public StringProperty getCSVPath(){return this.CSVPath;}

	public StringProperty getAttributesPath(){return this.AttributesPath;}

	public StringProperty timerProperty() {return Timer;}

	public IntegerProperty TimeStepProperty() {return TimeStep;}

	public StringProperty getListFeatures() {return ListFeatures;}

	public StringProperty plugInPathProperty() {return PlugInPath; }

	public BooleanProperty okToBindProperty() {return OkToBind;}

	public BooleanProperty sendToListProperty() {return SendToList;}

	public IntegerProperty chosenIndexProperty() {return ChosenIndex;}

	public BooleanProperty sliderChangeProperty(){ return SliderChange; }

	public LineChart<String, Number> getRight() {return Right;}

	public LineChart<String, Number>  getLeft() {return Left;}

	public LineChart<Number, Number> getBottom() { return Bottom; }

	public ObservableValue<? extends String> getLineStyle() {return Bottom.styleProperty();}

	public StringProperty notificationsProperty() {return Notifications;}

	public void SetFeatures (){ // Function to get the attributes from a txt file and set them into the application.
		String path;
		if((AttributesPath.getValue() != null)) {
			if (this.model.CheckAttributes(AttributesPath.getValue())) {
				path = AttributesPath.getValue();
				Notifications.setValue("Attributes file uploaded successfully.");
			} else {
				Notifications.setValue("Something occurred, Probably missing attributes, please reload Attribute file.");
				return;
			}
		}
		else // if an attribute file haven't been chosen.
			path = "Attributes.txt";
		try {
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(path)));
			int i = 0;
			while(scanner.hasNext()) {
				String line = scanner.nextLine();
				String[] LineData = line.split(" ");
				Features.put(LineData[0], new SimpleDoubleProperty()); // init features.
				FeaturesIndexes[i] = Integer.parseInt(LineData[1]);
				i++;
			}
			scanner.close();
			Features.get("aileron").setValue(100);  // place the joystick to the middle of the canvas as default.
			Features.get("elevator").setValue(100);  // place the joystick to the middle of the canvas as default.
			OkToBind.setValue(true);
		} catch (NumberFormatException | FileNotFoundException e) {e.printStackTrace();}
	}

	public void runTS(){
		if(model.CheckCSV(CSVPath.getValue())) {
			if (AttributesPath.getValue() == null)
				SetFeatures();
			this.model.setTs(CSVPath.getValue());
			this.Data = SwapData(this.model.anomaly.getData());
			Notifications.setValue("CSV file upload successfully.");
		}
		else
			Notifications.setValue("Missing Attributes in the CSV file");
	}

	public void setData(int CurrentRow) { // Set the data for the CSV into the FXML features.
		Features.get("aileron").set((this.model.ts.getData()[this.FeaturesIndexes[0]][CurrentRow]-1)*(-100));
		Features.get("elevator").set((this.model.ts.getData()[this.FeaturesIndexes[1]][CurrentRow]+1)*100);
		Features.get("rudder").set(this.model.ts.getData()[this.FeaturesIndexes[2]][CurrentRow]);
		Features.get("throttle").set(this.model.ts.getData()[this.FeaturesIndexes[3]][CurrentRow]);
		Features.get("roll-deg").set((this.model.ts.getData()[this.FeaturesIndexes[6]][CurrentRow]+40)/ 60);
		Features.get("pitch-deg").set((this.model.ts.getData()[this.FeaturesIndexes[7]][CurrentRow]+10)/ 30);
		Features.get("heading-deg").set(this.model.ts.getData()[this.FeaturesIndexes[8]][CurrentRow]/3.6);
		Features.get("side-slip-deg").set((this.model.ts.getData()[this.FeaturesIndexes[9]][CurrentRow]+30)/80);
		Features.get("airspeed-kt").set((this.model.ts.getData()[this.FeaturesIndexes[10]][CurrentRow]));
		Features.get("altimeter_indicated-altitude-ft").set((this.model.ts.getData()[this.FeaturesIndexes[11]][CurrentRow]+15)/7.2);
		DataToSim.setValue(Arrays.toString(Data[CurrentRow]));
	}

	public float[][] SwapData(float [][] data){  // Function to rotate our data table from out TS class.
		float[][] swap =new float[data[0].length][data.length];
		for (int i =0;i<data.length;i++)
			for(int j=0;j<data[0].length;j++)
				swap[j][i]=data[i][j];
		return swap;
	}

	public void RunPlugIn(){  // Init and run the chosen class.
		try {
			URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[] {new URL("file://"+PlugInPath.getValue())});
			Class<?> c =urlClassLoader.loadClass("test.HybridAlgo");
			Model.TimeSeriesAnomalyDetector ad=(Model.TimeSeriesAnomalyDetector)c.getConstructor().newInstance();
			this.model.setAd(ad);
			Notifications.setValue("Class loaded successfully.");
			urlClassLoader.close();
		} catch (ClassNotFoundException | IllegalAccessException | IOException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {Notifications.setValue("Error accord, please choose another class file");}
	}

	@Override
	public void update(Observable o, Object arg1) {
		Notifications.setValue(model.Notifications);
		if((!model.IfPlugInRunning)&&(model.anomaly != null)) {
			TimeStep.setValue(this.model.timeStep.getValue());
			if (TimeStep.getValue() % 10 == 0) {
				Timer.setValue(model.GetCurrentTime());
				if((TimeStep.getValue() % 50 == 0)&&(!Notifications.getValue().equals("")));
					model.setNotifications("");
			}
			Notifications.setValue(model.Notifications);
			setData(TimeStep.getValue());
			if(model.IsGraphsRunning)
				Platform.runLater(()->{
					this.Right.setData(this.model.RightGraph.getData());
					this.Left.setData(this.model.LeftGraph.getData());
					this.Bottom.setData(this.model.BottomGraph.getData());
					this.Bottom.setStyle(this.model.BottomGraph.getStyle());
				});
		}
		else this.ListFeatures.setValue(this.model.ListFeatures);
		SendToList.setValue(this.model.ListDone);
	}
}
