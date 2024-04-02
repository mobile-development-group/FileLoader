package com.mdgroup.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mdgroup.fileloader.FileLoader
import com.mdgroup.sample.ui.theme.FileLoaderTheme
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileDownloader = FileLoader(this)

        val urls = listOf(
            "https://raw.githubusercontent.com/mobile-development-group/fileloader/main/assets/kittens.jpeg",
        )

        val uuid = fileDownloader.load(
            urls,
//            directoryName = Environment.DIRECTORY_DOWNLOADS,
//            directoryType = DirType.DIR_EXTERNAL_PUBLIC
        )

        setContent {

            var state by remember { mutableStateOf("STOP") }
            var uris by remember { mutableStateOf<List<String>>(emptyList()) }
            var progress by remember { mutableIntStateOf(0) }
            var error by remember { mutableStateOf<Throwable?>(null) }

            LaunchedEffect(Unit) {
                fileDownloader.getUrisByIdAsFlow(uuid)
                    .onEach {
                        uris = it ?: emptyList()
                    }
                    .catch {
                        error = it
                    }
                    .launchIn(this)

                // OR
                fileDownloader.getWorkInfoByIdAsFlow(uuid)
                    .onEach {
                        state = it.state.name

                        progress = it.progress.getInt(FileLoader.KEY_PROGRESS, 0)

                        uris = it.outputData.getStringArray(FileLoader.OUTPUT_URIS)?.toList()
                            ?: emptyList()

                        error = fileDownloader.getThrowable(it)
                    }
                    .launchIn(this)
            }

            FileLoaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Text(
                            text = "State: $state",
                            modifier = Modifier.padding(16.dp)
                        )

                        if (uris.isEmpty()) {
                            Text(
                                text = "$progress/${urls.count()}",
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            uris.forEach {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = null
                                    )

                                    Text(text = "Url: $it")

                                    Button(onClick = {
                                        fileDownloader.remove(it)
                                        urls.toMutableList().remove(it)
                                    }) {
                                        Text(text = "Delete")
                                    }
                                }
                            }
                        }

                        error?.let {
                            Text(
                                text = "Error: ${it.localizedMessage}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FileLoaderTheme {
        Greeting("Android")
    }
}