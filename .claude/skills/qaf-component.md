---
name: qaf-component
description: Scaffold a QAF custom component (QAFWebComponent subclass) with nested child components, locators, data beans, and page wiring. Pass the component name and list of child elements/components needed.
---

You are scaffolding a QAF Custom Component for the Matrix automation project.

## What is a QAF Component?

A `QAFWebComponent` is a reusable wrapper around a section of the UI (e.g. a card, row, panel, dropdown group). It has its own `@FindBy` elements scoped inside its root locator, and can be nested inside pages or other components.

---

## Project Conventions

- Component classes: `src/test/java/com/matrix/components/`
- Page classes that use them: `src/test/java/com/matrix/pages/`
- Locator properties: `resources/locators/<pagename>.properties`
- Data bean classes (optional): `src/test/java/com/matrix/beans/`

---

## Rules

### Rule 1 — Base Class

Every component extends `QAFWebComponent` and must have a constructor that calls `super(locator)`:

```java
public class MyComponent extends QAFWebComponent {
    public MyComponent(String locator) {
        super(locator);
    }
}
```

### Rule 2 — Child Elements Inside a Component

Use `@FindBy` inside the component class for its child elements. All locators are scoped to the component root:

```java
public class Property extends QAFWebComponent {

    @FindBy(locator = "property.name")
    private QAFWebElement name;

    @FindBy(locator = "property.bookBtn")
    private QAFWebElement bookBtn;

    public Property(String locator) {
        super(locator);
    }

    public String getName() {
        return name.getText();
    }

    public void clickBook() {
        bookBtn.waitForPresent();
        bookBtn.click();
    }
}
```

### Rule 3 — Nested Child Components

A component can contain other components or `List<ChildComponent>`:

```java
public class Property extends QAFWebComponent {

    @FindBy(locator = "property.rateOption")
    private List<RateOption> rateOptions;   // list of child components

    @FindBy(locator = "property.rateCalendar")
    private RateCalendar rateCalendar;      // single child component

    public Property(String locator) {
        super(locator);
    }

    public static class RateOption extends QAFWebComponent {

        @FindBy(locator = "rateoption.label")
        private QAFWebElement label;

        @FindBy(locator = "rateoption.rateText")
        private QAFWebElement rateText;

        public RateOption(String locator) {
            super(locator);
        }
    }
}
```

### Rule 4 — Using Components in a Page

Declare the component with `@FindBy` in the page class, using `List<ComponentType>` for repeated items:

```java
public class SearchResultsPage extends WebDriverBaseTestPage<HomePage> {

    @FindBy(locator = "searchresults.property")
    private List<Property> properties;

    public List<Property> getProperties() {
        return properties;
    }

    private Property getProperty(Object key) {
        for (Property p : getProperties()) {
            if (p.equals(key)) return p;
        }
        return null;
    }

    @Override
    protected void openPage(PageLocator loc, Object... args) {
        parent.getSearchLink().click();
    }
}
```

### Rule 5 — Wiring the Component via `component-class` in Locator Properties

To bind a locator key to a specific component class, use the `component-class` field in the properties file:

```properties
searchresults.property={'locator':'css=.property','desc':'Property card on Search Results','component-class':'com.matrix.components.Property'}
searchresults.rateOption={'locator':'css=.rateOption','desc':'Rate option inside property card'}
searchresults.rateCalendar={'locator':'css=.multiRateCalendarLink','desc':'Browse Dates/Rates Calendar Link'}
```

### Rule 6 — Locator Strategy (same rules as pages)

- All locators go in `resources/locators/<pagename>.properties` — never hardcode in Java
- Format: `pagename.elementname={'locator':'strategy=value','desc':'Label'}`
- Preferred strategy order: `css` > `id` > `name` > `xpath`
- Fallback chain: `{'locator':['css=#a','name=b'],'desc':'Label'}`

### Rule 7 — `equals()` Override for Component Matching

Override `equals()` to support finding a component by string key or a data bean:

```java
@Override
public boolean equals(Object obj) {
    if (obj instanceof PropertyDataBean) {
        PropertyDataBean bean = (PropertyDataBean) obj;
        return (StringUtil.isBlank(bean.getPropertyName())
                || name.getText().trim().equalsIgnoreCase(bean.getPropertyName().trim()))
            && (StringUtil.isBlank(bean.getPropertyId())
                || getId().trim().equalsIgnoreCase(bean.getPropertyId().trim()));
    }
    if (obj instanceof String) {
        return name.getText().equalsIgnoreCase((String) obj);
    }
    return super.equals(obj);
}
```

### Rule 8 — Data Bean (Optional but Recommended for Complex Components)

Create a data bean extending `BaseDataBean` to carry comparison/search data:

```java
package com.matrix.beans;

import com.qmetry.qaf.automation.data.BaseDataBean;

public class PropertyDataBean extends BaseDataBean {
    private String propertyName;
    private String propertyId;
    private String cityStateZip;
    private Boolean isAvailable = null;

    public PropertyDataBean() {}

    // generate getters and setters for all fields
}
```

### Rule 9 — Element Interaction Rules (same as pages)

- Always call `element.waitForPresent()` before `element.sendKeys()`
- Always use `element.verifyPresent()` for assertions — never `Assert.assertTrue`
- Do NOT use `verifyVisible()` or pass arguments to `verifyPresent()`

### Rule 10 — Getter for Every `@FindBy` Field (MANDATORY)

Every `private QAFWebElement` field annotated with `@FindBy` **must** have a corresponding `public` getter that returns the `QAFWebElement` directly. The caller uses the getter to call `.getText()`, `.isPresent()`, `.verifyPresent()`, etc. — there is **no need** for separate text helper or state helper methods.

```java
@FindBy(locator = "componentname.elementname")
private QAFWebElement elementName;

// MANDATORY getter — caller uses element.getText(), element.isPresent(), etc.
public QAFWebElement getElementName() {
    return elementName;
}
```

**Naming convention:** `get` + field name with first letter capitalised (standard Java bean naming).

**Do NOT add text helpers or state helpers.** Methods like `getElementNameText()`, `isSomethingPresent()`, `isPopular()` are redundant — callers get the raw `QAFWebElement` from the getter and call the needed method directly:

```java
// WRONG — redundant helpers
public String getServiceNameText() { return serviceName.getText().trim(); }
public boolean isPopular() { return popularBadge.isPresent(); }

// CORRECT — caller uses getter
card.getServiceName().getText().trim();
card.getPopularBadge().isPresent();
```

**IMPORTANT — Avoid name clashes with `QAFWebComponent` / `QAFExtendedWebElement`:**  
Do NOT name a getter after a method already inherited from `QAFExtendedWebElement`:

| Field name | Wrong getter | Correct getter |
|---|---|---|
| `location` | `getLocation()` ❌ | `getLocationElement()` ✓ |
| `size` | `getSize()` ❌ | `getSizeElement()` ✓ |
| `text` | `getText()` ❌ | `getTextElement()` ✓ |

**Mandatory class section order:**
1. `@FindBy` fields (all `private`)
2. Constructor calling `super(locator)`
3. `// ── Getters ──` — one `public QAFWebElement getXxx()` per field
4. `// ── Actions ──` — `click`, `sendKeys`, `select` methods only
5. `// ── Verification ──` — compound checks like `verifyCardDisplayed()`
6. `equals()` override (only if matching/search support is needed)

**No `// ── Text helpers ──` or `// ── State helpers ──` sections — ever.**

---

## Output Format

### Component Class (`src/test/java/com/matrix/components/<Name>Component.java`)

```java
package com.matrix.components;

import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebComponent;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class <Name>Component extends QAFWebComponent {

    @FindBy(locator = "componentname.fieldOne")
    private QAFWebElement fieldOne;

    @FindBy(locator = "componentname.fieldTwo")
    private QAFWebElement fieldTwo;

    public <Name>Component(String locator) {
        super(locator);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public QAFWebElement getFieldOne() {
        return fieldOne;
    }

    public QAFWebElement getFieldTwo() {
        return fieldTwo;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void clickFieldTwo() {
        fieldTwo.waitForPresent();
        fieldTwo.click();
    }

    // ── Verification ─────────────────────────────────────────────────────────

    public void verifyComponentDisplayed() {
        fieldOne.verifyPresent();
        fieldTwo.verifyPresent();
    }
}
```

### Locator File (`resources/locators/<pagename>.properties`)

All locator keys **must** have a `.loc` suffix:
```properties
pagename.componentname.loc={'locator':'css=.component-root','desc':'Component root','component-class':'com.matrix.components.<Name>Component'}
componentname.elementname.loc={'locator':'css=.child','desc':'Child element inside component'}
```

The same `.loc`-suffixed key is used in `@FindBy`:
```java
@FindBy(locator = "componentname.elementname.loc")
private QAFWebElement elementName;
```

### Page Wiring (inside the page that contains the component)

```java
@FindBy(locator = "pagename.componentname")
private List<<Name>Component> items;

public List<<Name>Component> getItems() {
    return items;
}
```

---

## Task

The user will name the component and list child elements / nested components needed. Generate:
1. The component class in `src/test/java/com/matrix/components/`
2. Locator properties entries in the appropriate `resources/locators/*.properties`
3. Page wiring snippet showing how to declare and use it in a page class
4. Data bean class if the component needs matching/search support
