package com.yy.plugin.lib.analysis.task

import com.yy.plugin.lib.analysis.convert.NodeConvert
import com.yy.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.yy.plugin.lib.analysis.model.FileDictionary
import com.yy.plugin.lib.analysis.model.Library
import com.yy.plugin.lib.analysis.render.HtmlRenderer
import com.yy.plugin.lib.analysis.render.OutputModuleList
import com.yy.plugin.lib.analysis.render.TextRenderer
import com.yy.plugin.lib.analysis.util.Logger
import com.yy.plugin.lib.analysis.util.PackageChecker
import com.yy.plugin.lib.analysis.util.ResourceUtils
import com.yy.plugin.lib.analysis.util.Timer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.diagnostics.AbstractReportTask
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult


class DependencyTreeReportTask extends AbstractReportTask {
    def renderer = new AsciiDependencyReportRenderer()

    Configuration configuration
    LibraryAnalysisExtension extension

    @Override
    protected ReportRenderer getRenderer() {
        return renderer
    }

    @Override
    protected void generate(Project project) throws IOException {
        def timer = new Timer()

        try {
            outputHtml()
            if (extension.showTree) {
                Logger.W.log("renderer startConfiguration")
                renderer.startConfiguration(configuration)
                renderer.render(configuration)
                renderer.completeConfiguration(configuration)
            }
        } catch (Exception e) {
            e.printStackTrace()
            Logger.W.log("generate report file failed!!! ERROR: " + e.message)
        }

        timer.mark(Logger.W, "${getName()} total")
    }

    private void outputHtml() {
        def timer = new Timer()

        def output = prepareOutputPath()
//        output = "D:\\temp"
        ResourceUtils.copyResources(output)
        Logger.W?.log("Html output path: ${output}")
        timer.mark(Logger.W, "copy resources")

        def resolutionResult = configuration.getIncoming().getResolutionResult()
        def dep = new RenderableModuleResult(resolutionResult.getRoot())

        timer.mark(Logger.W, "get dependencies")

        Logger.W?.log("create PackageChecker")
//        Logger.W?.log("create getAllDependencies" + configuration.getAllDependencies())

        // 通过依赖文件创建依赖字典
        def packageChecker = new PackageChecker()
        Logger.W?.log("create  dictionary")
        Logger.W?.log("configuration.getIncoming:" + configuration.getIncoming())
        Logger.W?.log("configuration.getIncoming getFiles:" + configuration.getIncoming().getFiles())
        def dictionary = new FileDictionary(configuration.getIncoming().getFiles())
        Logger.W?.log("create root library")

        def rootLib = Library.create(dep, dictionary)

        timer.mark(Logger.W, "create root library")

        extension.ignore?.each {
            rootLib.applyIgnoreLibrary(it)
        }

        def root = NodeConvert.convert(rootLib,
                NodeConvert.Args.get(dictionary).extension(extension).checker(packageChecker).brief(!extension.fullTree))

        timer.mark(Logger.W, "create root node")

        def msg = packageChecker.outputPackageRepeatList()
        def list = outputModuleList(rootLib, packageChecker)
        list.modules.each {
            Logger.D?.log("module: ${it.name}")
        }

        timer.mark(Logger.W, "output module list")

        if (extension.output.contains("html")) {
            def result = new HtmlRenderer(output).render(root, list, msg)
            if (msg && !msg.isEmpty()) {
                println msg
            }
            Logger.W?.log("Html output: ${result}")

            timer.mark(Logger.W, "output html file")
        }

        if (extension.output.contains("txt")) {
            def result = new TextRenderer(output).render(root, list, msg)
            Logger.W?.log("Txt output: ${result}")

            timer.mark(Logger.W, "output txt file")
        }
    }

    static OutputModuleList outputModuleList(Library root, PackageChecker checker) {
        OutputModuleList list = new OutputModuleList()
        root.contains?.each {
            if (!it.file) {
                list.addModule(new OutputModuleList.DependencyOutput(it.id, 0, "",
                        "pom", "",
                        it.contains.size(), it.useCount, it.useCountImmediate, ""))
                return
            }
            def pkgName = checker.parseModuleName(it.id, it.file.file)
            def isRepeat = checker.isRepeatPackage(pkgName)
            list.addModule(new OutputModuleList.DependencyOutput(it.id, it.file.size, pkgName,
                    it.file.type, isRepeat ? "package name repeat" : "",
                    it.contains.size(), it.useCount, it.useCountImmediate, isRepeat ? "danger" : ""))
        }
        list.sortModules()
        list
    }

    @Deprecated
    static OutputModuleList outputModuleList(FileDictionary dictionary, PackageChecker checker) {
        OutputModuleList list = new OutputModuleList()
        dictionary.cacheInfoMap.each {
            key, value ->
                def pkgName = checker.parseModuleName(key, value.file)
                def isRepeat = checker.isRepeatPackage(pkgName)
                list.addModule(new OutputModuleList.DependencyOutput(key, value.size, pkgName,
                        value.type, isRepeat ? "package name repeat" : "", 0, 0, 0, isRepeat ? "danger" : ""))
        }
        list.sortModules()
        list
    }

    private String prepareOutputPath() {
        def path = "${project.buildDir}/${extension.outputPath}/${configuration.name}"
        def file = new File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        path
    }

}