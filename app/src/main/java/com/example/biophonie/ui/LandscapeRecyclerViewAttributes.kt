package com.example.biophonie.ui

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.biophonie.databinding.LandscapeViewBinding
import com.example.biophonie.domain.Landscape
import com.example.biophonie.util.dpToPx

private const val TAG = "LandscapeRecyclerViewAt"
class LandscapesAdapter(private val dataset: List<Landscape>, private val mOnLandscapeListener: OnLandscapeListener) :
    RecyclerView.Adapter<LandscapesAdapter.LandscapeViewHolder>(){

    var selectedPosition = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LandscapeViewHolder {
        val binding = LandscapeViewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return LandscapeViewHolder(binding, mOnLandscapeListener)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: LandscapeViewHolder, position: Int) {
        holder.bind(dataset[position])
        holder.itemView.isSelected = selectedPosition == position
    }

    inner class LandscapeViewHolder(val binding: LandscapeViewBinding, private val onLandscapeListener: OnLandscapeListener): RecyclerView.ViewHolder(binding.root), View.OnClickListener  {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
            if (selectedPosition != RecyclerView.NO_POSITION) notifyItemChanged(selectedPosition)
            selectedPosition = layoutPosition
            notifyItemChanged(selectedPosition)
            onLandscapeListener.onLandscapeClick(adapterPosition)
        }

        fun bind(landscape: Landscape){
            binding.title.text = landscape.titre
            binding.landscapeImage.setImageResource(landscape.image)
        }
    }

    interface OnLandscapeListener {
        fun onLandscapeClick(position: Int)
    }

}