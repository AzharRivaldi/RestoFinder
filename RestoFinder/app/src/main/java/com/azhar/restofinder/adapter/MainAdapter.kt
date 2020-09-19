package com.azhar.restofinder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.restofinder.R
import com.azhar.restofinder.model.ModelMain
import com.azhar.restofinder.utils.OnItemClickCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.list_item_main.view.*

/**
 * Created by Azhar Rivaldi on 16-09-2020
 */

class MainAdapter (

    private val mContext: Context, private val items: List<ModelMain>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private var Rating = 0.0

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback?) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]

        Rating = data.aggregateRating

        Glide.with(mContext)
                .load(data.thumbResto)
                .transform(CenterCrop(), RoundedCorners(25))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgResto)

        val newValue = Rating.toFloat()
        holder.ratingResto.numStars = 5
        holder.ratingResto.stepSize = 0.5.toFloat()
        holder.ratingResto.rating = newValue

        holder.tvNameResto.text = data.nameResto
        holder.tvAddress.text = data.addressResto
        holder.tvRating.text = " |  " + data.aggregateRating + " " + data.ratingText
        holder.cvListMain.setOnClickListener {
            onItemClickCallback?.onItemMainClicked(data)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    //Class Holder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvListMain: CardView
        var imgResto: ImageView
        var tvNameResto: TextView
        var tvAddress: TextView
        var tvRating: TextView
        var ratingResto: RatingBar

        init {
            cvListMain = itemView.cvListMain
            imgResto = itemView.imgResto
            tvNameResto = itemView.tvNameResto
            tvRating = itemView.tvRating
            tvAddress = itemView.tvAddress
            ratingResto = itemView.ratingResto
        }
    }
}