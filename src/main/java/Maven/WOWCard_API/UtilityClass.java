package Maven.WOWCard_API;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UtilityClass 
{
	static JSONParser jsonParserj;
	static Object javaObject;
	static JSONObject jsObject;
	static Properties prop;
	static String[] headerArray;
	static Random random;
	static CharSequence currentDate;
	static Calendar cal;
	static Date d;
	static SimpleDateFormat sdf,sdf1;
 
	
	public static String getDataFromPF(String key) throws IOException
  {   
	  FileInputStream file=new FileInputStream(System.getProperty("user.dir")+"\\static_data.properties");
	 // FileInputStream file=new FileInputStream("C:\\Users\\nikhil\\eclipse-workspace\\temple_project1\\static_data.properties");
	  
	  prop=new Properties();
	  
	  prop.load(file);
	  
	  String value=prop.getProperty(key);
	  
	  return value;
  }
  
 
  
 public static JSONObject convertDataStringToJsonObject(String expectedResponse) throws ParseException
 {
	 jsonParserj=new JSONParser();
	 javaObject=jsonParserj.parse(expectedResponse);
     jsObject=(JSONObject)javaObject;
     return jsObject;
     
     
 }
 

 
 public static JSONObject readDataFromJSONFile(String filePath) throws IOException, ParseException
 {
	 jsonParserj=new JSONParser();
	 FileReader reader=new FileReader(filePath);
	 javaObject=jsonParserj.parse(reader);
     jsObject=(JSONObject)javaObject;
     return jsObject; 
 }
 public static String getReqDatetimeUTC()
 {
	 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMYYYYHHmmss");
	    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
	    return dtf.format(now);
 }
 
 public static String getIdNumber()
 {
	random=new Random();
	int a1=random.nextInt(10);
	int a2=random.nextInt(10);
	String idNumber=getReqDatetimeUTC().concat(Integer.toString(a1).concat(Integer.toString(a2)));
	return idNumber;
 }
 
 public static String getEmail()
 { 
	String email=RandomStringUtils.randomAlphabetic(7).concat("@test.com");
	System.out.println(email);
	return email;
 }
 
 public static String getMobileno()
 { 
	 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMYYYYmmss");
	    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
	    return dtf.format(now);
 }
 
 public static String getNewDataForRegistration(String oldBody)
 {
	String newRequestBody=oldBody.replace("{{System Generated idNumber}}", UtilityClass.getIdNumber()).replace("{{System Generated email}}", UtilityClass.getEmail()).replace("{{System Generated mobileno}}",UtilityClass.getMobileno().replace("{{CurrentDate}}", UtilityClass.getCurrentDate()).replace("{{FutureDate}}", UtilityClass.getFutureDate()).replace("{{18yearoldDate}}",UtilityClass.get18yearOldDate()));
	System.out.println(newRequestBody);
	return newRequestBody;
 }
 
 public static CharSequence getCurrentDate()
 {
     currentDate= new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
     System.out.println(currentDate);
     return currentDate;
 }
 
 public static String getFutureDate()
 {
	 cal=Calendar.getInstance();
	 cal.add(Calendar.DATE,10);
	 sdf=new SimpleDateFormat("MM/dd/YYYY");
	 
	 Date d=cal.getTime();
     String FutureDate=sdf.format(d);
     System.out.println(FutureDate);
     return FutureDate;
 }
 
 public static String get18yearOldDate()
 {
	 cal=Calendar.getInstance();
	 sdf=new SimpleDateFormat("MM/dd/YYYY");
	 sdf1=new SimpleDateFormat("YYYY");
	 
	 Date d=cal.getTime();
	 
	 String Date=sdf.format(d);
     String currentYear=sdf1.format(d);
     CharSequence yearBefore18=Integer.toString(Integer.parseInt(currentYear)-18);
     String changeDate=Date.replace(sdf1.format(d), yearBefore18);
     System.out.println(changeDate);
     return changeDate;
 }
 
 public static String getRefNo()
 {
	    random=new Random();
		int a1=random.nextInt(10);
		int a2=random.nextInt(10);
		String refNo=getReqDatetimeUTC().concat(Integer.toString(a1).concat(Integer.toString(a2)));
		System.out.println(refNo);
		System.out.println(refNo);
		return refNo; 
 }
 
 public static String getNewDataForIssuerTopup(String oldBody )
 {
	 String newRequestBody=oldBody.replace("System Generated refNo",getRefNo());
		System.out.println(newRequestBody);
		return newRequestBody; 
 }
}
