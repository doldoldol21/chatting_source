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
	private Connection conn;    //DB 커넥션(연결) 객체
    private static final String USERNAME = "root";   //DB 접속시 ID
    private static final String PASSWORD = "1234";    //DB 접속시 패스워드
    private static final String URL = "jdbc:mysql://localhost:3306/userdb";  //DB접속 경로(스키마=데이터베이스명)설정
    private EtcFuntion ef = new EtcFuntion();
    
    public DAO() {
		try {
            Class.forName("com.mysql.jdbc.Driver"); 
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("드라이버 로딩 성공!!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("드라이버 로드 실패!!");
        }
    }   
    public static DAO getIntans() {
    	if(instance == null) {
    		instance = new DAO();
    	}
    	return instance;
    }
    
    //회원가입 할때 아이디비교
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
             
             //user.getId()가 null이 아니면 아이디가 존재하는 것이므로
             //false 리턴
             if(user.getId() != null)
            	 return false;
             else
            	 return true;
             
         }catch (Exception e) {
        	 System.out.println("userDuplicateCheck1 메서드 조회 실패");
        	 e.printStackTrace();
        	 return false;
		}
    }
    
    //회원가입 닉네임비교
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
            	 //존재하지 않으면 insert 시킨다.
            	 return userInsert(u);
             
         }catch (Exception e) {
        	 System.out.println("userDuplicateCheck2 메서드 조회 실패");
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
        	System.out.println("회원가입 성공");
        	
        	pstmt.close();
        	return true;
        }catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("userInsert 삽입 실패");
        	return false;
		}
    }
    
    //로그인 할 때
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
                	user.setId("차단");
                	user.setNickname(rs.getString(9));
                	return user;
                }
	        	user.setId(rs.getString(1));
	           	user.setPw(rs.getString(2));
	           	user.setNickname(rs.getString(3));
	           	user.setImage(rs.getBytes(4));
	           	user.setIp(u.getIp());
            }
            
            //로그인 할 때 마지막 접속시간을 업데이트 하면서 들어간다.
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
            System.out.println("아이디 확인 성공");
            
            pstmt.close();
            return user;
        }catch (Exception e) {
        	System.out.println("userCheck 메서드 조회 실패");
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
	     	System.out.println("이미지교체 성공");
	     	
	     	pstmt.close();
	     	return true;
	   	}catch (Exception e) {
	   		 System.out.println("이미지교체 실패");
	   		 return false;
		}
    }
    
    //유저 전체 목록
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
	     	System.out.println("회원 조회 성공");
	     	pstmt.close();
	     	return ol;
	   	}catch (Exception e) {
	   		e.printStackTrace();
	   		System.out.println("회원 조회 실패");
		}
    	return null;
    }
    
    //유저 삭제
    public void userDelate(UserTableView utv) {
    	String sql = "delete from usertbl where id = ?";
    	PreparedStatement pstmt = null;
    	try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, utv.getId());
			pstmt.executeUpdate();
			System.out.println("유저 삭제 성공");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("유저 삭제 실패");
		}
    }
    
    //유저 밴
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
	     	System.out.println("유저 차단 성공");
	     	
	     	pstmt.close();
	   	}catch (Exception e) {
	   		e.printStackTrace();
	   		 System.out.println("유저 차단 실패");
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
        	System.out.println("이미지 조회 실패");
        	
		}
    	return null;
    }
    
    //파일
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
        	System.out.println("파일 저장 성공");
        	
        	pstmt.close();
        	return true;
        }catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("파일 저장 실패");
        	return false;
		}
    }
    
    //첨에 시작할때
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
        	System.out.println("파일 조회 실패");
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
        	System.out.println("파일 조회 실패");
        	
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
    		System.out.println("파일 삭제 성공");
    		return true;
    	}catch (Exception e) {
    		e.printStackTrace();
        	System.out.println("파일 삭제 실패");
        	return false;
		}
    }
    
    public void userExport(String path) {
//    	C:\Desktop\iop.txt
//		이런식으로 경로가 들어오지만 mysql 쿼리에서는 \\으로 바꿔주어야한다.
    	path = path.replaceAll("\\\\", "\\\\\\\\");
    	
    	//utf-8이 아니라 utf8 혹시 안되면 my.ini 수정 필요
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
    		//executeUpdate는 안된다
    		pstmt.executeQuery();
    		System.out.println("성공");
    	}catch (Exception e) {
    		e.printStackTrace();
        	System.out.println("실패");
		}
    	
    }
    
}
