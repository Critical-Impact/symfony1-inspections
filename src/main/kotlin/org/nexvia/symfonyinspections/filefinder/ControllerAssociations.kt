package org.nexvia.symfonyinspections.filefinder

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import org.nexvia.symfonyinspections.files.PartialFile
import org.nexvia.symfonyinspections.files.SuccessFile


object ControllerAssociations {
    fun findTemplateFiles(file: PsiFile): List<PsiFile> {
        val templateFiles: MutableList<PsiFile> = ArrayList()
        if (file is PhpFile) {
            if(isActionFile(file))
            {
                val templateFile = findTemplateForAction(file)
                if(templateFile != null)
                {
                    templateFiles.add(SuccessFile(templateFile.viewProvider, templateFile.language))
                }
                file.accept(object : PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        super.visitElement(element)
                        if (element is MethodReference) {
                            val methodRef = element
                            if (isRenderMethod(methodRef)) {
                                val partialFile = getTemplateFileFromMethod(methodRef, file)
                                if (partialFile != null) {
                                    templateFiles.add(partialFile)
                                }
                            }
                        }
                    }
                })
            }
        }
        return templateFiles
    }

    fun isActionFile(psiFile: PsiFile): Boolean {
        // Example logic to determine if a file is an action file
        return psiFile.name.endsWith("Action.class.php")
    }
    fun findTemplateForAction(actionFile: PsiFile): PsiFile? {
        val templateName = actionFile.name.replace("Action.class.php", "") + "Success.php"
        val templateFile = actionFile.virtualFile.parent.findFileByRelativePath(
            "../templates/$templateName"
        )
        return if (templateFile != null) PsiManager.getInstance(actionFile.project).findFile(templateFile) else null
    }

    private fun isRenderMethod(methodRef: MethodReference): Boolean {
        val methodName = methodRef.name!!
        return "getPartial" == methodName || "renderAjax" == methodName // Add other method names if necessary
    }

    private fun getTemplateFileFromMethod(methodRef: MethodReference, actionFile: PsiFile): PsiFile? {
        val parameters = methodRef.parameters
        if (parameters.isNotEmpty() && parameters[0] is StringLiteralExpression) {
            val templatePathExpr = parameters[0] as StringLiteralExpression
            var templatePath = templatePathExpr.contents
            val explodedPath = templatePath.split("/").toMutableList();
            var partialPath = explodedPath[explodedPath.count() - 2];
            partialPath = "templates/_${partialPath}"
            explodedPath[explodedPath.count() - 2] = partialPath
            templatePath = explodedPath.reduce { result, nr -> "$result/$nr" }

            val templateFile = actionFile.virtualFile.parent.findFileByRelativePath(
                "../../$templatePath.php"
            )
            val foundFile = if (templateFile != null) PsiManager.getInstance(actionFile.project).findFile(templateFile) else null
            if (foundFile != null) {
                return PartialFile(foundFile.viewProvider, foundFile.language)
            }
        }
        return null
    }
}