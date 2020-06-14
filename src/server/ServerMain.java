package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.fxml.FXMLflag;

public class ServerMain extends Application{

	public static Stage serverStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		serverStage = primaryStage;
		Parent parent = FXMLLoader.load(FXMLflag.class.getResource("server.fxml"));
		Scene scene = new Scene(parent);
		primaryStage.setScene(scene);
		primaryStage.setTitle("server");
		primaryStage.toFront();
		primaryStage.show();
		
	}
	public static void main(String[] args) {
		launch(args);
	}

}
