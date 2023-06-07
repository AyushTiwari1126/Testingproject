package card_management;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import Maven.WOWCard_API.ExtentReportBase;
import Maven.WOWCard_API.HeaderTestCase;
import Maven.WOWCard_API.Pair;
import Maven.WOWCard_API.UtilityClass;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GenerateCard 
{
	String dirPath = System.getProperty("user.dir");
	public static ExtentReports extent;
    public static ExtentSparkReporter spark;
    public static ExtentTest test;
    public static SoftAssert softassert;
    JSONObject jsonObject; 
    RequestSpecification httpsRequest,httpsRequest1,httpsRequest2;
    Response response,response1,response2;
    String[] headerInfo;
    String AuthorizationToken;
    String productIdTest;
    
   @BeforeTest
    public void getAccessToken() throws IOException, ParseException
    {   //Authorization Token Created
	   
    	RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri_Token");
    	httpsRequest1 = RestAssured.given();
    	
    	//read data from json file
    	jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\AutherizationToken\\AuthToken.json");
    	
    	//add json body into RequestSpecification variable "httpRequest1"
    	httpsRequest1.body(jsonObject.get("RequestBody").toString());
    	
    	
    	// add header information into request specification variable
    	Map<String, String> header = HeaderTestCase.headerforToken();
	    httpsRequest1.headers(header);
    			
    			
	    //get response
		response1 = httpsRequest1.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));
		System.out.println(response1.asPrettyString());
		AuthorizationToken=new String("Bearer "+response1.asString());
     }
     
     @BeforeMethod
	 public void beforeMethod() throws IOException
	 {   //attach result to existing report
    	extent=ExtentReportBase.getReports();
		
		//set base uri
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri");
		httpsRequest = RestAssured.given();
		httpsRequest2 = RestAssured.given();
		softassert=new SoftAssert();
		
		
	}
	@AfterMethod
	public void afterMethod()
	{ 
		extent.flush();
		
	}
	
	@Test(priority=1)
	public void authrizationTokenGenration()
	{   //validate Authorization Token
		test=extent.createTest("Authorization Token Genration");
		softassert.assertNotNull(AuthorizationToken);
		if(response1.asString().isEmpty())
		{
			test.log(Status.FAIL,"Authorization Token not genrated");
		}
		else 
		{
			test.log(Status.PASS,"Authorization Token for further Tests genrated succesfully");
		}
		
		softassert.assertAll();
		
	}
	
	@Test(priority=2,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC001() throws IOException, ParseException 
	{           //Validate the response after providing productId(1)in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC001.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=3,dependsOnMethods= {"TGC001"} )
	public void TGC001a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC001.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=4,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC002() throws IOException, ParseException 
	{           //Validate the response after providing productId(35) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC002.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=5,dependsOnMethods= {"TGC002"} )
	public void TGC002a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC002.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=6,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC003() throws IOException, ParseException 
	{           //Validate the response after providing productId(26) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC003.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=7,dependsOnMethods= {"TGC003"} )
	public void TGC003a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC003.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=8,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC004() throws IOException, ParseException 
	{           //Validate the response after providing productId(123)in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC004.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=9,dependsOnMethods= {"TGC004"} )
	public void TGC004a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC004.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=10,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC005() throws IOException, ParseException 
	{           //Validate the response after providing productId(222)in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC005.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=11,dependsOnMethods= {"TGC005"} )
	public void TGC005a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC005.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=11,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC006() throws IOException, ParseException 
	{           //Validate the response after providing productId(213)in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC006.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=12,dependsOnMethods= {"TGC006"} )
	public void TGC006a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC006.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}

	@Test(priority=13,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC007() throws IOException, ParseException 
	{           //Validate the response after providing productId(000) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC007.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=14,dependsOnMethods= {"TGC007"} )
	public void TGC007a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC007.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	
	@Test(priority=15,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC008() throws IOException, ParseException 
	{           //Validate the response after providing productId(36) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC008.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=16,dependsOnMethods= {"TGC008"} )
	public void TGC008a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC008.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=17,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC009() throws IOException, ParseException 
	{           //Validate the response after providing productId(65) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC009.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=18,dependsOnMethods= {"TGC009"} )
	public void TGC009a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC009.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=19,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC010() throws IOException, ParseException 
	{           //Validate the response after providing productId(13)in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC010.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=20,dependsOnMethods= {"TGC010"} )
	public void TGC010a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC010.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=21,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC011() throws IOException, ParseException 
	{           //Validate the response after providing productId(11) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC011.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=22,dependsOnMethods= {"TGC011"} )
	public void TGC011a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC0011.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=23,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC012() throws IOException, ParseException 
	{           //Validate the response after providing productId(005) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC012.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=24,dependsOnMethods= {"TGC012"} )
	public void TGC012a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC012.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=25,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC013() throws IOException, ParseException 
	{           //Validate the response after providing productId(999) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC013.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=26,dependsOnMethods= {"TGC013"} )
	public void TGC013a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC013.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}

	@Test(priority=27,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC014() throws IOException, ParseException 
	{           //Validate the response after providing productId(17) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC014.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=28,dependsOnMethods= {"TGC014"} )
	public void TGC014a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC014.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=29,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC015() throws IOException, ParseException 
	{           //Validate the response after providing productId(33) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC015.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=30,dependsOnMethods= {"TGC015"} )
	public void TGC015a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC015.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}
	
	@Test(priority=31,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC016() throws IOException, ParseException 
	{           //Validate the response after providing productId(25) in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC016.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response body node
				String s2=jsonObject.get("KeysForValidationInResponse").toString();	
				String[] s3=s2.replace("["," ").replace("]"," ").replace('"',' ').split(",");
				System.out.println("s3 : "+s3);
				
				
				for(int i=0;s3.length>i;i++)
				{ 	System.out.println(s3[i]);
					//1.validate responseCode present in body
					softassert.assertTrue(response.getBody().asString().contains(s3[i].trim()));
					if(response.asString().contains(s3[i].trim()))
					{System.out.println(s3[i].trim());
						test.log(Status.PASS, "Response Body contains " + s3[i]);
					}
					else
					{
						test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
					}
				}
				
				
		       //2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test(priority=32,dependsOnMethods= {"TGC016"} )
	public void TGC016a() throws IOException, ParseException 
	{           //Validate the response after providing valid header with valid request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC016.json");
			    
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description1"));
				
				
				// add json body into RequestSpecification variable "httpRequest2"
				JSONObject requestParamas = new JSONObject();
                requestParamas.put("cardId", response.jsonPath().get("responseData.cardId"));
				System.out.println("*"+response.jsonPath().get("responseData.cardId"));
				httpsRequest2.body(requestParamas.toJSONString());
				
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
				httpsRequest2.headers(header);
				
				//get response
				response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("getCardInfoEndPontUrl"));
				System.out.println("TGCa"+response2.asString());
				
				
				//test
				//validate productId of new generated card
				softassert.assertEquals(response2.jsonPath().getString("responseData.cardProductId"),productIdTest);
				
			      if (response2.jsonPath().getString("responseData.cardProductId").equals(productIdTest)) 
					 {
					test.log(Status.PASS,"Generated card productId is same");
					 } 
				else 
					{
					 test.log(Status.FAIL," Generated card productId is Different" );
					 }
				
			      softassert.assertAll();   
				
	}

	@Test(priority=33,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH001() throws IOException, ParseException 
	{           //Validate the response with ReqDatetime header value blank
		
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH001.json");
			    
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
			    httpsRequest.headers(header);
				
				
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH001"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateForBlankReqDateTime(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				softassert.assertAll();
				
	}
	
	@Test(priority=34,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH002() throws IOException, ParseException 
	{           //Validate the response with Sign header value blank 
		        
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH002.json");
			    
			    //ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
			    System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH002"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateForBlankSign(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				
				softassert.assertAll();
				
	}
	
	@Test(priority=35,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH003() throws IOException, ParseException 
	{           //Validate the response with Authorization header value blank         
		
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH003.json");
			    
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH003"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateForBlankAuthrization(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				softassert.assertAll();
				
	}
	
	@Test(priority=36,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH004() throws IOException, ParseException 
	{           //Validate the response without ReqDatetime header
		        
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH004.json");
			    
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
			    httpsRequest.headers(header);
				
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH004"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateWithoutReqDateTime(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				softassert.assertAll();
				
	}
	
	@Test(priority=37,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH005() throws IOException, ParseException 
	{           //Validate the response without Sign Header.
		
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH005.json");
			    
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH005"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateWithoutSign(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				softassert.assertAll();
				
	}
	
	@Test(priority=38,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH006() throws IOException, ParseException 
	{           //Validate the response without Authorization header
		
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH006.json");
			    
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
			    System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH006"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateWithoutAuthrization(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				
				softassert.assertAll();
				
	}
	
	@Test(priority=39,dependsOnMethods= {"authrizationTokenGenration"})
	public void TH007() throws IOException, ParseException 
	{           //Validate the response after providing  all header  without value
		
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\GenerateCard\\TH007.json");
			    
			   //ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
			    httpsRequest.headers(header);
				
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TH007"+response.asString());
				
				
				//test
				//1.validate http status code		
				Pair result =  HeaderTestCase.validateAllHeaderBlank(response);
				softassert.assertTrue(result.isPass());
				if (result.isPass())
				{ 
					test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+result.getCode());
				
				}
				else
					{
					test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+
					result.getCode());
					
					}
				
				
				softassert.assertAll();
				
	}
	
	@Test(priority=40,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC017() throws IOException, ParseException 
	{           //Validate the response after providing blank productId node in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC017.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	@Test(priority=41,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC018() throws IOException, ParseException 
	{           //Validate the response after providing spaces in  productId node in request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC018.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//2.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //3.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	@Test(priority=42,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC019() throws IOException, ParseException 
	{           //Validate the response after providing blank request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC019.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
			   //1.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
	
	@Test(priority=43,dependsOnMethods= {"authrizationTokenGenration"} )
	public void TGC020() throws IOException, ParseException 
	{           //Validate the response after providing invalid productId(not present on server) request body
                
		        //read data from json file
				jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\cardManagement\\generateCard\\TGC020.json");
			    
				//store productId
				productIdTest=(String) jsonObject.get("productId");
				
				//ADD test case Description in test report
				test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
				
				// add json body into RequestSpecification variable "httpRequest"
				System.out.println(jsonObject.get("RequestBody").toString());
				httpsRequest.body(jsonObject.get("RequestBody").toString());
				
				// add header information into request specification variable
				Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
			    httpsRequest.headers(header);
				
				//get response
				response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("generateCardEndPointUrl"));
				System.out.println("TGCBI"+response.asString());
				
				
				//test
				//1.validate response code
				softassert.assertEquals(response.jsonPath().getString("responseCode"),jsonObject.get("ExpectedResponseCode").toString());
									
			      if (jsonObject.get("ExpectedResponseCode").toString().equals(response.jsonPath().getString("responseCode"))) 
					 {
					test.log(Status.PASS,"Test Expected response code and actual response code is same "+response.jsonPath().get("responseCode"));
					 } 
				else 
					{
					 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ "but we get "+response.jsonPath().get("responseCode"));
					 }
			     
			   
			    //2.validate http status code		
					softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
					if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +"and actual is "+Integer.toString(response.getStatusCode()));
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is"+" "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
						
						}
			      softassert.assertAll();   
				
	}
}
