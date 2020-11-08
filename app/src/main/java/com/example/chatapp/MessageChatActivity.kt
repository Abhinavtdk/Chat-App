package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.notifications.*
import com.example.chatapp.model.Chat
import com.example.chatapp.model.UserProfile
import com.example.chatapp.adapter.ChatListAdapter
import com.example.chatapp.fragments.APIService
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {

    lateinit var userIdVisit: String
    var firebaseUser: FirebaseUser? = null
    var chatListAdapter : ChatListAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_view_chat: RecyclerView
    var seenListener : ValueEventListener? = null
    var reference : DatabaseReference? = null

    var notify = false
    var apiService: APIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chat = findViewById(R.id.recyclerview_chat)
        recycler_view_chat.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.stackFromEnd = true
        recycler_view_chat.layoutManager = layoutManager

//        val reference = FirebaseDatabase.getInstance().reference
//            .child("Users").child(firebaseUser!!.uid)
        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: UserProfile? = snapshot.getValue(UserProfile::class.java)

                username_chat.text = user!!.Username
                Picasso.get().load(user.profile).into(profile_chat)

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.profile)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        send_message.setOnClickListener {
            notify = true
            val message = editText_chat.text.toString()
            if (message == null) {
                Toast.makeText(this@MessageChatActivity, "Type Something", Toast.LENGTH_SHORT)
                    .show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            editText_chat.setText("")
        }

        attach_image.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick image"), 438)
        }

        seenMessage(userIdVisit)
    }

    private fun retrieveMessages(senderId: String, receiverId: String?, imageUrl: String) {

        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList).clear()

                for(snap in snapshot.children){
                    val chat = snap.getValue(Chat::class.java)

                    if((chat!!.sender.equals(receiverId) && chat.receiver.equals(senderId)) || (chat.receiver.equals(receiverId) && chat.sender.equals(senderId))) {

                        (mChatList as ArrayList<Chat>).add(chat)

                    }
                }
                chatListAdapter = ChatListAdapter(this@MessageChatActivity, mChatList as ArrayList<Chat>, imageUrl)

                recycler_view_chat.adapter = chatListAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {

        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["receiver"] = receiverId
        messageHashMap["message"] = message
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey

        reference.child("Chats").child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList") // using chatlist to find the total number of unread chats
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists()){
                                chatListReference.child("id").setValue(userIdVisit)
                            }

                            val chatListRecieverReference = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList") // using chatlist to find the total number of unread chats
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)

                            chatListRecieverReference.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                }
            }

        val userReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        //push notifications
        userReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserProfile::class.java)
                if(notify){
                    sendNotification(receiverId,user!!.Username,message)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendNotification(receiverId: String?, username: String, message: String) {

        var ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val token: Token? = snap.getValue(Token::class.java)
                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_launcher,
                        "$username: $message",
                        "New Message",
                        userIdVisit
                    )
                    val sender = Sender(data,token!!.token.toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>{
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if(response.code() == 200){
                                    if(response.body()!!.success != 1){
                                        Toast.makeText(this@MessageChatActivity,"Failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data!=null && data.data!= null) {

            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Please wait ... ")
            loadingBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("ChatImages")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task->
                            if(task.isSuccessful){

                                loadingBar.dismiss()

                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)

                                //push notifications
                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(UserProfile::class.java)
                                        if(notify){
                                            sendNotification(userIdVisit,user!!.Username,"sent you an image.")
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })

                            }
                        }



                }

            }

        }
    }

    private fun seenMessage(userID : String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                for(snap in snapshot.children){
                    val chat = snap.getValue(Chat::class.java)

                    if((chat!!.receiver == firebaseUser!!.uid) && chat.sender == userID){

                        val hashMap = HashMap<String,Any>()
                        hashMap["isSeen"] = true
                        snap.ref.updateChildren(hashMap)

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }
}