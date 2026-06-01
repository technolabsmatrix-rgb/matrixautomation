---
name: qaf-property
description: Reference and scaffold QAF application properties — all built-in property keys, default values, override precedence, encryption, and how to read properties in Java and BDD.
---

You are managing QAF application properties for the Matrix automation project.

## Sample Project Reference

Canonical config files are inside the zip at:
```
.claude/resources/qaf-blank-project-maven-master.zip
→ resources/application.properties
→ resources/env1/env.properties
→ resources/env2/env.properties
```

The sample uses `env.resources=resources` + `resources.load.subdirs=1` — QAF auto-discovers all `.properties` files under the `resources/` tree. This is the preferred pattern for new projects (see `/qaf-resource` for details).

## What are QAF Properties?

QAF uses a central `ConfigurationManager` backed by the `ApplicationProperties` enum. Every framework behaviour — driver, waits, screenshots, listeners, reporting, locale, encryption — is controlled by a named property key. Properties can be set in files, TestNG XML, or JVM system properties.

---

## Project Conventions

- Master config: `resources/application.properties`
- Environment overrides: `resources/<env>/env.properties`
- Common shared config: `resources/common/common.properties`

---

## Rules

### Rule 1 — Property Override Precedence (Highest → Lowest)

```
1. JVM System property   (-Dkey=value on command line)   ← highest
2. TestNG XML parameter  (<parameter name="key" value="val"/>)
3. Properties file       (application.properties / env.properties)  ← lowest
```

Later-listed `env.resources` directories override earlier ones within the same level.

**Example:** If `env.properties` has `brand.name=westin` and TestNG XML has `<parameter name="brand.name" value="whotels"/>`, the value used is `whotels`.

---

### Rule 2 — Reading Properties in Java

```java
import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

// String
String url = getBundle().getString("env.baseurl");

// With default fallback
String env = getBundle().getString("env.name", "dev");

// Integer
int timeout = getBundle().getInt("selenium.wait.timeout", 30000);

// Boolean
boolean parallel = getBundle().getBoolean("global.datadriven.parallel", false);

// Set at runtime
getBundle().setProperty("my.flag", "true");

// Using ApplicationProperties enum (type-safe)
String baseUrl = ApplicationProperties.SELENIUM_BASE_URL.getStringVal();
int retries    = ApplicationProperties.RETRY_CNT.getIntVal(0);
boolean ssl    = ApplicationProperties.HTTPS_ACCEPT_ALL_CERT.getBoolenVal(false);
```

---

### Rule 3 — Reading Properties in BDD / Feature Files

Use `${key}` substitution directly in step text:
```gherkin
Given user logs in with '${admin.user.name}' and '${admin.user.pwd}'
Then the base url should be '${env.baseurl}'
```

---

### Rule 4 — Providing Properties via TestNG XML

```xml
<suite name="Matrix Test Suite" verbose="0" parallel="false">
  <test name="Staging Run">
    <parameter name="env.name"    value="staging"/>
    <parameter name="driver.name" value="chromeDriver"/>
    <parameter name="brand.name"  value="whotels"/>
    <classes>
      <class name="com.matrix.tests.LoginTest"/>
    </classes>
  </test>
</suite>
```

---

### Rule 5 — Encrypted Properties (QAF 2.1.13+)

Store sensitive values Base64-encoded with the `encrypted.` prefix. QAF decrypts automatically — retrieve using the key **without** the prefix.

**In properties file:**
```properties
encrypted.admin.user.pwd=Q2hpcmFnMTIzIw==
encrypted.db.password=U2VjcmV0UGFzcw==
```

**In Java — read without prefix:**
```java
String pwd = getBundle().getString("admin.user.pwd");  // decrypted automatically
element.sendKeys(pwd);
```

**Custom decryptor** (implement `PasswordDecryptor`):
```properties
password.decryptor.impl=com.matrix.security.AESPasswordDecryptor
```

**Prevent plain-text logging** — mark the locator as `type:password`:
```properties
login.passwordField={"locator":"xpath=//*[@name='password']","desc":"Password field","type":"password"}
```

---

## Complete Property Reference

### Driver & Browser

| Property Key | Default | Description |
|---|---|---|
| `driver.name` | — | Driver type: `chromeDriver`, `firefoxDriver`, `iexplorerDriver`, `safariDriver`, `androidDriver`, `iosDriver`, `firefoxRemoteDriver`, `chromeRemoteDriver` etc. |
| `remote.server` | `localhost` | Selenium Grid / Appium server host |
| `remote.port` | `4444` | Selenium Grid / Appium server port |
| `webdriver.remote.session` | — | Existing WebDriver session ID (for debugging) |
| `webdriver.chrome.driver` | — | Path to `chromedriver` executable |
| `webdriver.ie.driver` | — | Path to `IEDriverServer` executable |
| `driver.init.retry.timeout` | `0` | Timeout (10-second increments) to retry driver init on failure |
| `https.accept.all.cert` | `false` | Trust all SSL certificates; disable hostname verification |
| `driver.additional.capabilities` | — | JSON map of capabilities applied to any driver |
| `<drivername>.additional.capabilities` | — | JSON map of capabilities for a specific driver only |
| `driver.capabilities.<cap>` | — | Individual capability key-value (prefix form) |
| `<drivername>.capabilities.<cap>` | — | Driver-specific capability (prefix form) |
| `driverClass` | — | Fully-qualified custom driver class name (used with `otherDriver`) |

### Waits & Timing

| Property Key | Default | Description |
|---|---|---|
| `selenium.wait.timeout` | `30000` | Default timeout (ms) for element waits and wait-service methods |
| `commands.execution.interval` | — | Pause (ms) between consecutive Selenium commands |
| `selenium.skip.autowait` | `false` | Comma-separated commands to exclude from auto-wait |
| `auto.wait.include.commands` | — | Additional commands to include in auto-wait |

### Screenshots & Reporting

| Property Key | Default | Description |
|---|---|---|
| `selenium.success.screenshots` | `1` | Capture screenshot on checkpoint **success** (`1`=yes, `0`=no) |
| `selenium.failure.screenshots` | `1` | Capture screenshot on checkpoint **failure** (`1`=yes, `0`=no) |
| `selenium.screenshots.dir` | — | Directory to save screenshots |
| `selenium.screenshots.relative.path` | — | Relative path used in HTML report links |
| `report.log.level` | `Info` | Minimum level to log: `Info`, `Pass`, `Warn`, `Fail` |
| `report.log.skip.success` | `false` | `true` hides success verification messages from report |
| `reporter.log.exclude.commands` | — | Comma-separated commands excluded from HTML report |
| `test.results.dir` | — | Directory for JSON/HTML test results |
| `tc.identifier.key` | `testCaseId` | Metadata key used as filename for test result files |
| `metadata.formatter.<key>` | — | MessageFormat string to render metadata as HTML link (e.g. Jira) |

### Listeners

| Property Key | Default | Description |
|---|---|---|
| `qaf.listeners` | — | Comma-separated fully-qualified classes implementing any QAF listener |
| `teststep.listeners` | — | `QAFTestStepListener` implementations |
| `wd.command.listeners` | — | `QAFWebDriverCommandListener` implementations |
| `we.command.listeners` | — | `QAFWebElementCommandListener` implementations |
| `selenium.command.listeners` | — | Legacy Selenium command listener implementations |
| `element.default.listener` | `true` | Attach `ElementMetaDataListener` to all elements |
| `element.default.metadata` | — | JSON map of default metadata applied to every `QAFWebElement` |

### Test Execution

| Property Key | Default | Description |
|---|---|---|
| `scenario.file.loc` | `scenarios` | File/folder containing BDD/keyword scenarios to run |
| `step.provider.pkg` | — | Comma-separated packages QAF scans for `@QAFTestStep` methods |
| `step.provider.sharedinstance` | `false` | Share a single instance of step class across all steps in that class |
| `global.datadriven.parallel` | `false` | `true` runs data-driven iterations in parallel |
| `bean.populate.random` | `false` | `true` fills data beans randomly from available datasets |
| `retry.count` | `0` | Number of times to retry a failed test |
| `retry.analyzer` | — | Fully-qualified `IRetryAnalyzer` implementation |
| `selenium.singletone` | — | Driver instance scope: `Tests`, `Methods`, or `Groups` |
| `selenium.auto.shutdown` | — | Auto-shutdown Selenium server after run |
| `include` | — | Metadata filter — run only matching scenarios (see `/qaf-metadata`) |
| `exclude` | — | Metadata filter — skip matching scenarios (see `/qaf-metadata`) |

### Resources & Environment

| Property Key | Default | Description |
|---|---|---|
| `env.baseurl` | — | Base URL of the application under test |
| `env.resources` | `resources` | Semicolon-separated list of resource dirs/files to load |
| `env.load.locales` | — | Semicolon-separated locale names to load (e.g. `en;hi;fr`) |
| `env.default.locale` | — | Default locale key for `getBundle().getString()` |
| `<drivername>.resources` | — | Resource dir loaded only when that driver is active (QAF 2.1.12+) |

### Security & Encryption

| Property Key | Default | Description |
|---|---|---|
| `password.decryptor.impl` | — | Fully-qualified class implementing `PasswordDecryptor` (default: Base64) |
| `encrypted.<key>` | — | Prefix to store an encrypted value; retrieve using `<key>` without prefix |

### Proxy

| Property Key | Default | Description |
|---|---|---|
| `proxy.server` | — | Proxy server address |
| `proxy.port` | `80` | Proxy server port |
| `host.to.proxy` | — | Semicolon-separated hosts to route through the proxy |

### Network & REST

| Property Key | Default | Description |
|---|---|---|
| `selenium.capture.network.traffic` | `false` | Capture browser network traffic |
| `rest.client.impl` | — | Fully-qualified `RestClientFactory` implementation |

### QMetry / Jira / QC Integration

| Property Key | Description |
|---|---|
| `integration.param.jira.baseurl` | Jira base URL |
| `integration.param.jira.username` | Jira username |
| `integration.param.jira.password` | Jira password |
| `integration.param.jira.project` | Jira project key |
| `integration.param.qmetry.service.url` | QMetry web service URL |
| `integration.param.qmetry.user` | QMetry username |
| `integration.param.qmetry.pwd` | QMetry password |
| `integration.param.qmetry.project` | QMetry project |
| `integration.param.qmetry.release` | QMetry release |
| `integration.param.qmetry.cycle` | QMetry cycle |
| `integration.param.qmetry.build` | QMetry build ID |
| `integration.param.qmetry.suitid` | QMetry suite ID |
| `integration.tool.qmetry` | Enable QMetry integration (`true`/`false`) |
| `integration.tool.qmetry.uploadattachments` | Upload attachments to QMetry |
| `qc.service.url` | HP ALM / QC service URL |
| `qc.domain` | QC domain |
| `qc.project` | QC project |
| `qc.user` | QC username |
| `qc.pwd` | QC password |
| `qc.testcase.folder.path` | QC test case folder path |
| `qc.testset.folder.path` | QC test set folder path |
| `qc.testset.name` | QC test set name |
| `qc.run.name` | QC run name |
| `qc.timezone` | QC server timezone |

### Runtime / Internal (read-only)

| Property Key | Description |
|---|---|
| `current.testcase.name` | Name of the currently executing test |
| `current.testcase.desc` | Description of the currently executing test |
| `current.testcase.result` | `ITestResult` object for the running test |
| `tng.context` | `ITestContext` object for the running test |
| `isfw.version` | QAF framework version |
| `isfw.revision` | QAF framework revision |
| `isfw.build.date` | QAF framework build date |

---

### Driver Capabilities (Priority Order — Highest to Lowest)

QAF merges capabilities from 4 property sources in this priority order:

| Priority | Property Key Pattern | Scope |
|---|---|---|
| 1 (highest) | `<driverName>.capabilities.<capName>` | Specific driver, specific cap |
| 2 | `<driverName>.additional.capabilities` | Specific driver, all caps as JSON map |
| 3 | `driver.capabilities.<capName>` | All drivers, specific cap |
| 4 (lowest) | `driver.additional.capabilities` | All drivers, all caps as JSON map |

```properties
# Specific driver, individual capability
firefox.capabilities.version=48
appium.capabilities.automationName=Appium
appium.capabilities.applicationName=Calculator

# Specific driver, all capabilities as JSON
appium.additional.capabilities={'user':'user1@co.com','password':'pass','automationName':'Appium'}

# All drivers, individual capability
driver.capabilities.platform=Windows
driver.capabilities.user=myCloudeUser

# All drivers, all capabilities as JSON
driver.additional.capabilities={'user':'user1@co.com','password':'pass','platform':'Windows'}
```

### BDD Runner Configuration

Three factory classes for running BDD scenarios via TestNG XML:

| Format | Factory Class |
|---|---|
| BDD (QAF classic) | `com.qmetry.qaf.automation.step.client.text.BDDTestFactory` |
| BDD2 (preferred) | `com.qmetry.qaf.automation.step.client.text.BDDTestFactory2` |
| Gherkin/Cucumber | `com.qmetry.qaf.automation.step.client.gherkin.GherkinScenarioFactory` |

```xml
<!-- Basic BDD2 run -->
<test name="Matrix-BDD2">
  <classes>
    <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
  </classes>
</test>

<!-- With metadata filter -->
<test name="Matrix-Smoke">
  <parameter name="include" value="{'groups':['smoke']}"/>
  <classes>
    <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
  </classes>
</test>

<!-- Specific scenario folder + step package -->
<test name="Matrix-Login">
  <parameter name="scenario.file.loc" value="scenarios/login;scenarios/home"/>
  <parameter name="step.provider.pkg" value="com.matrix.steps.common;com.matrix.steps.web"/>
  <classes>
    <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
  </classes>
</test>

<!-- Parallel multi-platform run -->
<suite name="Matrix Suite" parallel="true">
  <test name="Web Tests">
    <parameter name="driver.name" value="chromeDriver"/>
    <parameter name="step.provider.pkg" value="com.matrix.steps.common;com.matrix.steps.web"/>
    <classes>
      <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
    </classes>
  </test>
  <test name="Mobile Tests">
    <parameter name="driver.name" value="androidRemoteDriver"/>
    <parameter name="remote.server" value="10.12.48.87"/>
    <parameter name="remote.port" value="8080"/>
    <parameter name="step.provider.pkg" value="com.matrix.steps.common;com.matrix.steps.mobile"/>
    <classes>
      <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
    </classes>
  </test>
</suite>
```

Key BDD config properties:

| Property | Default | Description |
|---|---|---|
| `scenario.file.loc` | `scenarios` | Semicolon-separated files/folders containing scenarios |
| `txt.scenario.file.ext` | `.bdd` / `.feature` | File extension for scenario files |
| `step.provider.pkg` | — | Semicolon-separated packages to scan for `@QAFTestStep` |

### Retry Analyzer

```properties
# Default — retry up to N times on any exception (not on checkpoint failure)
retry.count=2

# Custom — specify your own IRetryAnalyzer (overrides retry.count)
retry.analyzer=com.matrix.listeners.CustomRetryAnalyzer
```

Custom implementation:
```java
public class CustomRetryAnalyzer implements IRetryAnalyzer {
    @Override
    public boolean retry(ITestResult result) {
        // return true to retry, false to stop
        return false;
    }
}
```

> **Important:** `retry.count` is ignored when `retry.analyzer` is set — they are mutually exclusive.  
> Built-in analyzer does **NOT** retry on checkpoint/assertion failures — only on exceptions.

### Debugging with Existing Driver Session

Connect QAF to an already-open browser session to debug tests against live state:

```properties
driver.name=firefoxRemoteDriver
webdriver.remote.session=<SESSION_ID_FROM_HUB>
```

Steps:
1. Start Selenium server, open `http://localhost:4444/wd/hub/static/resource/hub.html`
2. Create a new session → copy the Session ID
3. Set the two properties above
4. Navigate the browser manually to the desired state
5. Run test snippets — they attach to the existing session

---

## Standard `application.properties` Full Template

> Follows the sample project pattern (`env.resources=resources` + `resources.load.subdirs=1`). The surefire plugin in `pom.xml` injects `outputDir`, `selenium.screenshots.dir`, etc. at runtime — those do not need to be in this file.

```properties
# ── Base URL ─────────────────────────────────────────────────────
env.baseurl=https://www.example.com

# ── Resource loading (sample project pattern) ─────────────────────
env.resources=resources
resources.load.subdirs=1

# ── Step provider ─────────────────────────────────────────────────
step.provider.pkg=com.matrix.steps

# ── Driver ────────────────────────────────────────────────────────
remote.server=localhost
remote.port=4444
driver.name=chromeDriver

# ── Waits ─────────────────────────────────────────────────────────
selenium.wait.timeout=30000
commands.execution.interval=0

# ── Screenshots ───────────────────────────────────────────────────
selenium.success.screenshots=1

# ── Reporting ─────────────────────────────────────────────────────
report.log.level=Info
report.log.skip.success=0
tc.identifier.key=TestID

# ── Test Execution ────────────────────────────────────────────────
retry.count=0
global.datadriven.parallel=false

# ── Listeners ─────────────────────────────────────────────────────
qaf.listeners=com.matrix.listeners.MatrixListener
element.default.listener=true

# ── Locale (uncomment if i18n needed) ─────────────────────────────
# env.load.locales=en
# env.default.locale=en

# ── Security ──────────────────────────────────────────────────────
# encrypted.admin.user.pwd=<base64value>
# password.decryptor.impl=com.matrix.security.AESDecryptor

# ── Jira (optional) ───────────────────────────────────────────────
# jira.url=https://yourcompany.atlassian.net/browse
# metadata.formatter.storyKey=<a href="${jira.url}/{0}">{0}</a>

# ── Driver-specific resources ─────────────────────────────────────
# android.resources=resources/android
# ios.resources=resources/ios
```

---

## Output Format

When the user asks about a property:
1. Give the exact key string, default value, and purpose
2. Show the `application.properties` entry
3. Show the Java access pattern (`getBundle()` or `ApplicationProperties` enum)
4. Show the Maven CLI override (`-Dkey=value`) if runtime override is relevant

When the user asks to add encrypted credentials:
1. Show the `encrypted.<key>` entry in the properties file
2. Show the Java read pattern (without the `encrypted.` prefix)
3. Show locator `type:password` if the field should mask values in reports
