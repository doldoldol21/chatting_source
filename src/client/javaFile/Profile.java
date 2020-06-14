package client.javaFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.jfoenix.controls.JFXButton;

import client.css.CSSflag;
import client.fxml.FXMLflag;
import data.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Profile {
	
	private Stage profile;
	private Stage client;
	private AnchorPane anchor;
	private Circle circle;
	private Label labelNick;
	private Label labelId;
	
	public Profile(Stage client) {
		try {
			this.client = client;
			profile = new Stage(StageStyle.UNDECORATED);
			anchor = FXMLLoader.load(FXMLflag.class.getResource("profile.fxml"));
			Scene scene = new Scene(anchor);
			scene.getStylesheets().add(CSSflag.class.getResource("profile.css").toExternalForm());
			profile.setScene(scene);
			
			circle = (Circle)anchor.lookup("#circle");
			labelNick = (Label)anchor.lookup("#labelNick");
			labelId = (Label)anchor.lookup("#labelId");
			JFXButton btnOk = (JFXButton)anchor.lookup("#btnOk");
			
			
			scene.setOnKeyReleased( e -> {
				if(e.getCode() == KeyCode.ESCAPE) {
					if(profile.isShowing()) {
						profile.hide();
					}
				}
			});
			btnOk.setOnAction( e -> {
				profile.hide();
			});
			
			profile.initOwner(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AnchorPane makeProfile(User user, byte[] b) {
		Image img = new Image(new ByteArrayInputStream(b));
		labelId.setText(user.getId());
		labelNick.setText(user.getNickname());
		circle.setFill(new ImagePattern(img));
		anchor.setId(user.getNickname());
		
		return anchor;
	}
	
	public void show() {
		profile.setX(this.client.getX() - anchor.getPrefWidth());
		profile.setY(this.client.getY());
		
		this.client.xProperty().addListener( (ob, oldN, newN) -> {
			profile.setX(newN.doubleValue() - profile.getWidth());
		});
		this.client.yProperty().addListener( (ob, oldN, newN) -> {
			profile.setY(newN.doubleValue());
		});
		
		if(!profile.isShowing())
			profile.show();
	}
	
	public byte[] imgAction(MouseEvent event, User u) {
		if(event.getButton() == MouseButton.PRIMARY) {
			if(anchor.getId().equals(u.getId())) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setSelectedExtensionFilter(new ExtensionFilter("¿ÃπÃ¡ˆ", "*.jpg", "*.png", "*.gif"));
				try {
					File file = fileChooser.showOpenDialog(client);
					FileInputStream fis = new FileInputStream(file);
					byte[] b = new byte[fis.available()];
					fis.read(b);
					fileChooser.setInitialDirectory(file.getParentFile());
					fis.close();
					return b;
				}catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}
	
}
