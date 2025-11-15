package com.valtecna.iadoc.license

import com.intellij.ui.LicensingFacade

object LicenseChecker {
    private const val PLUGIN_PRODUCT_CODE = "PVALDOC"

    fun isPro(): Boolean {
        val facade = LicensingFacade.getInstance() ?: return false
        val confirmationStamp = facade.getConfirmationStamp(PLUGIN_PRODUCT_CODE)
        return confirmationStamp != null
    }

    fun isFree(): Boolean = !isPro()
}
