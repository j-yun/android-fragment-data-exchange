package net.owlfamily.android.dataexchange

import android.os.Bundle
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class DataExchangeHelper {
    companion object {
        fun createRandomID():String {
            return UUID.randomUUID().toString()
        }

        const val dataExchangeBundleId = "dataExchangeBundleId"
        const val dataExchangeUniqueId = "dataExchangeUniqueId"
        const val dataExchangeOwnerId = "dataExchangeOwnerId"
        const val dataExchangeRequestId = "dataExchangeRequestId"

        fun getOrCreateExchangeBundle(fromBundle: Bundle): Bundle {
            var result = fromBundle.getBundle(dataExchangeBundleId)
            if(result == null){
                result = Bundle()
                fromBundle.putBundle(dataExchangeBundleId, result)
            }
            return result
        }

        fun getOrCreateExchangeBundleForRequest(fromBundle: Bundle, parentId:String, requestId:String, instanceUniqueId:String? = null): Bundle {
            val bundle = getOrCreateExchangeBundle(fromBundle)
            bundle.putString(dataExchangeOwnerId, parentId)
            bundle.putString(dataExchangeRequestId, requestId)
            instanceUniqueId?.let { id ->
                bundle.putString(dataExchangeUniqueId, id)
            }
            return bundle
        }
    }
}

