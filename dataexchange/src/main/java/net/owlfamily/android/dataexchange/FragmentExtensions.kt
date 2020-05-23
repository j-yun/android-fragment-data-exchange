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

fun Fragment.getDataExchangeCallerId(): String? {
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



fun Fragment.getDataExchangeViewModel(): DataExchangeViewModel {
    return ViewModelProvider(requireActivity()).get(DataExchangeViewModel::class.java)
}



fun Fragment.getArchiveSubject(ownerId:String): BehaviorSubject<Archive> {
    val vm = getDataExchangeViewModel()
    return vm.getOrCreateArchive(ownerId)
}

fun Fragment.getOwnedArchiveSubject(): BehaviorSubject<Archive> {
    return getArchiveSubject(getOrCreateDataExchangeUniqueId())
}

fun Fragment.getCallerArchiveSubject(): BehaviorSubject<Archive>? {
    getDataExchangeCallerId()?.let {
        return getArchiveSubject(it)
    }
    return null
}



fun Fragment.removeArchiveSubject(ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    return vm.removeArchive(ownerId)
}
fun Fragment.removeOwnedArchiveSubject(): Boolean {
    getDataExchangeUniqueId(false)?.let {
        return removeArchiveSubject(ownerId = it)
    }
    return false
}
fun Fragment.removeCallerArchiveSubject(): Boolean {
    getDataExchangeCallerId()?.let {
        return removeArchiveSubject(ownerId = it)
    }
    return false
}


fun <T> Fragment.getArchiveItemSubject(requestId:String, ownerId:String): BehaviorSubject<Archive.Item<T>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.getOrCreateItemSubject<T>(requestId = requestId)
}
fun <T> Fragment.getOwnedArchiveItemSubject(requestId:String): BehaviorSubject<Archive.Item<T>> {
    return getArchiveItemSubject(requestId, getOrCreateDataExchangeUniqueId())
}
fun <T> Fragment.getCallerArchiveItemSubject(): BehaviorSubject<Archive.Item<T>>? {
    val requestId = getDataExchangeRequestId() ?: return null
    val callerId = getDataExchangeCallerId() ?: return null
    return getArchiveItemSubject(requestId = requestId, ownerId = callerId)
}



fun Fragment.removeArchiveItemSubject(requestId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.removeItemSubject(requestId = requestId)
}
fun Fragment.removeOwnedArchiveItemSubject(requestId:String): Boolean {
    return removeArchiveItemSubject(requestId = requestId, ownerId = getOrCreateDataExchangeUniqueId())
}
fun Fragment.removeCallerArchiveItemSubject(): Boolean {
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false

    return removeArchiveItemSubject(requestId = requestId, ownerId = callerId)
}


fun Fragment.hasArchiveItemSubject(requestId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    val archive = vm.getOrCreateArchive(ownerId).value!!
    return archive.hasItemSubject(requestId)
}
fun Fragment.hasOwnedArchiveItemSubject(requestId:String): Boolean {
    return hasArchiveItemSubject(requestId = requestId, ownerId = getOrCreateDataExchangeUniqueId())
}
fun Fragment.hasCallerArchiveItemSubject(): Boolean {
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemSubject(requestId = requestId, ownerId = callerId)
}



fun Fragment.getArchiveItemStateChangeSubject(ownerId:String): PublishSubject<Pair<String, Archive.Item<*>>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.itemStateChangedSubject
}
fun Fragment.getOwnedArchiveItemStateChangeSubject(): PublishSubject<Pair<String, Archive.Item<*>>> {
    return getArchiveItemStateChangeSubject(getOrCreateDataExchangeUniqueId())
}
fun Fragment.getCallerArchiveItemStateChangeSubject(): PublishSubject<Pair<String, Archive.Item<*>>>? {
    getDataExchangeCallerId()?.let {
        return getArchiveItemStateChangeSubject(it)
    }
    return null
}



fun Fragment.hasArchiveItemState(ownerId:String, requestId:String):Boolean{
    val dataArchive = getArchiveSubject(ownerId).value ?: return false
    if(dataArchive.isItemNullOrUnknownState(requestId)){
        return false
    }
    return true
}
fun Fragment.hasCallerArchiveItemState():Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemState(ownerId = callerId, requestId = requestId)
}
fun Fragment.hasOwnedArchiveItemState(requestId:String):Boolean{
    return hasArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), requestId = requestId)
}



fun <T> Fragment.setArchiveItem(ownerId:String, requestId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, requestId = requestId, itemState = itemState, data = data)
    return true
}
fun <T> Fragment.setCallerArchiveItem(itemState: Archive.Item.State, data:T? = null):Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItem(ownerId = callerId, requestId = requestId, itemState = itemState, data = data)
}
fun <T> Fragment.setOwnedArchiveItem(requestId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    return setArchiveItem(ownerId = getOrCreateDataExchangeUniqueId(), requestId = requestId, itemState = itemState, data = data)
}



fun Fragment.setArchiveItemStateWithNullData(ownerId:String, requestId:String, itemState: Archive.Item.State):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, requestId = requestId, itemState = itemState, data = null)
    return true
}
fun Fragment.setCallerArchiveItemStateWithNullData(itemState: Archive.Item.State):Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItemStateWithNullData(ownerId = callerId, requestId = requestId, itemState = itemState)
}
fun Fragment.setOwnedArchiveItemStateWithNullData(requestId:String, itemState: Archive.Item.State):Boolean{
    return setArchiveItemStateWithNullData(getOrCreateDataExchangeUniqueId(), requestId, itemState)
}


/** Item 상태를 Unknown 으로 설정 */
fun Fragment.resetArchiveItemState(ownerId:String, requestId:String):Boolean{
    return setArchiveItemStateWithNullData(ownerId=ownerId, requestId = requestId, itemState = Archive.Item.State.Unknown)
}
/** Item 상태를 Unknown 으로 설정 */
fun Fragment.resetCallerArchiveItemState():Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return resetArchiveItemState(ownerId = callerId, requestId = requestId)
}
/** Item 상태를 Unknown 으로 설정 */
fun Fragment.resetOwnedArchiveItemState(requestId:String):Boolean{
    return resetArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), requestId = requestId)
}