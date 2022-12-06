package eu.livesport.kotlintest

import com.intellij.codeInsight.CodeInsightUtil
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.childrenOfType
import com.intellij.testIntegration.createTest.CreateTestDialog
import com.intellij.testIntegration.createTest.TestGenerator
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.jetbrains.kotlin.idea.refactoring.memberInfo.toKotlinMemberInfo
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.IOException
import java.util.*

class KotlinTestGenerator : TestGenerator {

    override fun toString(): String = KotlinLanguage.INSTANCE.displayName

    override fun generateTest(project: Project, d: CreateTestDialog): PsiElement? {
        val applicationManager = ApplicationManager.getApplication()

        return PostprocessReformattingAspect.getInstance(project).postponeFormattingInside(Computable {
            applicationManager.runWriteAction(Computable<PsiFile?> {
                val file = try {
                    val targetDirectory = getFixedTargetDirectory(d, project) ?: d.targetDirectory

                    generateTestFile(project, d, targetDirectory)
                } catch (e: IncorrectOperationException) {
                    applicationManager.invokeLater { showErrorMessage(d, project) }
                    null
                }
                catch (e: IOException) {
                    applicationManager.invokeLater { showErrorMessage(d, project) }
                    null
                }

                file?.apply {
                    // without this the file is created but the caret stays in the original file
                    CodeInsightUtil.positionCursor(project, this, this)

                    // Find annotations on the functions from templates and shorten references
                    val classBody = this.childrenOfType<KtClass>().first().childrenOfType<KtClassBody>().first()
                    classBody.childrenOfType<KtNamedFunction>().forEach {
                        ShortenReferences.DEFAULT.process(it.annotationEntries.first())
                    }
                }
                file
            })
        })
    }

    @Suppress("DialogTitleCapitalization")
    private fun showErrorMessage(d: CreateTestDialog, project: Project) {
        val message = Bundle.message("intention.error.cannot.create.class.message", d.className)
        val title = Bundle.message("intention.error.cannot.create.class.title")
        Messages.showErrorDialog(
            project,
            message,
            title,
        )
    }

    private fun getFixedTargetDirectory(d: CreateTestDialog, project: Project): PsiDirectory? {
        val fullyQualifiedName = d.targetClass.qualifiedName ?: return null
        val targetNamespace = fullyQualifiedName.split(".").dropLast(1)

        val srcModule = ModuleUtilCore.findModuleForPsiElement(d.targetClass) ?: return null
        val modules: MutableSet<com.intellij.openapi.module.Module> = mutableSetOf()
        ModuleUtilCore.collectModulesDependsOn(srcModule, modules)

        val name = srcModule.name.replace("main", "androidMain").split(".")
        val sourceNamespace = name.dropLast(1).joinToString(".")
        val sourceModuleName = name.last().replace("Main", "Test")

        val targetCandidate = modules
            .filter { it.name.contains("test", true) || !it.name.startsWith(sourceNamespace) }
            .find { it.name.endsWith(sourceModuleName) } ?: return null

        var targetCandidateRoot = ModuleRootManager.getInstance(targetCandidate).contentRoots
            .firstOrNull { it.name == sourceModuleName } ?: return null

        targetCandidateRoot = targetCandidateRoot.findOrCreateChildData(this, "kotlin")
        targetNamespace.forEach {
            targetCandidateRoot = targetCandidateRoot.findOrCreateChildData(this, it)
        }
        return targetCandidateRoot.toPsiDirectory(project)
    }

    private fun generateTestFile(project: Project, d: CreateTestDialog, targetDirectory: PsiDirectory): PsiFile? {

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
                targetDirectory,
            ) as PsiFile
        }
        return null
    }
}