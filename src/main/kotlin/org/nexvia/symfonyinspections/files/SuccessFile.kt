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
import com.intellij.ui.IconManager
import com.intellij.ui.JBColor
import com.intellij.util.AstLoadingFilter
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.PhpFile
import java.awt.Color
import javax.swing.Icon

class SuccessFile(viewProvider: FileViewProvider, language: Language) : PsiFileBase(viewProvider, language) {
    override fun getFileType(): FileType {
        return PhpFileType.INSTANCE
    }

    override fun getIcon(flags: Int): Icon? {
        try {
            return computeIcon(flags)
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: IndexNotReadyException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }

    private fun computeIcon(@IconFlags flags: Int): Icon? {
        val psiElement = this as PsiElement
        if (!psiElement.isValid) return null

        if (Registry.`is`("psi.deferIconLoading", true)) {
            var baseIcon = LastComputedIconCache.get(psiElement, flags)
            if (baseIcon == null) {
                baseIcon = AstLoadingFilter.disallowTreeLoading<Icon?, RuntimeException> {
                    computeBaseIcon(
                        flags
                    )
                }
            }
            if (baseIcon == null) {
                return null
            }

            return IconManager.getInstance().withIconBadge(PhpFileType.INSTANCE.icon, JBColor.GREEN)
        }

        return super.computeBaseIcon(flags)
    }
}
