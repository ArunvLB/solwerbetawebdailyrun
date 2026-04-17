package com.slower.framework.utils;

import com.slower.framework.exceptions.FrameworkException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class FormFiller {
    private static final Logger log = LogManager.getLogger(FormFiller.class);

    private final WebDriver driver;
    private final WaitUtils waits;
    private final ActionUtils actions;

    public FormFiller(WebDriver driver) {
        this.driver = driver;
        this.waits = new WaitUtils(driver);
        this.actions = new ActionUtils(driver);
    }

    public void fillAndSubmitVisibleForm(FormData data) {
        try {
            WebElement form = waitForVisibleFormContainer();
            fillForm(form, data);
            attemptSubmit(form);
        } catch (RuntimeException e) {
            String screenshot = null;
            try {
                screenshot = ScreenshotUtil.captureFullPage(driver, "form_failure");
            } catch (Exception ignored) {
            }
            ReportManager.logFail("Form fill/submit failed: " + e.getMessage(), screenshot);
            throw new FrameworkException("Form fill/submit failed", e);
        }
    }

    private WebElement waitForVisibleFormContainer() {
        Duration timeout = Duration.ofSeconds(ConfigReader.getTimeoutSeconds());
        WebDriverWait w = new WebDriverWait(driver, timeout);

        // Prefer <form>, but some sites use dialog divs with inputs.
        List<By> candidates = List.of(
                By.cssSelector("form"),
                By.cssSelector("[role='dialog']"),
                By.cssSelector(".modal, .modal-content, .dialog, .popup")
        );

        for (By by : candidates) {
            try {
                WebElement el = w.until(ExpectedConditions.visibilityOfElementLocated(by));
                if (el != null) {
                    return el;
                }
            } catch (TimeoutException ignored) {
            }
        }
        throw new FrameworkException("No visible form/dialog found to fill");
    }

    private void fillForm(WebElement container, FormData data) {
        waits.waitForPageLoad();
        List<WebElement> fields = new ArrayList<>(container.findElements(By.cssSelector("input, textarea, select")));
        try {
            fields.addAll(ShadowDomUtils.querySelectorAllDeep(driver, container, "input, textarea, select"));
        } catch (Exception ignored) {
        }

        // De-duplicate while preserving order
        LinkedHashSet<WebElement> unique = new LinkedHashSet<>(fields);
        fields = new ArrayList<>(unique);

        for (WebElement field : fields) {
            if (!isInteractable(field)) {
                continue;
            }
            String tag = safeLower(field.getTagName());
            String type = safeLower(field.getAttribute("type"));

            if ("input".equals(tag) && ("hidden".equals(type) || "submit".equals(type) || "button".equals(type) || "file".equals(type))) {
                continue;
            }
            if ("input".equals(tag) && ("checkbox".equals(type) || "radio".equals(type))) {
                // If required checkbox is present, tick it.
                if (!field.isSelected()) {
                    String meta = fieldMeta(field);
                    if (looksLikeConsent(meta) || isRequired(field)) {
                        actions.click(field, "Checkbox/Radio");
                    }
                }
                continue;
            }

            String meta = fieldMeta(field);
            Optional<String> value = valueFor(meta, data, type);
            if (value.isEmpty()) {
                continue;
            }

            if ("select".equals(tag)) {
                selectValue(field, value.get());
            } else {
                setInputValue(field, value.get());
            }
        }
    }

    private void attemptSubmit(WebElement container) {
        WebElement submit = findSubmitButton(container);
        if (submit == null) {
            throw new FrameworkException("No submit button found on the form");
        }
        actions.click(submit, "Submit");

        // Wait for either navigation, success text, or validation errors to appear.
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeoutSeconds()));
            w.until(d -> {
                try {
                    String bodyText = d.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
                    return bodyText.contains("thank") || bodyText.contains("success") || bodyText.contains("submitted")
                            || bodyText.contains("captcha") || bodyText.contains("recaptcha")
                            || hasVisibleErrors(container)
                            || !"complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState"));
                } catch (Exception e) {
                    return true;
                }
            });
            waits.waitForDuration(Duration.ofSeconds(1));
        } catch (TimeoutException e) {
            // Don't hard-fail: requirement is "try to submit". Many forms remain idle due to CAPTCHA or server-side validation.
            log.info("Submit attempt did not complete within timeout (likely CAPTCHA/validation). Continuing.");
            ReportManager.logInfo("Submit attempt did not complete within timeout (likely CAPTCHA/validation).");
            try {
                ScreenshotUtil.captureFullPage(driver, "submit_timeout");
            } catch (Exception ignored) {
            }
        }
    }

    private boolean hasVisibleErrors(WebElement container) {
        try {
            List<WebElement> errors = container.findElements(By.cssSelector(".error, .invalid-feedback, .field-error, [aria-invalid='true']"));
            return errors.stream().anyMatch(this::isDisplayedSafe);
        } catch (Exception e) {
            return false;
        }
    }

    private WebElement findSubmitButton(WebElement container) {
        List<By> selectors = List.of(
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.xpath(".//button[" + XPathUtils.ciContains("normalize-space(.)", "submit") + " or " +
                        XPathUtils.ciContains("normalize-space(.)", "book") + " or " +
                        XPathUtils.ciContains("normalize-space(.)", "demo") + " or " +
                        XPathUtils.ciContains("normalize-space(.)", "send") + " or " +
                        XPathUtils.ciContains("normalize-space(.)", "request") + "]")
        );

        for (By by : selectors) {
            try {
                List<WebElement> els = container.findElements(by);
                for (WebElement el : els) {
                    if (isInteractable(el)) {
                        return el;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Shadow DOM fallback
        try {
            List<WebElement> deep = ShadowDomUtils.querySelectorAllDeep(driver, container, "button, input[type='submit']");
            for (WebElement el : deep) {
                if (!isInteractable(el)) {
                    continue;
                }
                String t = (el.getText() == null ? "" : el.getText()).toLowerCase(Locale.ROOT);
                if (t.contains("submit") || (t.contains("book") && t.contains("demo")) || t.contains("send") || t.contains("request")) {
                    return el;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Optional<String> valueFor(String meta, FormData data, String inputType) {
        String m = meta.toLowerCase(Locale.ROOT);
        if ("datetime-local".equals(inputType) || (m.contains("demo") && m.contains("date")) || m.contains("prefer") && m.contains("date")) {
            return Optional.ofNullable(data.preferredDemoDateTime());
        }
        if (m.contains("email") || "email".equals(inputType)) {
            return Optional.ofNullable(data.email());
        }
        if (m.contains("phone") || m.contains("mobile") || m.contains("contact") || "tel".equals(inputType)) {
            return Optional.ofNullable(data.phone());
        }
        if (m.contains("company") || m.contains("organization") || m.contains("organisation") || m.contains("business")) {
            return Optional.ofNullable(data.company());
        }
        if (m.contains("message") || m.contains("comment") || m.contains("requirement") || m.contains("notes")) {
            return Optional.ofNullable(data.message());
        }
        if (m.contains("name") || m.contains("full name") || "text".equals(inputType)) {
            return Optional.ofNullable(data.fullName());
        }
        return Optional.empty();
    }

    private void setInputValue(WebElement field, String value) {
        String tag = safeLower(field.getTagName());
        String type = safeLower(field.getAttribute("type"));

        if ("textarea".equals(tag)) {
            actions.sendKeys(field, "Textarea", value);
            return;
        }

        if ("input".equals(tag) && ("date".equals(type) || "time".equals(type) || "datetime-local".equals(type))) {
            // sendKeys often fails for datetime-local depending on locale; use JS set + events.
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "const el = arguments[0]; const val = arguments[1];" +
                                "el.focus();" +
                                "el.value = val;" +
                                "el.dispatchEvent(new Event('input', {bubbles:true}));" +
                                "el.dispatchEvent(new Event('change', {bubbles:true}));",
                        field, value
                );
                ReportManager.logInfo("Set date/time field via JS");
                return;
            } catch (Exception e) {
                // fallback
                actions.sendKeys(field, "Date/Time field", value);
                return;
            }
        }

        actions.sendKeys(field, "Field", value);
    }

    private void selectValue(WebElement selectEl, String preferred) {
        try {
            Select select = new Select(selectEl);
            // Try preferred, otherwise select first non-empty option.
            try {
                select.selectByVisibleText(preferred);
                return;
            } catch (Exception ignored) {
            }
            for (WebElement option : select.getOptions()) {
                String text = option.getText() == null ? "" : option.getText().trim();
                if (!text.isBlank() && !text.equalsIgnoreCase("select") && !text.equalsIgnoreCase("choose")) {
                    select.selectByVisibleText(text);
                    return;
                }
            }
        } catch (Exception e) {
            log.info("Select field could not be set: {}", e.getMessage());
        }
    }

    private boolean isInteractable(WebElement el) {
        try {
            return el.isDisplayed() && el.isEnabled();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private boolean isDisplayedSafe(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean looksLikeConsent(String meta) {
        String m = meta.toLowerCase(Locale.ROOT);
        return m.contains("terms") || m.contains("privacy") || m.contains("consent") || m.contains("agree");
    }

    private boolean isRequired(WebElement el) {
        try {
            String required = el.getAttribute("required");
            if (required != null) {
                return true;
            }
            String ariaRequired = el.getAttribute("aria-required");
            return "true".equalsIgnoreCase(ariaRequired);
        } catch (Exception e) {
            return false;
        }
    }

    private String fieldMeta(WebElement field) {
        StringBuilder sb = new StringBuilder();
        append(sb, field.getAttribute("name"));
        append(sb, field.getAttribute("id"));
        append(sb, field.getAttribute("placeholder"));
        append(sb, field.getAttribute("aria-label"));
        append(sb, field.getAttribute("autocomplete"));
        append(sb, field.getAttribute("type"));
        try {
            String label = findAssociatedLabelText(field);
            append(sb, label);
        } catch (Exception ignored) {
        }
        return sb.toString();
    }

    private String findAssociatedLabelText(WebElement field) {
        String id = field.getAttribute("id");
        if (id != null && !id.isBlank()) {
            List<WebElement> labels = driver.findElements(By.cssSelector("label[for='" + cssEscape(id) + "']"));
            for (WebElement l : labels) {
                if (isDisplayedSafe(l)) {
                    return l.getText();
                }
            }
        }
        // Fallback: parent label
        try {
            WebElement parentLabel = field.findElement(By.xpath("ancestor::label[1]"));
            if (parentLabel != null && isDisplayedSafe(parentLabel)) {
                return parentLabel.getText();
            }
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
        }
        return "";
    }

    private String cssEscape(String value) {
        return value.replace("'", "\\'");
    }

    private void append(StringBuilder sb, String value) {
        if (value == null) {
            return;
        }
        String v = value.trim();
        if (v.isBlank()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(v);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
