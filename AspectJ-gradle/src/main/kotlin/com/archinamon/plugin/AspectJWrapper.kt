package com.archinamon.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.archinamon.AndroidConfig
import com.archinamon.AspectJExtension
import com.archinamon.api.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import javax.inject.Inject

internal sealed class AspectJWrapper: Plugin<Project> {

    internal class Std @Inject constructor(): AspectJWrapper() {
        override fun getTransformer(project: Project): AspectJTransform = StdTransformer(project)
    }

    internal class Ext @Inject constructor(): AspectJWrapper() {
        override fun getTransformer(project: Project): AspectJTransform = ExtTransformer(project)
    }

    internal class Test @Inject constructor(): AspectJWrapper() {
        override fun getTransformer(project: Project): AspectJTransform = TstTransformer(project)
    }

    override fun apply(project: Project) {
        val config = AndroidConfig(project)
        val settings = project.extensions.create("aspectj", AspectJExtension::class.java)

        configProject(project, config, settings)

        val module: TestedExtension
        val transformer: AspectJTransform
        if (config.isLibraryPlugin) {
            transformer = LibTransformer(project)
            module = project.extensions.getByType(LibraryExtension::class.java)
        } else {
            transformer = getTransformer(project)
            module = project.extensions.getByType(AppExtension::class.java)
        }

        transformer.withConfig(config).prepareProject()
        module.registerTransform(transformer)
    }

    internal abstract fun getTransformer(project: Project): AspectJTransform
}