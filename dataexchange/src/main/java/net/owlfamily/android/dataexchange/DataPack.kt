package net.owlfamily.android.dataexchange

class DataPack {
    private val innerItemList:ArrayList<Item<*>> = ArrayList()
    val itemList:List<Item<*>> = innerItemList

    fun <T> findItem(requestId:String? = null, removeIfFound:Boolean = false): Item<T>? {
        var result: Item<T>? = null
        for((index,item) in innerItemList.withIndex()){
            @Suppress("UNCHECKED_CAST")
            if(requestId != null && requestId == item.requestId) {
                result = item as? Item<T>
                if(removeIfFound){
                    innerItemList.removeAt(index)
                }
                break
            }
        }
        return result
    }

    fun <T> getOrCreateItem(requestId:String): Item<T> {
        var data = findItem<T>(requestId = requestId)
        if(data == null){
            data = Item(requestId = requestId)
            innerItemList.add(data)
        }

        return data
    }

    data class Item<T>(val requestId:String, var data:T? = null, var state:Int = State.Unknown) {
        object State {
            const val Unknown = -1
            const val OK = 0
            const val Failed = 1
            const val Canceled = 2
        }
    }
}
