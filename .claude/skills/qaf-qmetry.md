---
name: qaf-qmetry
description: Configure QAF integration with QMetry test management — connection properties, test case mapping via @QmetryTestCase, all 5 integration scenarios, and custom TestCaseResultUpdator for any TMS.
---

You are configuring QAF integration with QMetry (and other test management tools) for the Matrix automation project.

## What is QAF-QMetry Integration?

QAF can automatically push test execution results to QMetry (or any test management tool) after each scenario completes. This is done via:
1. **Property-based config** — connection details in `application.properties`
2. **`@QmetryTestCase` annotation** — maps a Java test method to a QMetry test case ID
3. **`TestCaseResultUpdator` interface** — generic hook to push results to any TMS

---

## Project Conventions

- Config: `resources/application.properties` or `resources/<env>/env.properties`
- Test classes: `src/test/java/com/matrix/tests/`
- Custom updator class: `src/test/java/com/matrix/integration/`

---

## Rules

### Rule 1 — Mandatory Connection Properties

All of these are required for QMetry server connection (unless using QMetry scheduler XML):

```properties
integration.param.qmetry.service.url=https://qmetry.example.com/qcbin
integration.param.qmetry.user=avnish.choksi@matrix.com
integration.param.qmetry.pwd=<password_or_encrypted_value>
integration.param.qmetry.project=MATRIX_PROJECT
integration.param.qmetry.release=Release_1.0
integration.param.qmetry.cycle=Sprint_1
integration.param.qmetry.suitid=SUITE-001
integration.param.qmetry.build=Build_20260601
integration.param.qmetry.platform=Web_Chrome
integration.param.qmetry.drop=Drop_1
```

> **Tip:** Encrypt the password using QAF's `encrypted.` prefix to avoid plain text storage — see `/qaf-property` Rule 5.
> ```properties
> encrypted.integration.param.qmetry.pwd=Q2hpcmFnMTIzIw==
> ```

---

### Rule 2 — Optional Properties

```properties
# Use existing suite run instead of creating a new one
integration.param.qmetry.suitrunid=SUITERUN-456

# Suite path in QMetry tree
integration.param.qmetry.suit.path=/Matrix/Regression

# Description for the suite run
integration.param.qmetry.suit.rundesc=Automated regression run for Sprint 1

# QMetry scheduler XML (when provided, only url/user/pwd needed)
qmetry.schedule.file=resources/qmetry-schedule.xml

# Upload test attachments (screenshots, logs) to QMetry
integration.tool.qmetry.uploadattachments=true

# Enable QMetry integration flag
integration.tool.qmetry=true
```

---

### Rule 3 — Mapping Test Cases with `@QmetryTestCase`

#### By Test Case ID (creates new run under the suite)

```java
import com.qmetry.qaf.automation.integration.qmetry.QmetryTestCase;

@QmetryTestCase(TC_ID = "12345")
@Test(description = "Verify login with valid credentials")
public void verifyValidLogin() {
    // test code
}
```

#### By Test Case Run ID (updates an existing run)

```java
@QmetryTestCase(runId = "67890")
@Test(description = "Verify login with valid credentials")
public void verifyValidLogin() {
    // test code
}
```

---

### Rule 4 — Five Integration Scenarios

| Scenario | Annotation | Key Properties Required |
|---|---|---|
| **1** — New suite run + TC by ID | `@QmetryTestCase(TC_ID="...")` | All mandatory + `suitid` |
| **2** — Existing suite run + TC by ID | `@QmetryTestCase(TC_ID="...")` | All mandatory + `suitrunid` (instead of `suitid`) |
| **3** — Existing suite run + TC run by runId | `@QmetryTestCase(runId="...")` | All mandatory + `suitrunid` |
| **4** — Same as Scenario 3 | `@QmetryTestCase(runId="...")` | All mandatory + `suitrunid` |
| **5** — No annotation (creates new TC in QMetry) | *(none)* | Mandatory properties excluding suite identifiers |

**Scenario 1** — most common for CI/CD: each run creates a new suite run, maps by TC ID.
**Scenario 2/3** — use when the suite run already exists and you want to update it.
**Scenario 5** — QMetry auto-creates the test case entry from the test description.

---

### Rule 5 — Providing Properties (Multiple Ways)

**1. `application.properties` file (recommended):**
```properties
integration.tool.qmetry=true
integration.param.qmetry.service.url=https://qmetry.example.com
```

**2. TestNG XML `<parameter>` (overrides file):**
```xml
<suite name="Matrix Suite">
  <test name="Regression">
    <parameter name="integration.tool.qmetry" value="true"/>
    <parameter name="integration.param.qmetry.build" value="Build_20260601"/>
    <classes>
      <class name="com.matrix.tests.LoginTest"/>
    </classes>
  </test>
</suite>
```

**3. Maven CLI (highest priority — overrides everything):**
```bash
mvn test \
  -Dintegration.tool.qmetry=true \
  -Dintegration.param.qmetry.build=Build_20260601 \
  -Dintegration.param.qmetry.cycle=Sprint_2
```

**4. QMetry Scheduler XML** (when `qmetry.schedule.file` is set — only url/user/pwd needed in properties):
```properties
qmetry.schedule.file=resources/qmetry-schedule.xml
integration.param.qmetry.service.url=https://qmetry.example.com
integration.param.qmetry.user=avnish
integration.param.qmetry.pwd=secret
```

---

### Rule 6 — Custom Result Updator (for any TMS — Jira, Azure DevOps, etc.)

Implement `TestCaseResultUpdator` to push results to any test management tool.

**Step 1 — Create the implementation class:**

```java
package com.matrix.integration;

import com.qmetry.qaf.automation.integration.TestCaseResultUpdator;
import com.qmetry.qaf.automation.integration.TestCaseRunResult;

public class MatrixResultUpdator implements TestCaseResultUpdator {

    @Override
    public String getToolName() {
        return "MatrixTMS";
    }

    @Override
    public boolean updateResult(TestCaseRunResult result) {
        // Called after every test case / scenario completes
        String testName   = result.getTestCaseName();
        boolean passed    = result.isPassed();
        Map<?, ?> metadata = result.getMetaData();

        // push to your TMS API here...
        return true;   // return true on success, false to signal failure
    }

    @Override
    public boolean allowConfigAndRetry() {
        return false;  // true = also report @Configuration and retry attempts
    }

    @Override
    public boolean allowParallel() {
        return true;   // true = multi-threaded updates (default); false = single-thread
    }

    @Override
    public void beforeShutDown() {
        // cleanup — close connections, flush buffers, etc.
    }

    @Override
    public boolean enabled() {
        // conditionally enable — e.g. only in CI
        return Boolean.parseBoolean(
            System.getProperty("tms.integration.enabled", "true")
        );
    }
}
```

**Step 2 — Register in `application.properties`:**

```properties
# Single updator
result.updator=com.matrix.integration.MatrixResultUpdator

# Multiple updators (comma-separated)
result.updator=com.matrix.integration.MatrixResultUpdator,com.matrix.integration.SlackNotifier
```

**QMetry's own built-in updator** is registered automatically when `integration.tool.qmetry=true` — you do not need to add it to `result.updator`.

---

### Rule 7 — `TestCaseRunResult` — Data Available in `updateResult()`

| Method | Returns | Description |
|---|---|---|
| `getTestCaseName()` | `String` | Scenario / test method name |
| `isPassed()` | `boolean` | `true` if the test passed |
| `getMetaData()` | `Map` | All metadata (groups, TestID, author, channel, etc.) |
| `getStartTime()` | `long` | Test start timestamp (ms) |
| `getEndTime()` | `long` | Test end timestamp (ms) |
| `getThrowable()` | `Throwable` | Exception if test failed, `null` if passed |

---

## Full QMetry Configuration Block

Add this to `resources/application.properties` (or pass keys via Maven/TestNG XML):

```properties
# ── QMetry Integration ───────────────────────────────────────────
integration.tool.qmetry=true
integration.tool.qmetry.uploadattachments=true

# Connection
integration.param.qmetry.service.url=https://qmetry.example.com/qcbin
integration.param.qmetry.user=avnish.choksi@matrix.com
encrypted.integration.param.qmetry.pwd=Q2hpcmFnMTIzIw==

# Project hierarchy
integration.param.qmetry.project=MATRIX
integration.param.qmetry.release=Release_1.0
integration.param.qmetry.cycle=Sprint_1
integration.param.qmetry.build=Build_${expr:java.time.LocalDate.now()}
integration.param.qmetry.platform=Web_Chrome
integration.param.qmetry.drop=Drop_1

# Suite
integration.param.qmetry.suitid=SUITE-001
# integration.param.qmetry.suitrunid=SUITERUN-456   ← use for existing run

# Jira link in reports (optional)
jira.url=https://matrix.atlassian.net/browse
metadata.formatter.storyKey=<a href="${jira.url}/{0}">{0}</a>
```

---

## Test Class Template with QMetry Annotation

```java
package com.matrix.tests;

import com.qmetry.qaf.automation.integration.qmetry.QmetryTestCase;
import org.testng.annotations.Test;

public class LoginTest {

    @QmetryTestCase(TC_ID = "TC-1001")
    @Test(description = "Verify valid login", groups = {"smoke", "regression"})
    public void verifyValidLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.launchPage(null);
        loginPage.enterUsername("admin@example.com");
        loginPage.enterPassword("secret");
        loginPage.clickLogin();
        new DashboardPage().verifyPageLoaded();
    }

    @QmetryTestCase(TC_ID = "TC-1002")
    @Test(description = "Verify invalid login shows error", groups = {"regression"})
    public void verifyInvalidLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.launchPage(null);
        loginPage.enterUsername("wrong@example.com");
        loginPage.enterPassword("badpass");
        loginPage.clickLogin();
        loginPage.verifyErrorMessage();
    }
}
```

---

## Output Format

When the user asks to:

**Set up QMetry integration:** Generate the full property block for `application.properties` and show the Maven CLI override pattern.

**Map a test to QMetry:** Add `@QmetryTestCase(TC_ID="...")` to the test method and confirm the scenario number being used.

**Build a custom updator:** Generate the full `TestCaseResultUpdator` implementation class and the `result.updator` registration line.

**Override build/cycle at runtime:** Show the Maven `-D` flags for the relevant properties.

Always specify which scenario (1–5) applies based on the user's setup.
