---
name: qaf-databean
description: Scaffold QAF DataBean and FormDataBean classes — typed test data objects with @Randomizer, @UiElement, population from config/random, and use in data-driven tests.
---

You are scaffolding QAF DataBean classes for the Matrix automation project.

## What is a DataBean?

A DataBean is a typed Java class that holds test data. Instead of passing `Map<String,Object>` into data-driven tests, you define a class with typed fields and QAF maps data file columns to those fields automatically. Two base classes are available:

- **`BaseDataBean`** — holds and carries data only
- **`BaseFormDataBean`** — extends `BaseDataBean` with the ability to fill a web form directly from the bean

---

## Project Conventions

- Bean classes: `src/test/java/com/matrix/beans/`
- Data files: `resources/data/`

---

## Rules

### Rule 1 — Extend the Right Base Class

```java
// Data-only bean (no UI interaction)
public class LoginBean extends BaseDataBean { ... }

// Bean that can also fill a web form
public class ContactBean extends BaseFormDataBean { ... }
```

### Rule 2 — `@Randomizer` — Generate Random Test Data

Annotate fields with `@Randomizer` to auto-generate values when `fillRandomData()` is called:

| Parameter | Type | Purpose |
|---|---|---|
| `type` | `RandomizerTypes` | `LETTERS_ONLY`, `DIGITS_ONLY`, or default (mixed) |
| `length` | int | Number of characters |
| `minval` / `maxval` | String | Range for numeric or date values |
| `prefix` / `suffix` | String | Prepend/append fixed text |
| `format` | String | Pattern e.g. `"999-99-9999"` for SSN |
| `dataset` | String[] | Pick randomly from a list |
| `skip` | boolean | `true` = exclude this field from randomization |

```java
public class ContactBean extends BaseDataBean {

    @Randomizer(prefix = "http://www.", length = 4, suffix = ".com")
    private String webSiteURL;

    @Randomizer(length = 3, type = RandomizerTypes.DIGITS_ONLY)
    private String phoneAreaCode;

    @Randomizer(length = 3, type = RandomizerTypes.DIGITS_ONLY)
    private String faxAreaCode;

    @Randomizer(format = "999-99-9999")
    private String ssn;

    @Randomizer(dataset = {"Chrome", "Firefox", "Edge"})
    private String browser;

    @Randomizer(skip = true)
    private String fixedValue = "MATRIX";

    // Standard getters and setters for all fields
    public String getWebSiteURL() { return webSiteURL; }
    public void setWebSiteURL(String webSiteURL) { this.webSiteURL = webSiteURL; }
    // ... etc
}
```

### Rule 3 — `@UiElement` — Map Bean Fields to Web Form Elements (FormDataBean only)

Use `@UiElement` on `BaseFormDataBean` fields to wire each field to a page element:

| Parameter | Type | Purpose |
|---|---|---|
| `fieldLoc` | String | Locator key from `.properties` file |
| `fieldType` | String | Input type: `"text"` (default), `"optionbox"`, `"selectbox"`, `"checkbox"` |
| `order` | int | Fill sequence — lower numbers filled first |
| `dependsOnField` | String | Field name that must be filled before this one |
| `dependingValue` | String | Value of parent field that makes this field visible |

```java
public class RegistrationBean extends BaseFormDataBean {

    @UiElement(fieldLoc = "register.firstName", order = 1)
    private String firstName;

    @UiElement(fieldLoc = "register.lastName", order = 2)
    private String lastName;

    @UiElement(fieldLoc = "register.country", fieldType = "selectbox", order = 3)
    private String country;

    @UiElement(fieldLoc = "register.state", fieldType = "selectbox", order = 4,
               dependsOnField = "country", dependingValue = "India")
    private String state;   // only visible when country = India

    @UiElement(fieldLoc = "register.gender", fieldType = "optionbox", order = 5)
    private String gender;

    // Getters and setters...
}
```

### Rule 4 — Population Methods

```java
LoginBean user = new LoginBean();

// Populate with random data using @Randomizer annotations
user.fillRandomData("testdata.login");

// Populate from XML/properties config
user.fillFromConfig("testdata.login");
```

`bean.populate.random` property controls data set selection when multiple records exist:
- `false` (default) — sequential: first run uses record 1, second uses record 2, etc.
- `true` — random: picks a record randomly each run

```properties
bean.populate.random=false
```

### Rule 5 — Using DataBeans in Data-Driven Tests

**With `@QAFDataProvider` and typed bean parameter:**

```java
@QAFDataProvider(dataFile = "resources/data/logindata.csv")
@Test(description = "Login with multiple users")
public void login(LoginBean user) {
    new LoginPage().login(user.getUsername(), user.getPassword());
}
```

**With `Map<String, Object>` (column names = map keys):**

```java
@QAFDataProvider(dataFile = "resources/data/logindata.csv")
@Test(description = "Login with multiple users")
public void login(Map<String, Object> data) {
    new LoginPage().login(
        String.valueOf(data.get("username")),
        String.valueOf(data.get("password"))
    );
}
```

**In BDD feature file:**

```gherkin
@dataFile:resources/data/logindata.csv
Scenario: Data-driven login
  When user logs in with '${username}' and '${password}'
```

### Rule 6 — Supported Data File Formats

| Format | Notes |
|---|---|
| **CSV** | Comma-separated; first row = column headers = field names |
| **XML** | Nodes map to field names |
| **JSON** | Array of objects; keys = field names |
| **Excel (XLS/XLSX)** | Use `sheetName` metadata key to specify sheet |
| **Database** | Use `sql` metadata key with a SELECT query |

### Rule 7 — Record Identification

Add one of these columns to your data file to append row info to test result filenames and reports:

```
recid, summary, tcid, testcaseid
```

Example CSV with record ID:
```csv
recid,username,password
TC-Login-001,admin@matrix.com,Admin@123
TC-Login-002,user@matrix.com,User@456
```

### Rule 8 — Component Equality with DataBeans (`equals()` override)

Override `equals()` in `QAFWebComponent` subclasses to match a component against a bean — useful when searching a list:

```java
@Override
public boolean equals(Object obj) {
    if (obj instanceof LoginBean) {
        LoginBean bean = (LoginBean) obj;
        return usernameField.getText().equalsIgnoreCase(bean.getUsername());
    }
    if (obj instanceof String) {
        return usernameField.getText().equalsIgnoreCase((String) obj);
    }
    return super.equals(obj);
}
```

---

## Output Format

### DataBean Class (`src/test/java/com/matrix/beans/<Name>Bean.java`)

```java
package com.matrix.beans;

import com.qmetry.qaf.automation.data.BaseDataBean;
import com.qmetry.qaf.automation.util.Randomizer;
import com.qmetry.qaf.automation.util.RandomizerTypes;

public class <Name>Bean extends BaseDataBean {

    @Randomizer(length = 8, type = RandomizerTypes.LETTERS_ONLY)
    private String fieldName;

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}
```

### FormDataBean Class (if UI form filling is needed)

```java
package com.matrix.beans;

import com.qmetry.qaf.automation.data.BaseFormDataBean;
import com.qmetry.qaf.automation.ui.annotations.UiElement;

public class <Name>FormBean extends BaseFormDataBean {

    @UiElement(fieldLoc = "pagename.fieldname", order = 1)
    private String fieldName;

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}
```

### Data File (`resources/data/<name>data.csv`)

```csv
recid,fieldName,otherField
TC-001,value1,other1
TC-002,value2,other2
```

### Test Method Using the Bean

```java
@QAFDataProvider(dataFile = "resources/data/<name>data.csv")
@Test(description = "<description>")
public void testMethod(<Name>Bean data) {
    // use data.getFieldName() etc.
}
```

---

## Task

The user will describe the bean fields and whether UI form filling is needed. Generate:
1. The bean class with `@Randomizer` / `@UiElement` annotations as appropriate
2. A sample CSV data file with column headers matching field names
3. A test method showing how to use the bean with `@QAFDataProvider`
