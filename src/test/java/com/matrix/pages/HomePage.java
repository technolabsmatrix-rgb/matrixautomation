package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class HomePage extends WebDriverBaseTestPage<HomePage> {

    @FindBy(locator = "home.logo")
    private QAFWebElement logo;

    @FindBy(locator = "home.hero.heading")
    private QAFWebElement heroHeading;

    @FindBy(locator = "home.hero.cta.button")
    private QAFWebElement heroCtaButton;

    @FindBy(locator = "home.services.section")
    private QAFWebElement servicesSection;

    @FindBy(locator = "home.about.section")
    private QAFWebElement aboutSection;

    @FindBy(locator = "home.footer")
    private QAFWebElement footer;

    @FindBy(locator = "home.footer.copyright")
    private QAFWebElement footerCopyright;

    @Override
    protected void openPage(String pageUrl) {
        new WebDriverTestBase().getDriver().get(pageUrl);
    }

    public boolean isLogoDisplayed() {
        return logo.isDisplayed();
    }

    public boolean isHeroHeadingDisplayed() {
        return heroHeading.isDisplayed();
    }

    public String getHeroHeadingText() {
        return heroHeading.getText();
    }

    public void clickHeroCtaButton() {
        heroCtaButton.click();
    }

    public boolean isServicesSectionPresent() {
        return servicesSection.isPresent();
    }

    public boolean isAboutSectionPresent() {
        return aboutSection.isPresent();
    }

    public boolean isFooterDisplayed() {
        return footer.isDisplayed();
    }

    public boolean isFooterCopyrightPresent() {
        return footerCopyright.isPresent();
    }

    public String getPageTitle() {
        return new WebDriverTestBase().getDriver().getTitle();
    }
}
