package com.melonityhub.sublearn.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melonityhub.sublearn.core.data.repo.RecentVideo
import com.melonityhub.sublearn.core.design.SublearnTokens
import com.melonityhub.sublearn.core.model.MediaSource
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.res.stringResource
import com.melonityhub.sublearn.feature.home.R

/**
 * Home (APP-2): open a local video or a URL, and resume recent videos. Only real data is shown:
 * the list is what the user has opened on this device.
 */
@Composable
fun HomeScreen(
    recent: Flow<List<RecentVideo>>,
    onPickVideo: () -> Unit,
    onOpenUrl: () -> Unit,
    onOpenRecent: (MediaSource) -> Unit,
    onRemoveRecent: (String) -> Unit,
) {
    val items by recent.collectAsState(initial = emptyList())
    Column(
        modifier = Modifier.fillMaxSize().padding(SublearnTokens.SpaceL),
        verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceM),
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
            Button(onClick = onPickVideo) { Text(stringResource(R.string.home_open_video)) }
            OutlinedButton(onClick = onOpenUrl) { Text(stringResource(R.string.home_open_url)) }
        }
        Text(stringResource(R.string.home_recent), style = MaterialTheme.typography.titleMedium)
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.home_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
            items(items, key = { it.uri }) { video ->
                RecentCard(
                    video = video,
                    onOpen = { onOpenRecent(MediaSource(video.uri, video.title)) },
                    onRemove = { onRemoveRecent(video.uri) },
                )
            }
        }
    }
}

@Composable
private fun RecentCard(video: RecentVideo, onOpen: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(SublearnTokens.SpaceM)) {
            Text(video.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = SublearnTokens.SpaceXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS),
            ) {
                LinearProgressIndicator(
                    progress = { video.progress },
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                )
                OutlinedButton(onClick = onRemove) { Text(stringResource(R.string.home_remove)) }
            }
        }
    }
}
