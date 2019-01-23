/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data.internal.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

/**
 * Created by Cristian Pela on 21.01.2019.
 */
@Dao
@TypeConverters(Converters::class)
interface ReadDAO {

    @Insert
    fun insert(reads: List<ReadDB>)

    @Query("UPDATE ${TableNames.READ} SET isFavorite=:favorite, modifiedAt=:modifiedAt WHERE id=:id")
    fun updateFavorite(id: Int, favorite: Boolean, modifiedAt: String): Long

    @Update
    fun updateRead(read: ReadDB)

    @Query("SELECT * FROM ${TableNames.READ} WHERE id=:id")
    fun getRead(id: Int): ReadDB

    @Query("SELECT c.* FROM ${TableNames.READ} c WHERE c.type != 2")
    fun contents(): List<ReadDB>

    @Query("SELECT c.* FROM ${TableNames.READ} c WHERE c.type != 2 AND name LIKE :bookName")
    fun contents(bookName: String): List<ReadDB>

    @Query("SELECT * FROM ${TableNames.READ} ORDER BY id ASC")
    fun observeReads(): DataSource.Factory<Int, ReadDB>

    @Query("SELECT * FROM ${TableNames.READ} WHERE isFavorite = 1 ORDER BY datetime(modifiedAt) DESC")
    fun observeReadsFavorite(): DataSource.Factory<Int, ReadDB>

    @Query(
        """
        SELECT r.* FROM ${TableNames.VERSET_TAGS} vt, ${TableNames.READ} r
        WHERE vt.versetId = r.id AND vt.tagId IN(:tagIds)
        ORDER BY datetime(r.modifiedAt) DESC
    """
    )
    fun observeReadsFavoriteWithTags(tagIds: List<String>): DataSource.Factory<Int, ReadDB>


    @Query(
        """
        SELECT 	v.id, v.bookId, v.chapterId, v.number,
                v.chapterNumber,v.abbreviation,
                v.content,
                v.isFavorite,
                (SELECT group_concat(t.id||'${'$'}'||t.name||'${'$'}'||t.color, '&')
                    FROM ${TableNames.TAGS} t
                    JOIN ${TableNames.VERSET_TAGS} vt ON vt.tagId = t.id
                    WHERE vt.versetId = v.id AND vt.versetId=:id) as tags
        FROM ${TableNames.READ} v WHERE v.id == :id
    """
    )
    fun observeVerset(id: Int): LiveData<VersetDB>

    @Query(
        """
        SELECT 	v.id, v.bookId, v.chapterId, v.number,
                v.chapterNumber,v.abbreviation,
                v.content, v.isFavorite,
                (SELECT group_concat(t.id||'$'||t.name||'$'||t.color, '&')
                    FROM ${TableNames.TAGS} t
                    JOIN ${TableNames.VERSET_TAGS} vt ON vt.tagId = t.id
                    WHERE vt.versetId = v.id AND vt.versetId=:id) as tags
        FROM ${TableNames.READ} v WHERE tags NOTNULL
    """
    )
    fun getVerset(id: Int): VersetDB

}