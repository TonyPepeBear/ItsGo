package com.tonypepe.itsgo.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.entity.GoStation
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentGoStationListBinding
import com.tonypepe.itsgo.databinding.ItemGoStationBinding

class GoStationListFragment : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentGoStationListBinding
    private val adapter = GoStationAdapter(emptyList(), onItemClickListener = this)
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
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        model.allGoStationSortWithUserLocatoinLiveData.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }
        model.userLocationLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setUserLocation(it)
            }
        }
        return binding.root
    }

    override fun onItemClick(item: GoStation) {
        model.setDetailGoStationWithName(item.name)
        findNavController().navigate(R.id.nav_go_station_detail_fragment)
    }
}

class GoStationAdapter(
    private var items: List<GoStation>,
    private var userLocation: Point? = null,
    private var onItemClickListener: OnItemClickListener? = null
) :
    RecyclerView.Adapter<GoStationItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoStationItemViewHolder {
        return GoStationItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_go_station, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GoStationItemViewHolder, position: Int) {
        holder.bindView(items[position], userLocation)
        holder.itemView.setOnClickListener { onItemClickListener?.onItemClick(items[position]) }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<GoStation>) {
        this.items = items
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUserLocation(point: Point) {
        this.userLocation = point
        notifyDataSetChanged()
    }
}

class GoStationItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemGoStationBinding.bind(itemView)

    fun bindView(item: GoStation, userLocation: Point?) {
        binding.title.text = item.name
        if (userLocation != null) {
            val d = TurfMeasurement.distance(
                item.toPoint(),
                userLocation,
                TurfConstants.UNIT_KILOMETRES
            )
            binding.distance.text = itemView.context.resources.getString(R.string.double_km, d)
        }
    }
}

interface OnItemClickListener {
    fun onItemClick(item: GoStation)
}
