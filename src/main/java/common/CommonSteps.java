package common;

import utility.*;

public class CommonSteps {

    public boolean setTestName(String testName) {
        ReportUtility.currentTest = ReportUtility.getInstance().createTest(testName);
        return true;
    }

    public boolean waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
        return true;
    }


}
