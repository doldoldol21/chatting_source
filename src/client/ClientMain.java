package client;

import client.css.CSSflag;
import client.fxml.FXMLflag;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application{

	public static Stage clientStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		clientStage = primaryStage;
		Parent parent = FXMLLoader.load(FXMLflag.class.getResource("client.fxml"));
		Scene scene = new Scene(parent);
		scene.getStylesheets().add(CSSflag.class.getResource("client.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setTitle("client");
		primaryStage.setMinHeight(500D);
		primaryStage.setMinWidth(425D);
		primaryStage.toFront();
	}
	public static void main(String[] args) {
		
		
		launch(args);
	}


}
