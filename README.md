# Matrix Automation — QAF Test Suite

Automated test suite for the [Matrix Technolabs](https://matrixtechnolabs.in/) website, built with **QAF (QMetry Automation Framework)**, **Selenium WebDriver**, **Cucumber/Gherkin BDD**, and **TestNG**.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Test Suites](#test-suites)
- [Reports & Dashboard](#reports--dashboard)
- [CI/CD](#cicd)
- [Contributing](#contributing)

---

## Tech Stack

| Tool | Purpose |
|---|---|
| Java 8 | Programming language |
| Maven | Build & dependency management |
| QAF (QMetry Automation Framework) | Core test framework |
| Selenium WebDriver | Browser automation |
| TestNG | Test runner |
| Cucumber / Gherkin | BDD feature files |
| AspectJ | AOP instrumentation for QAF |
| WebDriverManager | Automatic browser driver management |
| Log4j | Logging |
| GitHub Actions | CI/CD pipeline |

---

## Project Structure

```
MatrixAutomation/
├── config/                         # TestNG XML suite configurations
│   ├── testrun_config.xml          # Master test suite (runs all tests)
│   ├── homepage.xml
│   ├── navigation.xml
│   ├── contact.xml
│   └── links.xml
│
├── scenarios/                      # Gherkin BDD feature files
│   ├── homepage.feature
│   ├── navigation.feature
│   ├── contact.feature
│   └── links.feature
│
├── src/test/java/com/matrix/
│   ├── pages/                      # Page Object Model classes
│   │   ├── HomePage.java
│   │   ├── ContactPage.java
│   │   └── NavigationPage.java
│   └── steps/                      # Step definition classes
│       ├── CommonSteps.java
│       ├── HomePageSteps.java
│       ├── ContactSteps.java
│       └── NavigationSteps.java
│
├── resources/
│   ├── application.properties      # Main framework configuration
│   ├── log4j.properties            # Logging configuration
│   ├── locators/                   # Element locator files
│   │   ├── home.properties
│   │   ├── contact.properties
│   │   └── navigation.properties
│   ├── env1/env.properties         # Environment 1 overrides
│   └── env2/env.properties         # Environment 2 overrides
│
├── test-results/                   # Auto-generated timestamped reports
├── dashboard/                      # Dashboard UI assets (jQuery, CSS)
├── dashboard.htm                   # Test results dashboard
├── pom.xml                         # Maven build file
└── .github/workflows/
    └── maven-test.yml              # GitHub Actions CI workflow
```

---

## Prerequisites

Make sure the following are installed on your machine:

- **Java JDK 8+** — [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** — [Download](https://maven.apache.org/download.cgi)
- **Google Chrome** (latest stable) — [Download](https://www.google.com/chrome/)
- **Git** — [Download](https://git-scm.com/)

Verify your setup:

```bash
java -version
mvn -version
```

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/<your-org>/MatrixAutomation.git
cd MatrixAutomation
```

### 2. Install dependencies

Maven will automatically download all dependencies on first run. To install them explicitly:

```bash
mvn dependency:resolve
```

> **Note:** No manual ChromeDriver installation is needed — `WebDriverManager` downloads the correct driver automatically at runtime.

### 3. Verify configuration

Open [resources/application.properties](resources/application.properties) and confirm the base URL and browser settings match your environment:

```properties
env.baseurl=https://matrixtechnolabs.in/
driver.name=chromeDriver
selenium.wait.timeout=30
step.provider.pkg=com.matrix.steps
```

---

## Configuration

### application.properties

| Property | Default | Description |
|---|---|---|
| `env.baseurl` | `https://matrixtechnolabs.in/` | Target website URL |
| `driver.name` | `chromeDriver` | WebDriver to use (`chromeDriver`, `firefoxDriver`) |
| `selenium.wait.timeout` | `30` | Default element wait timeout (seconds) |
| `step.provider.pkg` | `com.matrix.steps` | Package containing step definitions |
| `selenium.screenshots.dir` | `test-results/{timestamp}/img/` | Screenshot output directory |

### Running Against Different Environments

Use the `env1` or `env2` properties to override settings per environment:

```bash
# Run against env1
mvn test -Denv=env1

# Run against env2
mvn test -Denv=env2
```

---

## Running Tests

### Run the full test suite

```bash
mvn test
```

### Run in headless mode (no browser window)

```bash
mvn test -Dchrome.additional.capabilities="{'goog:chromeOptions':{'args':['headless']}}"
```

### Run a specific suite by XML config

```bash
mvn test -Dsurefire.suiteXmlFiles=config/homepage.xml
mvn test -Dsurefire.suiteXmlFiles=config/navigation.xml
mvn test -Dsurefire.suiteXmlFiles=config/contact.xml
mvn test -Dsurefire.suiteXmlFiles=config/links.xml
```

### Run by tag (Smoke / Regression)

```bash
# Smoke tests only
mvn test -Dgroups=Smoke

# Regression tests only
mvn test -Dgroups=Regression
```

### Run with Firefox

```bash
mvn test -Ddriver.name=firefoxDriver
```

---

## Test Suites

| Suite | Feature File | Scenarios | Tags |
|---|---|---|---|
| Homepage | `scenarios/homepage.feature` | 6 | `@Homepage`, `@Smoke`, `@Regression` |
| Navigation | `scenarios/navigation.feature` | 5 | `@Navigation`, `@Smoke`, `@Regression` |
| Contact Form | `scenarios/contact.feature` | 4 | `@Contact`, `@Regression` |
| Links & Buttons | `scenarios/links.feature` | 4 | `@Links`, `@Regression` |

### What is tested

- **Homepage** — Logo visibility, hero heading, CTA buttons, Services section, About section, footer
- **Navigation** — Menu items presence, active state, section navigation via menu links
- **Contact Form** — Form display, required field validation, error messages, successful submission
- **Links & Buttons** — CTA button clicks, footer links, copyright text, page load verification

---

## Reports & Dashboard

After each test run, results are saved to a timestamped directory:

```
test-results/
└── 01_Jun_2026_10_30_AM/
    ├── html/        # HTML reports per scenario
    ├── json/        # Machine-readable JSON results
    ├── img/         # Screenshots (captured on pass and fail)
    └── scenario.log # Detailed execution log
```

### Viewing the Dashboard

Open [dashboard.htm](dashboard.htm) in a browser after the test run to see a visual summary with charts and scenario results.

---

## CI/CD

The project uses **GitHub Actions** for continuous integration.

**Workflow file:** [.github/workflows/maven-test.yml](.github/workflows/maven-test.yml)

**Triggers:**
- Push to `master` branch
- Pull requests targeting `master`

**What the pipeline does:**
1. Spins up an Ubuntu runner
2. Installs JDK 11
3. Installs Chrome and Firefox
4. Runs the full test suite in headless Chrome mode

```yaml
mvn -B test --file pom.xml \
  -Dchrome.additional.capabilities="{'goog:chromeOptions':{'args':['headless']}}"
```

---

## Contributing

1. Fork the repository and create a feature branch (`git checkout -b feature/my-test`)
2. Add feature files in `scenarios/` following existing naming conventions
3. Add page objects in `src/test/java/com/matrix/pages/`
4. Add step definitions in `src/test/java/com/matrix/steps/`
5. Add locators in `resources/locators/`
6. Run the full suite locally before opening a PR: `mvn test`
7. Open a pull request against `master`

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
