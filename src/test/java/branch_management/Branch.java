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

public class Branch {
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
			test.log(Status.FAIL, "Authorization or access token is genrated");
		} else {
			test.log(Status.PASS, "Authorization or access token is genrated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 2, dependsOnMethods = { "Token_Generation" })
	public void TB001() throws IOException, ParseException {
// Validate the response after providing valid header with valid request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB001.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// ----------------Assertions-----------------

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
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().getString("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
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
		// To validate the response for valid request body with headers (takes
		// ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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
		// To validate the response after providing valid request body with headers

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH002.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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
		// To validate the response for valid request body with headers (takes
		// ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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

	@Test(priority = 6, dependsOnMethods = { "Token_Generation" })
	public void TH004() throws IOException, ParseException {
		// To validate the response for valid request body with headers (takes
		// ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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

	@Test(priority = 7, dependsOnMethods = { "Token_Generation" })
	public void TH005() throws IOException, ParseException {
		// To validate the response for valid request body with headers (takes
		// ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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

	@Test(priority = 8, dependsOnMethods = { "Token_Generation" })
	public void TH006() throws IOException, ParseException {
		// To validate the response for valid request body with headers (takes
		// ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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

	@Test(priority = 9, dependsOnMethods = { "Token_Generation" })
	public void TH007() throws IOException, ParseException {
		// To validate the response for valid request body with headers (takes
		// ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
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

	@Test(priority = 10, dependsOnMethods = { "Token_Generation" })
	public void TB002() throws IOException, ParseException {
// To validate the response after providing all the data with already existing branchName

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB002.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 10, dependsOnMethods = { "Token_Generation" })
	public void TB003() throws IOException, ParseException {
// To validate for all the nodes in requestBody are mandatory or not by passing nothing into request body except "accessAllBranch"

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB003.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TB004() throws IOException, ParseException {
// To validate the response when passing min-1 (4-characters) into branchName

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB004.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TB005() throws IOException, ParseException {
// To validate the response when passing max+1 (21-characters) into branchName

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB005.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TB006() throws IOException, ParseException {
// Validate the response after providing more than max(12) limit of characters into the IssuerID

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB006.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TB007() throws IOException, ParseException {
// To validate the length for state by passing the more than (20-characters) into the state

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB007.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("7 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TB008() throws IOException, ParseException {
// To validate the length for countryCode by passing the more than (2 digit ISO standard code) from "IN" to "IND"

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB008.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("8 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 16, dependsOnMethods = { "Token_Generation" })
	public void TB009() throws IOException, ParseException {
// To validate the length for state by passing the more than (20-characters) into the city

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB009.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("9 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 17, dependsOnMethods = { "Token_Generation" })
	public void TB010() throws IOException, ParseException {
// Validate the response for valid headers with passing integer type values into all the nodes

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB010.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("10 : " + response.asPrettyString());

// ------------Assertions-----------------

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
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().getString("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 18, dependsOnMethods = { "Token_Generation" })
	public void TB011() throws IOException, ParseException {
// Validate the response by passing the integer type values in all nodes except in 'accessAllBranch'

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB011.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("11 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 19, dependsOnMethods = { "Token_Generation" })
	public void TB012() throws IOException, ParseException {
// Validate the response for passing only spaces into the values of all nodes

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB012.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("12 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().getString("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 20, dependsOnMethods = { "Token_Generation" })
	public void TB013() throws IOException, ParseException {
// Validate the response after pass any other character except "Y" and "N" into "accessAllBranch" node

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB013.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("13 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 21, dependsOnMethods = { "Token_Generation" })
	public void TB014() throws IOException, ParseException {
// To validate the response for phoneNo after passing more than 12 digits into it

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB014.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("14 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 22, dependsOnMethods = { "Token_Generation" })
	public void TB015() throws IOException, ParseException {
// To validate the response for valid request body contains "YN" into the value of "accessAllBranch" node

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB015.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("15 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 23, dependsOnMethods = { "Token_Generation" })
	public void TB016() throws IOException, ParseException {
// To validate the response for address1 & address2 after passing the value contains more than 50 characters into it

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB016.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("16 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 24, dependsOnMethods = { "Token_Generation" })
	public void TB017() throws IOException, ParseException {
// To validate the response for contactPerson after passing the value contains more than 30 characters into it

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB017.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("17 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-3 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 25, dependsOnMethods = { "Token_Generation" })
	public void TB018() throws IOException, ParseException {
// To validate the response for after passing nothing into the values.

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//Branch//TB018.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("branchEndPointUrl"));
		System.out.println("18 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().getString("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatus code
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - " + response.getStatusCode());
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}
}