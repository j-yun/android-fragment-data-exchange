package net.owlfamily.android.dataexchange

import android.os.Bundle
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class DataExchangeHelper {
    companion object {
        /** just create unique id */
        fun createRandomID():String {
            return UUID.randomUUID().toString()
        }

        const val dataExchangeBundleId = "dataExchangeBundleId"
        const val dataExchangeUniqueId = "dataExchangeUniqueId"
        const val dataExchangeCallerId = "dataExchangeCallerId"
        const val dataExchangeArchiveItemId = "dataExchangeArchiveItemId"

        /**
         * get or create sub bundle in bundle for 'DataExchange'.
         */
        fun getOrCreateExchangeBundle(fromBundle: Bundle): Bundle {
            var result = fromBundle.getBundle(dataExchangeBundleId)
            if(result == null){
                result = Bundle()
                fromBundle.putBundle(dataExchangeBundleId, result)
            }
            return result
        }

        /**
         * bundle creation utility.
         */
        fun getOrCreateExchangeBundleForRequest(fromBundle: Bundle, ownerId:String, archiveItemId:String, instanceUniqueId:String? = null): Bundle {
            val bundle = getOrCreateExchangeBundle(fromBundle)
            bundle.putString(dataExchangeCallerId, ownerId)
            bundle.putString(dataExchangeArchiveItemId, archiveItemId)
            instanceUniqueId?.let { id ->
                bundle.putString(dataExchangeUniqueId, id)
            }
            return bundle
        }
    }
}

