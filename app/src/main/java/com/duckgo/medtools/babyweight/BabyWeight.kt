package com.duckgo.medtools.babyweight

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.duckgo.medtools.databinding.FragmentBabyWeightBinding
import com.duckgo.medtools.medicaltools.BaseFragmentDataBinding

class BabyWeight : BaseFragmentDataBinding<FragmentBabyWeightBinding>() {

    private val viewModel: BabyWeightViewModel by viewModels()
    private lateinit var adapter: UserCompareAdapter

    override fun getFragmentViewBinding(): FragmentBabyWeightBinding {
        return FragmentBabyWeightBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = UserCompareAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.predictedWeight.observe(viewLifecycleOwner) { result ->
            binding.tvPredictedWeight.text = result
        }

        viewModel.percentileReports.observe(viewLifecycleOwner) { reports ->
            adapter.updateData(reports)
            binding.recyclerView.scrollToPosition(0)
        }

        viewModel.pregnancyWeekError.observe(viewLifecycleOwner) { error ->
            binding.etPregnancyWeek.error = error
        }

        // 观察清除指令，清空所有输入框
        viewModel.clearTrigger.observe(viewLifecycleOwner) {
            clearAllInputs()
        }

        viewModel.isEarlyPregnancyMode.observe(viewLifecycleOwner) { isEarly ->
            if (isEarly) {
                binding.llStandardBiometrics.visibility = View.GONE
                binding.llEarlyPregnancyBiometrics.visibility = View.VISIBLE
            } else {
                binding.llStandardBiometrics.visibility = View.VISIBLE
                binding.llEarlyPregnancyBiometrics.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.tvLabelPregnancyWeek.setOnClickListener {
            viewModel.toggleMode()
        }

        binding.tvLabelBack.setOnClickListener {
            viewModel.toggleMode()
        }

        binding.btnCompare.setOnClickListener {
            hideKeyboard()
            viewModel.performCalculations(
                acStr = binding.etAC.text.toString(),
                flStr = binding.etFL.text.toString(),
                bpdStr = binding.etBPD.text.toString(),
                hcStr = binding.etHC.text.toString(),
                hlStr = binding.etHL.text.toString(),
                ulnaStr = binding.etUlna.text.toString(),
                tibiaStr = binding.etTibia.text.toString(),
                weekStr = binding.etPregnancyWeek.text.toString(),
                lmpStr = binding.LMP.text.toString(),
                inspectStr = binding.etInspectionDate.text.toString(),
                gsAvgStr = binding.etGSAvg.text.toString(),
                gs1Str = binding.etGS1.text.toString(),
                gs2Str = binding.etGS2.text.toString(),
                gs3Str = binding.etGS3.text.toString(),
                crlStr = binding.etCRL.text.toString()
            )
        }

        binding.btnClear.setOnClickListener {
            viewModel.clearAll()
        }
    }

    private fun clearAllInputs() {
        val root = binding.root
        if (root is ViewGroup) {
            clearChildInputs(root)
        }
        binding.tvPredictedWeight.text = ""
    }

    private fun clearChildInputs(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is EditText) {
                child.text.clear()
            } else if (child is ViewGroup) {
                clearChildInputs(child)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = activity?.currentFocus ?: View(requireContext())
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
