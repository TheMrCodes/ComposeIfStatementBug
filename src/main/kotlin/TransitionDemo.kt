import androidx.compose.desktop.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize
import system.nav.Navigator
import system.nav.ScreenData
import system.nav.ScreenKeeper
import system.nav.ViewModel



@ExperimentalMaterialApi
@ExperimentalFoundationApi
fun main() {
    Window(
        "Transition Test",
        IntSize(800, 600),
        centered = true
    ) {
        MaterialTheme(
            colors = if (true) darkColors() else lightColors()
        ) {
            TransitionDemo.navigator.render()
        }
    }
}

object TransitionDemo {

    private data class Entry(val name: String)


    object TransitionScreen: ScreenKeeper() {
        val Master by screen(TransitionModel::class)
        val Detail by screen(TransitionModel::class)
    }

    class TransitionModel: ViewModel() {
        override fun onCreate() {
            println("Hallo Welt :D")
        }
    }

    private val users = listOf(
        Entry("Adam"),
        Entry("Andrew"),
        Entry("Anna"),
        Entry("Boris"),
        Entry("Carl"),
        Entry("Donna"),
        Entry("Emily"),
        Entry("Fiona"),
        Entry("Grace"),
        Entry("Irene"),
        Entry("Jack"),
        Entry("Jake"),
        Entry("Mary"),
        Entry("Peter"),
        Entry("Rose"),
        Entry("Victor")
    )

    val navigator = Navigator(TransitionScreen.Master) {
        TransitionScreen.Master {
            Column {
                Text("Master")
                Button(onClick = { navigator.navigateTo(TransitionScreen.Detail) }) {
                    Text("Button")
                }
            }
        }

        TransitionScreen.Detail { EntryDetails(this) }
    }

    @Composable
    fun EntryDetails(data: ScreenData<TransitionModel>) {
        Column {
            Text("Detail")
            Button(onClick = {
                data.navigator.navigateTo(TransitionScreen.Master)
            }) {
                Text("Button")
            }
        }
    }

}