package net.owlfamily.android.dataexchange

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.BehaviorSubject
import kotlin.collections.HashMap


class DataExchangeViewModel :  ViewModel() {
    private val dataMap:HashMap<String, BehaviorSubject<Archive>> = HashMap()

    @Synchronized fun findDataPack(id:String): BehaviorSubject<Archive>? {
        @Suppress("UNCHECKED_CAST")
        return dataMap[id]
    }

    @Synchronized fun hasDataPack(id:String): Boolean {
        return dataMap.containsKey(id)
    }

    @Synchronized fun getOrCreateDataPack(id:String): BehaviorSubject<Archive> {
        @Suppress("UNCHECKED_CAST")
        var result = dataMap[id]
        if(result == null){
            result = BehaviorSubject.createDefault(Archive())
            dataMap[id] = result
        }

        return result
    }

    @Synchronized fun removeDataPack(id:String): Boolean {
        if(hasDataPack(id)){
            dataMap[id]?.onComplete()
            dataMap.remove(id)
            return true
        }
        return false
    }

    @Synchronized fun <T> saveItem(ownerId:String, requestId: String, itemState: Archive.Item.State, data:T?) {
        @Suppress("UNCHECKED_CAST")
        val resultDataPackSubject = getOrCreateDataPack(ownerId)

        val resultDataPack = resultDataPackSubject.value!!
        resultDataPack.setItem(requestId = requestId, itemState = itemState, data = data)

        // Caller 가 살아있는 경우를 대비하여 무조건 onNext 를 호출한다.
        resultDataPackSubject.onNext(resultDataPack)
    }
}


