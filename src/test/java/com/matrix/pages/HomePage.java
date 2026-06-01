package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.api.PageLocator;
import com.qmetry.qaf.automation.ui.api.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class HomePage extends WebDriverBaseTestPage<WebDriverTestPage> {

    @FindBy(locator = "home.logo.loc")
    private QAFWebElement logo;

    @FindBy(locator = "home.hero.heading.loc")
    private QAFWebElement heroHeading;

    @FindBy(locator = "home.hero.cta.button.loc")
    private QAFWebElement heroCtaButton;

    @FindBy(locator = "home.services.section.loc")
    private QAFWebElement servicesSection;

    @FindBy(locator = "home.about.section.loc")
    private QAFWebElement aboutSection;

    @FindBy(locator = "home.footer.loc")
    private QAFWebElement footer;

    @FindBy(locator = "home.footer.copyright.loc")
    private QAFWebElement footerCopyright;

    @Override
    protected void openPage(PageLocator locator, Object... args) {
        new WebDriverTestBase().getDriver().get(locator.getLocator());
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
