import api.*;
import common.*;
import org.testng.annotations.*;
import utility.*;
import web.*;

import java.io.*;

import static org.testng.Assert.*;

public class SampleTest {

    // Example use of report portal
    public static void main(String[] args) {

        //Get these details from Report portal UI
        ReportUtility.startReportPortal("http://localhost:8080/api/v1/", "e031f4d3-09c1-4e53-a4c6-ea21e6f61f18", "superadmin_personal", "Launch name");
        ReportUtility.startSuite("Suite name");
        ReportUtility.startTest("Test name");

        //Perform tests and log
        ReportUtility.reportTextInReportPortal(ReportUtility.REPORT_STATUS.pass, "Some passed steps");
        ReportUtility.reportJsonInReportPortal(ReportUtility.REPORT_STATUS.fail, "JSON response body", "{\"status\":\"success\"}");
        ReportUtility.reportExceptionInReportPortal(ReportUtility.REPORT_STATUS.debug, new Exception("Some exception"));
        ReportUtility.reportWithScreenshotInReportPortal(ReportUtility.REPORT_STATUS.info, new File("Sample.png"), "Sample screenshot");

        //Finish test
        ReportUtility.finishTest(ReportUtility.REPORT_STATUS.fail);
        ReportUtility.finishSuite();
        ReportUtility.finishReportPortal();
    }

    // Example of Extent report
    @BeforeMethod
    public void setUp() {
        new SetUp("extent.html");
    }

    @AfterMethod
    public void tearDown() {
        new TearDown();
    }

    @Test
    public void test1() {
        APISteps apiSteps = new APISteps();
        assertTrue(apiSteps.setTestName("Test1"));
        assertTrue(apiSteps.callGETForEndpoint("https://jsonplaceholder.typicode.com/todos/1"));
        String completed = apiSteps.getFromResponse("completed");
        assertTrue(apiSteps.callGETForEndpoint("https://jsonplaceholder.typicode.com/todos/" + completed));
    }

    @Test
    public void testInvalidURL() {
        APISteps apiSteps = new APISteps();
        assertTrue(apiSteps.setTestName("Test2"));
        assertTrue(apiSteps.callGETForEndpoint("https://jsonplaceholder.typicode.c/todos/1"));
    }

    @Test
    public void testWeb2() {
        WebSteps webSteps = new WebSteps();
        webSteps.setTestName("Web Test 2");
        webSteps.launchWebUrl("https://playwright.dev/");
        webSteps.takeScreenshotWithFileNameAndMessage("Sample21", "Launch");
        webSteps.enterTextInLocator("test", "css=input[placeholder='Search']");
        webSteps.waitSeconds(3);
        webSteps.takeScreenshotWithFileNameAndMessage("Sample22", "After entering text");
    }

}
