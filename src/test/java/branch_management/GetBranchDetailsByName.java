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
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GetBranchDetailsByName {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentSparkReporter spark;
	ExtentTest test;
	SoftAssert softassert;
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
		response_Token = httpsRequest_Token.request(Method.POST, UtilityClass.getDataFromPF("GenerateTokenURL"));

		AuthorizationToken = "Bearer " + response_Token.asString();
		System.out.println("AuthorizationToken : " + AuthorizationToken);
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
	public void TBDN001() throws IOException, ParseException {
// Validate the response after providing valid header with valid request body

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add Headerkey-values
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// ---------------Assertions-----------------

		// Assertions for the presence of nodes into the response
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

		// Assertion-1 : To validate the branchName
		softassert.assertEquals(response.jsonPath().getString("responseData[0].branchName"),
				jsonObject.get("ExpectedBranchName"));

		if (response.jsonPath().getString("responseData[0].branchName").equals(jsonObject.get("ExpectedBranchName"))) {
			test.log(Status.PASS, "Expected branch name is same as Actual branch name - "
					+ response.jsonPath().getString("responseData[0].branchName"));
		} else {
			test.log(Status.FAIL,
					"Test is Fail because we expect the branch name - " + jsonObject.get("ExpectedBranchName")
							+ " but we get the branch name - "
							+ response.jsonPath().getString("responseData[0].branchName"));
		}

		// Assertion-2 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code");
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// Validate response after providing valid request body with valid header (takes ReqDatetime as blank)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("H1 : " + response.asPrettyString());

// --------------Assertions----------------

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
// Validate response after providing valid request body with valid header (takes Sign header as blank)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
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
// Validate response after providing valid request body with valid header (takes authorization as blank)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
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
// Validate response after providing valid request body with valid header (without ReqDatetime)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
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
// Validate response after providing valid request body with valid header (without Sign header)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
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
// Validate response after providing valid request body with valid header (without authorization header)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
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
// Validate response after providing valid request body without any header

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
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
	public void TBDN002() throws IOException, ParseException {
// Validate response after providing valid headers with request body (contains invalid branchName)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// ---------------Assertions--------------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
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

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TBDN003() throws IOException, ParseException {
// Validate response for valid header without any node in request body

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ---------------Assertions--------------------

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

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TBDN005() throws IOException, ParseException {
// Validate response after providing valid header with request body (takes both nodes as blank)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ---------------Assertions--------------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
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

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TBDN006() throws IOException, ParseException {
// Validate response after providing valid header with request body (contains spaces in branchName)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ---------------Assertions--------------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code");
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
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

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TBDN007() throws IOException, ParseException {
// Validate response after providing valid header with valid request body (contains max(20-characters) in branchName)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("7 : " + response.asPrettyString());

// ---------------Assertions--------------------

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

	@Test(priority = 16, dependsOnMethods = { "Token_Generation" })
	public void TBDN008() throws IOException, ParseException {
// Validate response after providing valid header with valid request body (contains max+1(21-characters) in branchName)

		// Read data from testdata.json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsByName//TBDN008.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchDetailsByNameEndPointUrl"));
		System.out.println("8 : " + response.asPrettyString());

// ---------------Assertions--------------------

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
}