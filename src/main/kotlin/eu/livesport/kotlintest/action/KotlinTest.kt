package eu.livesport.kotlintest.action

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestFramework
import com.intellij.testIntegration.TestIntegrationUtils
import eu.livesport.kotlintest.utils.correctK2KIssue
import org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateTestSupportActionBase
import org.jetbrains.kotlin.psi.KtClassOrObject

class KotlinTest : KotlinGenerateTestSupportActionBase(TestIntegrationUtils.MethodKind.TEST) {
    override fun isApplicableTo(framework: TestFramework, targetClass: KtClassOrObject) = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        super.invoke(project, editor, file)
        // TODO only first method will be generated properly, because JUnit then marks itself as valid framework :-(
        correctK2KIssue(project, editor)
    }
}