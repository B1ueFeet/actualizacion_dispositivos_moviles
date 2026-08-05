package com.bluefeet.antidesperdicio.data.repository

import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.data.local.FoodDao
import com.bluefeet.antidesperdicio.data.local.FoodType
import com.bluefeet.antidesperdicio.data.local.FoodWithType
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao
) {
    val foods: Flow<List<FoodWithType>> = foodDao.observeFoodsWithType()
    val foodTypes: Flow<List<FoodType>> = foodDao.observeFoodTypes()

    suspend fun seedDefaultTypes() {
        foodDao.insertFoodTypes(FoodType.DEFAULT_TYPES)
    }

    suspend fun addFood(food: Food): Long {
        return foodDao.insertFood(food)
    }

    suspend fun updateFood(food: Food) {
        foodDao.updateFood(food)
    }

    suspend fun deleteFood(food: Food) {
        foodDao.deleteFood(food)
    }
}
