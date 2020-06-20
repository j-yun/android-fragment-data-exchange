package net.owlfamily.android.dataexchange.navextensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.navigation.*
import androidx.navigation.fragment.findNavController
import net.owlfamily.android.dataexchange.navextensions.NavExtensionsConfig.IgnoreNavigateSafeChecking
import net.owlfamily.android.dataexchange.DataExchangeHelper


/**
 * to prevent exception occurs when [NavController.navigate]
 *
 * @param from should be same with [NavController.getCurrentDestination]'s id. if not, this function will do nothing or wait until became it.
 * @param waitFrom will be used with unmatched [from] and [NavController.getCurrentDestination]'s id
 * @param to argument of [NavController.navigate]
 * @param bundle argument of [NavController.navigate]
 * @param navOptions argument of [NavController.navigate]
 * @param navigatorExtras argument of [NavController.navigate]
 *
 */
fun NavController.navigateSafe(
    @IdRes from: Int,
    waitFrom:Boolean,
    @IdRes to: Int,
    bundle: Bundle? = null, navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
):Boolean {
    return if(!IgnoreNavigateSafeChecking){
        if (currentDestination?.id == from) {
            navigate(to, bundle, navOptions, navigatorExtras)
            true
        }else{
            if(waitFrom){
                this.addOnDestinationChangedListener(object:NavController.OnDestinationChangedListener {
                    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
                        controller.removeOnDestinationChangedListener(this)
                        if(destination.id == from){
                            navigate(to, bundle, navOptions, navigatorExtras)
                        }
                    }
                })
            }
            true
        }
    }else{
        navigate(to, bundle, navOptions, navigatorExtras)
        true
    }
}


/**
 * for support [net.owlfamily.android.dataexchange.DataExchangeViewModel]
 * this will create a DataExchange bundle as argument for [to]
 */
@Suppress("NAME_SHADOWING")
fun NavController.dataExchangeAwareNavigate(
    @IdRes from: Int? = null,
    waitFrom:Boolean = false,
    @IdRes to: Int,
    callerId:String? = null, archiveItemId:String? = null, calleeId:String? = null,
    bundle: Bundle? = null, navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
){
    // argument 는 기본적으로 non null 로 하도록 한다.
    val bundle = bundle ?: Bundle()

    // 데이터 교환이 필요하다면, 요청자, 요청 ID 값을 argument 로 생성하여 넘긴다.
    if(callerId != null && archiveItemId != null){
        DataExchangeHelper.getOrCreateExchangeBundleForRequest(bundle, callerId, archiveItemId, calleeId)
    }

    val navOptions = navOptions ?: NavExtensionsConfig.createNavOpt()

    from?.let {
        navigateSafe(from = it, waitFrom = waitFrom, to = to, bundle = bundle, navOptions = navOptions, navigatorExtras = navigatorExtras)
    } ?: run {
        navigate(to, bundle, navOptions, navigatorExtras)
    }
}