package cardProductSetting;

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

public class GetCardSchemeList {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentSparkReporter spark;
	ExtentTest test;
	SoftAssert softassert;
	JSONObject jsonObject_Issuer, jsonObject_ASP;
	RequestSpecification httpsRequest_Issuer, httpsRequest_ASP, httpsRequest;
	Response response_Issuer, response_ASP, response;
	String[] headerInfo;
	String AuthorizationToken_Issuer, AuthorizationToken_ASP;

	@BeforeMethod
	public void beforeMethod() throws IOException {
		extent = ExtentReportBase.getReports();
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri");
		httpsRequest = RestAssured.given();
		softassert = new SoftAssert();
	}

	@AfterMethod
	public void afterMethod() {
		extent.flush();
	}

	@BeforeTest
	public void AuthorizationToken_TestCase() throws IOException, ParseException {
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri_Token");
		httpsRequest_Issuer = RestAssured.given();
		httpsRequest_ASP = RestAssured.given();

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//AutherizationToken//AuthToken.json");
		jsonObject_ASP = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//AutherizationToken//AuthTokenASP.json");

		// Request Body
		httpsRequest_Issuer.body(jsonObject_Issuer.get("RequestBody").toString());
		httpsRequest_ASP.body(jsonObject_ASP.get("RequestBody").toString());

		// add header information into request specification variable
		Map<String, String> header = HeaderTestCase.headerforToken();
		httpsRequest_Issuer.headers(header);
		httpsRequest_ASP.headers(header);

		// Response Object
		response_Issuer = httpsRequest_Issuer.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));
		response_ASP = httpsRequest_ASP.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));

		AuthorizationToken_Issuer = "Bearer " + response_Issuer.asString();
		AuthorizationToken_ASP = "Bearer " + response_ASP.asString();
		System.out.println("Issuer1 : " + AuthorizationToken_Issuer);
		System.out.println("ASP : " + AuthorizationToken_ASP);
	}

	@Test(priority = 1)
	public void Token_Generation_Issuer1() {
		test = extent.createTest("<h5>Generate Token for TestCase-TGCSL001</h5>");

		softassert.assertNotNull(AuthorizationToken_Issuer);
		if (AuthorizationToken_Issuer.isBlank()) {
			test.log(Status.FAIL, "Authorization or access token is generated");
		} else {
			test.log(Status.PASS, "Authorization or access token is generated successfully");
		}
		softassert.assertAll();
	}
	
	@Test(priority = 2, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TGCSL001() throws IOException, ParseException {
// Validate the responseData for Issuer1 with valid headers

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TGCSL001.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// ---------------Assertions-----------------

		// Assertions for the presence of all the nodes
		String s2 = jsonObject_Issuer.get("KeysForValidationInResponse").toString();
		String[] s3 = s2.replace("[", " ").replace("]", " ").replace('"', ' ').split(",");
		for (int i = 0; i < s3.length; i++) {
			softassert.assertTrue(response.asString().contains(s3[i].trim()));
			if (response.asString().contains(s3[i].trim())) {
				test.log(Status.PASS, "Response Body contains " + s3[i]);
			} else {
				test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
			}
		}

		// Assertion-2 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject_Issuer.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject_Issuer.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject_Issuer.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject_Issuer.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()),
				jsonObject_Issuer.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject_Issuer.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject_Issuer.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject_Issuer.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 3)
	public void Token_Generation_ASP() {
		test = extent.createTest("<h5>Generate Token for TestCase-TGCSL002</h5>");

		softassert.assertNotNull(AuthorizationToken_ASP);
		if (AuthorizationToken_ASP.isBlank()) {
			test.log(Status.FAIL, "Authorization or access token is generated");
		} else {
			test.log(Status.PASS, "Authorization or access token is generated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 4, dependsOnMethods = { "Token_Generation_ASP" })
	public void TGCSL002() throws IOException, ParseException {
// Validate the responseData for Issuer1 with valid headers

		// Read data from json file
		jsonObject_ASP = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TGCSL002.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject_ASP.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_ASP.get("Test Case Description") + "</h5>");

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken_ASP);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// ---------------Assertions-----------------

		// Assertions for the presence of all the nodes
		String s2 = jsonObject_ASP.get("KeysForValidationInResponse").toString();
		String[] s3 = s2.replace("[", " ").replace("]", " ").replace('"', ' ').split(",");
		for (int i = 0; i < s3.length; i++) {

			softassert.assertTrue(response.asString().contains(s3[i].trim()));
			if (response.asString().contains(s3[i].trim())) {
				test.log(Status.PASS, "Response Body contains " + s3[i]);
			} else {
				test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
			}
		}

		// Assertion-2 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject_ASP.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject_ASP.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject_ASP.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject_ASP.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()),
				jsonObject_ASP.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject_ASP.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject_ASP.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject_ASP.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 5, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH001() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H1 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 6, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH002() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H2 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateForBlankSign(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 7, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH003() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H3 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateForBlankAuthrization(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 8, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH004() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H4 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateWithoutReqDateTime(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 9, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH005() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H5 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateWithoutSign(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 10, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH006() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H6 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateWithoutAuthrization(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation_Issuer1" })
	public void TH007() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject_Issuer = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject_Issuer.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject_Issuer.get("Test Case Description") + "</h5>");

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken_Issuer);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("H7 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		Pair result = HeaderTestCase.validateAllHeaderBlank(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}
}