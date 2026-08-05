package com.bluefeet.antidesperdicio.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bluefeet.antidesperdicio.R
import com.bluefeet.antidesperdicio.data.local.AppDatabase
import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.data.local.FoodType
import com.bluefeet.antidesperdicio.data.repository.FoodRepository
import com.bluefeet.antidesperdicio.databinding.FragmentAddFoodBinding
import com.bluefeet.antidesperdicio.notification.ExpirationAlarmScheduler
import com.bluefeet.antidesperdicio.ui.food.FoodViewModel
import com.bluefeet.antidesperdicio.ui.food.FoodViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFoodFragment : Fragment() {

    private var _binding: FragmentAddFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FoodViewModel
    private var selectedExpirationDate: Long? = null
    private var editingFoodId: Int? = null
    private var editingTypeId: Int? = null
    private var pendingTypeSelectionId: Int? = null
    private var foodTypes: List<FoodType> = emptyList()

    private val unitOptions: List<String>
        get() = resources.getStringArray(R.array.food_units).toList()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUnitSpinner()
        observeFoodTypes()
        setupClickListeners()
        loadFoodForEditingIfNeeded()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = FoodRepository(database.foodDao())
        val factory = FoodViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[FoodViewModel::class.java]
        viewModel.seedDefaultTypes()
    }

    private fun setupUnitSpinner() {
        binding.unitSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            unitOptions
        )
    }

    private fun observeFoodTypes() {
        viewModel.foodTypes.observe(viewLifecycleOwner) { types ->
            foodTypes = types
            binding.typeSpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                types.map { it.name }
            )

            val selectedId = pendingTypeSelectionId ?: editingTypeId
            val selectedIndex = types.indexOfFirst { it.id == selectedId }
            if (selectedIndex >= 0) {
                binding.typeSpinner.setSelection(selectedIndex)
                pendingTypeSelectionId = null
            }
        }
    }

    private fun setupClickListeners() {
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.dateInputLayout.setEndIconOnClickListener {
            showDatePicker()
        }

        binding.saveFoodButton.setOnClickListener {
            saveFood()
        }

        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadFoodForEditingIfNeeded() {
        val foodId = arguments?.getInt("food_id", 0) ?: 0

        if (foodId == 0) {
            return
        }

        val foodName = arguments?.getString("food_name").orEmpty()
        val expirationDate = arguments?.getLong("food_expiration_date") ?: return
        val quantity = arguments?.getDouble("food_quantity", 0.0) ?: 0.0
        val unit = arguments?.getString("food_unit").orEmpty()
        val typeId = arguments?.getInt("food_type_id", 0) ?: 0

        editingFoodId = foodId
        editingTypeId = typeId
        pendingTypeSelectionId = typeId
        selectedExpirationDate = expirationDate

        binding.titleTextView.text = getString(R.string.edit_food_title)
        binding.nameEditText.setText(foodName)
        binding.quantityEditText.setText(formatQuantity(quantity))
        binding.dateEditText.setText(dateFormatter.format(expirationDate))
        binding.saveFoodButton.text = getString(R.string.update_button)

        val unitIndex = unitOptions.indexOf(unit)
        if (unitIndex >= 0) {
            binding.unitSpinner.setSelection(unitIndex)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, 23, 59, 59)
                selectedCalendar.set(Calendar.MILLISECOND, 999)

                selectedExpirationDate = selectedCalendar.timeInMillis
                binding.dateEditText.setText(dateFormatter.format(selectedCalendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun saveFood() {
        val name = binding.nameEditText.text.toString().trim()
        val quantity = binding.quantityEditText.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val expirationDate = selectedExpirationDate
        val selectedType = foodTypes.getOrNull(binding.typeSpinner.selectedItemPosition)
        val unit = unitOptions.getOrNull(binding.unitSpinner.selectedItemPosition).orEmpty()

        if (name.isBlank()) {
            binding.nameInputLayout.error = getString(R.string.error_food_name_required)
            return
        } else {
            binding.nameInputLayout.error = null
        }

        if (quantity == null || quantity <= 0.0) {
            binding.quantityInputLayout.error = getString(R.string.error_quantity_required)
            return
        } else {
            binding.quantityInputLayout.error = null
        }

        if (selectedType == null) {
            Toast.makeText(requireContext(), getString(R.string.error_type_required), Toast.LENGTH_SHORT).show()
            return
        }

        if (expirationDate == null) {
            binding.dateInputLayout.error = getString(R.string.error_date_required)
            return
        } else {
            binding.dateInputLayout.error = null
        }

        val editingId = editingFoodId

        if (editingId == null) {
            val food = Food(
                name = name,
                expirationDate = expirationDate,
                quantity = quantity,
                unit = unit,
                typeId = selectedType.id
            )

            viewModel.addFood(food) { savedFood ->
                ExpirationAlarmScheduler(requireContext()).schedule(savedFood)
                Toast.makeText(requireContext(), getString(R.string.food_saved), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        } else {
            val food = Food(
                id = editingId,
                name = name,
                expirationDate = expirationDate,
                quantity = quantity,
                unit = unit,
                typeId = selectedType.id
            )

            ExpirationAlarmScheduler(requireContext()).cancel(food)

            viewModel.updateFood(food) { updatedFood ->
                ExpirationAlarmScheduler(requireContext()).schedule(updatedFood)
                Toast.makeText(requireContext(), getString(R.string.food_updated), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun formatQuantity(quantity: Double): String {
        return if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            quantity.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


