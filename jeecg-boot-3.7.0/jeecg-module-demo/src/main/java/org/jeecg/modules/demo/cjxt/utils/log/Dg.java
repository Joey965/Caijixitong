package org.jeecg.modules.demo.cjxt.utils.log;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class Dg {


	public static String wpath = "c:/webService_log";
	public static String lpath = "/home/sxby/cjxt_log/webService_log";
	public static boolean isPrintSql = true;

	public static void writeContent(String fileName,String content) {
		  try{
			  String os = System.getProperty("os.name").toLowerCase();
		         if(os.startsWith("windows")) {
		        	 try {
		 				File temFile = new File(wpath+"/"+getCurDate("yyyy-MM-dd"));
		 				if (!temFile.exists()) {
		 					temFile.mkdirs();
		 				}
		 				String pathFile = wpath+"/"+getCurDate("yyyy-MM-dd")+"/"+fileName+".txt";
		 				File f = new File(pathFile);
		 				if (!f.exists()) {
		 					f.createNewFile();
		 				}
		 				BufferedWriter bw = new BufferedWriter(new FileWriter(pathFile,true));
		 				bw.newLine();// 换行
		 				bw.write(getCurDate("yyyy-MM-dd HH:mm:ss")+": "+content);
		 				bw.close();
		 			} catch (IOException e) {
		 				 e.printStackTrace();
		 			}
		         } else if (os.startsWith("linux")) {
		        	 try {
		 				File temFile = new File(lpath+"/"+getCurDate("yyyy-MM-dd"));
		 				if (!temFile.exists()) {
		 					temFile.mkdirs();
		 				}
		 				String pathFile = lpath+"/"+getCurDate("yyyy-MM-dd")+"/"+fileName+".txt";
		 				File f = new File(pathFile);
		 				if (!f.exists()) {
		 					f.createNewFile();
		 				}
		 				BufferedWriter bw = new BufferedWriter(new FileWriter(pathFile,true));
		 				bw.newLine();// 换行
		 				bw.write(getCurDate("yyyy-MM-dd HH:mm:ss")+": "+content);
		 				bw.close();

		 			} catch (IOException e) {
		 				e.printStackTrace();
		 			}
		         }

		  }catch(Exception e){
			  e.printStackTrace();
		  }
	}
	public static String getStackTraceInfo(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);//将出错的栈信息输出到printWriter中
            pw.flush();
            sw.flush();
            return sw.toString();
        } catch (Exception ex) {
        	ex.printStackTrace();
            return "发生错误";
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }

    }

	public static void writeContent(String fileName,String ycTitle,Exception e) {
		writeContent(fileName,ycTitle + "=====发送异常，异常信息如下: " + getStackTraceInfo(e));
	}
	/**
	 * 返回当前字符串型日期
	 *
	 * @param format
	 *            格式规则
	 *
	 * @return String 返回的字符串型日期
	 */
	public static String getCurDate(String format) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpledateformat = new SimpleDateFormat(format);
		String strDate = simpledateformat.format(calendar.getTime());
		return strDate;
	}
}
