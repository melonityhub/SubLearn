package com.melonityhub.sublearn.feature.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.melonityhub.sublearn.core.data.repo.MyWord
import com.melonityhub.sublearn.core.data.repo.MyWordsRepository
import com.melonityhub.sublearn.core.design.SublearnTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/** My Words list with search and removal (LRN-1). Search is FTS4 prefix search through the repository. */
class MyWordsViewModel(private val repository: MyWordsRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val results: Flow<List<MyWord>> = query.flatMapLatest { repository.observe(it) }

    fun onQuery(text: String) {
        query.value = text
    }

    fun remove(id: Long) {
        viewModelScope.launch { repository.remove(id) }
    }

    fun setKnown(id: Long, known: Boolean) {
        viewModelScope.launch { repository.setKnown(id, known) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWordsScreen(viewModel: MyWordsViewModel, onBack: () -> Unit) {
    val words by viewModel.results.collectAsState(initial = emptyList())
    var search by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_words_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "back" }) {
                        Text("←")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(SublearnTokens.SpaceL)) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.onQuery(it)
                },
                label = { Text(stringResource(R.string.my_words_search)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (words.isEmpty()) {
                Text(
                    text = stringResource(R.string.my_words_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = SublearnTokens.SpaceXl),
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
                items(words, key = { it.id }) { word ->
                    WordRow(word, onRemove = { viewModel.remove(word.id) }, onKnown = { viewModel.setKnown(word.id, it) })
                }
            }
        }
    }
}

@Composable
private fun WordRow(word: MyWord, onRemove: () -> Unit, onKnown: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = SublearnTokens.SpaceXs),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(SublearnTokens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(word.term, style = MaterialTheme.typography.titleMedium)
                word.translation?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                word.contextSentence?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = word.markedKnown,
                onCheckedChange = onKnown,
                modifier = Modifier.semantics { contentDescription = "known" },
            )
            IconButton(onClick = onRemove, modifier = Modifier.semantics { contentDescription = "remove" }) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    }
}
