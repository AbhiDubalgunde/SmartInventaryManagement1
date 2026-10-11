package com.example.smartinventarymanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class ItemAdapter(
    private var itemList: List<Item>,
    private val onDeleteClick: (Item) -> Unit,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var fullList: List<Item> = itemList
    private var lastAnimatedPosition = -1

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val qty: TextView = view.findViewById(R.id.tvQty)
        val warning: TextView = view.findViewById(R.id.tvWarning)
        val image: ImageView = view.findViewById(R.id.imgItem)
        val delete: View = view.findViewById(R.id.itemDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.name.text = item.name
        val context = holder.itemView.context
        val money = DecimalFormat("#,##0.00")
        holder.price.text = context.getString(R.string.inventory_value_line,
            "₹${money.format(item.price)}", "₹${money.format(item.quantity * item.price)}")
        holder.qty.text = context.getString(R.string.inventory_units, item.quantity)
        val status = InventoryRules.status(item.quantity, item.lowStockThreshold)
        holder.warning.visibility = View.VISIBLE
        holder.warning.setText(when (status) {
            StockStatus.OUT_OF_STOCK -> R.string.inventory_out_of_stock
            StockStatus.LOW_STOCK -> R.string.inventory_low_stock
            StockStatus.IN_STOCK -> R.string.inventory_in_stock
        })
        holder.warning.setTextColor(androidx.core.content.ContextCompat.getColor(context, when (status) {
            StockStatus.OUT_OF_STOCK -> R.color.si_danger
            StockStatus.LOW_STOCK -> R.color.si_warning
            StockStatus.IN_STOCK -> R.color.si_primary
        }))
        holder.delete.contentDescription = "Delete ${item.name}"
        holder.delete.setOnClickListener { onDeleteClick(item) }

        if (!item.imageUri.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUri)
                .placeholder(R.drawable.ic_grocery)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_grocery)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
        
        holder.itemView.setOnLongClickListener {
            onDeleteClick(item)
            true
        }

        // Entrance animation
        // Keep recycled cards stable while searching and sorting.
        holder.itemView.alpha = 1f
        holder.itemView.translationY = 0f
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.alpha = 0f
            viewToAnimate.translationY = 50f
            viewToAnimate.animate()
                .alpha(1f)
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(300)
                .setStartDelay(position * 50L)
                .start()
            lastAnimatedPosition = position
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun filter(query: String) {
        itemList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<Item>) {
        itemList = newList
        fullList = newList
        lastAnimatedPosition = -1 // Reset animations on full update
        notifyDataSetChanged()
    }
}
