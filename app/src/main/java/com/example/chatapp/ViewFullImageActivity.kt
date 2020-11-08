package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {

    private var imageView: ImageView? = null
    private var imageUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        imageUrl = intent.getStringExtra("url")
        imageView = findViewById<ImageView>(R.id.image_full_view)

        Picasso.get().load(imageUrl).into(imageView)

    }
}