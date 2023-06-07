package Maven.WOWCard_API;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportBase
{
	public static ExtentReports extent;
	  public static ExtentSparkReporter spark;
	  public static ExtentTest test;
	  
	  public static ExtentReports getReports()
	  {
		  if(extent==null)
		  {
			  extent=new ExtentReports();
			  spark=new ExtentSparkReporter(System.getProperty("user.dir")+"\\test-output\\ExtentReport\\testReport.html");
			  extent.attachReporter(spark);
		  }
		  return extent;
		  
	  }
}
