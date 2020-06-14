package server.javaFile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.Data;
import data.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import server.ServerMain;

public class ServerController implements Initializable{
	private static int PORT;

	private Stage serverStage = ServerMain.serverStage;
	
	//server.fxml
	private @FXML ListView<User> clientListView;
	private @FXML ListView<Data> fileListView;
	private @FXML TextArea textArea;
	private @FXML TextField textField;
	private @FXML Button btnSS;
	private @FXML Button btnSend;
	private @FXML Button btnLog;
	private @FXML TableView<FileTableView> tv;
	private @FXML Button filesave;
	private @FXML Button filedel;
	private @FXML Button allUser;
	private @FXML Label ip;
	private @FXML TextField port;
	
	private ObservableList<User> c_ol = FXCollections.observableArrayList();
	private ObservableList<FileTableView> ftv_ol;
	
	private ExecutorService threadPool;
	private ServerSocket ss;
	private Socket s;
	private DAO dao;
	
	private List<Client> c_list = new ArrayList<>();
	private List<Room> r_list = new ArrayList<>();

	private User admin;
	private EtcFuntion ef;
	private LogExport le;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		dao = DAO.getIntans();
		ef = new EtcFuntion(serverStage);
		le = new LogExport();
		admin = new User("admin", "admin", "서버", null);
		
		btnSS.setOnAction( e -> btnSSAction(e));
		filesave.setOnAction( e -> filesaveAction(e));
		filedel.setOnAction( e -> filedelAction(e));
		allUser.setOnAction( e -> ef.userTable());
		btnLog.setOnAction( e -> {
			if(!textArea.getText().equals(""))
				le.Export(textArea);
		});
		
		serverStage.setOnCloseRequest(e -> {
			stopServer();
			System.exit(0);
		});
		clientListView.setItems(c_ol);
		
		serverStage.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if(e.getCode() == KeyCode.ENTER)
				btnSend.getOnAction();
		});
		
		this.ftv_ol = dao.fileSelectAll();
		this.tv.setItems(ftv_ol);
		
		TableColumn<FileTableView, ?> tv_date  = tv.getColumns().get(0);
		tv_date.setCellValueFactory(new PropertyValueFactory<>("date"));
		
		TableColumn<FileTableView, ?> tv_id  = tv.getColumns().get(1);
		tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
		
		TableColumn<FileTableView, ?> tv_foramt  = tv.getColumns().get(2);
		tv_foramt.setCellValueFactory(new PropertyValueFactory<>("format"));
		
		TableColumn<FileTableView, ?> tv_title  = tv.getColumns().get(3);
		tv_title.setCellValueFactory(new PropertyValueFactory<>("title"));
		
		TableColumn<FileTableView, ?> tv_size  = tv.getColumns().get(4);
		tv_size.setCellValueFactory(new PropertyValueFactory<>("size"));
		
		this.tv.setOnMouseClicked( e -> {
			if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
				FileTableView ftv = tv.getSelectionModel().getSelectedItem();
				if(ftv.getFormat().equals("이미지")) {
					byte[] b = dao.getFile(ftv);
					ef.imgShow(b);
				}
			}
		});
		
		try {
			ip.setText("IP : " + InetAddress.getLocalHost().getHostAddress() + " / PORT : ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Pattern p = Pattern.compile("^[0-9]*$");
		port.textProperty().addListener( (ob, oldS, newS) -> {
			Matcher m = p.matcher(newS);
			if(!m.find()) {
				port.setText(oldS);
			}else {
				if(port.getText().equals("")) {
					return;
				}
				PORT = Integer.parseInt(port.getText());
			}
		});
		
		
	}
	
	private void btnSSAction(ActionEvent event) {
		if(btnSS.getText().equals("서버시작")) {
			if(port.getText().equals("")) {
				textArea.appendText("포트번호를 입력하세요\n");
				return;
			}
			startServer();
			port.setDisable(true);
			knock();
			Platform.runLater( () -> btnSS.setText("서버중지"));
		}else {
			stopServer();
			port.setDisable(false);
			Platform.runLater( () -> btnSS.setText("서버시작"));
		}
	}
	
	private void filesaveAction(ActionEvent event) {
		FileTableView ftv = tv.getSelectionModel().getSelectedItem();
		byte[] b = dao.getFile(ftv);
		if(b != null)
			ef.fileSave(ftv, b);
		
	}
	private void filedelAction(ActionEvent event) {
		FileTableView ftv = tv.getSelectionModel().getSelectedItem();
		if(dao.deleteFile(ftv))
			ftv_ol.remove(ftv);
		
	}
	
	private void startServer() {
		try {
			threadPool = Executors.newFixedThreadPool(20);
			ss = new ServerSocket(PORT);
			
			textArea.appendText(ef.showTime() + "[서버 시작합니다]\n");
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					while(true) {
						try {
							s = ss.accept();
							Client client = new Client(s);
							if(!s.isClosed())
								client.start();
						} catch (Exception e) {
							try {
								if(!s.isClosed())
									s.close();
							} catch (Exception e1) {
								break;
							}
						}
					}
				}
			};
			threadPool.execute(runnable);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//서버에 들어왔던 유저가 아직 접속중인지 확인(에러로 그냥 꺼져버릴수도있으니)
	private void knock() {
		Data data = new Data();
		data.setStatus("접속확인");
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while(true) {
					if(c_ol.size() != 0) {
						Iterator<Client> it = c_list.iterator();
						while(it.hasNext()) {
							if(c_list.size() == 0) {
								continue;
							}
							Client c = it.next();
//							System.out.println("돌아가는중");
							if(c.s.isClosed()) {
								Data data2 = new Data();
								data2.setStatus("유저나감");
								data2.setUser(c.user);
								data2.setRoomName(c.myRoom);
								c.clientOut(data2);
								textArea.appendText(ef.showTime() + "[연결끊음]" + c.user.getId() + " 유저가 응답이 없어 연결을 끊습니다.\n");
							}
							c.send(data);
						}
					}else {
						if(!btnSend.isDisable())
							btnSend.setDisable(true);
					}
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						continue;
					}	
				}
			}
		};
		threadPool.execute(runnable);
	}
	
	private void stopServer() {
		le.Export(textArea);
		
		c_list.clear();
		r_list.clear();
		Platform.runLater( () -> c_ol.clear());
		try {
			for(Client c : c_list) {
				c.s.close();
			}
			if(s != null) {
				if(!s.isClosed())
					s.close();
			}
			if(ss != null) {
				if(!ss.isClosed())
					ss.close();
			}
			if(threadPool != null) {
				if(!threadPool.isShutdown())
					threadPool.shutdown();
			}

			textArea.appendText(ef.showTime() + "[서버 정지합니다]\n");
			
		} catch (Exception e) {
			try {
				ss.close();
				
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	private class Client extends Thread{

	    private ObjectOutputStream oos;
	    private ObjectInputStream ois;
		private Socket s;
		private User user;
		private String myRoom = "기본 방";
		
		public Client(Socket socket) {
			this.s = socket;
			loginCheck();
		}
		
		private void loginCheck() {
			try {
				ois = new ObjectInputStream(s.getInputStream());
				oos = new ObjectOutputStream(s.getOutputStream());
				Data data = (Data)ois.readObject();
				if(data.getStatus().equals("회원가입"))
					account(data);
				else if(data.getStatus().equals("로그인")) {
					for(User u : c_ol) {
						if(u.getId().equals(data.getUser().getId())) {
							data = new Data();
							data.setStatus("이미접속");
							send(data);
							return;
						}
					}
					if(login(data))
						afterConnect();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void account(Data d) {
			try {
				boolean account1 = dao.userDuplicateCheck1(d.getUser());
				boolean account2 = dao.userDuplicateCheck2(d.getUser());
				Data data = new Data();
				if(account1) {
					if(account2) {
						data.setStatus("회원가입성공");
					}else {
						data.setStatus("회원가입실패/닉네임");
					}
				}else {
					data.setStatus("회원가입실패/아이디");
				}
				send(data);
				close(this);
				this.interrupt();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		private boolean login(Data d) {
			try {
				this.user = dao.userCheck(d.getUser());
				
				Data data = new Data();
				if(this.user.getId() == null) {
					data.setStatus("로그인실패");
					send(data);
					close(this);
					this.interrupt();
					return false;
				}else if(this.user.getId().equals("차단")) {
					data.setStatus("차단");
					data.setMessage(user.getNickname());
					send(data);
					close(this);
					this.interrupt();
					return false;
				}else {
					data.setStatus("로그인성공");
					data.setUser(this.user);
					send(data);
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					Data data = (Data)ois.readObject();
					requestProcessing(data);
				}catch (Exception e) {
					e.printStackTrace();
					this.interrupt();
					return;
				}
			}
		}
		
		private void afterConnect() {
			if(btnSend.isDisable()) {
				btnSend.setDisable(false);
			}
			clientAddRoom(this, "기본 방");
			
			Platform.runLater( () -> c_ol.add(this.user));
			textArea.appendText(ef.showTime() + "[입장(" + this.user.getIp() + ")] " + this.user.getNickname() + " 님 입장합니다.\n");
			
			//보내기버튼
			btnSend.setOnAction( e -> {
				Data data = new Data();
				data.setStatus("채팅");
				data.setUser(admin);
				data.setTarget("전체");
				data.setMessage(textField.getText());
				broadCastAll(data, null);
				textField.clear();
			});
			//엔터키
			btnSend.setOnKeyReleased( e -> {
				if(e.getCode() == KeyCode.ENTER) {
					Data data = new Data();
					data.setStatus("채팅");
					data.setUser(admin);
					data.setTarget("전체");
					data.setMessage(textField.getText());
					broadCastAll(data, null);
					textField.clear();
				}
			});
			
			
			//뉴비 전송
			Data data = new Data();
			data.setStatus("뉴비");
			data.setUser(this.user);
			broadCastAll(data, null);
			
			//올드비 전송
			if(c_list.size() != 0) {
				List<User> users = new ArrayList<>();
				for(Client c : c_list) {
					users.add(c.user);
				}
				data = new Data();
				data.setStatus("올드비");
				data.setUsers(users);
				send(data);
			}
			
			//올드룸 전송
			if(r_list.size() != 0) {
				List<data.Room> rooms = new ArrayList<>();
				for(Room r : r_list) {
					rooms.add(new data.Room(r.name, r.c_list.size()));
				}
				data = new Data();
				data.setStatus("올드룸");
				data.setRooms(rooms);
				send(data);
			}
			
			//클라이언트 리스트에 자신은 마지막에 추가
			c_list.add(this);
		}
		
		private void requestProcessing(Data data) {
			String status = data.getStatus();
			User u = data.getUser();
			String msg = data.getMessage();
//			System.out.println(status);
			if(status.equals("접속확인")) {
				
			}else if(status.equals("채팅")) {
				broadCast(data);
				textArea.appendText(ef.showTime() + "[" + status + "|" + data.getTarget() + "|" + data.getRoomName() + "] " + u.getNickname() + " : "  + msg + "\n");
			}else if(status.equals("유저나감")){
				clientOut(data);
			}else if(status.equals("뉴룸")) {
				for(Room r : r_list) {
					if(r.name.equals(data.getMessage())) {
						data.setStatus("채팅");
						data.setUser(admin);
						data.setTarget(data.getUser().getNickname());
						data.setMessage("(방만들기실패)이미 존재하는 방의 이름입니다.");
						send(data);
						return;
					}
				}
				Room r = new Room(data.getMessage());
				r_list.add(r);
				data.setStatus("뉴룸");
				data.setUser(this.user);
				data.setRoom(new data.Room(r.name, 0));
				broadCastAll(data, null);
				textArea.appendText(ef.showTime() + "[" + status + "|" + r.name + "] " + u.getNickname() + " 유저가 방을 생성했습니다.\n");
				joinRoom(r.name, this);
			}else if(status.equals("방입장")) {
				System.out.println("도착");
				joinRoom(data.getRoomName(), this);
			}else if(status.equals("이미지교체")) {
				if(dao.changeImage(data.getUser(), data.getFile())) {
					System.out.println("성공");
					this.user.setImage(data.getFile());
					System.out.println(this.user.getImage().length);
					data.setUser(this.user);
					data.setFile(data.getUser().getImage());
					broadCastAll(data, null);
				}else {
					System.out.println("실패");
					data.setStatus("채팅");
					data.setMessage("사진용량이 커서 이미지 변경 실패했습니다.");
					data.setUser(admin);
					send(data);
				}
				
			}else if(status.equals("유저정보요청")) {
				for(Client c : c_list) {
					if(c.user.getId().equals(data.getUser().getId())) {
						data.setUser(c.user);
						data.setFile(c.user.getImage());
						this.send(data);
						break;
					}
				}
			}else if(status.equals("이미지") || status.equals("파일")) {
				dao.fileInsert(data);
				ftv_ol = dao.fileSelectAll();
				tv.setItems(ftv_ol);
				broadCast(data);
				textArea.appendText(ef.showTime() + "[" + status + "|" + data.getTarget() + "|" + data.getRoomName() + "] " + u.getNickname() + " : "  + msg + "\n");
			}
		}
		
		private void joinRoom(String name, Client c) {
			for(Room r : r_list) {
				if(r.name.equals(myRoom)) {
					r.c_list.remove(this);
					if(r.name != "기본 방" && r.c_list.size() == 0) {
						r_list.remove(r);
					}
					break;
				}
			}
			System.out.println(name);
			for(Room r : r_list) {
				if(r.name.equals(name)) {
					r.c_list.add(c);
					Data data = new Data();
					data.setStatus("방입장");
					data.setUser(c.user);
					data.setRoomName(name);
					data.setMessage(c.myRoom);
					textArea.appendText(ef.showTime() + "[방입장|" + this.myRoom + "] " + c.user.getNickname() + " 유저가 " + name + "으로 접속했습니다.\n");
					broadCastAll(data, null);
					
					data = new Data();
					data.setStatus("채팅");
					data.setTarget("전체");
					data.setUser(admin);
					data.setRoomName(name);
					data.setMessage(c.user.getNickname() + " 님이 " + name + " 으로 접속했습니다.");
					broadCast(data);
					break;
				}
			}
			if(c.equals(this)) {
				this.myRoom = name;
			}
		}
		
		private void send(Data d) {
			try {
				oos.writeObject(d);
				oos.flush();
			} catch (Exception e) {
				close(this);
//				e.printStackTrace();
			}
			
		}
		
		private void broadCast(Data data) {
			if(data.getTarget().equals("전체")) {
				String rName = data.getRoomName();
				for(Room r : r_list) {
					if(r.name.equals(rName)) {
						for(Client c : r.c_list) {
							c.send(data);
						}
					}
				}
			}else {
				for(Client c : c_list) {
					if(data.getTarget().equals(c.user.getNickname())) {
						c.send(data);
						return;
					}
				}
				//목록에 없으면
				data.setUser(admin);
				data.setMessage(data.getTarget() + "님에게 귓속말 실패했습니다.");
				send(data);
			}
		}
		
		private void broadCastAll(Data data, Client client) {
			//유저나갔을 때 자신은 들을수 없으므로 예외가 발생한다
			for(Client c : c_list) {
				if(c.equals(client))
					continue;
				else {
					c.send(data);
				}
			}
		}
		
		private void close(Client c) {
			try {
				c.oos.close();
				c.ois.close();
				if(!c.s.isClosed()) {
					c.s.close();
				}
				
			} catch (Exception e) {
				if(!c.s.isClosed()) {
					try {
						c.s.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}
		
		private void clientOut(Data data) {
			for(Room r : r_list) {
				if(r.name.equals(data.getRoomName())) {
					Iterator<Client> it = r.c_list.iterator();
					while(it.hasNext()) {
						Client c = it.next();
						if(c.user.getNickname().equals(data.getUser().getNickname())) {
							Data d = new Data();
							d.setStatus("유저나감");
							d.setUser(c.user);
							d.setRoomName(data.getRoomName());
							broadCastAll(data, c);
							//방에서 빼기
							r.c_list.remove(c);
							//클라이언트 리스트에서 빼기
							c_list.remove(c);
							close(c);
							c.interrupt();
							Platform.runLater( () -> c_ol.remove(c.user));
							textArea.appendText(ef.showTime() + "[퇴장(" + this.user.getIp() + ")] " + this.user.getNickname() + " 님 퇴장합니다.\n");
							break;
						}
					}
				}
			}
			
			
		}
		
		private void clientAddRoom(Client c, String str) {
			Iterator<Room> it = r_list.iterator();
			boolean flag = false;
			while(it.hasNext()) {
				Room r = it.next();
				if(r.name.equals(str)) {
					r.add_client(c);
					flag = true;
					break;
				}
			}
			if(flag == false) {
				r_list.add(new Room(str, c));
			}
		}
		
	}
	
	private class Room{
		private String name;
		private List<Client> c_list = new ArrayList<>();

		public Room(String name) {
			this.name = name;
		}
		public Room(String name, Client c) {
			super();
			this.name = name;
			this.c_list.add(c);
		}
		
		private void add_client(Client c) {
			this.c_list.add(c);
		}

		
	}
}
