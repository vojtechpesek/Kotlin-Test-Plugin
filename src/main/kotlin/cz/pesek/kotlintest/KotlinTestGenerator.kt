package cz.pesek.kotlintest

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.CodeInsightUtil
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testIntegration.createTest.CreateTestDialog
import com.intellij.testIntegration.createTest.TestGenerator
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.refactoring.memberInfo.toKotlinMemberInfo
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.plugins.groovy.lang.psi.util.childrenOfType
import java.util.*

@Suppress("InvalidBundleOrProperty")
class KotlinTestGenerator : TestGenerator {

    override fun toString(): String = KotlinLanguage.INSTANCE.displayName

    override fun generateTest(project: Project, d: CreateTestDialog): PsiElement? {
        return PostprocessReformattingAspect.getInstance(project).postponeFormattingInside(Computable {
            ApplicationManager.getApplication().runWriteAction(Computable<PsiElement?> {
                val file = try {
                    generateTestFile(project, d)
                } catch (e: IncorrectOperationException) {
                    ApplicationManager.getApplication().invokeLater {
                        val message =
                            CodeInsightBundle.message("intention.error.cannot.create.class.message", d.className)
                        val title = CodeInsightBundle.message("intention.error.cannot.create.class.title")
                        Messages.showErrorDialog(project, message, title)
                    }
                    null
                }

                file?.let {
                    // without this the file is created but the caret stays in the original file
                    CodeInsightUtil.positionCursor(project, file, file)

                    // Find annotations on the functions from templates and shorten references
                    val classBody = file.childrenOfType<KtClass>().first().childrenOfType<KtClassBody>().first()
                    classBody.childrenOfType<KtNamedFunction>().forEach {
                        ShortenReferences.DEFAULT.process(it.annotationEntries.first())
                    }
                }
                file
            })
        })
    }

    private fun generateTestFile(project: Project, d: CreateTestDialog): PsiFile? {

        val framework = d.selectedTestFrameworkDescriptor

        if (framework is KotlinTestFramework) {
            IdeDocumentHistory.getInstance(project).includeCurrentPlaceAsChangePlace()

            val fileTemplateManager = FileTemplateManager.getInstance(project)
            val classTemplate = fileTemplateManager.getCodeTemplate("Kotlin Test Class.kt")

            val body: StringBuilder = StringBuilder()

            val defaultProperties = fileTemplateManager.defaultProperties
            val props = Properties(defaultProperties)
            props.setProperty(FileTemplate.ATTRIBUTE_NAME, d.className)

            val targetClass = d.targetClass
            if (targetClass != null && targetClass.isValid) {
                props.setProperty(FileTemplate.ATTRIBUTE_CLASS_NAME, targetClass.qualifiedName)
            }

            if (d.shouldGeneratedBefore()) {
                val beforeTemplate = fileTemplateManager.getCodeTemplate("Kotlin Test Before.kt")
                body.append(beforeTemplate.getText(props))
            }

            if (d.shouldGeneratedAfter()) {
                val afterTemplate = fileTemplateManager.getCodeTemplate("Kotlin Test After.kt")
                body.append(afterTemplate.getText(props))
            }

            if (d.selectedMethods.isNotEmpty()) {
                val methodTemplate = fileTemplateManager.getCodeTemplate("Kotlin Test Method.kt")
                d.selectedMethods.mapNotNull {
                    it.toKotlinMemberInfo()?.member?.name
                }.forEach {
                    val properties = Properties()
                    properties.setProperty(FileTemplate.ATTRIBUTE_NAME, it)
                    body.append(methodTemplate.getText(properties))
                }
            }

            props.setProperty("BODY", body.toString())

            return FileTemplateUtil.createFromTemplate(
                classTemplate,
                d.className,
                props,
                d.targetDirectory
            ) as PsiFile
        }
        return null
    }
}