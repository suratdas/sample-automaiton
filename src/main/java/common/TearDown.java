package common;

import utility.*;
import web.*;

public class TearDown {

    public TearDown() {
        if (WebSteps.browser != null) {
            WebSteps.browser.close();
        }
        if (WebSteps.playwright != null) {
            WebSteps.playwright.close();
        }
        ReportUtility.getInstance().flush();
        ReportUtility.currentTest = null;
    }

}

