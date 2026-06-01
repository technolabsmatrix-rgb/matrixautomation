---
name: qaf-step
description: Generate a new QAF step definition and wire it to a page object method. Pass a plain-English description of the step.
---

You are generating QAF (QMetry Automation Framework) step definitions for the Matrix automation project.

## Sample Project Reference

The canonical step class is inside the zip at:
```
.claude/resources/qaf-blank-project-maven-master.zip
→ src/test/java/com/qmetry/qaf/example/steps/StepsLibrary.java
```

Key patterns demonstrated in that file:
- Step methods are **`static`** (not instance methods)
- `import static com.qmetry.qaf.automation.step.CommonStep.*` gives access to built-in steps (`get`, `sendKeys`, `submit`, `verifyLinkWithPartialTextPresent`, etc.)
- `QAFExtendedWebElement` is constructed directly from a locator key when a `@FindBy` page object is not needed
- Helper methods (`private static`) can be extracted without `@QAFTestStep` — only public steps that BDD should see need the annotation

```java
// From StepsLibrary.java in the sample project
import static com.qmetry.qaf.automation.step.CommonStep.*;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.webdriver.*;

public class StepsLibrary {

    @QAFTestStep(description = "navigate to search page")
    public static void navigateToSearchPage() {
        get("/");                          // CommonStep.get() — navigates relative to env.baseurl
        rejectAllCookies();               // private helper — no annotation
    }

    @QAFTestStep(description = "search for {0}")
    public static void searchFor(String searchTerm) {
        sendKeys(searchTerm, "input.search");   // locator key from search.properties
        submit("input.search");
    }

    private static void rejectAllCookies() {
        QAFWebElement btn = new QAFExtendedWebElement("reject.all");
        if (btn.isPresent()) {
            btn.click();
        }
    }
}
```

The locator file for the above (`resources/search.properties`):
```properties
input.search={"locator":"name=q","desc":"Search Input Box"}
button.search={"locator":"name=btnG","desc":"Search Button"}
reject.all={"locator":"id=W0wltc","desc":"Reject All Button"}
```

## Rules

1. Step class lives in `src/test/java/com/matrix/steps/`
2. Page class lives in `src/test/java/com/matrix/pages/`
3. Always annotate with `@QAFTestStep(description = "...")` — the description becomes the Gherkin text
4. Use `{0}`, `{1}`, `{2}` placeholders for parameters in the description string
5. For WebElement verification, always use `element.verifyPresent()` — never `Assert.assertTrue(element.isPresent())`
6. Always call `element.waitForPresent()` before `element.sendKeys()` in page classes
7. Do NOT use `verifyVisible()` and do NOT pass arguments to `verifyPresent()`

### Rule 8 — Page Object Model (POM) in Steps

Steps instantiate page objects and call their methods. Follow these conventions:

- Every page extends `WebDriverBaseTestPage<ParentPageType>`
- Call `page.launchPage(new DefaultPageLocator("key"))` when navigation is required
- When a step navigates to a new page, the page's `openPage()` handles the click — steps never call `driver.get()` or `element.click()` for navigation directly
- For pages reachable from multiple routes, pass the desired parent: `new SamplePage(new Route2Page())`
- Set launch strategy per-test when a clean state is needed:
  ```java
  LoginPage loginPage = new LoginPage();
  loginPage.setLaunchStrategy(LaunchStrategy.alwaysRelaunchFromRoot);
  loginPage.launchPage(loc);
  ```
- Action methods that transition to a new page should return that page object:
  ```java
  ViewPage viewPage = formPage.clickSave();
  viewPage.waitForPageToLoad();
  ```

### Rule 9 — Locator Strategy

All locators go in `resources/locators/<pagename>.properties`. Never hardcode selectors in Java.

**Locator key format:** `pagename.elementname`

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

**Standard property format:**
```properties
pagename.elementname={"locator":"css=.selector", "desc":"Human readable label"}
```

**Full JSON locator format (all optional fields):**
```properties
# semicolons OR commas are both valid separators inside the JSON map
pagename.elementname={'locator':'css=.selector','desc':'Label','cacheable':true,'scroll':'Always','scroll-options':'{block:\'center\'}','sendkeys-options':'click clear'}
```

**Alternate/fallback locator chain (tried in order until one works):**
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

### Rule 10 — Step Parameter Types

`@QAFTestStep` supports multiple parameter declaration styles:

| Style | Example description | Java signature |
|---|---|---|
| Numbered | `"user logins with {0} and {1}"` | `(String u, String p)` |
| Named | `"user logins with {username} and {password}"` | `(String username, String password)` |
| Typed numbered | `"user logins with {0:string} and {1:string}"` | `(String u, String p)` |
| Map | `"fill form with {0:map}"` | `(Map<String,Object> data)` |
| Regex alt | `"user (logins\|signins) with {0} and {1}"` | `(String u, String p)` |

Arrays are passed in step text as `['read','write','modify']`.

### Rule 11 — Step Metadata and Threshold

Add `@MetaData` to a step to attach groups or a performance threshold:

```java
@MetaData(value = "{'groups':['login'],'threshold':10}")
@QAFTestStep(description = "user login with username {username} and password {password}")
public void login(String username, String password) {
    new LoginPage().login(username, password);
}
```

- `threshold` — maximum allowed duration in seconds; exceeded threshold shows as warning in QAF report
- Use `@MetaData` alongside `@QAFTestStep` — never inside the description string

**Transaction time-tracking** for a group of steps in BDD:
```gherkin
start transaction for 'Login' with 10s threshold
    When user navigates to signin screen
    And user logins with random valid credentials
    Then validate user should be logged in
stop transaction
```

### Rule 12 — Assertion and Verification Methods

Use QAF built-in methods — never raw TestNG `Assert` for element checks:

| Method | Type | When to use |
|---|---|---|
| `element.verifyPresent()` | Soft | Element exists in DOM |
| `element.assertPresent()` | Hard | Element exists — fail test immediately if not |
| `element.verifyText("expected")` | Soft | Exact text match |
| `element.assertText(StringMatcher.contains("x"))` | Hard | Partial text with matcher |
| `element.verifyAttribute("class","val")` | Soft | Attribute value |
| `element.verifyCssClass("cls")` | Soft | CSS class present |
| `element.verifyCssStyle("prop","val")` | Soft | CSS style value |
| `element.verifyEnabled()` | Soft | Element is enabled |
| `element.verifyVisible()` | Soft | Element is visible |

**`verify*`** = soft assertion — records failure but continues test.  
**`assert*`** = hard assertion — stops test immediately on failure.

For non-element assertions use `Validator` with Hamcrest matchers:
```java
Validator.verifyThat(actualValue, Matchers.equalTo(expectedValue));
Validator.verifyThat("Page title", driver.getTitle(), Matchers.containsString("Dashboard"));
```

**Customising assertion messages** via properties:
```properties
element.present.pass=Element '{0}' is present as expected
element.present.fail=Element '{0}' is NOT present
# placeholders: {0}=desc, {1}=expected, {2}=actual
```

### Rule 13 — Wait Methods

Use element wait methods before interactions — never `Thread.sleep()`:

```java
element.waitForPresent();           // wait until in DOM
element.waitForVisible();           // wait until visible
element.waitForEnabled();           // wait until enabled
element.waitForSelected();          // wait until selected (checkbox/radio)
element.waitForCssClass("active");  // wait until CSS class is applied
element.waitForCssStyle("display","block"); // wait until CSS style matches
```

Wait timeout is controlled by `selenium.wait.timeout` (default 30000ms).

### Rule 14 — Data-Driven Steps

Steps that receive data from `@QAFDataProvider` accept `Map<String,Object>` or a typed DataBean:

```java
// Map-based — works with any CSV/JSON/Excel column
@QAFTestStep(description = "user submits login form")
public void submitLoginForm(Map<String, Object> data) {
    new LoginPage().login(
        String.valueOf(data.get("username")),
        String.valueOf(data.get("password"))
    );
}
```

In BDD feature file with data file:
```gherkin
@dataFile:resources/data/logindata.csv
Scenario: Data-driven login
  When user submits login form
```

## Output Format

For each step requested:

### Step Definition (`*Steps.java`)
```java
@MetaData(value = "{'groups':['<group>']}")
@QAFTestStep(description = "...")
public void methodName(String param) {
    new PageClass().methodName(param);
}
```

### Page Object Method (`*Page.java`)
```java
public void methodName(String param) {
    element.waitForPresent();
    element.sendKeys(param);
}
```

### Feature File Usage
```gherkin
@groups:['smoke']
Scenario: scenario name
  Given/When/Then the step description with "value"
```

## Task

The user will describe the step they want. Generate:
1. The `@QAFTestStep` method in the appropriate Steps class
2. The backing method in the Page class
3. Example usage in a `.feature` file with BDD2 metadata tags
4. Any new locator properties needed in `resources/locators/*.properties`
