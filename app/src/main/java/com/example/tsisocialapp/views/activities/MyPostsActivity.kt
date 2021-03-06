package com.example.tsisocialapp.views.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.tsisocialapp.R
import com.example.tsisocialapp.model.Post
import com.example.tsisocialapp.utils.convertSnapshotToPostList
import com.example.tsisocialapp.utils.getCurrentUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_post_list.*
import kotlinx.android.synthetic.main.options_card.view.*

class MyPostsActivity : AppCompatActivity() {
    var database: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)
        configurarFirebase()
    }

    private fun configurarFirebase(){
        database = FirebaseDatabase.getInstance().reference.child("posts")

        val vEvListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                refreshUI(convertSnapshotToPostList(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyPostsActivity, "Erro de servidor", Toast.LENGTH_LONG).show()
                Log.e("PostListActivity", "configurarFirebase", error.toException())
            }
        }

        database?.addValueEventListener(vEvListener)
    }

    fun refreshUI(posts: List<Post>){
        container.removeAllViews()

        val uid = getCurrentUser()!!.uid

        posts.forEach {
            val cardPost = layoutInflater.inflate(R.layout.options_card, container, false)

            if (it.user_uid == uid){
                cardPost.txtBtn.text = it.title

                cardPost.setOnClickListener {post ->
                    val intent = Intent(this, PostActivity::class.java)
                    intent.putExtra("post", it.id)

                    startActivity(intent)
                }

                container.addView(cardPost)
            }
        }
    }
}