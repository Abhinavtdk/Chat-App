package com.example.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.model.Chat
import com.example.chatapp.R
import com.example.chatapp.ViewFullImageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatListAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder?>() {

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profile_chat_list: CircleImageView? = null
        var show_text_message: TextView? = null
        var show_image_left: ImageView? = null
        var message_status: TextView? = null
        var show_image_right: ImageView? = null

        init {
            profile_chat_list = itemView.findViewById(R.id.profile_chat_list)
            show_text_message = itemView.findViewById(R.id.show_text_message)
            show_image_left = itemView.findViewById(R.id.show_image_left)
            message_status = itemView.findViewById(R.id.message_status)
            show_image_right = itemView.findViewById(R.id.show_image_right)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == 0) {  //0 for left message and 1 for right
            val view =
                LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false)
            return ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false)
            return ViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val chat: Chat = mChatList[position]

        Picasso.get().load(imageUrl).into(holder.profile_chat_list)

        if (chat.message == "sent you an image." && chat.url != "") {
            //if it's an image
            if (chat.sender == firebaseUser!!.uid) {

                holder.show_text_message!!.visibility = View.GONE
                holder.show_image_right!!.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.show_image_right)

                holder.show_image_right!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )

                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want")
                    builder.setItems(options,DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            0 -> {
                                val intent = Intent(mContext, ViewFullImageActivity::class.java)
                                intent.putExtra("url",chat.url)
                                mContext.startActivity(intent)
                            }
                            1 -> {
                                deleteSendMessage(position,holder)
                            }
                        }
                    })
                    builder.show()

                }

            } else if (chat.sender != firebaseUser!!.uid) {

                holder.show_text_message!!.visibility = View.GONE
                holder.show_image_left!!.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.show_image_left)

                holder.show_image_left!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Cancel"
                    )

                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want")
                    builder.setItems(options,DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            0 -> {
                                val intent = Intent(mContext, ViewFullImageActivity::class.java)
                                intent.putExtra("url",chat.url)
                                mContext.startActivity(intent)
                            }

                        }
                    })
                    builder.show()

                }

            }
        } else {

            holder.show_text_message!!.text = chat.message

            if(firebaseUser!!.uid == chat.sender){
                holder.show_text_message!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete Image",
                        "Cancel"
                    )

                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want")
                    builder.setItems(options,DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            0 -> {
                                deleteSendMessage(position,holder)
                            }
                        }
                    })
                    builder.show()

                }
            }
        }

        //message status
        if (position == mChatList.size - 1) {

            if (chat.isSeen) {
                holder.message_status!!.text = "Seen"

                if (chat.message == "sent you an image." && chat.url != "") {
                    val layoutParams: RelativeLayout.LayoutParams? =
                        holder.message_status!!.layoutParams as RelativeLayout.LayoutParams
                    layoutParams!!.setMargins(0, 245, 10, 0)
                    holder.message_status!!.layoutParams = layoutParams
                }
            } else {
                holder.message_status!!.text = "Sent"

                if (chat.message == "sent you an image." && chat.url != "") {
                    val layoutParams: RelativeLayout.LayoutParams? =
                        holder.message_status!!.layoutParams as RelativeLayout.LayoutParams
                    layoutParams!!.setMargins(0, 245, 10, 0)
                    holder.message_status!!.layoutParams = layoutParams
                }
            }
        } else {
            holder.message_status!!.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun getItemViewType(position: Int): Int {


        return if (mChatList[position].sender == firebaseUser!!.uid) {
            1
        } else {
            0
        }
    }

    private fun deleteSendMessage(position: Int,holder: ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).messageId!!)
            .removeValue()
            .addOnCompleteListener { task->
                if(task.isSuccessful){
                    Toast.makeText(holder.itemView.context,"Message Deleted",Toast.LENGTH_SHORT).show()
                }
            }
    }
}