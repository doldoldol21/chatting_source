package server.javaFile;

import javafx.beans.property.SimpleStringProperty;

public class UserTableView {

	private SimpleStringProperty id;
	private SimpleStringProperty pw;
	private SimpleStringProperty nickname;
	private SimpleStringProperty join_date;
	private SimpleStringProperty last_date;
	private SimpleStringProperty ip;
	private SimpleStringProperty escape_date;
	private SimpleStringProperty ban;

	public UserTableView() {
		this.id = new SimpleStringProperty();
		this.pw = new SimpleStringProperty();
		this.nickname = new SimpleStringProperty();
		this.join_date = new SimpleStringProperty();
		this.last_date = new SimpleStringProperty();
		this.ip = new SimpleStringProperty();
		this.escape_date = new SimpleStringProperty();
		this.ban = new SimpleStringProperty();
	}
	
	
	public UserTableView(String id, String pw, String nickname,
			String join_date, String last_date,  
			String ip, String escape_date, String ban) {
		this();
		this.id.set(id);
		this.pw.set(pw);
		this.nickname.set(nickname);
		this.join_date.set(join_date);
		this.last_date.set(last_date);
		this.ip.set(ip);
		this.escape_date.set(escape_date);
		this.ban.set(ban);
	}


	public String getId() {
		return id.get();
	}
	public void setId(String id) {
		this.id.set(id);
	}
	public String getPw() {
		return pw.get();
	}
	public void setPw(String pw) {
		this.pw.set(pw);
	}
	public String getNickname() {
		return nickname.get();
	}
	public void setNickname(String nickname) {
		this.nickname.set(nickname);
	}
	public String getJoin_date() {
		return join_date.get();
	}
	public void setJoin_date(String join_date) {
		this.join_date.set(join_date);
	}
	public String getLast_date() {
		return last_date.get();
	}
	public void setLast_date(String last_date) {
		this.last_date.set(last_date);
	}
	public String getIp() {
		return ip.get();
	}
	public void setIp(String ip) {
		this.ip.set(ip);
	}
	public String getEscape_date() {
		return escape_date.get();
	}
	public void setEscape_date(String escape_date) {
		this.escape_date.set(escape_date);
	}
	public String getBan() {
		return ban.get();
	}
	public void setBan(String ban) {
		this.ban.set(ban);
	}
}
