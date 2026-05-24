package dev.robin.privacify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import dev.robin.privacify.core.provider.ProFeature
import dev.robin.privacify.ui.theme.AutoGuardGlow
import dev.robin.privacify.ui.theme.AutoGuardPrimary

@Composable
fun PrivacifyCard(
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	Card(
		modifier = modifier
			.then(
				if (onClick != null) Modifier.clickable { onClick() }
				else Modifier
			),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		content()
	}
}

@Composable
fun PrivacifyExpressiveCard(
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	Card(
		modifier = modifier
			.then(
				if (onClick != null) Modifier.clickable { onClick() }
				else Modifier
			),
		shape = MaterialTheme.shapes.extraLarge,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 4.dp
		)
	) {
		content()
	}
}

@Composable
fun PrivacifyGradientCard(
	colors: List<Color>,
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	Box(
		modifier = modifier
			.clip(MaterialTheme.shapes.medium)
			.background(
				Brush.linearGradient(colors)
			)
			.then(
				if (onClick != null) Modifier.clickable { onClick() }
				else Modifier
			)
	) {
		content()
	}
}

@Composable
fun PrivacifyListItem(
	title: String,
	subtitle: String? = null,
	leadingIcon: ImageVector? = null,
	leadingIconTint: Color = MaterialTheme.colorScheme.primary,
	leadingIconBackground: Color = MaterialTheme.colorScheme.primaryContainer,
	trailing: @Composable (() -> Unit)? = null,
	onClick: (() -> Unit)? = null,
	modifier: Modifier = Modifier
) {
	PrivacifyCard(
		modifier = modifier.fillMaxWidth(),
		onClick = onClick
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (leadingIcon != null) {
				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(RoundedCornerShape(16.dp))
						.background(leadingIconBackground),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = leadingIcon,
						contentDescription = null,
						tint = leadingIconTint,
						modifier = Modifier.size(24.dp)
					)
				}
				Spacer(modifier = Modifier.width(16.dp))
			}

			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				if (subtitle != null) {
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = subtitle,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			if (trailing != null) {
				trailing()
			}
		}
	}
}

@Composable
fun PrivacifySwitch(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	val scale by animateFloatAsState(
		targetValue = if (checked) 1f else 0.95f,
		animationSpec = spring(stiffness = Spring.StiffnessMedium),
		label = "switch_scale"
	)

	Switch(
		checked = checked,
		onCheckedChange = onCheckedChange,
		enabled = enabled,
		modifier = modifier.scale(scale),
		colors = SwitchDefaults.colors(
			checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
			checkedTrackColor = MaterialTheme.colorScheme.primary,
			uncheckedThumbColor = MaterialTheme.colorScheme.outline,
			uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
		)
	)
}

@Composable
fun PrivacifyBadge(
	text: String,
	color: Color = MaterialTheme.colorScheme.primary,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(999.dp))
			.background(color.copy(alpha = 0.15f))
			.padding(horizontal = 10.dp, vertical = 4.dp)
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Black,
			color = color
		)
	}
}

@Composable
fun PrivacifyChip(
	text: String,
	color: Color = MaterialTheme.colorScheme.primary,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(10.dp))
			.background(color.copy(alpha = 0.12f))
			.padding(horizontal = 12.dp, vertical = 6.dp)
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Bold,
			color = color
		)
	}
}

@Composable
fun PrivacifySectionHeader(
	title: String,
	badge: String? = null,
	badgeColor: Color = MaterialTheme.colorScheme.primary,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 4.dp, vertical = 4.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = title.uppercase(),
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
		)
		if (badge != null) {
			PrivacifyChip(text = badge, color = badgeColor)
		}
	}
}

@Composable
fun PrivacifyIconBox(
	icon: ImageVector,
	tint: Color = MaterialTheme.colorScheme.primary,
	background: Color = MaterialTheme.colorScheme.primaryContainer,
	size: Int = 48,
	iconSize: Int = 24,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.size(size.dp)
			.clip(RoundedCornerShape(16.dp))
			.background(background),
		contentAlignment = Alignment.Center
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = tint,
			modifier = Modifier.size(iconSize.dp)
		)
	}
}

@Composable
fun PrivacifyStatusIndicator(
	status: String,
	color: Color,
	modifier: Modifier = Modifier
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
	) {
		Box(
			modifier = Modifier
				.size(8.dp)
				.clip(CircleShape)
				.background(color)
		)
		Spacer(modifier = Modifier.width(6.dp))
		Text(
			text = status,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Bold,
			color = color
		)
	}
}

@Composable
fun PrivacifyWarningBanner(
	text: String,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
	) {
		Row(
			modifier = Modifier.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(32.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(MaterialTheme.colorScheme.errorContainer),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = "!",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black,
					color = MaterialTheme.colorScheme.error
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
			)
		}
	}
}

@Composable
fun PrivacifyDivider(
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.height(1.dp)
			.padding(horizontal = 16.dp)
			.background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
	)
}

@Composable
fun PrivacifyAutoGuardCard(
	enabled: Boolean,
	onToggle: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val isPro = ProFeature.isAutoGuardAvailable()
	val showProDialog = remember { mutableStateOf(false) }

	val cardColors = listOf(
		AutoGuardPrimary,
		AutoGuardPrimary.copy(alpha = 0.85f),
		AutoGuardGlow.copy(alpha = 0.3f)
	)
	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.extraLarge)
			.background(
				Brush.linearGradient(
					colors = cardColors,
					start = androidx.compose.ui.geometry.Offset(0f, 0f),
					end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
				)
			)
			.padding(20.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = "Auto-Guard",
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Black,
					color = Color.White
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = if (enabled) "Automatically manages kill switches\nwhen sensors are in use"
					else "Intelligent sensor protection",
					style = MaterialTheme.typography.bodySmall,
					color = Color.White.copy(alpha = 0.85f)
				)
			}
			PrivacifySwitch(
				checked = enabled,
				onCheckedChange = { newValue ->
					if (isPro) {
						onToggle(newValue)
					} else {
						showProDialog.value = true
					}
				}
			)
		}
	}

	if (showProDialog.value) {
		PrivacifyProDialog(
			featureName = "Auto-Guard",
			description = "Automatically manage kill switches when sensors are in use.",
			onDismiss = { showProDialog.value = false }
		)
	}
}

private val PRO_URL = "https://www.patreon.com/posts/privacify-159119797"

@Composable
fun PrivacifyProDialog(
	featureName: String,
	description: String,
	onDismiss: () -> Unit
) {
	val uriHandler = LocalUriHandler.current

	Dialog(onDismissRequest = onDismiss) {
		Surface(
			shape = RoundedCornerShape(28.dp),
			color = MaterialTheme.colorScheme.surface,
			tonalElevation = 8.dp
		) {
			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(
							Brush.linearGradient(
								colors = listOf(AutoGuardPrimary, AutoGuardGlow.copy(alpha = 0.6f)),
								start = androidx.compose.ui.geometry.Offset(0f, 0f),
								end = androidx.compose.ui.geometry.Offset(
									Float.POSITIVE_INFINITY,
									Float.POSITIVE_INFINITY
								)
							)
						)
						.padding(top = 32.dp, bottom = 24.dp),
					contentAlignment = Alignment.Center
				) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Icon(
							imageVector = Icons.Default.Lock,
							contentDescription = null,
							modifier = Modifier.size(40.dp),
							tint = Color.White
						)
						Spacer(modifier = Modifier.height(12.dp))
						Text(
							text = "Pro Feature",
							style = MaterialTheme.typography.titleLarge,
							fontWeight = FontWeight.Black,
							color = Color.White
						)
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = featureName,
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							color = Color.White.copy(alpha = 0.85f)
						)
					}
				}

				Column(
					modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = description,
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center
					)

					Spacer(modifier = Modifier.height(6.dp))

					Text(
						text = "Upgrade to Pro to unlock this and other premium features.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
						textAlign = TextAlign.Center
					)

					Spacer(modifier = Modifier.height(20.dp))

					Button(
						onClick = {
							try {
								uriHandler.openUri(PRO_URL)
							} catch (_: Exception) {}
						},
						modifier = Modifier
							.fillMaxWidth()
							.height(48.dp),
						shape = RoundedCornerShape(14.dp),
						colors = ButtonDefaults.buttonColors(
							containerColor = AutoGuardPrimary
						)
					) {
						Icon(
							imageVector = Icons.Default.Favorite,
							contentDescription = null,
							modifier = Modifier.size(18.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							text = "Support on Patreon",
							fontWeight = FontWeight.Black
						)
					}

					Spacer(modifier = Modifier.height(8.dp))

					TextButton(onClick = onDismiss) {
						Text(
							text = "Maybe Later",
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
		}
	}
}
