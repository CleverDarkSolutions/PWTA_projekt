package com.example.pwta_projekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pwta_projekt.data.parsers.DxfParser
import com.example.pwta_projekt.data.parsers.StlParser
import com.example.pwta_projekt.data.repository.ModelRepository
import com.example.pwta_projekt.domain.usecases.GetModelListUseCase
import com.example.pwta_projekt.domain.usecases.LoadModelUseCase
import com.example.pwta_projekt.rendering.gestures.GestureHandler3D
import com.example.pwta_projekt.ui.components.*
import com.example.pwta_projekt.viewmodels.ModelViewerUiState
import com.example.pwta_projekt.viewmodels.ModelViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelViewerScreen(
    modelPath: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val viewModel: ModelViewerViewModel = viewModel(key = modelPath) {
        val repository = ModelRepository(context)
        val stlParser = StlParser()
        val dxfParser = DxfParser()
        val getModelListUseCase = GetModelListUseCase(repository)
        val loadModelUseCase = LoadModelUseCase(repository, stlParser, dxfParser)
        ModelViewerViewModel(getModelListUseCase, loadModelUseCase, modelPath)
    }

    val uiState by viewModel.uiState.collectAsState()
    val gestureHandler = remember { GestureHandler3D() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (val state = uiState) {
                            is ModelViewerUiState.Success3D -> state.modelInfo.filename
                            is ModelViewerUiState.Success2D -> state.modelInfo.filename
                            else -> "Model Viewer"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ModelViewerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ModelViewerUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Błąd",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is ModelViewerUiState.Success3D -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 3D Viewer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f)
                        ) {
                            Stl3DViewer(
                                model3D = state.model3D,
                                gestureHandler = gestureHandler,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Controls
                        ViewerControls(
                            onResetView = { gestureHandler.reset() }
                        )

                        // Info Panel
                        ModelInfoPanel(
                            modelInfo = state.modelInfo,
                            model3D = state.model3D,
                            modifier = Modifier.weight(0.4f)
                        )
                    }
                }

                is ModelViewerUiState.Success2D -> {
                    var resetTrigger by remember { mutableIntStateOf(0) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // 2D Viewer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f)
                        ) {
                            key(resetTrigger) {
                                Dxf2DViewer(
                                    model2D = state.model2D,
                                    onResetRequested = {},
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Controls
                        ViewerControls(
                            onResetView = { resetTrigger++ }
                        )

                        // Info Panel
                        ModelInfoPanel(
                            modelInfo = state.modelInfo,
                            model2D = state.model2D,
                            modifier = Modifier.weight(0.4f)
                        )
                    }
                }
            }
        }
    }
}
