package cardProductSetting;

import java.io.IOException;
import java.util.ArrayList;
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

public class AddCardScheme {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentSparkReporter spark;
	ExtentTest test;
	JSONObject jsonObject;
	SoftAssert softassert;
	RequestSpecification httpsRequest, httpsRequest_Token, httpsRequest_GetCardSchemeList;
	Response response, response_Token, response_GetCardSchemeList;
	String[] headerInfo;
	String AuthorizationToken;
	ArrayList<String> existingCardSchemeIDs;

	@BeforeMethod
	public void beforeMethod() throws IOException {
		extent = ExtentReportBase.getReports();

		// Set base uri
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri");
		httpsRequest = RestAssured.given();
		httpsRequest_GetCardSchemeList = RestAssured.given();
		softassert = new SoftAssert();
	}

	@AfterMethod
	public void afterMethod() {
		extent.flush();
	}

	@BeforeTest
	public void AuthorizationToken_TestCase() throws IOException, ParseException {
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri_Token");
		httpsRequest_Token = RestAssured.given();

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//AutherizationToken//AuthToken.json");

		httpsRequest_Token.body(jsonObject.get("RequestBody").toString());

		// add header information into request specification variable
		Map<String, String> header = HeaderTestCase.headerforToken();
		httpsRequest_Token.headers(header);

		// Response Object
		response_Token = httpsRequest_Token.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));

		AuthorizationToken = "Bearer " + response_Token.asString();
		System.out.println("Token : " + AuthorizationToken);
	}

	@Test(priority = 1)
	public void Token_Generation() {
		test = extent.createTest("<h5>Generate Token for furthur Testcases</h5>");

		softassert.assertNotNull(AuthorizationToken);
		if (AuthorizationToken.isBlank()) {
			test.log(Status.FAIL, "Authorization token is generated");
		} else {
			test.log(Status.PASS, "Authorization token is generated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 2, dependsOnMethods = { "Token_Generation" })
	public void GetCardSchemeList() throws IOException, ParseException {
// Get all card Scheme list details

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TGCSL001.json");

		// Add TestCaseid & testcase description into reports
		test = extent.createTest("<h5>GetCardSchemeList API</h5>");

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest_GetCardSchemeList.headers(header);

		// Response Object
		response_GetCardSchemeList = httpsRequest_GetCardSchemeList.request(Method.POST,
				UtilityClass.getDataFromPF("getCardSchemeListEndPointUrl"));
		System.out.println("GetCardSchemeList : " + response_GetCardSchemeList.asPrettyString());
		
// -------Assertion-------
		
		// Assertion for the response generation
		softassert.assertNotNull(response_GetCardSchemeList);
		if (response_GetCardSchemeList.asString().isBlank()) {
			test.log(Status.FAIL, "Response is not generated");
		} else {
			test.log(Status.PASS, "Response is generated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 2)
	public void TACS001() throws IOException, ParseException {
// To validate the response after passing the valid data into the request body

		// read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS001.json");

		// Add TestCaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// ---------------Assertions-----------------

		// Assertions for the presence of all the nodes
		String s2 = jsonObject.get("KeysForValidationInResponse").toString();
		String[] s3 = s2.replace("[", " ").replace("]", " ").replace('"', ' ').split(",");
		for (int i = 0; i < s3.length; i++) {

			softassert.assertTrue(response.asString().contains(s3[i].trim()));
			if (response.asString().contains(s3[i].trim())) {
				test.log(Status.PASS, "Response Body contains " + s3[i]);
			} else {
				test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
			}
		}

//		// Assertion-1 : To validate for the generation of new cardSchemeId
//		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
//		softassert.assertTrue(existingCardSchemeIDs.contains(response.jsonPath().get("responseData.cardSchemeId")));
//		if(existingCardSchemeIDs.contains(response.jsonPath().get("responseData.cardSchemeId"))) {
//			test.log(Status.PASS, "The new cardSchemeId-"+response.jsonPath().get("responseData.cardSchemeId")+ " is added successfully");
//		} else {
//			test.log(Status.FAIL, "The new cardSchemeId-" + response.jsonPath().get("responseData.cardSchemeId")
//					+ " is not generated");
//		}
		
		// Assertion-2 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
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

	@Test(priority = 4, dependsOnMethods = { "Token_Generation" })
	public void TH002() throws IOException, ParseException {
// To validate the response after providing valid request body with headers (takes sign as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("H2 : " + response.asPrettyString());

// ------------Assertions-----------------

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

	@Test(priority = 5, dependsOnMethods = { "Token_Generation" })
	public void TH003() throws IOException, ParseException {
// To validate the response for valid request body with header (takes authorization as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("H3 : " + response.asPrettyString());

// ------------Assertions-----------------

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

	@Test(priority = 6, dependsOnMethods = { "Token_Generation" })
	public void TH004() throws IOException, ParseException {
// To validate the response for valid request body with header (without reqDatetime)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("H4 : " + response.asPrettyString());

// -----------------Assertions------------------

		// Assertion-1 : To validate the HttpStatus code
		Pair result = HeaderTestCase.validateWithoutReqDateTime(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 7, dependsOnMethods = { "Token_Generation" })
	public void TH005() throws IOException, ParseException {
// To validate the response for valid request body with header (without sign header)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("H5 : " + response.asPrettyString());

// ------------------Assertions------------------

		// Assertion-1 : To validate the HTTPStatuscode
		Pair result = HeaderTestCase.validateWithoutSign(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 8, dependsOnMethods = { "Token_Generation" })
	public void TH006() throws IOException, ParseException {
// To validate the response for valid request body with header (without authorization header)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("H6 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatuscode
		Pair result = HeaderTestCase.validateWithoutAuthrization(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}
		softassert.assertAll();
	}

	@Test(priority = 9, dependsOnMethods = { "Token_Generation" })
	public void TH007() throws IOException, ParseException {
// To validate the response after providing valid request body without any header value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("H7 : " + response.asPrettyString());

// ------------Assertions-----------------

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

	@Test(priority = 10, dependsOnMethods = { "Token_Generation" })
	public void TACS002() throws IOException, ParseException {
// To validate that the generated cardSchemeId is unique or not same as any of the existing cardSchemeId

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS002.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertions for the presence of all the nodes
		String s2 = jsonObject.get("KeysForValidationInResponse").toString();
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
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TACS003() throws IOException, ParseException {
// Validate the response for providing all the data with already existing cardSchemeDesc

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS003.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertions for the presence of all the nodes
		String s2 = jsonObject.get("KeysForValidationInResponse").toString();
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
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TACS004() throws IOException, ParseException {
// To validate the response when passing max+1 (21-characters) into branchName

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS004.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TACS005() throws IOException, ParseException {
// To validate for all the nodes in requestBody are mandatory or not by passing few nodes into the request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS005.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TACS006() throws IOException, ParseException {
// Validation for the response after providing the cardScheme with 3-digit value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS006.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TACS007() throws IOException, ParseException {
// To validate the response when we pass the  cardNextValue smaller than the lowRange value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS007.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("7 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 16, dependsOnMethods = { "Token_Generation" })
	public void TACS008() throws IOException, ParseException {
// To validate the response when we pass the  cardNextValue is greater than the highRange value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS008.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("8 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 17, dependsOnMethods = { "Token_Generation" })
	public void TACS009() throws IOException, ParseException {
// To validate the checkDigitScheme value other than 'MOD10' in request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS009.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("9 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 18, dependsOnMethods = { "Token_Generation" })
	public void TACS010() throws IOException, ParseException {
// Validation for the response when passing the Bin length (greater than value-6)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS010.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("10 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 19, dependsOnMethods = { "Token_Generation" })
	public void TACS011() throws IOException, ParseException {
// Validate the response after providing the max+1 (20) in cardNoLength value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS011.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("11 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 20, dependsOnMethods = { "Token_Generation" })
	public void TACS012() throws IOException, ParseException {
// To validate the response after passing The lowRangePostion value smaller than the Bin length

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS012.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("12 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 21, dependsOnMethods = { "Token_Generation" })
	public void TACS013() throws IOException, ParseException {
// To validate response after passing the same values in lowRange and highRange

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS013.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("13 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 22, dependsOnMethods = { "Token_Generation" })
	public void TACS014() throws IOException, ParseException {
// To validate the response after providing the highRange value smaller than the lowRange value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS014.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("14 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 23, dependsOnMethods = { "Token_Generation" })
	public void TACS015() throws IOException, ParseException {
// Validation for the response when we pass the settlementBin value with max+1 (7-digits)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS015.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("15 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 24, dependsOnMethods = { "Token_Generation" })
	public void TACS016() throws IOException, ParseException {
// Validate the response after providing the cardNoLength smaller than the sum of Bin  and digits of lowRange & highRange

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS016.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("16 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 25, dependsOnMethods = { "Token_Generation" })
	public void TACS017() throws IOException, ParseException {
// To validate the response after passing the spaces only in the values of all nodes

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS017.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("17 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 26, dependsOnMethods = { "Token_Generation" })
	public void TACS018() throws IOException, ParseException {
// To validate response when passing all blank values into the request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS018.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("18 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual response code- " + response.jsonPath().get("responseCode")
					+ " is same as the expected response code- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the response code- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 27, dependsOnMethods = { "Token_Generation" })
	public void TACS019() throws IOException, ParseException {
// Validate the response for after passing the empty request body without any node

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//AddCardScheme//TACS019.json");

		// add TestCaseid and testcase Description for reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("addCardSchemeEndPointUrl"));
		System.out.println("19 : " + response.asPrettyString());

// ---------------Assertions---------------

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}
}