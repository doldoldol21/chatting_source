package client.javaFile;

import com.jfoenix.controls.JFXButton;

import client.css.CSSflag;
import client.fxml.FXMLflag;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Dialog {
	
	private Stage dialog;
	private AnchorPane ap;
	
	public Dialog(Stage clientStage) {
		try {
			dialog = new Stage(StageStyle.UNDECORATED);
			ap = FXMLLoader.load(FXMLflag.class.getResource("makeRoomDialog.fxml"));
			JFXButton btnNo = (JFXButton)ap.lookup("#jbtnNo");
			Scene scene = new Scene(ap);
			scene.getStylesheets().add(CSSflag.class.getResource("login.css").toExternalForm());
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.toFront();
			dialog.initOwner(clientStage);
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initStyle(StageStyle.TRANSPARENT);
			scene.setFill(Color.TRANSPARENT);
			
			scene.setOnKeyReleased( e -> {
				if(e.getCode() == KeyCode.ESCAPE) {
					dialog.close();
				}
			});
			
			btnNo.setOnAction( e -> dialog.close());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AnchorPane get() {
		return ap;
	}
	
	public void show() {
		Platform.runLater( () -> this.dialog.show());
	}
	public void close() {
		Platform.runLater( () -> this.dialog.close());
	}
}
