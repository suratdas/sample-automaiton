package utility;

import io.restassured.*;
import io.restassured.builder.*;
import io.restassured.http.*;
import io.restassured.response.*;
import io.restassured.specification.*;

import java.io.*;
import java.text.*;

import static io.restassured.path.json.JsonPath.*;
import static java.nio.charset.StandardCharsets.*;

public class ReportPortalUtility {

    private static ReportPortalUtility reportPortalUtility;
    private String reportPortalBaseUrl;
    private String reportPortalProject;
    private String reportPortalApiToken;

    public enum REPORT_PORTAL_LOG_TYPE {
        FATAL,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }

    public enum REPORT_PORTAL_TEST_STATUS {
        passed,
        failed,
        stopped,
        skipped,
        interrupted,
        cancelled
    }

    String launchId;
    protected String topLevelSuiteItem;
    protected String currentSuiteItem;
    protected String testItem;
    protected String stepItem;


    private ReportPortalUtility() {
    }

    public static ReportPortalUtility initializeReportPortal(String reportPortalBaseUrlPassed, String reportPortalApiTokenPassed, String reportPortalProjectPassed) {
        reportPortalUtility = new ReportPortalUtility();
        reportPortalUtility.reportPortalBaseUrl = reportPortalBaseUrlPassed;
        reportPortalUtility.reportPortalApiToken = reportPortalApiTokenPassed;
        reportPortalUtility.reportPortalProject = reportPortalProjectPassed;
        return reportPortalUtility;
    }

    public void resetSuiteLevel() {
        //If you created a sub suite, this will reset it to the top level suite.
        if (currentSuiteItem != topLevelSuiteItem) {
            reportPortalFinishSuite();
            currentSuiteItem = topLevelSuiteItem;
        }
    }

    public ReportPortalUtility startLaunch(String launchName) {
        String responseBody = sendPOSTToReportPortalServer(reportPortalBaseUrl  + reportPortalProject + "/launch", "{\n" +
                "  \"name\": \"" + launchName + "\",\n" +
                "  \"startTime\": \"" + getCurrentTime() + "\"" +
                "}");
        launchId = getFromBody(responseBody, "id");
        return this;
    }

    public ReportPortalUtility reportPortalStartSuite(String suiteName) {
        resetSuiteLevel();
        String responseBodyString = sendPOSTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/item",
                "{\n" +
                        "  \"name\": \"" + suiteName + "\",\n" +
                        "  \"startTime\": \"" + getCurrentTime() + "\",\n" +
                        "  \"type\": \"suite\",\n" +
                        "  \"launchUuid\": \"" + launchId + "\"\n" +
                        "}");
        currentSuiteItem = getFromBody(responseBodyString, "id");
        return this;
    }


    public String reportPortalStartTest(String testName) {
        if (currentSuiteItem == null) {
            reportPortalStartSuite("Suite Run");
            topLevelSuiteItem = currentSuiteItem;
        }
        String responseBody = sendPOSTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/item/" + currentSuiteItem, "{\n" +
                "  \"name\": \"" + testName + "\",\n" +
                "  \"startTime\": \"" + getCurrentTime() + "\",\n" +
                "  \"type\": \"test\",\n" +
                "  \"launchUuid\": \"" + launchId + "\"\n" +
                "}");
        testItem = getFromBody(responseBody, "id");
        return testItem;
    }

    //Optionally, you can start test step
    public String reportPortalStartTestStep(String stepName) {
        String responseBody = sendPOSTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/item/" + testItem, "{\n" +
                "  \"name\": \"" + stepName + "\",\n" +
                "  \"startTime\": \"" + getCurrentTime() + "\",\n" +
                "  \"type\": \"step\",\n" +
                "  \"launchUuid\": \"" + launchId + "\"\n" +
                "}");
        stepItem = getFromBody(responseBody, "id");
        return stepItem;
    }

    //If you started a test step, remember to finish the step.
    public String reportPortalFinishTestStep(REPORT_PORTAL_TEST_STATUS status) {
        String responseBody = sendPUTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/item/" + stepItem,
                "{\n" +
                        "  \"endTime\": \"" + getCurrentTime() + "\",\n" +
                        "  \"status\": \"" + status + "\",\n" +
                        "  \"launchUuid\": \"" + launchId + "\"\n" +
                        "}");
        return getFromBody(responseBody, "id");
    }

    //Log steps here

    public String reportPortalFinishTest(REPORT_PORTAL_TEST_STATUS status) {
        String responseBody = sendPUTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/item/" + testItem,
                "{\n" +
                        "  \"endTime\": \"" + getCurrentTime() + "\",\n" +
                        //If you created test steps earlier, status below is optional
                        "  \"status\": \"" + status + "\",\n" +
                        "  \"launchUuid\": \"" + launchId + "\"\n" +
                        "}");
        return getFromBody(responseBody, "id");
    }

    public String reportPortalFinishSuite() {
        String responseBody = sendPUTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/item/" + currentSuiteItem,
                "{\n" +
                        "  \"endTime\": \"" + getCurrentTime() + "\",\n" +
                        "  \"launchUuid\": \"" + launchId + "\"\n" +
                        "}");
        return getFromBody(responseBody, "id");
    }

    public String reportPortalFinishLaunch() {
        String responseBody = sendPUTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/launch/" + launchId + "/finish",
                "{\n" +
                        "  \"endTime\": \"" + getCurrentTime() + "\"\n" +
                        "}");
        return getFromBody(responseBody, "id");
    }


    public String reportPortalLogStep(REPORT_PORTAL_LOG_TYPE level, String message) {
        String responseBody = sendPOSTToReportPortalServer(reportPortalBaseUrl + reportPortalProject + "/log",
                "{\n" +
                        "  \"launchUuid\": \"" + launchId + "\",\n" +
                        "  \"itemUuid\": \"" + testItem + "\",\n" +
                        "  \"time\": \"" + getCurrentTime() + "\",\n" +
                        "  \"message\": \"" + message + "\",\n" +
                        "  \"level\": \"" + level + "\"\n" +
                        "}");
        return getFromBody(responseBody, "id");
    }

    private String getCurrentTime() {
        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(new java.util.Date());
    }

    public String reportPortalLogStepWithScreenshot(REPORT_PORTAL_LOG_TYPE level, String screenShotPath, String message) {

        String[] parts = screenShotPath.split("/");
        String fileName = parts[parts.length - 1];

        String url = reportPortalBaseUrl + reportPortalProject + "/log";
        String currentTime = getCurrentTime();
        String body = "[{ \"launchUuid\": \"" + launchId + "\"," +
                "  \"itemUuid\": \"" + testItem + "\"," +
                "  \"time\": \"" + currentTime + "\"," +
                "  \"message\": \"" + message + "\"," +
                "  \"level\": \"" + level.name().toLowerCase() + "\"," +
                "  \"file\":{\"name\":\"" + fileName + "\"}" +
                "}]";
        MultiPartSpecification multiPartSpecificationText = new MultiPartSpecBuilder(body)
                .with()
                .controlName("json_request_part")
                .mimeType(ContentType.JSON.toString())
                .charset(UTF_8)
                .build();
        MultiPartSpecification multiPartSpecificationScreenshot = new MultiPartSpecBuilder(new File(screenShotPath))
                .with()
                .fileName(fileName)
                .controlName("file")
                .mimeType("image/png")
                .build();
        RequestSpecification requestSpecification = RestAssured.given()
                .header("Authorization", "Bearer " + reportPortalApiToken)
                .contentType("multipart/form-data")
                .multiPart(multiPartSpecificationText)
                .multiPart(multiPartSpecificationScreenshot);
        try {
            Response response = requestSpecification.post(url);
            String responseBody = response.getBody().asString();
            return getFromBody(responseBody, "id");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public String reportPortalLogStepWithJSON(REPORT_PORTAL_LOG_TYPE level, String message, String jsonBody) {

        File file = createFile("jsonBody.txt", jsonBody);
        String url = reportPortalBaseUrl + reportPortalProject + "/log";
        String currentTime = getCurrentTime();
        String body = "[{ \"launchUuid\": \"" + launchId + "\"," +
                "  \"itemUuid\": \"" + testItem + "\"," +
                "  \"time\": \"" + currentTime + "\"," +
                "  \"message\": \"" + message + "\"," +
                "  \"level\": \"" + level.name().toLowerCase() + "\"," +
                "  \"file\":{\"name\":\"" + file.getName() + "\"}" +
                "}]";
        MultiPartSpecification multiPartSpecificationText = new MultiPartSpecBuilder(body)
                .with()
                .controlName("json_request_part")
                .mimeType(ContentType.JSON.toString())
                .charset(UTF_8)
                .build();
        MultiPartSpecification multiPartSpecificationJson = new MultiPartSpecBuilder(file)
                .with()
                .fileName(file.getName())
                .controlName("file")
                .mimeType(ContentType.JSON.toString())
                .build();
        RequestSpecification requestSpecification = RestAssured.given()
                .header("Authorization", "Bearer " + reportPortalApiToken)
                .contentType("multipart/form-data")
                .multiPart(multiPartSpecificationText)
                .multiPart(multiPartSpecificationJson);
        try {
            Response response = requestSpecification.post(url);
            String responseBody = response.getBody().asString();
            return getFromBody(responseBody, "id");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (file.exists())
                file.delete();
        }
        return "";
    }

    private String sendPOSTToReportPortalServer(String url, String body) {
        return sendRequestToServer(body, url, "post");
    }

    private String sendPUTToReportPortalServer(String url, String body) {
        return sendRequestToServer(body, url, "put");
    }

    private String sendRequestToServer(String body, String url, String type) {
        RequestSpecification requestSpecification = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + reportPortalApiToken)
                .body(body);
        Response response = type.toLowerCase().contains("put") ? requestSpecification.put(url) : requestSpecification.post(url);
        return response.getBody().asString();
    }

    private String getFromBody(String responseBodyString, String path) {
        try {
            return from(responseBodyString).get(path).toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static File createFile(String fileName, String string) {
        File file = new File(fileName);
        try {
            FileOutputStream fileWriter = new FileOutputStream(file);
            fileWriter.write(string.getBytes());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
