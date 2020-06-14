package client.javaFile;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import client.ClientMain;
import client.css.CSSflag;
import client.fxml.FXMLflag;
import data.Data;
import data.Room;
import data.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ClientController implements Initializable{
	
	public static String IP;
	public static int PORT;
	
	private Stage clientStage = ClientMain.clientStage;
	
	
	private User u;
	private Socket s;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	private String myRoom = "기본 방";
	private ListUnit lu = new ListUnit();
	
	private @FXML TextField textFieldSerach;
	private @FXML JFXListView<HBox> listViewUser;
	private @FXML JFXListView<HBox> listViewRoom;
	private @FXML VBox vBox;
	private @FXML JFXButton btnMakeRoom;
	private @FXML JFXTextField textField;
	private @FXML JFXTextField target;
	private @FXML ScrollPane scrollPane;
	private @FXML Label label;
	private @FXML HBox label_h;
	private @FXML JFXButton btnImg;
	
	private ObservableList<HBox> user_ol = FXCollections.observableArrayList();
	private ObservableList<HBox> room_ol = FXCollections.observableArrayList();
	
	private CreateMsg cm;
	private Dialog dialog;
	private Profile profile;
	private EtcFuntion ef;
	
	private Pattern p = Pattern.compile("/\\S* ");
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//셧다운훅은 더 공부하고 시도해보자 
//		Runtime.getRuntime().addShutdownHook(new ShutDownHook());
		
		cm = new CreateMsg(this.clientStage);
		Login login = new Login();
		login.show();
		dialog = new Dialog(this.clientStage);
		profile = new Profile(this.clientStage);
		ef = new EtcFuntion(this.clientStage);
		
		clientStage.setOnCloseRequest(e -> exit());
		target.setText("전체");
		
		textField.focusedProperty().addListener( (ob, oldB, newB) -> {
			if(newB == true && target.getText().trim().equals("")) {
				target.setText("전체");
			}
		});
		
		target.textProperty().addListener( (ob, oldS, newS) -> {
			if(newS.trim().equals("")) {
				target.setText("전체");
			}
		});
		
		textField.textProperty().addListener( (ob, oldS, newS) -> {
			
			Matcher m = p.matcher(newS);
			int i = 1;
			String str = "";
			if(m.find()) {
				
				while(newS.substring(i, newS.length()-1).indexOf("/") == 0) {
					if(newS.substring(i-1, newS.length()-1).indexOf(" ") != -1) {
						return;
					}else if(newS.substring(i, newS.length()-1).indexOf("/") == 0) {
						return;
					}
					i++;
				}
				str = newS.substring(i, newS.length()-1);
				target.setText(str);
				Platform.runLater( () -> textField.clear());
			}
		});
		
		
		listViewUser.setItems(user_ol);
		listViewRoom.setItems(room_ol);
		
		vBox.heightProperty().addListener(ob -> scrollPane.setVvalue(1D));
		vBox.widthProperty().addListener((ob, oldN, newN) -> {
//			label.setPadding(new Insets(0, newN.intValue()/2 - 50, 0, 0));
			label_h.setPrefWidth(newN.doubleValue());
		});
//		label.setPadding(new Insets(0, vBox.getWidth()/2- 50, 0, 0));
		label_h.setPrefWidth(vBox.getWidth());
		
		
		//채팅(Enter키)
		clientStage.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if(!textField.getText().equals("")) {
				String str = textField.getText();
				if(e.getCode() == KeyCode.ENTER) {
					Data data = new Data();
					data.setStatus("채팅");
					if(target.getText().equals(""))
						target.setText("전체");
					data.setTarget(target.getText());
					data.setRoomName(myRoom);
					data.setUser(this.u);
					data.setMessage(str);
					data.setFile(this.u.getImage());
					send(data);
					
					HBox hbox = null;
					if(!data.getTarget().equals("전체"))
						hbox = cm.makeMsg(this.u, "(" + data.getTarget() + ")" + str, true);
					else
						hbox = cm.makeMsg(this.u, str, true);
					
					hbox.setOnMouseClicked( e1 -> clipBoard(e1, str));
					this.vBox.getChildren().add(hbox);
					textField.clear();
				}
			}
		});
		
		btnMakeRoom.setOnAction( e -> makeRoomAction(e));
		btnImg.setOnAction(e -> fileSend(e));
		
		textFieldSerach.textProperty().addListener((ob, oldstr, newstr) -> {
			ObservableList<HBox> olcopy = FXCollections.observableArrayList();
			if(newstr.equals("") || newstr.equals(null)) {
				listViewUser.setItems(user_ol);	//listViewList랑 bind
				olcopy.clear();
			}else {
				for(HBox h : user_ol) {
					if(h.getId().indexOf(newstr) != -1) {
						if(olcopy.size() == 0) {
							olcopy.add(h);
						}else {
							for(int i=0; i<olcopy.size(); i++) {
								if(!olcopy.get(i).getId().equals(h.getId())) {
									olcopy.add(h);
								}
							}
						}
					}
					listViewUser.setItems(olcopy);	//listViewList랑 bind
				}
			}
		});
		
		
	}
	
	private void afterConnect(){
		try {
			inlistViewUser(this.u, myRoom);
			receive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void send(Data data) {
		try {
			if(!s.isClosed()) {
				oos.writeObject(data);
				oos.flush();
			}
		} catch (Exception e) {
			exit();
		}
		
	}
	
	private void receive() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Data data = (Data)ois.readObject();
						requestProcessing(data);
					} catch (Exception e) {
						try {
							oos.close();
							ois.close();
							s.close();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						Platform.exit();
						System.exit(0);
					}
				}
			}
		});
		t.start();
	}
	
	private void exit() {
		try {
			if(!s.isClosed()) {
			Data data = new Data();
			data.setStatus("유저나감");
			data.setUser(this.u);
			data.setRoomName(myRoom);
			send(data);
			oos.close();
			ois.close();
			s.close();
			Platform.exit();
			System.exit(0);
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void requestProcessing(Data data) {
		try {
		String status = data.getStatus();
//		System.out.println(status);
		if(status.equals("접속확인")) {
			Data d = new Data();
			d.setStatus("접속확인");
			send(d);
		}else if(status.equals("채팅")) {
			if(data.getFile() != null)
				data.getUser().setImage(data.getFile());
			
			if(!data.getUser().getNickname().equals(this.u.getNickname())) {
				if(data.getUser().getNickname().equals("서버")) {
					HBox hbox = cm.makeServerMsg(data.getUser(), data.getMessage());
					Platform.runLater( () -> this.vBox.getChildren().add(hbox));
					hbox.setOnMouseClicked( e1 -> clipBoard(e1, data.getMessage()));
					return;
				}
				else if(data.getTarget().equals(this.u.getNickname())) {
					HBox hbox = cm.makeMsg(data.getUser(), "(귓속말)" + data.getMessage(),false);
					hbox.setOnMouseClicked( e1 -> clipBoard(e1, data.getMessage()));
					Platform.runLater( () -> this.vBox.getChildren().add(hbox));
					return;
				}
				
				HBox hbox = cm.makeMsg(data.getUser(), data.getMessage(), false);
				hbox.setOnMouseClicked( e1 -> clipBoard(e1, data.getMessage()));
				Platform.runLater( () -> this.vBox.getChildren().add(hbox));
			}
		}else if(status.equals("뉴비")) {
			System.out.println(data.getUser().getNickname() + "뉴비임");
			if(!data.getUser().getNickname().equals(this.u.getNickname())) {
				inlistViewUser(data.getUser(), "기본 방");
				roomIn("기본 방");
				
			}
		}else if(status.equals("올드비")) {
			List<User> users = data.getUsers();
			for(User u : users) {
				inlistViewUser(u, "기본 방");
			}
		}else if(status.equals("유저나감")) {
			for(HBox h : user_ol) {
				if(h.getId().equals(data.getUser().getNickname())) {
					Platform.runLater( () -> user_ol.remove(h));
					break;
				}
			}
			roomOut(data.getRoomName());
			
		}else if(status.equals("올드룸")) {
			List<Room> list = new ArrayList<>();
			list = data.getRooms();
			for(Room r : list) {
				HBox hbox = lu.makeRoom(r.getName(), r.getCnt());
				Platform.runLater( () -> room_ol.add(hbox));
				hbox.setOnMouseReleased(e -> {
					if(e.getClickCount() ==2  && e.getButton() == MouseButton.PRIMARY) {
						if(!hbox.getId().equals(myRoom)) {
							Data d = new Data();
							d.setUser(this.u);
							d.setStatus("방입장");
							d.setRoomName(hbox.getId());
							send(d);
						}
					}
				});
			}
		}else if(status.equals("뉴룸")) {
			Room r = data.getRoom();
			HBox hbox = lu.makeRoom(r.getName(), r.getCnt());
			Platform.runLater( () -> room_ol.add(hbox));
			hbox.setOnMouseReleased(e -> {
				if(e.getClickCount() ==2  && e.getButton() == MouseButton.PRIMARY) {
					if(!hbox.getId().equals(myRoom)) {
						Data d = new Data();
						d.setUser(this.u);
						d.setStatus("방입장");
						d.setRoomName(hbox.getId());
						send(d);
					}
				}
			});
		}else if(status.equals("방입장")) {
			roomIn(data.getRoomName());
			roomOut(data.getMessage());
			for(HBox h : user_ol) {
				if(h.getId().equals(data.getUser().getNickname())) {
					Label label = (Label)h.lookup("#label");
					Platform.runLater( () -> label.setText(data.getUser().getNickname() + " - " + data.getRoomName()));
					if(data.getUser().equals(this.u)) {
						this.myRoom = data.getRoomName();
						break;
					}
				}
			}
		}else if(status.equals("이미지교체")) {
			System.out.println(data.getUser().getId() + "mmmmmmmmmmmmmmm" + data.getUser().getImage().length);
			if(data.getUser().getId().equals(this.u.getId())){
				this.u.setImage(data.getFile());
			}
			for(HBox h : user_ol) {
				if(h.getId().equals(data.getUser().getId())) {
					Circle c = (Circle)h.lookup("#circle");
					Image img = new Image(new ByteArrayInputStream(data.getFile()));
					c.setFill(new ImagePattern(img));
					break;
				}
			}
			for(int i=0; i<vBox.getChildren().size(); i++) {
				HBox h = (HBox) vBox.getChildren().get(i);
				if(h.getId().indexOf(data.getUser().getId()) != -1) {
					Circle c = (Circle)h.lookup("#circle");
					Image img = new Image(new ByteArrayInputStream(data.getFile()));
					c.setFill(new ImagePattern(img));
				}
			}
			
		}else if(status.equals("유저정보요청")) {
			if(data.getMessage().equals("프로필")) {
				profileShow(data.getUser(), data.getFile());
			}
		}else if(status.equals("이미지")) {
			if(!data.getUser().getId().equals(this.u.getId())) {
				Platform.runLater( () -> {
					HBox hbox = cm.makeImgMsg(data, false);
					vBox.getChildren().add(hbox);
				});
			}
		}else if(status.equals("파일")) {
			try {
				if(!data.getUser().getId().equals(this.u.getId())) {
					Platform.runLater( () -> {
						HBox hbox = cm.makeFileMsg(data, false);
						vBox.getChildren().add(hbox);
					});
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void clipBoard(MouseEvent e, String s) {
		if(e.getButton() == MouseButton.SECONDARY) {
			Clipboard c = Clipboard.getSystemClipboard();
			ClipboardContent cc = new ClipboardContent();
			cc.putString(s);
			c.setContent(cc);
			showLabel("복사되었습니다.");
		}
	}
	
	private void inlistViewUser(User u, String room) {
		try {
			HBox hbox = lu.make(u, room);
			if(hbox.getId().equals(this.u.getNickname())) {
				StackPane sp1 = (StackPane)hbox.getChildren().get(0);
				StackPane sp2 = (StackPane)sp1.getChildren().get(0);
				sp2.setStyle("-fx-background-color: white; -fx-background-radius: 7; -fx-border-color : #ff0080; -fx-border-radius: 7;");
			}
			Platform.runLater( () -> user_ol.add(hbox));
			hbox.setOnMouseReleased(e -> {
				if(e.getClickCount() ==2  && e.getButton() == MouseButton.PRIMARY) {
					Platform.runLater( () -> target.setText(hbox.getId()));
				}
			});
			Circle circle = (Circle)hbox.lookup("#circle");
			circle.setOnMouseReleased( e -> {
				if(e.getButton() == MouseButton.PRIMARY) {
					Data d = new Data();
					d.setStatus("유저정보요청");
					d.setUser(u);
					d.setMessage("프로필");
					send(d);
				}
			});
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void roomIn(String name) {
		for(HBox h : room_ol) {
			if(h.getId().equals(name)) {
				Label cnt = (Label)h.lookup("#label2");
				int i = Integer.parseInt(cnt.getText().substring(1, cnt.getText().length()-1)) + 1;
				Platform.runLater( () -> cnt.setText("(" + i + ")"));
				return;
			}
		}
	}
	private void roomOut(String name) {
		for(HBox h : room_ol) {
			if(h.getId().equals(name)) {
				Label cnt = (Label)h.lookup("#label2");
				int i = Integer.parseInt(cnt.getText().substring(1, cnt.getText().length()-1)) - 1;
				if(i == 0 && !h.getId().equals("기본 방")) {
					Platform.runLater(() -> room_ol.remove(h));
					return;
				}
				Platform.runLater( () -> cnt.setText("(" + i + ")"));
				return;
			}
		}
	}
	
	private void makeRoomAction(ActionEvent event) {
		AnchorPane ap = dialog.get();
		JFXTextField jtextField = (JFXTextField)ap.lookup("#jtextField");
		JFXButton jbtnOk = (JFXButton)ap.lookup("#jbtnOk");
		jbtnOk.setOnAction( e -> {
			dialogOkAction(e, jtextField.getText());
			Platform.runLater( () -> jtextField.clear());
		});
		ap.setOnKeyReleased( e -> {
			if(e.getCode() == KeyCode.ENTER) {
				dialogOkAction(new ActionEvent(), jtextField.getText());
				Platform.runLater( () -> jtextField.clear());
			}
		});
		dialog.show();
	}
	
	private void profileShow(User u, byte[] i) {
		
			AnchorPane a = profile.makeProfile(u, i);
			Circle circle = (Circle)a.lookup("#circle");
			circle.setOnMouseClicked( e -> {
				byte[] b = profile.imgAction(e, this.u);
				if(b != null && b != this.u.getImage()) {
					Image img = new Image(new ByteArrayInputStream(b));
					circle.setFill(new ImagePattern(img));
					this.u.setImage(b);
					Data data = new Data();
					data.setStatus("이미지교체");
					data.setUser(this.u);
					data.setFile(b);
					send(data);
				}
			});
			Platform.runLater( () -> {profile.show();});
			
			
		
		
	}
	
	private void dialogOkAction(ActionEvent e, String str) {
		Data data = new Data();
		data.setStatus("뉴룸");
		data.setMessage(str);
		data.setUser(this.u);
		send(data);
		dialog.close();
	}
	
	private void showLabel(String str) {
		Platform.runLater( () -> label.setText(str));
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
	
	private void fileSend(ActionEvent event) {
		Data data = null;
		data = ef.show(this.u, myRoom);
		if(data == null) {
			return;
		}else if(data.getStatus().equals("용량초과")) {
			showLabel("파일의 크기는 150MB 를 넘을 수 없습니다.");
			return;
		}
//		}else if(data.getFile().length > 150000000) {
//			System.out.println("여기");
//			showLabel("파일의 크기는 150MB 를 넘을 수 없습니다.");
//			return;
//		}
		
		if(target.getText().equals(""))
			data.setTarget("전체");
		else {
			data.setTarget(target.getText());
		}
		send(data);
		
		if(data.getStatus().equals("이미지")) {
			HBox hbox = cm.makeImgMsg(data, true);
			Platform.runLater( () -> vBox.getChildren().add(hbox));
		}else if(data.getStatus().equals("파일")) {
			HBox hbox = cm.makeFileMsg(data, true);
			Platform.runLater( () -> vBox.getChildren().add(hbox));
		}
	}
	
	private class Login {

		private Stage loginStage;
		
		
		//login.fxml
		private JFXTextField tfId;
		private JFXPasswordField pfPw;
		private JFXButton btnOk;
		private JFXButton btnNew;
		private Label label;
		private JFXTextField ip;
		private JFXTextField port;
		
		public Login() {}

		public void show() {
			try {
//				URL url = FXMLflag.class.getResource("login.fxml");
				Parent parent = FXMLLoader.load(FXMLflag.class.getResource("login.fxml"));
				loginStage = new Stage();
				Scene scene = new Scene(parent);
				
//				url = CSSflag.class.getResource("login.css");
				scene.getStylesheets().add(CSSflag.class.getResource("login.css").toExternalForm());
				
				loginStage.setScene(scene);
				loginStage.setTitle("로그인");
				loginStage.toFront();
				loginStage.setResizable(false);
				tfId = (JFXTextField)parent.lookup("#textFieldId");
				pfPw = (JFXPasswordField)parent.lookup("#passwordFieldPw");
				btnOk = (JFXButton)parent.lookup("#btnOk");
				btnNew = (JFXButton)parent.lookup("#btnNew");
				label = (Label)parent.lookup("#label");
				ip = (JFXTextField)parent.lookup("#ip");
				port = (JFXTextField)parent.lookup("#port");
				loginStage.show();
				loginStage.setOnCloseRequest(e -> System.exit(0));
				btnNew.setOnAction(e -> {
					Account account = new Account(loginStage);
					account.show();
				});
				
				btnOk.setOnAction(e -> btnOkAction(e));
				//엔터 키
				scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
					if(e.getCode() == KeyCode.ENTER) 
						btnOkAction(new ActionEvent());
				});
				
				ip.textProperty().addListener( (ob, oldS, newS) -> ClientController.IP = ip.getText());
				
				Pattern p = Pattern.compile("^[0-9]*$");
				port.textProperty().addListener( (ob, oldS, newS) -> {
					Matcher m = p.matcher(newS);
					if(!m.find())
						port.setText(oldS);
					else
						if(!newS.equals("")) {
							ClientController.PORT = Integer.parseInt(port.getText());
						}
				});
				
				
			} catch (Exception e) {
				System.out.println(FXMLflag.class.getResource("login.fxml"));
				e.printStackTrace();
			}
		}
		
		private void btnOkAction(ActionEvent event) {
			if(ip.getText().equals("") || port.getText().equals("")) {
				showLabel("아이피와 포트를 확인해주세요.");
				return;
			}
			String id = tfId.getText();
			String pw = pfPw.getText();
			if(!id.equals("") && pw.equals("")) {
				return;
			}
			User user = new User(id, pw, null, null);
			
			try {
				user.setIp(InetAddress.getLocalHost().getHostAddress());
				Data data = new Data();
				data.setStatus("로그인");
				data.setUser(user);
				System.out.println(user.getIp());
				s = new Socket(ClientController.IP, ClientController.PORT);
				oos = new ObjectOutputStream(s.getOutputStream());
				send(data);
				
				ois = new ObjectInputStream(s.getInputStream());
				data = (Data)ois.readObject();
				
				if(data.getStatus().equals("로그인실패")) {
					showLabel("로그인실패! 아이디와 비밀번호를 확인해주세요.");
					
				}else if(data.getStatus().equals("차단")) {
					showLabel("차단된 유저입니다. 사유 : " + data.getMessage());
					
				}else if(data.getStatus().equals("로그인성공")) {
					System.out.println("로그인 성공했숨다");
					u = data.getUser();
					loginStage.close();
					clientStage.show();
					afterConnect();
					return;
				}else if(data.getStatus().equals("이미접속")) {
					showLabel("이미 접속중인 유저입니다.");
				}
			}catch (Exception e) {
				showLabel("서버와 연결할 수 없습니다.");
//				e.printStackTrace();
			}
			
		}
		
		private void showLabel(String str) {
			label.setText(str);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						label.setVisible(true);
						Thread.sleep(5000);
						label.setVisible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}
	
//	private class ShutDownHook extends Thread{
//		@Override
//		public void run() {
//			Data data = new Data();
//			data.setStatus("유저나감");
//			data.setUser(u);
//			data.setRoom(myRoom);
//			send(data);
//			System.out.println("실행");
//		}
//	}
}
