package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val chartColors = listOf(
    Color(0xFFE57373),
    Color(0xFF64B5F6),
    Color(0xFF81C784),
    Color(0xFFFFD54F),
    Color(0xFFBA68C8),
    Color(0xFFFF8A65),
    Color(0xFF4DB6AC),
    Color(0xFFA1887F)
)

@Composable
fun StatisticsScreen(viewModel: ExpenseViewModel) {
    val allExpensesFlow = remember { viewModel.getAllExpensesForStats() }
    val expenses by allExpensesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle(initialValue = 0.0)
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val monthlyExpensesTotal by viewModel.monthlyExpenses.collectAsStateWithLifecycle()
    val budget by viewModel.currentBudget.collectAsStateWithLifecycle(initialValue = null)

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

        if (expenses.isNotEmpty()) {
            item {
                ChartPager(
                    expenses = expenses,
                    categoryExpenses = categoryExpenses,
                    totalExpenses = totalExpenses ?: 0.0,
                    monthlyExpensesTotal = monthlyExpensesTotal,
                    budgetAmount = budget?.amount,
                    categories = categories
                )
            }

            item {
                Text(
                    text = "Wydatki wedlug kategorii",
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

// ===== Pager z wykresami =====

@Composable
private fun ChartPager(
    expenses: List<Expense>,
    categoryExpenses: List<Pair<String, Double>>,
    totalExpenses: Double,
    monthlyExpensesTotal: Double,
    budgetAmount: Double?,
    categories: List<CustomCategory>
) {
    val pageCount = 5
    val pagerState = rememberPagerState(pageCount = { pageCount })

    val chartTitles = listOf(
        "Rozklad wydatkow",
        "Wydatki dzienne",
        "Trend miesiczny",
        "Budzet",
        "Top wydatki"
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
                text = chartTitles[pagerState.currentPage],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> PieChartPage(
                            categoryExpenses = categoryExpenses,
                            total = totalExpenses,
                            categories = categories
                        )
                        1 -> DailyBarChartPage(expenses = expenses)
                        2 -> MonthlyTrendPage(expenses = expenses)
                        3 -> BudgetGaugePage(
                            spent = monthlyExpensesTotal,
                            budget = budgetAmount
                        )
                        4 -> TopExpensesPage(
                            expenses = expenses,
                            monthlyTotal = monthlyExpensesTotal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kropki nawigacyjne
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ===== Strona 1: Wykres kolowy =====

@Composable
private fun PieChartPage(
    categoryExpenses: List<Pair<String, Double>>,
    total: Double,
    categories: List<CustomCategory>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        PieChart(
            data = categoryExpenses,
            total = total,
            colors = chartColors,
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChartLegend(
            categoryExpenses = categoryExpenses,
            colors = chartColors,
            categories = categories
        )
    }
}

// ===== Strona 2: Wykres slupkowy - dzienne wydatki =====

@Composable
private fun DailyBarChartPage(expenses: List<Expense>) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    val dailyExpenses = remember(expenses) {
        val cal = Calendar.getInstance()
        val daily = DoubleArray(daysInMonth)
        expenses.forEach { expense ->
            cal.timeInMillis = expense.date
            if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                val day = cal.get(Calendar.DAY_OF_MONTH) - 1
                daily[day] += expense.amount
            }
        }
        daily.toList()
    }

    val maxDaily = dailyExpenses.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxSize()) {
        // Etykieta max
        Text(
            text = String.format("%.0f zl", maxDaily),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val barWidth = size.width / daysInMonth
            val chartHeight = size.height - 20f

            dailyExpenses.forEachIndexed { index, amount ->
                val barHeight = if (maxDaily > 0) (amount / maxDaily * chartHeight).toFloat() else 0f
                val isToday = index == today - 1
                drawRoundRect(
                    color = if (isToday) todayColor else barColor,
                    topLeft = Offset(
                        x = index * barWidth + barWidth * 0.15f,
                        y = chartHeight - barHeight
                    ),
                    size = Size(barWidth * 0.7f, barHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Linia bazowa
            drawLine(
                color = textColor.copy(alpha = 0.3f),
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = 1f
            )
        }

        // Etykiety dni
        Row(modifier = Modifier.fillMaxWidth()) {
            val labels = listOf(1, daysInMonth / 4, daysInMonth / 2, daysInMonth * 3 / 4, daysInMonth)
            labels.forEachIndexed { i, day ->
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.weight(1f),
                    textAlign = if (i == labels.lastIndex) TextAlign.End
                        else if (i == 0) TextAlign.Start
                        else TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Podsumowanie
        val avg = dailyExpenses.take(today).let { days ->
            val nonZeroDays = days.count { it > 0 }.takeIf { it > 0 } ?: 1
            days.sum() / nonZeroDays
        }
        Text(
            text = String.format("Srednia dzienna: %.2f zl", avg),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// ===== Strona 3: Trend miesiczny =====

@Composable
private fun MonthlyTrendPage(expenses: List<Expense>) {
    val monthlyData = remember(expenses) {
        val cal = Calendar.getInstance()
        val data = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            cal.timeInMillis = expense.date
            val key = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            data[key] = (data[key] ?: 0.0) + expense.amount
        }
        data.toSortedMap().toList().takeLast(6)
    }

    if (monthlyData.size < 2) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Za malo danych - potrzeba min. 2 miesiace",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val maxAmount = monthlyData.maxOf { it.second }
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dateFormat = SimpleDateFormat("MMM", Locale("pl"))
    val parseFormat = SimpleDateFormat("yyyy-MM", Locale("pl"))

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = String.format("%.0f zl", maxAmount),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val chartHeight = size.height - 10f
            val stepX = size.width / (monthlyData.size - 1)

            val points = monthlyData.mapIndexed { index, (_, amount) ->
                Offset(
                    x = index * stepX,
                    y = (chartHeight - (amount / maxAmount * chartHeight)).toFloat()
                )
            }

            // Wypelnienie
            val fillPath = Path().apply {
                moveTo(points.first().x, chartHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, chartHeight)
                close()
            }
            drawPath(fillPath, fillColor)

            // Linia
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Punkty
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 6f,
                    center = point
                )
            }
        }

        // Etykiety miesiecy
        Row(modifier = Modifier.fillMaxWidth()) {
            monthlyData.forEachIndexed { index, (key, _) ->
                val date = parseFormat.parse(key)
                val label = if (date != null) dateFormat.format(date) else key
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.weight(1f),
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        monthlyData.lastIndex -> TextAlign.End
                        else -> TextAlign.Center
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Zmiana m/m
        if (monthlyData.size >= 2) {
            val current = monthlyData.last().second
            val previous = monthlyData[monthlyData.size - 2].second
            val change = if (previous > 0) ((current - previous) / previous * 100) else 0.0
            val changeText = if (change >= 0) "+%.1f%%" else "%.1f%%"
            Text(
                text = String.format("Zmiana m/m: $changeText", change),
                style = MaterialTheme.typography.bodySmall,
                color = if (change > 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ===== Strona 4: Gauge budzetu =====

@Composable
private fun BudgetGaugePage(spent: Double, budget: Double?) {
    if (budget == null || budget <= 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Ustaw budzet aby zobaczyc postep",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val percentage = (spent / budget).coerceIn(0.0, 1.5)
    val gaugeColor = when {
        percentage <= 0.6 -> Color(0xFF81C784)
        percentage <= 0.85 -> Color(0xFFFFD54F)
        else -> Color(0xFFE57373)
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24f
                val arcSize = size.minDimension - strokeWidth
                val topLeft = Offset(
                    (size.width - arcSize) / 2,
                    (size.height - arcSize) / 2
                )

                // Tlo luku
                drawArc(
                    color = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Wypelnienie
                val sweepAngle = (percentage * 270).toFloat().coerceAtMost(270f)
                drawArc(
                    color = gaugeColor,
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.0f%%", percentage * 100),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = gaugeColor
                )
                Text(
                    text = "budzetu",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.2f zl", spent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Wydano",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.2f zl", (budget - spent).coerceAtLeast(0.0)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = gaugeColor
                )
                Text(
                    text = "Pozostalo",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Prognoza
        val cal = Calendar.getInstance()
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (dayOfMonth > 1) {
            val dailyAvg = spent / dayOfMonth
            val forecast = dailyAvg * daysInMonth
            val forecastColor = if (forecast > budget) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            Text(
                text = String.format("Prognoza: %.2f zl", forecast),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = forecastColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ===== Strona 5: Top wydatki =====

@Composable
private fun TopExpensesPage(expenses: List<Expense>, monthlyTotal: Double) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    val monthlyExpenses = remember(expenses) {
        val cal = Calendar.getInstance()
        expenses.filter { expense ->
            cal.timeInMillis = expense.date
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
    }

    val topExpenses = remember(monthlyExpenses) {
        monthlyExpenses.sortedByDescending { it.amount }.take(5)
    }

    val dailyAvg = if (today > 0) monthlyTotal / today else 0.0

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Srednia dzienna
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Srednia dzienna",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = String.format("%.2f zl", dailyAvg),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Text(
            text = "Top 5 w tym miesiacu",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (topExpenses.isEmpty()) {
            Text(
                text = "Brak wydatkow w tym miesiacu",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val maxAmount = topExpenses.first().amount
            topExpenses.forEachIndexed { index, expense ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = expense.description.ifEmpty { expense.category },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                        // Pasek proporcjonalny
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((expense.amount / maxAmount).toFloat())
                                .height(4.dp)
                                .background(
                                    chartColors[index % chartColors.size],
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                    }
                    Text(
                        text = String.format("%.2f zl", expense.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ===== Wspolne komponenty =====

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
                text = "Laczne wydatki",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f zl", total),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                text = String.format("%.2f zl", amount),
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
                text = "Dodaj wydatki aby zobaczyc statystyki",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
