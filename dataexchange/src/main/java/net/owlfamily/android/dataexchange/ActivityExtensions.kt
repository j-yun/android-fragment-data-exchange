package net.owlfamily.android.dataexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
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

fun FragmentActivity.getDataExchangeCallerId(): String? {
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


fun FragmentActivity.getDataExchangeViewModel(): DataExchangeViewModel {
    return ViewModelProvider(this).get(DataExchangeViewModel::class.java)
}


fun FragmentActivity.getArchiveSubject(ownerId:String): BehaviorSubject<Archive> {
    val vm = getDataExchangeViewModel()
    return vm.getOrCreateArchive(ownerId)
}

fun FragmentActivity.getOwnedArchiveSubject(): BehaviorSubject<Archive> {
    return getArchiveSubject(getOrCreateDataExchangeUniqueId())
}

fun FragmentActivity.getCallerArchiveSubject(): BehaviorSubject<Archive>? {
    getDataExchangeCallerId()?.let {
        return getArchiveSubject(it)
    }
    return null
}



fun FragmentActivity.removeArchiveSubject(ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    return vm.removeArchive(ownerId)
}
fun FragmentActivity.removeOwnedArchiveSubject(): Boolean {
    getDataExchangeUniqueId(false)?.let {
        return removeArchiveSubject(ownerId = it)
    }
    return false
}
fun FragmentActivity.removeCallerArchiveSubject(): Boolean {
    getDataExchangeCallerId()?.let {
        return removeArchiveSubject(ownerId = it)
    }
    return false
}


fun <T> FragmentActivity.getArchiveItemSubject(requestId:String, ownerId:String): BehaviorSubject<Archive.Item<T>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.getOrCreateItemSubject<T>(requestId = requestId)
}
fun <T> FragmentActivity.getOwnedArchiveItemSubject(requestId:String): BehaviorSubject<Archive.Item<T>> {
    return getArchiveItemSubject(requestId, getOrCreateDataExchangeUniqueId())
}
fun <T> FragmentActivity.getCallerArchiveItemSubject(): BehaviorSubject<Archive.Item<T>>? {
    val requestId = getDataExchangeRequestId() ?: return null
    val callerId = getDataExchangeCallerId() ?: return null
    return getArchiveItemSubject(requestId = requestId, ownerId = callerId)
}



fun FragmentActivity.removeArchiveItemSubject(requestId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.removeItemSubject(requestId = requestId)
}
fun FragmentActivity.removeOwnedArchiveItemSubject(requestId:String): Boolean {
    return removeArchiveItemSubject(requestId = requestId, ownerId = getOrCreateDataExchangeUniqueId())
}
fun FragmentActivity.removeCallerArchiveItemSubject(): Boolean {
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false

    return removeArchiveItemSubject(requestId = requestId, ownerId = callerId)
}


fun FragmentActivity.hasArchiveItemSubject(requestId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    val archive = vm.getOrCreateArchive(ownerId).value!!
    return archive.hasItemSubject(requestId)
}
fun FragmentActivity.hasOwnedArchiveItemSubject(requestId:String): Boolean {
    return hasArchiveItemSubject(requestId = requestId, ownerId = getOrCreateDataExchangeUniqueId())
}
fun FragmentActivity.hasCallerArchiveItemSubject(): Boolean {
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemSubject(requestId = requestId, ownerId = callerId)
}



fun FragmentActivity.getArchiveItemStateChangeSubject(ownerId:String): PublishSubject<Pair<String, Archive.Item<*>>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.itemStateChangedSubject
}
fun FragmentActivity.getOwnedArchiveItemStateChangeSubject(): PublishSubject<Pair<String, Archive.Item<*>>> {
    return getArchiveItemStateChangeSubject(getOrCreateDataExchangeUniqueId())
}
fun FragmentActivity.getCallerArchiveItemStateChangeSubject(): PublishSubject<Pair<String, Archive.Item<*>>>? {
    getDataExchangeCallerId()?.let {
        return getArchiveItemStateChangeSubject(it)
    }
    return null
}



fun FragmentActivity.hasArchiveItemState(ownerId:String, requestId:String):Boolean{
    val dataArchive = getArchiveSubject(ownerId).value ?: return false
    if(dataArchive.isItemNullOrUnknownState(requestId)){
        return false
    }
    return true
}
fun FragmentActivity.hasCallerArchiveItemState():Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemState(ownerId = callerId, requestId = requestId)
}
fun FragmentActivity.hasOwnedArchiveItemState(requestId:String):Boolean{
    return hasArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), requestId = requestId)
}



fun <T> FragmentActivity.setArchiveItem(ownerId:String, requestId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, requestId = requestId, itemState = itemState, data = data)
    return true
}
fun <T> FragmentActivity.setCallerArchiveItem(itemState: Archive.Item.State, data:T? = null):Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItem(ownerId = callerId, requestId = requestId, itemState = itemState, data = data)
}
fun <T> FragmentActivity.setOwnedArchiveItem(requestId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    return setArchiveItem(ownerId = getOrCreateDataExchangeUniqueId(), requestId = requestId, itemState = itemState, data = data)
}



fun FragmentActivity.setArchiveItemStateWithNullData(ownerId:String, requestId:String, itemState: Archive.Item.State):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, requestId = requestId, itemState = itemState, data = null)
    return true
}
fun FragmentActivity.setCallerArchiveItemStateWithNullData(itemState: Archive.Item.State):Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItemStateWithNullData(ownerId = callerId, requestId = requestId, itemState = itemState)
}
fun FragmentActivity.setOwnedArchiveItemStateWithNullData(requestId:String, itemState: Archive.Item.State):Boolean{
    return setArchiveItemStateWithNullData(getOrCreateDataExchangeUniqueId(), requestId, itemState)
}


/** Item 상태를 Unknown 으로 설정 */
fun FragmentActivity.resetArchiveItemState(ownerId:String, requestId:String):Boolean{
    return setArchiveItemStateWithNullData(ownerId=ownerId, requestId = requestId, itemState = Archive.Item.State.Unknown)
}
/** Item 상태를 Unknown 으로 설정 */
fun FragmentActivity.resetCallerArchiveItemState():Boolean{
    val requestId = getDataExchangeRequestId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return resetArchiveItemState(ownerId = callerId, requestId = requestId)
}
/** Item 상태를 Unknown 으로 설정 */
fun FragmentActivity.resetOwnedArchiveItemState(requestId:String):Boolean{
    return resetArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), requestId = requestId)
}
