package utility;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;

public class ReportUtility {

    private static ReportPortalUtility reportPortalUtility;
    public static String screenshotFolderLocation = "FitNesseRoot" + File.separator + "files" + File.separator;

    public enum REPORT_STATUS {
        info,
        fail,
        pass,
        debug
    }

    public static boolean isReportPortalEnabled() {
        return reportPortalUtility != null;
    }

    public static ReportPortalUtility.REPORT_PORTAL_LOG_TYPE getStepStatus(REPORT_STATUS status) {
        switch (status) {
            case fail:
                return ReportPortalUtility.REPORT_PORTAL_LOG_TYPE.ERROR;
            case debug:
                return ReportPortalUtility.REPORT_PORTAL_LOG_TYPE.DEBUG;
        }
        return ReportPortalUtility.REPORT_PORTAL_LOG_TYPE.INFO;
    }

    public static ReportPortalUtility.REPORT_PORTAL_TEST_STATUS getTestStatus(REPORT_STATUS status) {
        switch (status) {
            case fail:
                return ReportPortalUtility.REPORT_PORTAL_TEST_STATUS.failed;
        }
        return ReportPortalUtility.REPORT_PORTAL_TEST_STATUS.passed;

    }

    public static synchronized ReportPortalUtility startReportPortal(String baseUrl, String apiToken, String project, String name) {
        if (reportPortalUtility == null) {
            reportPortalUtility = ReportPortalUtility.initializeReportPortal(baseUrl, apiToken, project);
            reportPortalUtility.startLaunch(name);
        }
        return reportPortalUtility;
    }

    public static void startSuite(String name) {
        reportPortalUtility.reportPortalStartSuite(name);
    }

    public static void startTest(String name) {
        reportPortalUtility.reportPortalStartTest(name);
    }

    public static void reportTextInReportPortal(REPORT_STATUS status, String message) {
        reportPortalUtility.reportPortalLogStep(getStepStatus(status), message);
    }

    public static void reportJsonInReportPortal(REPORT_STATUS status, String message, String jsonText) {
        reportPortalUtility.reportPortalLogStepWithJSON(getStepStatus(status), message, jsonText);
    }

    public static void reportExceptionInReportPortal(REPORT_STATUS status, Exception message) {
        reportPortalUtility.reportPortalLogStep(getStepStatus(status), message.getMessage());
    }

    public static String reportWithScreenshotInReportPortal(REPORT_STATUS type, File destinationFile, String message) {
        reportPortalUtility.reportPortalLogStepWithScreenshot(getStepStatus(type), destinationFile.getName(), message);
        return destinationFile.getAbsolutePath();
    }

    public static void finishTest(REPORT_STATUS status) {
        reportPortalUtility.reportPortalFinishTest(getTestStatus(status));
    }

    public static void finishSuite() {
        reportPortalUtility.reportPortalFinishSuite();
    }

    public static void finishReportPortal() {
        reportPortalUtility.reportPortalFinishLaunch();
    }

    //Extent part

    private static ExtentReports extentReports;
    public static String fileName = "extent.html";
    public static ExtentTest currentTest;

    private ReportUtility() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            extentReports = new ExtentReports();
            extentReports.attachReporter(new ExtentHtmlReporter(fileName));
        }
        return extentReports;
    }

    public static final void changeExtentLocation(String path) {
        int lastIndexOfSlash = path.lastIndexOf(File.separator);
        if (lastIndexOfSlash > 1) {
            screenshotFolderLocation = path.substring(0, lastIndexOfSlash + 1);
            fileName = path.substring(lastIndexOfSlash);
        } else {
            fileName = path;
        }
    }

    public static void reportTextInExtent(Status type, String message) {
        currentTest.log(type, message);
    }

    public static final void reportJsonInExtent(String message, String valueToBeJsonFormatted) {
        reportTextInExtent(Status.INFO, message + getFormattedJson(valueToBeJsonFormatted));
    }

    public static void reportExceptionInExtent(Status type, Exception exception) {
        currentTest.log(type, exception);
    }

    public static String reportWithScreenshotInExtent(File destinationFile, Status type, String message) throws IOException {
        currentTest.log(type, message, MediaEntityBuilder.createScreenCaptureFromPath(destinationFile.getPath()).build());
        return destinationFile.getAbsolutePath();
    }


    private static final String getFormattedJson(String body) {
        try {
            Object json = new ObjectMapper().readValue(body, Object.class);
            body = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
        }
        return "</br><pre>" + body + "</pre>";
    }

}
