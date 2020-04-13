package net.owlfamily.android.dataexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.reactivex.subjects.BehaviorSubject

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
        DataExchangeHelper.dataExchangeCallerId)
}

fun Fragment.getDataExchangeRequestId(): String? {
    return arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)?.getString(
        DataExchangeHelper.dataExchangeRequestId)
}

fun Fragment.getOrCreateDataExchangeUniqueId(): String {
    return getDataExchangeUniqueId(true)!!
}

fun Fragment.getDataExchangeUniqueId(createIfEmpty:Boolean = true): String? {
    var bundle = arguments?.getBundle(DataExchangeHelper.dataExchangeBundleId)
    bundle?.getString(DataExchangeHelper.dataExchangeId)?.let {
        return it
    }
    if(!createIfEmpty) { return null }

    if(bundle == null){
        bundle = Bundle()
        getOrCreateArguments().putBundle(DataExchangeHelper.dataExchangeBundleId, bundle)
    }

    val id = DataExchangeHelper.createRandomID()
    bundle.putString(DataExchangeHelper.dataExchangeId,id)
    return id
}

fun Fragment.getDataExchangeSubject(): BehaviorSubject<DataPack> {
    val vm = getDataExchangeViewModel()
    return vm.getOrCreateDataPack(getOrCreateDataExchangeUniqueId())
}

fun Fragment.getDataExchangeViewModel(): DataExchangeViewModel {
    return ViewModelProviders.of(requireActivity()).get(DataExchangeViewModel::class.java)
}

fun <T> Fragment.saveExchangeData(itemState:Int, data:T):Boolean{
    val callerId = getDataExchangeCallerId() ?: return false
    val requestId = getDataExchangeRequestId() ?: return false

    val dataBusVm = getDataExchangeViewModel()
    dataBusVm.saveItem(callerId = callerId, requestId =  requestId, itemState = itemState, data = data)
    return true
}
