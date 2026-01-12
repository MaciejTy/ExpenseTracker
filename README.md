# 💰 Expense Tracker - Mobilna aplikacja do zarządzania wydatkami

**Projekt licencjacki - Informatyka**

Nowoczesna aplikacja mobilna na Androida umożliwiająca kompleksowe zarządzanie finansami osobistymi, ewidencję wydatków oraz ich analizę.

## 📱 Opis projektu

Expense Tracker to aplikacja mobilna stworzona w Android Studio, która pomaga użytkownikom w kontrolowaniu osobistych finansów poprzez:
- Śledzenie codziennych wydatków
- Kategoryzację transakcji
- Wizualizację danych w formie wykresów
- Zarządzanie miesięcznym budżetem
- Zaawansowane filtrowanie i wyszukiwanie

## ✨ Funkcjonalności

### 🎯 Podstawowe funkcje
- **Dodawanie wydatków** - szybkie rejestrowanie transakcji z kwotą, kategorią, opisem i datą
- **Lista wydatków** - przejrzysty widok wszystkich transakcji z możliwością usuwania
- **Edycja wydatków** - modyfikacja istniejących wpisów
- **Kategorie** - 8 predefiniowanych kategorii z emoji (Jedzenie, Transport, Zakupy, Rozrywka, Rachunki, Zdrowie, Edukacja, Inne)

### 📊 Statystyki i analiza
- **Wykres kołowy** - wizualizacja wydatków według kategorii
- **Legenda wykresu** - kolorowe oznaczenia kategorii dla łatwej identyfikacji
- **Zestawienie kategorii** - szczegółowe podsumowanie z kwotami i procentami
- **Suma wydatków** - łączna kwota wszystkich transakcji

### 🔍 Filtrowanie i wyszukiwanie
- **Filtr czasowy** - Wszystkie / Dziś / Tydzień / Miesiąc
- **Filtr kategorii** - wyświetlanie wydatków z wybranej kategorii
- **Wyszukiwanie** - szukanie po opisie transakcji
- **Kombinowane filtry** - możliwość łączenia wielu filtrów jednocześnie
- **Przycisk czyszczenia** - szybkie resetowanie wszystkich filtrów

### 💰 Budżet miesięczny
- **Ustawianie budżetu** - definiowanie miesięcznego limitu wydatków
- **Pasek postępu** - wizualizacja wykorzystania budżetu
- **Ostrzeżenia** - powiadomienie o przekroczeniu limitu
- **Pozostała kwota** - informacja o dostępnych środkach

## 🛠 Technologie

### Architektura i wzorce
- **Język programowania:** Kotlin
- **Framework UI:** Jetpack Compose (nowoczesny, deklaratywny UI)
- **Architektura:** MVVM (Model-View-ViewModel)
- **Zarządzanie stanem:** StateFlow, Flow

### Główne biblioteki
- **Room Database** - lokalna baza danych SQLite
  - `androidx.room:room-runtime:2.6.1`
  - `androidx.room:room-ktx:2.6.1`
  - Obsługa Flow dla reaktywnych zapytań
  
- **Jetpack Compose** - nowoczesny toolkit UI
  - Material Design 3
  - Material Icons Extended
  - Lifecycle integration
  
- **Kotlin Coroutines** - asynchroniczne operacje
  - Flow dla reaktywnego programowania
  - StateFlow dla zarządzania stanem
  
- **KSP (Kotlin Symbol Processing)** - generowanie kodu Room

### Struktura bazy danych
```kotlin
// Tabela Expenses
- id: Long (Primary Key, Auto-generated)
- amount: Double
- category: String
- description: String
- date: Long (timestamp)
- timestamp: Long

// Tabela Budgets
- id: Long (Primary Key, Auto-generated)
- amount: Double
- month: Int (1-12)
- year: Int
```

## 📁 Struktura projektu

```
app/src/main/java/com/maciejtyszczuk/expensetracker/
│
├── data/
│   ├── database/
│   │   ├── ExpenseDao.kt          # Interfejs dostępu do danych
│   │   └── ExpenseDatabase.kt     # Konfiguracja Room Database
│   │
│   └── model/
│       ├── Expense.kt             # Model wydatku
│       ├── Budget.kt              # Model budżetu
│       └── ExpenseCategory.kt     # Enum kategorii
│
├── ui/
│   ├── components/
│   │   └── ExpenseItem.kt         # Komponent pojedynczego wydatku
│   │
│   ├── screens/
│   │   ├── MainScreen.kt          # Ekran główny z listą
│   │   ├── StatisticsScreen.kt    # Ekran statystyk
│   │   ├── BudgetScreen.kt        # Ekran budżetu
│   │   ├── AddExpenseDialog.kt    # Dialog dodawania wydatku
│   │   └── EditExpenseDialog.kt   # Dialog edycji wydatku
│   │
│   └── theme/
│       └── Theme.kt               # Motyw Material Design 3
│
├── viewmodel/
│   └── ExpenseViewModel.kt        # ViewModel z logiką biznesową
│
└── MainActivity.kt                # Główna aktywność z nawigacją
```

## 🚀 Instalacja i uruchomienie

### Wymagania
- Android Studio Hedgehog | 2023.1.1 lub nowszy
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36
- Kotlin 2.0.21+
- Gradle 8.0+

### Kroki instalacji

1. **Sklonuj repozytorium**
```bash
git clone https://github.com/twoj-username/expense-tracker.git
cd expense-tracker
```

2. **Otwórz projekt w Android Studio**
   - File → Open → wybierz folder projektu
   - Zaczekaj na synchronizację Gradle

3. **Skonfiguruj emulator lub urządzenie**
   - Uruchom emulator Android (Tools → Device Manager)
   - LUB podłącz fizyczne urządzenie z włączonym USB Debugging

4. **Uruchom aplikację**
   - Kliknij zieloną strzałkę ▶️ (Run)
   - LUB użyj skrótu: Shift + F10 (Windows/Linux) / Control + R (Mac)

## 📸 Zrzuty ekranu

*(Możesz dodać screenshoty aplikacji)*

## 🎓 Praca licencjacka

### Cel projektu
Stworzenie praktycznej aplikacji mobilnej demonstrującej:
- Umiejętność projektowania i implementacji aplikacji Android
- Znajomość nowoczesnych technologii (Kotlin, Jetpack Compose)
- Zastosowanie wzorców architektonicznych (MVVM)
- Pracę z bazą danych (Room)
- Tworzenie intuicyjnego interfejsu użytkownika

### Potencjalne rozszerzenia
- 📅 Kalendarz wydatków z widokiem miesięcznym
- 📤 Export danych do CSV/PDF
- 📈 Dodatkowe wykresy (wykres liniowy, słupkowy)
- 🔔 Powiadomienia o przekroczeniu budżetu
- 🌐 Synchronizacja z chmurą
- 👥 Wspólne budżety rodzinne
- 💱 Obsługa wielu walut
- 🎨 Personalizacja motywów

## 📝 Licencja

Ten projekt został stworzony na potrzeby pracy licencjackiej.

## 👨‍💻 Autor

**Maciej Tyszczuk**  
Informatyka - Praca licencjacka  
Rok akademicki: 2025/2026

---

## 🐛 Znane problemy i TODO

- [ ] Dodać możliwość wyboru daty przy dodawaniu wydatku
- [ ] Implementować ciemny motyw
- [ ] Dodać animacje przejść między ekranami
- [ ] Optymalizacja wydajności dla dużej liczby wydatków
- [ ] Testy jednostkowe i integracyjne

## 📞 Kontakt
maciejtyszczuk@gmail.com
Pytania? Problemy? Otwórz issue na GitHubie!

---

⭐ Jeśli podoba Ci się ten projekt, zostaw gwiazdkę na GitHubie!
