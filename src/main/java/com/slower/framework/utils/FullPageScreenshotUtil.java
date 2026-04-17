package com.slower.framework.utils;

import com.slower.framework.exceptions.FrameworkException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

final class FullPageScreenshotUtil {
    private FullPageScreenshotUtil() {
    }

    static String capture(WebDriver driver, String fileBaseName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            Number devicePixelRatio = (Number) js.executeScript("return window.devicePixelRatio || 1;");
            double dpr = devicePixelRatio == null ? 1.0 : devicePixelRatio.doubleValue();

            long originalX = asLong(js.executeScript("return window.pageXOffset || 0;"));
            long originalY = asLong(js.executeScript("return window.pageYOffset || 0;"));

            long viewportHeightCss = asLong(js.executeScript(
                    "return Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"));
            if (viewportHeightCss <= 0) {
                return ScreenshotUtil.capture(driver, fileBaseName);
            }

            List<BufferedImage> shots = new ArrayList<>();
            long scrollY = -1;
            int safety = 0;

            // Dynamic pages may increase scroll height while scrolling (lazy loading).
            // Keep scrolling until we reach bottom AND scrollHeight stabilizes.
            while (safety++ < 250) {
                long totalHeightCss = getTotalHeightCss(js);
                if (totalHeightCss <= 0) {
                    break;
                }

                long nextY;
                if (scrollY < 0) {
                    nextY = 0;
                } else {
                    nextY = scrollY + viewportHeightCss;
                }

                long maxY = Math.max(0, totalHeightCss - viewportHeightCss);
                if (nextY > maxY) {
                    nextY = maxY;
                }

                if (nextY == scrollY) {
                    // Already at bottom for current scrollHeight; wait for height to stabilize.
                    long stableHeight = waitForScrollHeightStable(driver, js);
                    long newMaxY = Math.max(0, stableHeight - viewportHeightCss);
                    if (scrollY >= newMaxY) {
                        break;
                    }
                    // Height grew; continue scrolling.
                    continue;
                }

                js.executeScript("window.scrollTo(arguments[0], arguments[1]);", 0, nextY);
                long expectedY = nextY;
                ScreenshotUtil.waitUntil(
                        driver,
                        () -> {
                            Object yy = js.executeScript("return Math.round(window.pageYOffset || 0);");
                            if (yy instanceof Number n) {
                                return Math.abs(n.longValue() - expectedY) <= 2;
                            }
                            return false;
                        },
                        Duration.ofSeconds(ConfigReader.getTimeoutSeconds()),
                        "Timed out waiting for scroll to y=" + expectedY
                );

                // Give lazy-loaded pages a chance to grow; no Thread.sleep, wait by polling.
                waitForScrollHeightStable(driver, js);

                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                BufferedImage img = ImageIO.read(src);
                if (img == null) {
                    throw new FrameworkException("Failed to read screenshot image from WebDriver output");
                }
                shots.add(img);
                scrollY = expectedY;
            }

            if (shots.isEmpty()) {
                return ScreenshotUtil.capture(driver, fileBaseName);
            }

            long finalTotalHeightCss = getTotalHeightCss(js);
            int firstWidth = shots.getFirst().getWidth();
            int stitchedHeightPx = (int) Math.round(finalTotalHeightCss * dpr);
            BufferedImage stitched = new BufferedImage(firstWidth, stitchedHeightPx, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = stitched.createGraphics();

            int currentY = 0;
            for (int i = 0; i < shots.size(); i++) {
                BufferedImage shot = shots.get(i);

                int remaining = stitchedHeightPx - currentY;
                int drawHeight = Math.min(shot.getHeight(), remaining);
                if (drawHeight <= 0) {
                    break;
                }

                BufferedImage cropped = shot;
                if (drawHeight < shot.getHeight()) {
                    cropped = shot.getSubimage(0, shot.getHeight() - drawHeight, shot.getWidth(), drawHeight);
                }

                g.drawImage(cropped, 0, currentY, null);
                currentY += drawHeight;
            }
            g.dispose();

            Path out = ScreenshotUtil.screenshotsDir().resolve(fileBaseName + "_full.png");
            ImageIO.write(stitched, "png", out.toFile());

            js.executeScript("window.scrollTo(arguments[0], arguments[1]);", originalX, originalY);
            return out.toString();
        } catch (Exception e) {
            throw (e instanceof FrameworkException fe) ? fe : new FrameworkException("Full-page screenshot failed", e);
        }
    }

    private static long getTotalHeightCss(JavascriptExecutor js) {
        return asLong(js.executeScript(
                "return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight," +
                        "document.body.offsetHeight, document.documentElement.offsetHeight," +
                        "document.body.clientHeight, document.documentElement.clientHeight);"));
    }

    private static long waitForScrollHeightStable(WebDriver driver, JavascriptExecutor js) {
        final long[] last = new long[]{-1L};
        ScreenshotUtil.waitUntil(
                driver,
                () -> {
                    long h = getTotalHeightCss(js);
                    boolean stable = (h > 0 && h == last[0]);
                    last[0] = h;
                    return stable;
                },
                Duration.ofSeconds(ConfigReader.getTimeoutSeconds()),
                "Timed out waiting for page height to stabilize"
        );
        return last[0];
    }

    private static long asLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }
}
