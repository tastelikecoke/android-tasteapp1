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
import androidx.compose.ui.tooling.preview.Preview
import com.example.tasteapp1.ui.theme.TasteApp1Theme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TasteApp1Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TodoList(
                        { getData(this, it) },
                        { a, b -> storeData(this, a, b) },
                        { getDataCount(this) },
                    )
                }
            }
        }
    }
}

fun storeData(activity: MainActivity, data: String, index: Int) {
    val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
    with(sharedPref.edit()) {
        putString("tasteApp1Note${index}", data)
        apply()
    }
}
fun getData(activity: MainActivity, index: Int): String {
    val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return ""
    return sharedPref.getString("tasteApp1Note${index}", "") ?: return ""
}

fun getDataCount(activity: MainActivity): Int {
    val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return 1
    return sharedPref.getInt("tasteApp1Count", 1) ?: return 1
}

@Composable
fun TodoList(getData: (Int) -> String, storeData: (String, Int) -> Unit, getDataCount: () -> Int, modifier: Modifier = Modifier) {
    Column {
        val count = getDataCount()
        for (i in 0..<count)
            Entry(getData, storeData, i, modifier)
    }
}

@Composable
fun Entry(getData: (Int) -> String, storeData: (String, Int) -> Unit, index: Int, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(getData(index)) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            storeData(it, index)
        },
        label = { Text("Note") },
        modifier = modifier.padding(48.dp)

    )
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    TasteApp1Theme {
        TodoList({ "preview" }, { _, _ -> Unit }, { 1 })
    }
}