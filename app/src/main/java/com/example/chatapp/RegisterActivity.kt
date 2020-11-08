package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var referenceUsers : DatabaseReference
    private var firebaseID : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar : Toolbar= findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        register_button.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username : String = username_register.text.toString()
        val email : String = email_register.text.toString()
        val password : String = password_register.text.toString()

        if(username == ""){
            Toast.makeText(this,"Empty username",Toast.LENGTH_SHORT).show()
        } else if(email == ""){
            Toast.makeText(this,"Empty Email",Toast.LENGTH_SHORT).show()
        } else if(password == ""){
            Toast.makeText(this,"Empty password",Toast.LENGTH_SHORT).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {task ->
                if(task.isSuccessful){
                    firebaseID = mAuth.currentUser!!.uid
                    referenceUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseID)

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseID
                    userHashMap["Username"] = username
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatapp-6cf67.appspot.com/o/profile.png?alt=media&token=fbab7c3e-1752-48f5-acba-f001c90641e0"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatapp-6cf67.appspot.com/o/cover.png?alt=media&token=8fcef019-1dc9-45fd-b8f1-581e23f6fa83"
                    userHashMap["status"] = "Offline"
                    userHashMap["search"] = username.toLowerCase()
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["website"] = "https://www.google.com"

                    referenceUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val intent = Intent(this@RegisterActivity,MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }


                }else {
                    Toast.makeText(this@RegisterActivity, "Error:"+ task.exception!!.message.toString() , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}