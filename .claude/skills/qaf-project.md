---
name: qaf-project
description: Master skill — creates a new QAF project from scratch or converts an existing project to QAF. Triggered by "create a new QAF Project" or "Convert this project to QAF Project". Asks 3 confirmation questions first, then scaffolds the full project structure using all QAF skills.
---

You are the master QAF project scaffolding skill for the Matrix automation team.

## Sample Project Reference

The canonical working QAF blank project is stored as a zip at:
```
.claude/resources/qaf-blank-project-maven-master.zip
```

Extract it locally if you need to inspect it. **Always use this as the ground truth** for:
- `pom.xml` structure (AspectJ plugin, exec-maven-plugin, `LATEST` versions, surefire with timestamp output dir)
- `config/testrun_config.xml` structure (BDD + Java test blocks)
- `resources/application.properties` base config (`env.resources=resources`, `resources.load.subdirs=1`)
- `resources/search.properties` — canonical locator file format
- `src/test/java/.../steps/StepsLibrary.java` — canonical step class with `static` methods and `import static CommonStep.*`
- `src/test/java/.../test/SampleTest.java` — canonical Java test extending `WebDriverTestCase`

When scaffolding a new project, copy and adapt the structure from this sample rather than inventing it from scratch. File paths inside the zip:
| Purpose | Path inside zip |
|---|---|
| Maven build | `pom.xml` |
| TestNG suite | `config/testrun_config.xml` |
| App config | `resources/application.properties` |
| Locators | `resources/search.properties` |
| Steps | `src/test/java/com/qmetry/qaf/example/steps/StepsLibrary.java` |
| Java test | `src/test/java/com/qmetry/qaf/example/test/SampleTest.java` |
| Feature file | `scenarios/suite1.feature` |
| Env override | `resources/env1/env.properties` |

## Trigger Phrases

This skill activates when the user says any of:
- "create a new QAF Project"
- "Convert this project to QAF Project"
- "setup QAF project"
- "initialise QAF project"

---

## Phase 1 — ALWAYS Ask These Questions First (NEVER skip)

Before writing a single file, ask the user ALL THREE questions:

```
Before I scaffold the project, I need a few details:

1. **Project name** — What is the project name? (used for Java package, Maven artifactId, directory name)

2. **Environments** — How many environments are there (dev, staging, prod, etc.)?
   For each environment, please provide:
   - Environment name (e.g. dev, staging, uat, prod)
   - Base URL
   - Login credentials (username / password) if any
   - Any other environment-specific config (API keys, DB URLs, etc.)

3. **Localisation / i18n** — Does the project use multiple languages or locales?
   - If yes: which locales? (e.g. en, hi, fr, de) — which is the default?
   - If no: we will set up English only
```

Wait for the user's answers before proceeding to Phase 2.

---

## Phase 2 — Scaffold the Full Project

Once answers are received, execute ALL of the following steps in order.

---

### STEP 1 — Confirm the Plan

Echo back a summary of what will be created:

```
Here is what I will create:

Project: <name>
Package: com.matrix.<name>
Environments: <list>
Locales: <list or "en only">

Directory structure:
  config/
  lib/
  resources/
    application.properties
    common/
    locators/
    data/
    <env1>/
    <env2>/
    ...
  scenarios/
  src/test/java/com/matrix/<name>/
    steps/
    pages/
    components/
    listeners/
    beans/
    tests/
  test-results/

Shall I proceed?
```

Wait for confirmation before writing files.

---

### STEP 2 — Create Directory Structure

Create all folders:

```
config/
lib/
resources/
resources/common/
resources/locators/
resources/data/
resources/<env1>/         ← one per environment
resources/<env2>/
scenarios/
src/test/java/com/matrix/<projectname>/steps/
src/test/java/com/matrix/<projectname>/pages/
src/test/java/com/matrix/<projectname>/components/
src/test/java/com/matrix/<projectname>/listeners/
src/test/java/com/matrix/<projectname>/beans/
src/test/java/com/matrix/<projectname>/tests/
test-results/
```

---

### STEP 3 — Static Files (always the same regardless of project)

#### `config/testng.xml`

> **Derived from** `.claude/resources/qaf-blank-project-maven-master/config/testrun_config.xml`.
> The sample includes both a BDD2 block (for feature files) and a Java Test block (for `WebDriverTestCase` subclasses). Include both unless the project uses only one style.

```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="<ProjectName> Test Suite" verbose="0">

  <test name="BDD Test" enabled="true">
    <classes>
      <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
    </classes>
  </test>

  <test name="Java Test" enabled="true">
    <classes>
      <class name="com.matrix.<projectname>.tests.SampleTest"/>
    </classes>
  </test>

</suite>
```

#### `config/testng-parallel.xml`

```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="<ProjectName> Parallel Suite" verbose="0" parallel="tests" thread-count="2">

  <test name="Web-Tests">
    <parameter name="env.name" value="dev"/>
    <parameter name="driver.name" value="chromeDriver"/>
    <parameter name="step.provider.pkg" value="com.matrix.<projectname>.steps"/>
    <classes>
      <class name="com.qmetry.qaf.automation.step.client.text.BDDTestFactory2"/>
    </classes>
  </test>

</suite>
```

#### `resources/common/common.properties`

```properties
# ── Shared config (all environments) ─────────────────────
selenium.wait.timeout=30000
commands.execution.interval=0
selenium.success.screenshots=0
selenium.failure.screenshots=1
selenium.screenshots.dir=test-results/screenshots
report.log.level=Info
report.log.skip.success=false
test.results.dir=test-results
tc.identifier.key=TestID
retry.count=0
global.datadriven.parallel=false
element.default.listener=true
bean.populate.random=false
step.provider.pkg=com.matrix.<projectname>.steps
scenario.file.loc=scenarios
```

#### `src/test/java/com/matrix/<projectname>/listeners/MatrixListener.java`

```java
package com.matrix.<projectname>.listeners;

import com.qmetry.qaf.automation.step.client.StepExecutionTracker;
import com.qmetry.qaf.automation.testng.listener.QAFListenerAdapter;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.ui.webdriver.commandtracker.CommandTracker;

public class MatrixListener extends QAFListenerAdapter {

    @Override
    public void onFailure(StepExecutionTracker tracker) {
        // screenshot / logging on step failure
    }

    @Override
    public void afterCommand(QAFExtendedWebElement element, CommandTracker commandTracker) {
        // post-command hook
    }
}
```

#### `src/test/java/com/matrix/<projectname>/pages/BasePage.java`

```java
package com.matrix.<projectname>.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestPage;

public abstract class BasePage<P extends WebDriverTestPage> extends WebDriverBaseTestPage<P> {
    // shared page utilities go here
}
```

#### `scenarios/.gitkeep`

Empty placeholder so the `scenarios/` folder is tracked by git.

#### `resources/data/.gitkeep`

Empty placeholder.

---

### STEP 4 — Dynamic Files (generated from user answers)

#### `resources/application.properties`

> **Derived from** `.claude/resources/qaf-blank-project-maven-master/resources/application.properties`.
> The sample uses `env.resources=resources` + `resources.load.subdirs=1` so QAF auto-discovers all `.properties` files in the `resources/` tree, including subdirectory overrides. This is simpler than the semicolon-list pattern and is the correct approach for new projects.

```properties
# ── Base URL ──────────────────────────────────────────────
env.baseurl=<base_url_from_user>

# ── Resource loading ──────────────────────────────────────
# QAF walks all subdirs under resources/ automatically
env.resources=resources
resources.load.subdirs=1

# ── Step provider ─────────────────────────────────────────
step.provider.pkg=com.matrix.<projectname>

# ── Driver ────────────────────────────────────────────────
remote.server=localhost
remote.port=4444
driver.name=chromeDriver

# ── Waits & screenshots ───────────────────────────────────
selenium.wait.timeout=30000
selenium.success.screenshots=1

# ── Listeners ─────────────────────────────────────────────
qaf.listeners=com.matrix.<projectname>.listeners.MatrixListener

# ── Retry / reporting ─────────────────────────────────────
retry.count=0
report.log.skip.success=0

# ── Locale (uncomment if i18n is needed) ─────────────────
# env.load.locales=<locales_semicolon_separated>
# env.default.locale=<default_locale>

# ── Filters (uncomment to activate) ──────────────────────
# include={'groups':['smoke']}
# exclude={'enabled':['false']}
```

#### `resources/<envname>/env.properties` — one per environment

```properties
# ── <EnvName> environment ────────────────────────────────
env.baseurl=<url_from_user>
admin.user.name=<username_from_user>
encrypted.admin.user.pwd=<base64_of_password>
```

> Encode passwords with Base64. Show the user the encoded value and remind them it uses QAF's built-in `encrypted.` prefix decryption.

#### Locale files — only if user said YES to localisation

For each locale provided, create `resources/common/Translation.<locale>`:

```properties
# Translation file for <locale>
# Add your translation keys below
app.title=<translated value>
welcome.message=<translated value>
```

Create the same file under each env folder if env-specific translations are needed.

#### `resources/locators/home.properties` — starter locator file

```properties
# Home page locators
home.pageHeader={'locator':'css=h1','desc':'Home page header'}
```

#### `pom.xml` — Maven project file

> **Derived from** `.claude/resources/qaf-blank-project-maven-master/pom.xml` — the authoritative template.
> Key points: Java 1.8, `LATEST` for QAF/Selenium/WebDriverManager, AspectJ plugin for QAF instrumentation, exec-maven-plugin for RepoEditor, surefire with timestamped output directory.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.matrix</groupId>
  <artifactId><projectname></artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>jar</packaging>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <testSuiteFile>config/testng.xml</testSuiteFile>
    <sourceVersion>1.8</sourceVersion>
    <targetVersion>1.8</targetVersion>
    <qaf.version>LATEST</qaf.version>
    <selenium.version>LATEST</selenium.version>
    <webdrivermanager.version>LATEST</webdrivermanager.version>
    <test.results.dir>test-results</test.results.dir>
    <run.time>${maven.build.timestamp}</run.time>
    <lib.dir>${project.basedir}/lib</lib.dir>
    <resource.dir>${project.basedir}/resources</resource.dir>
    <output.dir>${test.results.dir}/${run.time}</output.dir>
    <maven.build.timestamp.format>dd_MMM_yyyy_hh_mm_aa</maven.build.timestamp.format>
  </properties>

  <repositories>
    <repository>
      <id>jai</id>
      <url>https://repository.jboss.org/nexus/content/repositories/thirdparty-releases</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>${selenium.version}</version>
    </dependency>
    <dependency>
      <groupId>com.qmetry</groupId>
      <artifactId>qaf</artifactId>
      <version>${qaf.version}</version>
    </dependency>
    <dependency>
      <groupId>io.github.bonigarcia</groupId>
      <artifactId>webdrivermanager</artifactId>
      <version>${webdrivermanager.version}</version>
    </dependency>
    <dependency>
      <groupId>com.qmetry</groupId>
      <artifactId>qaf-support</artifactId>
      <version>${qaf.version}</version>
      <exclusions>
        <exclusion>
          <groupId>com.qmetry</groupId>
          <artifactId>qaf</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!-- QAF RepoEditor — locator repository management -->
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.2.1</version>
        <executions>
          <execution>
            <id>repo-editor</id>
            <goals><goal>java</goal></goals>
          </execution>
        </executions>
        <configuration>
          <mainClass>com.qmetry.qaf.automation.tools.RepoEditor</mainClass>
          <classpathScope>test</classpathScope>
        </configuration>
      </plugin>

      <!-- Compiler — Java 1.8 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <compilerVersion>${sourceVersion}</compilerVersion>
          <source>${sourceVersion}</source>
          <target>${targetVersion}</target>
        </configuration>
      </plugin>

      <!-- AspectJ — required for QAF step instrumentation -->
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <version>1.14.0</version>
        <executions>
          <execution>
            <id>test-compile</id>
            <goals><goal>test-compile</goal></goals>
          </execution>
        </executions>
        <configuration>
          <source>${sourceVersion}</source>
          <target>${targetVersion}</target>
          <showWeaveInfo>true</showWeaveInfo>
          <complianceLevel>${sourceVersion}</complianceLevel>
          <aspectLibraries>
            <aspectLibrary>
              <groupId>com.qmetry</groupId>
              <artifactId>qaf</artifactId>
            </aspectLibrary>
          </aspectLibraries>
        </configuration>
      </plugin>

      <!-- Surefire — timestamped output directories -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.9</version>
        <configuration>
          <suiteXmlFiles>
            <suiteXmlFile>${testSuiteFile}</suiteXmlFile>
          </suiteXmlFiles>
          <reportsDirectory>${test.results.dir}/${run.time}</reportsDirectory>
          <systemPropertyVariables>
            <log4j.configuration>file:///${resource.dir}/log4j.properties</log4j.configuration>
            <outputDir>${output.dir}</outputDir>
            <test.results.dir>${output.dir}/html</test.results.dir>
            <json.report.root.dir>${test.results.dir}</json.report.root.dir>
            <json.report.dir>${output.dir}/json</json.report.dir>
            <selenium.screenshots.dir>${output.dir}/img</selenium.screenshots.dir>
            <selenium.screenshots.relative.path>../img</selenium.screenshots.relative.path>
          </systemPropertyVariables>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

#### `.gitignore`

```
bin/
test-results/
*.class
.DS_Store
target/
*.log
resources/drivers/
```

---

### STEP 5 — Starter Scenario File

#### `scenarios/home.feature`

```gherkin
@channel:['web']
@module:home
Feature: Home Page

  @description:Verify home page loads successfully
  @groups:['smoke']
  @priority:1
  @author:Matrix
  @TestID:TC-HOME-001
  Scenario: Home page should load
    Given user navigates to home page
    Then home page header should be present
```

---

### STEP 6 — Starter Step and Page Classes

#### `src/test/java/com/matrix/<projectname>/pages/HomePage.java`

```java
package com.matrix.<projectname>.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.annotations.PageIdentifier;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;
import com.qmetry.qaf.automation.ui.page.PageLocator;

public class HomePage extends WebDriverBaseTestPage<WebDriverTestPage> {

    @PageIdentifier
    @FindBy(locator = "home.pageHeader")
    private QAFWebElement pageHeader;

    @Override
    protected void openPage(PageLocator loc, Object... args) {
        driver.get(props.getString("env.baseurl"));
    }

    @Override
    public boolean isPageActive(PageLocator loc, Object... args) {
        return pageHeader.isPresent();
    }

    public void verifyPageLoaded() {
        pageHeader.verifyPresent();
    }
}
```

#### `src/test/java/com/matrix/<projectname>/steps/HomeSteps.java`

> **Pattern from** `.claude/resources/qaf-blank-project-maven-master/src/.../steps/StepsLibrary.java`.
> Use `static` methods and `import static CommonStep.*` to access built-in QAF step helpers. For page-backed steps, delegate to the page class.

```java
package com.matrix.<projectname>.steps;

import static com.qmetry.qaf.automation.step.CommonStep.*;

import com.matrix.<projectname>.pages.HomePage;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class HomeSteps {

    @QAFTestStep(description = "user navigates to home page")
    public static void userNavigatesToHomePage() {
        new HomePage().launchPage(null);
    }

    @QAFTestStep(description = "home page header should be present")
    public static void homePageHeaderShouldBePresent() {
        new HomePage().verifyPageLoaded();
    }

    @QAFTestStep(description = "user is on {0}")
    public static void userIsOnPage(String url) {
        // CommonStep.get() navigates relative to env.baseurl
        get(url);
    }
}
```

#### `src/test/java/com/matrix/<projectname>/tests/SampleTest.java`

> **Pattern from** `.claude/resources/qaf-blank-project-maven-master/src/.../test/SampleTest.java`.
> Java tests extend `WebDriverTestCase` and call step methods directly (static import style).

```java
package com.matrix.<projectname>.tests;

import static com.qmetry.qaf.automation.step.CommonStep.*;
import static com.matrix.<projectname>.steps.HomeSteps.*;
import org.testng.annotations.Test;
import com.qmetry.qaf.automation.ui.WebDriverTestCase;

public class SampleTest extends WebDriverTestCase {

    @Test
    public void testHomePage() {
        userNavigatesToHomePage();
        homePageHeaderShouldBePresent();
    }
}
```

---

### STEP 7 — Final Summary

After all files are created, print:

```
✅ QAF Project scaffolded successfully!

Project structure:
  config/testng.xml                          ← run with: mvn test
  config/testng-parallel.xml                 ← parallel run
  resources/application.properties           ← master config
  resources/common/common.properties         ← shared settings
  resources/<env>/env.properties             ← one per environment
  resources/locators/home.properties         ← starter locators
  scenarios/home.feature                     ← starter scenario
  src/.../pages/HomePage.java                ← starter page
  src/.../steps/HomeSteps.java               ← starter steps
  src/.../listeners/MatrixListener.java      ← combined listener
  pom.xml                                    ← Maven config
  .gitignore

Next steps:
1. Run: mvn test -Denv.name=<first_env>
2. Add pages with:  /qaf-page
3. Add steps with:  /qaf-step
4. Add components:  /qaf-component
5. Tag scenarios:   /qaf-metadata
6. Add data beans:  /qaf-databean
```

---

## Rules Applied from All Skills

This master skill enforces rules from every QAF skill:

- **Locators** — all in `resources/locators/*.properties`, never hardcoded in Java (`/qaf-page`, `/qaf-step`)
- **POM** — every page extends `WebDriverBaseTestPage<Parent>`, has `openPage()` override (`/qaf-page`)
- **Steps** — `@QAFTestStep`, `waitForPresent()` before `sendKeys()`, `verifyPresent()` not `Assert.assertTrue` (`/qaf-step`)
- **Click errors** — on `ElementClickInterceptedException`, add `waitForPresent()` + `scrollIntoView(true)` via `JavascriptExecutor` before `.click()` (`/qaf-page` Rule 6)
- **Listeners** — `QAFListenerAdapter` registered via `qaf.listeners` (`/qaf-listeners`)
- **Resources** — `env.resources=resources/common;resources/locators;resources/${env.name}` pattern (`/qaf-resource`)
- **Metadata** — BDD2 `@key:value` format, Feature-level + Scenario-level (`/qaf-metadata`)
- **Properties** — precedence: system > TestNG XML > file; `encrypted.` prefix for passwords (`/qaf-property`)
- **Components** — extend `QAFWebComponent`, `component-class` in locator property (`/qaf-component`)
- **DataBeans** — extend `BaseDataBean`, `@Randomizer` for test data generation (`/qaf-databean`)
- **QMetry** — `integration.tool.qmetry=true` + mandatory properties when integration is needed (`/qaf-qmetry`)
