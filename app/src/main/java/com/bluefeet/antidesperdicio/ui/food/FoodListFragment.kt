package com.bluefeet.antidesperdicio.ui.food

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluefeet.antidesperdicio.R
import com.bluefeet.antidesperdicio.data.local.AppDatabase
import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.data.local.FoodWithType
import com.bluefeet.antidesperdicio.data.repository.FoodRepository
import com.bluefeet.antidesperdicio.databinding.FragmentFoodListBinding
import com.bluefeet.antidesperdicio.notification.ExpirationAlarmScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FoodListFragment : Fragment() {

    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FoodViewModel
    private val foodAdapter = FoodAdapter()
    private var currentFoods: List<FoodWithType> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFoodListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeFoods()
        setupSwipeActions()
        setupClickListeners()
        setupDebugButton()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = FoodRepository(database.foodDao())
        val factory = FoodViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[FoodViewModel::class.java]
        viewModel.seedDefaultTypes()
    }

    private fun setupRecyclerView() {
        binding.foodRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.foodRecyclerView.adapter = foodAdapter
    }

    private fun observeFoods() {
        viewModel.foods.observe(viewLifecycleOwner) { foods ->
            currentFoods = foods
            foodAdapter.submitList(foods)
            updateSummary(foods)
            binding.emptyTextView.visibility = if (foods.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun updateSummary(foods: List<FoodWithType>) {
        var freshCount = 0
        var warningCount = 0
        var criticalCount = 0

        foods.forEach { foodWithType ->
            when (calculateDaysLeft(foodWithType.food.expirationDate)) {
                in Long.MIN_VALUE..1L -> criticalCount++
                in 2L..7L -> warningCount++
                else -> freshCount++
            }
        }

        binding.freshCountTextView.text = freshCount.toString()
        binding.warningCountTextView.text = warningCount.toString()
        binding.criticalCountTextView.text = criticalCount.toString()
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
    private fun setupSwipeActions() {
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        val updateIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_update)
        val deleteBackground = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.swipe_delete))
        val updateBackground = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.swipe_update))

        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                val food = foodAdapter.getFoodAt(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    foodAdapter.notifyItemChanged(position)
                    navigateToEditFood(food)
                } else {
                    foodAdapter.notifyItemChanged(position)
                    showDeleteConfirmationDialog(food)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                val itemView = viewHolder.itemView

                if (dX > 0) {
                    val icon = updateIcon ?: return
                    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2

                    updateBackground.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )

                    icon.setBounds(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + icon.intrinsicWidth,
                        itemView.bottom - iconMargin
                    )

                    updateBackground.draw(c)
                    icon.draw(c)
                } else if (dX < 0) {
                    val icon = deleteIcon ?: return
                    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2

                    deleteBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )

                    icon.setBounds(
                        itemView.right - iconMargin - icon.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin
                    )

                    deleteBackground.draw(c)
                    icon.draw(c)
                } else {
                    deleteBackground.setBounds(0, 0, 0, 0)
                    updateBackground.setBounds(0, 0, 0, 0)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.foodRecyclerView)
    }

    private fun navigateToEditFood(food: Food) {
        val bundle = Bundle().apply {
            putInt("food_id", food.id)
            putString("food_name", food.name)
            putLong("food_expiration_date", food.expirationDate)
            putDouble("food_quantity", food.quantity)
            putString("food_unit", food.unit)
            putInt("food_type_id", food.typeId)
        }

        findNavController().navigate(
            R.id.action_foodListFragment_to_addFoodFragment,
            bundle
        )
    }

    private fun showDeleteConfirmationDialog(food: Food) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("Seguro que quieres eliminar ${food.name}?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                ExpirationAlarmScheduler(requireContext()).cancel(food)
                viewModel.deleteFood(food)
            }
            .show()
    }

    private fun setupClickListeners() {
        binding.addFoodButton.setOnClickListener {
            findNavController().navigate(R.id.action_foodListFragment_to_addFoodFragment)
        }
    }

    private fun setupDebugButton() {
        binding.debugAlarmButton.setOnClickListener {
            val food = currentFoods.randomOrNull()?.food
            val foodName = food?.name ?: "producto de prueba"
            ExpirationAlarmScheduler(requireContext()).scheduleDebug(foodName)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

