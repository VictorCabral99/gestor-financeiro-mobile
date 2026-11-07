package com.victorcabral.gestorfinanceiro.notification.sim

import com.victorcabral.gestorfinanceiro.notification.parser.KnownBankPackages

/**
 * Sample bank push payloads used by the debug simulator.
 * Texts mirror the unit-test fixtures so parse → dedupe → pending matches CI.
 */
data class SimulatedBankNotification(
    val id: String,
    val label: String,
    val bankPackage: String,
    val title: String,
    val text: String,
    val bigText: String? = null,
)

object SimulatedBankNotifications {
    val samples: List<SimulatedBankNotification> = listOf(
        SimulatedBankNotification(
            id = "nu_debit",
            label = "Nubank — compra no débito",
            bankPackage = KnownBankPackages.NUBANK,
            title = "Compra no débito",
            text = "Você pagou R$ 42,90 em Padaria Sol",
        ),
        SimulatedBankNotification(
            id = "nu_credit",
            label = "Nubank — compra no crédito",
            bankPackage = KnownBankPackages.NUBANK,
            title = "Compra no crédito",
            text = "Compra de R$ 189,00 em Posto Ipiranga",
        ),
        SimulatedBankNotification(
            id = "nu_pix",
            label = "Nubank — Pix enviado",
            bankPackage = KnownBankPackages.NUBANK,
            title = "Pix enviado",
            text = "Pix de R$ 15,00 enviado para Joao Silva",
        ),
        SimulatedBankNotification(
            id = "nu_boleto",
            label = "Nubank — boleto",
            bankPackage = KnownBankPackages.NUBANK,
            title = "Boleto pago",
            text = "Você pagou um boleto de R$ 250,40",
        ),
        SimulatedBankNotification(
            id = "nu_security_ignore",
            label = "Nubank — código (deve ignorar)",
            bankPackage = KnownBankPackages.NUBANK,
            title = "Nubank",
            text = "Seu código de segurança é 123456",
        ),
        SimulatedBankNotification(
            id = "san_compra",
            label = "Santander — compra aprovada",
            bankPackage = KnownBankPackages.SANTANDER,
            title = "Compra aprovada",
            text = "Compra de R$ 120,55 no estabelecimento Mercado Central",
        ),
        SimulatedBankNotification(
            id = "san_pix",
            label = "Santander — Pix enviado",
            bankPackage = KnownBankPackages.SANTANDER,
            title = "Pix enviado",
            text = "Você enviou um Pix de R$ 80,00 para Maria Souza",
        ),
        SimulatedBankNotification(
            id = "san_review",
            label = "Santander — texto cru (revisar)",
            bankPackage = KnownBankPackages.SANTANDER,
            title = "Aviso",
            text = "Movimentação na sua conta. Abra o app.",
        ),
        SimulatedBankNotification(
            id = "san_declined_ignore",
            label = "Santander — compra recusada (ignorar)",
            bankPackage = KnownBankPackages.SANTANDER,
            title = "Compra recusada",
            text = "Compra de R$ 10,00 em Loja X foi recusada",
        ),
    )
}
