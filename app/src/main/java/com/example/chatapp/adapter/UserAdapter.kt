package com.example.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MessageChatActivity
import com.example.chatapp.model.UserProfile
import com.example.chatapp.R
import com.example.chatapp.VisitUserProfile
import com.example.chatapp.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    mContext: Context,
    mUsersList: List<UserProfile>,
    isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val mContext: Context
    private val mUsersList: List<UserProfile>
    private val isChatCheck: Boolean
    private var lastMsg: String = ""

    init {
        this.mContext = mContext
        this.mUsersList = mUsersList
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_search_itemlayout,parent,false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user : UserProfile? = mUsersList[position]
        holder.userNameText.text = user!!.Username
        Picasso.get().load(user.profile).placeholder(R.drawable.profile_img).into(holder.profileImageView)

        if(isChatCheck){
            retrieveLastMessage(user.uid,holder.lastMessagetxt)

            if(user.status == "online"){
                holder.statusOnline.visibility = View.VISIBLE
                holder.statusOffline.visibility = View.GONE
            } else {
                holder.statusOnline.visibility = View.GONE
                holder.statusOffline.visibility = View.VISIBLE
            }
        }
        else{
            holder.lastMessagetxt.visibility = View.GONE
            holder.statusOffline.visibility = View.GONE
            holder.statusOnline.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )

            val builder: AlertDialog.Builder= AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options,DialogInterface.OnClickListener{dialog, position ->
                if(position == 0){
                    val intent = Intent(mContext,MessageChatActivity::class.java)
                    intent.putExtra("visit_id",user.uid)
                    mContext.startActivity(intent)
                }
                if(position == 1){
                    val intent = Intent(mContext,VisitUserProfile::class.java)
                    intent.putExtra("visit_id",user.uid)
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }
    }

    private fun retrieveLastMessage(chatUserID: String, lastMessagetxt: TextView) {
        lastMsg = "defaultMsg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val chat: Chat? = snap.getValue(Chat::class.java)

                    if(firebaseUser!=null && chat!=null){
                        if((chat.receiver == firebaseUser!!.uid && chat.sender == chatUserID) || (chat.receiver == chatUserID && chat.sender == firebaseUser!!.uid)){
                            lastMsg = chat.message
                        }
                    }
                }
                when(lastMsg){
                    "defaultMsg" -> lastMessagetxt.text = "No Message"
                    "sent you an image." -> lastMessagetxt.text = "Image sent"
                    else -> lastMessagetxt.text = lastMsg
                }
                lastMsg = "defaultMsg"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getItemCount(): Int {
        return mUsersList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameText: TextView
        var profileImageView: CircleImageView
        var statusOnline: CircleImageView
        var statusOffline: CircleImageView
        var lastMessagetxt: TextView

        init {
            userNameText = itemView.findViewById(R.id.username_search)
            profileImageView = itemView.findViewById(R.id.profile_img_search)
            statusOnline = itemView.findViewById(R.id.status_online_img)
            statusOffline = itemView.findViewById(R.id.status_offline_img)
            lastMessagetxt = itemView.findViewById(R.id.last_message)
        }
    }

}