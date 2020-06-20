package net.owlfamily.android.dataexchange.navextensions

import android.app.Activity
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import net.owlfamily.android.dataexchange.Archive

/**
 * global NavController configuration
 *
 * All activities should use same with [defaultActivityNavControllerViewResId]
 */
object NavExtensionsConfig {
    /** the viewResId for [Activity.findNavController] */
    @IdRes
    var defaultActivityNavControllerViewResId:Int = 0
    var defaultNavControllerFinder:(FragmentActivity)->NavController = {
        it.findNavController(defaultActivityNavControllerViewResId)
    }

    @IdRes
    var navDefaultEnterAnim:Int = 0
    @IdRes
    var navDefaultExitAnim:Int = 0
    @IdRes
    var navDefaultPopEnterAnim:Int = 0
    @IdRes
    var navDefaultPopExitAnim:Int = 0

    /** default animations */
    var createNavOpt:()->NavOptions? = fun():NavOptions? {
        if(navDefaultEnterAnim == 0 || navDefaultExitAnim == 0 || navDefaultPopEnterAnim == 0 || navDefaultPopExitAnim == 0){
            return null
        }

        return NavOptions.Builder()
            .setEnterAnim(navDefaultEnterAnim)
            .setExitAnim(navDefaultExitAnim)
            .setPopEnterAnim(navDefaultPopEnterAnim)
            .setPopExitAnim(navDefaultPopExitAnim)
            .build()
    }

    /** true : disable [navigateSafe] usage */
    var IgnoreNavigateSafeChecking = false

    /** if exception occurred during [FragmentActivity.showDialogFragmentForDataExchange] execution. [ArchiveItemStateWhenNavException]  will set to caller's [Archive.Item.State] */
    var ArchiveItemStateWhenNavException: Archive.Item.State = Archive.Item.State.Unknown
}