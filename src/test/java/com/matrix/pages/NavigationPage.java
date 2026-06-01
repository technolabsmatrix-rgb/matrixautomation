package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.api.PageLocator;
import com.qmetry.qaf.automation.ui.api.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class NavigationPage extends WebDriverBaseTestPage<WebDriverTestPage> {

    @FindBy(locator = "nav.menu.loc")
    private QAFWebElement navMenu;

    @FindBy(locator = "nav.menu.home.loc")
    private QAFWebElement homeLink;

    @FindBy(locator = "nav.menu.about.loc")
    private QAFWebElement aboutLink;

    @FindBy(locator = "nav.menu.services.loc")
    private QAFWebElement servicesLink;

    @FindBy(locator = "nav.menu.portfolio.loc")
    private QAFWebElement portfolioLink;

    @FindBy(locator = "nav.menu.contact.loc")
    private QAFWebElement contactLink;

    @FindBy(locator = "nav.hamburger.loc")
    private QAFWebElement hamburgerMenu;

    @FindBy(locator = "nav.section.about.loc")
    private QAFWebElement aboutSectionHeading;

    @FindBy(locator = "nav.section.services.loc")
    private QAFWebElement servicesSectionHeading;

    @FindBy(locator = "nav.section.contact.loc")
    private QAFWebElement contactSectionHeading;

    @Override
    protected void openPage(PageLocator locator, Object... args) {
        new WebDriverTestBase().getDriver().get(locator.getLocator());
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
        return new WebDriverTestBase().getDriver().getCurrentUrl();
    }

    public String getPageTitle() {
        return new WebDriverTestBase().getDriver().getTitle();
    }

    public QAFWebElement getNavMenu() {
        return navMenu;
    }

    public QAFWebElement getHomeLink() {
        return homeLink;
    }

    public QAFWebElement getAboutLink() {
        return aboutLink;
    }

    public QAFWebElement getServicesLink() {
        return servicesLink;
    }

    public QAFWebElement getPortfolioLink() {
        return portfolioLink;
    }

    public QAFWebElement getContactLink() {
        return contactLink;
    }

    public QAFWebElement getHamburgerMenu() {
        return hamburgerMenu;
    }

    public QAFWebElement getAboutSectionHeading() {
        return aboutSectionHeading;
    }

    public QAFWebElement getServicesSectionHeading() {
        return servicesSectionHeading;
    }

    public QAFWebElement getContactSectionHeading() {
        return contactSectionHeading;
    }
}
