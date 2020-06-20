package net.owlfamily.android.dataexchange.navextensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import net.owlfamily.android.dataexchange.*

/**
 * just call [NavController.dataExchangeAwareNavigate]
 */
fun Fragment.dataExchangeAwareNavigate(
    @IdRes from: Int? = null,
    waitFrom:Boolean = false,
    @IdRes to: Int,
    callerId:String? = null,
    archiveItemId:String? = null,
    calleeId: String? = null,
    bundle: Bundle? = null, navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    navController: NavController = findNavController()){

    navController.dataExchangeAwareNavigate(from = from, waitFrom = waitFrom, to = to, callerId = callerId, archiveItemId = archiveItemId, calleeId = calleeId,
        bundle = bundle, navOptions = navOptions, navigatorExtras = navigatorExtras)
}


/**
 * return [DataExchangePair] after call [NavController.dataExchangeAwareNavigate]
 * @return return of [getDataExchangePair]
 */
fun <Out,In> Fragment.dataExchangeAwareNavigate(
    @IdRes from: Int? = null,
    waitFrom:Boolean = false,
    @IdRes to: Int,
    archiveItemId:String = DataExchangeHelper.createRandomID(),
    callerId:String = getOrCreateDataExchangeUniqueId(),
    calleeId: String = DataExchangeHelper.createRandomID(),
    bundle: Bundle? = null, navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    navController: NavController = findNavController(),
    autoRemoveOutItemAfterNext:Boolean = true,
    excludedStatesFromAutoRemove:List<Archive.Item.State>? = null
): DataExchangePair<Out, In> {
    navController.dataExchangeAwareNavigate(from = from, waitFrom = waitFrom, to = to, callerId = callerId, archiveItemId = archiveItemId, calleeId = calleeId, bundle = bundle, navOptions = navOptions, navigatorExtras = navigatorExtras)

    return getDataExchangePair<Out,In>(archiveItemId=archiveItemId, callerId=callerId,
        calleeId=calleeId,
        autoRemoveCallerItemAfterNext = autoRemoveOutItemAfterNext,
        excludedStatesFromAutoRemove = excludedStatesFromAutoRemove)
}

/**
 * @param archiveItemId id of [Archive.Item]
 * @param callerId id of caller
 * @param calleeId id of callee
 * @param initializeCallerItemStateWith default state of [Archive.Item] owned by caller.
 * @param initializeCalleeItemStateWith default state of [Archive.Item] owned by callee.
 * @param autoRemoveCallerItemAfterNext for memory management
 * @param excludedStatesFromAutoRemove the filters to avoid [autoRemoveCallerItemAfterNext] running
 *
 * @return a [DataExchangePair] instance to exchange data between caller & callee
 */
fun <Caller,Callee> Fragment.getDataExchangePair(
    archiveItemId:String,
    callerId:String = getOrCreateDataExchangeUniqueId(),
    calleeId: String = DataExchangeHelper.createRandomID(),
    initializeCallerItemStateWith: Archive.Item.State? = Archive.Item.State.Unknown,
    initializeCalleeItemStateWith: Archive.Item.State? = Archive.Item.State.Unknown,
    autoRemoveCallerItemAfterNext:Boolean = true,
    excludedStatesFromAutoRemove:List<Archive.Item.State>? = null
): DataExchangePair<Caller, Callee> {
    initializeCallerItemStateWith?.let {
        setArchiveItemStateWithNullData(ownerId=callerId, archiveItemId = archiveItemId, itemState = it)
    }
    initializeCalleeItemStateWith?.let {
        setArchiveItemStateWithNullData(ownerId=calleeId, archiveItemId = archiveItemId, itemState = it)
    }

    val callerItemSubjectRemover = {
        removeArchiveItemSubject(archiveItemId = archiveItemId, ownerId = callerId)
    }

    val callerObservable:Observable<Archive.Item<Caller>> = if(autoRemoveCallerItemAfterNext) {
        getArchiveItemSubject<Caller>(archiveItemId = archiveItemId, ownerId = callerId).filter { !it.isUnknownState }.doAfterNext { item ->
            excludedStatesFromAutoRemove?.let { excludes ->
                if(!excludes.contains(item.state)){
                    callerItemSubjectRemover()
                }
            } ?: run {
                callerItemSubjectRemover()
            }
        }
    }else{
        getArchiveItemSubject<Caller>(archiveItemId = archiveItemId, ownerId = callerId).filter { !it.isUnknownState }
    }

    val calleeSubject: Subject<Archive.Item<Callee>> = getArchiveItemSubject<Callee>(archiveItemId = archiveItemId, ownerId = calleeId)

    return DataExchangePair(callerObservable = callerObservable, calleeSubject = calleeSubject, callerItemRemover = callerItemSubjectRemover)
}



//region Independent DialogFragment from NavController

/** show DialogFragment */
fun Fragment.showDialogFragment(
    instance: DialogFragment,
    bundle: Bundle? = null,
    calleeId: String = DataExchangeHelper.createRandomID()) {
    requireActivity().showDialogFragment(instance = instance, bundle = bundle, calleeId = calleeId)
}

/** redirect to [FragmentActivity.showDialogFragmentForDataExchange]  */
fun <Caller,Callee> Fragment.showDialogFragmentForDataExchange(
    instance: DialogFragment,
    archiveItemId:String = DataExchangeHelper.createRandomID(),
    callerId:String = getOrCreateDataExchangeUniqueId(),
    calleeId: String = DataExchangeHelper.createRandomID(),
    callerItemStateWhenException:Archive.Item.State = NavExtensionsConfig.ArchiveItemStateWhenNavException,
    bundle: Bundle? = null,
    autoRemoveCallerItemAfterNext:Boolean = true,
    excludedStatesFromAutoRemove:List<Archive.Item.State>? = null
): DataExchangePair<Caller, Callee> {
    return requireActivity().showDialogFragmentForDataExchange(
        instance = instance,
        archiveItemId = archiveItemId,
        callerId = callerId,
        calleeId = calleeId,
        callerItemStateWhenException = callerItemStateWhenException,
        bundle = bundle,
        autoRemoveCallerItemAfterNext = autoRemoveCallerItemAfterNext,
        excludedStatesFromAutoRemove = excludedStatesFromAutoRemove)
}

//endregion