package com.example.chatapp.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.chatapp.model.UserProfile
import com.example.chatapp.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var ImageUri: Uri? = null
    private var storagrRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        storagrRef = FirebaseStorage.getInstance().reference.child("User_Images")

        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: UserProfile? = snapshot.getValue(UserProfile::class.java)

                    if (context != null) {
                        view.username_settings.text = user!!.Username
                        Picasso.get().load(user.profile).into(view.profile_image_settings)
                        Picasso.get().load(user.cover).into(view.cover_image_settings)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        view.profile_image_settings.setOnClickListener {
            pickImage()
        }

        view.cover_image_settings.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }

        view.set_Facebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }

        view.set_Instagram.setOnClickListener {
            socialChecker = "Instagram"
            setSocialLinks()
        }

        view.set_Website.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }

        return view
    }

    private fun setSocialLinks() {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(
                requireContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert
            )

        val editText = EditText(context)

        if (socialChecker == "website") {

            builder.setTitle("Write the URL")
            editText.hint = "eg: www.aabbcc.com"

        } else {

            builder.setTitle("Write Username")
            editText.hint = "eg: qwertyuiop"

        }

        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener { dialog, which ->
            val str = editText.text.toString()

            if (str == "") {
                Toast.makeText(context, "Write something", Toast.LENGTH_SHORT).show()
            } else {
                saveLinks(str)
            }
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }

    private fun saveLinks(str: String) {

        val mapLinks = HashMap<String, Any>()

        when (socialChecker) {
            "facebook" -> {
                mapLinks["facebook"] = "https://m.facebook.com/$str"
            }
            "Instagram" -> {
                mapLinks["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                mapLinks["website"] = "https://$str"
            }
        }

        userReference!!.updateChildren(mapLinks).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Links uploaded", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun pickImage() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            ImageUri = data.data
            Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading")
        progressBar.show()

        if (ImageUri != null) {
            val fileRef = storagrRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(ImageUri!!)

            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover") {

                        val mapCover = HashMap<String, Any>()
                        mapCover["cover"] = url
                        userReference!!.updateChildren(mapCover)
                        coverChecker = ""

                    } else {

                        val mapProfile = HashMap<String, Any>()
                        mapProfile["profile"] = url
                        userReference!!.updateChildren(mapProfile)
                        coverChecker = ""

                    }
                    progressBar.dismiss()
                }
            }
        }
    }


}