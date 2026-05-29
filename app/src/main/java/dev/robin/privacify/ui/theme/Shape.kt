package dev.robin.privacify.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PrivacifyShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val ExpressiveLargeIncreased = RoundedCornerShape(20.dp)
val ExpressiveExtraLargeIncreased = RoundedCornerShape(32.dp)
val ExpressiveExtraExtraLarge = RoundedCornerShape(48.dp)

object MdSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 16.dp
    val md = 24.dp
    val lg = 32.dp
    val xl = 48.dp
}
