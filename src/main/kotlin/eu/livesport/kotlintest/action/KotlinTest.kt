package eu.livesport.kotlintest.action

import com.intellij.testIntegration.TestFramework
import com.intellij.testIntegration.TestIntegrationUtils
import eu.livesport.kotlintest.KotlinTestFramework
import org.jetbrains.kotlin.psi.KtClassOrObject

class KotlinTest : KotlinGenerateTestSupportActionBaseWithFixedFrameworkResolving(TestIntegrationUtils.MethodKind.TEST) {
    override fun isApplicableTo(framework: TestFramework, targetClass: KtClassOrObject): Boolean {
        return KotlinTestFramework.isTheSame(framework)
    }
}