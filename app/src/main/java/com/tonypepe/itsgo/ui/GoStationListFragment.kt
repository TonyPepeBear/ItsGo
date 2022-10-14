package com.tonypepe.itsgo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.entity.GoStation
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentGoStationListBinding
import com.tonypepe.itsgo.databinding.ItemGoStationBinding

class GoStationListFragment : Fragment() {
    private lateinit var binding: FragmentGoStationListBinding
    private val adapter = GoStationAdapter(emptyList())
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoStationListBinding.inflate(inflater, container, false)
        binding.recycler.apply {
            adapter = this@GoStationListFragment.adapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
        model.allGoStationLiveData.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }
        return binding.root
    }
}

class GoStationAdapter(private var items: List<GoStation>) :
    RecyclerView.Adapter<GoStationItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoStationItemViewHolder {
        return GoStationItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_go_station, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GoStationItemViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<GoStation>) {
        this.items = items
        notifyDataSetChanged()
    }
}

class GoStationItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemGoStationBinding.bind(itemView)

    fun bindView(item: GoStation) {
        binding.title.text = item.name
    }
}