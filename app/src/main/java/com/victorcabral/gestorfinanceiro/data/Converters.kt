package com.victorcabral.gestorfinanceiro.data

import androidx.room.TypeConverter
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType

class Converters {
    @TypeConverter
    fun fromPaymentSourceType(value: PaymentSourceType): String = value.name

    @TypeConverter
    fun toPaymentSourceType(value: String): PaymentSourceType = PaymentSourceType.valueOf(value)
}
