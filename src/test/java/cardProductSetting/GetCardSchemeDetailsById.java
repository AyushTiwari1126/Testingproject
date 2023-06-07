package cardProductSetting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

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

public class GetCardSchemeDetailsById {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentSparkReporter spark;
	ExtentTest test;
	JSONObject jsonObject;
	SoftAssert softassert;
	Random random = new Random();
	RequestSpecification httpsRequest, httpsRequest_Token, httpsRequest_GetCardSchemeList;
	Response response, response_Token, response_GetCardSchemeList;
	String[] headerInfo;
	String AuthorizationToken, newCardSchemeID;
	ArrayList<String> existingCardSchemeIDs;
	int existingCardSchemeListSize;

	public String getNewReqBody(String oldReqBody) {
		String newRequestBody = oldReqBody.replace("SystemGeneratedID", newCardSchemeID);
		System.out.println("newRequestBody : " + newRequestBody);
		return newRequestBody;
	}

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

// -------------Assertion------------

		// Assertion for the response generation
		softassert.assertNotNull(response_GetCardSchemeList);
		if (response_GetCardSchemeList.asString().isBlank()) {
			test.log(Status.FAIL, "Response is not generated");
		} else {
			test.log(Status.PASS, "Response is generated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TGCDI001() throws IOException, ParseException {
// Validate the response for valid cardSchemeId with valid headers

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TGCDI001.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 4, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

//		httpsRequest.body(jsonObject.get("RequestBody").toString());

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 5, dependsOnMethods = { "Token_Generation" })
	public void TH002() throws IOException, ParseException {
// To validate the response after providing valid request body with headers (takes sign as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 6, dependsOnMethods = { "Token_Generation" })
	public void TH003() throws IOException, ParseException {
// To validate the response for valid request body with header (takes authorization as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 7, dependsOnMethods = { "Token_Generation" })
	public void TH004() throws IOException, ParseException {
// To validate the response for valid request body with header (without reqDatetime)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 8, dependsOnMethods = { "Token_Generation" })
	public void TH005() throws IOException, ParseException {
// To validate the response for valid request body with header (without sign header)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 9, dependsOnMethods = { "Token_Generation" })
	public void TH006() throws IOException, ParseException {
// To validate the response for valid request body with header (without authorization header)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 10, dependsOnMethods = { "Token_Generation" })
	public void TH007() throws IOException, ParseException {
// To validate the response after providing valid request body without any header value

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(
						dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
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

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TGCDI002() throws IOException, ParseException {
// To validate the response after passing the invalid cardSchemeId (which is not present into the database)

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TGCDI002.json");

		// Add testcaseId and testcase Description into the extent reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// -------------Assertions-------------

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

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TGCDI003() throws IOException, ParseException {
// To validate the response after pass spaces only into the CardSchemeId

		// read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TGCDI003.json");

		// add testcaseid and testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers Info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response body
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// ------------Assertions-------------

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

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TGCDI004() throws IOException, ParseException {
// To validate the response for kept the cardSchemeId as blank

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TGCDI004.json");

		// add testcaseid and testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers Info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response body
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

// ---------------Assertions----------------

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

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TGCDI005() throws IOException, ParseException {
// To validate the response after passing the cardSchemeId of ASP with token of Issuer1

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TGCDI005.json");

		// add testcaseid and testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers Info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response body
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// ---------------Assertions----------------

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

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TGCDI006() throws IOException, ParseException {
// Validate the response after pass nothing into the request body

		// Read data from json file
		jsonObject = UtilityClass.readDataFromJSONFile(
				dirPath + "//Test_Data//CardProductSetting//GetCardSchemeDetailsById//TGCDI006.json");

		// add testcaseid and testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request body
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headers Info
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response body
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("getCardSchemeDetailsByIdEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// ---------------Assertions----------------

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
