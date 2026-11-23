package com.worldprices.converter.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.worldprices.converter.R
import com.worldprices.converter.data.model.Currency
import com.worldprices.converter.data.model.CurrencyCategory
import com.worldprices.converter.data.model.CurrencyRegistry
import com.worldprices.converter.ui.components.CurrencyFlag

@Composable
fun CurrencyPickerSheet(
    title: String,
    isRussian: Boolean,
    selectedCodes: Set<String>,
    multiSelect: Boolean,
    onDismiss: () -> Unit,
    onSelectCurrency: (Currency) -> Unit,
    onToggleCurrency: ((Currency) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val categories = listOf(
        CurrencyCategory.POPULAR to stringResource(id = R.string.cat_popular),
        CurrencyCategory.CIS to stringResource(id = R.string.cat_cis),
        CurrencyCategory.FIAT to stringResource(id = R.string.cat_fiat),
        CurrencyCategory.CRYPTO to stringResource(id = R.string.cat_crypto)
    )

    val all = CurrencyRegistry.getAll()
    val filtered = all.filter { currency ->
        currency.searchMatches(searchQuery, isRussian) &&
                (searchQuery.isNotBlank() || currency.category == categories[selectedCategoryIndex].first)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(id = R.string.search_hint)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ScrollableTabRow(
                        selectedTabIndex = selectedCategoryIndex,
                        edgePadding = 0.dp,
                        divider = {},
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        categories.forEachIndexed { index, pair ->
                            Tab(
                                selected = selectedCategoryIndex == index,
                                onClick = { selectedCategoryIndex = index },
                                text = {
                                    Text(
                                        text = pair.second,
                                        fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Currency List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.code }) { currency ->
                        val isSelected = selectedCodes.contains(currency.code.lowercase())
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (multiSelect) {
                                        onToggleCurrency?.invoke(currency)
                                    } else {
                                        onSelectCurrency(currency)
                                        onDismiss()
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            CurrencyFlag(currency = currency, size = 36.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currency.code.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currency.localizedName(isRussian),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = currency.symbol,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (multiSelect) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleCurrency?.invoke(currency) }
                                )
                            } else if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
