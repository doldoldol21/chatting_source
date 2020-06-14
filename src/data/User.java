package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable{
	
	private String id;
	private String pw;
	private String nickname; 
	private byte[] image;
	private String ip;
	
	public User() {
	}
	
	public User(String id, String pw, String nickname, byte[] image) {
		this.id = id;
		this.pw = pw;
		this.nickname = nickname;
		this.image = image;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
	@Override
	public String toString() {
		return this.getNickname() + "(" + this.getId() + ") - " + this.getIp();
	}
	
}
