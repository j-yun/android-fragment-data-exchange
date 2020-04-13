package net.owlfamily.android.dataexchange

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.BehaviorSubject
import kotlin.collections.HashMap





class DataExchangeViewModel :  ViewModel {
    constructor():super()

    private val dataMap:HashMap<String, BehaviorSubject<DataPack>> = HashMap()

    fun findDataPack(id:String): BehaviorSubject<DataPack>? {
        @Suppress("UNCHECKED_CAST")
        return dataMap[id]
    }

    fun getOrCreateDataPack(id:String): BehaviorSubject<DataPack> {
        @Suppress("UNCHECKED_CAST")
        var result = dataMap[id]
        if(result == null){
            result = BehaviorSubject.createDefault(DataPack())
            dataMap[id] = result
        }

        return result
    }

    fun <T> saveItem(callerId:String, requestId: String, itemState:Int, data:T?) {
        @Suppress("UNCHECKED_CAST")
        val resultDataPackSubject = getOrCreateDataPack(callerId)

        val resultDataPack = resultDataPackSubject.value!!
        val item = resultDataPack.getOrCreateItem<T>(requestId = requestId)
        item.data = data
        item.state = itemState

        // Caller 가 살아있는 경우를 대비하여 무조건 onNext 를 호출한다.
        resultDataPackSubject.onNext(resultDataPack)
    }
}


