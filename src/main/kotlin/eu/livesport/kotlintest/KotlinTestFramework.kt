package eu.livesport.kotlintest

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.intention.AddAnnotationFix
import com.intellij.execution.junit.JUnitUtil
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.testIntegration.JavaTestFramework
import com.intellij.testIntegration.TestFramework
import eu.livesport.kotlintest.utils.isUnderTestSources
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.base.util.module
import org.jetbrains.kotlin.psi.KtNamedFunction
import javax.swing.Icon

class KotlinTestFramework : JavaTestFramework() {

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

    override fun isTestClass(clazz: PsiClass?, canBePotential: Boolean): Boolean {
        return clazz is PsiElement && isPotentialTestClass(clazz)
    }

    override fun getName(): String = "Kotlin test"
    override fun getLanguage(): Language = KotlinLanguage.INSTANCE
    override fun getMarkerClassFQName(): String = TEST_ANNOTATION_NAME
    override fun isTestMethod(element: PsiElement?): Boolean {
        return testMethod(element)
    }

    override fun getIcon(): Icon = IconLoader.getIcon("/kotlin-test-icon16.png", KotlinTestFramework::class.java)

    override fun getLibraryPath(): String? = null

    override fun findSetUpMethod(clazz: PsiClass): PsiMethod? {
        return clazz.methods.filterNotNull().find {
            AnnotationUtil.isAnnotated(it, BEFORE_ANNOTATION_NAME, 0)
        }
    }

    override fun findTearDownMethod(clazz: PsiClass): PsiMethod? {
        return clazz.methods.filterNotNull().find {
            AnnotationUtil.isAnnotated(it, AFTER_ANNOTATION_NAME, 0)
        }
    }

    override fun isFrameworkAvailable(clazz: PsiElement): Boolean = clazz.module?.let { isLibraryAttached(it) } ?: false

    override fun findOrCreateSetUpMethod(element: PsiElement): PsiElement? {
        if (element is PsiClass) {
            return findOrCreateSetUpMethod(element)
        }
        return null
    }

    @Suppress("DialogTitleCapitalization")
    override fun findOrCreateSetUpMethod(
        clazz: PsiClass,
    ): PsiMethod {
        var method = findSetUpMethod(clazz)
        if (method != null) return method
        val manager = clazz.manager
        val factory = JavaPsiFacade.getInstance(manager.project).elementFactory
        method = createSetUpPatternMethod(factory)
        val existingMethod = clazz.findMethodBySignature(method, false)
        if (existingMethod != null) {
            if (AnnotationUtil.isAnnotated(existingMethod, BEFORE_ANNOTATION_NAME, 0)) return existingMethod
            val exit =
                if (ApplicationManager.getApplication().isUnitTestMode) Messages.OK else Messages.showOkCancelDialog(
                    Bundle.message("create.setup.dialog.message", "@BeforeTest"),
                    Bundle.message("create.setup.dialog.title"),
                    Bundle.message("create.setup.dialog.ok"),
                    Bundle.message("create.setup.dialog.cancel"),
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

    private fun testMethod(element: PsiElement?): Boolean {
        return when (element) {
            is KtNamedFunction -> element.annotationEntries.any { it.shortName?.identifier == "Test" || it.shortName?.identifier == TEST_ANNOTATION_NAME }
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

    companion object {
        fun isTheSame(framework: TestFramework) = framework is KotlinTestFramework
    }
}