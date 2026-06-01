package com.matrix.steps;

import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;

public class CommonSteps extends WebDriverTestBase {

    @QAFTestStep(description = "browser is launched and maximized")
    public void browserIsLaunchedAndMaximized() {
        getDriver().manage().window().maximize();
    }
}
