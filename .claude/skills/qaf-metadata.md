---
name: qaf-metadata
description: Guide QAF scenario metadata — tagging scenarios with groups/priority/custom keys, and writing include/exclude filters in application.properties to select which tests run.
---

You are managing QAF scenario metadata and test filters for the Matrix automation project.

## What is QAF Metadata?

Metadata is structured key-value information attached to a Feature or Scenario that lets QAF:
- **Filter** which scenarios to run or skip (`include` / `exclude` properties)
- **Group** tests (smoke, regression, P1, etc.)
- **Order** tests by priority
- **Document** tests (description, author, TestID, storyKey)
- **Configure** data-driven sources (dataFile, dataProvider)

---

## Project Conventions

- Feature files: `scenarios/**/*.feature`
- Filter config: `resources/application.properties` (or env-specific `env.properties`)
- Metadata scope: Feature-level (inherited by all scenarios) or Scenario-level

---

## Rules

### Rule 1 — Predefined Metadata Keys

| Key | Type | Purpose |
|---|---|---|
| `description` | String | Detailed scenario description |
| `enabled` | Boolean | `false` disables the scenario without deleting it |
| `groups` | Array of String | Tags: `['smoke','regression','P1']` |
| `priority` | Number | Execution order — lower runs first |
| `dependsOnGroups` | Array of String | Groups that must complete before this scenario |
| `dependsOnMethods` | Array of String | Specific scenarios that must run before this one |
| `dataProvider` | String | Custom data provider name |
| `dataProviderClass` | Fully qualified class | Class that implements the custom data provider |

Custom keys (e.g. `channel`, `module`, `author`, `TestID`, `storyKey`) are also valid — any key can be used for filtering.

---

### Rule 2 — Metadata Syntax by Format

#### BDD2 format (preferred — `@key:value` prefix)

```gherkin
@enabled:true
@channel:['web','mobile']
Feature: Login Feature

@description:Verify login with valid credentials
@groups:['smoke','regression']
@author:Avnish
@TestID:TC-001
@priority:1
Scenario: valid login
  Given user navigates to login page
  When user enters valid credentials
  Then user should be on dashboard
```

Values without a key are treated as groups:
```gherkin
@smoke @regression
Scenario: another scenario
```

#### BDD (classic) format — `META-DATA:` block

```gherkin
Feature: Login Feature
META-DATA: {'enabled':true, 'channel':['web','mobile']}

SCENARIO: valid login
META-DATA: {'TestID':'TC-001', 'description':'Verify valid login', 'groups':['smoke','regression'], 'author':'Avnish', 'priority':1, 'channel':'Web', 'module':'login'}
END
```

#### Gherkin / Cucumber format — `@tags` only

```gherkin
@smoke @regression
Feature: Login Feature

@smoke @P1
Scenario: valid login
```

#### Java TestNG format

```java
@Test(description = "Verify valid login", groups = {"smoke", "regression"})
@MetaData("{'TestID':'TC-001', 'storyKey':'PROJ-123', 'channel':['web','mobile'], 'author':'Avnish'}")
public void validLogin() { }
```

---

### Rule 3 — Feature-Level vs Scenario-Level Metadata

Metadata on a Feature is **inherited** by all its scenarios. Scenario-level metadata **overrides or extends** feature metadata.

```gherkin
@channel:['web']
@module:login
Feature: Login Feature

  @groups:['smoke']
  @priority:1
  Scenario: valid login
    # inherits channel=web, module=login; adds groups=smoke, priority=1

  @groups:['regression']
  @enabled:false
  Scenario: locked account login
    # inherits channel=web, module=login; disabled, tagged regression
```

---

### Rule 4 — `include` Filter (AND between keys, OR within values)

Set in `application.properties` or passed as a system property.

```properties
include={'channel':['Mobile'],'module':['checkout']}
```

**Logic:**
- Multiple values for the **same key** → **OR** (`channel=Mobile OR Web`)
- Different keys → **AND** (`channel match AND module match`)

Examples:
```properties
# Run only smoke tests
include={'groups':['smoke']}

# Run Mobile OR Web channel, for checkout OR search module
include={'channel':['Mobile','Web'],'module':['checkout','search']}

# Run a specific test by ID
include={'TestID':['TC-001','TC-002']}

# Run all P1 priority tests
include={'priority':['1']}
```

---

### Rule 5 — `exclude` Filter (OR across all keys and values)

Exclude uses **OR logic across everything** — a scenario is excluded if it matches **any** listed value under **any** listed key.

```properties
# Exclude any scenario tagged with MobileWeb channel OR checkout/PDP module
exclude={'channel':['MobileWeb'],'module':['checkout','PDP']}
```

---

### Rule 6 — Combined `include` + `exclude`

`include` is applied first, then `exclude` removes from that set.

```properties
# Include: (channel=Mobile OR MobileWeb) AND (module=PDP OR search)
# Then exclude: channel=web OR module=cart OR module=profile
include={'channel':['Mobile','MobileWeb'],'module':['PDP','search']}
exclude={'channel':['web'],'module':['cart','profile']}
```

---

### Rule 7 — Passing Filters at Runtime (Maven / CLI)

Filters can be set in properties files or passed on the command line:

```bash
# Run only smoke tests
mvn test -Dinclude={'groups':['smoke']}

# Run staging env, only regression, exclude checkout
mvn test -Denv.name=staging -Dinclude={'groups':['regression']} -Dexclude={'module':['checkout']}
```

---

### Rule 8 — Disabling a Scenario Without Deleting It

```gherkin
@enabled:false
Scenario: work in progress scenario
  Given something not ready
```

Or in BDD format:
```gherkin
SCENARIO: work in progress
META-DATA: {'enabled':false}
END
```

---

### Rule 9 — Data-Driven Metadata Keys

Configure a scenario as data-driven directly via metadata — no Java annotation needed:

| Key | Type | Description |
|---|---|---|
| `dataFile` | Path string | CSV, XML, JSON, XLS, or DB query file |
| `sheetName` | String | Excel sheet name (XLS only) |
| `key` | String | XML node name or Excel table key |
| `dataProvider` | String | Custom `@DataProvider` name |
| `dataProviderClass` | Fully-qualified class | Class containing the custom data provider |
| `filter` | Expression | Filter expression applied to the data set |
| `indices` | Array | Zero-based row indices to use e.g. `[0,2,4]` |
| `from` | Number | Start row (1-based) |
| `to` | Number | End row (1-based) |

**BDD2 examples:**
```gherkin
@dataFile:resources/data/logindata.csv
Scenario: CSV data-driven login
  When user submits login form with '${username}' and '${password}'

@dataFile:resources/data/testdata.xls
@sheetName:LoginSheet
Scenario: Excel data-driven login
  When user submits login form with '${username}' and '${password}'

@dataProvider:my-custom-dp
@dataProviderClass:com.matrix.data.CustomDataProvider
Scenario: Custom provider login
  When user submits login form with '${username}' and '${password}'
```

**BDD classic examples:**
```gherkin
SCENARIO: Data-driven login
META-DATA: {"dataFile":"resources/data/logindata.csv","description":"Login with multiple users"}
  When user submits login form with '${username}' and '${password}'
END

SCENARIO: Excel data-driven
META-DATA: {"dataFile":"resources/data/testdata.xls","sheetName":"Login","from":1,"to":5}
  When user submits login form with '${username}' and '${password}'
END
```

**Global data-driven config** (applies to all tests matching the pattern):
```properties
# All tests use same-named CSV by default
global.testdata.dataFile=resources/data/${class}/${method}.csv

# Or override per test case
login.testdata={'dataFile':'resources/data/testdata.xls','sheetName':'login'}
```

**Available interpolation tokens in data config values:**
- `${class}` — Java class name
- `${method}` — Java method name
- `${meta-key}` — any existing test case metadata value

**Record identification** — add one of these columns to your data file to append row info to test result filenames: `recid`, `summary`, `tcid`, `testcaseid`

---

### Rule 10 — Metadata Formatter for External Links (e.g. Jira)

Renders metadata values as clickable links in QAF reports:

```properties
# In application.properties
jira.url=https://yourcompany.atlassian.net/browse
metadata.formatter.storyKey=<a href="${jira.url}/{0}">{0}</a>
```

Any scenario tagged `@storyKey:PROJ-123` will render as a Jira link in the report.

---

## Standard Metadata Template

### BDD2 Feature file
```gherkin
@channel:['web']
@module:login
Feature: <FeatureName>

  @description:<what this scenario verifies>
  @groups:['smoke','regression']
  @priority:1
  @author:Avnish
  @TestID:TC-XXX
  Scenario: <scenario name>
    Given ...
    When ...
    Then ...
```

### application.properties filter block
```properties
# ── Test filters ─────────────────────────────────────────
# Uncomment and set as needed before running
# include={'groups':['smoke']}
# include={'channel':['web'],'module':['login','home']}
# exclude={'enabled':['false']}
```

---

## Output Format

When the user asks to:

**Tag a scenario:** Add the correct metadata in BDD2/BDD/Gherkin format with appropriate predefined and custom keys.

**Write a filter:** Generate the `include`/`exclude` lines for `application.properties` with the correct AND/OR logic, plus the Maven CLI equivalent.

**Disable a test:** Add `@enabled:false` (BDD2) or `'enabled':false` (BDD) to the scenario.

**Link to Jira:** Add `@storyKey:PROJ-XXX` to the scenario and `metadata.formatter.storyKey` to `application.properties`.

Always show:
1. The feature file metadata change
2. The `application.properties` filter entry (if a filter is involved)
3. The Maven CLI command equivalent (if a filter is involved)
