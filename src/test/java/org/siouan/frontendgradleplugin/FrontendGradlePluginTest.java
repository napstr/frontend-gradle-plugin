package org.siouan.frontendgradleplugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siouan.frontendgradleplugin.infrastructure.gradle.AssembleTask;
import org.siouan.frontendgradleplugin.infrastructure.gradle.CheckTask;
import org.siouan.frontendgradleplugin.infrastructure.gradle.CleanTask;
import org.siouan.frontendgradleplugin.infrastructure.gradle.FrontendExtension;
import org.siouan.frontendgradleplugin.infrastructure.gradle.InstallTask;
import org.siouan.frontendgradleplugin.infrastructure.gradle.NodeInstallTask;
import org.siouan.frontendgradleplugin.infrastructure.gradle.YarnInstallTask;

/**
 * Unit tests for the {@link FrontendGradlePlugin} class.
 */
@ExtendWith(MockitoExtension.class)
class FrontendGradlePluginTest {

    private FrontendGradlePlugin plugin;

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        plugin = new FrontendGradlePlugin();
    }

    @Test
    void shouldRegisterTasksWithDefaultExtensionValuesWhenApplied() {
        plugin.apply(project);

        final FrontendExtension extension = project.getExtensions().findByType(FrontendExtension.class);

        assertThat(extension).isNotNull();
        assertThat(extension.getYarnEnabled().get()).isFalse();
        assertThat(extension.getNodeInstallDirectory().getAsFile().get()).isEqualTo(
            project.file(FrontendGradlePlugin.DEFAULT_NODE_INSTALL_DIRNAME));
        assertThat(extension.getYarnInstallDirectory().getAsFile().get()).isEqualTo(
            project.file(FrontendGradlePlugin.DEFAULT_YARN_INSTALL_DIRNAME));

        final NodeInstallTask nodeInstallTask = project
            .getTasks()
            .named(FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, NodeInstallTask.class)
            .get();
        assertThat(nodeInstallTask.getNodeVersion().isPresent()).isFalse();
        assertThat(nodeInstallTask.getNodeDistributionUrl().isPresent()).isFalse();
        assertThat(nodeInstallTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(nodeInstallTask.getDependsOn()).isEmpty();

        final YarnInstallTask yarnInstallTask = project
            .getTasks()
            .named(FrontendGradlePlugin.YARN_INSTALL_TASK_NAME, YarnInstallTask.class)
            .get();
        assertThat(yarnInstallTask.getOnlyIf()).isNotNull();
        assertThat(yarnInstallTask.getYarnVersion().isPresent()).isFalse();
        assertThat(yarnInstallTask.getYarnDistributionUrl().isPresent()).isFalse();
        assertThat(yarnInstallTask.getYarnInstallDirectory().get()).isEqualTo(
            extension.getYarnInstallDirectory().get());
        assertThat(yarnInstallTask.getDependsOn()).isEmpty();

        final InstallTask frontendInstallTask = project
            .getTasks()
            .named(FrontendGradlePlugin.INSTALL_TASK_NAME, InstallTask.class)
            .get();
        assertThat(frontendInstallTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendInstallTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendInstallTask.getYarnInstallDirectory().isPresent()).isFalse();
        assertThat(frontendInstallTask.getDependsOn()).containsExactlyInAnyOrder(nodeInstallTask.getName(),
            yarnInstallTask.getName());

        final CleanTask frontendCleanTask = project
            .getTasks()
            .named(FrontendGradlePlugin.CLEAN_TASK_NAME, CleanTask.class)
            .get();
        assertThat(frontendCleanTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendCleanTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendCleanTask.getYarnInstallDirectory().isPresent()).isFalse();
        assertThat(frontendCleanTask.getCleanScript().isPresent()).isFalse();
        assertThat(frontendCleanTask.getDependsOn()).isEmpty();
        assertThat(project.getTasks().named(BasePlugin.CLEAN_TASK_NAME).get().getDependsOn()).contains(
            frontendCleanTask.getName());

        final AssembleTask frontendAssembleTask = project
            .getTasks()
            .named(FrontendGradlePlugin.ASSEMBLE_TASK_NAME, AssembleTask.class)
            .get();
        assertThat(frontendAssembleTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendAssembleTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendAssembleTask.getYarnInstallDirectory().isPresent()).isFalse();
        assertThat(frontendAssembleTask.getAssembleScript().isPresent()).isFalse();
        assertThat(frontendAssembleTask.getDependsOn()).isEmpty();
        assertThat(project.getTasks().named(BasePlugin.ASSEMBLE_TASK_NAME).get().getDependsOn()).contains(
            frontendAssembleTask.getName());

        final CheckTask frontendCheckTask = project
            .getTasks()
            .named(FrontendGradlePlugin.CHECK_TASK_NAME, CheckTask.class)
            .get();
        assertThat(frontendCheckTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendCheckTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendCheckTask.getYarnInstallDirectory().isPresent()).isFalse();
        assertThat(frontendCheckTask.getCheckScript().isPresent()).isFalse();
        assertThat(frontendCheckTask.getDependsOn()).isEmpty();
        assertThat(project.getTasks().named(FrontendGradlePlugin.GRADLE_CHECK_TASK_NAME).get().getDependsOn()).contains(
            frontendCheckTask.getName());
    }

    @Test
    void shouldRegisterTasksWithCustomExtensionValuesWhenApplied() {
        plugin.apply(project);

        final FrontendExtension extension = project.getExtensions().findByType(FrontendExtension.class);
        extension.getNodeVersion().set("3.65.4");
        extension.getNodeDistributionUrl().set("https://node");
        extension.getNodeInstallDirectory().set(project.file("node-dist"));
        extension.getYarnEnabled().set(true);
        extension.getYarnVersion().set("6.5.4");
        extension.getYarnDistributionUrl().set("http://yarn");
        extension.getYarnInstallDirectory().set(project.file("yarn-dist"));
        extension.getCleanScript().set("clean");
        extension.getAssembleScript().set("assemble");
        extension.getCheckScript().set("test");

        final NodeInstallTask nodeInstallTask = project
            .getTasks()
            .named(FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, NodeInstallTask.class)
            .get();
        assertThat(nodeInstallTask.getNodeVersion().get()).isEqualTo(extension.getNodeVersion().get());
        assertThat(nodeInstallTask.getNodeDistributionUrl().get()).isEqualTo(extension.getNodeDistributionUrl().get());
        assertThat(nodeInstallTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(nodeInstallTask.getDependsOn()).isEmpty();

        final YarnInstallTask yarnInstallTask = project
            .getTasks()
            .named(FrontendGradlePlugin.YARN_INSTALL_TASK_NAME, YarnInstallTask.class)
            .get();
        assertThat(yarnInstallTask.getOnlyIf()).isNotNull();
        assertThat(yarnInstallTask.getYarnVersion().get()).isEqualTo(extension.getYarnVersion().get());
        assertThat(yarnInstallTask.getYarnDistributionUrl().get()).isEqualTo(extension.getYarnDistributionUrl().get());
        assertThat(yarnInstallTask.getYarnInstallDirectory().get()).isEqualTo(
            extension.getYarnInstallDirectory().get());
        assertThat(yarnInstallTask.getDependsOn()).isEmpty();

        final InstallTask frontendInstallTask = project
            .getTasks()
            .named(FrontendGradlePlugin.INSTALL_TASK_NAME, InstallTask.class)
            .get();
        assertThat(frontendInstallTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendInstallTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendInstallTask.getYarnInstallDirectory().get()).isEqualTo(
            extension.getYarnInstallDirectory().get());
        assertThat(frontendInstallTask.getDependsOn()).containsExactlyInAnyOrder(nodeInstallTask.getName(),
            yarnInstallTask.getName());

        final CleanTask frontendCleanTask = project
            .getTasks()
            .named(FrontendGradlePlugin.CLEAN_TASK_NAME, CleanTask.class)
            .get();
        assertThat(frontendCleanTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendCleanTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendCleanTask.getYarnInstallDirectory().get()).isEqualTo(
            extension.getYarnInstallDirectory().get());
        assertThat(frontendCleanTask.getCleanScript().get()).isEqualTo(extension.getCleanScript().get());
        assertThat(frontendCleanTask.getDependsOn()).containsExactlyInAnyOrder(frontendInstallTask.getName());
        assertThat(project.getTasks().named(BasePlugin.CLEAN_TASK_NAME).get().getDependsOn()).contains(
            frontendCleanTask.getName());

        final AssembleTask frontendAssembleTask = project
            .getTasks()
            .named(FrontendGradlePlugin.ASSEMBLE_TASK_NAME, AssembleTask.class)
            .get();
        assertThat(frontendAssembleTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendAssembleTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendAssembleTask.getYarnInstallDirectory().get()).isEqualTo(
            extension.getYarnInstallDirectory().get());
        assertThat(frontendAssembleTask.getAssembleScript().get()).isEqualTo(extension.getAssembleScript().get());
        assertThat(frontendAssembleTask.getDependsOn()).containsExactlyInAnyOrder(frontendInstallTask.getName());
        assertThat(project.getTasks().named(BasePlugin.ASSEMBLE_TASK_NAME).get().getDependsOn()).contains(
            frontendAssembleTask.getName());

        final CheckTask frontendCheckTask = project
            .getTasks()
            .named(FrontendGradlePlugin.CHECK_TASK_NAME, CheckTask.class)
            .get();
        assertThat(frontendCheckTask.getYarnEnabled().get()).isEqualTo(extension.getYarnEnabled().get());
        assertThat(frontendCheckTask.getNodeInstallDirectory().get()).isEqualTo(
            extension.getNodeInstallDirectory().get());
        assertThat(frontendCheckTask.getYarnInstallDirectory().get()).isEqualTo(
            extension.getYarnInstallDirectory().get());
        assertThat(frontendCheckTask.getCheckScript().get()).isEqualTo(extension.getCheckScript().get());
        assertThat(frontendCheckTask.getDependsOn()).containsExactlyInAnyOrder(frontendInstallTask.getName());
        assertThat(project.getTasks().named(FrontendGradlePlugin.GRADLE_CHECK_TASK_NAME).get().getDependsOn()).contains(
            frontendCheckTask.getName());
    }
}
