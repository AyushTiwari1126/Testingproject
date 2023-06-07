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

public class UpdateCardScheme {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentTest test;
	ExtentSparkReporter spark;
	JSONObject jsonObject;
	SoftAssert softassert;
	Random random = new Random();
	RequestSpecification httpsRequest_Token, httpsRequest, httpsRequest_GetCardSchemeList;
	Response response_Token, response, response_GetCardSchemeList;
	String[] headerInfo;
	String AuthorizationToken, cardSchemeID, newCardSchemeID;
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
	public void TUCS001() throws IOException, ParseException {
// To validate the response for cardSchemeId-14 after updating the cardScheme details

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS001.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");
		System.out.println("existingCardSchemeIDs : " + existingCardSchemeIDs);

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);
		

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("1 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : Validation for the presence of all the nodes
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

		// Assertion-2 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 4, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// To validate the response for valid request body with headers (takes ReqDatetime as blank) 

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH001.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH002.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH003.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH004.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));
		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH005.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH006.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TH007.json");

		// Add testcase id and description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Resposne Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
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
	public void TUCS002() throws IOException, ParseException {
// Validation for passing invalid cardSchemeId that is not present into the database

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS002.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TUCS003() throws IOException, ParseException {
// Validate response after change BinLength from 6 to 7 or more than 6

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS003.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TUCS004() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS004.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TUCS005() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS005.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TUCS006() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS006.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 16, dependsOnMethods = { "Token_Generation" })
	public void TUCS007() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS007.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("7 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 17, dependsOnMethods = { "Token_Generation" })
	public void TUCS008() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS008.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("8 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 18, dependsOnMethods = { "Token_Generation" })
	public void TUCS009() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS009.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("9 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 19, dependsOnMethods = { "Token_Generation" })
	public void TUCS010() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS010.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("10 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 20, dependsOnMethods = { "Token_Generation" })
	public void TUCS011() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS011.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("11 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 21, dependsOnMethods = { "Token_Generation" })
	public void TUCS012() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS012.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("12 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 22, dependsOnMethods = { "Token_Generation" })
	public void TUCS013() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS013.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		existingCardSchemeIDs = response_GetCardSchemeList.jsonPath().get("responseData.cardSchemeId");

		existingCardSchemeListSize = existingCardSchemeIDs.size();
		newCardSchemeID = existingCardSchemeIDs.get(random.nextInt(existingCardSchemeListSize));
		System.out.println("newCardSchemeID : " + newCardSchemeID);

		// Request Object
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("13 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 23, dependsOnMethods = { "Token_Generation" })
	public void TUCS014() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS014.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("14 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 24, dependsOnMethods = { "Token_Generation" })
	public void TUCS015() throws IOException, ParseException {
// Validate the response after change the bin from 6-digits to 7 or more digits

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//CardProductSetting//GetCardSchemeList//TUCS015.json");

		// add testcaseid and testcasedescription into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// add headersInfo
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.PUT, UtilityClass.getDataFromPF("updateCardSchemeEndPointUrl"));
		System.out.println("15 : " + response.asPrettyString());

// --------------Assertions---------------

		// Assertion-1 : To validate the Resposne code
		softassert.assertEquals(response.jsonPath().get("responseCode"), jsonObject.get("ExpectedResponseCode"));
		if (response.jsonPath().get("responseCode").equals(jsonObject.get("ExpectedResponseCode"))) {
			test.log(Status.PASS, "The actual responseCode- " + response.jsonPath().getByte("responseCode")
					+ " is same as the expected response code");
		} else {
			test.log(Status.FAIL, "We expect the responseCode - " + jsonObject.get("ExpectedResponseCode")
					+ " but we get the responseCode- " + response.jsonPath().get("responseCode"));
		}

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "The actual HTTP StatusCode- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP statusCode");
		} else {
			test.log(Status.FAIL, "We expect the HTTPStatusCode - " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the statusCode- " + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}
}