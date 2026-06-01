---
name: qaf-listeners
description: Scaffold a QAF listener class (TestStep, WebDriver, WebElement, or Configuration). Pass the listener type and the behaviour you want to implement.
---

You are scaffolding a QAF Listener for the Matrix automation project.

## What is a QAF Listener?

QAF listeners let you hook into the framework's execution pipeline — before/after commands on the driver or elements, before/after test step execution, on failures, and when configuration loads. They are the correct QAF way to add cross-cutting behaviour (logging, auto-clear, retry, threshold checks, screenshot capture, etc.) without modifying test or page code.

---

## Project Conventions

- Listener classes: `src/test/java/com/matrix/listeners/`
- Registration: `resources/application.properties` (or the relevant `.properties` config file)

---

## Listener Types — Quick Reference

| Type | Extend | Register with property | When to use |
|---|---|---|---|
| TestStep | `QAFTestStepAdapter` | `teststep.listeners` or `qaf.listeners` | Before/after/on-failure of a `@QAFTestStep` |
| WebDriver Command | `QAFWebDriverCommandAdapter` | `wd.command.listeners` or `qaf.listeners` | Before/after/on-failure of any driver command |
| WebElement Command | `QAFWebElementCommandAdapter` | `we.command.listeners` or `qaf.listeners` | Before/after/on-failure of any element command |
| Configuration | implement `QAFConfigurationListener` | `qaf.listeners` or as Java service | After config loads or changes |
| Combined (all types) | `QAFListenerAdapter` | `qaf.listeners` | Single class covering multiple listener types |

> **Prefer `QAFListenerAdapter`** when a single listener needs to cover more than one type — extend it, override only the methods you need, and register once via `qaf.listeners`.

---

## Rules

### Rule 1 — Always extend the Adapter, never implement the interface directly

Adapters provide empty default implementations so you only override the methods you need:

```java
// CORRECT
public class MyListener extends QAFTestStepAdapter { ... }

// WRONG — forces you to implement all interface methods
public class MyListener implements QAFTestStepListener { ... }
```

### Rule 2 — Registration in properties file

Add the fully-qualified class name to the correct property key in `resources/application.properties`:

```properties
# TestStep listener only
teststep.listeners=com.matrix.listeners.MyStepListener

# WebDriver command listener only
wd.command.listeners=com.matrix.listeners.MyWDListener

# WebElement command listener only
we.command.listeners=com.matrix.listeners.MyWEListener

# Combined / configuration listener — covers all types
qaf.listeners=com.matrix.listeners.MyUnifiedListener
```

Multiple listeners — comma-separated:
```properties
qaf.listeners=com.matrix.listeners.ListenerOne,com.matrix.listeners.ListenerTwo
```

### Rule 3 — CommandTracker: read command name and parameters

Both WebDriver and WebElement listeners receive a `CommandTracker`. Use it to inspect and modify execution:

```java
String command = commandTracker.getCommand();              // e.g. "sendKeysToElement"
Map<String, Object> params = commandTracker.getParameters(); // command parameters

// Skip the original command execution (respond without running it):
commandTracker.setResponce(new Response());

// Check if the command caused an exception:
if (commandTracker.hasException()) { ... }

// Clear a handled exception so QAF does not re-throw it:
commandTracker.setException(null);
```

### Rule 4 — StepExecutionTracker: read step info and inject failures

```java
long duration = (tracker.getEndTime() - tracker.getStartTime()) / 1000;
int threshold = tracker.getStep().getThreshold();

// Inject a soft verification failure into the step result:
tracker.setVerificationError("Threshold exceeded: " + duration + "s");
```

### Rule 5 — Never call `element.clear()` in test/page code when a listener handles it

If a `SendKeysListener` auto-clears before typing, remove manual `element.clear()` calls from page objects — the listener makes them redundant.

---

## Full Examples

### TestStep Listener — Threshold Check

```java
package com.matrix.listeners;

import com.qmetry.qaf.automation.step.client.StepExecutionTracker;
import com.qmetry.qaf.automation.testng.listener.QAFTestStepAdapter;

public class ThresholdListener extends QAFTestStepAdapter {

    @Override
    public void afterExecute(StepExecutionTracker tracker) {
        long duration = (tracker.getEndTime() - tracker.getStartTime()) / 1000;
        if (tracker.getStep().getThreshold() > duration) {
            tracker.setVerificationError("Threshold value exceeded: " + duration + "s");
        }
    }
}
```

Registration:
```properties
teststep.listeners=com.matrix.listeners.ThresholdListener
```

---

### WebDriver Command Listener — Frame / Window Handling

```java
package com.matrix.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import com.qmetry.qaf.automation.ui.webdriver.commandtracker.CommandTracker;
import com.qmetry.qaf.automation.util.StringUtil;
import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.testng.listener.QAFWebDriverCommandAdapter;
import java.util.Map;
import java.util.Set;

public class WDListener extends QAFWebDriverCommandAdapter {

    Log logger = LogFactory.getLog(getClass());

    @Override
    public void beforeCommand(QAFExtendedWebDriver driver, CommandTracker commandTracker) {
        super.beforeCommand(driver, commandTracker);
        String command = commandTracker.getCommand();
        Map<String, Object> params = commandTracker.getParameters();

        // Redirect selectFrame("") to main window
        if (command.equalsIgnoreCase(DriverCommand.SWITCH_TO_FRAME)
                && StringUtil.isBlank((String) params.get("id"))) {
            String mainWindow = driver.getWindowHandle();
            params.put("id", mainWindow);
            driver.switchTo().window(mainWindow);
            commandTracker.setResponce(new Response());
        }

        // Track parent window handle before switching
        if (command.equalsIgnoreCase(DriverCommand.SWITCH_TO_WINDOW)) {
            String target = String.valueOf(params.get("name"));
            String parent = driver.getWindowHandle();
            if (!target.equalsIgnoreCase(parent)) {
                ConfigurationManager.getBundle().setProperty("parentWindowHandel", parent);
            }
        }
    }

    @Override
    public void onFailure(QAFExtendedWebDriver driver, CommandTracker commandTracker) {
        super.onFailure(driver, commandTracker);
        if (commandTracker.getCommand().equalsIgnoreCase(DriverCommand.SWITCH_TO_WINDOW)
                && commandTracker.hasException()) {
            String parentHandle = ConfigurationManager.getBundle().getString("parentWindowHandel");
            for (String window : driver.getWindowHandles()) {
                if (!parentHandle.equalsIgnoreCase(window)) {
                    driver.switchTo().window(window);
                    commandTracker.setException(null); // exception handled
                    break;
                }
            }
        }
    }
}
```

Registration:
```properties
wd.command.listeners=com.matrix.listeners.WDListener
```

---

### WebElement Command Listener — Auto-Clear Before sendKeys

```java
package com.matrix.listeners;

import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.ui.webdriver.commandtracker.CommandTracker;
import com.qmetry.qaf.automation.util.StringUtil;
import com.qmetry.qaf.automation.testng.listener.QAFWebElementCommandAdapter;

public class SendKeysListener extends QAFWebElementCommandAdapter {

    @Override
    public void beforeCommand(QAFExtendedWebElement element, CommandTracker commandTracker) {
        if (commandTracker.getCommand().equalsIgnoreCase(DriverCommand.SEND_KEYS_TO_ELEMENT)) {
            element.clear();
            String value = String.valueOf(commandTracker.getParameters().get("value"));
            if (StringUtil.isBlank(value)) {
                // nothing to type — skip the actual sendKeys command
                commandTracker.setResponce(new Response());
            }
        }
    }
}
```

Registration:
```properties
we.command.listeners=com.matrix.listeners.SendKeysListener
```

---

### Configuration Listener — Post-Load Setup

```java
package com.matrix.listeners;

import com.qmetry.qaf.automation.core.QAFConfigurationListener;
import com.qmetry.qaf.automation.util.PropertyUtil;

public class ConfigListener implements QAFConfigurationListener {

    @Override
    public void onLoad(PropertyUtil bundle) {
        // e.g. load additional properties or override values after config loads
        bundle.setProperty("env.loaded", "true");
    }

    @Override
    public void onChange() {
        // called when any property changes at runtime
    }
}
```

Registration:
```properties
qaf.listeners=com.matrix.listeners.ConfigListener
```

---

### Combined Listener — Single class for multiple concerns

```java
package com.matrix.listeners;

import com.qmetry.qaf.automation.testng.listener.QAFListenerAdapter;
import com.qmetry.qaf.automation.step.client.StepExecutionTracker;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.ui.webdriver.commandtracker.CommandTracker;

public class MatrixListener extends QAFListenerAdapter {

    @Override
    public void onFailure(StepExecutionTracker tracker) {
        // e.g. take screenshot, log step name
    }

    @Override
    public void beforeCommand(QAFExtendedWebElement element, CommandTracker commandTracker) {
        // e.g. highlight element before every interaction
    }

    @Override
    public void afterCommand(QAFExtendedWebElement element, CommandTracker commandTracker) {
        // e.g. log element action to custom report
    }
}
```

Registration:
```properties
qaf.listeners=com.matrix.listeners.MatrixListener
```

---

## Output Format

Generate:
1. **Listener class** in `src/test/java/com/matrix/listeners/`
   - Correct adapter base class for the requested type
   - Only override the methods needed for the described behaviour
   - Correct imports for `CommandTracker`, `StepExecutionTracker`, `QAFExtendedWebDriver`, `QAFExtendedWebElement`
2. **Registration line** to add in `resources/application.properties`
3. **Brief comment** in the class only if the logic is non-obvious (no method-level Javadoc blocks)

## Task

The user will describe the listener type and the behaviour they want. Generate the complete listener class and its registration entry.
