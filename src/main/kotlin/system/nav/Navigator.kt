package system.nav

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import lib.helper.cast
import system.Composable
import system.StateDelegate
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import androidx.compose.runtime.Composable as JetComposable

typealias Datamap = Map<String, Any>
typealias MutableDatamap = MutableMap<String, Any>


interface NavigationScope: Composable {
    infix operator fun <T: ViewModel> Screen<T>.invoke(foo: @JetComposable ScreenData<T>.() -> Unit)
    infix fun <T: ViewModel> Screen<T>.exiting(foo: @JetComposable ScreenData<T>.() -> Boolean)
}

data class ScreenData<T>(
    val viewModel: T,
    val paras: Datamap,
    val navigator: Navigator
)

data class Screen<T: ViewModel>(val name: String, val viewModel: KClass<T>)
abstract class ScreenKeeper {
    fun <T: ViewModel> screen(viewModel: KClass<T>) = PropertyDelegateProvider<Any?, StateDelegate<Screen<T>>> { _, property ->
        StateDelegate(property.name, Screen(property.name, viewModel))
    }
}


class Navigator(
    initialState: Screen<*>,
    initialParameters: Datamap = mapOf(),
    foo: NavigationScope.() -> Unit
): NavigationScope {
    private enum class ScreenStates { Static, Exiting }

    private val defaultViewModel = object: ViewModel() {}
    private var screenState: MutableState<ScreenStates> = mutableStateOf(ScreenStates.Static)
    private var currScreen: Screen<*> = initialState
    private var nextScreen = initialState
    private var currScreenParameters = initialParameters
    private var nextScreenParameters = initialParameters

    private val staticScreens = mutableMapOf<Screen<*>, @JetComposable ScreenData<*>.() -> Unit>()
    private val exitingScreen = mutableMapOf<Screen<*>, @JetComposable ScreenData<*>.() -> Boolean>()
    private val enteringScreen = mutableMapOf<Screen<*>, @JetComposable ScreenData<*>.() -> Boolean>()


    private val screenViewModelCache = mutableMapOf<Screen<*>, ViewModel>()
    private fun <T: ViewModel> getViewModel(screen: Screen<T>): ViewModel {
        if (screen in screenViewModelCache) return screenViewModelCache[screen]!!
        screenViewModelCache[screen] = screen.viewModel.primaryConstructor?.call() as ViewModel
        return defaultViewModel
    }

    //TODO back stack

    @JetComposable
    override fun render() {
        val screenState by this.screenState
        val currViewModel = getViewModel(currScreen)
        val mainContent = if (screenState != ScreenStates.Exiting) currScreen else nextScreen
        val mainViewModel = if (screenState != ScreenStates.Exiting) currViewModel else getViewModel(nextScreen)
        val mainData = ScreenData(mainViewModel, currScreenParameters, this)

        staticScreens[mainContent]?.invoke(mainData)
        if (screenState == ScreenStates.Exiting) {
            if (exitingScreen[currScreen]?.invoke(mainData) == true) {
                this.screenState.value = ScreenStates.Static
                this.currScreen = nextScreen
                this.currScreenParameters = nextScreenParameters
            }
        }
        //TODO Enter
    }

    override fun <T: ViewModel> Screen<T>.exiting(foo: @JetComposable ScreenData<T>.() -> Boolean) {
        this@Navigator.exitingScreen[this] = { foo.invoke(this.cast()) }
    }
    override fun <T: ViewModel> Screen<T>.invoke(foo: @JetComposable ScreenData<T>.() -> Unit) {
        this@Navigator.staticScreens[this] = { foo.invoke(this.cast()) }
    }


    fun navigateTo(screen: Screen<*>, parameters: Datamap = mapOf()) {
        screenState.value = ScreenStates.Exiting
        nextScreen = screen
        nextScreenParameters = parameters
    }

    init { this.foo() }
}