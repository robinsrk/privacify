package dev.robin.privacify.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HostsEditorScreen(
	onBack: () -> Unit = {}
) {
	val viewModel: HostsEditorViewModel = viewModel(factory = HostsEditorViewModel.Factory)
	val state by viewModel.state.collectAsState()
	val context = LocalContext.current

	LaunchedEffect(state.statusMessage) {
		state.statusMessage?.let { msg ->
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
			viewModel.clearStatusMessage()
		}
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp, vertical = 16.dp)
		) {
			// Header
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onBack) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = "Back"
					)
				}
				Spacer(modifier = Modifier.width(8.dp))
				Column {
					Text(
						text = "Hosts Editor",
						style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
					)
					Text(
						text = "/etc/hosts • Root Required",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
					)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			// Quick add domain
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				OutlinedTextField(
					value = state.newDomain,
					onValueChange = { viewModel.onNewDomainChanged(it) },
					modifier = Modifier.weight(1f),
					shape = RoundedCornerShape(12.dp),
					singleLine = true,
					placeholder = { Text("Block a domain...") },
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline,
						focusedContainerColor = MaterialTheme.colorScheme.surface,
						unfocusedContainerColor = MaterialTheme.colorScheme.surface
					)
				)
				Button(
					onClick = { viewModel.addBlockRule() },
					shape = RoundedCornerShape(12.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = Color(0xFFEF4444)
					)
				) {
					Text("Block", fontWeight = FontWeight.Bold)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			if (state.isLoading) {
				Column(
					modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					CircularProgressIndicator()
					Spacer(modifier = Modifier.height(8.dp))
					Text("Reading hosts file...", style = MaterialTheme.typography.bodySmall)
				}
			} else {
				// Editor
				OutlinedTextField(
					value = state.content,
					onValueChange = { viewModel.onContentChanged(it) },
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					shape = RoundedCornerShape(12.dp),
					textStyle = MaterialTheme.typography.bodySmall.copy(
						fontFamily = FontFamily.Monospace
					),
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline,
						focusedContainerColor = MaterialTheme.colorScheme.surface,
						unfocusedContainerColor = MaterialTheme.colorScheme.surface
					)
				)

				Spacer(modifier = Modifier.height(12.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					OutlinedButton(
						onClick = onBack,
						modifier = Modifier.weight(1f),
						shape = RoundedCornerShape(12.dp)
					) {
						Text("Cancel")
					}
					Button(
						onClick = { viewModel.saveHosts() },
						modifier = Modifier.weight(1f),
						shape = RoundedCornerShape(12.dp)
					) {
						Text("Save", fontWeight = FontWeight.Bold)
					}
				}
			}
		}
	}
}
