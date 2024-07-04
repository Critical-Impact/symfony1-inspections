package org.nexvia.symfonyinspections.scope

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.jetbrains.php.lang.psi.elements.PhpClass
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.impl.YAMLHashImpl

class PhpControllerDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        if (sourceElement == null) {
            return null
        }

        val sourceParent = sourceElement.parent as? PhpClass ?: return null

        // Check if the PHP class is in a folder called "actions"
        val virtualFile = sourceParent.containingFile.virtualFile
        if (!virtualFile.path.contains("/actions/")) {
            return null
        }

        val targets = mutableListOf<PsiElement>()

        // Locate the "routing.yml" file
        val routingFile = virtualFile.parent.parent.parent.parent.findChild("config")?.findChild("routing.yml")
        val project = sourceParent.project

        if (routingFile != null && routingFile.exists()) {
            val psiFile = PsiManager.getInstance(project).findFile(routingFile) as? YAMLFile ?: return null

            // Find a matching entry in "routing.yml"
            val className = sourceParent.name
            val targetKey = findMatchingEntry(psiFile, className)
            if (targetKey != null) {
                targets.add(targetKey)
            }
        }

        val actionName = sourceParent.name.replace("Action", "").decapitalize()
        val templateFile = virtualFile.parent.parent.findChild("templates")?.findChild("${actionName}Success.php")
        if (templateFile != null && templateFile.exists()) {
            targets.add(PsiManager.getInstance(project).findFile(templateFile) as PsiElement)
        }

        return if (targets.isNotEmpty()) targets.toTypedArray() else null
    }

    private fun findMatchingEntry(yamlFile: YAMLFile, className: String): PsiElement? {
        val root = yamlFile.documents.firstOrNull()?.topLevelValue ?: return null
        val entries = root.children

        for (entry in entries) {
            val value = entry as? YAMLKeyValue ?: continue
            val yamlMapping = entry.value as? YAMLMapping ?: continue
            val param = yamlMapping.getKeyValueByKey("param") ?: continue
            val paramHash = param.value as? YAMLHashImpl ?: continue
            var action = paramHash.getKeyValueByKey("action")?.value?.text ?: continue
            if (action == className.replace("Action", "").decapitalize()) {
                return entry
            }
        }
        return null
    }

    private fun YAMLKeyValue.findKeyValueByKey(key: String): YAMLKeyValue? {
        return (value as? YAMLKeyValue)?.findKeyValueByKey(key)
    }

    override fun getActionText(context: DataContext): String? {
        return "Go to scope declaration"
    }
}