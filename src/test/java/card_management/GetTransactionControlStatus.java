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

public class GetTransactionControlStatus {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentSparkReporter spark;
	ExtentTest test;
	JSONObject jsonObject;
	SoftAssert softassert;
	RequestSpecification httpsRequest_Token, httpsRequest;
	Response response_Token, response;
	String[] headerInfo;
	String AuthorizationToken;
	
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
	public void AuthorizationToken_Testcase() throws IOException, ParseException {
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri_Token");
		httpsRequest_Token = RestAssured.given();

		// Read data from json file
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
			test.log(Status.FAIL, "Authorization token is not generated");
		} else {
			test.log(Status.PASS, "Authorization token is generated successfully");
		}
		softassert.assertAll();
	}
	
	@Test(priority = 2, dependsOnMethods = { "Token_Generation" })
	public void TGTCS001() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS001.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// ----------------Assertions--------------

		// Assertions for the presence of the nodes from Request body
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
		
		// Assertion-4 : To validate the CardId
		softassert.assertEquals(response.jsonPath().get("cardId"), jsonObject.get("ExpectedCardID"));
		if (response.jsonPath().get("cardId").equals(jsonObject.get("ExpectedCardID"))) {
			test.log(Status.PASS,
					"The actual cardId- " + response.jsonPath().get("cardId") + " is same as the expected cardId");
		} else {
			test.log(Status.FAIL, "We expect the cardId- " + jsonObject.get("ExpectedCardID") + " but we get "
					+ response.jsonPath().get("cardId"));
		}

		// Assertion-5 : To validate the TransactionType
		softassert.assertEquals(response.jsonPath().get("transactionType"), jsonObject.get("ExpectedtransactionType"));
		if (response.jsonPath().get("transactionType").equals(jsonObject.get("ExpectedtransactionType"))) {
			test.log(Status.PASS, "The actual transaction_Type - " + response.jsonPath().get("transactionType")
					+ " is same as the expected transaction_Type");
		} else {
			test.log(Status.FAIL, "We expect the transactionType - " + jsonObject.get("ExpectedtransactionType")
					+ " but we get " + response.jsonPath().get("transactionType"));
		}
		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
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
	public void TGTCS002() throws IOException, ParseException {
// Validation for CardId with no status set for transactionType

		// read data from testdata.json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS002.json");

		// Add testcase id and description into the reports
		test = extent
				.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
						+ jsonObject.get("Test Case Description") + "</h5>");
		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers Info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// ----------------Assertions-----------------

		// Assertions for the presence of nodes into the request body
		String s2 = jsonObject.get("KeysForValidationInResponse").toString();
		String[] s3 = s2.replace("[", " ").replace("]", " ").replace('"', ' ').split(",");
		for (int i = 0; i < s3.length; i++) {
			softassert.assertTrue(response.asString().contains(s3[i].trim()));
			if (response.asString().contains(s3[i].trim())) {
				test.log(Status.PASS, "Response Body contains " + s3[i]);
			} else {
				test.log(Status.FAIL, "Resposne Body contains " + s3[i]);
			}
		}

		// Assertion-2 : to validate the response code
		softassert.assertEquals(response.jsonPath().get("resposneCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().get("responseCode")
					+ " is same as the expected responseCode- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the responseCode- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTP StatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected responseCode- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the responseCode- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}

		// Assertion-4 : To validate the cardId for which we are requesting
		String[] s = response.jsonPath().getString("errorDetail.errorMessage[1]").split(":");
		softassert.assertEquals(s[1].trim(), jsonObject.get("ExpectedCardId"));
		if (s[1].trim().equals(jsonObject.get("ExpectedCardId"))) {
			test.log(Status.PASS, "The actual cardId- " + s[1].trim() + " is same as the expected cardId- "
					+ jsonObject.get("ExpectedCardId"));
		} else {
			test.log(Status.FAIL, "We expect the cardId- " + jsonObject.get("ExpectedCardId")
					+ " but we get the cardId- " + s[1].trim());
		}
		softassert.assertAll();
	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TGTCS003() throws IOException, ParseException {
// Validation for CardId with no status set for transactionType

		// read data from testdata.json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");
		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers Info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ----------------Assertions-----------------

		// Assertion-1 : to validate the response code
		softassert.assertEquals(response.jsonPath().get("resposneCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().get("responseCode")
					+ " is same as the expected responseCode- " + jsonObject.get("ExpectedResponseCode"));
		} else {
			test.log(Status.FAIL, "We expect the responseCode- " + jsonObject.get("ExpectedResponseCode")
					+ " but we get " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTP StatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected responseCode- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the responseCode- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TGTCS004() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS004.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

// ----------------Assertions--------------

		// Assertions for the presence of the nodes from Request body
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

		// Assertion-4 : To validate the CardId
		softassert.assertEquals(response.jsonPath().get("cardId"), jsonObject.get("ExpectedCardID"));
		if (response.jsonPath().get("cardId").equals(jsonObject.get("ExpectedCardID"))) {
			test.log(Status.PASS,
					"The actual cardId- " + response.jsonPath().get("cardId") + " is same as the expected cardId");
		} else {
			test.log(Status.FAIL, "We expect the cardId- " + jsonObject.get("ExpectedCardID") + " but we get "
					+ response.jsonPath().get("cardId"));
		}

		// Assertion-5 : To validate the TransactionType
		softassert.assertEquals(response.jsonPath().get("transactionType"), jsonObject.get("ExpectedtransactionType"));
		if (response.jsonPath().get("transactionType").equals(jsonObject.get("ExpectedtransactionType"))) {
			test.log(Status.PASS, "The actual transaction_Type - " + response.jsonPath().get("transactionType")
					+ " is same as the expected transaction_Type");
		} else {
			test.log(Status.FAIL, "We expect the transactionType - " + jsonObject.get("ExpectedtransactionType")
					+ " but we get " + response.jsonPath().get("transactionType"));
		}
		softassert.assertAll();
	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TGTCS005() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS005.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ----------------Assertions--------------

		// Assertions for the presence of the nodes from Request body
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

		// Assertion-4 : To validate the CardId
		softassert.assertEquals(response.jsonPath().get("cardId"), jsonObject.get("ExpectedCardID"));
		if (response.jsonPath().get("cardId").equals(jsonObject.get("ExpectedCardID"))) {
			test.log(Status.PASS,
					"The actual cardId- " + response.jsonPath().get("cardId") + " is same as the expected cardId");
		} else {
			test.log(Status.FAIL, "We expect the cardId- " + jsonObject.get("ExpectedCardID") + " but we get "
					+ response.jsonPath().get("cardId"));
		}

		// Assertion-5 : To validate the TransactionType
		softassert.assertEquals(response.jsonPath().get("transactionType"), jsonObject.get("ExpectedtransactionType"));
		if (response.jsonPath().get("transactionType").equals(jsonObject.get("ExpectedtransactionType"))) {
			test.log(Status.PASS, "The actual transaction_Type - " + response.jsonPath().get("transactionType")
					+ " is same as the expected transaction_Type");
		} else {
			test.log(Status.FAIL, "We expect the transactionType - " + jsonObject.get("ExpectedtransactionType")
					+ " but we get " + response.jsonPath().get("transactionType"));
		}
		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TGTCS006() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS006.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ----------------Assertions--------------

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
	public void TGTCS007() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS007.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ----------------Assertions--------------

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
	public void TGTCS008() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS008.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("8 : " + response.asPrettyString());

// ----------------Assertions--------------

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
	public void TGTCS009() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS009.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("9 : " + response.asPrettyString());

// ----------------Assertions--------------

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

	@Test(priority = 18, dependsOnMethods = { "Token_Generation" })
	public void TGTCS010() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS010.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("10 : " + response.asPrettyString());

// ----------------Assertions--------------

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
	public void TGTCS011() throws IOException, ParseException {
// To validate the status for the cardId from Active card state with Ecom transaction type

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//cardManagement//GetTransactionControlStatus//TGTCS011.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Body
		response = httpsRequest.request(Method.POST,
				UtilityClass.getDataFromPF("getTranactionControlStatusEndPointUrl"));
		System.out.println("11 : " + response.asPrettyString());

// ----------------Assertions--------------

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
}