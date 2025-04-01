package com.example.tasteapp1

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.tasteapp1.ui.theme.TasteApp1Theme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.SharedPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TasteApp1Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Greeting(
                        { getData(this) },
                        { storeData(this, it) },
                    )
                }
            }
        }
    }
}

fun storeData(activity: MainActivity, data: String) {
    val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
    with(sharedPref.edit()) {
        putString("tasteApp1Note", data)
        apply()
    }
}
fun getData(activity: MainActivity): String {
    val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return ""
    return sharedPref.getString("tasteApp1Note", "default value") ?: return ""
}

@Composable
fun Greeting(getData: () -> String, storeData: (String) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(getData()) }
    Surface() {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                storeData(it)
            },
            label = { Text("Note") },
            modifier = modifier.padding(48.dp)

        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TasteApp1Theme {
        Greeting({ "none" }, { })
    }
}