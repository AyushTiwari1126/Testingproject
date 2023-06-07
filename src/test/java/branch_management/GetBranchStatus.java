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

public class GetBranchStatus {
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
	public void Authorization_Token() throws IOException, ParseException {
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
		System.out.println(AuthorizationToken);

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
	
	@Test(priority=2, dependsOnMethods = { "Token_Generation" })
	public void TGBS001() throws IOException, ParseException {
// Validate the response after providing valid header with valid request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TGBS001.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());
		
// --------------Assertions------------------
		
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
					+ response.jsonPath().getByte("responseCode"));
		} else {
			test.log(Status.FAIL,
					"Test is Fail because we expect the response code - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the branch status
		softassert.assertEquals(response.jsonPath().getString("responseData.status"), jsonObject.get("Expectedstatus"));
		if (response.jsonPath().getString("responseData.status").equals(jsonObject.get("Expectedstatus"))) {
			test.log(Status.PASS, "Actual branch status-" + response.jsonPath().getString("responseData.status")
					+ " is same as expected branch status");
		} else {
			test.log(Status.FAIL, "We expect the branch status-" + jsonObject.get("Expectedstatus")
					+ " but we get the branch status" + response.jsonPath().getString("responseData.status"));
		}

		// Assertion-4 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - "
					+ Integer.toString(response.getStatusCode()));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH001.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		httpsRequest.header(UtilityClass.getDataFromPF("HeaderKey"), UtilityClass.getDataFromPF("HeaderValue"));

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H1 : " + response.asPrettyString());

// -----------Assertions--------------

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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH002.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H2 : " + response.asPrettyString());

// --------------Assertions----------------

		// Assertion-1 : To validate the HTTPStatusCode
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH003.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H3 : " + response.asPrettyString());

// -----------------Assertions----------------

		// Assertion-1 : To validate the HTTPStatusCode
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH004.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H4 : " + response.asPrettyString());

// ------------Assertions----------------

		// Assertion-1 : To validate the HTTPStatusCode
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
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
// Validate the response after providing valid header with valid request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH005.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H5 : " + response.asPrettyString());

// --------------Assertions-------------------

		// Assertion-1 : To validate the HTTPStatusCode
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
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
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH006.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H6 : " + response.asPrettyString());

// ---------------Assertions--------------

		// Assertion-1 : To validate the HTTPStatusCode
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
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

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TH007.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("H7 : " + response.asPrettyString());

// ------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatusCode
		Pair result = HeaderTestCase.validateForBlankReqDateTime(response);
		softassert.assertTrue(result.isPass());
		if (result.isPass()) {
			test.log(Status.PASS, "Test is PASS because we expect status code - " + result.getCode());
		} else {
			test.log(Status.FAIL, "Test is FAIL because we expect status code - " + result.getCode());
		}

		softassert.assertAll();
	}

	@Test(priority = 10, dependsOnMethods = { "Token_Generation" })
	public void TGBS002() throws IOException, ParseException {
// Valiadte the response after providing valid headers with request body (pass invalid branchId)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TGBS002.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// ----------------Assertions-----------------

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
					+ response.jsonPath().get("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - "
					+ Integer.toString(response.getStatusCode()));
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		softassert.assertAll();

	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TGBS003() throws IOException, ParseException {
// Validate the response for valid headers with no data present in request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TGBS003.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ----------------Assertions-----------------

		// Assertion-1 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - "
					+ Integer.toString(response.getStatusCode()));
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TGBS005() throws IOException, ParseException {
// Validate response after providing vald header with passing spaces only in branchId

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TGBS005.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ----------------Assertions-----------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().get("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - "
					+ Integer.toString(response.getStatusCode()));
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TGBS006() throws IOException, ParseException {
// Validate response after providing vald header with request body (pass blank branchId)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchStatus//TGBS006.json");

		// Add testcaseid & testcase description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getBranchStatusEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ----------------Assertions-----------------

		// Assertion-1 : To validate the response code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "Expected Response code is same as actual response code - "
					+ response.jsonPath().get("responseCode"));
		} else {
			test.log(Status.FAIL, "Test is Fail because we expect " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatuscode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Expected Status code is same as actual status code - "
					+ Integer.toString(response.getStatusCode()));
		} else {
			test.log(Status.FAIL, "Test is Fail beacause we expect " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}
}