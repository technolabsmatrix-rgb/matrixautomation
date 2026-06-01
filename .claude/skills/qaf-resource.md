---
name: qaf-resource
description: Guide and scaffold QAF resource management — application.properties configuration, environment-specific resources, locale/translation files, property interpolation, and driver-specific resources.
---

You are managing QAF resources for the Matrix automation project.

## What is QAF Resource Management?

QAF uses a central `ConfigurationManager` that loads properties from files at startup. Resources include:
- `application.properties` — master config entry point
- Environment-specific property folders (`env1/`, `env2/`, `staging/`, etc.)
- Locale/translation files (`Translation.en`, `Translation.hi`, etc.)
- Locator properties files (`resources/locators/*.properties`)
- Driver-specific resource folders

---

## Project Conventions

- Master config: `resources/application.properties`
- Common resources: `resources/common/`
- Environment resources: `resources/<envname>/`
- Locator files: `resources/locators/`
- Translation files: `resources/common/Translation.<locale>` and `resources/<env>/Translation.<locale>`

---

## Rules

### Rule 1 — Master Config: `application.properties`

The default config file loaded by QAF at startup. Lives at `resources/application.properties`.
An alternate path can be set via the JVM system property:
```
-Dapplication.properties.file=resources/myconfig.properties
```

Minimal structure:
```properties
# Entry point — load additional resource dirs
env.resources=resources/common;resources/env1

# Browser / driver
driver.name=chromeDriver

# App URL
env.baseurl=https://www.example.com

# Listeners
qaf.listeners=com.matrix.listeners.MatrixListener

# Locale
env.load.locales=en
env.default.locale=en
```

---

### Rule 2 — Loading Additional Resource Directories

Use `env.resources` to pull in one or more resource folders. All `.properties` and `.xml` files inside those directories are loaded:

```properties
env.resources=resources/common;resources/env1
```

Multiple dirs — semicolon-separated:
```properties
env.resources=resources/common;resources/web;resources/staging
```

Use `${token}` interpolation to select the env at runtime:
```properties
env.name=staging
env.resources=resources/common;resources/${env.name}
```

Pass `env.name` as a system property at run time:
```
mvn test -Denv.name=staging
```

---

### Rule 3 — Environment-Specific Directory Structure

Organise resources by environment so common data lives once and overrides are per-env:

```
resources/
├── application.properties        ← master entry point
├── common/
│   ├── common.properties         ← shared config (timeouts, flags)
│   ├── locators.properties       ← shared locators
│   ├── Translation.en
│   └── Translation.hi
├── dev/
│   ├── env.properties            ← dev-specific URLs, credentials
│   ├── locators.properties       ← dev-specific locator overrides
│   ├── Translation.en
│   └── Translation.hi
├── staging/
│   ├── env.properties
│   ├── locators.properties
│   ├── Translation.en
│   └── Translation.hi
└── prod/
    ├── env.properties
    └── locators.properties
```

**Rule:** Put shared/default values in `common/`. Override only what differs in `dev/`, `staging/`, `prod/`.
Later-loaded directories override earlier ones — so list `common` first, env-specific second.

```properties
env.resources=resources/common;resources/${env.name}
```

---

### Rule 4 — Locator Properties Files

Locators live in `resources/locators/` (one file per page) and are loaded via `env.resources` or directly:

```properties
# Inside common.properties or application.properties
env.resources=resources/common;resources/locators;resources/${env.name}
```

Locator file format — see Locator Strategy rules. Example `resources/locators/login.properties`:
```properties
login.usernameField={'locator':'id=username','desc':'Username input'}
login.passwordField={'locator':'id=password','desc':'Password input'}
login.submitBtn={'locator':'css=button[type=submit]','desc':'Login button'}
```

---

### Rule 5 — Property Interpolation (`${token}`)

QAF resolves `${token}` placeholders within property values automatically.

**Basic cross-reference:**
```properties
env.name=staging
env.resources=resources/common;resources/${env.name}
app.url=https://${env.name}.example.com
```

**Built-in prefix types:**

| Prefix | Purpose | Example |
|---|---|---|
| *(none)* | Reference another property | `${env.name}` |
| `rnd:` | Random data — `a`=letter, `9`=digit | `${rnd:aaa-999}` → `xyz-042` |
| `expr:` | Evaluate a Java expression | `${expr:java.util.UUID.randomUUID()}` |

**`expr:` examples:**
```properties
current.timestamp=${expr:java.lang.System.currentTimeMillis()}
today.date=${expr:com.qmetry.qaf.automation.util.DateUtil.getDate(0, 'MM/dd/yyyy')}
future.date=${expr:com.qmetry.qaf.automation.util.DateUtil.getDate(1, 'MM/dd/yyyy')}
random.uuid=${expr:java.util.UUID.randomUUID()}
random.instant=${expr:java.time.Instant.now()}
random.5digit=${rnd:99999}
random.ref=${rnd:aaa-aaa-aaa}
```

**Nested parameter resolution (`<% %>`):**
Use `<% %>` when a token value itself must be resolved first:
```properties
env=staging
staging.url=https://staging.example.com
prod.url=https://www.example.com
active.url=${<%env%>.url}
# resolves: env → "staging" → ${staging.url} → "https://staging.example.com"
```

**Unresolved tokens:** If a property or prefix is unknown, the `${token}` is returned as-is (no crash).

---

### Rule 6 — System Properties

Properties prefixed with `system.` are promoted to JVM system properties at load time:

```properties
system.webdriver.gecko.driver=/drivers/geckodriver
system.webdriver.chrome.driver=/drivers/chromedriver
```

Equivalent to: `-Dwebdriver.gecko.driver=/drivers/geckodriver` on the command line.

---

### Rule 7 — Accessing Properties in Java

```java
import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

// Read string
String url = getBundle().getString("env.baseurl");

// Read int
int timeout = getBundle().getInt("element.wait.time");

// Set a property at runtime
getBundle().setProperty("my.flag", "true");

// Read a property with a default fallback
String env = getBundle().getString("env.name", "dev");
```

**In BDD step descriptions** — use `${token}` directly in the feature file:
```gherkin
Given user logs in with '${admin.user.name}' and '${admin.user.pwd}'
```

---

### Rule 8 — Locale / Translation Files

#### File naming

Translation files use the pattern `Translation.<locale>` (no `.properties` extension):
```
resources/common/Translation.en
resources/common/Translation.hi
resources/common/Translation.fr
```

#### File content (key=value, same key across all locales)

`Translation.en`:
```properties
morning.greeting.text=Good Morning
welcome.message=Welcome to the application
```

`Translation.hi`:
```properties
morning.greeting.text=शुभ प्रभात
welcome.message=एप्लिकेशन में आपका स्वागत है
```

`Translation.fr`:
```properties
morning.greeting.text=Bonjour
welcome.message=Bienvenue dans l'application
```

#### Configuration in `application.properties`

```properties
# Semicolon-separated list of locales to load simultaneously
env.load.locales=en;hi;fr

# Default locale — used by getBundle().getString("key")
env.default.locale=en
```

#### Accessing translations in Java

```java
import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

// Default locale
String greeting = getBundle().getString("morning.greeting.text");

// Non-default loaded locale
String frGreeting = getBundle().subset("fr").getString("morning.greeting.text");
String hiGreeting = getBundle().subset("hi").getString("morning.greeting.text");
```

#### In BDD steps
```gherkin
Then user should see greeting '${morning.greeting.text}'
```

#### Rules
- Translation keys must be identical across all locale files
- Locale names in config must exactly match the file extensions (`en` → `Translation.en`)
- Load all locales you will need simultaneously via `env.load.locales`
- Use `getBundle().subset("<locale>")` to access a non-default locale

---

### Rule 9 — Standard Project Directory Structure

```
project-root/
├── config/                     ← TestNG XML suite files
│   └── testng.xml
├── lib/                        ← external JAR dependencies
├── resources/                  ← all properties, locators, data, translations
│   ├── application.properties  ← master config entry point
│   ├── common/                 ← shared across all environments
│   │   ├── common.properties
│   │   ├── locators.properties
│   │   ├── Translation.en
│   │   └── Translation.hi
│   ├── locators/               ← one .properties file per page
│   │   ├── login.properties
│   │   └── home.properties
│   ├── data/                   ← test data files (CSV, JSON, XML, Excel)
│   ├── dev/
│   │   └── env.properties
│   ├── staging/
│   │   └── env.properties
│   └── prod/
│       └── env.properties
├── scenarios/                  ← default BDD/KWD feature files
│   └── login.feature
├── scripts/                    ← Ant build scripts, .bat runner files
├── src/
│   └── test/java/com/matrix/
│       ├── steps/              ← @QAFTestStep classes
│       ├── pages/              ← WebDriverBaseTestPage subclasses
│       ├── components/         ← QAFWebComponent subclasses
│       ├── listeners/          ← QAF/TestNG listener classes
│       ├── beans/              ← BaseDataBean subclasses
│       └── tests/              ← WebDriverTestCase/TestNGTestCase subclasses
├── bin/                        ← compiled classes (deleted on clean)
└── test-results/               ← JSON/HTML execution reports
```

### Rule 10 — Driver-Specific Resources (since QAF 2.1.12)

Load additional resource folders only when a specific driver is active:

```properties
# Loaded when driver.name=androidDriver or androidRemoteDriver
android.resources=resources/android

# Loaded when driver.name=iosDriver
ios.resources=resources/ios

# Loaded when driver.name=chromeDriver
chrome.resources=resources/chrome
```

Pattern: `<drivername>.resources=<path>`

Useful when switching drivers within a test run — each driver picks up its own locators and config without extra logic.

---

## Standard `application.properties` Template

```properties
# ── Environment ──────────────────────────────────────────
env.name=dev
env.resources=resources/common;resources/locators;resources/${env.name}
env.baseurl=https://dev.example.com

# ── Driver ───────────────────────────────────────────────
driver.name=chromeDriver
system.webdriver.chrome.driver=resources/drivers/chromedriver.exe

# ── Waits ────────────────────────────────────────────────
selenium.wait.time=5
element.wait.time=10

# ── Locale ───────────────────────────────────────────────
env.load.locales=en
env.default.locale=en

# ── Listeners ────────────────────────────────────────────
qaf.listeners=com.matrix.listeners.MatrixListener

# ── Test data / credentials ──────────────────────────────
admin.user.name=admin@example.com
admin.user.pwd=Secret123

# ── Driver-specific resources ────────────────────────────
android.resources=resources/android
ios.resources=resources/ios
```

---

## Output Format

When the user asks to:

**Set up a new environment:** Generate the env folder structure and `env.properties` file with environment-specific overrides.

**Add a new locale:** Generate the `Translation.<locale>` file for each existing env folder, add the locale to `env.load.locales`, and show access examples.

**Add a new property:** Add to the correct file (`application.properties` for global, `env.properties` for env-specific, `common.properties` for shared), show Java and BDD access patterns.

**Configure driver resources:** Add the `<drivername>.resources` entry and create the resource folder with a stub properties file.

Always show:
1. The file(s) to create or edit with full content
2. The registration/reference line in `application.properties`
3. Java and/or BDD access example where relevant
