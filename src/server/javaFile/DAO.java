package server.javaFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import data.Data;
import data.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DAO {
	
	private static DAO instance;
	private Connection conn;    //DB Ŀ�ؼ�(����) ��ü
    private static final String USERNAME = "root";   //DB ���ӽ� ID
    private static final String PASSWORD = "1234";    //DB ���ӽ� �н�����
    private static final String URL = "jdbc:mysql://localhost:3306/userdb";  //DB���� ���(��Ű��=�����ͺ��̽���)����
    private EtcFuntion ef = new EtcFuntion();
    
    public DAO() {
		try {
            Class.forName("com.mysql.jdbc.Driver"); 
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("����̹� �ε� ����!!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("����̹� �ε� ����!!");
        }
    }   
    public static DAO getIntans() {
    	if(instance == null) {
    		instance = new DAO();
    	}
    	return instance;
    }
    
    //ȸ������ �Ҷ� ���̵��
    public boolean userDuplicateCheck1(User u) {
    	String sql = "select * from usertbl where id = ?;";
    	 PreparedStatement pstmt = null;
         try {
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, u.getId());
             ResultSet rs = pstmt.executeQuery();
             User user = new User();
             while(rs.next()) {
            	 user.setId(rs.getString(1));
             }
             
             //user.getId()�� null�� �ƴϸ� ���̵� �����ϴ� ���̹Ƿ�
             //false ����
             if(user.getId() != null)
            	 return false;
             else
            	 return true;
             
         }catch (Exception e) {
        	 System.out.println("userDuplicateCheck1 �޼��� ��ȸ ����");
        	 e.printStackTrace();
        	 return false;
		}
    }
    
    //ȸ������ �г��Ӻ�
    public boolean userDuplicateCheck2(User u) {
    	String sql = "select * from usertbl where nickname = ?;";
    	 PreparedStatement pstmt = null;
         try {
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, u.getNickname());
             ResultSet rs = pstmt.executeQuery();
             User user = new User();
             while(rs.next()) {
            	 user.setNickname(rs.getString(3));
             }
            
             if(user.getNickname() != null)
            	 return false;
             else
            	 //�������� ������ insert ��Ų��.
            	 return userInsert(u);
             
         }catch (Exception e) {
        	 System.out.println("userDuplicateCheck2 �޼��� ��ȸ ����");
        	 e.printStackTrace();
        	 return false;
		}
    }
    
    public boolean userInsert(User user) {
    	String sql = "insert into usertbl value(?, ?, ?, ?, now(), null, null, null);";
    	PreparedStatement pstmt = null;
        try {
        	pstmt = conn.prepareStatement(sql);
        	pstmt.setString(1, user.getId());
        	pstmt.setString(2, user.getPw());
        	pstmt.setString(3, user.getNickname());
        	pstmt.setBytes(4, user.getImage());
        	
        	pstmt.executeUpdate();
        	System.out.println("ȸ������ ����");
        	
        	pstmt.close();
        	return true;
        }catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("userInsert ���� ����");
        	return false;
		}
    }
    
    //�α��� �� ��
    public User userCheck(User u) {
    	String sql = "select * from usertbl where id =? and pw =?;";
   	 	PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, u.getId());
            pstmt.setString(2, u.getPw());
            ResultSet rs = pstmt.executeQuery();
            User user = new User();
           
            while(rs.next()) {
            	if(rs.getString(9) != null) {
                	user.setId("����");
                	user.setNickname(rs.getString(9));
                	return user;
                }
	        	user.setId(rs.getString(1));
	           	user.setPw(rs.getString(2));
	           	user.setNickname(rs.getString(3));
	           	user.setImage(rs.getBytes(4));
	           	user.setIp(u.getIp());
            }
            
            //�α��� �� �� ������ ���ӽð��� ������Ʈ �ϸ鼭 ����.
            if(user.getId() != null) {
            	sql = "update usertbl set last_date = now() where id = ?;";
            	pstmt = conn.prepareStatement(sql);
            	pstmt.setString(1, user.getId());
            	pstmt.executeUpdate();
            	
            	sql = "update usertbl set ip = ? where id = ?;";
            	pstmt = conn.prepareStatement(sql);
            	pstmt.setString(1, u.getIp());
            	pstmt.setString(2, user.getId());
            	pstmt.executeUpdate();
            }
            System.out.println("���̵� Ȯ�� ����");
            
            pstmt.close();
            return user;
        }catch (Exception e) {
        	System.out.println("userCheck �޼��� ��ȸ ����");
        	e.printStackTrace();
        	return null;
		}
    }
    
    public boolean changeImage(User u, byte[] b) {
    	String sql = "update usertbl set image = ? where id = ?;";
   	 	PreparedStatement pstmt = null;
	   	try {
	     	pstmt = conn.prepareStatement(sql);
	     	pstmt.setBytes(1, b);
	     	pstmt.setString(2, u.getId());
	     	pstmt.executeUpdate();
	     	System.out.println("�̹�����ü ����");
	     	
	     	pstmt.close();
	     	return true;
	   	}catch (Exception e) {
	   		 System.out.println("�̹�����ü ����");
	   		 return false;
		}
    }
    
    //���� ��ü ���
    public ObservableList<UserTableView> allUser(){
    	String sql = "select * from usertbl";
    	ObservableList<UserTableView> ol = FXCollections.observableArrayList();
   	 	PreparedStatement pstmt = null;
	   	try {
	     	pstmt = conn.prepareStatement(sql);
	     	ResultSet rs =  pstmt.executeQuery();
	     	while(rs.next()) {
	     		UserTableView utv = new UserTableView(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(5),
	     												rs.getDate(6) + " " + rs.getTime(6), rs.getString(7), rs.getString(8), rs.getString(9));
	     		ol.add(utv);
	     	}
	     	System.out.println("ȸ�� ��ȸ ����");
	     	pstmt.close();
	     	return ol;
	   	}catch (Exception e) {
	   		e.printStackTrace();
	   		System.out.println("ȸ�� ��ȸ ����");
		}
    	return null;
    }
    
    //���� ����
    public void userDelate(UserTableView utv) {
    	String sql = "delete from usertbl where id = ?";
    	PreparedStatement pstmt = null;
    	try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, utv.getId());
			pstmt.executeUpdate();
			System.out.println("���� ���� ����");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("���� ���� ����");
		}
    }
    
    //���� ��
    public void userBan(UserTableView utv, String ban) {
    	String sql = null;
    	if(ban.equals("")) {
        	sql = "update usertbl set ban = ?, escape_date = null where id = ?;";
    	}else {
    		if(utv.getEscape_date() == null) {
    			sql = "update usertbl set ban = ?, escape_date = now() where id = ?;";
    		}else {
    			sql = "update usertbl set ban = ? where id = ?;";
    		}
    	}
   	 	PreparedStatement pstmt = null;
	   	try {
	     	pstmt = conn.prepareStatement(sql);
	     	pstmt.setString(1, ban);
	     	pstmt.setString(2, utv.getId());
	     	pstmt.executeUpdate();
	     	System.out.println("���� ���� ����");
	     	
	     	pstmt.close();
	   	}catch (Exception e) {
	   		e.printStackTrace();
	   		 System.out.println("���� ���� ����");
		}
    }
    
    
    public byte[] getUserImage(UserTableView utv) {
    	String sql = "select image from usertbl where id = ?;";
    	PreparedStatement pstmt = null;
    	byte[] b = null;
    	try {
    		pstmt = conn.prepareStatement(sql);
    		pstmt.setString(1, utv.getId());
    		ResultSet rs = pstmt.executeQuery();
    		
    		rs.next();
    		b = rs.getBytes(1);
    		
    		
    		pstmt.close();
            return b;
    	}catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("�̹��� ��ȸ ����");
        	
		}
    	return null;
    }
    
    //����
    public boolean fileInsert(Data data) {
    	String sql = "insert into filetbl value(null, now(), ?, ?, ?, ?, ?);";
    	PreparedStatement pstmt = null;
        try {
        	pstmt = conn.prepareStatement(sql);
        	pstmt.setString(1, data.getUser().getId());
        	pstmt.setString(2, data.getMessage());
        	pstmt.setBytes(3, data.getFile());
        	pstmt.setString(4, data.getStatus());
        	pstmt.setInt(5, data.getFile().length);
        	pstmt.executeUpdate();
        	System.out.println("���� ���� ����");
        	
        	pstmt.close();
        	return true;
        }catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("���� ���� ����");
        	return false;
		}
    }
    
    //÷�� �����Ҷ�
    public ObservableList<FileTableView> fileSelectAll(){
    	String sql = "select * from filetbl";
    	ObservableList<FileTableView> ol = FXCollections.observableArrayList();
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(sql);
    		ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
            	FileTableView ftv = new FileTableView(rs.getDate(2) + " " + rs.getTime(2), rs.getString(3), rs.getString(6), rs.getString(4), ef.calculation(rs.getInt(7)));
            	ftv.setNo(rs.getInt(1));
            	ol.add(ftv);
            }
            
            pstmt.close();
            return ol;
    	}catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("���� ��ȸ ����");
        	return null;
		}
    }
    
    public byte[] getFile(FileTableView ftv) {
    	System.out.println(ftv.getNo());
    	String sql = "select file, size from filetbl where file_no = ?;";
    	PreparedStatement pstmt = null;
    	byte[] b = null;
    	try {
    		pstmt = conn.prepareStatement(sql);
    		pstmt.setInt(1, ftv.getNo());
    		ResultSet rs = pstmt.executeQuery();
    		
    		rs.next();
    		b = rs.getBytes(1);
    		
    		
    		pstmt.close();
            return b;
    	}catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("���� ��ȸ ����");
        	
		}
    	return null;
    }

    
    public boolean deleteFile(FileTableView ftv) {
    	String sql = "delete from filetbl where file_no = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(sql);
    		pstmt.setInt(1, ftv.getNo());
    		pstmt.executeUpdate();
    		System.out.println("���� ���� ����");
    		return true;
    	}catch (Exception e) {
    		e.printStackTrace();
        	System.out.println("���� ���� ����");
        	return false;
		}
    }
    
    public void userExport(String path) {
//    	C:\Desktop\iop.txt
//		�̷������� ��ΰ� �������� mysql ���������� \\���� �ٲ��־���Ѵ�.
    	path = path.replaceAll("\\\\", "\\\\\\\\");
    	
    	//utf-8�� �ƴ϶� utf8 Ȥ�� �ȵǸ� my.ini ���� �ʿ�
    	String sql = "SELECT * FROM usertbl "
    			+ "INTO OUTFILE ? "
    			+ "CHARACTER SET utf8 "
    			+ "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' "
    			+ "ESCAPED BY '\\\\' "
    			+ "LINES TERMINATED BY '\\n';";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(sql);
    		pstmt.setString(1, path);
    		//executeUpdate�� �ȵȴ�
    		pstmt.executeQuery();
    		System.out.println("����");
    	}catch (Exception e) {
    		e.printStackTrace();
        	System.out.println("����");
		}
    	
    }
    
}
