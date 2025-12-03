package dev.romainguy.graphics.flatpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.romainguy.graphics.flatpath.ui.theme.FlatPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlatPathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Editor()
                }
            }
        }
    }

    @Composable
    private fun Editor() {
        var analyticalSubdivision by remember { mutableStateOf(true) }
        var referenceRender by remember { mutableStateOf(true) }

        Column {
            QuadEditor(
                Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                analyticalSubdivision,
                referenceRender
            )
            Row {
                Checkbox(
                    checked = analyticalSubdivision,
                    onCheckedChange = { checked -> analyticalSubdivision = checked }
                )
                Text(
                    modifier = Modifier.align(CenterVertically),
                    text = "Analytical subdivision"
                )
                Spacer(Modifier.width(16.dp))
                Checkbox(
                    checked = referenceRender,
                    onCheckedChange = { checked -> referenceRender = checked }
                )
                Text(
                    modifier = Modifier.align(CenterVertically),
                    text = "Reference"
                )
            }
        }
    }
}
