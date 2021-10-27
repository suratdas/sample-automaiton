package common;

import utility.*;

import java.io.*;

public class SetUp {

    public SetUp() {
        ReportUtility.changeExtentLocation("FitNesseRoot" + File.separator + "files" + File.separator + "extent.html");
    }

    public SetUp(String path) {
        ReportUtility.changeExtentLocation(path);
    }

}
