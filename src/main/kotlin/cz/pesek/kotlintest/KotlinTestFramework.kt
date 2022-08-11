package cz.pesek.kotlintest

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.lang.Language
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testIntegration.TestFramework
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import javax.swing.Icon

class KotlinTestFramework : TestFramework {

    override fun getDefaultSuperClass(): String = ""

    private fun isUnderTestSources(clazz: PsiClass): Boolean {
        val psiFile = clazz.containingFile
        val vFile = psiFile.virtualFile ?: return false
        return ProjectRootManager.getInstance(clazz.project).fileIndex.isInTestSourceContent(vFile)
    }

    /**
     * Returns true if the given element could be a test class.
     * Used to quickly filter out elements that are immediately clear are
     * not test classes - such as elements in src/main or elements that are not class elements.
     */
    override fun isPotentialTestClass(clazz: PsiElement): Boolean {
        return clazz is PsiClass && isUnderTestSources(clazz)
    }

    override fun isTestClass(clazz: PsiElement): Boolean {
        return false
//      return when (clazz) {
//         is KtUltraLightClass -> clazz.kotlinOrigin.isSpec()
//         is KtLightClass -> clazz.kotlinOrigin?.isSpec() ?: false
//         is KtClass -> clazz.isSpec()
//         else -> false
//      }
    }

    override fun getName(): String = "Kotlin test"
    override fun getLanguage(): Language = KotlinLanguage.INSTANCE
    override fun getIcon(): Icon = IconLoader.getIcon("/icon16.png", KotlinTestFramework::class.java)

    override fun getLibraryPath(): String? = null

    override fun findOrCreateSetUpMethod(clazz: PsiElement): PsiElement? = null

    // kotest does not use method as tests
    override fun isIgnoredMethod(element: PsiElement?): Boolean = false

    override fun findSetUpMethod(clazz: PsiElement): PsiElement? = null

    override fun findTearDownMethod(clazz: PsiElement): PsiElement? = null


    // kotest does not use method as tests
    override fun isTestMethod(element: PsiElement?): Boolean = false

    // kotest does not use method as tests
    override fun isTestMethod(element: PsiElement?, checkAbstract: Boolean): Boolean = false

    override fun getSetUpMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Kotlin Test After.kt")
    override fun getTearDownMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Kotlin Test Before.kt")

    override fun getTestMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Kotlin Test Method.kt")

    override fun isLibraryAttached(module: Module): Boolean {
        val scope = GlobalSearchScope.allScope(module.project)
        val c = JavaPsiFacade.getInstance(module.project).findClass(defaultSuperClass, scope)
        return c != null
    }
}