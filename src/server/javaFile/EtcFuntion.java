package server.javaFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import server.fxml.FXMLflag;

public class EtcFuntion {

	private Stage server;

	private Date date;
//	private SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
//	private SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일 HH시mm분ss초");
	private SimpleDateFormat format = new SimpleDateFormat ( "[yyyy/MM/dd-HH:mm:ss]");
			
	private FileChooser fc = new FileChooser();
	private DAO dao;
	
	private Stage userTableStage;
	private TableView<UserTableView> tv;
	private ObservableList<UserTableView> tv_ol;

	public EtcFuntion() {
	}

	public EtcFuntion(Stage server) {
		this.server = server;
	}
	 

	public String showTime() {
		date = new Date();
		String time = format.format(date);
		return time;
	}
	
	public String calculation(int bytes) {
		String retFormat = "0";
		Double size = (double)bytes;
		
		String[] s = { "bytes", "KB", "MB", "GB", "TB", "PB" };
		
		if(bytes != 0) {
			int idx = (int)Math.floor(Math.log(size) / Math.log(1024));
//			System.out.println("Math.floor : " + Math.floor(Math.log(size) / Math.log(1024)));
//			System.out.println("idx : " + idx);
			
			DecimalFormat df = new DecimalFormat("#,###.##");
			
			double ret = ((size / Math.pow(1024, Math.floor(idx))));
//			System.out.println("ret : " + ret);
            retFormat = df.format(ret) + " " + s[idx];
		}else {
            retFormat += " " + s[0];
       }
		
		return retFormat;
	}

	public void fileSave(FileTableView ftv, byte[] b) {
		fc.setInitialFileName(ftv.getTitle());
		fc.getExtensionFilters().clear();
		fc.getExtensionFilters().add((new ExtensionFilter("모든파일", "*.all")));
		
		
		File file = fc.showSaveDialog(this.server);
		if(file != null) {
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
			fc.setInitialDirectory(file.getParentFile());
		}
	}
	
	public void imgShow(byte[] b) {
		Stage fileimgStage = new Stage();
		ImageView iv = new ImageView();
		AnchorPane pane = new AnchorPane();
		Scene scene = new Scene(pane);
		
		iv.setPreserveRatio(false);
		fileimgStage.setWidth(800);
		fileimgStage.setHeight(800);
		iv.fitWidthProperty().bind(fileimgStage.widthProperty());
		iv.fitHeightProperty().bind(fileimgStage.heightProperty());
		
		pane.getChildren().add(iv);
		fileimgStage.setScene(scene);
		iv.setImage(new Image(new ByteArrayInputStream(b)));
		fileimgStage.show();
	}
	
	@SuppressWarnings("unchecked")
	public void userTable() {
		
		if(userTableStage == null) {
			try {
				dao = DAO.getIntans();
				userTableStage = new Stage();
				VBox v = FXMLLoader.load(FXMLflag.class.getResource("userTable.fxml"));
				tv = (TableView<UserTableView>)v.lookup("#tv");
				Button btnBan = (Button)v.lookup("#btnBan");
				Button btnDel = (Button)v.lookup("#btnDel");
				Button btnImg = (Button)v.lookup("#btnImg");
				Button btnEp = (Button)v.lookup("#btnEp");
				
				Scene scene = new Scene(v);
				userTableStage.setScene(scene);

				TableColumn<UserTableView, ?> id = tv.getColumns().get(0);
				id.setCellValueFactory(new PropertyValueFactory<>("id"));
				TableColumn<UserTableView, ?> pw = tv.getColumns().get(1);
				pw.setCellValueFactory(new PropertyValueFactory<>("pw"));
				TableColumn<UserTableView, ?> nickname = tv.getColumns().get(2);
				nickname.setCellValueFactory(new PropertyValueFactory<>("nickname"));
				TableColumn<UserTableView, ?> join_date = tv.getColumns().get(3);
				join_date.setCellValueFactory(new PropertyValueFactory<>("join_date"));
				TableColumn<UserTableView, ?> last_date = tv.getColumns().get(4);
				last_date.setCellValueFactory(new PropertyValueFactory<>("last_date"));
				TableColumn<UserTableView, ?> ip = tv.getColumns().get(5);
				ip.setCellValueFactory(new PropertyValueFactory<>("ip"));
				TableColumn<UserTableView, ?> escape_date = tv.getColumns().get(6);
				escape_date.setCellValueFactory(new PropertyValueFactory<>("escape_date"));
				TableColumn<UserTableView, ?> ban = tv.getColumns().get(7);
				ban.setCellValueFactory(new PropertyValueFactory<>("ban"));
				
				btnDel.setOnAction( e -> {
					UserTableView utv = tv.getSelectionModel().getSelectedItem();
					dao.userDelate(utv);
					tv_ol.remove(utv);
				});
				
				btnBan.setOnAction( e -> {
					TextInputDialog d = new TextInputDialog();
					d.setTitle("유저 밴");
					d.setHeaderText("밴 사유를 입력해주세요");
					d.setContentText("사유 : ");
					if(tv.getSelectionModel().getSelectedItem() != null) {
						d.show();
					}
					d.setOnCloseRequest( ee -> {
						if(d.getResult() != null) {
							dao.userBan(tv.getSelectionModel().getSelectedItem(), d.getResult());
							tv_ol = dao.allUser();
							tv.setItems(tv_ol);
						}
					});
					
				});
				
				btnImg.setOnAction( e -> {
					byte[] b = dao.getUserImage(tv.getSelectionModel().getSelectedItem());
					imgShow(b);
				});
				
				btnEp.setOnAction( e -> {
					fc.getExtensionFilters().clear();
					fc.getExtensionFilters().add((new ExtensionFilter("텍스트(*.txt)", "*.txt")));
					File file = fc.showSaveDialog(this.server);
					if(file == null) {
						return;
					}
					fc.setInitialDirectory(file.getParentFile());
					dao.userExport(file.getPath());
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.tv_ol = dao.allUser();
		tv.setItems(tv_ol);
		userTableStage.show();
	}
	
	
}
