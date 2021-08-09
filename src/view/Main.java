package view;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Model;
import model.XMLReader;
import viewModel.ViewModel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxml = new FXMLLoader();
			BorderPane root = fxml.load(getClass().getResource("Main.fxml").openStream());
			File inputFile = new File("playback_small.xml");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader XMLreader = new XMLReader();
			saxParser.parse(inputFile, XMLreader);
			XMLreader.FinishWriting();
			Model model = new Model();
			ViewModel viewModel = new ViewModel(model);
			MainWindowController view = fxml.getController();
			view.setViewModel(viewModel);
			Scene scene = new Scene(root, 860, 525);
			primaryStage.setScene(scene);
			primaryStage.show();
			model.StartSim();
		} catch(Exception e) {e.printStackTrace();}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
