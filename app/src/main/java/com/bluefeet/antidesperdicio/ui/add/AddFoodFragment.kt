package com.bluefeet.antidesperdicio.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bluefeet.antidesperdicio.data.local.AppDatabase
import com.bluefeet.antidesperdicio.data.local.Food
import com.bluefeet.antidesperdicio.data.repository.FoodRepository
import com.bluefeet.antidesperdicio.databinding.FragmentAddFoodBinding
import com.bluefeet.antidesperdicio.ui.food.FoodViewModel
import com.bluefeet.antidesperdicio.ui.food.FoodViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.bluefeet.antidesperdicio.notification.ExpirationAlarmScheduler

class AddFoodFragment : Fragment() {

    private var _binding: FragmentAddFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FoodViewModel
    private var selectedExpirationDate: Long? = null

    private var editingFoodId: Int? = null

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
        setupClickListeners()

        loadFoodForEditingIfNeeded()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = FoodRepository(database.foodDao())
        val factory = FoodViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[FoodViewModel::class.java]
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

        editingFoodId = foodId
        selectedExpirationDate = expirationDate

        binding.titleTextView.text = "Actualizar producto"
        binding.nameEditText.setText(foodName)
        binding.dateEditText.setText(dateFormatter.format(expirationDate))
        binding.saveFoodButton.text = "Actualizar"
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
        val expirationDate = selectedExpirationDate

        if (name.isBlank()) {
            binding.nameInputLayout.error = "Ingresa el nombre"
            return
        } else {
            binding.nameInputLayout.error = null
        }

        if (expirationDate == null) {
            binding.dateInputLayout.error = "Selecciona una fecha"
            return
        } else {
            binding.dateInputLayout.error = null
        }

        val editingId = editingFoodId

        if (editingId == null) {
            val food = Food(
                name = name,
                expirationDate = expirationDate
            )

            viewModel.addFood(food) { savedFood ->
                ExpirationAlarmScheduler(requireContext()).schedule(savedFood)
                Toast.makeText(requireContext(), "Alimento guardado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        } else {
            val food = Food(
                id = editingId,
                name = name,
                expirationDate = expirationDate
            )

            ExpirationAlarmScheduler(requireContext()).cancel(food)

            viewModel.updateFood(food) { updatedFood ->
                ExpirationAlarmScheduler(requireContext()).schedule(updatedFood)
                Toast.makeText(requireContext(), "Alimento actualizado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}