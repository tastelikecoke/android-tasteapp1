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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import java.time.Instant

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
    val NOTE_KEY: String
    val DURATION_KEY: String
    val DATE_KEY: String

    fun storeData(key: String, data: String, index: Int): Unit
    fun getData(key: String, index: Int): String
    fun getDataCount(): Int
    fun setDataCount(count: Int): Unit
    fun removeData(data: String, index: Int): Unit
    fun getProgress(index: Int): Double
    fun storeProgress(progress: Double, index: Int): Unit
}

fun durationToSeconds(duration: String): Double {
    if (duration == "0s") return 0.0
    if (duration == "") return 0.0

    if (duration.last() == 'd' || duration.last() == 'h' || duration.last() == 'm' || duration.last() == 's') {
        val substringNumber = duration.substring(0, duration.length - 1).toDoubleOrNull()
        if(substringNumber != null)
        {
            if(duration.last() == 'd') return (substringNumber * 86400.0)
            if(duration.last() == 'h') return (substringNumber * 3600.0)
            if(duration.last() == 'm') return (substringNumber * 60.0)
            if(duration.last() == 's') return substringNumber
        }
    }
    return 0.0
}

class DataAccessor(val activity : MainActivity) : IDataAccessor {
    override val NOTE_KEY = "Note"
    override val DURATION_KEY = "Duration"
    override val DATE_KEY = "Date"

    override fun storeData(key: String, data: String, index: Int): Unit {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("tasteApp1Note.${key}.${index}", data)
            apply()
        }
    }
    override fun getData(key: String, index: Int): String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return ""
        return sharedPref.getString("tasteApp1Note.${key}.${index}", "") ?: return ""
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
            for (i in index..<count) {
                putString("tasteApp1Note.${NOTE_KEY}.${i}", getData(NOTE_KEY, i+1))
                putString("tasteApp1Note.${DURATION_KEY}.${i}", getData(DURATION_KEY, i+1))
                putString("tasteApp1Note.${DATE_KEY}.${i}", getData(DATE_KEY, i+1))
            }
            apply()
        }

        setDataCount(count - 1)
    }
    override fun getProgress(index: Int): Double {
        val futureDateString = getData(DATE_KEY, index)
        val currentDate = Instant.now().epochSecond.toDouble()
        val futureDate = when (futureDateString) {
            "" -> currentDate
            else -> futureDateString.toDouble()
        }

        val durationSeconds = durationToSeconds(getData(DURATION_KEY, index))

        if (durationSeconds < 0.0001) return 1.0
        if (futureDate < currentDate) return 1.0
        if ((futureDate - currentDate) / durationSeconds < 0.0001) return 0.0
        return 1.0 - (futureDate - currentDate) / durationSeconds

    }
    override fun storeProgress(progress: Double, index: Int): Unit {
        val durationSeconds = durationToSeconds(getData(DURATION_KEY, index))
        val currentDate = Instant.now().epochSecond.toDouble()
        val remainingSeconds = ((1.0 - progress) * durationSeconds)
        val futureDate = currentDate + remainingSeconds

        storeData(DATE_KEY, futureDate.toString(), index)
    }
}


@Composable
fun TodoList(accessor: IDataAccessor, modifier: Modifier = Modifier) {
    var count by remember { mutableStateOf(accessor.getDataCount()) }

    Column (modifier = modifier.padding(12.dp)){
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
fun validateDuration(value: String): Boolean {

    if (value.last() == 'd' || value.last() == 'h' || value.last() == 'm' || value.last() == 's') {
        val substringNumber = value.substring(0, value.length - 1).toFloatOrNull()
        if(substringNumber != null)
        {
            return true
        }
    }
    return false
}

@Composable
fun Entry(accessor: IDataAccessor, subtractItems: () -> Unit, index: Int, modifier: Modifier = Modifier) {
    var text = remember { mutableStateOf(accessor.getData(accessor.NOTE_KEY, index)) }
    var duration = remember { mutableStateOf(accessor.getData(accessor.DURATION_KEY, index)) }
    var progress = remember { mutableDoubleStateOf(accessor.getProgress(index)) }
    val isShowDialog = remember { mutableStateOf(false) }

    if (isShowDialog.value) {
        EntryDialog(
            accessor,
            text,
            duration,
            subtractItems,
            index,
            {
                accessor.storeData(accessor.NOTE_KEY, text.value, index)
                if(validateDuration(duration.value))
                    accessor.storeData(accessor.DURATION_KEY, duration.value, index)
                else
                    duration.value = "0s"

                isShowDialog.value = false
            },
            modifier
        )
    }
    Column {
        Row( ) {
            Text(text.value, modifier=modifier.fillMaxWidth(0.5f))
            Text(duration.value, modifier=modifier.fillMaxWidth(0.4f))
            IconButton(
                onClick = {
                    isShowDialog.value = true
                },
                //modifier=modifier.fillMaxWidth(0.2f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
        LinearProgressIndicator(
            progress = { progress.value.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EntryDialog(accessor: IDataAccessor, text: MutableState<String>, duration: MutableState<String>, subtractItems: () -> Unit, index: Int, onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        {
            Column (modifier = modifier.padding(12.dp)) {

                OutlinedTextField(
                    value = text.value,
                    onValueChange = {
                        text.value = it
                    },
                    label = { Text("Note") }
                )

                OutlinedTextField(
                    value = duration.value,
                    onValueChange = {
                        duration.value = it
                    },
                    label = { Text("Duration") }
                )
                Row () {

                    Button(
                        onClick = {
                            accessor.removeData("", index)
                            subtractItems()
                        },
                    ) {
                        Text("Remove")
                    }

                    Button(
                        onClick = {
                            accessor.storeProgress(0.0, index)
                            onDismissRequest()
                        },
                    ) {
                        Text("Restart")
                    }
                    Button(
                        onClick = {
                            onDismissRequest()
                        },
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


class DummyAccessor : IDataAccessor {
    override val NOTE_KEY = "Note"
    override val DURATION_KEY = "Duration"
    override val DATE_KEY = "Date"

    override fun storeData(key: String, data: String, index: Int): Unit {}
    override fun getData(key: String, index: Int): String { return "Note" }
    override fun getDataCount(): Int { return 2 }
    override fun setDataCount(count: Int): Unit { return }
    override fun removeData(data: String, index: Int): Unit {}

    override fun getProgress(index: Int): Double {
        return 0.5
    }
    override fun storeProgress(progress: Double, index: Int): Unit {

    }
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    TasteApp1Theme {
        TodoList(DummyAccessor())
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListDialogPreview() {
    TasteApp1Theme {
        EntryDialog(DummyAccessor(), remember { mutableStateOf("Note")}, remember { mutableStateOf("0s")}, {}, 0, {})
    }
}