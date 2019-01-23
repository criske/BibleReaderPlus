/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data.internal.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Created by Cristian Pela on 21.01.2019.
 */
@Dao
interface TagDAO {

    @Insert
    fun insert(tags: List<TagDB>)

    @Query("SELECT * FROM ${TableNames.TAGS}")
    fun observeTags(): LiveData<List<TagDB>>

    @Query("SELECT * FROM ${TableNames.TAGS}")
    fun getTags(): List<TagDB>

    @Query("DELETE FROM ${TableNames.TAGS} WHERE id=:id")
    fun delete(id: String)

    @Update
    fun update(tag: TagDB)

    @Insert
    fun addTagToFavorites(versetTags: List<VersetTagDB>)

    @Query("DELETE FROM ${TableNames.VERSET_TAGS} WHERE tagId=:tagId AND versetId=:versetId")
    fun removeTagFromFavorites(tagId: String, versetId: Int)

    @Query("DELETE FROM ${TableNames.VERSET_TAGS} WHERE versetId=:versetId")
    fun removeAllTagsFromFavorites(versetId: Int)

    @Query(
        """
      SELECT t.* FROM ${TableNames.TAGS} as t, ${TableNames.VERSET_TAGS} as vt
        WHERE t.id = vt.tagId AND vt.versetId = :versetId
    """
    )
    fun getTagsForFavorite(versetId: Int): List<TagDB>

    @Query("SELECT * FROM ${TableNames.TAGS} WHERE id = :id")
    fun getTag(id: String): TagDB

}