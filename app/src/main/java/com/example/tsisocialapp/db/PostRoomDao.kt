package com.example.tsisocialapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tsisocialapp.model.PostRoom

@Dao
interface PostRoomDao {

    @Insert
    fun inserir(post: PostRoom)

    @Query(value = "select * from PostRoom")
    fun listarTodas(): List<PostRoom>

    @Query("select * FROM PostRoom WHERE id IN (:postId)")
    fun getOne(postId: String): PostRoom

    @Query("select count(*) FROM PostRoom WHERE id IN (:postId)")
    fun countIfExists(postId: String): Int

    @Query("delete FROM PostRoom WHERE id = :postId")
    fun deleteOne(postId: String)
}