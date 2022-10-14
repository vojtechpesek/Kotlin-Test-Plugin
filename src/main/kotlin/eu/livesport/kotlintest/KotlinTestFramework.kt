package eu.livesport.kotlintest

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.intention.AddAnnotationFix
import com.intellij.execution.JUnitBundle
import com.intellij.execution.junit.JUnitUtil
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.testIntegration.TestFramework
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtNamedFunction
import javax.swing.Icon

class KotlinTestFramework : TestFramework {

    override fun getDefaultSuperClass(): String = ""

    /**
     * Returns true if the given element could be a test class.
     * Used to quickly filter out elements that are immediately clear are
     * not test classes - such as elements in src/main or elements that are not class elements.
     */
    override fun isPotentialTestClass(clazz: PsiElement): Boolean {
        return clazz is PsiClass && clazz.isUnderTestSources()
    }

    override fun isTestClass(clazz: PsiElement): Boolean {
        return isPotentialTestClass(clazz)
    }

    override fun getName(): String = "Kotlin test"
    override fun getLanguage(): Language = KotlinLanguage.INSTANCE
    override fun getIcon(): Icon = IconLoader.getIcon("/icon16.png", KotlinTestFramework::class.java)

    override fun getLibraryPath(): String? = null

    override fun findSetUpMethod(element: PsiElement): PsiElement? {
        if (element is PsiClass) {
            return element.methods.filterNotNull().find {
                AnnotationUtil.isAnnotated(it, BEFORE_ANNOTATION_NAME, 0)
            }
        }
        return null
    }

    override fun findTearDownMethod(element: PsiElement): PsiElement? {
        if (element is PsiClass) {
            return element.methods.filterNotNull().find {
                AnnotationUtil.isAnnotated(it, AFTER_ANNOTATION_NAME, 0)
            }
        }
        return null
    }

    override fun findOrCreateSetUpMethod(element: PsiElement): PsiElement? {
        if (element is PsiClass) {
            return findOrCreateSetUpMethod(element)
        }
        return null
    }

    private fun findOrCreateSetUpMethod(
        clazz: PsiClass,
    ): PsiMethod? {
        var method = findSetUpMethod(clazz)
        if (method != null) return method as? PsiMethod
        val manager = clazz.manager
        val factory = JavaPsiFacade.getInstance(manager.project).elementFactory
        method = createSetUpPatternMethod(factory)
        val existingMethod = clazz.findMethodBySignature(method, false)
        if (existingMethod != null) {
            if (AnnotationUtil.isAnnotated(existingMethod, BEFORE_ANNOTATION_NAME, 0)) return existingMethod
            val exit =
                if (ApplicationManager.getApplication().isUnitTestMode) Messages.OK else Messages.showOkCancelDialog(
                    JUnitBundle.message("create.setup.dialog.message", "@BeforeTest"),
                    JUnitBundle.message("create.setup.dialog.title"),
                    Messages.getWarningIcon()
                )
            if (exit == Messages.OK) {
                AddAnnotationFix(BEFORE_ANNOTATION_NAME, existingMethod).invoke(
                    existingMethod.project,
                    null,
                    existingMethod.containingFile
                )
                return existingMethod
            }
        }
        val testMethod = JUnitUtil.findFirstTestMethod(clazz)
        method = if (testMethod != null) {
            clazz.addBefore(method, testMethod) as PsiMethod
        } else {
            clazz.add(method) as PsiMethod
        }
        JavaCodeStyleManager.getInstance(manager.project).shortenClassReferences(method)
        return method
    }

    private fun createSetUpPatternMethod(factory: JVMElementFactory): PsiMethod {
        val template = FileTemplateManager.getDefaultInstance().getCodeTemplate(
            setUpMethodFileTemplateDescriptor.fileName
        )
        val templateText = StringUtil.replace(StringUtil.replace(template.text, "\${BODY}\n", ""), "\${NAME}", "setUp")
        return factory.createMethodFromText(templateText, null)
    }

    override fun isTestMethod(element: PsiElement?): Boolean = testMethod(element)

    override fun isTestMethod(element: PsiElement?, checkAbstract: Boolean): Boolean = testMethod(element)

    private fun testMethod(element: PsiElement?): Boolean {
        return when (element) {
            is KtNamedFunction -> element.annotationEntries.any { it.shortName?.identifier == "Test" || it.shortName?.identifier == "kotlin.test.Test" }
            else -> false
        }
    }

    override fun getSetUpMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Kotlin Test After.kt")
    override fun getTearDownMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Kotlin Test Before.kt")

    override fun getTestMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Kotlin Test Method.kt")

    override fun isIgnoredMethod(element: PsiElement?): Boolean {
        val testMethod = if (element is PsiMethod) JUnitUtil.getTestMethod(element) else null
        return testMethod != null && AnnotationUtil.isAnnotated(testMethod, IGNORE_ANNOTATION_NAME, 0)
    }

    override fun isLibraryAttached(module: Module): Boolean {
        return true
    }
}