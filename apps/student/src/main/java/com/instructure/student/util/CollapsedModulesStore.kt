package com.instructure.student.util

import com.instructure.canvasapi2.models.CanvasContext

object CollapsedModulesStore {

    fun getCollapsedModuleIds(canvasContext: CanvasContext): Set<Long> {
        val keyName = getKeyName(canvasContext)
        return StudentPrefs.getString(keyName)
                .orEmpty()
                .split('|')
                .mapNotNull { it.toLongOrNull() }
                .toSet()
    }

    fun setCollapsedModuleIds(canvasContext: CanvasContext, moduleIds: Set<Long>) {
        val keyName = getKeyName(canvasContext)
        val joined = moduleIds.joinToString("|")
        StudentPrefs.putString(keyName, joined)
    }

    fun markModuleCollapsed(canvasContext: CanvasContext, moduleId: Long, isCollapsed: Boolean) {
        val collapsedIds = getCollapsedModuleIds(canvasContext).let {
            if (isCollapsed) it + moduleId else it - moduleId
        }
        setCollapsedModuleIds(canvasContext, collapsedIds)
    }

    private fun getKeyName(canvasContext: CanvasContext) = "collapsed_modules_${canvasContext.contextId}"
}