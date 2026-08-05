package com.bluefeet.antidesperdicio.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bluefeet.antidesperdicio.R
import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.data.local.FoodWithType
import com.bluefeet.antidesperdicio.databinding.ItemFoodBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FoodAdapter : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val foods = mutableListOf<FoodWithType>()

    fun submitList(newFoods: List<FoodWithType>) {
        foods.clear()
        foods.addAll(newFoods)
        notifyDataSetChanged()
    }

    fun getFoodAt(position: Int): Food {
        return foods[position].food
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foods[position])
    }

    override fun getItemCount(): Int = foods.size

    class FoodViewHolder(
        private val binding: ItemFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(foodWithType: FoodWithType) {
            val food = foodWithType.food
            val daysLeft = calculateDaysLeft(food.expirationDate)
            val status = getStatus(daysLeft)
            val color = getStatusColor(daysLeft)

            binding.foodNameTextView.text = food.name
            binding.expirationTextView.text = binding.root.context.getString(
                R.string.food_detail_format,
                formatQuantity(food.quantity),
                food.unit,
                foodWithType.type.name,
                formatDate(food.expirationDate)
            )
            binding.statusTextView.text = status
            binding.statusTextView.setTextColor(color)
            binding.foodCardView.strokeColor = color
        }

        private fun calculateDaysLeft(expirationDate: Long): Long {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val expiration = Calendar.getInstance().apply {
                timeInMillis = expirationDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diff = expiration.timeInMillis - today.timeInMillis
            return TimeUnit.MILLISECONDS.toDays(diff)
        }

        private fun getStatus(daysLeft: Long): String {
            return when {
                daysLeft < 0 -> binding.root.context.getString(R.string.status_expired)
                daysLeft == 0L -> binding.root.context.getString(R.string.status_today)
                daysLeft == 1L -> binding.root.context.getString(R.string.status_tomorrow)
                else -> binding.root.context.getString(R.string.status_days_left, daysLeft)
            }
        }

        private fun getStatusColor(daysLeft: Long): Int {
            val context = binding.root.context
            val colorRes = when {
                daysLeft <= 1 -> R.color.status_red
                daysLeft <= 7 -> R.color.status_yellow
                else -> R.color.status_green
            }
            return ContextCompat.getColor(context, colorRes)
        }

        private fun formatDate(dateMillis: Long): String {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return formatter.format(Date(dateMillis))
        }

        private fun formatQuantity(quantity: Double): String {
            return if (quantity % 1.0 == 0.0) {
                quantity.toInt().toString()
            } else {
                quantity.toString()
            }
        }
    }
}

