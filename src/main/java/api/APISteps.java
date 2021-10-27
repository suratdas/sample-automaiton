package api;

import com.aventstack.extentreports.*;
import com.jayway.jsonpath.*;
import common.*;
import io.restassured.builder.*;
import io.restassured.response.*;
import utility.*;

import static io.restassured.RestAssured.*;

public class APISteps extends CommonSteps {

    protected static Response response;

    public boolean callGETForEndpoint(String serverEndpoint) {
        return callAPIWithRequest(serverEndpoint, null, "GET");
    }

    private boolean callAPIWithRequest(String serverEndpoint, String requestBody, String type) {
        try {
            RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder().setBaseUri(serverEndpoint);
            if (requestBody != null) {
                requestSpecBuilder.setBody(requestBody);
                if (ReportUtility.isReportPortalEnabled())
                    ReportUtility.reportJsonInReportPortal(ReportUtility.REPORT_STATUS.info, "Request body : ", requestBody);
                ReportUtility.reportJsonInExtent("Request body : ", requestBody);
            }

            if (type.contentEquals("GET")) {
                if (ReportUtility.isReportPortalEnabled())
                    ReportUtility.reportTextInReportPortal(ReportUtility.REPORT_STATUS.info, "Calling GET for endpoint " + serverEndpoint);
                ReportUtility.reportTextInExtent(Status.INFO, "Calling GET for endpoint " + serverEndpoint);
                response = given().spec(requestSpecBuilder.build()).when().get();
            }
            if (response == null || response.getBody() == null) {
                if (ReportUtility.isReportPortalEnabled())
                    ReportUtility.reportTextInReportPortal(ReportUtility.REPORT_STATUS.fail, "Did not get a valid response or valid response body.");
                ReportUtility.reportTextInExtent(Status.FAIL, "Did not get a valid response or valid response body.");
                return false;
            }
            String responseString = response.getBody().asString();
            if (ReportUtility.isReportPortalEnabled())
                ReportUtility.reportJsonInReportPortal(ReportUtility.REPORT_STATUS.info, "Response body :", responseString);
            ReportUtility.reportJsonInExtent("Response body :", responseString);

            return true;
        } catch (Exception ex) {
            if (ReportUtility.isReportPortalEnabled())
                ReportUtility.reportTextInReportPortal(ReportUtility.REPORT_STATUS.fail, "Call to the server failed. " + System.lineSeparator() + ex.getMessage());
            ReportUtility.reportTextInExtent(Status.FAIL, "Call to the server failed. " + System.lineSeparator() + ex.getMessage());
            return false;
        }
    }

    public String getFromResponse(String jsonPath) {
        String responseBodyString = response.getBody().asString();
        Object actualValue = JsonPath.parse(responseBodyString).read(jsonPath);
        return actualValue.toString();
    }

}
