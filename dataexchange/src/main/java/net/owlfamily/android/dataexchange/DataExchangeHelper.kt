package net.owlfamily.android.dataexchange

import android.os.Bundle
import java.util.*

class DataExchangeHelper {
    companion object {
        fun createRandomID():String {
            return UUID.randomUUID().toString()
        }

        const val dataExchangeBundleId = "dataExchangeBundleId"
        const val dataExchangeId = "dataExchangeId"
        const val dataExchangeCallerId = "dataExchangeCallerId"
        const val dataExchangeRequestId = "dataExchangeRequestId"

        fun getOrCreateExchangeBundle(fromBundle: Bundle): Bundle {
            var result = fromBundle.getBundle(dataExchangeBundleId)
            if(result == null){
                result = Bundle()
                fromBundle.putBundle(dataExchangeBundleId, result)
            }
            return result
        }

        fun getOrCreateExchangeBundleForRequest(fromBundle: Bundle, parentId:String, requestId:String): Bundle {
            val bundle = getOrCreateExchangeBundle(fromBundle)
            bundle.putString(dataExchangeCallerId, parentId)
            bundle.putString(dataExchangeRequestId, requestId)
            return bundle
        }
    }
}

