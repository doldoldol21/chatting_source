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
		admin = new User("admin", "admin", "����", null);
		
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
				if(ftv.getFormat().equals("�̹���")) {
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
		if(btnSS.getText().equals("��������")) {
			if(port.getText().equals("")) {
				textArea.appendText("��Ʈ��ȣ�� �Է��ϼ���\n");
				return;
			}
			startServer();
			port.setDisable(true);
			knock();
			Platform.runLater( () -> btnSS.setText("��������"));
		}else {
			stopServer();
			port.setDisable(false);
			Platform.runLater( () -> btnSS.setText("��������"));
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
			
			textArea.appendText(ef.showTime() + "[���� �����մϴ�]\n");
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

	//������ ���Դ� ������ ���� ���������� Ȯ��(������ �׳� ������������������)
	private void knock() {
		Data data = new Data();
		data.setStatus("����Ȯ��");
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
//							System.out.println("���ư�����");
							if(c.s.isClosed()) {
								Data data2 = new Data();
								data2.setStatus("��������");
								data2.setUser(c.user);
								data2.setRoomName(c.myRoom);
								c.clientOut(data2);
								textArea.appendText(ef.showTime() + "[�������]" + c.user.getId() + " ������ ������ ���� ������ �����ϴ�.\n");
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

			textArea.appendText(ef.showTime() + "[���� �����մϴ�]\n");
			
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
		private String myRoom = "�⺻ ��";
		
		public Client(Socket socket) {
			this.s = socket;
			loginCheck();
		}
		
		private void loginCheck() {
			try {
				ois = new ObjectInputStream(s.getInputStream());
				oos = new ObjectOutputStream(s.getOutputStream());
				Data data = (Data)ois.readObject();
				if(data.getStatus().equals("ȸ������"))
					account(data);
				else if(data.getStatus().equals("�α���")) {
					for(User u : c_ol) {
						if(u.getId().equals(data.getUser().getId())) {
							data = new Data();
							data.setStatus("�̹�����");
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
						data.setStatus("ȸ�����Լ���");
					}else {
						data.setStatus("ȸ�����Խ���/�г���");
					}
				}else {
					data.setStatus("ȸ�����Խ���/���̵�");
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
					data.setStatus("�α��ν���");
					send(data);
					close(this);
					this.interrupt();
					return false;
				}else if(this.user.getId().equals("����")) {
					data.setStatus("����");
					data.setMessage(user.getNickname());
					send(data);
					close(this);
					this.interrupt();
					return false;
				}else {
					data.setStatus("�α��μ���");
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
			clientAddRoom(this, "�⺻ ��");
			
			Platform.runLater( () -> c_ol.add(this.user));
			textArea.appendText(ef.showTime() + "[����(" + this.user.getIp() + ")] " + this.user.getNickname() + " �� �����մϴ�.\n");
			
			//�������ư
			btnSend.setOnAction( e -> {
				Data data = new Data();
				data.setStatus("ä��");
				data.setUser(admin);
				data.setTarget("��ü");
				data.setMessage(textField.getText());
				broadCastAll(data, null);
				textField.clear();
			});
			//����Ű
			btnSend.setOnKeyReleased( e -> {
				if(e.getCode() == KeyCode.ENTER) {
					Data data = new Data();
					data.setStatus("ä��");
					data.setUser(admin);
					data.setTarget("��ü");
					data.setMessage(textField.getText());
					broadCastAll(data, null);
					textField.clear();
				}
			});
			
			
			//���� ����
			Data data = new Data();
			data.setStatus("����");
			data.setUser(this.user);
			broadCastAll(data, null);
			
			//�õ�� ����
			if(c_list.size() != 0) {
				List<User> users = new ArrayList<>();
				for(Client c : c_list) {
					users.add(c.user);
				}
				data = new Data();
				data.setStatus("�õ��");
				data.setUsers(users);
				send(data);
			}
			
			//�õ�� ����
			if(r_list.size() != 0) {
				List<data.Room> rooms = new ArrayList<>();
				for(Room r : r_list) {
					rooms.add(new data.Room(r.name, r.c_list.size()));
				}
				data = new Data();
				data.setStatus("�õ��");
				data.setRooms(rooms);
				send(data);
			}
			
			//Ŭ���̾�Ʈ ����Ʈ�� �ڽ��� �������� �߰�
			c_list.add(this);
		}
		
		private void requestProcessing(Data data) {
			String status = data.getStatus();
			User u = data.getUser();
			String msg = data.getMessage();
//			System.out.println(status);
			if(status.equals("����Ȯ��")) {
				
			}else if(status.equals("ä��")) {
				broadCast(data);
				textArea.appendText(ef.showTime() + "[" + status + "|" + data.getTarget() + "|" + data.getRoomName() + "] " + u.getNickname() + " : "  + msg + "\n");
			}else if(status.equals("��������")){
				clientOut(data);
			}else if(status.equals("����")) {
				for(Room r : r_list) {
					if(r.name.equals(data.getMessage())) {
						data.setStatus("ä��");
						data.setUser(admin);
						data.setTarget(data.getUser().getNickname());
						data.setMessage("(�游������)�̹� �����ϴ� ���� �̸��Դϴ�.");
						send(data);
						return;
					}
				}
				Room r = new Room(data.getMessage());
				r_list.add(r);
				data.setStatus("����");
				data.setUser(this.user);
				data.setRoom(new data.Room(r.name, 0));
				broadCastAll(data, null);
				textArea.appendText(ef.showTime() + "[" + status + "|" + r.name + "] " + u.getNickname() + " ������ ���� �����߽��ϴ�.\n");
				joinRoom(r.name, this);
			}else if(status.equals("������")) {
				System.out.println("����");
				joinRoom(data.getRoomName(), this);
			}else if(status.equals("�̹�����ü")) {
				if(dao.changeImage(data.getUser(), data.getFile())) {
					System.out.println("����");
					this.user.setImage(data.getFile());
					System.out.println(this.user.getImage().length);
					data.setUser(this.user);
					data.setFile(data.getUser().getImage());
					broadCastAll(data, null);
				}else {
					System.out.println("����");
					data.setStatus("ä��");
					data.setMessage("�����뷮�� Ŀ�� �̹��� ���� �����߽��ϴ�.");
					data.setUser(admin);
					send(data);
				}
				
			}else if(status.equals("����������û")) {
				for(Client c : c_list) {
					if(c.user.getId().equals(data.getUser().getId())) {
						data.setUser(c.user);
						data.setFile(c.user.getImage());
						this.send(data);
						break;
					}
				}
			}else if(status.equals("�̹���") || status.equals("����")) {
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
					if(r.name != "�⺻ ��" && r.c_list.size() == 0) {
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
					data.setStatus("������");
					data.setUser(c.user);
					data.setRoomName(name);
					data.setMessage(c.myRoom);
					textArea.appendText(ef.showTime() + "[������|" + this.myRoom + "] " + c.user.getNickname() + " ������ " + name + "���� �����߽��ϴ�.\n");
					broadCastAll(data, null);
					
					data = new Data();
					data.setStatus("ä��");
					data.setTarget("��ü");
					data.setUser(admin);
					data.setRoomName(name);
					data.setMessage(c.user.getNickname() + " ���� " + name + " ���� �����߽��ϴ�.");
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
			if(data.getTarget().equals("��ü")) {
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
				//��Ͽ� ������
				data.setUser(admin);
				data.setMessage(data.getTarget() + "�Կ��� �ӼӸ� �����߽��ϴ�.");
				send(data);
			}
		}
		
		private void broadCastAll(Data data, Client client) {
			//���������� �� �ڽ��� ������ �����Ƿ� ���ܰ� �߻��Ѵ�
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
							d.setStatus("��������");
							d.setUser(c.user);
							d.setRoomName(data.getRoomName());
							broadCastAll(data, c);
							//�濡�� ����
							r.c_list.remove(c);
							//Ŭ���̾�Ʈ ����Ʈ���� ����
							c_list.remove(c);
							close(c);
							c.interrupt();
							Platform.runLater( () -> c_ol.remove(c.user));
							textArea.appendText(ef.showTime() + "[����(" + this.user.getIp() + ")] " + this.user.getNickname() + " �� �����մϴ�.\n");
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
