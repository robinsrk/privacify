package dev.robin.privacify.core.provider

import dev.robin.privacify.core.PermissionAutomationController
import dev.robin.privacify.free.FreePermissionAutomationController

object PermissionAutomationControllerProvider {

    fun provide(): PermissionAutomationController {
        return try {

            val clazz = Class.forName(
                "dev.robin.privacify.pro.RealPermissionAutomationController"
            )

            clazz.getDeclaredConstructor().newInstance() as PermissionAutomationController

        } catch (e: Exception) {

            FreePermissionAutomationController()

        }
    }

}
