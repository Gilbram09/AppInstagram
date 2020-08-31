package com.gilbram.appinstagram.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.gilbram.appinstagram.CommentsActivity
import com.gilbram.appinstagram.MainActivity
import com.gilbram.appinstagram.Model.Post
import com.gilbram.appinstagram.Model.User
import com.gilbram.appinstagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext:Context,private val mPost:List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolader>() {

    private var firebaseUser: FirebaseUser? = null
    
    class ViewHolader (@NonNull itemView: View):RecyclerView.ViewHolder(itemView) {
        var profileImage:CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var postImage : ImageView = itemView.findViewById(R.id.post_image_home)
        var likeButton : ImageView = itemView.findViewById(R.id.post_image_like_button)
        var commentButton : ImageView = itemView.findViewById(R.id.post_image_commend_button)
        var saveButton : ImageView = itemView.findViewById(R.id.post_save_btn)
        var userName : TextView = itemView.findViewById(R.id.post_user_name)
        var likes : TextView = itemView.findViewById(R.id.post_like)
        var publisher : TextView = itemView.findViewById(R.id.post_publisher)
        var description : TextView = itemView.findViewById(R.id.post_description)
        var comment : TextView= itemView.findViewById(R.id.post_commend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolader {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout,parent,false)
        return ViewHolader(view)
    }

    override fun onBindViewHolder(holder: ViewHolader, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)
        if (post.getDescription().equals("")) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.getDescription())
        }
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())

        isLikes(post.getPostid(),holder.likeButton)
        numberOfLikes(holder.likes,post.getPostid())

        getTotalComment(holder.comment,post.getPostid())


        holder.likeButton.setOnClickListener{
            if (holder.likeButton.tag == "Like"){
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostid()).child(firebaseUser!!.uid)
                    .setValue(true)
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostid()).child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent (mContext,MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }
        holder.commentButton.setOnClickListener {
            val intencomment = Intent(mContext, CommentsActivity::class.java)
            intencomment.putExtra("postId",post.getPostid())
            intencomment.putExtra("publisherId",post.getPublisher())
                mContext.startActivity(intencomment)
        }
        holder.comment.setOnClickListener {
            val intencomment = Intent(mContext, CommentsActivity::class.java)
            intencomment.putExtra("postId", post.getPostid())
            intencomment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intencomment)
        }
    }

    private fun getTotalComment(comment: TextView, postid: String) {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postid)

        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    comment.text = "view all" + p0.childrenCount.toString() + "comment"
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun numberOfLikes(likes: TextView, postid: String) {
        val likeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    likes.text = p0.childrenCount.toString() + "likes"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        likeRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"

                }
                else
                {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun publisherInfo(profileImage: CircleImageView,
                              userName: TextView,
                              publisher: TextView,
                              publisherID: String) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user?.getUsername()
                    publisher.text = user?.getFullname()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getItemCount(): Int {
        return mPost.size
    }
}