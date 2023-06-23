package app.revanced.manager.ui.screen.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.R
import app.revanced.manager.network.dto.ReVancedContributor
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.viewmodel.ContributorScreenViewModel
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributorScreen(
    onBackClick: () -> Unit,
    viewModel: ContributorScreenViewModel
) {
    val repositories = viewModel.getRepositories()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.contributors),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            repositories.forEach {
                ExpandableListCard(
                    title = it.name,
                    contributors = it.contributors
                )
            }
        }
    }
}

@Composable
fun ExpandableListCard(
    title: String,
    contributors: List<ReVancedContributor>
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.outlinedCardElevation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.outlinedCardColors(),
    ) {
        Column() {
            Row() {
                ListItem(
                    headlineContent = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    trailingContent = {
                        if (contributors.size > 0) {
                            IconButton(
                                onClick = { expanded = !expanded },
                            ) {
                                Icon(
                                    Icons.Outlined.ExpandMore,
                                    stringResource(R.string.expand_more)
                                )
                            }
                        }
                    },
                )
            }

            if (expanded) {
                contributors.forEach {
                    ListItem(
                        headlineContent = {
                            AsyncImage(
                                model = it.avatarUrl,
                                contentDescription = it.avatarUrl,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape)
                                    .width(100.dp)
                            )
                        },

                        trailingContent = {
                            Text(
                                text = it.username,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },

                    )
                }
            }
        }

    }
}