package net.owlfamily.android.dataexchange

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

/**
 * parent of [Item]s.
 * "in generally", Activity or Fragment can have and manage one instance of [Archive].
 */
@Suppress("MemberVisibilityCanBePrivate")
class Archive {
    /** data set of [Item], internal */
    private val innerItemSubjectMap = HashMap<String, BehaviorSubject<Item<*>>>()

    /** data set of [Item] */
    val itemSubjectMap:Map<String, BehaviorSubject<Item<*>>> = innerItemSubjectMap

    /** will notify through this if any item has changes */
    val itemStateChangedSubject: PublishSubject<Pair<String, Item<*>>> = PublishSubject.create()

    fun clear(){
        for(item in innerItemSubjectMap){
            item.value.onComplete()
        }
        innerItemSubjectMap.clear()
    }

    fun <T> getOrCreateItemSubject(archiveItemId:String): BehaviorSubject<Item<T>> {
        return getItemSubject(archiveItemId, true)!!
    }

    fun <T> getItemSubject(archiveItemId:String, createIfNotExists:Boolean = false): BehaviorSubject<Item<T>>? {
        if(hasItemSubject(archiveItemId)){
            @Suppress("UNCHECKED_CAST")
            return innerItemSubjectMap[archiveItemId] as? BehaviorSubject<Item<T>>
        }else if(createIfNotExists){
            innerItemSubjectMap[archiveItemId] = BehaviorSubject.create()
            @Suppress("UNCHECKED_CAST")
            return innerItemSubjectMap[archiveItemId] as BehaviorSubject<Item<T>>
        }
        return null
    }

    fun hasItemSubject(archiveItemId:String): Boolean {
        return innerItemSubjectMap.containsKey(archiveItemId)
    }

    fun removeItemSubject(archiveItemId:String):Boolean {
        if(hasItemSubject(archiveItemId)){
            innerItemSubjectMap[archiveItemId]?.onComplete()
            innerItemSubjectMap.remove(archiveItemId)
            return true
        }
        return false
    }

    fun <T> findItem(archiveItemId:String, removeSubjectIfFound:Boolean = false): Item<T>? {
        var result: Item<T>? = null
        if(innerItemSubjectMap.containsKey(archiveItemId)){
            @Suppress("UNCHECKED_CAST")
            val subject = innerItemSubjectMap[archiveItemId] as BehaviorSubject<Item<T>>
            result = subject.value

            if(removeSubjectIfFound){
                innerItemSubjectMap.remove(archiveItemId)
                subject.onComplete()
            }
        }
        return result
    }

    fun <T> setItem(archiveItemId: String, itemState: Item.State, data:T?) {
        @Suppress("UNCHECKED_CAST")

        val itemSubject = getOrCreateItemSubject<T>(archiveItemId = archiveItemId)
        val item = Item<T>(state = itemState, data = data)
        itemSubject.onNext(item)
        itemStateChangedSubject.onNext(Pair(archiveItemId,item))
    }

    fun hasItem(archiveItemId:String): Boolean {
        if(hasItemSubject(archiveItemId)){
            if(innerItemSubjectMap[archiveItemId]?.value != null){
                return true
            }
        }

        return false
    }

    fun isItemNullOrUnknownState(archiveItemId:String): Boolean {
        if(!hasItemSubject(archiveItemId)) return true
        val item = innerItemSubjectMap[archiveItemId]?.value ?: return true
        if(item.state == Item.State.Unknown){
            return true
        }

        return false
    }

    /** Child of [Archive]. Each item has state, data */
    open class Item<T>(open var state: State = State.Unknown, open var data:T? = null) {
        data class State(val code:Int, val message:String?=null) : Serializable {
            companion object {
                val Unknown = State(-1)
            }
        }

        val isUnknownState:Boolean get() = state == State.Unknown
    }
}
