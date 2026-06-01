---
name: qaf-page
description: Scaffold a new QAF Page Object class with locators wired to a properties file. Pass the page name and list of elements needed.
---

You are scaffolding a QAF Page Object for the Matrix automation project.

## Project Conventions

- Page classes: `src/test/java/com/matrix/pages/`
- Locator properties: `resources/locators/`
- Extend or use `QAFWebElement` fields annotated with `@FindBy`
- Locator key format: `pagename.elementname` (e.g. `contact.submitButton`)

## Rules

1. Each page class has a corresponding `resources/locators/<pagename>.properties` file
2. `@FindBy(locator = "key")` references the key from the properties file
3. Always call `element.waitForPresent()` before `element.sendKeys()`
4. Always use `element.verifyPresent()` for assertions — never `Assert.assertTrue`
5. No direct CSS/XPath strings inside Java — all locators go in `.properties`

### Rule 7 — Page Object Model (POM) Design

#### Base class

Every page class extends `WebDriverBaseTestPage<ParentPageType>`:
```java
public class LoginPage extends WebDriverBaseTestPage<HomePage> { }
```
The generic type parameter is the **parent page** (the page that navigates TO this one).

#### Mandatory override: `openPage`

Every page MUST override `openPage` — this is how QAF navigates to the page:
```java
@Override
protected void openPage(PageLocator loc, Object... args) {
    parent.getLoginLink().click();   // use parent page method to arrive here
}
```
For the root/entry page (no parent), use the driver directly:
```java
@Override
protected void openPage(PageLocator loc, Object... args) {
    driver.get("/");
}
```

#### Optional override: `isPageActive`

Override to let QAF detect whether the page is already open (avoids re-launch):
```java
@Override
public boolean isPageActive(PageLocator loc, Object... args) {
    return driver.getCurrentUrl().contains("/login");
}
```

#### Page hierarchy patterns

**Single parent (one route):**
```java
// P → P1 → P11
public class P11 extends WebDriverBaseTestPage<P1> {
    @Override
    protected void openPage(PageLocator loc, Object... args) {
        parent.getP11Link().click();
    }
}
```

**Multiple routes (page reachable from 2+ parents):**
```java
// 1. Define a launcher interface
interface SamplePageLauncher extends WebDriverTestPage {
    void launchSamplePage(PageLocator loc, Object... args);
}

// 2. Each parent implements it
class Route1Page extends WebDriverBaseTestPage<Route1ParentPage>
        implements SamplePageLauncher {
    @Override
    public void launchSamplePage(PageLocator loc, Object... args) {
        pageLink.click();
    }
}

// 3. Page uses the interface as its generic type
public class SamplePage extends WebDriverBaseTestPage<SamplePageLauncher> {
    public SamplePage() { this(new Route1Page()); }           // default route
    public SamplePage(SamplePageLauncher parent) { super(parent); }

    @Override
    protected void openPage(PageLocator loc, Object... args) {
        parent.launchSamplePage(loc, args);
    }
}
```

**Dynamic parent (chosen at runtime from config/workflow):**
```java
@Override
protected void initParent() {
    parent = (pageProps.getInt("review.next.flow") == 6)
        ? new ReviewFlightPage()
        : new PassengerPage();
}
```

#### PageLocator — passing navigation parameters

`PageLocator` carries the identifier used to open a specific instance (e.g. an item name).

```java
// In the page:
@Override
protected void openPage(PageLocator loc, Object... args) {
    parent.openItemDetails(loc.getLocator());   // loc.getLocator() returns the string key
}

// In a step / test:
ItemDetailsPage page = new ItemDetailsPage();
page.launchPage(new DefaultPageLocator("XYZ"));
```

For deep hierarchies, build the locator chain via static factory methods:
```java
// Builds: EditSupplierDetailPage ← UploadDetailsPage ← UploadStatusPage ← UploadHistoryPage
PageLocator locator = EditSupplierDetailPage.createPageLocator("SupplierA", "job123");
editPage.launchPage(locator);
```

#### Linked pages (return next page from an action method)

**Static linked page (always goes to same page):**
```java
public ViewPage clickSave() {
    saveBtn.click();
    ViewPage next = new ViewPage();
    next.waitForPageToLoad();
    return next;
}
```

**Dynamic linked page (caller decides the next page):**
```java
public <T extends BaseTestPage<TestPage>> T clickSave(T expectedPage) {
    saveContinueBtn.click();
    expectedPage.waitForPageToLoad();
    return expectedPage;
}
```

#### Launch strategies

Set in the page constructor (global) or per test method (override):

| Strategy | Behaviour |
|---|---|
| `onlyIfRequired` | **(default)** Launches page only if `isPageActive()` returns false |
| `alwaysRelaunchFromParent` | Always re-navigates from parent page |
| `alwaysRelaunchFromRoot` | Always re-navigates from the root page |

```java
// Per test — override default strategy
loginPage.setLaunchStrategy(LaunchStrategy.alwaysRelaunchFromRoot);
loginPage.launchPage(loc);

// In page constructor — applies to all tests using this page
public LoginPage() {
    setLaunchStrategy(LaunchStrategy.alwaysRelaunchFromParent);
}
```

#### Template / reusable page pattern

Extract shared navigation structure into an abstract template page, then extend it:
```java
// Shared nav bar logic lives in TopNavTab
public class OverviewTab extends TopNavTab { ... }
public class MyCompanyTab extends TopNavTab { ... }
```

#### Summary rules
- Every page class has exactly one `openPage` override
- Parent type in generic = the page that navigates TO this page
- Use `DefaultPageLocator(string)` to pass item names / IDs into `openPage`
- Multiple-route pages use a launcher interface, not a concrete parent
- Dynamic parent goes in `initParent()`, not the constructor body
- Set `LaunchStrategy.alwaysRelaunchFromRoot` for login/auth pages in tests that must start clean

### Rule 6 — Locator Strategy

**Supported strategies (prefix=value):**

| Strategy | Example |
|---|---|
| `css` | `css=.btn-submit` |
| `xpath` | `xpath=//*[@id='email']` |
| `id` | `id=submitBtn` |
| `name` | `name=username` |
| `link` | `link=Click Here` |
| `partialLink` | `partialLink=Click` |
| `className` | `className=form-control` |
| `tagName` | `tagName=button` |
| `jquery` | `jquery=div:contains('Submit')` |
| `accessibility id` | `accessibility id=myBtn` (mobile) |
| `-android uiautomator` | `-android uiautomator=text('OK')` (Android) |
| `-ios predicate string` | `-ios predicate string=label == 'OK'` (iOS) |

**Standard format:**
```properties
pagename.elementname={"locator":"css=.selector", "desc":"Human label"}
```

**Full JSON format (all optional fields):**
```properties
# semicolons OR commas are both valid separators inside the JSON map
pagename.elementname={'locator':'css=.selector','desc':'Label','cacheable':true,'scroll':'Always','scroll-options':'{block:\'center\'}','sendkeys-options':'click clear'}
```

**Alternate/fallback locator chain (tries each until one works):**
```properties
# array form — no desc
pagename.elementname=['css=#qa','name=eleName','xpath=.//*[@id=\'tabs\']']

# array form — with desc
pagename.elementname={'locator':['css=#qa','name=eleName'],'desc':'Element label'}
```

**Hybrid (native + WebView context for Appium):**
```properties
pagename.elementname={"locator":"xpath=//*[@name='Result']","desc":"Input box","context":"WEBVIEW"}
```

**Field reference:**
- `locator` — **required**; `strategy=value` string, or an array for fallback chain
- `desc` — shown in assertion/report messages
- `cacheable` — `true` to cache element after first find
- `context` — `"WEBVIEW"` or `"NATIVE_APP"` for Appium hybrid apps
- `scroll` — `"Always"` / `"true"` to always scroll, `"OnFail"` to scroll only on failure
- `scroll-options` — ScrollIntoView alignment: `"true"`, `"false"`, or `"{block:'center'}"`
- `sendkeys-options` — `"click"` (click before typing), `"clear"` (clear before typing), or both
- `type` — `"select"` for dropdowns, `"password"` / `"encrypted"` for masked fields
- `component-class` — fully-qualified class extending `QAFWebComponent` for custom components

**Preferred strategy priority (web):** `css` > `id` > `name` > `xpath`  
**Use `xpath` only** when CSS cannot target the element (e.g. text-based matching, parent traversal).  
**jQuery strategy** (`jquery=div:contains('text')`) — use for text-based selection when xpath is too verbose.

## Output Format

### Page Class
```java
package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class <PageName>Page extends WebDriverBaseTestPage<<PageName>Page> {

    @FindBy(locator = "pagename.elementname")
    private QAFWebElement elementName;

    public void methodName(String value) {
        elementName.waitForPresent();
        elementName.sendKeys(value);
    }
}
```

### Locator Properties (`resources/locators/<pagename>.properties`)
```
pagename.elementname={"locator":"css=selector", "desc":"Element label"}
```

### Rule 8 — `@PageIdentifier` Annotation

Mark the element used to confirm the page is loaded with `@PageIdentifier`. QAF uses it in `waitForPageToLoad()` and the default `isPageActive()`:

```java
@PageIdentifier
@FindBy(locator = "home.searchTextbox")
private QAFWebElement searchTextbox;

// Now isPageActive() and waitForPageToLoad() check this element automatically
@Override
public boolean isPageActive(PageLocator loc, Object... args) {
    return searchTextbox.isPresent();
}
```

### Rule 9 — Direct Element Construction (without `@FindBy`)

When a locator key is known at runtime, construct a `QAFExtendedWebElement` directly:
```java
QAFWebElement item = new QAFExtendedWebElement("locator.key.from.properties");
item.waitForPresent();
item.click();
```

Also valid in BDD step bodies:
```gherkin
assert 'login.username.txt' is present
sendKeys 'myusername' into 'login.username.txt'
```

### Rule 10 — Locator Repository File Extensions

Locator files may use `.properties` or `.loc` extensions — both are loaded from `env.resources` dirs:
```
resources/locators/login.properties   ← standard
resources/locators/login.loc          ← also valid
```

### Rule 11 — Web Services Page Base Class

For REST API test pages, extend `RestWSTestCase` instead of `WebDriverBaseTestPage`:
```java
public class OrderApiTest extends RestWSTestCase {
    @Test(testName = "create order")
    public void createOrder() {
        Map<String, String> data = new HashMap<>();
        data.put("clientName", "Matrix");
        data.put("amount", "500");
        getWebResource("/orders.json").post(new Gson().toJson(data));
        verifyThat("Response Status", getResponse().getStatus(),
            Matchers.equalTo(Status.CREATED));
    }
}
```
Available objects: `getWebResource()`, `getClient()`, `getResponse()`, all `Validator` methods.

## Task

The user will name the page and list elements/actions needed. Generate the full page class and properties file.
