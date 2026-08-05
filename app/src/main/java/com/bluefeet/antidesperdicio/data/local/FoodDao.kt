package com.bluefeet.antidesperdicio.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Transaction
    @Query("SELECT * FROM foods ORDER BY expirationDate ASC")
    fun observeFoodsWithType(): Flow<List<FoodWithType>>

    @Query("SELECT * FROM food_types ORDER BY name ASC")
    fun observeFoodTypes(): Flow<List<FoodType>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoodTypes(types: List<FoodType>)

    @Insert
    suspend fun insertFood(food: Food): Long

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)
}
