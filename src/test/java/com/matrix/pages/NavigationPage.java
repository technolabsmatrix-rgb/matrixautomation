package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class NavigationPage extends WebDriverBaseTestPage<NavigationPage> {

    @FindBy(locator = "nav.menu")
    private QAFWebElement navMenu;

    @FindBy(locator = "nav.menu.home")
    private QAFWebElement homeLink;

    @FindBy(locator = "nav.menu.about")
    private QAFWebElement aboutLink;

    @FindBy(locator = "nav.menu.services")
    private QAFWebElement servicesLink;

    @FindBy(locator = "nav.menu.portfolio")
    private QAFWebElement portfolioLink;

    @FindBy(locator = "nav.menu.contact")
    private QAFWebElement contactLink;

    @FindBy(locator = "nav.hamburger")
    private QAFWebElement hamburgerMenu;

    @Override
    protected void openPage(String pageUrl) {
        getDriver().get(pageUrl);
    }

    public boolean isNavMenuDisplayed() {
        return navMenu.isDisplayed();
    }

    public void clickHome() {
        homeLink.click();
    }

    public void clickAbout() {
        aboutLink.click();
    }

    public void clickServices() {
        servicesLink.click();
    }

    public void clickPortfolio() {
        portfolioLink.click();
    }

    public void clickContact() {
        contactLink.click();
    }

    public boolean isHomeLinkPresent() {
        return homeLink.isPresent();
    }

    public boolean isAboutLinkPresent() {
        return aboutLink.isPresent();
    }

    public boolean isServicesLinkPresent() {
        return servicesLink.isPresent();
    }

    public boolean isPortfolioLinkPresent() {
        return portfolioLink.isPresent();
    }

    public boolean isContactLinkPresent() {
        return contactLink.isPresent();
    }

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public String getPageTitle() {
        return getDriver().getTitle();
    }
}
