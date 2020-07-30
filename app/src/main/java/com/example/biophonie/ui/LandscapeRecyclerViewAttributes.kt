package com.example.biophonie.ui

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.biophonie.R
import com.example.biophonie.databinding.LandscapeViewBinding
import com.example.biophonie.domain.Landscape
import com.example.biophonie.util.dpToPx


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
            if(adapterPosition == RecyclerView.NO_POSITION) return
            notifyItemChanged(selectedPosition)
            selectedPosition = layoutPosition
            notifyItemChanged(selectedPosition)
            //binding.background.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(v.context, R.color.colorPrimaryDark), (50F/100*255).toInt()))
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

class GridItemDecoration(context: Context, space: Int = 10, private val spanCount: Int) : RecyclerView.ItemDecoration() {

    private val spaceInDp = dpToPx(context, space)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        outRect.left = spaceInDp
        outRect.right = spaceInDp
        outRect.bottom = 0
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) < spanCount)
            outRect.top = 0
        else
            outRect.top = spaceInDp
        if(parent.getChildAdapterPosition(view) % spanCount == 0) {
            outRect.right = spaceInDp/2
            outRect.left = spaceInDp
        } else {
            outRect.right = spaceInDp
            outRect.left = spaceInDp/2
        }
    }
}