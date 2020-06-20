package net.owlfamily.android.dataexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * All of extensions are same with [FragmentExtensions.kt]
 * except ActivityDataExchangerKey, getOrCreateArguments, getDataExchangeArguments
 */

/**
 * will be used 'key' of bundle in intent
 */
@Suppress("SpellCheckingInspection")
const val ActivityDataExchangerKey = "net.owlfamily.android.dataexchange.ActivityDataExchangerKey"

/**
  * @return bundle for DataExchange features. this will enable to 'Activity' be Owner of [Archive]. like [Fragment].
 */
fun FragmentActivity.getOrCreateArguments(): Bundle {
    var result: Bundle? = intent.getBundleExtra(ActivityDataExchangerKey)
    if(result == null){
        result = Bundle()
        intent.putExtra(ActivityDataExchangerKey, result)
    }
    return result
}

/**
 * @return same as [getOrCreateArguments] but 'null' if not exists
 */
fun FragmentActivity.getDataExchangeArguments(): Bundle? {
    return intent.getBundleExtra(ActivityDataExchangerKey)
}

fun FragmentActivity.getDataExchangeCallerId(): String? {
    return getDataExchangeArguments()?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeCallerId
    )
}

fun FragmentActivity.getDataExchangeArchiveItemId(): String? {
    return getDataExchangeArguments()?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeArchiveItemId
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


fun <T> FragmentActivity.getArchiveItemSubject(archiveItemId:String, ownerId:String): BehaviorSubject<Archive.Item<T>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.getOrCreateItemSubject<T>(archiveItemId = archiveItemId)
}
fun <T> FragmentActivity.getOwnedArchiveItemSubject(archiveItemId:String): BehaviorSubject<Archive.Item<T>> {
    return getArchiveItemSubject(archiveItemId, getOrCreateDataExchangeUniqueId())
}
fun <T> FragmentActivity.getCallerArchiveItemSubject(): BehaviorSubject<Archive.Item<T>>? {
    val archiveItemId = getDataExchangeArchiveItemId() ?: return null
    val callerId = getDataExchangeCallerId() ?: return null
    return getArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
}



fun FragmentActivity.removeArchiveItemSubject(archiveItemId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.removeItemSubject(archiveItemId = archiveItemId)
}
fun FragmentActivity.removeOwnedArchiveItemSubject(archiveItemId:String): Boolean {
    return removeArchiveItemSubject(archiveItemId = archiveItemId, ownerId = getOrCreateDataExchangeUniqueId())
}
fun FragmentActivity.removeCallerArchiveItemSubject(): Boolean {
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false

    return removeArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
}


fun FragmentActivity.hasArchiveItemSubject(archiveItemId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    val archive = vm.getOrCreateArchive(ownerId).value!!
    return archive.hasItemSubject(archiveItemId)
}
fun FragmentActivity.hasOwnedArchiveItemSubject(archiveItemId:String): Boolean {
    return hasArchiveItemSubject(archiveItemId = archiveItemId, ownerId = getOrCreateDataExchangeUniqueId())
}
fun FragmentActivity.hasCallerArchiveItemSubject(): Boolean {
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
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



fun FragmentActivity.hasArchiveItemState(ownerId:String, archiveItemId:String):Boolean{
    val dataArchive = getArchiveSubject(ownerId).value ?: return false
    if(dataArchive.isItemNullOrUnknownState(archiveItemId)){
        return false
    }
    return true
}
fun FragmentActivity.hasCallerArchiveItemState():Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemState(ownerId = callerId, archiveItemId = archiveItemId)
}
fun FragmentActivity.hasOwnedArchiveItemState(archiveItemId:String):Boolean{
    return hasArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), archiveItemId = archiveItemId)
}



fun <T> FragmentActivity.setArchiveItem(ownerId:String, archiveItemId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, archiveItemId = archiveItemId, itemState = itemState, data = data)
    return true
}
fun <T> FragmentActivity.setCallerArchiveItem(itemState: Archive.Item.State, data:T? = null):Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItem(ownerId = callerId, archiveItemId = archiveItemId, itemState = itemState, data = data)
}
fun <T> FragmentActivity.setOwnedArchiveItem(archiveItemId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    return setArchiveItem(ownerId = getOrCreateDataExchangeUniqueId(), archiveItemId = archiveItemId, itemState = itemState, data = data)
}



fun FragmentActivity.setArchiveItemStateWithNullData(ownerId:String, archiveItemId:String, itemState: Archive.Item.State):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, archiveItemId = archiveItemId, itemState = itemState, data = null)
    return true
}
fun FragmentActivity.setCallerArchiveItemStateWithNullData(itemState: Archive.Item.State):Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItemStateWithNullData(ownerId = callerId, archiveItemId = archiveItemId, itemState = itemState)
}
fun FragmentActivity.setOwnedArchiveItemStateWithNullData(archiveItemId:String, itemState: Archive.Item.State):Boolean{
    return setArchiveItemStateWithNullData(getOrCreateDataExchangeUniqueId(), archiveItemId, itemState)
}


fun FragmentActivity.resetArchiveItemState(ownerId:String, archiveItemId:String):Boolean{
    return setArchiveItemStateWithNullData(ownerId=ownerId, archiveItemId = archiveItemId, itemState = Archive.Item.State.Unknown)
}
fun FragmentActivity.resetCallerArchiveItemState():Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return resetArchiveItemState(ownerId = callerId, archiveItemId = archiveItemId)
}
fun FragmentActivity.resetOwnedArchiveItemState(archiveItemId:String):Boolean{
    return resetArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), archiveItemId = archiveItemId)
}
