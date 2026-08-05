package com.bluefeet.antidesperdicio.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bluefeet.antidesperdicio.R
import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.databinding.ItemFoodBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FoodAdapter : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val foods = mutableListOf<Food>()

    fun submitList(newFoods: List<Food>) {
        foods.clear()
        foods.addAll(newFoods)
        notifyDataSetChanged()
    }

    fun getFoodAt(position: Int): Food {
        return foods[position]
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

        fun bind(food: Food) {
            val daysLeft = calculateDaysLeft(food.expirationDate)
            val status = getStatus(daysLeft)
            val color = getStatusColor(daysLeft)

            binding.foodNameTextView.text = food.name
            binding.expirationTextView.text = "Fecha de caducidad: ${formatDate(food.expirationDate)}"
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
                daysLeft < 0 -> "Vencido"
                daysLeft == 0L -> "Vence hoy"
                daysLeft == 1L -> "Vence manana"
                else -> "Vence en $daysLeft dias"
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
    }
}
