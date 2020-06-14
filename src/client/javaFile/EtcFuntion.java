package client.javaFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

import data.Data;
import data.User;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class EtcFuntion {

	private Stage client;
	private File file;
	private FileChooser fc;
	
	public EtcFuntion() {
	}
	
	public EtcFuntion(Stage c) {
		this.client = c;
		fc = new FileChooser();
	}
	
	public Data show(User user, String roomName) {
		try {
			Data data = new Data();
			fc.setSelectedExtensionFilter(new ExtensionFilter("모든 파일", "*.all"));
			file = fc.showOpenDialog(client);
			fc.setInitialDirectory(file.getParentFile());
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			if(fis.available() > 150000000) {
				System.out.println(Byte.MAX_VALUE);
				bis.close();
				fis.close();
				data.setStatus("용량초과");
				return data;
			}
			byte[] b = new byte[fis.available()];
			bis.read(b);
			bis.close();
			fis.close();
			data.setMessage(file.getName());
			data.setFile(b);
			data.setUser(user);
			data.setRoomName(roomName);
			String regex = file.getName().substring(file.getName().lastIndexOf(".")+1, file.getName().length());
			if(regex.equals("jpg") || regex.equals("gif") || regex.equals("png"))
				data.setStatus("이미지");
			else
				data.setStatus("파일");
			
			return data;
			
		}catch (Exception e) {
		}
		return null;
	}
	
	public String byteCalculation(int bytes) {
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
	
}
