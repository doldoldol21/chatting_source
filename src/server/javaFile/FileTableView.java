package server.javaFile;

import javafx.beans.property.SimpleStringProperty;

public class FileTableView {
	
	private int no;
	private SimpleStringProperty id;
	private SimpleStringProperty title;
	private SimpleStringProperty date;
	private SimpleStringProperty size;
	private SimpleStringProperty format;
	
	public FileTableView() {
		this.date = new SimpleStringProperty();
		this.id = new SimpleStringProperty();
		this.format = new SimpleStringProperty();
		this.title = new SimpleStringProperty();
		this.size = new SimpleStringProperty();
	}
	
	
	public FileTableView(String date, String id, String format, String title, String size) {
		this();
		this.date.set(date);
		this.id.set(id);
		this.format.set(format);
		this.title.set(title);
		this.size.set(size);
	}
	
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}

	public String getId() {
		return id.get();
	}
	public void setId(String id) {
		this.id.set(id);
	}
	public String getTitle() {
		return title.get();
	}
	public void setTitle(String title) {
		this.title.set(title);
	}
	public String getDate() {
		return date.get();
	}
	public void setDate(String date) {
		this.date.set(date);
	}
	public String getSize() {
		return size.get();
	}
	public void setSize(String size) {
		this.size.set(size);
	}
	public String getFormat() {
		return format.get();
	}
	public void setFormat(String format) {
		this.format.set(format);
	}
	
	
}
