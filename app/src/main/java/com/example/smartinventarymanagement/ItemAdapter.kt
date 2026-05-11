package com.example.smartinventarymanagement

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ItemAdapter(
    private var itemList: List<Item>,
    private val onDeleteClick: (Item) -> Unit,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var fullList: List<Item> = itemList.toList()

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val name: TextView =
            view.findViewById(R.id.tvName)

        val price: TextView =
            view.findViewById(R.id.tvPrice)

        val qty: TextView =
            view.findViewById(R.id.tvQty)

        val warning: TextView =
            view.findViewById(R.id.tvWarning)

        val image: ImageView =
            view.findViewById(R.id.imgItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {

        val item = itemList[position]

        // NAME
        holder.name.text = item.name

        // PRICE
        holder.price.text = holder.itemView.context.getString(R.string.price_label, item.price.toString())

        // QUANTITY
        holder.qty.text = holder.itemView.context.getString(R.string.qty_label, item.quantity)

        // LOW STOCK
        if (item.isLowStock) {

            holder.warning.visibility = View.VISIBLE

        } else {

            holder.warning.visibility = View.GONE
        }

        // 🔥 IMAGE LOGIC

        if (!item.imageUri.isNullOrEmpty()) {

            // USER SELECTED IMAGE

            Glide.with(holder.itemView.context)
                .load(Uri.parse(item.imageUri))
                .placeholder(R.drawable.ic_grocery)
                .into(holder.image)

        } else {

            // DEFAULT CATEGORY IMAGE

            when (item.category.lowercase()) {

                "grocery" ->
                    holder.image.setImageResource(R.drawable.ic_grocery)

                "electronics" ->
                    holder.image.setImageResource(R.drawable.electronics)

                "personal care" ->
                    holder.image.setImageResource(R.drawable.personal_care)

                "stationery" ->
                    holder.image.setImageResource(R.drawable.stationary)

                else ->
                    holder.image.setImageResource(R.drawable.ic_grocery)
            }
        }

        // DELETE
        holder.itemView.setOnLongClickListener {

            onDeleteClick(item)

            true
        }

        // EDIT
        holder.itemView.setOnClickListener {

            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    // SEARCH
    fun filter(query: String) {

        itemList = if (query.isEmpty()) {

            fullList

        } else {

            fullList.filter {

                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }

        notifyDataSetChanged()
    }

    // UPDATE
    fun updateData(newList: List<Item>) {

        itemList = newList

        fullList = newList.toList()

        notifyDataSetChanged()
    }
}