package dev.robin.privacify.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.theme.GreenVibrant

@Composable
fun HostsEditorScreen(
	onBack: () -> Unit = {}
) {
	val context = LocalContext.current
	val viewModel: HostsEditorViewModel = viewModel(factory = HostsEditorViewModel.Factory)
	val state by viewModel.state.collectAsState()

	LaunchedEffect(state.statusMessage) {
		state.statusMessage?.let {
			Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
				Text(
					text = "Hosts Editor",
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Black
				)
			}
			Spacer(modifier = Modifier.height(16.dp))

			PrivacifyExpressiveCard {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Text(
						text = "Add Block Rule",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Black
					)
					Spacer(modifier = Modifier.height(8.dp))
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						OutlinedTextField(
							value = state.newDomain,
							onValueChange = { viewModel.onNewDomainChanged(it) },
							modifier = Modifier.weight(1f),
							shape = RoundedCornerShape(14.dp),
							singleLine = true,
							placeholder = { Text("domain.to.block") },
							colors = OutlinedTextFieldDefaults.colors(
								focusedBorderColor = MaterialTheme.colorScheme.primary,
								unfocusedBorderColor = MaterialTheme.colorScheme.outline,
								focusedContainerColor = MaterialTheme.colorScheme.surface,
								unfocusedContainerColor = MaterialTheme.colorScheme.surface
							)
						)
						Button(
							onClick = { viewModel.addBlockRule() },
							shape = RoundedCornerShape(14.dp),
							colors = ButtonDefaults.buttonColors(
								containerColor = MaterialTheme.colorScheme.primary,
								contentColor = MaterialTheme.colorScheme.onPrimary
							)
						) {
							Icon(
								imageVector = Icons.Outlined.Add,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(4.dp))
							Text(
								text = "Add",
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.Black
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = "HOSTS CONTENT",
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
				modifier = Modifier.padding(start = 4.dp)
			)
			Spacer(modifier = Modifier.height(8.dp))

			OutlinedTextField(
				value = state.content,
				onValueChange = { viewModel.onContentChanged(it) },
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				shape = RoundedCornerShape(16.dp),
				textStyle = MaterialTheme.typography.bodySmall.copy(
					fontWeight = FontWeight.Medium
				),
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = MaterialTheme.colorScheme.primary,
					unfocusedBorderColor = MaterialTheme.colorScheme.outline,
					focusedContainerColor = MaterialTheme.colorScheme.surface,
					unfocusedContainerColor = MaterialTheme.colorScheme.surface
				)
			)

			Spacer(modifier = Modifier.height(12.dp))

			Button(
				onClick = { viewModel.saveHosts() },
				modifier = Modifier
					.fillMaxWidth()
					.height(52.dp),
				shape = RoundedCornerShape(16.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = GreenVibrant,
					contentColor = MaterialTheme.colorScheme.onPrimary
				)
			) {
				Icon(
					imageVector = Icons.Outlined.Save,
					contentDescription = null,
					modifier = Modifier.size(18.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Save Changes",
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Black
				)
			}
		}
	}
}
