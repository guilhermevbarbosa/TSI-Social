package com.example.tsisocialapp.views.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import com.example.tsisocialapp.R
import com.example.tsisocialapp.db.AppDatabase
import com.example.tsisocialapp.model.Comment
import com.example.tsisocialapp.model.Post
import com.example.tsisocialapp.model.PostRoom
import com.example.tsisocialapp.utils.convertSnapshotToCommentList
import com.example.tsisocialapp.utils.convertSnapshotToPost
import com.example.tsisocialapp.utils.getCurrentUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.activity_posts_in_category.*
import kotlinx.android.synthetic.main.comentario_card.view.*
import java.time.LocalDateTime

class PostActivity : AppCompatActivity() {
    var database: DatabaseReference? = null
    var postSelecionado: String? = null

    var actualPostToRoom: PostRoom? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        postSelecionado = intent.getStringExtra("post")

        configurarFirebase()
        getComments()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.favorite, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.favoriteBtn){
            salvarPostOffline(actualPostToRoom!!)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun configurarFirebase(){
        database = FirebaseDatabase.getInstance().reference.child("posts").child(postSelecionado.toString())

        val vEvListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                convertSnapshotToPost(snapshot)
                refreshUI(convertSnapshotToPost(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PostActivity, "Erro de servidor", Toast.LENGTH_LONG).show()
                Log.e("PostListActivity", "configurarFirebase", error.toException())
            }
        }

        database?.addValueEventListener(vEvListener)
    }

    fun getComments(){
        val commentNode = database?.child("comentarios")

        val vEvListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                convertSnapshotToCommentList(snapshot)
                refreshComments(convertSnapshotToCommentList(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PostActivity, "Erro de servidor", Toast.LENGTH_LONG).show()
                Log.e("PostListActivity", "configurarFirebase", error.toException())
            }
        }

        commentNode?.addValueEventListener(vEvListener)
    }

    fun refreshComments(comments: List<Comment>){
        comentarios.removeAllViews()

        comments.forEach {
            val card = layoutInflater.inflate(R.layout.comentario_card, comentarios, false)

            card.txtUser.text = it.email
            card.txtComment.text = it.comment
            card.txtData.text = it.timestamp

            comentarios.addView(card)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshUI(post: Post){
        tvTitulo.text = post.title
        tvTexto.text = post.text
        tvUserData.text = post.user
        tvTimestamp.text = post.timestamp
        tvLikes.text = post.likes.toString()

        if (post.image!!.isNotEmpty()){
            Picasso
                .get()
                .load(post.image)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_image_24)
                .into(ivImage)
        }

        likeBtn.setOnClickListener {
            database?.child("likes")?.setValue(++post.likes)
        }

        btnComment.setOnClickListener {
            novoComentario()
        }

        actualPostToRoom = PostRoom(post.id!!, post.user, post.timestamp, post.title, post.text, post.category)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun novoComentario(){
        val campoTexto = EditText(this)
        campoTexto.hint = "Comentário"

        val uid = getCurrentUser()?.uid
        val email = getCurrentUser()?.email
        val currentTime = LocalDateTime.now()

        AlertDialog.Builder(this)
            .setTitle("Comentário")
            .setView(campoTexto)
            .setPositiveButton("Inserir"){ dialog, button ->
                val comentario = Comment(user_uid = uid.toString(), email = email.toString(), comment = campoTexto.text.toString(), timestamp = currentTime.toString())

                val novaEntrada = database?.child("comentarios")?.push()
                comentario.id = novaEntrada?.key

                novaEntrada?.setValue(comentario)
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun salvarPostOffline(post: PostRoom){
        Thread{
            val db = Room.databaseBuilder(this, AppDatabase::class.java, "UserDb").build()

            val count = db.PostRoomDao().countIfExists(post.id)

            if(count == 0){
                db.PostRoomDao().inserir(post)
            }
        }.start()

        Toast.makeText(this@PostActivity, "Post salvo offline com sucesso", Toast.LENGTH_SHORT).show()
    }
}