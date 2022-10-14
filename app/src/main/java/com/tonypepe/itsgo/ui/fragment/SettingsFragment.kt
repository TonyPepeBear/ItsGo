package com.tonypepe.itsgo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    lateinit var binding: FragmentSettingsBinding
    val model: MainViewModel by activityViewModels()

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
        return root
    }
}
