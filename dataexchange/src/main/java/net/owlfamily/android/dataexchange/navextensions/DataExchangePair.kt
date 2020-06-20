package net.owlfamily.android.dataexchange.navextensions

import io.reactivex.Observable
import io.reactivex.subjects.Subject
import net.owlfamily.android.dataexchange.Archive

/**
 * Data exchanges caller <-> callee
 */
class DataExchangePair<Caller, Callee>(
    /** callee -> caller */
    val callerObservable: Observable<Archive.Item<Caller>>,
    /** caller -> callee */
    val calleeSubject: Subject<Archive.Item<Callee>>,
    val callerItemRemover:()->Boolean
)