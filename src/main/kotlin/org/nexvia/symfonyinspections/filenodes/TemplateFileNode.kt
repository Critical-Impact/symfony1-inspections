package org.nexvia.symfonyinspections.filenodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FileStatus
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import java.awt.Color


class TemplateFileNode(
    project: Project?,
    psiFile: PsiFile?,
    viewSettings: ViewSettings?
) :
    PsiFileNode(project, psiFile!!, viewSettings) {
    override fun getHighlightColor(): Color {
        return JBColor.BLUE
    }

    override fun getFileStatusColor(status: FileStatus?): Color? {
        return JBColor.BLUE
    }
}