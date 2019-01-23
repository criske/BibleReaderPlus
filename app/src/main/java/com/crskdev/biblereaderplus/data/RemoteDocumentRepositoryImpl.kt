/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data

import com.crskdev.biblereaderplus.domain.entity.ModifiedAt
import com.crskdev.biblereaderplus.domain.entity.RemoteVerset
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 02.12.2018.
 */
class RemoteDocumentRepositoryImpl : RemoteDocumentRepository {

    private val database by lazy {
        FirebaseDatabase.getInstance().apply {
            //todo: offline support on prod
            // setPersistenceEnabled(true)
        }
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    //TODO: FIND A WAY IN DOX TO FILTER BY START/END WITH BECAUSE WHAT I'M DOING HERE FOR NOW IS COMPLETELY DUM!
    //(getting all the keys and then filter them locally)
    override fun getAllFavorites(): List<RemoteVerset> {
        val refVersets = ref("versets")
        val ids = refVersets.child("ids")
            .getValueSync()
            .children
            .map { it.key!! to it.value.toString() }
        val refVersetTags = refVersets.child("tags")
        return ids.map { v ->
            val tagIds = refVersetTags
                .ref
                .getValueSync()
                .children
                .filter { it.key?.contains("_${v.first}") == true }
                .map { it.key!!.split("_").first() }
            RemoteVerset(v.first.toInt(), ModifiedAt(v.second), tagIds)
        }
    }

    override fun getAllTags(): List<Tag> {
        val snapshot = ref("tags").getValueSync()
        return mutableListOf<Tag>().apply {
            for (c in snapshot.children) {
                val id = c.key ?: throw IllegalAccessException("Invalid key for tag snapshot $c")
                c.getValue(RemoteTag::class.java)?.let {
                    add(Tag(id, it.name, it.color))
                }
            }
        }
    }

    override fun favoriteAction(versetId: Int, add: Boolean, modifiedAt: ModifiedAt) {
        val versetsRef = ref("versets").child("ids")
            .child(versetId.toString())
        if (add) {
            versetsRef.setValue(modifiedAt.date)
        } else {
            versetsRef.removeValue()
            //cascade on delete
            ref("versets")
                .child("tags")
                .ref
                .getValueSync()
                .children
                .forEach {
                    if (it.key?.contains("_$versetId") == true)
                        it.ref.removeValue()
                }
        }
    }

    override fun tagFavoriteVerset(add: Boolean, versetId: Int, tagId: String, modifiedAt: ModifiedAt) {
        val compoundKey = "${tagId}_$versetId"
        val tagVersetsRef = ref("versets").child("tags")
            .child(compoundKey)
        if (add) {
            favoriteAction(versetId, true, modifiedAt)
            tagVersetsRef.setValue(true)
        } else {
            tagVersetsRef.removeValue()
        }
    }


    override fun tagDelete(id: String) {
        ref("tags").child(id).removeValue()
        //cascade on delete
        ref("versets")
            .child("tags")
            .ref
            .getValueSync()
            .children
            .forEach {
                if (it.key?.contains("${id}_") == true)
                    it.ref.removeValue()
            }
    }

    override fun tagRename(id: String, newName: String) {
        ref("tags").child(id).child("name").setValue(newName)
    }

    override fun tagColor(id: String, color: String) {
        ref("tags").child(id).child("color").setValue(color)
    }

    override fun tagCreate(vararg newTags: Tag): List<String> {
        if (newTags.isEmpty())
            throw IllegalArgumentException("No tag(s) to crate provided")
        val tagsRef = ref("tags")
        val ids = mutableListOf<String>()
        newTags.forEach {
            require(it.id.isNotEmpty())
            tagsRef.child(it.id).apply { ids.add(it.id) }.setValue(RemoteTag(it.name, it.color))
        }
        return ids
    }

    private fun ref(location: String): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid ?: throw Exception("User is not signed in")
        return database.getReference("users/$userId/$location")
    }

    private fun DatabaseReference.getValueSync(): DataSnapshot {
        val latch = CountDownLatch(1)
        var snap: DataSnapshot? = null
        val listener = object : ValueEventListener {
            override fun onCancelled(err: DatabaseError) {
                throw  err.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                snap = dataSnapshot
                latch.countDown()
            }
        }
        this.addListenerForSingleValueEvent(listener)
        latch.await(15, TimeUnit.SECONDS)
        this.removeEventListener(listener)
        return snap ?: throw IllegalAccessException("Could not get db snapshot. Timeout!")
    }

}

class RemoteTag(var name: String = "", var color: String = "#ffffff")
