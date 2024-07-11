package org.nexvia.symfonyinspections.filefinder

import com.intellij.psi.*
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl
import org.nexvia.symfonyinspections.cache.Cache
import org.nexvia.symfonyinspections.cache.RoutingCacheEntry
import org.nexvia.symfonyinspections.files.PartialFile
import org.nexvia.symfonyinspections.files.SuccessFile
import org.nexvia.symfonyinspections.files.YamlFile

class ControllerFileInfo(val files:List<PsiFile>, val isAjax:Boolean)
{

}

object ControllerAssociations {
    fun findTemplateFiles(file: PsiFile): ControllerFileInfo {
        val templateFiles: MutableList<PsiFile> = ArrayList()
        var isAjax = false
        if (file is PhpFile) {
            if(isActionFile(file)) {
                val actionName = getActionName(file)
                val templateFile = findTemplateForAction(file)
                if (templateFile != null) {
                    templateFiles.add(SuccessFile(templateFile.viewProvider, templateFile.language))
                }
                if(file.fileDocument.text.contains("getPartial")) {
                    file.accept(object : PsiRecursiveElementVisitor() {
                        override fun visitElement(element: PsiElement) {
                            super.visitElement(element)
                            if (element is MethodReference) {
                                val methodRef = element
                                if (isRenderMethod(methodRef)) {
                                    val partialFile = getTemplateFileFromMethod(methodRef, file)
                                    if (partialFile != null) {
                                        templateFiles.add(partialFile)
                                    } else {
                                        isAjax = true
                                    }
                                }
                            }
                        }
                    })
                }

                val ymlForAction = findRoutingYmlForAction(file)
                if(ymlForAction != null) {
                    var moduleInfo = getModuleForAction(file)
                    if(moduleInfo != null) {
                        var routingCache = Cache.getRoutingCache(ymlForAction)
                        var results = routingCache.filter { routingCacheEntry: RoutingCacheEntry -> routingCacheEntry.action == actionName && routingCacheEntry.module == moduleInfo.module }
                        for (route: RoutingCacheEntry in results) {
                            templateFiles.add(YamlFile(ymlForAction.viewProvider, ymlForAction.language, route.moduleElement))
                        }
                    }
                }

            }
        }
        return ControllerFileInfo(templateFiles, isAjax)
    }

    fun getModuleForAction(file: PsiFile): ModuleInfo? {
        var module = extractModuleName(file.virtualFile.path, file.project.basePath.toString())
        return module
    }

    fun extractModuleName(filePath: String, rootPath: String): ModuleInfo? {
        // Ensure the filePath starts with the rootPath
        if (!filePath.startsWith(rootPath)) return null

        // Define the apps directory as a constant
        val appsDirectory = "apps"
        val modulesDirectory = "modules"

        // Remove the rootPath from the filePath
        val relativePath = filePath.removePrefix(rootPath)

        // Split the relativePath into segments
        val pathSegments = relativePath.split("/").filter { it.isNotEmpty() }

        // Check if the apps directory exists in the segments and get its index
        val appsIndex = pathSegments.indexOf(appsDirectory)
        if (appsIndex == -1 || appsIndex + 1 >= pathSegments.size) return null
        var app = pathSegments[appsIndex + 1]


        val modulesIndex = pathSegments.indexOf(modulesDirectory)
        if (modulesIndex == -1 || modulesIndex + 1 >= pathSegments.size) return null
        var module = pathSegments[modulesIndex + 1]

        // The module name should be the segment after the apps directory and the module's name
        return ModuleInfo(module, app)
    }

    fun getActionName(psiFile: PsiFile): String
    {
        return psiFile.name.replace("Action.class.php", "")
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
    fun findRoutingYmlForAction(actionFile: PsiFile): PsiFile? {
        val routingFile = actionFile.virtualFile.parent.findFileByRelativePath(
            "../../../config/routing.yml"
        )
        return if (routingFile != null) PsiManager.getInstance(actionFile.project).findFile(routingFile) else null
    }

    private fun isRenderMethod(methodRef: MethodReference): Boolean {
        val methodName = methodRef.name!!
        return "getPartial" == methodName || "renderAjax" == methodName  || "renderPartial" == methodName // Add other method names if necessary
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

class ModuleInfo(val module: String, val app: String )
{
}