package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun StatisticsScreen(viewModel: ExpenseViewModel) {
    // Używamy wszystkich wydatków dla statystyk, nie filtrowanych
    val allExpensesFlow = remember { viewModel.getAllExpensesForStats() }
    val expenses by allExpensesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle(initialValue = 0.0)
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    // Grupowanie wydatków według kategorii
    val categoryExpenses = remember(expenses) {
        expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Statystyki",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            TotalExpenseCard(total = totalExpenses ?: 0.0)
        }

        if (categoryExpenses.isNotEmpty()) {
            item {
                PieChartCard(
                    categoryExpenses = categoryExpenses,
                    total = totalExpenses ?: 0.0,
                    categories = categories
                )
            }

            item {
                Text(
                    text = "Wydatki według kategorii",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(categoryExpenses) { (category, amount) ->
                CategoryStatItem(
                    category = category,
                    amount = amount,
                    percentage = (amount / (totalExpenses ?: 1.0)) * 100,
                    categories = categories
                )
            }
        } else {
            item {
                EmptyStatisticsState()
            }
        }
    }
}

@Composable
fun TotalExpenseCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Łączne wydatki",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f zł", total),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun PieChartCard(
    categoryExpenses: List<Pair<String, Double>>,
    total: Double,
    categories: List<CustomCategory> = emptyList()
) {
    val colors = listOf(
        Color(0xFFE57373), // Czerwony
        Color(0xFF64B5F6), // Niebieski
        Color(0xFF81C784), // Zielony
        Color(0xFFFFD54F), // Żółty
        Color(0xFFBA68C8), // Fioletowy
        Color(0xFFFF8A65), // Pomarańczowy
        Color(0xFF4DB6AC), // Turkusowy
        Color(0xFFA1887F)  // Brązowy
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rozkład wydatków",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            PieChart(
                data = categoryExpenses,
                total = total,
                colors = colors,
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Legenda
            ChartLegend(
                categoryExpenses = categoryExpenses,
                colors = colors,
                categories = categories
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<Pair<String, Double>>,
    total: Double,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f
        val strokeWidth = radius * 0.3f

        var startAngle = -90f

        data.forEachIndexed { index, (_, amount) ->
            val sweepAngle = (amount / total * 360f).toFloat()
            val color = colors[index % colors.size]

            // Rysuj wycinek
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    (size.width - canvasSize) / 2f + strokeWidth / 2,
                    (size.height - canvasSize) / 2f + strokeWidth / 2
                ),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryStatItem(
    category: String,
    amount: Double,
    percentage: Double,
    categories: List<CustomCategory> = emptyList()
) {
    val emoji = categories.find { it.name == category }?.emoji ?: "\uD83D\uDCE6"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%.1f%%", percentage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = String.format("%.2f zł", amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChartLegend(
    categoryExpenses: List<Pair<String, Double>>,
    colors: List<Color>,
    categories: List<CustomCategory> = emptyList()
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoryExpenses.forEachIndexed { index, (category, _) ->
            val emoji = categories.find { it.name == category }?.emoji ?: "\uD83D\uDCE6"
            val color = colors[index % colors.size]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kolorowy kwadrat
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = color)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Emoji i nazwa kategorii
                Text(
                    text = "$emoji $category",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun EmptyStatisticsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\uD83D\uDCCA",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak danych",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Dodaj wydatki aby zobaczyć statystyki",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
