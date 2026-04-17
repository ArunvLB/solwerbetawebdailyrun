package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.NavBar;
import com.slower.framework.pages.SolutionsOverviewPage;
import com.slower.framework.utils.FormData;
import org.testng.annotations.Test;

public class SolutionsBookDemoTest extends BaseClass {

    @Test
    public void shouldBookDemoForAllSolutionsAndNavigateBackEachTime() {
        FormData data = FormData.fromConfig();
        SolutionsOverviewPage solutions = new NavBar(DriverFactory.getDriver()).goToSolutionsOverview();

        String[] solutionNames = new String[]{
                "KAIZEN IoT",
                "Transport & Warehouse Management",
                "Carbon Footprint Management",
                "Vehicle Digital Inspection",
                "Aftermarket SuperApp"
        };

        for (String solutionName : solutionNames) {
            solutions
                    .openBookDemoFor(solutionName)
                    .fillBookDemoFormAndSubmit(data, sanitize(solutionName))
                    .navigateBackToOverview("solwer");
        }
    }

    private String sanitize(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
}

