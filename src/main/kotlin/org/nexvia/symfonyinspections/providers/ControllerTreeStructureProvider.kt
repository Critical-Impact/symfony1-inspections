package org.nexvia.symfonyinspections.providers

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.nexvia.symfonyinspections.filefinder.ControllerAssociations
import org.nexvia.symfonyinspections.filenodes.TemplateFileNode
import java.util.*


class ControllerTreeStructureProvider : TreeStructureProvider {
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): MutableCollection<AbstractTreeNode<*>> {
        if (parent is PsiFileNode) {
            val psiFile = parent.value
            if (psiFile != null && ControllerAssociations.isActionFile(psiFile)) {
                val templateFiles: List<PsiFile> = ControllerAssociations.findTemplateFiles(psiFile)
                if(templateFiles.isNotEmpty()) {
                    val newChildren: MutableList<AbstractTreeNode<*>> = ArrayList(children)
                    for (templateFile in templateFiles) {
                        newChildren.add(TemplateFileNode(psiFile.project, templateFile, settings))
                    }
                    return newChildren
                }
            }
        }
        return children
    }

}