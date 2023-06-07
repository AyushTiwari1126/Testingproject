package branch_management;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
//import Maven.WOWCard_API.Test_BaseClass;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BranchStatus {
	String dirPath = System.getProperty("user.dir");
	public ExtentReports extent;
	public ExtentSparkReporter spark;
	public ExtentTest test;
	public SoftAssert softassert;
	JSONObject jsonObject;
	RequestSpecification httpsRequest_Token, httpsRequest;
	Response response_Token, response;
	String[] headerInfo;
	String AuthorizationToken;

	public String getReqDatetimeUTC() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMYYYYHHmmss");
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		return dtf.format(now);
	}

	@BeforeMethod
	public void beforeMethod() throws IOException {
		extent = ExtentReportBase.getReports();

		// Set base uri
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
		httpsRequest_Token = RestAssured.given();

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//AutherizationToken//AuthToken.json");

		httpsRequest_Token.body(jsonObject.get("RequestBody").toString());

		// add header information into request specification variable
		Map<String, String> header = HeaderTestCase.headerforToken();
		httpsRequest_Token.headers(header);

		// Response Object
		response_Token = httpsRequest_Token.request(Method.POST,
				UtilityClass.getDataFromPF("GenerateTokenURL"));
		System.out.println(response_Token.asPrettyString());

		AuthorizationToken = "Bearer " + response_Token.asString();
		System.out.println("mytoken : " + AuthorizationToken);
	}

	@Test(priority = 1)
	public void Token_Generation() {

		test = extent.createTest("<h5>Generate Token for furthur Testcases</h5>");

		softassert.assertNotNull(AuthorizationToken);
		if (AuthorizationToken.isBlank()) {
			test.log(Status.FAIL, "Authorization or access token is genrated");
		} else {
			test.log(Status.PASS, "Authorization or access token is genrated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 2, dependsOnMethods = { "Token_Generation" })
	public void TBS0001() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// -----------Assertions-----------------

		// Assertions for the presence of all nodes
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
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().getString("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}
		
		// Assertion-3 : To validate the branchId 
		String[] s = response.jsonPath().getString("responseData.responseText").split(":");
		String ActualbranchId = s[1].trim();
		softassert.assertEquals(ActualbranchId, jsonObject.get("ExpectedbranchId"));
		if (ActualbranchId.equals(jsonObject.get("ExpectedbranchId"))) {
			test.log(Status.PASS,
					"Actual branchId - " + ActualbranchId + " is same as the Expected branchId");
		} else {
			test.log(Status.FAIL, "Test is Fail beacuse we expect the branchId - " + jsonObject.get("ExpectedbranchId")
					+ " but we get the branchId - " + ActualbranchId);
		}

		// Assertion-4 : To validate the branch status
		softassert.assertEquals(response.jsonPath().getString("responseData.status"), jsonObject.get("Expectedstatus"));
		if (response.jsonPath().getString("responseData.status").equals(jsonObject.get("Expectedstatus"))) {
			test.log(Status.PASS, "Actual branch status-" + response.jsonPath().getString("responseData.status")
					+ " is same as expected branch status");
		} else {
			test.log(Status.FAIL, "We expect the branch status-" + jsonObject.get("Expectedstatus")
					+ " but we get the branch status" + response.jsonPath().getString("responseData.status"));
		}

		// Assertion-5 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
// To validate the response after providing valid request body without any header

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
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
	public void TBS002() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// -------------Assertions--------------

		// Assertions for the prsence of all the nodes
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
		
		// Assertion-2 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		// Assertion-3 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		softassert.assertAll();
	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TBS003() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ---------------Assertions--------------------

		// Assertions for the prsence of all the nodes
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
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();

	}

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TBS004() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertions for the prsence of all the nodes
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
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();

	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TBS005() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertions for the prsence of all the nodes
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
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TBS006() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();
	}

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TBS007() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("7 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertions for the presence of all nodes
		String s2 = jsonObject.get("KeysForValidationInResponse").toString();
		String[] s3 = s2.replace("[", "").replace("]", "").replace('"', ' ').split(",");
		for (int i = 0; i < s3.length; i++) {
			softassert.assertTrue(response.asString().contains(s3[i].trim()));
			if (response.asString().contains(s3[i].trim())) {
				test.log(Status.PASS, "Response Body contains " + s3[i]);
			} else {
				test.log(Status.FAIL, "Response Body did not contains " + s3[i]);
			}
		}

		// Assertion-2 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();
	}

	@Test(priority = 16, dependsOnMethods = { "Token_Generation" })
	public void TBS010() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS010.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("10 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode"));
		}

		// Assertion-2 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();
	}

	@Test(priority = 17, dependsOnMethods = { "Token_Generation" })
	public void TBS011() throws IOException, ParseException {
// Validate response after providing valid headers with request body (takes all nodes as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//BranchStatus//TBS011.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("branchStatusEndPointUrl"));
		System.out.println("11 : " + response.asPrettyString());

// ----------Assertions----------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode"));
		}

		// Assertion-2 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();
	}
}