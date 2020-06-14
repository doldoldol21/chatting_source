package client.javaFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import data.Data;
import data.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Account {

	private Stage loginStage;
	private Stage accountStage;
	private File file;
	
	//account.fxml
	private  JFXTextField tfId;
	private  JFXPasswordField pfPw;
	private  JFXTextField tfNick;
	private  JFXButton btnOk;
	private  JFXButton btnCancel;
	private  Circle circle;
	private  AnchorPane anchorPane;
	private  Label label;
	
	public Account(Stage loginStage) {
		this.loginStage = loginStage;
	}
	
	public void show() {
		try {
			URL url = getClass().getResource("../fxml/account.fxml");
			Parent parent = FXMLLoader.load(url);
			accountStage = new Stage();
			Scene scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../css/login.css").toExternalForm());
			accountStage.setScene(scene);
			accountStage.setTitle("ȸ������");
			accountStage.toFront();
			accountStage.setResizable(false);
			tfId = (JFXTextField)parent.lookup("#tfId");
			tfNick = (JFXTextField)parent.lookup("#tfNick");
			pfPw = (JFXPasswordField)parent.lookup("#pfPw");
			btnCancel = (JFXButton)parent.lookup("#btnCancel");
			btnOk = (JFXButton)parent.lookup("#btnOk");
			circle = (Circle)parent.lookup("#circle");
			label = (Label)parent.lookup("#label");
			anchorPane = (AnchorPane)parent.lookup("#anchorPane");
			
			loginStage.hide();
			accountStage.show();
			btnCancel.setOnAction(e -> close());
			accountStage.setOnCloseRequest(e -> close());
			anchorPane.setOnMouseClicked(e -> setImage(e));
			btnOk.setOnAction(e -> btnOkAction(e));
			
			tfId.textProperty().addListener( (ob, olds, news) -> {
				if(tfId.getText().equals("") || news.length() < olds.length())
					label.setVisible(false);
				if(news.length() > 15) {
					String tmp = news.substring(0, 15);
					tfId.setText(tmp);
				}
			});
			tfNick.textProperty().addListener( (ob, olds, news) -> {
				if(tfNick.getText().equals("") || news.length() < olds.length())
					label.setVisible(false);
				if(news.length() > 7) {
					String tmp = news.substring(0, 7);
					tfNick.setText(tmp);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void btnOkAction(ActionEvent event) {
		if(!tfId.getText().equals("") && !pfPw.getText().equals("") && !tfNick.getText().equals("") ) {
			String id = tfId.getText();
			if(id.indexOf(" ") != -1 || id.indexOf("/") != -1 ||
					id.indexOf("$") != -1 || id.indexOf("@") != -1) {
				showLabel("���̵𿡴� ����, Ư������(/,$,@)�� ����� �� �����ϴ�.");
				return;
			}
			String nick = tfNick.getText();
			if(nick.indexOf(" ") != -1 || nick.indexOf("/") != -1 ||
					nick.indexOf("$") != -1 || nick.indexOf("@") != -1) {
				showLabel("�г��ӿ��� ����, Ư������(/,$,@)�� ����� �� �����ϴ�.");
				return;
			}
			// �ѱ��� ���Ե� ���ڿ�
			if(tfId.getText().matches(".*[��-����-�Ӱ�-�R]+.*")) {
				showLabel("���̵� �ѱ��� ������ �� �����ϴ�.");
				return;
			}
			serverConn();
		}
	}
	
	private void serverConn() {
		try {
			System.out.println(ClientController.IP);
			System.out.println(ClientController.PORT);
			Socket s = new Socket(ClientController.IP, ClientController.PORT);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			Data data = new Data();
			
			if(file == null) {
				file = new File(getClass().getResource("../image/img1.png").toURI());
			}
	        FileInputStream fin = new FileInputStream(file);
	        byte b[] = new byte[fin.available()];
	        fin.read(b);
             
			User user = new User(tfId.getText(), pfPw.getText(), tfNick.getText(), b);
			data.setStatus("ȸ������");
			data.setUser(user);
			oos.writeObject(data);
			oos.flush();
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			data = (Data)ois.readObject();
			
			if(data.getStatus().equals("ȸ�����Լ���"))
				close();
			else if(data.getStatus().equals("ȸ�����Խ���/���̵�"))
				showLabel("�̹� ���Ե� ���̵��Դϴ� Ȯ�����ּ���.");
			else if(data.getStatus().equals("ȸ�����Խ���/�г���"))
				showLabel("�̹� �����ϴ� �г����Դϴ� Ȯ�����ּ���.");
			
			ois.close();
			fin.close();
			oos.close();
			s.close();
			
			
		} catch (Exception e) {
			showLabel("������ ������ �� �����ϴ�.");
			e.printStackTrace();
		}
	}
	
	private void showLabel(String str) {
		label.setText(str);
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					label.setVisible(true);
					Thread.sleep(3000);
					label.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		t.start();
	}
	
	public void setImage(MouseEvent event) {
		//�̹������� ������ �̹������ϼ���
		FileChooser fileChooser = new FileChooser();
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("�̹���", "*.jpg", "*.png", "*.gif"));
		try {
			file = fileChooser.showOpenDialog(this.anchorPane.getScene().getWindow());
			if(file != null) {
				fileChooser.setInitialDirectory(file.getParentFile());
				Image image = new Image(file.toURI().toString(), false);
				//�� �ȿ� �̹��� �ֱ�
				circle.setFill(new ImagePattern(image));
				circle.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
			}
		}catch (Exception e) {
			System.out.println("�׳� ����");
		}
	}
	
	private void close() {
		accountStage.close();
		loginStage.show();
	}
	
	
	
}
