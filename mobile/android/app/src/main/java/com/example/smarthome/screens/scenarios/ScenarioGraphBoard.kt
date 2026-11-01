package com.example.smarthome.screens.scenarios

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinGreen
import kotlin.math.abs
import kotlin.math.roundToInt

enum class GraphNodeKind {
    TRIGGER,
    CONDITION,
    ACTION,
}

data class ScenarioGraphNodeView(
    val id: String,
    val kind: GraphNodeKind,
    val badge: String,
    val title: String,
    val subtitle: String,
)

data class ScenarioGraphEdgeView(
    val from: String,
    val to: String,
)

@Composable
fun ScenarioGraphBoard(
    nodes: List<ScenarioGraphNodeView>,
    edges: List<ScenarioGraphEdgeView>,
    positions: Map<String, Offset>,
    onMove: (String, Offset) -> Unit,
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val nodeWidth = with(density) { 150.dp.toPx() }
    val nodeHeight = with(density) { 92.dp.toPx() }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val nodesById = remember(nodes) { nodes.associateBy { it.id } }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clipToBounds(),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        pan += dragAmount
                    }
                },
        ) {
            val step = 22.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = gridColor,
                        radius = 1.1.dp.toPx(),
                        center = Offset(x, y),
                    )
                    y += step
                }
                x += step
            }

            edges.forEach { edge ->
                val sourceNode = nodesById[edge.from] ?: return@forEach
                val targetNode = nodesById[edge.to] ?: return@forEach
                val source = positions[edge.from] ?: return@forEach
                val target = positions[edge.to] ?: return@forEach
                val from = if (sourceNode.kind == GraphNodeKind.TRIGGER &&
                    targetNode.kind == GraphNodeKind.CONDITION
                ) {
                    Offset(source.x + pan.x + nodeWidth / 2f, source.y + pan.y + nodeHeight)
                } else {
                    Offset(source.x + pan.x + nodeWidth, source.y + pan.y + nodeHeight / 2f)
                }
                val to = if (sourceNode.kind == GraphNodeKind.TRIGGER &&
                    targetNode.kind == GraphNodeKind.CONDITION
                ) {
                    Offset(target.x + pan.x + nodeWidth / 2f, target.y + pan.y)
                } else {
                    Offset(target.x + pan.x, target.y + pan.y + nodeHeight / 2f)
                }

                val path = Path()
                if (abs(from.x - to.x) < nodeWidth / 2f) {
                    val midY = (from.y + to.y) / 2f
                    path.moveTo(from.x, from.y)
                    path.cubicTo(from.x, midY, to.x, midY, to.x, to.y)
                } else {
                    val midX = (from.x + to.x) / 2f
                    path.moveTo(from.x, from.y)
                    path.cubicTo(midX, from.y, midX, to.y, to.x, to.y)
                }
                drawPath(
                    path = path,
                    color = UjinCyan.copy(alpha = 0.55f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                )
                drawCircle(
                    color = UjinCyan.copy(alpha = 0.85f),
                    radius = 3.dp.toPx(),
                    center = to,
                )
            }
        }

        nodes.forEach { node ->
            val position = positions[node.id] ?: Offset.Zero
            GraphNodeCard(
                node = node,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (position.x + pan.x).roundToInt(),
                            (position.y + pan.y).roundToInt(),
                        )
                    }
                    .width(150.dp)
                    .height(92.dp),
                onDrag = { drag ->
                    onMove(node.id, (positions[node.id] ?: position) + drag)
                },
                onClick = { onNodeClick(node.id) },
            )
        }

        if (nodes.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Добавьте ЕСЛИ и ТОГДА",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Узлы можно перетаскивать, пустое место — панорама",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        IconButton(
            onClick = { pan = Offset.Zero },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
        ) {
            Icon(
                imageVector = Icons.Filled.CenterFocusStrong,
                contentDescription = "Центрировать",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun GraphNodeCard(
    node: ScenarioGraphNodeView,
    modifier: Modifier,
    onDrag: (Offset) -> Unit,
    onClick: () -> Unit,
) {
    val accent = when (node.kind) {
        GraphNodeKind.TRIGGER -> Color(0xFFFFB74D)
        GraphNodeKind.CONDITION -> UjinCyan
        GraphNodeKind.ACTION -> UjinGreen
    }

    Card(
        modifier = modifier
            .pointerInput(node.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, accent.copy(alpha = 0.9f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = node.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
                Text(
                    text = "•••",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = node.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = node.subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
