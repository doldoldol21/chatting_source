package client.javaFile;

import java.io.ByteArrayInputStream;

import client.fxml.FXMLflag;
import data.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ListUnit {

	private User user;
	private String room;

	public ListUnit() {}
	
	public HBox make(User user, String room) {
		try {
			this.user = user;
			this.room = room;
			HBox hbox = FXMLLoader.load(FXMLflag.class.getResource("listUnit.fxml"));
			Circle circle = (Circle)hbox.lookup("#circle");
			Label label = (Label)hbox.lookup("#label");
			Image img = new Image(new ByteArrayInputStream(this.user.getImage()));
			
			label.setText(this.user.getNickname() + " - " + this.room);
			circle.setFill(new ImagePattern(img));
			hbox.setId(this.user.getNickname());
			
			return hbox;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public HBox makeRoom(String name, int i) {
		try {
			HBox hbox = FXMLLoader.load(FXMLflag.class.getResource("room.fxml"));
			Label label = (Label)hbox.lookup("#label");
			Label label2 = (Label)hbox.lookup("#label2");
			label.setText(name);
			label2.setText("(" + i + ")");
			hbox.setId(name);
			return hbox;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return null;
	}
	
	

}
