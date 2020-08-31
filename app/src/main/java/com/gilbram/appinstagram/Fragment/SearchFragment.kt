package com.gilbram.appinstagram.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gilbram.appinstagram.Adapter.UserAdapter
import com.gilbram.appinstagram.Model.User
import com.gilbram.appinstagram.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment() {

    private var recyclerView : RecyclerView? = null
    private var userAdapter : UserAdapter? = null
    private var myUser : MutableList<User>? = null

       override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
           recyclerView = view.findViewById(R.id.search_recyclerview)
           recyclerView?.setHasFixedSize(true)
           recyclerView?.layoutManager = LinearLayoutManager(context)

           myUser = ArrayList()
           userAdapter = context?.let { UserAdapter(it,myUser as ArrayList<User>,true)}
           recyclerView?.adapter = userAdapter

           view.search_edittext.addTextChangedListener(object : TextWatcher{
               override fun afterTextChanged(s: Editable?) {
               }

               override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){
               }

               override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                   if (view.search_edittext.toString() == ""){
                   }else{
                       recyclerView?.visibility = View.VISIBLE
                       getUsers()

                       searchUser(s.toString().toLowerCase())
                   }
               }
           })
           return view
    }
    private fun searchUser(input: String) {
        val query= FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("fullname")
            .startAt(input).endAt(input + "\uf8ff")

        query.addValueEventListener(object  : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                myUser?.clear()
                for (snapshot in p0.children){
                    val user= snapshot.getValue(User::class.java)
                    if (user != null){
                        myUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
        })
    }

    private fun getUsers() {
        val userRef = FirebaseDatabase.getInstance()
            .getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                if (view?.search_edittext?.toString() == ""){

                    for (snapshot in p0.children){
                        val user= snapshot.getValue(User::class.java)
                        if (user != null){
                            myUser?.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }
        })
    }
}