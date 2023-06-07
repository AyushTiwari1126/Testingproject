package user_management;

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

public class GetAllRoles 
{
	 String dirPath = System.getProperty("user.dir");
		public static ExtentReports extent;
	    public static ExtentSparkReporter spark;
	    public static ExtentTest test;
	    public static SoftAssert softassert;
	    JSONObject jsonObject,jsonObject1; 
	    RequestSpecification httpsRequest,httpsRequest1,httpsRequest2;
	    Response response,response1,response2;
	    String[] headerInfo;
	    String AuthorizationTokenIssuer,AuthorizationTokenASP;
	    
	   @BeforeTest
	    public void getAccessToken() throws IOException, ParseException
	    {
	    	//Authorization Token Created
		   RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri_Token");
	   	httpsRequest1 = RestAssured.given();
	   	httpsRequest2=RestAssured.given();
	   	//read data from json file
	   	jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\AutherizationToken\\AuthToken.json");
	   	jsonObject1=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\AutherizationToken\\AuthTokenASP.json");
	   	//assign body
	   	httpsRequest1.body(jsonObject.get("RequestBody").toString());
	   	httpsRequest2.body(jsonObject1.get("RequestBody").toString());
	   	
	   	// add header information into request specification variable
	   	Map<String, String> header = HeaderTestCase.headerforToken();
		    httpsRequest1.headers(header);
		    httpsRequest2.headers(header);	
	   			
		//get response
			response1 = httpsRequest1.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));
			response2 = httpsRequest2.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));
			System.out.println(response1.asPrettyString());
			System.out.println(response2.asPrettyString());
			AuthorizationTokenIssuer=new String("Bearer "+response1.asString());
			AuthorizationTokenASP=new String("Bearer "+response2.asString());
	    }
	    @BeforeMethod
		public void beforeMethod() throws IOException
		{   //attach result to existing report
	    	extent=ExtentReportBase.getReports();
			
			//set base uri
			RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri");
			httpsRequest = RestAssured.given();
			softassert=new SoftAssert();
			
			
		}
		@AfterMethod
		public void afterMethod()
		{ 
			extent.flush();
			
		}
		
		@Test(priority=1)
		public void authrizationTokenGenration()
		{
			//Verify Authorization Token.
			test=extent.createTest("Authorization Token Genration");
			softassert.assertNotNull(AuthorizationTokenIssuer);
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
		public void TGAR001() throws IOException, ParseException 
		{     //Validate the response after providing valid header and valid request body
			
			
			        //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TGAR001.json");
				    
					
					
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
				
					httpsRequest.headers(HeaderTestCase.getValidHeader(AuthorizationTokenIssuer));
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("1"+response.asString());
					
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
						 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ " but we get "+response.jsonPath().get("responseCode"));
						 }
				      
				      
				    //3.validate http status code		
						softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
						if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
						{ 
							test.log(Status.PASS, "Test is PASS because we expect status code is "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" and actual is "+Integer.toString(response.getStatusCode()));
						
						}
						else
							{
							test.log(Status.FAIL, "Test is FAIL because we expect status code is "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
							
							}
				      softassert.assertAll();   
					
		}
		
		@Test(priority=3,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH001() throws IOException, ParseException 
		{          //Validate the response with ReqDatetime header value blank
			
			       //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH001.json");
				    
		           //ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("TH001"+response.asString());
					
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateForBlankReqDateTime(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is "+result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					
					
					softassert.assertAll();
					
		}
		
		@Test(priority=4,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH002() throws IOException, ParseException 
		{           //Validate the response with Sign header value blank
			
			        //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH002.json");
				    
					
					
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("TH002"+response.asString());
					
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateForBlankSign(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is "+result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					
					softassert.assertAll();
					
		}
		
		@Test(priority=5,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH003() throws IOException, ParseException 
		{   //Validate the response with Authorization header value blank 
			
			        //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH003.json");
				    
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
					
					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("TH003"+response.asString());
					
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateForBlankAuthrization(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is "+result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					softassert.assertAll();
					
		}
		
		@Test(priority=6,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH004() throws IOException, ParseException 
		{           //Validate the response without ReqDatetime header
			        
			       //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH004.json");
				    
					
				    //ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
			
					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("TH004"+response.asString());
					
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateWithoutReqDateTime(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is "+result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					softassert.assertAll();
					
		}
		
		@Test(priority=7,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH005() throws IOException, ParseException 
		{          //Validate the response without Sign Header.
			        
			       //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH005.json");
				    
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					

					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("5"+response.asString());
					System.out.println("5"+response.getStatusLine());
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateWithoutSign(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is "+result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					softassert.assertAll();
					
		}
		
		@Test(priority=8,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH006() throws IOException, ParseException 
		{           //Validate the response without Authorization header
			        
			         //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH006.json");
				    
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
				
					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("TH006"+response.asString());
					
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateWithoutAuthrization(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is "+result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					
					softassert.assertAll();
					
		}
		
		@Test(priority=9,dependsOnMethods= {"authrizationTokenGenration"})
		public void TH007() throws IOException, ParseException 
		{           //Validate the response after providing  all header  without value
			
			       //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TH007.json");
				    
					
					
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
				
					// add header information into request specification variable
					Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationTokenIssuer);
				    httpsRequest.headers(header);
					
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("TH007"+response.asString());
					
					
					//test
					//1.validate http status code		
					Pair result =  HeaderTestCase.validateAllHeaderBlank(response);
					softassert.assertTrue(result.isPass());
					if (result.isPass())
					{ 
						test.log(Status.PASS, "Test is PASS because we expect status code is " +result.getCode());
					
					}
					else
						{
						test.log(Status.FAIL, "Test is FAIL because we expect status code is "+result.getCode());
						
						}
					
					
					softassert.assertAll();
					
		}
	  
	
		@Test(priority=10,dependsOnMethods= {"authrizationTokenGenration"} )
		public void TGAR002() throws IOException, ParseException 
		{            //Validate the response after providing valid header of issuer ASP
			
		            //read data from json file
					jsonObject=UtilityClass.readDataFromJSONFile(dirPath+"\\Test_Data\\userManagement\\GetAllRoles\\TGAR002.json");
				    
					//ADD test case Description in test report
					test=extent.createTest("<h5>"+ jsonObject.get("TestCaseid")+"</h5>"+" :- "+(String) jsonObject.get("Test Case Description"));
					
				    //add header
					httpsRequest.headers(HeaderTestCase.getValidHeader(AuthorizationTokenASP));
					
					//get response
					response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getAllRolesEndPointUrl"));
					System.out.println("2"+response.asString());
					
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
						 test.log(Status.FAIL," We expect RESPONSE CODE is " +jsonObject.get("ExpectedResponseCode")+ " but we get "+response.jsonPath().get("responseCode"));
						 }
				      
				      
				    //3.validate http status code		
						softassert.assertEquals(Integer.toString(response.getStatusCode()),jsonObject.get("ExpectedHTTPStatusCode").toString());
						if (jsonObject.get("ExpectedHTTPStatusCode").toString().equals(Integer.toString(response.getStatusCode())))
						{ 
							test.log(Status.PASS, "Test is PASS because we expect status code is "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" and actual is "+Integer.toString(response.getStatusCode()));
						
						}
						else
							{
							test.log(Status.FAIL, "Test is FAIL because we expect status code is "+jsonObject.get("ExpectedHTTPStatusCode").toString() +" but we found "+Integer.toString(response.getStatusCode()));
							
							}
				      softassert.assertAll();   
					
		}
		
		
		
		
}
