package org.siouan.frontendgradleplugin.tasks;

import static org.siouan.frontendgradleplugin.util.FunctionalTestHelper.assertTaskOutcome;
import static org.siouan.frontendgradleplugin.util.FunctionalTestHelper.runGradle;
import static org.siouan.frontendgradleplugin.util.FunctionalTestHelper.runGradleAndExpectFailure;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.siouan.frontendgradleplugin.util.FunctionalTestHelper;

/**
 * Functional tests to verify the {@link NodeInstallTask} integration in a Gradle build.
 */
class NodeInstallTaskFuncTest {

    private static final String GRADLE_NODE_INSTALL_TASK = "installNode";

    @TempDir
    protected File projectDirectory;

    @Test
    public void shouldFailInstallingNodeWhenVersionIsNotSet() throws IOException {
        FunctionalTestHelper.createBuildFile(projectDirectory, Collections.emptyMap());

        final BuildResult result = runGradleAndExpectFailure(projectDirectory, GRADLE_NODE_INSTALL_TASK);

        assertTaskOutcome(result, NodeInstallTask.DEFAULT_NAME, TaskOutcome.FAILED);
    }

    @Test
    public void shouldFailInstallingNodeWhenDistributionCannotBeDownloadedWithUnknownVersion() throws IOException {
        FunctionalTestHelper.createBuildFile(projectDirectory, Collections.singletonMap("nodeVersion", "0.76.34"));

        final BuildResult result = runGradleAndExpectFailure(projectDirectory, GRADLE_NODE_INSTALL_TASK);

        assertTaskOutcome(result, NodeInstallTask.DEFAULT_NAME, TaskOutcome.FAILED);
    }

    @Test
    public void shouldFailInstallingNodeWhenDistributionCannotBeDownloadedWithInvalidUrl() throws IOException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("nodeVersion", "10.15.3");
        properties.put("nodeDistributionUrl", "protocol://domain/unknown");
        FunctionalTestHelper.createBuildFile(projectDirectory, properties);

        final BuildResult result = runGradleAndExpectFailure(projectDirectory, GRADLE_NODE_INSTALL_TASK);

        assertTaskOutcome(result, NodeInstallTask.DEFAULT_NAME, TaskOutcome.FAILED);
    }

    @Test
    public void shouldInstallNodeFirstAndNothingMoreSecondly() throws IOException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("nodeVersion", "10.15.3");
        properties.put("nodeDistributionUrl", getClass().getClassLoader().getResource("node-v10.15.3.zip").toString());
        FunctionalTestHelper.createBuildFile(projectDirectory, properties);

        final BuildResult result1 = runGradle(projectDirectory, GRADLE_NODE_INSTALL_TASK);

        assertTaskOutcome(result1, NodeInstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);

        final BuildResult result2 = runGradle(projectDirectory, GRADLE_NODE_INSTALL_TASK);

        assertTaskOutcome(result2, NodeInstallTask.DEFAULT_NAME, TaskOutcome.UP_TO_DATE);
    }
}
