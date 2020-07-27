package com.example.biophonie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.biophonie.R
import com.example.biophonie.databinding.LandscapeViewBinding
import com.example.biophonie.domain.Landscape


class LandscapesAdapter(private val dataset: List<Landscape>, private val mOnLandscapeListener: OnLandscapeListener) :
    RecyclerView.Adapter<LandscapesAdapter.LandscapeViewHolder>(){

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
    }

    class LandscapeViewHolder(val binding: LandscapeViewBinding, private val onLandscapeListener: OnLandscapeListener): RecyclerView.ViewHolder(binding.root), View.OnClickListener  {
        init {
            binding.root.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            onLandscapeListener.onLandscapeClick(adapterPosition)
        }

        fun bind(landscape: Landscape){
            binding.title.text = landscape.titre
            binding.landscapeImage.setImageDrawable(landscape.image)
        }
    }

    interface OnLandscapeListener {
        fun onLandscapeClick(position: Int)
    }

}