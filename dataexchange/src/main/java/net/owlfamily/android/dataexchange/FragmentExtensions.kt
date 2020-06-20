package net.owlfamily.android.dataexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


/** get or create arguments */
fun Fragment.getOrCreateArguments(): Bundle {
    var result = arguments
    if(result == null){
        result = Bundle()
        arguments = result
    }

    return result
}

/**
 * for callee.
 * find and return 'id' of caller
 */
fun Fragment.getDataExchangeCallerId(): String? {
    return arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeCallerId
    )
}

/**
 * for callee.
 * find and return 'archiveItemId' that specified from caller
 */
fun Fragment.getDataExchangeArchiveItemId(): String? {
    return arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeArchiveItemId
    )
}

/**
 * variation of [getDataExchangeUniqueId]
 */
fun Fragment.getOrCreateDataExchangeUniqueId(): String {
    return getDataExchangeUniqueId(true)!!
}

/**
 * @param createIfEmpty true - create if 'id' not exists
 * @return the 'id' of [Fragment] instance. this will be used for 'key' of [Archive] in [DataExchangeViewModel]
 */
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


/**
 * @return [DataExchangeViewModel] instance of Activity
 */
fun Fragment.getDataExchangeViewModel(): DataExchangeViewModel {
    return ViewModelProvider(requireActivity()).get(DataExchangeViewModel::class.java)
}


/**
 * @return subject of [Archive] from parent [DataExchangeViewModel]
 * @param ownerId owner id of Item
 */
fun Fragment.getArchiveSubject(ownerId:String): BehaviorSubject<Archive> {
    val vm = getDataExchangeViewModel()
    return vm.getOrCreateArchive(ownerId)
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [getArchiveSubject]
 */
fun Fragment.getOwnedArchiveSubject(): BehaviorSubject<Archive> {
    return getArchiveSubject(getOrCreateDataExchangeUniqueId())
}
/**
 * for callee.
 * find arguments automatically for [getArchiveSubject]
 */
fun Fragment.getCallerArchiveSubject(): BehaviorSubject<Archive>? {
    getDataExchangeCallerId()?.let {
        return getArchiveSubject(it)
    }
    return null
}


/**
 * remove subject of [Archive] from parent [DataExchangeViewModel]
 * @return true if [Archive] removed from parent [DataExchangeViewModel]
 * @param ownerId owner id of Item
 */
fun Fragment.removeArchiveSubject(ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    return vm.removeArchive(ownerId)
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [removeArchiveSubject]
 */
fun Fragment.removeOwnedArchiveSubject(): Boolean {
    getDataExchangeUniqueId(false)?.let {
        return removeArchiveSubject(ownerId = it)
    }
    return false
}
/**
 * for callee.
 * find arguments automatically for [removeArchiveSubject]
 */
fun Fragment.removeCallerArchiveSubject(): Boolean {
    getDataExchangeCallerId()?.let {
        return removeArchiveSubject(ownerId = it)
    }
    return false
}


/**
 * @return subject of [Archive.Item] from parent [Archive]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun <T> Fragment.getArchiveItemSubject(archiveItemId:String, ownerId:String): BehaviorSubject<Archive.Item<T>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.getOrCreateItemSubject<T>(archiveItemId = archiveItemId)
}
/**
 * for callee.
 * find arguments automatically for [getArchiveItemSubject]
 */
fun <T> Fragment.getCallerArchiveItemSubject(): BehaviorSubject<Archive.Item<T>>? {
    val archiveItemId = getDataExchangeArchiveItemId() ?: return null
    val callerId = getDataExchangeCallerId() ?: return null
    return getArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [getArchiveItemSubject]
 */
fun <T> Fragment.getOwnedArchiveItemSubject(archiveItemId:String): BehaviorSubject<Archive.Item<T>> {
    return getArchiveItemSubject(archiveItemId, getOrCreateDataExchangeUniqueId())
}


/**
 * @return true if subject of [Archive.Item] removed from parent [Archive]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun Fragment.removeArchiveItemSubject(archiveItemId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.removeItemSubject(archiveItemId = archiveItemId)
}
/**
 * for callee.
 * find arguments automatically for [removeArchiveItemSubject]
 */
fun Fragment.removeCallerArchiveItemSubject(): Boolean {
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false

    return removeArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [removeArchiveItemSubject]
 */
fun Fragment.removeOwnedArchiveItemSubject(archiveItemId:String): Boolean {
    return removeArchiveItemSubject(archiveItemId = archiveItemId, ownerId = getOrCreateDataExchangeUniqueId())
}


/**
 * @return true if subject of [Archive.Item] exists from parent [Archive]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun Fragment.hasArchiveItemSubject(archiveItemId:String, ownerId:String): Boolean {
    val vm = getDataExchangeViewModel()
    val archive = vm.getOrCreateArchive(ownerId).value!!
    return archive.hasItemSubject(archiveItemId)
}
/**
 * for callee.
 * find arguments automatically for [hasArchiveItemSubject]
 */
fun Fragment.hasCallerArchiveItemSubject(): Boolean {
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [hasArchiveItemSubject]
 */
fun Fragment.hasOwnedArchiveItemSubject(archiveItemId:String): Boolean {
    return hasArchiveItemSubject(archiveItemId = archiveItemId, ownerId = getOrCreateDataExchangeUniqueId())
}


/**
 * @return [net.owlfamily.android.dataexchange.Archive.itemStateChangedSubject] of archive
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun Fragment.getArchiveItemStateChangeSubject(ownerId:String): PublishSubject<Pair<String, Archive.Item<*>>> {
    val vm = getDataExchangeViewModel()

    val dataPack = vm.getOrCreateArchive(ownerId).value!!
    return dataPack.itemStateChangedSubject
}
/**
 * for callee.
 * find arguments automatically for [getArchiveItemStateChangeSubject]
 */
fun Fragment.getCallerArchiveItemStateChangeSubject(): PublishSubject<Pair<String, Archive.Item<*>>>? {
    getDataExchangeCallerId()?.let {
        return getArchiveItemStateChangeSubject(it)
    }
    return null
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [getArchiveItemStateChangeSubject]
 */
fun Fragment.getOwnedArchiveItemStateChangeSubject(): PublishSubject<Pair<String, Archive.Item<*>>> {
    return getArchiveItemStateChangeSubject(getOrCreateDataExchangeUniqueId())
}

/**
 * @return 'true' if Item state is != [Archive.Item.State.Unknown]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun Fragment.hasArchiveItemState(ownerId:String, archiveItemId:String):Boolean{
    val dataArchive = getArchiveSubject(ownerId).value ?: return false
    if(dataArchive.isItemNullOrUnknownState(archiveItemId)){
        return false
    }
    return true
}
/**
 * for callee.
 * find arguments automatically for [hasArchiveItemState]
 */
fun Fragment.hasCallerArchiveItemState():Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return hasArchiveItemState(ownerId = callerId, archiveItemId = archiveItemId)
}
/**
 * for caller ( owner of Archive ).
 * find arguments automatically for [hasArchiveItemState]
 */
fun Fragment.hasOwnedArchiveItemState(archiveItemId:String):Boolean{
    return hasArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), archiveItemId = archiveItemId)
}



/**
 * set state of item to [itemState], data to [data]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun <T> Fragment.setArchiveItem(ownerId:String, archiveItemId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, archiveItemId = archiveItemId, itemState = itemState, data = data)
    return true
}
/**
 * for callee.
 * set state of item to [itemState], data to [data]
 * find arguments automatically for [setArchiveItem]
 */
fun <T> Fragment.setCallerArchiveItem(itemState: Archive.Item.State, data:T? = null):Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItem(ownerId = callerId, archiveItemId = archiveItemId, itemState = itemState, data = data)
}
/**
 * for caller ( owner of [Archive] ).
 * set state of item to [itemState]
 * find arguments automatically for [setArchiveItemStateWithNullData]
 */
fun <T> Fragment.setOwnedArchiveItem(archiveItemId:String, itemState: Archive.Item.State, data:T? = null):Boolean{
    return setArchiveItem(ownerId = getOrCreateDataExchangeUniqueId(), archiveItemId = archiveItemId, itemState = itemState, data = data)
}


/**
 * set state of item to [itemState]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun Fragment.setArchiveItemStateWithNullData(ownerId:String, archiveItemId:String, itemState: Archive.Item.State):Boolean{
    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(ownerId = ownerId, archiveItemId = archiveItemId, itemState = itemState, data = null)
    return true
}
/**
 * for callee.
 * set state of item to [itemState]
 * find arguments automatically for [setArchiveItemStateWithNullData]
 */
fun Fragment.setCallerArchiveItemStateWithNullData(itemState: Archive.Item.State):Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return setArchiveItemStateWithNullData(ownerId = callerId, archiveItemId = archiveItemId, itemState = itemState)
}
/**
 * for caller ( owner of [Archive] ).
 * set state of item to [itemState]
 * find arguments automatically for [setArchiveItemStateWithNullData]
 */
fun Fragment.setOwnedArchiveItemStateWithNullData(archiveItemId:String, itemState: Archive.Item.State):Boolean{
    return setArchiveItemStateWithNullData(getOrCreateDataExchangeUniqueId(), archiveItemId, itemState)
}


/**
 * set state of item to [Archive.Item.State.Unknown]
 * @param ownerId owner id of Item
 * @param archiveItemId key of Item
 */
fun Fragment.resetArchiveItemState(ownerId:String, archiveItemId:String):Boolean{
    return setArchiveItemStateWithNullData(ownerId=ownerId, archiveItemId = archiveItemId, itemState = Archive.Item.State.Unknown)
}
/**
 * for callee.
 * set state of item to [Archive.Item.State.Unknown]
 * find arguments automatically for [resetArchiveItemState]
 */
fun Fragment.resetCallerArchiveItemState():Boolean{
    val archiveItemId = getDataExchangeArchiveItemId() ?: return false
    val callerId = getDataExchangeCallerId() ?: return false
    return resetArchiveItemState(ownerId = callerId, archiveItemId = archiveItemId)
}
/**
 * for caller ( owner of [Archive] ).
 * set state of item to [Archive.Item.State.Unknown]
 * find arguments automatically for [resetArchiveItemState]
 */
fun Fragment.resetOwnedArchiveItemState(archiveItemId:String):Boolean{
    return resetArchiveItemState(ownerId = getOrCreateDataExchangeUniqueId(), archiveItemId = archiveItemId)
}