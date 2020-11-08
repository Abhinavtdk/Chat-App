package com.example.chatapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.model.UserProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_user_profile.*

class VisitUserProfile : AppCompatActivity() {

    private var userVisitId: String = ""
    private var user: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)

        userVisitId = intent.getStringExtra("visit_id")

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
             if(snapshot.exists()){
                  user = snapshot.getValue(UserProfile::class.java)

                 username_view_profile.text = user!!.Username
                 Picasso.get().load(user!!.profile).into(profile_image_view_profile)
                 Picasso.get().load(user!!.cover).into(cover_image_view_profile)
             }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        set_Facebook_view_profile.setOnClickListener {
            val uri = Uri.parse(user!!.facebook)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        set_Instagram_view_profile.setOnClickListener {
            val uri = Uri.parse(user!!.instagram)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        set_Website_view_profile.setOnClickListener {
            val uri = Uri.parse(user!!.website)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        send_msg_button.setOnClickListener {
            val intent = Intent(this@VisitUserProfile, MessageChatActivity::class.java)
            intent.putExtra("visit_id",user!!.uid)
            startActivity(intent)
        }
    }
}