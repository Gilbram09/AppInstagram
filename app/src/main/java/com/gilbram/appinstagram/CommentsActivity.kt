package com.gilbram.appinstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gilbram.appinstagram.Adapter.CommentAdapter
import com.gilbram.appinstagram.Model.Comment
import com.gilbram.appinstagram.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comments.*
import java.util.HashMap

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser : FirebaseUser? = null
    private var commentAdapter : CommentAdapter? = null
    private var commentList: MutableList<Comment>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView: RecyclerView? = null
        recyclerView = findViewById(R.id.recyler_comment)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this,commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComment()
        getPostImageComment()

        txtpost_comments.setOnClickListener {
            if (add_comment!!.text.toString() == "") {
                Toast.makeText(this,"Please Write Comment First" ,Toast.LENGTH_SHORT).show()
            }
            else
            {
                addComment()
            }
        }
    }

    private fun getPostImageComment() {
        val postCommentRef = FirebaseDatabase.getInstance().reference
            .child("Posts").child(postId).child("postimage")

        postCommentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val image = p0.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun readComment() {
       var commentRef = FirebaseDatabase.getInstance().reference
           .child("Comments").child(postId)

        commentRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (snapshot in p0.children) {
                        val comment = snapshot.getValue(Comment::class.java)

                        commentList!!.add(comment!!)
                    }

                    commentAdapter!!.notifyDataSetChanged()
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun addComment() {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId!!)

        val commentsMap = HashMap<String,Any>()
        commentsMap["comment"] = add_comment!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentRef.push().setValue(commentsMap)

        add_comment!!.text.clear()
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_comment)
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}