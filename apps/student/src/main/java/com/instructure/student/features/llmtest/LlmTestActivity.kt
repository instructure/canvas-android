/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.student.features.llmtest

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LlmTestActivity : ComponentActivity() {

    private val viewModel: LlmTestViewModel by viewModels()

    private val pickModel = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.onModelSelected(it, contentResolver) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)
        setContent {
            MaterialTheme {
                Scaffold { innerPadding ->
                    LlmTestScreen(
                        viewModel = viewModel,
                        onPickModel = { pickModel.launch(arrayOf("*/*")) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun LlmTestScreen(
    viewModel: LlmTestViewModel,
    onPickModel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var prompt by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("LLM Test", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // State indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("State: ", style = MaterialTheme.typography.labelLarge)
            Text(
                uiState.stateLabel,
                color = when {
                    uiState.isError -> MaterialTheme.colorScheme.error
                    uiState.isReady -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 8.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        val downloadProgress = uiState.downloadProgress
        if (downloadProgress != null) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { downloadProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        // Model controls
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPickModel,
                enabled = !uiState.isLoading && !uiState.isModelLoaded
            ) {
                Text("Load Model")
            }
            if (uiState.isModelLoaded) {
                Button(onClick = { viewModel.unloadModel() }) {
                    Text("Unload")
                }
            }
        }

        if (uiState.modelName != null) {
            Text(
                "Model: ${uiState.modelName}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Prompt input
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Prompt") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isModelLoaded && !uiState.isGenerating,
            minLines = 3,
            maxLines = 5
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.generate(prompt)
            },
            enabled = uiState.isModelLoaded && !uiState.isGenerating && prompt.isNotBlank()
        ) {
            Text(if (uiState.isGenerating) "Generating..." else "Send")
        }

        Spacer(Modifier.height(16.dp))

        // Output
        Text("Response:", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))

        val scrollState = rememberScrollState()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = uiState.response.ifEmpty { "(no response yet)" },
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(scrollState),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }

        // Error display
        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
