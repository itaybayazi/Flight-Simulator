package view;

import java.net.URL;
import java.util.*;
import eu.hansolo.medusa.Gauge;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import viewModel.ViewModel;

public class MainWindowController implements Initializable {
	/* ********************* Control Panel ************************ */
	@FXML Gauge Altimeter;
	@FXML Gauge Heading;
	@FXML Gauge AirSpeed;
	@FXML ProgressBar Roll;
	@FXML ProgressBar Yaw;
	@FXML ProgressBar Pitch;
	/* ************************ JoyStick ************************* */
	@FXML Canvas joystick;
	@FXML Slider Throttle;
	@FXML Slider Rudder;
	/* *********************** Media Player ********************** */
	@FXML Slider VideoSlider;
	@FXML Label PlayButton;
	@FXML Label PauseButton;
	@FXML Label ForwardButton;
	@FXML Label BackwardButton;
	@FXML Label StopButton;
	@FXML Label RewindButton;
	@FXML Label EndButton;
	@FXML TextField MediaSpeed;
	@FXML Text Notifications;
	@FXML Button OpenCSV;
	@FXML Button OpenXML;
	@FXML Button OpenPlugIn;
	@FXML BorderPane borderPane;
	@FXML Text Timer;
	Runnable play,pause,stop,FastForward,Backwards,rewindButton,endButton;
	/* *********************** Graphs ************************* */
	@FXML LineChart<String,Number> LeftGraph;
	@FXML LineChart<String,Number> RightGraph;
	@FXML LineChart<Number,Number> BottomGraph;
	@FXML ListView<String> List;
	/* ******************************************************** */
	ViewModel viewModel;
	DoubleProperty PlaySpeed,Aileron,Elevators;
	StringProperty CSVPath,AttributesPath,PlugInPath,FeatureList;
	IntegerProperty TimeStep,Index;
	ObservableList<String> ListCollection;
	BooleanProperty StartBinding,FillList,SliderChanged;
	volatile boolean PaintGraphs = false , IsTsRunning = false;

	public MainWindowController() {  // Constructor
		PlaySpeed = new SimpleDoubleProperty();
		CSVPath=new SimpleStringProperty();
		AttributesPath=new SimpleStringProperty();
		PlugInPath=new SimpleStringProperty();
		Aileron = new SimpleDoubleProperty();
		Elevators = new SimpleDoubleProperty();
		TimeStep = new SimpleIntegerProperty();
		Index = new SimpleIntegerProperty();
		FeatureList = new SimpleStringProperty();
		StartBinding = new SimpleBooleanProperty(false);
		FillList = new SimpleBooleanProperty(false);
		SliderChanged = new SimpleBooleanProperty(false);
		ListCollection= FXCollections.observableArrayList();
	}

	public void setViewModel(ViewModel viewModel) {   // Set the ViewModel and all the bindings.
		this.viewModel = viewModel;
		this.viewModel.getPlaySpeed().bind(PlaySpeed);
		this.viewModel.getAttributesPath().bind(AttributesPath);
		this.viewModel.getCSVPath().bind(CSVPath);
		this.viewModel.plugInPathProperty().bind(PlugInPath);
		this.viewModel.chosenIndexProperty().bind(Index);
		VideoSlider.valueProperty().bindBidirectional(this.viewModel.TimeStepProperty());
		Timer.textProperty().bind(this.viewModel.timerProperty());
		StartBinding.bind(this.viewModel.okToBindProperty());
		Notifications.textProperty().bindBidirectional(this.viewModel.notificationsProperty());
		this.viewModel.sliderChangeProperty().bind(SliderChanged);
		FillList.bind(this.viewModel.sendToListProperty());
		TimeStep.bind(this.viewModel.TimeStepProperty());
		this.play = viewModel.play;
		this.pause = viewModel.pause;
		this.stop = viewModel.stop;
		this.FastForward = viewModel.fastForward;
		this.Backwards = viewModel.Backwards;
		this.rewindButton = viewModel.Rewind;
		this.endButton = viewModel.EndButton;
		RightGraph.dataProperty().bindBidirectional(this.viewModel.getRight().dataProperty());
		LeftGraph.dataProperty().bindBidirectional(this.viewModel.getLeft().dataProperty());
		BottomGraph.dataProperty().bindBidirectional(this.viewModel.getBottom().dataProperty());
		BottomGraph.styleProperty().bind(this.viewModel.getLineStyle());
		StartBinding.addListener((obj,oldValue,newValue)-> BindFeatures());
		FillList.addListener((obj,oldValue,newValue)-> this.List.setItems(this.ListCollection));
		Aileron.addListener((obj,oldValue,newValue)-> PaintJoyStick());
		Elevators.addListener((obj,oldValue,newValue)-> PaintJoyStick());
		FeatureList.addListener((obj,oldValue,newValue)-> this.ListCollection.add(FeatureList.getValue()));
		BottomGraph.getStylesheets().add(Objects.requireNonNull(getClass().getResource("BGraph.css")).toExternalForm());
	}

	public void BindFeatures() {  // Bind the Features
		Aileron.bind(viewModel.GetProperty("aileron"));
		AirSpeed.valueProperty().bind(viewModel.GetProperty("airspeed-kt"));
		Elevators.bind(viewModel.GetProperty("elevator"));
		Yaw.progressProperty().bind(viewModel.GetProperty("side-slip-deg"));
		Roll.progressProperty().bind(viewModel.GetProperty("roll-deg"));
		Pitch.progressProperty().bind(viewModel.GetProperty("pitch-deg"));
		Heading.valueProperty().bind(viewModel.GetProperty("heading-deg"));
		Altimeter.valueProperty().bind(viewModel.GetProperty("altimeter_indicated-altitude-ft"));
		Rudder.valueProperty().bind(viewModel.GetProperty("rudder"));
		Throttle.valueProperty().bind(viewModel.GetProperty("throttle"));
		FeatureList.bind(this.viewModel.getListFeatures());
	}
	/* ************************************  Media Player ************************************** */
	public void PlayClicked(){
		setPlaySpeed(1.0);
		MediaSpeed.setText(""+PlaySpeed.getValue());
		play.run();
		if(PaintGraphs)
			Changed();
	}

	public void PauseClicked(){
		PlaySpeed.setValue(0.0);
		MediaSpeed.setText(""+PlaySpeed.getValue());
		pause.run();
	}

	public void FastForwardClicked(){
		setPlaySpeed(PlaySpeed.getValue()+0.25);
		MediaSpeed.setText(""+PlaySpeed.getValue());
		FastForward.run();
		if(PaintGraphs)
			Changed();
	}

	public void BackWardClicked(){
		if(PlaySpeed.getValue()!=0) {
			setPlaySpeed(PlaySpeed.getValue() + (-0.25));
			MediaSpeed.setText("" + PlaySpeed.getValue());
		}
		Backwards.run();
		if(PaintGraphs)
			Changed();
	}

	public void setVideoSlider(int vs) {
		if ((vs > 1 )&&(vs < 2175))
			VideoSlider.setValue(vs);
		else if(vs >=2175)
			VideoSlider.setValue(2175);
		else
			VideoSlider.setValue(1);
	}

	public void StopButtonActivated(){
		PlaySpeed.setValue(0);
		RewindButtonActivated();
		MediaSpeed.setText(""+PlaySpeed.getValue());
		stop.run();
	}

	public void RewindButtonActivated(){
		setVideoSlider(1);
		rewindButton.run();
		if(PaintGraphs)
			Changed();
	}

	public void EndButtonActivated(){
		PlaySpeed.setValue(0);
		setVideoSlider(2175);
		MediaSpeed.setText(""+PlaySpeed.getValue());
		endButton.run();
		if(PaintGraphs)
			Changed();
	}

	public void LoadAttributesButtonClicked(){ // load attributes to the application.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Attribute File");
		Stage stage = (Stage) borderPane.getScene().getWindow();
		String path = null;
		try{path =fileChooser.showOpenDialog(stage).getAbsolutePath();}
		catch (Exception ignored){}
		if(path == null)
			Notifications.setText("No attribute file has been chosen");
		else if(!path.contains("txt"))
			Notifications.setText("Wrong file, Please choose an Attribute file.");
		else
			AttributesPath.setValue(path);
	}

	public void openCSVButtonClicked(){ // load CSV file to the application.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Flight CSV File");
		Stage stage = (Stage) borderPane.getScene().getWindow();
		String path = null;
		try{ path =fileChooser.showOpenDialog(stage).getAbsolutePath();}
		catch (Exception ignored){}
		if(path == null)
			Notifications.setText("No CSV file has been chosen");
		else if(!path.contains(".csv"))
			Notifications.setText("Wrong file, Please choose a CSV flight file.");
		else {
			CSVPath.setValue(path);
			IsTsRunning = true;
		}
	}

	public void openPlugInButtonClicked(){ // load a class to the application.
		DirectoryChooser DirectoryChooser = new DirectoryChooser();
		DirectoryChooser.setTitle("Open Class File");
		Stage stage = (Stage) borderPane.getScene().getWindow();
		String path = null;
		try{ path =DirectoryChooser.showDialog(stage).getAbsolutePath();}
		catch (Exception ignored){}
		if(path == null)
			Notifications.setText("No file has been chosen");
		else
			PlugInPath.setValue(path);
	}

	public void setPlaySpeed(double Speed) {
		if ((Speed > 0 )&&(Speed < 2)){
			PlaySpeed.setValue(Speed);
		}
		else if(Speed >=2){
			PlaySpeed.setValue(2);
		}
		else
			PlaySpeed.setValue(0.25);
	}

	/* ************************************  Graphs ************************************** */
	public void IfFeaturedSelected() {
		if(List.getEditingIndex() != -1) {
			this.Index.setValue(List.getEditingIndex());
			if(PaintGraphs)
				Changed();
			PaintGraphs = true;
		}
	}

	public void Changed (){
		SliderChanged.setValue(true);
		SliderChanged.setValue(false);
	}

	/* ************************************  JOYSTICK ***************************************/
	void PaintJoyStick(){
		GraphicsContext GC = joystick.getGraphicsContext2D();
		GC.clearRect(0,0,joystick.getWidth(), joystick.getHeight());
		GC.strokeOval(Aileron.getValue()-20 , Elevators.getValue()-20 , 50 , 50);
		GC.strokeOval(Aileron.getValue() - 32.5 , Elevators.getValue()-32.5 , 75 , 75);
		GC.fillOval(Aileron.getValue()-20,Elevators.getValue()-20,50,50);
	}
	/* ************************************  ****** ************************************** */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {}
}
