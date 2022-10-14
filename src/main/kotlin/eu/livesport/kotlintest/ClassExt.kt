package eu.livesport.kotlintest

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass

fun PsiClass.isUnderTestSources(): Boolean {
    val psiFile = this.containingFile
    val vFile = psiFile.virtualFile ?: return false
    return ProjectRootManager.getInstance(this.project).fileIndex.isInTestSourceContent(vFile)
}