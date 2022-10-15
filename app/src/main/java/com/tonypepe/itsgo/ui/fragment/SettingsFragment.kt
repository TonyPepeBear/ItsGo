package com.tonypepe.itsgo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    lateinit var binding: FragmentSettingsBinding
    val model: MainViewModel by activityViewModels()
    val TAG = SettingsFragment::class.simpleName

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        model.goStationCountLiveData.observe(viewLifecycleOwner) {
            binding.text.text = resources.getString(R.string.go_station_count, it)
        }
        binding.buttonUpdateGoStation.setOnClickListener {
            model.fetchGoStation()
            Snackbar.make(it, R.string.updating, Snackbar.LENGTH_SHORT).show()
        }
        model.settingIsochroneLiveData.observe(viewLifecycleOwner) {
            binding.isochroneInput.setText(it.toString())
        }
        binding.isochroneInput.addTextChangedListener {
            val isochrone = binding.isochroneInput
            isochrone.error = null
            it?.toString()?.toIntOrNull()?.run {
                if (this < 1) {
                    isochrone.error = resources.getString(R.string.require_1_to_100)
                }
                if (this > 100) {
                    isochrone.error = resources.getString(R.string.require_1_to_100)
                }
            }
        }
        binding.isochroneInput.setOnEditorActionListener { v, actionId, _ ->
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(v.windowToken, 0)
            val num = binding.isochroneInput.text?.toString()?.toIntOrNull() ?: 50
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                if (num > 100) {
                    model.setSettingIsochrone(100)
                } else if (num < 1) {
                    model.setSettingIsochrone(1)
                } else {
                    model.setSettingIsochrone(num)
                }
            }
            true
        }
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        val num = binding.isochroneInput.text?.toString()?.toIntOrNull() ?: 50
        if (num > 100) {
            model.setSettingIsochrone(100)
        } else if (num < 1) {
            model.setSettingIsochrone(1)
        } else {
            model.setSettingIsochrone(num)
        }
    }
}
