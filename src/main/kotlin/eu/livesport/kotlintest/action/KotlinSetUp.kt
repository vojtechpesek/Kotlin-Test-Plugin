package eu.livesport.kotlintest.action

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestFramework
import com.intellij.testIntegration.TestIntegrationUtils
import eu.livesport.kotlintest.utils.correctK2KIssue
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateTestSupportActionBase
import org.jetbrains.kotlin.psi.KtClassOrObject

class KotlinSetUp : KotlinGenerateTestSupportActionBase(TestIntegrationUtils.MethodKind.SET_UP) {
    override fun isApplicableTo(framework: TestFramework, targetClass: KtClassOrObject) =
        framework.findSetUpMethod(targetClass.toLightClass()!!) == null

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        super.invoke(project, editor, file)
        correctK2KIssue(project, editor)
    }
}