package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Package names observed for Brazilian bank apps.
 * Confirm on device with `adb shell pm list packages | grep -iE 'nu|santander'`.
 */
object KnownBankPackages {
    const val NUBANK = "com.nu.production"
    const val SANTANDER = "com.santander.app"

    /** Extra package aliases seen on some devices / older builds. */
    val aliases: Map<String, String> = mapOf(
        // Some sideload/debug builds report the same parser family.
        "com.nu.production.debug" to NUBANK,
    )

    val all: Set<String> = setOf(NUBANK, SANTANDER) + aliases.keys

    fun canonical(packageName: String): String = aliases[packageName] ?: packageName
}
