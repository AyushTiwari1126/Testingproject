package Maven.WOWCard_API;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import io.restassured.response.Response;
  

public class HeaderTestCase 
{
	
	static Map<String, String> createMapForHeaderWithContent() throws IOException {
		Map<String,String> header=new HashMap<String,String>();
		header.put(UtilityClass.getDataFromPF("HeaderKey"), UtilityClass.getDataFromPF("HeaderValue"));
		return header;
	}
	
	
	public static Map<String, String> getValidHeader(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\Valid.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0], UtilityClass.getReqDatetimeUTC());
    	
		
		//3 Sign
		headerInfo=jsonObject.get("AuthKey2").toString().split(":");
		
		if (headerInfo[1].equals("System Generated")){
			
			headerInfo[1]=UtilityClass.getReqDatetimeUTC();
			}
		header.put(headerInfo[0],headerInfo[1]);
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0], authorizationToken);
	    
	    return header;
		
    }
	
    public static Map<String,String>  getHeaderForBlankReqdatetime(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankReqDatetime.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0]," ");
    	
		
		//3 Sign
		headerInfo=jsonObject.get("AuthKey2").toString().split(":");
		
		if (headerInfo[1].equals("System Generated")){
			
			headerInfo[1]=UtilityClass.getReqDatetimeUTC();
			}
		header.put(headerInfo[0],headerInfo[1]);
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0], authorizationToken);
	    return header;
		
    }
    
    public static Pair validateForBlankReqDateTime( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankReqDatetime.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    public static Pair validateForBlankSign( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankSign.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    
    public static Pair validateForBlankAuthrization( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankAuthrization.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    
    public static Pair validateWithoutReqDateTime( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankReqDatetime.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    
    public static Pair validateWithoutSign( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankSign.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    
    public static Pair validateWithoutAuthrization( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankAuthrization.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    public static Pair validateAllHeaderBlank( Response response) throws IOException, ParseException {
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankAuthrization.json");
    	String statusCode = jsonObject.get("ExpectedHTTPStatusCode").toString();
    	boolean pass = Integer.toString(response.getStatusCode()).equals(statusCode);
    	return new Pair(pass, statusCode);
    	
    }
    public static Map<String,String>  getHeaderForBlankSign(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankSign.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0], UtilityClass.getReqDatetimeUTC());
    	
		
		//3 Sign
		headerInfo=jsonObject.get("AuthKey2").toString().split(":");
		header.put(headerInfo[0]," ");
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0], authorizationToken);
	    
	    return header;
		
    }
    
    public static Map<String,String>  getHeaderForBlankAuthrization(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankAuthrization.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0], UtilityClass.getReqDatetimeUTC());
    	
		
		//3 Sign
         headerInfo=jsonObject.get("AuthKey2").toString().split(":");
 		
 		if (headerInfo[1].equals("System Generated")){
 			
 			headerInfo[1]=UtilityClass.getReqDatetimeUTC();
 			}
 		header.put(headerInfo[0]," ");
 		
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0]," ");
	    
	    return header;
		
    }
    
    public static Map<String,String>  getHeaderForWithoutReqdatetime(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankReqDatetime.json");
    	 Map<String, String> header = createMapForHeaderWithContent();
        
    	
    	 String[] headerInfo=jsonObject.get("AuthKey2").toString().split(":");
		//3 Sign
		
		if (headerInfo[1].equals("System Generated")){
			
			headerInfo[1]=UtilityClass.getReqDatetimeUTC();
			}
		header.put(headerInfo[0],headerInfo[1]);
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0], authorizationToken);
	    return header;
		
    }
    
    public static Map<String,String>  getHeaderWithoutSign(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankSign.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0], UtilityClass.getReqDatetimeUTC());
    	
		
		
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0], authorizationToken);
	    
	    return header;
		
    }
    
    public static Map<String,String>  getHeaderForWithoutAuthrization(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\blankAuthrization.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0], UtilityClass.getReqDatetimeUTC());
    	
		
		//3 Sign
         headerInfo=jsonObject.get("AuthKey2").toString().split(":");
 		
 		if (headerInfo[1].equals("System Generated")){
 			
 			headerInfo[1]=UtilityClass.getReqDatetimeUTC();
 			}
 		header.put(headerInfo[0]," ");
 		
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0]," ");
	    
	    return header;
		
    }
    
    public static Map<String, String> getAllHeaderBlank(String authorizationToken) throws IOException, ParseException
    {   
    	
    	
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\Valid.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 Map<String, String> header = createMapForHeaderWithContent();
         header.put(headerInfo[0], " ");
    	
		
		//3 Sign
		headerInfo=jsonObject.get("AuthKey2").toString().split(":");
		
		
		header.put(headerInfo[0]," ");
		
		//4 Authorization
		headerInfo=jsonObject.get("AuthKey3").toString().split(":");
		
	    header.put(headerInfo[0]," ");
	    
	    return header;
		
    }
    
    public static Map<String, String> headerforToken() throws IOException, ParseException{
    	String dirPath = System.getProperty("user.dir");
    	JSONObject jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\HeaderTestCase\\Valid.json");
    	 String[] headerInfo=jsonObject.get("AuthKey1").toString().split(":");
    	 Map<String, String> header = createMapForHeaderWithContent();
    	 header.put(headerInfo[0], UtilityClass.getReqDatetimeUTC());
		return header;
    }
    
    
}
