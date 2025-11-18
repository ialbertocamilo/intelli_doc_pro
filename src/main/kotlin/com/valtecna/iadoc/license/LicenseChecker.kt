package com.valtecna.iadoc.license

import com.intellij.ui.LicensingFacade

object LicenseChecker {
    private const val PLUGIN_PRODUCT_CODE = "PVALDOC"
    private const val KEY_PREFIX = "key:"
    private const val STAMP_PREFIX = "stamp:"

    private const val DEVELOPMENT_MODE = true

    fun isLicensed(): Boolean? {
        if (DEVELOPMENT_MODE) {
            return true
        }

        val facade = LicensingFacade.getInstance() ?: return null

        val confirmationStamp = facade.getConfirmationStamp(PLUGIN_PRODUCT_CODE)
            ?: return false

        return when {
            confirmationStamp.startsWith(KEY_PREFIX) -> true
            confirmationStamp.startsWith(STAMP_PREFIX) -> true
            else -> false
        }
    }

    fun isPro(): Boolean = isLicensed() == true

    fun isFree(): Boolean = isLicensed() != true
}
