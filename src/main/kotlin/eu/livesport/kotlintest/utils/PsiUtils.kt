package eu.livesport.kotlintest.utils

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.testIntegration.TestFramework
import com.intellij.util.IncorrectOperationException
import com.intellij.util.SmartList
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtClassOrObject

fun PsiClass.isUnderTestSources(): Boolean {
    val psiFile = this.containingFile
    val vFile = psiFile.virtualFile ?: return false
    return ProjectRootManager.getInstance(this.project).fileIndex.isInTestSourceContent(vFile)
}

fun correctK2KIssue(project: Project, editor: Editor) {
    try {
        project.executeWriteCommand("Correct K2K Issue") {
            PsiDocumentManager.getInstance(project).commitAllDocuments()
            with(editor.document) {
                setText(text.replace(": `fun`", ""))
            }
        }
    } catch (e: IncorrectOperationException) {
        val message =
            KotlinBundle.message("action.generate.test.support.error.cant.generate.method", e.message.toString())
        HintManager.getInstance().showErrorHint(editor, message)
    }
}

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// Filter for Kotlin based TestFrameworks and return all possible frameworks
fun findSuitableFrameworks(klass: KtClassOrObject): List<TestFramework> {
    val lightClass = klass.toLightClass() ?: return emptyList()
    val frameworks = TestFramework.EXTENSION_NAME.extensionList.filter { it.language == KotlinLanguage.INSTANCE }
    return frameworks
        .filter { it.isTestClass(lightClass) }
        .filterTo(SmartList()) { it.isPotentialTestClass(lightClass) }
}
