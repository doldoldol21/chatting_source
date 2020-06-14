package server.javaFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.scene.control.TextArea;

public class LogExport {
	
	private Workbook wb;
	private Sheet sheet;
	private SimpleDateFormat sdf1;
	private SimpleDateFormat sdf2;
	private Row r;
	private Cell c1;
	private TextArea ta;
	
	
	public LogExport() {
		sdf1 = new SimpleDateFormat("yyyyMMdd");
		sdf2 = new SimpleDateFormat("ahh.mm.ss ");
	}
	
	@SuppressWarnings("deprecation")
	public void Export(TextArea ta) {
		this.ta = ta;
		Date date = new Date();
		
		if(ta.getText().equals("")) {
			return;
		}
		try {
			//파일 저장될 폴더.
			File dir = new File("C:/logTemp/");
			
			if(!dir.exists())
				dir.mkdirs();
			
			
			File[] fileList = dir.listFiles();
			boolean flag = false;
			for(File f : fileList) {
				if(f.isFile()) {
					if(!f.getName().endsWith("xlsx") && !f.getName().startsWith("log"))
						continue;
					//지금 저장하는 날짜와 파일의 날짜가 같을 때는 기존 파일 끝행부터 붙여넣는다.
					if(f.getName().equals("log" + sdf1.format(date) + ".xlsx")) {
						FileInputStream inputStream = new FileInputStream(f);
			            wb = new XSSFWorkbook(inputStream);	//엑셀읽기
			            sheet = wb.getSheetAt(0);	//시트가져오기 0은 첫번째 시트
			            System.out.println(sheet.getSheetName());
		            	r = sheet.getRow(sheet.getPhysicalNumberOfRows()-1);	//시트의 마지막 행번호 받기
		            	c1 = r.getCell(0);	//마지막 행의 첫번째 셀
		            	String s = c1.getStringCellValue().substring(c1.getStringCellValue().indexOf("-")+1);	//값 받기
		            	
		            	int hour = Integer.parseInt(s.split(":")[0]);
		            	int minute = Integer.parseInt(s.split(":")[1]);
		            	int second = Integer.parseInt(s.split(":")[2]);
		            	Time t = new Time(hour, minute, second);
		            	
		            	StringTokenizer st1 = new StringTokenizer(this.ta.getText(), "\n");
		            	
		         		while(st1.hasMoreTokens()) {
		         			String str = st1.nextToken();
		         			
		         			if(str.indexOf("[") == -1) {
		         				continue;
		         			}
		         			
		         			StringTokenizer st2 = new StringTokenizer(str, "]");
		         			
		         			while(st2.hasMoreTokens()) {
		         				String str1 = st2.nextToken();
		         				String str2 = str1.substring(str1.indexOf("-")+1);
		         				
		         				int hour2 = Integer.parseInt(str2.split(":")[0]);
				            	int minute2 = Integer.parseInt(str2.split(":")[1]);
				            	int second2 = Integer.parseInt(str2.split(":")[2]);
				            	Time t2 = new Time(hour2, minute2, second2);
				            	
				            	if(t2.before(t)) {
				            		break;
				            	}
				            	
				            	r = sheet.createRow(sheet.getPhysicalNumberOfRows());
		         				c1 = r.createCell(0);
		         				c1.setCellValue(str1.substring(1));
		         				
		         				c1 = r.createCell(1);
		         				c1.setCellValue(st2.nextToken().substring(1));

		         				c1 = r.createCell(2);
		         				
		         				if(st2.countTokens() > 0) {
		         					c1.setCellValue(st2.nextToken().substring(1));
								}
		         			}
		         		}
		         		
		         		FileOutputStream fos = new FileOutputStream(f);
		    			wb.write(fos);
		    			System.out.println("완료");
		    			flag = true;
						break;
					}//if문 끝
				}
			}//for문 끝
			
			//파일 중 오늘 날짜의 파일 이없을 땐 새로 생성한다.
			if(!flag) {
				wb = new XSSFWorkbook();
				sheet = wb.createSheet(sdf2.format(date.getTime()));
				sheet.setColumnWidth(0, 10000);
				sheet.setColumnWidth(1, 10000);
				sheet.setColumnWidth(2, 30000);
				StringTokenizer st1 = new StringTokenizer(this.ta.getText(), "\n");
         		int cnt = 0;
				while(st1.hasMoreTokens()) {
         			String str = st1.nextToken();
         			if(str.indexOf("[") == -1) {
         				continue;
         			}
					r = sheet.createRow(cnt++);
					StringTokenizer st2 = new StringTokenizer(str, "]");
					int cnt2 = 0;
	     			while(st2.hasMoreTokens()) {
	     				c1 = r.createCell(cnt2++);
	     				c1.setCellValue(st2.nextToken().substring(1));
	     			}
         		}

				File f = new File(dir.getPath() + "/log" + sdf1.format(date) + ".xlsx");
				if(!f.exists()) {
					f.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(f);
				wb.write(fos);
				System.out.println("새로");
			}
			
			
			System.out.println("로그 저장 완료");
			//마지막엔 텍스트창 클리어
			this.ta.clear();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
