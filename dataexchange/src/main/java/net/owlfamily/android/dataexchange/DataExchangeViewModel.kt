package net.owlfamily.android.dataexchange

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.BehaviorSubject
import kotlin.collections.HashMap

/**
 * parent of all [Archive]s.
 * "in generally", Activity manage one instance of [DataExchangeViewModel].
 */
class DataExchangeViewModel :  ViewModel() {
    private val archiveMap:HashMap<String, BehaviorSubject<Archive>> = HashMap()

    @Synchronized fun findArchive(id:String): BehaviorSubject<Archive>? {
        @Suppress("UNCHECKED_CAST")
        return archiveMap[id]
    }

    /**
     * @param id key of archive
     * @return true if Archive exists for [id]
     */
    @Synchronized fun hasArchive(id:String): Boolean {
        return archiveMap.containsKey(id)
    }

    @Synchronized fun getOrCreateArchive(id:String): BehaviorSubject<Archive> {
        @Suppress("UNCHECKED_CAST")
        var result = archiveMap[id]
        if(result == null){
            result = BehaviorSubject.createDefault(Archive())
            archiveMap[id] = result
        }

        return result
    }

    @Synchronized fun removeArchive(id:String): Boolean {
        if(hasArchive(id)){
            archiveMap[id]?.onComplete()
            archiveMap.remove(id)
            return true
        }
        return false
    }

    /**
     * save item to archive
     */
    @Synchronized fun <T> saveItem(ownerId:String, requestId: String, itemState: Archive.Item.State, data:T?) {
        @Suppress("UNCHECKED_CAST")
        val resultArchiveSubject = getOrCreateArchive(ownerId)

        val resultArchive = resultArchiveSubject.value!!
        resultArchive.setItem(requestId = requestId, itemState = itemState, data = data)

        // Caller 가 살아있는 경우를 대비하여 무조건 onNext 를 호출한다.
        resultArchiveSubject.onNext(resultArchive)
    }
}


