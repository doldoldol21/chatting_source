package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Room implements Serializable{

	String name;
	int cnt;
	
	public Room() {
	}
	public Room(String name, int cnt) {
		this.name = name;
		this.cnt = cnt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCnt() {
		return cnt;
	}
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
}
