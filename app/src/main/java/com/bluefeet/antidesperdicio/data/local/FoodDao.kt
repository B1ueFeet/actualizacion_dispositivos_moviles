package com.bluefeet.antidesperdicio.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Update
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM foods ORDER BY expirationDate ASC")
    fun observeFoods(): Flow<List<Food>>

    @Insert
    suspend fun insertFood(food: Food): Long

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)
}