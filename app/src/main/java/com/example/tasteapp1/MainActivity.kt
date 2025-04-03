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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TasteApp1Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TodoList(
                        DataAccessor(this)
                    )
                }
            }
        }
    }
}

interface IDataAccessor {
    fun storeData(data: String, index: Int): Unit
    fun getData(index: Int): String
    fun getDataCount(): Int
    fun setDataCount(count: Int): Unit
    fun removeData(data: String, index: Int): Unit
}

class DataAccessor(val activity : MainActivity) : IDataAccessor {
    override fun storeData(data: String, index: Int): Unit {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("tasteApp1Note${index}", data)
            apply()
        }
    }
    override fun getData(index: Int): String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return ""
        return sharedPref.getString("tasteApp1Note${index}", "") ?: return ""
    }

    override fun getDataCount(): Int {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return 1
        return sharedPref.getInt("tasteApp1Count", 1) ?: return 1
    }

    override fun setDataCount(count: Int): Unit {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("tasteApp1Count", count)
            apply()
        }
    }

    override fun removeData(data: String, index: Int): Unit {
        val count = getDataCount()
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            for (i in index..<count)
                putString("tasteApp1Note${i}", getData(i+1))
            apply()
        }

        setDataCount(count - 1)
    }
}


@Composable
fun TodoList(accessor: IDataAccessor, modifier: Modifier = Modifier) {
    var count by remember { mutableStateOf(accessor.getDataCount()) }

    Column (modifier = modifier.padding(24.dp)){
        for (i in 0..<count)
            Entry(accessor, { count -= 1 }, i, modifier)

        Button(
            onClick = {
                accessor.setDataCount(count + 1)
                count += 1
            },
            modifier = modifier.padding(12.dp)
        ) {
            Text("Add")
        }
    }


}

@Composable
fun Entry(accessor: IDataAccessor, subtractItems: () -> Unit, index: Int, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(accessor.getData(index)) }
    Row( ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                accessor.storeData(it, index)
            },
            label = { Text("Note") },
            modifier = modifier.fillMaxWidth(0.8f)

        )

        IconButton(
            onClick = {
                accessor.removeData("", index)
                subtractItems()
            },
        ) {
            Icon(Icons.Default.Clear, contentDescription = "Remove")
        }
    }
}


class DummyAccessor : IDataAccessor {
    override fun storeData(data: String, index: Int): Unit {}
    override fun getData(index: Int): String { return "Note" }
    override fun getDataCount(): Int { return 2 }
    override fun setDataCount(count: Int): Unit { return }
    override fun removeData(data: String, index: Int): Unit {}
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    TasteApp1Theme {
        TodoList(DummyAccessor())
    }
}