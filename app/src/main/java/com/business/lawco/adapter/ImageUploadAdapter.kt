package com.business.lawco.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.business.lawco.R
import com.business.lawco.databinding.ItemUploadBinding
import com.business.lawco.utility.OnItemSelectListener

class ImageUploadAdapter( var list: MutableList<Uri>,var requireContext: Context,var OnItemSelectListener: OnItemSelectListener) :
    RecyclerView.Adapter<ImageUploadAdapter.Holder>() {
    class Holder(val binding: ItemUploadBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(uriList: MutableList<Uri>) {
        list=uriList
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = list[position]

        val uri = Uri.parse(dataItem.toString())

        if (isDocument(uri)) {
            holder.binding.hideUpload.visibility = View.GONE
            if (isPdf(uri)){
                holder.binding.imageData.setImageResource(R.drawable.pdficon)
            }else{
                holder.binding.imageData.setImageResource(R.drawable.docicon)
            }
        } else {
            holder.binding.hideUpload.visibility = View.VISIBLE
            Glide.with(requireContext)
                .load(uri)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.hideUpload.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.hideUpload.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.binding.imageData)
        }


        holder.binding.imgCross.setOnClickListener {
            OnItemSelectListener.itemSelect(position,"","")
        }

    }

    private fun isPdf(uri: Uri): Boolean {
        return requireContext
            .contentResolver
            .getType(uri)
            .equals("application/pdf", ignoreCase = true)
    }
    private fun isDocument(uri: Uri): Boolean {
        val mimeType = requireContext.contentResolver.getType(uri)
        return mimeType in listOf(
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
        )
    }

}