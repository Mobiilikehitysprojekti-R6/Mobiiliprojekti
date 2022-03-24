package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.aikataulusuunnitteluapp.Model.HourModel

class HourAdapter(private val context: Context,
                  private val dataset: List<HourModel>) :
    RecyclerView.Adapter<HourAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.hourcell, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.hour_tv.text = context.resources.getString(item.stringResourceId)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view){

        val hour_tv: TextView = view.findViewById(R.id.hourcell_tv)
    }


}