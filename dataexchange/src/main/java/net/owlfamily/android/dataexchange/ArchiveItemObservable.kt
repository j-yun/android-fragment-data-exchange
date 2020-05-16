package net.owlfamily.android.dataexchange

import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

interface ObserversCleaner {
    fun removeOtherObservers()
}

@Deprecated("사용보류")
class ArchiveItemObservable<T> : Subject<T>, ObserversCleaner {
    companion object {
        fun <T> create(): ArchiveItemObservable<T> {
            return ArchiveItemObservable()
        }

        fun <T> createDefault(defaultValue:T): ArchiveItemObservable<T> {
            return ArchiveItemObservable(defaultValue)
        }
    }

    constructor():super(){
        subject = BehaviorSubject.create()
    }

    constructor(defaultValue:T):super(){
        subject = BehaviorSubject.createDefault(defaultValue)
    }

    val subject:BehaviorSubject<T>
    val value:T? get() = subject.value

    val disposables = CompositeDisposable()

    override fun subscribeActual(observer: Observer<in T>) {
        val disposable = subject.subscribe({
            observer.onNext(it)
        },{
            observer.onError(it)
        },{
            observer.onComplete()
        },{ disposable ->
            observer.onSubscribe(disposable)

            if(!disposable.isDisposed){
                disposables.add(disposable)
            }
        })

        if(!disposable.isDisposed){
            disposables.add(disposable)
        }else{
            disposables.delete(disposable)
        }
    }

    override fun removeOtherObservers(){
        disposables.clear()
    }

    override fun onComplete() {
        subject.onComplete()
        disposables.dispose()
    }

    override fun onSubscribe(d: Disposable) {
        subject.onSubscribe(d)
    }

    override fun onNext(t: T) {
        subject.onNext(t)
    }

    override fun onError(e: Throwable) {
        subject.onError(e)
        disposables.dispose()
    }

    override fun hasThrowable(): Boolean {
        return subject.hasThrowable()
    }

    override fun hasObservers(): Boolean {
        return subject.hasObservers()
    }

    override fun getThrowable(): Throwable? {
        return subject.throwable
    }

    override fun hasComplete(): Boolean {
        return subject.hasComplete()
    }
}