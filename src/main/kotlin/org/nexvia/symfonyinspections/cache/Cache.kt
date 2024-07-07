package org.nexvia.symfonyinspections.cache

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.nexvia.symfonyinspections.filefinder.ControllerAssociations.getModuleForAction
import org.nexvia.symfonyinspections.files.YamlFile
import java.util.Dictionary

class Cache {

    companion object {
        val routingCache: MutableMap<String, List<RoutingCacheEntry>> = mutableMapOf()
        private fun generateRoutingCache(routingFile: PsiFile): List<RoutingCacheEntry> {
            val routeCache = ArrayList<RoutingCacheEntry>()
            //var moduleInfo = getModuleForAction(routingFile)
            routingFile.accept(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    super.visitElement(element)

                    if (element is YAMLKeyValue) {
                        if ("action" == element.keyText) {
                            val parent = element.getParent() as? YAMLMapping ?: return

                            val moduleKeyValue = parent.getKeyValueByKey("module")
                                ?: return

                            val module = moduleKeyValue.valueText
                            val action: String = element.valueText
                            val originalElement = element.value?.navigationElement
                            routeCache.add(RoutingCacheEntry(module, action, moduleKeyValue))
                        }
                    }
                }
            })
            return routeCache
        }

        fun getRoutingCache(routingFile: PsiFile): List<RoutingCacheEntry> {
            if(!routingCache.containsKey(routingFile.virtualFile.path))
            {
                routingCache[routingFile.virtualFile.path] = generateRoutingCache(routingFile)
            }
            return routingCache[routingFile.virtualFile.path]!!
        }



        fun reset(routingFile: PsiFile)
        {
            routingCache.remove(routingFile.virtualFile.path)
        }
    }


}

class RoutingCacheEntry(val module: String, val action: String, val moduleElement: YAMLKeyValue)
{

}