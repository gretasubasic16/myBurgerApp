package org.unizd.rma.subasic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.unizd.rma.subasic.R

class BurgerAdapter(
    private val burgerList: List<MainActivity.Burger>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<BurgerAdapter.BurgerViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(position: Int, isExpanded: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BurgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.burger_item, parent, false)
        return BurgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BurgerViewHolder, position: Int) {
        val burger = burgerList[position]
        holder.bind(burger)
    }

    override fun getItemCount(): Int {
        return burgerList.size
    }

    inner class BurgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewBurgerName: TextView = itemView.findViewById(R.id.textViewBurgerName)

        fun bind(burger: MainActivity.Burger) {
            textViewBurgerName.text = burger.name

            itemView.setOnClickListener {
                val isExpanded = !burger.isExpanded
                itemClickListener.onItemClick(adapterPosition, isExpanded)
            }
        }
    }
}
