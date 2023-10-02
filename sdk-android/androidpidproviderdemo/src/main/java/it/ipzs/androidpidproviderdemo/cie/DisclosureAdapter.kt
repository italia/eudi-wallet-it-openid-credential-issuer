package it.ipzs.androidpidproviderdemo.cie

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.authlete.sd.Disclosure
import it.ipzs.androidpidproviderdemo.databinding.ViewDisclosureItemBinding

class DisclosureAdapter(
    private val list: ArrayList<Disclosure>
) : RecyclerView.Adapter<DisclosureAdapter.CarConditionViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarConditionViewHolder {
        val binding = ViewDisclosureItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return CarConditionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarConditionViewHolder, position: Int) {
        val item = list[position]
        holder.claimName.text = item.claimName
        holder.claimValue.text = item.claimValue.toString()
    }

    override fun getItemCount(): Int = list.size

    inner class CarConditionViewHolder(binding: ViewDisclosureItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val claimName: TextView = binding.claimName
        val claimValue: TextView = binding.claimValue
    }

}