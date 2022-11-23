package eu.livesport.kotlintest.action

import com.intellij.testIntegration.TestFramework
import com.intellij.testIntegration.TestIntegrationUtils
import eu.livesport.kotlintest.KotlinTestFramework
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClassOrObject

class KotlinTearDown : KotlinGenerateTestSupportActionBaseWithFixedFrameworkResolving(TestIntegrationUtils.MethodKind.TEAR_DOWN) {
    override fun isApplicableTo(framework: TestFramework, targetClass: KtClassOrObject) =
        KotlinTestFramework.isTheSame(framework) && framework.findTearDownMethod(targetClass.toLightClass()!!) == null
}