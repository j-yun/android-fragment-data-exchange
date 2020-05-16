package net.owlfamily.android.dataexchange

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Suppress("SpellCheckingInspection")
const val ActivityDataExchangerKey = "net.owlfamily.android.dataexchange.ActivityDataExchangerKey"

fun FragmentActivity.getOrCreateArguments(): Bundle {
    var result: Bundle? = intent.getBundleExtra(ActivityDataExchangerKey)
    if(result == null){
        result = Bundle()
        intent.putExtra(ActivityDataExchangerKey, result)
    }
    return result
}

fun FragmentActivity.getDataExchangeArguments(): Bundle? {
    return intent.getBundleExtra(ActivityDataExchangerKey)
}

fun FragmentActivity.getDataExchangeOwnerId(): String? {
    return getDataExchangeArguments()?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeOwnerId
    )
}

fun FragmentActivity.getDataExchangeRequestId(): String? {
    return getDataExchangeArguments()?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeRequestId
    )
}

fun FragmentActivity.getOrCreateDataExchangeUniqueId(): String {
    return getDataExchangeUniqueId(true)!!
}

fun FragmentActivity.getDataExchangeUniqueId(createIfEmpty:Boolean = true): String? {
    var bundle = getDataExchangeArguments()?.getBundle(DataExchangeHelper.dataExchangeBundleId)
    bundle?.getString(DataExchangeHelper.dataExchangeUniqueId)?.let {
        return it
    }
    if(!createIfEmpty) { return null }

    if(bundle == null){
        bundle = Bundle()
        getOrCreateArguments().putBundle(DataExchangeHelper.dataExchangeBundleId, bundle)
    }

    val id = DataExchangeHelper.createRandomID()
    bundle.putString(DataExchangeHelper.dataExchangeUniqueId,id)
    return id
}

fun FragmentActivity.getDataExchangeSubject(ownerId:String? = null): BehaviorSubject<Archive> {
    val vm = getDataExchangeViewModel()
    val ownerId = ownerId ?: getOrCreateDataExchangeUniqueId()
    return vm.getOrCreateDataPack(ownerId)
}

fun FragmentActivity.removeDataExchangeSubject(ownerId:String? = null): Boolean {
    val vm = getDataExchangeViewModel()
    val oId = ownerId ?: getDataExchangeUniqueId(false) ?: return false
    return vm.removeDataPack(oId)
}

fun <T> FragmentActivity.getDataExchangeItemSubject(requestId:String, ownerId:String? = null): BehaviorSubject<Archive.Item<T>> {
    val vm = getDataExchangeViewModel()

    val oId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(oId).value!!
    return dataPack.getOrCreateItemSubject<T>(requestId = requestId)
}

fun FragmentActivity.removeDataExchangeItemSubject(requestId:String, ownerId:String? = null): Boolean {
    val vm = getDataExchangeViewModel()

    val oId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(oId).value!!
    return dataPack.removeItemSubject(requestId = requestId)
}

fun FragmentActivity.hasDataExchangeItem(requestId:String, ownerId:String? = null): Boolean {
    val vm = getDataExchangeViewModel()

    val oId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(oId).value!!
    return dataPack.hasItemSubject(requestId)
}

fun FragmentActivity.getDataExchangeItemStateChangeSubject(ownerId:String? = null): PublishSubject<Pair<String, Archive.Item<*>>> {
    val vm = getDataExchangeViewModel()

    val ownerId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(ownerId).value!!
    return dataPack.itemStateChangedSubject
}

fun FragmentActivity.getDataExchangeViewModel(): DataExchangeViewModel {
    return ViewModelProvider(this).get(DataExchangeViewModel::class.java)
}

fun FragmentActivity.hasExchangeItem(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId()):Boolean{
    val oId = ownerId ?: return false
    val reqId = requestId ?: return false

    val dataArchive = getDataExchangeSubject(ownerId).value ?: return false
    if(dataArchive.isItemNullOrUnknownState(reqId)){
        return false
    }

    return true
}

fun <T> FragmentActivity.setExchangeItem(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId(), itemState: Archive.Item.State, data:T? = null):Boolean{
    val oId = ownerId ?: return false
    val reqId = requestId ?: return false

    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = oId, requestId =  reqId, itemState = itemState, data = data)
    return true
}

fun FragmentActivity.setExchangeItemStateWithNullData(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId(), itemState: Archive.Item.State):Boolean{
    val oId = ownerId ?: return false
    val reqId = requestId ?: return false

    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = oId, requestId =  reqId, itemState = itemState, data = null)
    return true
}

/** Item 상태를 Unknown 으로 설정 */
fun FragmentActivity.resetExchangeItemState(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId()):Boolean{
    return setExchangeItemStateWithNullData(ownerId=ownerId, requestId = requestId, itemState = Archive.Item.State.Unknown)
}
