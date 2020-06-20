package net.owlfamily.android.dataexchange.navextensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import net.owlfamily.android.dataexchange.*


/**
 * used to more transaction safety checking on [FragmentActivity.showDialogFragmentForDataExchange] execution.
 * implementor should return right value that is safe state for Fragment Transaction.
 */
interface TransactionSafeChecker {
    val isTransactionSafe:Boolean
}

/**
 * just call [NavController.dataExchangeAwareNavigate]
 */
fun FragmentActivity.dataExchangeAwareNavigate(
    @IdRes from: Int? = null, @IdRes to: Int,
    callerId:String? = null,
    archiveItemId:String? = null,
    calleeId: String? = null,
    bundle: Bundle? = null, navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    navController: NavController = NavExtensionsConfig.defaultNavControllerFinder(this)){
    navController.dataExchangeAwareNavigate(from = from, to = to, callerId = callerId, archiveItemId = archiveItemId, calleeId = calleeId,
        bundle = bundle, navOptions = navOptions, navigatorExtras = navigatorExtras)
}

/**
 * return [DataExchangePair] after call [NavController.dataExchangeAwareNavigate]
 * @return return of [getDataExchangePair]
 */
fun <Caller,Callee> FragmentActivity.dataExchangeAwareNavigate(
    @IdRes from: Int? = null, @IdRes to: Int,
    archiveItemId:String = DataExchangeHelper.createRandomID(),
    callerId:String = getOrCreateDataExchangeUniqueId(),
    calleeId: String = DataExchangeHelper.createRandomID(),
    bundle: Bundle? = null, navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    navController: NavController = NavExtensionsConfig.defaultNavControllerFinder(this),
    autoRemoveCallerItemAfterNext:Boolean = true,
    excludedStatesFromAutoRemove:List<Archive.Item.State>? = null
): DataExchangePair<Caller, Callee> {
    navController.dataExchangeAwareNavigate(from = from, to = to, callerId = callerId, archiveItemId = archiveItemId, calleeId = calleeId, bundle = bundle, navOptions = navOptions, navigatorExtras = navigatorExtras)

    return getDataExchangePair<Caller,Callee>(archiveItemId=archiveItemId, callerId=callerId,
        calleeId=calleeId,
        autoRemoveCallerItemAfterNext = autoRemoveCallerItemAfterNext,
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
fun <Caller,Callee> FragmentActivity.getDataExchangePair(
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
        // onNext 후에 DataExchange 값 자동으로 제거
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

//region Independent DialogFragment - FragmentActivity

/** show DialogFragment via [FragmentActivity.getSupportFragmentManager] */
fun FragmentActivity.showDialogFragment(
    instance: DialogFragment,
    bundle: Bundle? = null,
    calleeId: String = DataExchangeHelper.createRandomID()) {

    instance.arguments = bundle
    instance.show(supportFragmentManager, calleeId)
}

/**
 * @param instance DialogFragment instance
 * @param archiveItemId id of [Archive.Item]
 * @param callerId id of caller
 * @param calleeId id for callee [instance]
 * @param bundle [instance]'s arguments
 * @param callerItemStateWhenException if exception occurred during execution. it will set to caller's [Archive.Item.State]
 * @param autoRemoveCallerItemAfterNext for memory management
 * @param excludedStatesFromAutoRemove the filters to avoid [autoRemoveCallerItemAfterNext] running
 */
fun <Caller,Callee> FragmentActivity.showDialogFragmentForDataExchange(
    instance: DialogFragment,
    archiveItemId:String = DataExchangeHelper.createRandomID(),
    callerId:String = getOrCreateDataExchangeUniqueId(),
    calleeId: String = DataExchangeHelper.createRandomID(),
    bundle: Bundle? = null,
    callerItemStateWhenException:Archive.Item.State = NavExtensionsConfig.ArchiveItemStateWhenNavException,
    autoRemoveCallerItemAfterNext:Boolean = true,
    excludedStatesFromAutoRemove:List<Archive.Item.State>? = null
): DataExchangePair<Caller, Callee> {
    // argument 는 기본적으로 non null 로 하도록 한다.
    @Suppress("NAME_SHADOWING")
    val bundle = bundle ?: Bundle()

    // 데이터 교환이 필요하다면, 요청자, 요청 ID 값을 argument 로 생성하여 넘긴다.
    DataExchangeHelper.getOrCreateExchangeBundleForRequest(bundle, callerId, archiveItemId, calleeId)
    instance.arguments = bundle

    return if(!this.isDestroyed && !this.isFinishing && this is TransactionSafeChecker && this.isTransactionSafe){
        instance.show(supportFragmentManager, calleeId)
        getDataExchangePair<Caller,Callee>(
            archiveItemId=archiveItemId, callerId=callerId,
            calleeId=calleeId,
            autoRemoveCallerItemAfterNext = autoRemoveCallerItemAfterNext,
            excludedStatesFromAutoRemove = excludedStatesFromAutoRemove)
    }else{
        getDataExchangePair<Caller,Callee>(
            archiveItemId=archiveItemId, callerId=callerId,
            calleeId=calleeId,
            initializeCallerItemStateWith = callerItemStateWhenException,
            autoRemoveCallerItemAfterNext = autoRemoveCallerItemAfterNext,
            excludedStatesFromAutoRemove = excludedStatesFromAutoRemove)
    }
}

//endregion