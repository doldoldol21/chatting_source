package client.javaFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.jfoenix.controls.JFXButton;

import client.fxml.FXMLflag;
import data.Data;
import data.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class CreateMsg {

	private Stage client;
	private Circle circle;
	private Text text;
	private Label label;
	private Label title;
	private Label size;
	private ImageView iv;
	
	private FileChooser fc;
	
	EtcFuntion ef = new EtcFuntion();
	
	public CreateMsg(Stage c) {
		this.client = c ;
		fc = new FileChooser();
		
	}
	
	//자신이면 check = ture else check = false;
	public HBox makeMsg(User user, String msg, boolean check) {
		try {
			HBox hbox = null;
			if(check) {
				hbox = FXMLLoader.load(FXMLflag.class.getResource("msg2.fxml"));
			}else {
				hbox = FXMLLoader.load(FXMLflag.class.getResource("msg.fxml"));
			}
			
			circle = (Circle)hbox.lookup("#circle");
			text = (Text)hbox.lookup("#text");
			label = (Label)hbox.lookup("#label");
			
			if(user.getImage() != null) {
				Image img = new Image(new ByteArrayInputStream(user.getImage()));
				circle.setFill(new ImagePattern(img));
			}
			label.setText(user.getNickname());
			text.setText(msg);
			hbox.setId(user.getId());
			
			return hbox;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public HBox makeServerMsg(User user, String msg) {
		try {
			HBox hbox = FXMLLoader.load(FXMLflag.class.getResource("serverMsg.fxml"));
			text = (Text)hbox.lookup("#text");
			text.setText(msg);
			hbox.setId(user.getId());
			return hbox;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//자신이면 check = ture else check = false;
	public HBox makeImgMsg(Data data, boolean check) {
		try {
			HBox hbox = null;
			System.out.println(check);
			if(check) {
				hbox = FXMLLoader.load(FXMLflag.class.getResource("imgMsg2.fxml"));
			}else {
				hbox = FXMLLoader.load(FXMLflag.class.getResource("imgMsg.fxml"));
			}
			circle = (Circle)hbox.lookup("#circle");
			label = (Label)hbox.lookup("#label");
			iv = (ImageView)hbox.lookup("#iv");
			JFXButton download = (JFXButton)hbox.lookup("#download");
			download.setStyle("-fx-background-image: url('client/icon/download.png'); -fx-background-size: 25px 25px;");
			if(data.getUser().getImage() != null) {
				Image img = new Image(new ByteArrayInputStream(data.getUser().getImage()));
				circle.setFill(new ImagePattern(img));
			}
			
			Image img = null;
			if(data.getFile() != null) {
				img = new Image(new ByteArrayInputStream(data.getFile()));
				iv.setImage(img);
			}
			label.setText(data.getUser().getNickname());
			hbox.setId(data.getUser().getId() + "/" + data.getMessage());
			
			Stage stage = new Stage();
			ImageView imageView = new ImageView(img);
			imageView.setPreserveRatio(false);
			stage.setWidth(800);
			stage.setHeight(800);
			stage.setTitle(data.getMessage());
			imageView.fitWidthProperty().bind(stage.widthProperty());
			imageView.fitHeightProperty().bind(stage.heightProperty());
			HBox h = new HBox(imageView);
			Scene scene = new Scene(h);
			stage.setScene(scene);
			
			scene.setOnKeyReleased(e -> {
				if(e.getCode() == KeyCode.ESCAPE) {
					if(stage.isShowing())
						stage.close();
				}
			});
			iv.setOnMouseReleased( e -> {
				if(e.getButton() == MouseButton.PRIMARY) {
					if(!stage.isShowing())
						stage.show();
				}
			});
			
			download.setOnAction( e -> {
				fc.setInitialFileName(data.getMessage());
				fc.getExtensionFilters().add((new ExtensionFilter("이미지(*.jpg,*.gif,*.png)", "*.jpg", "*.gif", "*.png")));
				File file = fc.showSaveDialog(this.client);
				if(file != null) {
					saveFile(file, data.getFile());
					fc.setInitialDirectory(file.getParentFile());
				}
			});
			
			return hbox;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void saveFile(File file, byte[] b) {
		try {
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			
			if(!file.exists()) 
				file.createNewFile();
			
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(b);
			bos.flush();
			bos.close();
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public HBox makeFileMsg(Data data, boolean check) {
		try {
			HBox hbox = null;
			if(check) {
				hbox = FXMLLoader.load(FXMLflag.class.getResource("fileMsg2.fxml"));
			}else {
				hbox = FXMLLoader.load(FXMLflag.class.getResource("fileMsg.fxml"));
			}
			circle = (Circle)hbox.lookup("#circle");
			label = (Label)hbox.lookup("#label");
			title = (Label)hbox.lookup("#title");
			size = (Label)hbox.lookup("#size");
			JFXButton download = (JFXButton)hbox.lookup("#download");
			download.setStyle("-fx-background-image: url('client/icon/download.png'); -fx-background-size: 25px 25px;");
			
			if(data.getUser().getImage() != null) {
				Image img = new Image(new ByteArrayInputStream(data.getUser().getImage()));
				circle.setFill(new ImagePattern(img));
			}
			label.setText(data.getUser().getNickname());
			title.setText(data.getMessage());
			String str = ef.byteCalculation(data.getFile().length);
			size.setText("(" + str + ")");
			hbox.setId(data.getUser().getId() + "/" + data.getMessage());
			
			
			download.setOnAction( e -> {
				fc.setInitialFileName(data.getMessage());
				fc.getExtensionFilters().add((new ExtensionFilter("모든파일", "*.all")));
				File file = fc.showSaveDialog(this.client);
				if(file != null) {
					saveFile(file, data.getFile());
					fc.setInitialDirectory(file.getParentFile());
				}
			});
			
			return hbox;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
