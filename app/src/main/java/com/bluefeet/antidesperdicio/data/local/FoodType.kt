package com.bluefeet.antidesperdicio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_types")
data class FoodType(
    @PrimaryKey
    val id: Int,
    val name: String,
) {
    companion object {
        val DEFAULT_TYPES = listOf(
            FoodType(1, "Lacteos"),
            FoodType(2, "Carnes"),
            FoodType(3, "Frutas"),
            FoodType(4, "Verduras"),
            FoodType(5, "Bebidas"),
            FoodType(6, "Granos"),
            FoodType(7, "Otros"),
        )
    }
}
