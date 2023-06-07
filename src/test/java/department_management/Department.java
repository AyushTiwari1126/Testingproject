package department_management;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
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

public class Department {
	String dirPath = System.getProperty("user.dir");
	ExtentReports extent;
	ExtentTest test;
	ExtentSparkReporter spark;
	JSONObject jsonObject;
	SoftAssert softassert;
	Random random = new Random();
	RequestSpecification httpsRequest_Token, httpsRequest, httpsRequest_GetAllDept;
	Response response_Token, response, response_GetAllDept;
	String[] headerInfo;
	String AuthorizationToken, departmentId, departmentDesc;
	ArrayList<String> existingDeptId, existingDeptDesc;

	public String getReqDatetimeUTC() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMYYYYHHmmss");
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		return dtf.format(now);
	}

	public String getDepartmentId() {
		random = new Random();
		departmentId = Integer.toString(random.nextInt(100));
		return departmentId;
	}

	public String getDepartmentIdstartwith0() {
		random = new Random();
		String getDepartmentIdstartwith0 = "0" + Integer.toString(random.nextInt(9));
		return getDepartmentIdstartwith0;
	}

	public String getDepartmentIdwith2AlphaChars() {
		random = new Random();
		String departmentIdwith2AlphaChars = RandomStringUtils.randomAlphabetic(2);
		return departmentIdwith2AlphaChars;
	}

	public String getDepartmentDesc() {
		random = new Random();
		String departmentDesc = RandomStringUtils.randomAlphabetic(30);
		return departmentDesc;
	}

	public String getDepartmentDesc31() {
		random = new Random();
		String departmentDesc31 = RandomStringUtils.randomAlphabetic(31);
		return departmentDesc31;
	}

	public String getNewReqBody(String oldReqBody, String d2) {
		String newRequestBody = oldReqBody.replace("System Generated DepartmentId", d2)
				.replace("System Generated DepartmentDesc", getDepartmentDesc());
		System.out.println("newRequestBody : " + newRequestBody);
		return newRequestBody;
	}

	public String getNewReqBodyforDeptIdstartwith0(String oldReqBody, String d2) {
		String getNewReqBodyforDeptIdstartwith0 = oldReqBody
				.replace("System Generated DepartmentId", getDepartmentIdstartwith0())
				.replace("System Generated DepartmentDesc", getDepartmentDesc());
		System.out.println("getNewReqBodyforDeptIdstartwith0 : " + getNewReqBodyforDeptIdstartwith0);
		return getNewReqBodyforDeptIdstartwith0;
	}

	public String getNewReqBodyOnlyforUniqueDeptId(String oldReqBody, String d2) {
		String getNewReqBodyOnlyforUniqueDeptId = oldReqBody.replace("System Generated DepartmentId", d2);
		System.out.println("getNewReqBodyOnlyforUniqueDeptId : " + getNewReqBodyOnlyforUniqueDeptId);
		return getNewReqBodyOnlyforUniqueDeptId;
	}

	public String getNewReqBodyforDeptDesc31Chars(String oldReqBody, String d2) {
		String getNewReqBodyforDeptDesc31Chars = oldReqBody.replace("System Generated DepartmentId", d2)
				.replace("System Generated DepartmentDesc", getDepartmentDesc31());
		System.out.println("getNewReqBodyforDeptDesc31Chars : " + getNewReqBodyforDeptDesc31Chars);
		return getNewReqBodyforDeptDesc31Chars;
	}
	
	public String getNewReqBodyforDeptIdwith2AlphaChars(String oldReqBody, String d2) {
		String getNewReqBodyforDeptIdwith2AlphaChars = oldReqBody
				.replace("System Generated DepartmentId", getDepartmentIdwith2AlphaChars())
				.replace("System Generated DepartmentDesc", getDepartmentDesc());
		System.out.println("getNewReqBodyforDeptIdwith2AlphaChars : " + getNewReqBodyforDeptIdwith2AlphaChars);
		return getNewReqBodyforDeptIdwith2AlphaChars;
	}

	@BeforeMethod
	public void BeforeMethod() throws IOException {
		extent = ExtentReportBase.getReports();

		// Set base uri
		RestAssured.baseURI = UtilityClass.getDataFromPF("baseuri");
		httpsRequest = RestAssured.given();
		httpsRequest_GetAllDept = RestAssured.given();
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
	
	@Test(priority=1)
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
	public void GetAllDepartment() throws IOException, ParseException {
// Validate response after providing valid headers with valid request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//GetAllDept.json");

		// Add testcaseid & descripton into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest_GetAllDept.headers(header);

		// Response Object
		response_GetAllDept = httpsRequest_GetAllDept.request(Method.POST,
				UtilityClass.getDataFromPF("getAllDepartmentEndPointUrl"));
		System.out.println("GetAllDepartment : " + response_GetAllDept.asPrettyString());

// ---------------Assertions-----------------

		// Assertion-1 : To validate the response
		softassert.assertNotNull(response_GetAllDept);
		if (response_GetAllDept.asString().isBlank()) {
			test.log(Status.FAIL, "Response is not generated");
		} else {
			test.log(Status.PASS, "Response is generated successfully");
		}
		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TD001() throws IOException, ParseException {
// Validate response after providing valid headers with valid request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD001.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		System.out.println("ExistingDeptIDsfromGetAllDept_response : "
				+ response_GetAllDept.jsonPath().get("responseData.departmentId"));

//		System.out.println("ExistingDeptDescfromGetAllDept_response : " + response_GetAllDept.jsonPath().get("responseData.departmentDesc"));
//		System.out.println(response_GetAllDept.jsonPath().get("responseData.departmentId").getClass().getSimpleName());

		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();

//		for (int i = 0; i < existingDeptId.size(); i++) {
//			boolean isPresent = existingDeptId.get(i).contains(d2);
//			if (isPresent) {
//				d2 = getDepartmentId();
//			} else {
//				System.out.println("d2 :" + d2);
//				httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString()));
//				break;
//			}
//		}

		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
//		System.out.println("d2 : " + d2);
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Request Object
//		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 3, dependsOnMethods = { "Token_Generation" })
	public void TH001() throws IOException, ParseException {
// Validate the response after providing valid request body with header (contains ReqDatetime as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TH001.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getHeaderForBlankReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("H1 : " + response.asPrettyString());

// ------------------Assertions------------------

		// Assertion-1 : To validate the HTTPStatuscode
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
// Validate the response after providing valid request body with header (contains Sign as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TH002.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getHeaderForBlankSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("H2 : " + response.asPrettyString());

// ------------------Assertions------------------

		// Assertion-1 : To validate the HTTPStatuscode
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
// Validate the response after providing valid request body with header (contains Authorization as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TH003.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getHeaderForBlankAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("H3 : " + response.asPrettyString());

// ------------------Assertions------------------

		// Assertion-1 : To validate the HTTPStatuscode
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
// Validate the response after providing valid request body with header (without reqdatetime)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TH004.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getHeaderForWithoutReqdatetime(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("H4 : " + response.asPrettyString());

// ------------------Assertions------------------

		// Assertion-1 : To validate the HTTPStatuscode
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
// Validate the response after providing valid request body with header (without sign)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TH005.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getHeaderWithoutSign(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
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
// Validate the response after providing valid request body with header (without authorization)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TH006.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getHeaderForWithoutAuthrization(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("H6 : " + response.asPrettyString());

// ------------------Assertions------------------

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
// Validate the response after providing valid request body with header (takes all headers as blank)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//branch-management//GetBranchDetailsById//TH007.json");

		// add testcaseid & description into the reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey, values
		Map<String, String> header = HeaderTestCase.getAllHeaderBlank(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("H7 : " + response.asPrettyString());

// ------------------Assertions------------------

		// Assertion-1 : To validate the HTTPStatuscode
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
	public void TD002() throws IOException, ParseException {
// To validate the response after passing the already existing departmentId

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD002.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("2 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 11, dependsOnMethods = { "Token_Generation" })
	public void TD003() throws IOException, ParseException {
// To validate response after passing the already existing departmentDesc

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD003.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBodyOnlyforUniqueDeptId(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("3 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 12, dependsOnMethods = { "Token_Generation" })
	public void TD004() throws IOException, ParseException {
// To validate the response after providing the valid request data with status = D

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD004.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("4 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 13, dependsOnMethods = { "Token_Generation" })
	public void TD005() throws IOException, ParseException {
// Validate the response for request body (pass max+1 (31-chars into departmentDesc)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD005.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBodyforDeptDesc31Chars(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("5 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 14, dependsOnMethods = { "Token_Generation" })
	public void TD006() throws IOException, ParseException {
// Validate the response after providing request body (pass any character except "A" & "D")

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD006.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBody(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("6 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 15, dependsOnMethods = { "Token_Generation" })
	public void TD007() throws IOException, ParseException {
// To validate the response for the request body (pass integer type value in departmentId)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD007.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("7 : " + response.asPrettyString());
	}

	@Test(priority = 16, dependsOnMethods = { "Token_Generation" })
	public void TD008() throws IOException, ParseException {
// Validate the response for passing the float value into the departmentId

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD008.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("8 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 17, dependsOnMethods = { "Token_Generation" })
	public void TD009() throws IOException, ParseException {
// To validate response after providing blank values into all nodes of request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD009.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("9 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 18, dependsOnMethods = { "Token_Generation" })
	public void TD010() throws IOException, ParseException {
// Validate response for providing request body as same as in TD011 (but pass only 2 characters into departmentId)

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD010.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBodyforDeptIdwith2AlphaChars(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("10 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 19, dependsOnMethods = { "Token_Generation" })
	public void TD011() throws IOException, ParseException {
// Validate the response for providing request body (pass special characters) in all nodes

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD011.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("11 : " + response.asPrettyString());
	}

	@Test(priority = 20, dependsOnMethods = { "Token_Generation" })
	public void TD012() throws IOException, ParseException {
// To validate the response after passing only spaces in departmentId & departmentDesc

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD012.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("12 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 21, dependsOnMethods = { "Token_Generation" })
	public void TD013() throws IOException, ParseException {
// Validate the response after passing nothing into the request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD013.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		httpsRequest.body(jsonObject.get("RequestBody").toString());

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("13 : " + response.asPrettyString());

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

		// Assertion-2 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}

	@Test(priority = 22, dependsOnMethods = { "Token_Generation" })
	public void TD014() throws IOException, ParseException {
// Validate the response after passing nothing into the request body

		// Read data from json file
		jsonObject = UtilityClass
				.readDataFromJSONFile(dirPath + "//Test_Data//department_management//Department//TD014.json");

		// Add testcaseid & testcase description into reports
		test = extent.createTest("<h5>" + jsonObject.get("TestCaseid") + "</h5>" + "<h5>"
				+ jsonObject.get("Test Case Description") + "</h5>");

		// Request Object
		existingDeptId = response_GetAllDept.jsonPath().get("responseData.departmentId");
		existingDeptDesc = response_GetAllDept.jsonPath().get("responseData.departmentDesc");
		String d2 = getDepartmentId();
		while (existingDeptId.contains(d2)) {
			d2 = getDepartmentId();
		}
		httpsRequest.body(getNewReqBodyforDeptIdstartwith0(jsonObject.get("RequestBody").toString(), d2));

		// Add headerkey and value into requestSpecification
		Map<String, String> header = HeaderTestCase.getValidHeader(AuthorizationToken);
		httpsRequest.headers(header);

		// Response Object
		response = httpsRequest.request(Method.POST, UtilityClass.getDataFromPF("departmentEndPointUrl"));
		System.out.println("14 : " + response.asPrettyString());

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
					+ " but we get the response code" + response.jsonPath().get("responseCode"));
		}

		// Assertion-3 : To validate the HTTPStatusCode
		softassert.assertEquals(Integer.toString(response.getStatusCode()), jsonObject.get("ExpectedHTTPStatusCode"));
		if (Integer.toString(response.getStatusCode()).equals(jsonObject.get("ExpectedHTTPStatusCode"))) {
			test.log(Status.PASS, "Actual HTTP Status code- " + Integer.toString(response.getStatusCode())
					+ " is same as the expected HTTP Status code- " + jsonObject.get("ExpectedHTTPStatusCode"));
		} else {
			test.log(Status.FAIL, "We expect the status code- " + jsonObject.get("ExpectedHTTPStatusCode")
					+ " but we get the status code" + Integer.toString(response.getStatusCode()));
		}
		softassert.assertAll();
	}
}