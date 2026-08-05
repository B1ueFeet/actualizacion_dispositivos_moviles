package com.bluefeet.antidesperdicio.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.data.repository.FoodRepository
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    val foods = repository.foods.asLiveData()

    fun addFood(food: Food, onSaved: (Food) -> Unit) {
        viewModelScope.launch {
            val generatedId = repository.addFood(food)
            val savedFood = food.copy(id = generatedId.toInt())
            onSaved(savedFood)
        }
    }

    fun updateFood(food: Food, onUpdated: (Food) -> Unit) {
        viewModelScope.launch {
            repository.updateFood(food)
            onUpdated(food)
        }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch {
            repository.deleteFood(food)
        }
    }
}

class FoodViewModelFactory(
    private val repository: FoodRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}