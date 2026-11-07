package com.victorcabral.gestorfinanceiro.data

import com.victorcabral.gestorfinanceiro.data.entity.Category

object DefaultCategories {
    val seed: List<Category> = listOf(
        Category(name = "alimentação", isDefault = true),
        Category(name = "transporte", isDefault = true),
        Category(name = "moradia", isDefault = true),
        Category(name = "lazer", isDefault = true),
        Category(name = "outros", isDefault = true),
    )
}
