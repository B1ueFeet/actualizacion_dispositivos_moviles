package com.bluefeet.antidesperdicio.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class FoodWithType(
    @Embedded val food: Food,
    @Relation(
        parentColumn = "typeId",
        entityColumn = "id"
    )
    val type: FoodType,
)
