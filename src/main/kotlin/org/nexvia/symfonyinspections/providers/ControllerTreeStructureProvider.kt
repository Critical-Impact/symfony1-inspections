package org.nexvia.symfonyinspections.providers

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import org.nexvia.symfonyinspections.filefinder.ControllerAssociations
import org.nexvia.symfonyinspections.filefinder.ControllerFileInfo
import org.nexvia.symfonyinspections.filenodes.TemplateFileNode
import org.nexvia.symfonyinspections.icons.Symfony1Icons
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
                val templateFiles: ControllerFileInfo = ControllerAssociations.findTemplateFiles(psiFile)
                if(templateFiles.files.isNotEmpty()) {
                    val newChildren: MutableList<AbstractTreeNode<*>> = ArrayList(children)
                    for (templateFile in templateFiles.files) {
                        newChildren.add(TemplateFileNode(psiFile.project, templateFile, settings))
                    }
                    return newChildren
                }
                else if(templateFiles.isAjax)
                {
                    parent.presentation.setIcon(Symfony1Icons.AjaxIcon)
                }
            }
        }
        return children
    }

}