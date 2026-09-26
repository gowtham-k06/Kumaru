package com.kumaru.assistant.presentation.memory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.MemoryCategory
import com.kumaru.assistant.core.model.MemoryItem
import com.kumaru.assistant.core.model.MemoryOwner
import com.kumaru.assistant.core.model.MemorySource
import com.kumaru.assistant.presentation.components.GlassCard
import com.kumaru.assistant.presentation.components.KumaruBackground
import com.kumaru.assistant.presentation.components.KumaruMiniOrb
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassBorderPink
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clean, glassmorphic Memory Vault UI for Kumaru V0.2.6.
 *
 * Displays structured memories segmented by:
 * - Gowtham (Personal)
 * - Pavi (Personal)
 * - Shared (Relationship & Mutual Memories)
 *
 * Supports adding, editing, pinning, and deleting memories.
 */
@Composable
fun MemoryScreen(
    memories: List<MemoryItem>,
    onBack: () -> Unit,
    onDeleteMemory: (String) -> Unit,
    onPinMemory: (String, Boolean) -> Unit,
    onUpdateMemory: (MemoryItem) -> Unit,
    onAddMemory: (MemoryItem) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedOwner by remember { mutableStateOf(MemoryOwner.PERSONAL_GOWTHAM) }
    var selectedCategoryFilter by remember { mutableStateOf<MemoryCategory?>(null) }

    // Dialog state for adding / editing
    var memoryToEdit by remember { mutableStateOf<MemoryItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var memoryToDeleteId by remember { mutableStateOf<String?>(null) }

    val ownerMemories = memories.filter { it.owner == selectedOwner && !it.isArchived }
    val filteredMemories = ownerMemories.filter {
        selectedCategoryFilter == null || it.category == selectedCategoryFilter
    }.sortedWith(compareByDescending<MemoryItem> { it.isPinned }.thenByDescending { it.updatedAt })

    val gowthamCount = memories.count { it.owner == MemoryOwner.PERSONAL_GOWTHAM && !it.isArchived }
    val paviCount = memories.count { it.owner == MemoryOwner.PERSONAL_PAVI && !it.isArchived }
    val sharedCount = memories.count { it.owner == MemoryOwner.SHARED && !it.isArchived }

    KumaruBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // ==========================================
            // Top Navigation Bar
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x12000000))
                            .clip(CircleShape)
                            .background(GlassSurfaceWhite)
                            .border(1.dp, GlassBorderLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Memory Vault",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            KumaruMiniOrb(size = 14.dp)
                        }
                        Text(
                            text = "100% Local-first persistent context",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Add Memory Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AE05697))
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add memory",
                            tint = AccentPinkPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentPinkPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // Owner Segmented Tabs (Gowtham / Pavi / Shared)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x0C1E1B2E))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple(MemoryOwner.PERSONAL_GOWTHAM, "Gowtham", gowthamCount),
                    Triple(MemoryOwner.PERSONAL_PAVI, "Pavi", paviCount),
                    Triple(MemoryOwner.SHARED, "Shared", sharedCount)
                ).forEach { (owner, label, count) ->
                    val isSelected = selectedOwner == owner
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GlassSurfaceWhite else Color.Transparent)
                            .then(
                                if (isSelected) Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color(0x15000000))
                                else Modifier
                            )
                            .clickable {
                                selectedOwner = owner
                                selectedCategoryFilter = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AccentPinkPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0x22E05697) else Color(0x101E1B2E))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) AccentPinkPrimary else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // Category Horizontal Filter Chips
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "All" chip
                val isAllSelected = selectedCategoryFilter == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAllSelected) Color(0x22E05697) else Color(0x0C1E1B2E))
                        .border(
                            width = 1.dp,
                            color = if (isAllSelected) GlassBorderPink else GlassBorderLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCategoryFilter = null }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "All (${ownerMemories.size})",
                        fontSize = 11.5.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAllSelected) AccentPinkPrimary else TextSecondary
                    )
                }

                // Category chips present in this owner's vault
                val categories = MemoryCategory.values().filter { cat ->
                    ownerMemories.any { it.category == cat }
                }
                categories.forEach { category ->
                    val isCatSelected = selectedCategoryFilter == category
                    val count = ownerMemories.count { it.category == category }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCatSelected) Color(0x22E05697) else Color(0x0C1E1B2E))
                            .border(
                                width = 1.dp,
                                color = if (isCatSelected) GlassBorderPink else GlassBorderLight,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategoryFilter = category }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${category.name.lowercase().replaceFirstChar { it.uppercase() }} ($count)",
                            fontSize = 11.5.sp,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCatSelected) AccentPinkPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // Memory Items List
            // ==========================================
            if (filteredMemories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No memories here yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Say \"Remember that...\" to Kumaru or tap + Add above.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMemories, key = { it.id }) { memory ->
                        MemoryCard(
                            memory = memory,
                            onPinToggle = { onPinMemory(memory.id, !memory.isPinned) },
                            onEdit = { memoryToEdit = memory },
                            onDelete = { memoryToDeleteId = memory.id }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // ==========================================
    // Delete Confirmation Dialog
    // ==========================================
    if (memoryToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { memoryToDeleteId = null },
            title = { Text(text = "Forget Memory?", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text(text = "This memory will be permanently removed from Kumaru's local memory store.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        memoryToDeleteId?.let { onDeleteMemory(it) }
                        memoryToDeleteId = null
                    }
                ) {
                    Text(text = "Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memoryToDeleteId = null }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }

    // ==========================================
    // Add / Edit Memory Dialog
    // ==========================================
    if (showAddDialog) {
        MemoryEditorDialog(
            initialItem = null,
            defaultOwner = selectedOwner,
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                onAddMemory(newItem)
                showAddDialog = false
            }
        )
    }

    if (memoryToEdit != null) {
        MemoryEditorDialog(
            initialItem = memoryToEdit,
            defaultOwner = memoryToEdit!!.owner,
            onDismiss = { memoryToEdit = null },
            onSave = { updatedItem ->
                onUpdateMemory(updatedItem)
                memoryToEdit = null
            }
        )
    }
}

/**
 * Individual glass card representing a persistent memory item.
 */
@Composable
private fun MemoryCard(
    memory: MemoryItem,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(memory.updatedAt) { dateFormat.format(Date(memory.updatedAt)) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = GlassSurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Category Badge + Source + Pin + Edit + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AE05697))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = memory.category.name.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPinkPrimary,
                            letterSpacing = 0.3.sp
                        )
                    }

                    if (memory.source == MemorySource.USER_EXPLICIT) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1410B981))
                                .padding(horizontal = 6.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "EXPLICIT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                // Actions: Pin, Edit, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (memory.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (memory.isPinned) "Unpin" else "Pin",
                            tint = if (memory.isPinned) AccentPinkPrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Memory Content
            Text(
                text = memory.content,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Key & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (memory.key.isNotBlank()) {
                    Text(
                        text = "Topic: ${memory.key}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = formattedDate,
                    fontSize = 10.5.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Dialog for adding or editing a memory item.
 */
@Composable
private fun MemoryEditorDialog(
    initialItem: MemoryItem?,
    defaultOwner: MemoryOwner,
    onDismiss: () -> Unit,
    onSave: (MemoryItem) -> Unit
) {
    var contentText by remember { mutableStateOf(initialItem?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: MemoryCategory.PREFERENCE) }
    var selectedOwner by remember { mutableStateOf(initialItem?.owner ?: defaultOwner) }
    var topicKey by remember { mutableStateOf(initialItem?.key ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "New Memory" else "Edit Memory",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("Memory Fact or Preference") },
                    placeholder = { Text("e.g. Loves beach and mountain hikes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                OutlinedTextField(
                    value = topicKey,
                    onValueChange = { topicKey = it },
                    label = { Text("Topic Keyword (Optional)") },
                    placeholder = { Text("e.g. beach, anime, coffee") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MemoryCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x22E05697) else Color(0x0C1E1B2E))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GlassBorderPink else GlassBorderLight,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentPinkPrimary else TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (contentText.isNotBlank()) {
                        val item = initialItem?.copy(
                            content = contentText.trim(),
                            key = topicKey.trim().lowercase(),
                            category = selectedCategory,
                            owner = selectedOwner,
                            updatedAt = System.currentTimeMillis()
                        ) ?: MemoryItem(
                            content = contentText.trim(),
                            key = topicKey.trim().lowercase(),
                            category = selectedCategory,
                            owner = selectedOwner,
                            source = MemorySource.USER_EXPLICIT
                        )
                        onSave(item)
                    }
                },
                enabled = contentText.isNotBlank()
            ) {
                Text(text = "Save", fontWeight = FontWeight.Bold, color = AccentPinkPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextSecondary)
            }
        }
    )
}
