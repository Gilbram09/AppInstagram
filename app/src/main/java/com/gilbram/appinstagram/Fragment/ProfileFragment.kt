package com.gilbram.appinstagram.Fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gilbram.appinstagram.AccountSettingsActivity
import com.gilbram.appinstagram.Adapter.MyImageAdapter
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
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {
    private lateinit var profileId : String
    private lateinit var firebaseUser :FirebaseUser

    var postListGrid : MutableList<Post>? = null
    var myImageAdapter : MyImageAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewProfile = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        var recyclerViewUploadImage: RecyclerView? = null
        recyclerViewUploadImage = viewProfile.findViewById(R.id.recycler_upload_picimage)
        recyclerViewUploadImage?.setHasFixedSize(true)
        var LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewUploadImage?.layoutManager = LinearLayoutManager

        postListGrid = ArrayList()
        myImageAdapter = context?.let { MyImageAdapter(it,postListGrid as ArrayList<Post>) }
        recyclerViewUploadImage?.adapter = myImageAdapter

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if (pref !=null) {
            this.profileId = pref.getString("profileId", "none")!!
        }
        if (profileId == firebaseUser.uid){
            view?.btn_edit_account?.text = "Edit Profile"

        }else if (profileId != firebaseUser.uid){
            cekFollowAndFollowingButtonStatus()

        }

        viewProfile.btn_edit_account.setOnClickListener{
            val getButtonText = view?.btn_edit_account?.text.toString()

            when{
                getButtonText == "Edit Profile" -> startActivity(Intent(context,AccountSettingsActivity::class.java))
                getButtonText == "Follow" ->{
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it.toString()).setValue(true)
                    }
                }
                getButtonText == "Following" ->{
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it.toString()).removeValue()
                    }
                }
            }
        }

        getFollowers()
        getFollowing()
        UserInfo()
        myPost()
        return viewProfile
    }

    private fun myPost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    (postListGrid as ArrayList<Post>).clear()

                    for (snapshot in p0.children){
                        val post = snapshot.getValue(Post::class.java)
                        if (post?.getPublisher().equals(profileId)){
                            (postListGrid as ArrayList<Post>).add(post!!)
                        }

                        Collections.reverse(postListGrid)
                        myImageAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun UserInfo() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(profileId)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(view!!.profile_image)
                    view?.profile_fragment_username?.text = user?.getUsername()
                    view?.txt_full_namaProfile?.text = user?.getFullname()
                    view?.txt_bio_profile?.text = user?.getBio()
                }
            }

        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.txt_totalFollowing?.text = p0.childrenCount.toString()
                }

            }

        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    view?.txt_totalFollowers?.text= p0.childrenCount.toString()
                }
            }
        })
    }

    private fun cekFollowAndFollowingButtonStatus() {
        val follwingref = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
        }
        if (follwingref != null){
            follwingref.addValueEventListener(object  : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()){
                        view?.btn_edit_account?.text = "Following"
                    }else{
                        view?.btn_edit_account?.text = "Follow"
                    }
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }
}