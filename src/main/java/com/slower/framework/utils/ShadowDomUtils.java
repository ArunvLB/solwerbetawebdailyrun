package com.slower.framework.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;

public final class ShadowDomUtils {
    private ShadowDomUtils() {
    }

    @SuppressWarnings("unchecked")
    public static List<WebElement> querySelectorAllDeep(WebDriver driver, String cssSelector) {
        Object res = ((JavascriptExecutor) driver).executeScript(
                "const selector = arguments[0];" +
                        "const results = [];" +
                        "const seen = new Set();" +
                        "function collect(root) {" +
                        "  if (!root) return;" +
                        "  try {" +
                        "    root.querySelectorAll(selector).forEach(el => {" +
                        "      if (!seen.has(el)) { seen.add(el); results.push(el); }" +
                        "    });" +
                        "  } catch (e) {}" +
                        "  const all = root.querySelectorAll ? root.querySelectorAll('*') : [];" +
                        "  for (const el of all) {" +
                        "    if (el && el.shadowRoot) collect(el.shadowRoot);" +
                        "  }" +
                        "}" +
                        "collect(document);" +
                        "return results;",
                cssSelector
        );
        if (res instanceof List<?>) {
            return (List<WebElement>) res;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static List<WebElement> querySelectorAllDeep(WebDriver driver, WebElement root, String cssSelector) {
        Object res = ((JavascriptExecutor) driver).executeScript(
                "const root = arguments[0];" +
                        "const selector = arguments[1];" +
                        "const results = [];" +
                        "const seen = new Set();" +
                        "function collect(node) {" +
                        "  if (!node) return;" +
                        "  try {" +
                        "    node.querySelectorAll(selector).forEach(el => {" +
                        "      if (!seen.has(el)) { seen.add(el); results.push(el); }" +
                        "    });" +
                        "  } catch (e) {}" +
                        "  const all = node.querySelectorAll ? node.querySelectorAll('*') : [];" +
                        "  for (const el of all) {" +
                        "    if (el && el.shadowRoot) collect(el.shadowRoot);" +
                        "  }" +
                        "}" +
                        "collect(root);" +
                        "return results;",
                root, cssSelector
        );
        if (res instanceof List<?>) {
            return (List<WebElement>) res;
        }
        return Collections.emptyList();
    }
}

