package com.revolut.interview.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.revolut.interview.R
import com.revolut.interview.domain.Rate
import androidx.recyclerview.widget.ListAdapter

/**
 * Task 4 adapter implementation.
 *
 * ListAdapter and DiffUtil replace the base project's manual list plus
 * notifyDataSetChanged(), so frequent one-second rate updates only rebind changed
 * rows instead of refreshing the whole RecyclerView.
 */
class RatesAdapter : ListAdapter<Rate, RatesAdapter.ViewHolder>(RateDiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val currency: TextView = itemView.findViewById(R.id.currency)
        val value: TextView = itemView.findViewById(R.id.value)

        fun bind(rate: Rate) {
            currency.text = rate.currency
            bindValue(rate)
        }

        // Used by DiffUtil payloads when only the numeric rate changes.
        fun bindValue(rate: Rate) {
            value.text = rate.value.toString()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_rates, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(RatePayload.ValueChanged)) {
            viewHolder.bindValue(getItem(position))
        } else {
            super.onBindViewHolder(viewHolder, position, payloads)
        }
    }

}

object RateDiffCallback : DiffUtil.ItemCallback<Rate>() {

    // Currency is stable for each row, so it is the item identity.
    override fun areItemsTheSame(oldItem: Rate, newItem: Rate): Boolean {
        return oldItem.currency == newItem.currency
    }

    override fun areContentsTheSame(oldItem: Rate, newItem: Rate): Boolean {
        return oldItem.currency == newItem.currency &&
                oldItem.value == newItem.value
    }

    // Payload lets the adapter update only the right-aligned value TextView.
    override fun getChangePayload(oldItem: Rate, newItem: Rate): Any? {
        return if (oldItem.value != newItem.value) {
            RatePayload.ValueChanged
        } else {
            null
        }
    }
}

private enum class RatePayload {
    ValueChanged
}
