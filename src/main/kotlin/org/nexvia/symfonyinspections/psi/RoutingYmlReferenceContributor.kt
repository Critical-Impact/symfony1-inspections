package org.nexvia.symfonyinspections.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.nexvia.symfonyinspections.routing.RoutingActionReference
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl
import java.io.File


class RoutingYmlReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLKeyValue::class.java).withLanguage(YAMLLanguage.INSTANCE),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element !is YAMLKeyValue) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    if ("action" != element.keyText) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    if(element !is YAMLKeyValueImpl)
                    {
                        return PsiReference.EMPTY_ARRAY
                    }



                    val parent = element.getParent() as? YAMLMapping ?: return PsiReference.EMPTY_ARRAY

                    val moduleKeyValue = parent.getKeyValueByKey("module")
                        ?: return PsiReference.EMPTY_ARRAY

                    val module = moduleKeyValue.valueText
                    val action: String = element.valueText
                    val originalElement = element.value?.navigationElement
                    if (module == null || action == null) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val keyReferences = createReferencesForKey(element, module, action)
                    val valueReferences = createReferencesForValue(element, module, action)
                    return valueReferences + keyReferences
                }
            }
        )
    }

    fun getPsiFileFromPath(project: Project, path: String): PsiFile? {
        val virtualFile: VirtualFile? = LocalFileSystem.getInstance().findFileByPath(path)
        return virtualFile?.let { PsiManager.getInstance(project).findFile(it) }
    }

    private fun createReferencesForKey(keyValue: YAMLKeyValue, module: String, action: String): Array<PsiReference> {
        val keyElement = keyValue.key ?: return PsiReference.EMPTY_ARRAY
        return arrayOf(
            object : PsiReferenceBase<YAMLKeyValue>(keyValue) {
                override fun resolve(): PsiElement? {
                    val routingYmlFile = myElement!!.containingFile ?: return null

                    val controllerFilePath = String.format("../modules/%s/actions/%sAction.class.php", module, action)
                    val controllerFile = File(routingYmlFile.virtualFile.parent.path, controllerFilePath)

                    if (!controllerFile.exists()) {
                        return null
                    }

                    val psiFileFromPath = getPsiFileFromPath(myElement.project, controllerFile.path)
                    return psiFileFromPath
                }

                override fun getVariants(): Array<Any> {
                    // Implement logic to provide completion variants for the key
                    return emptyArray()
                }
            }
        )
    }

    private fun createReferencesForValue(keyValue: YAMLKeyValue, module: String, action: String): Array<PsiReference> {
        val valueElement = keyValue.value as? YAMLScalar ?: return PsiReference.EMPTY_ARRAY
        return arrayOf(
            object : PsiReferenceBase<YAMLScalar>(valueElement) {
                override fun resolve(): PsiElement? {
                    val routingYmlFile = myElement!!.containingFile ?: return null

                    val controllerFilePath = String.format("../modules/%s/actions/%sAction.class.php", module, action)
                    val controllerFile = File(routingYmlFile.virtualFile.parent.path, controllerFilePath)

                    if (!controllerFile.exists()) {
                        return null
                    }

                    val psiFileFromPath = getPsiFileFromPath(myElement.project, controllerFile.path)
                    return psiFileFromPath
                }

                override fun getVariants(): Array<Any> {
                    val project: Project = myElement.project
                    val variants = mutableListOf<Any>()

                    val routingYmlFile = myElement.containingFile.originalFile ?: return emptyArray()

                    val actionsFilePath = String.format("../modules/%s/actions", module)
                    val actionsDirectory = File(routingYmlFile.virtualFile.parent.path, actionsFilePath)

                    if(!actionsDirectory.exists())
                    {
                        return emptyArray()
                    }

                    var virtualDir = LocalFileSystem.getInstance().findFileByPath(actionsDirectory.path)

                    virtualDir?.let { dir ->
                        val psiDirectory = PsiManager.getInstance(project).findDirectory(dir)
                        psiDirectory?.let {
                            val files = it.files.filter { file -> file.name.endsWith(".class.php") }
                            variants.addAll(files.map { file -> file.name.replace("Action.class.php", "") })
                        }
                    }

                    return variants.toTypedArray()
                }
            }
        )
    }
}