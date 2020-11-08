package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.notifications.Token
import com.example.chatapp.model.ChatList
import com.example.chatapp.model.UserProfile
import com.example.chatapp.R
import com.example.chatapp.adapter.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId


/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<UserProfile>? = null
    private var userChatList : List<ChatList>? = null
    lateinit var recyclerViewChatList: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerViewChatList = view.findViewById(R.id.recyclerview_chats_fragment)
        recyclerViewChatList.setHasFixedSize(true)
        recyclerViewChatList.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                (userChatList as ArrayList).clear()

                for(snap in snapshot.children){

                    val chatList = snap.getValue(ChatList::class.java)
                    (userChatList as ArrayList).add(chatList!!)

                }
                retrieveChatList()

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private fun retrieveChatList(){

        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for(snap in snapshot.children){

                    val user = snap.getValue(UserProfile::class.java)

                    for(eachChatList in userChatList!!){
                        if(user!!.uid == eachChatList.id){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }

                userAdapter = UserAdapter(context!!,(mUsers as ArrayList<UserProfile>),true)

                recyclerViewChatList.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

}