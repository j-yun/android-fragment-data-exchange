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

    fun <T> getOrCreateItemSubject(requestId:String): BehaviorSubject<Item<T>> {
        return getItemSubject(requestId, true)!!
    }

    fun <T> getItemSubject(requestId:String, createIfNotExists:Boolean = false): BehaviorSubject<Item<T>>? {
        if(hasItemSubject(requestId)){
            @Suppress("UNCHECKED_CAST")
            return innerItemSubjectMap[requestId] as? BehaviorSubject<Item<T>>
        }else if(createIfNotExists){
            innerItemSubjectMap[requestId] = BehaviorSubject.create()
            @Suppress("UNCHECKED_CAST")
            return innerItemSubjectMap[requestId] as BehaviorSubject<Item<T>>
        }
        return null
    }

    fun hasItemSubject(requestId:String): Boolean {
        return innerItemSubjectMap.containsKey(requestId)
    }

    fun removeItemSubject(requestId:String):Boolean {
        if(hasItemSubject(requestId)){
            innerItemSubjectMap[requestId]?.onComplete()
            innerItemSubjectMap.remove(requestId)
            return true
        }
        return false
    }

    fun <T> findItem(requestId:String, removeSubjectIfFound:Boolean = false): Item<T>? {
        var result: Item<T>? = null
        if(innerItemSubjectMap.containsKey(requestId)){
            @Suppress("UNCHECKED_CAST")
            val subject = innerItemSubjectMap[requestId] as BehaviorSubject<Item<T>>
            result = subject.value

            if(removeSubjectIfFound){
                innerItemSubjectMap.remove(requestId)
                subject.onComplete()
            }
        }
        return result
    }

    fun <T> setItem(requestId: String, itemState: Item.State, data:T?) {
        @Suppress("UNCHECKED_CAST")

        val itemSubject = getOrCreateItemSubject<T>(requestId = requestId)
        val item = Item<T>(state = itemState, data = data)
        itemSubject.onNext(item)
        itemStateChangedSubject.onNext(Pair(requestId,item))
    }

    fun hasItem(requestId:String): Boolean {
        if(hasItemSubject(requestId)){
            if(innerItemSubjectMap[requestId]?.value != null){
                return true
            }
        }

        return false
    }

    fun isItemNullOrUnknownState(requestId:String): Boolean {
        if(!hasItemSubject(requestId)) return true
        val item = innerItemSubjectMap[requestId]?.value ?: return true
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
