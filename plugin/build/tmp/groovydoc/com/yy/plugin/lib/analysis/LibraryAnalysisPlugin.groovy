package com.yy.plugin.lib.analysis

import com.yy.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.yy.plugin.lib.analysis.task.DependencyTreeReportTask
import com.yy.plugin.lib.analysis.util.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * 1. 白名单
 * 2. 数据统计
 * 3. 共用标识
 */
class LibraryAnalysisPlugin implements Plugin<Project> {
    private static final EXTENSION_NAME = 'depLibReport'
    private static final BASE_GROUP     = 'reporting'
    private static final TASK_PREFIX    = 'depLibReport'

    private LibraryAnalysisExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, LibraryAnalysisExtension)

        project.afterEvaluate {
            createTask(project)
        }
    }

    void createTask(Project project) {
        def configurations = project.configurations

        configurations.findAll {
            return !it.allDependencies.isEmpty() && getConfigurationSize(it) > 0
        }.each {
            def conf = it.getName()
            def task = project.tasks.create(genTaskName(conf), DependencyTreeReportTask)
            task.configuration = it
            task.group = BASE_GROUP
            task.extension = extension
            if (!extension.log) {
                Logger.D = null
            }
        }
    }

    static String genTaskName(String name) {
        char[] arr = name.toCharArray()
        if (arr[0].lowerCase) {
            arr[0] = Character.toUpperCase(arr[0])
            "${TASK_PREFIX}${String.valueOf(arr)}"
        } else {
            "${TASK_PREFIX}${name}"
        }
    }

    static int getConfigurationSize(Configuration conf) {
        try {
            return conf.incoming.resolutionResult.allDependencies.size()
        } catch (Exception e) {
            // ignore
        }
        0
    }
}
