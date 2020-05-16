package net.owlfamily.android.dataexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject



fun Fragment.getOrCreateArguments(): Bundle {
    var result = arguments
    if(result == null){
        result = Bundle()
        arguments = result
    }

    return result
}

fun Fragment.getDataExchangeOwnerId(): String? {
    return arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeOwnerId
    )
}

fun Fragment.getDataExchangeRequestId(): String? {
    return arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeRequestId
    )
}

fun Fragment.getOrCreateDataExchangeUniqueId(): String {
    return getDataExchangeUniqueId(true)!!
}

fun Fragment.getDataExchangeUniqueId(createIfEmpty:Boolean = true): String? {
    var bundle = arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)
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

fun Fragment.getDataExchangeSubject(ownerId:String? = null): BehaviorSubject<Archive> {
    val vm = getDataExchangeViewModel()
    val ownerId = ownerId ?: getOrCreateDataExchangeUniqueId()
    return vm.getOrCreateDataPack(ownerId)
}

fun Fragment.removeDataExchangeSubject(ownerId:String? = null): Boolean {
    val vm = getDataExchangeViewModel()
    val oId = ownerId ?: getDataExchangeUniqueId(false) ?: return false
    return vm.removeDataPack(oId)
}

fun <T> Fragment.getDataExchangeItemSubject(requestId:String, ownerId:String? = null): BehaviorSubject<Archive.Item<T>> {
    val vm = getDataExchangeViewModel()

    val oId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(oId).value!!
    return dataPack.getOrCreateItemSubject<T>(requestId = requestId)
}

fun Fragment.removeDataExchangeItemSubject(requestId:String, ownerId:String? = null): Boolean {
    val vm = getDataExchangeViewModel()

    val oId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(oId).value!!
    return dataPack.removeItemSubject(requestId = requestId)
}

fun Fragment.hasDataExchangeItem(requestId:String, ownerId:String? = null): Boolean {
    val vm = getDataExchangeViewModel()

    val oId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(oId).value!!
    return dataPack.hasItemSubject(requestId)
}

fun Fragment.getDataExchangeItemStateChangeSubject(ownerId:String? = null): PublishSubject<Pair<String, Archive.Item<*>>> {
    val vm = getDataExchangeViewModel()

    val ownerId = ownerId ?: getOrCreateDataExchangeUniqueId()
    val dataPack = vm.getOrCreateDataPack(ownerId).value!!
    return dataPack.itemStateChangedSubject
}

fun Fragment.getDataExchangeViewModel(): DataExchangeViewModel {
    return ViewModelProvider(requireActivity()).get(DataExchangeViewModel::class.java)
}

fun Fragment.hasExchangeItem(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId()):Boolean{
    val oId = ownerId ?: return false
    val reqId = requestId ?: return false

    val dataArchive = getDataExchangeSubject(ownerId).value ?: return false
    if(dataArchive.isItemNullOrUnknownState(reqId)){
        return false
    }

    return true
}

fun <T> Fragment.setExchangeItem(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId(), itemState: Archive.Item.State, data:T? = null):Boolean{
    val oId = ownerId ?: return false
    val reqId = requestId ?: return false

    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = oId, requestId =  reqId, itemState = itemState, data = data)
    return true
}

fun Fragment.setExchangeItemStateWithNullData(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId(),  itemState: Archive.Item.State):Boolean{
    val oId = ownerId ?: return false
    val reqId = requestId ?: return false

    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = oId, requestId =  reqId, itemState = itemState, data = null)
    return true
}

/** Item 상태를 Unknown 으로 설정 */
fun Fragment.resetExchangeItemState(ownerId:String? = getDataExchangeOwnerId(), requestId:String? = getDataExchangeRequestId()):Boolean{
    return setExchangeItemStateWithNullData(ownerId=ownerId, requestId = requestId, itemState = Archive.Item.State.Unknown)
}
