package web;

import com.aventstack.extentreports.*;
import com.microsoft.playwright.*;
import common.*;
import utility.*;

import java.io.*;
import java.nio.file.*;

import static utility.ReportUtility.screenshotFolderLocation;

public class WebSteps extends CommonSteps {

    public static Playwright playwright;
    public static Browser browser;
    public static Page page;

    public boolean takeScreenshotWithFileNameAndMessage(String fileName, String message) {
        try {
            File destinationFile = new File(screenshotFolderLocation + fileName + ".png");
            if (destinationFile.exists())
                destinationFile.delete();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(destinationFile.getAbsolutePath())));
            if (ReportUtility.isReportPortalEnabled())
                ReportUtility.reportWithScreenshotInReportPortal(ReportUtility.REPORT_STATUS.info, destinationFile, message);
            ReportUtility.reportWithScreenshotInExtent(destinationFile, Status.INFO, message);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean launchWebUrl(String url) {
        playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        Browser.NewContextOptions browserContext = new Browser.NewContextOptions().setIgnoreHTTPSErrors(true);
        page = browser.newContext(browserContext).newPage();
        page.navigate(url);
        return true;
    }

    public boolean clickLocator(String locator) {
        page.locator(locator).first().click();
        return true;
    }

    public boolean enterTextInLocator(String textToType, String locator) {
        page.locator(locator).fill(textToType);
        return true;
    }

}
