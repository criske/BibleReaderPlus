package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.entity.Read

/**
 * Created by Cristian Pela on 14.11.2018.
 */
interface ContentInteractor {

    suspend fun request(query: String? = null): List<Read.Content>
}

class ContentInteractorImpl: ContentInteractor{
    override suspend fun request(query: String?): List<Read.Content> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
