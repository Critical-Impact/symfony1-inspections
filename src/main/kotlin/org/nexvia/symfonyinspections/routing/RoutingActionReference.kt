package org.nexvia.symfonyinspections.routing

import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException
import java.io.File

class RoutingActionReference(element: PsiElement, private val module: String, private val action: String) :
    PsiReferenceBase<PsiElement>(element) {

    override fun resolve(): PsiElement? {
        val routingYmlFile = myElement!!.containingFile ?: return null

        val controllerFilePath = String.format("../modules/%s/actions/%sAction.class.php", module, action)
        val controllerFile = File(routingYmlFile.virtualFile.parent.path, controllerFilePath)

        if (!controllerFile.exists()) {
            return null
        }


        val controllerPsiFile: PsiFile? = routingYmlFile.parent?.parent?.findSubdirectory("modules")?.findSubdirectory(module)?.findSubdirectory("actions")?.findFile(String.format("%sAction.class.php", action));

        if (controllerPsiFile == null) {
            return null;
        }

        return controllerPsiFile
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        return myElement!!
    }

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement {
        return myElement!!
    }

    override fun getVariants(): Array<Any?> {
        return arrayOfNulls(0)
    }
}