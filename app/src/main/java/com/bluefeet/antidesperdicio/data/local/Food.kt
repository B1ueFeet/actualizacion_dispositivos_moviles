package com.bluefeet.antidesperdicio.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    foreignKeys = [
        ForeignKey(
            entity = FoodType::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("typeId")]
)
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val expirationDate: Long,
    val quantity: Double,
    val unit: String,
    val typeId: Int,
)
