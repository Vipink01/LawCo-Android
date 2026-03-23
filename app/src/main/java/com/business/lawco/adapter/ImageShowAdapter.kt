package com.business.lawco.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat.getColor
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.databinding.ItemUploadBinding
import java.net.URLEncoder

class ImageShowAdapter( var requireContext: Context , var list: MutableList<String>) :
    RecyclerView.Adapter<ImageShowAdapter.Holder>() {
    class Holder(val binding: ItemUploadBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.imgCross.visibility = View.GONE
        holder.binding.hideUpload.visibility = View.GONE
        val item = list[position]

        if (item.endsWith(".pdf",true) || item.endsWith(".doc",true)|| item.endsWith(".docx",true)){
            val fileIcon = when {
                item.lowercase().endsWith(".pdf") -> R.drawable.pdficon
                item.lowercase().endsWith(".doc") || item.lowercase().endsWith(".docx") ->
                    R.drawable.docicon
                else ->
                    R.drawable.docicon
            }
            Glide.with(requireContext)
                .load(fileIcon)
                .placeholder(fileIcon)
                .error(fileIcon)
                .into(holder.binding.imageData)
        }else{
            val progressDrawable = CircularProgressDrawable(requireContext).apply {
                strokeWidth = 5f
                centerRadius = 30f
                setColorSchemeColors(getColor(requireContext, R.color.orange))
                start()
            }
            Glide.with(requireContext)
                .load(item)
                .error(R.drawable.thumbnailicon)
                .placeholder(progressDrawable)
                .into(holder.binding.imageData)
        }


        holder.itemView.setOnClickListener {
            downloadImage(item)
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun downloadImage(url: String) {
        Log.d("openLogoutInBrowser", url)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(requireContext, url.toUri())
        /*val fullScreenDialog = Dialog(requireContext, com.business.lawco.R.style.FullScreenDialog)
        fullScreenDialog.setContentView(R.layout.imagepdfalert)
        fullScreenDialog.setCancelable(true)
        fullScreenDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val arrowWhite: ImageView = fullScreenDialog.findViewById(com.business.lawco.R.id.arrowWhite)
        val imgDownload: ImageView = fullScreenDialog.findViewById(com.business.lawco.R.id.imgDownload)
        val img: ImageView = fullScreenDialog.findViewById(com.business.lawco.R.id.img)
        val webView: WebView = fullScreenDialog.findViewById(com.business.lawco.R.id.webView)
        val loader: LottieAnimationView = fullScreenDialog.findViewById(com.business.lawco.R.id.loader)
        if (url.endsWith(".pdf",true) || url.endsWith(".doc",true)|| url.endsWith(".docx",true)){
            webView.visibility = View.VISIBLE
            loader.visibility = View.VISIBLE
            img.visibility = View.GONE
            val settings = webView.settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"

            webView.webViewClient = object : WebViewClient() {
                @SuppressLint("SetTextI18n")
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    loader.visibility = View.GONE
                }
            }
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val googleViewer = "https://drive.google.com/viewerng/viewer?embedded=true&url=$url"
            webView.loadUrl(googleViewer)
        }else{
            webView.visibility = View.GONE
            img.visibility = View.VISIBLE
            val progressDrawable = CircularProgressDrawable(requireContext).apply {
                strokeWidth = 5f
                centerRadius = 30f
                setColorSchemeColors(getColor(requireContext, R.color.orange))
                start()
            }
            Glide.with(requireContext)
                .load(url)
                .error(com.business.lawco.R.drawable.thumbnailicon)
                .placeholder(progressDrawable)
                .into(img)
        }
        imgDownload.setOnClickListener {
            val request = DownloadManager.Request(url.toUri())
                .setTitle("Image Download")
                .setDescription("Downloading image...")
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "downloaded_image.jpg"
                )

            val downloadManager = requireContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(requireContext, "Download started", Toast.LENGTH_SHORT).show()
        }

        arrowWhite.setOnClickListener {
            fullScreenDialog.dismiss()
        }
        fullScreenDialog.show()*/
    }



}