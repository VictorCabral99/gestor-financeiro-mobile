package com.victorcabral.gestorfinanceiro.data

import com.victorcabral.gestorfinanceiro.data.entity.Category

object DefaultCategories {
    /** Seed only root names; subcategories are optional user-created. */
    val rootNames: List<String> = listOf(
        "alimentação",
        "transporte",
        "moradia",
        "lazer",
        "outros",
    )

    val seedRoots: List<Category> = rootNames.map {
        Category(name = it, parentId = null, isDefault = true)
    }

    /** Optional starter subcategories keyed by root name. */
    val seedSubcategoriesByRoot: Map<String, List<String>> = mapOf(
        "alimentação" to listOf("mercado", "restaurante", "delivery"),
        "transporte" to listOf("combustível", "app de transporte"),
        "moradia" to listOf("aluguel", "contas"),
        "lazer" to listOf("streaming", "passeios"),
    )
}
