package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Package names observed for Brazilian bank apps.
 * Confirm on device with `adb shell pm list packages | grep -i nu` etc.
 */
object KnownBankPackages {
    const val NUBANK = "com.nu.production"
    const val SANTANDER = "com.santander.app"

    val all: Set<String> = setOf(NUBANK, SANTANDER)
}
