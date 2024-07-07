package org.nexvia.symfonyinspections.files

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.util.Iconable.IconFlags
import com.intellij.openapi.util.LastComputedIconCache
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.ElementBase
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.IconManager
import com.intellij.ui.JBColor
import com.intellij.util.AstLoadingFilter
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.PhpFile
import org.jetbrains.yaml.YAMLFileType
import java.awt.Color
import javax.swing.Icon

class YamlFile(viewProvider: FileViewProvider, language: Language, private val navigationElement: PsiElement?) : PsiFileBase(viewProvider, language) {
    override fun getFileType(): FileType {
        return YAMLFileType.YML
    }

    override fun getNavigationElement(): PsiElement {
        if(navigationElement != null)
        {
            return navigationElement
        }
        return super.getNavigationElement()
    }
}
