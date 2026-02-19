# Expense Tracker - Mobilna aplikacja do zarządzania wydatkami

**Projekt licencjacki - Informatyka**

Nowoczesna aplikacja mobilna na Androida umożliwiająca kompleksowe zarządzanie finansami osobistymi, ewidencję wydatków, budżetowanie, wydatki cykliczne oraz podział kosztów między osobami.

## Opis projektu

Expense Tracker to aplikacja mobilna stworzona w Android Studio z wykorzystaniem Jetpack Compose i architektury MVVM. Aplikacja pomaga użytkownikom w kontrolowaniu osobistych finansów poprzez:
- Śledzenie i kategoryzację codziennych wydatków
- Wizualizację danych w formie wykresów kołowych
- Zarządzanie miesięcznym budżetem z alertami
- Automatyzację wydatków cyklicznych
- Podział kosztów między osobami z śledzeniem spłat
- Powiadomienia push (przypomnienia, alerty budżetowe, wydatki cykliczne)

## Nawigacja

Aplikacja składa się z 5 głównych ekranów dostępnych z dolnego paska nawigacji:

| Zakładka | Ikona | Opis |
|----------|-------|------|
| Lista | Home | Główny ekran z listą wydatków |
| Statystyki | QueryStats | Wykresy i analiza wydatków |
| Budżet | AccountBalance | Zarządzanie miesięcznym budżetem |
| Cykliczne | Repeat | Wydatki cykliczne |
| Podziały | CallSplit | Podział kosztów między osobami |

## Funkcjonalności

### Zarządzanie wydatkami (Lista)
- **Dodawanie wydatków** - kwota, kategoria (z emoji), opis opcjonalny
- **Edycja wydatków** - modyfikacja istniejących wpisów
- **Usuwanie wydatków** - z dialogiem potwierdzenia
- **Karta podsumowania** - łączna suma wszystkich wydatków
- **Wyszukiwanie** - szukanie po opisie i kategorii
- **Filtrowanie czasowe** - Wszystkie / Dziś / Tydzień / Miesiąc
- **Filtrowanie po kategorii** - chipy z emoji kategorii
- **Łączenie filtrów** - możliwość jednoczesnego stosowania wielu filtrów
- **Czyszczenie filtrów** - szybkie resetowanie jednym przyciskiem

### Własne kategorie
- **8 domyślnych kategorii** - Jedzenie, Transport, Zakupy, Rozrywka, Rachunki, Zdrowie, Edukacja, Inne (z emoji)
- **Dodawanie własnych kategorii** - nazwa + emoji
- **Usuwanie kategorii** - tylko dla niestandardowych (domyślne są chronione)
- **Dialog zarządzania** - dostępny z ikony ustawień na ekranie głównym

### Statystyki i analiza
- **Wykres kołowy** - wizualizacja rozkładu wydatków według kategorii
- **Legenda wykresu** - kolorowe oznaczenia z emoji kategorii
- **Zestawienie kategorii** - lista z kwotami i procentowym udziałem, posortowana malejąco
- **Suma wszystkich wydatków** - karta z łączną kwotą

### Budżet miesięczny
- **Ustawianie budżetu** - definiowanie miesięcznego limitu wydatków
- **Pasek postępu** - wizualizacja wykorzystania budżetu (zielony/czerwony)
- **Wyświetlanie procentowe** - ile procent budżetu zostało wykorzystane
- **Pozostała kwota** - informacja o dostępnych środkach
- **Ostrzeżenie** - komunikat przy przekroczeniu budżetu
- **Automatyczny alert** - powiadomienie push gdy wydatki przekroczą budżet

### Wydatki cykliczne
- **Dodawanie wydatków cyklicznych** - kwota, kategoria, opis, częstotliwość
- **Częstotliwości** - Codziennie / Co tydzień / Co miesiąc / Co rok
- **Włączanie/wyłączanie** - przełącznik aktywności bez usuwania
- **Automatyczne generowanie** - worker w tle co godzinę sprawdza i tworzy wydatki
- **Powiadomienia** - informacja o każdym automatycznie dodanym wydatku

### Podział wydatków
- **Dzielenie kosztów** - przypisanie części wydatku do innej osoby
- **Wiele osób** - możliwość podziału jednego wydatku między kilka osób
- **Śledzenie spłat** - oznaczanie długów jako spłacone/niespłacone
- **Podsumowanie długów** - karta z łączną kwotą niespłaconych należności
- **Grupowanie po osobach** - widok długów pogrupowany wg dłużników
- **Usuwanie podziałów** - możliwość usunięcia pojedynczych wpisów

### Powiadomienia (3 kanały)
- **Alerty budżetowe** (priorytet: wysoki) - powiadomienie o przekroczeniu miesięcznego budżetu
- **Przypomnienia** (priorytet: domyślny) - codzienne przypomnienie o wpisywaniu wydatków
- **Wydatki cykliczne** (priorytet: niski) - informacja o automatycznie dodanych wydatkach

### Automatyzacja w tle (WorkManager)
- **RecurringExpenseWorker** - co 1h sprawdza i generuje wydatki cykliczne
- **BudgetCheckWorker** - co 6h sprawdza przekroczenie budżetu
- **DailyReminderWorker** - co 24h wysyła przypomnienie o wydatkach

## Technologie

### Architektura i wzorce
- **Język programowania:** Kotlin
- **Framework UI:** Jetpack Compose z Material Design 3
- **Architektura:** MVVM (Model-View-ViewModel)
- **Zarządzanie stanem:** StateFlow, Flow, collectAsStateWithLifecycle

### Główne biblioteki
| Biblioteka | Wersja | Zastosowanie |
|------------|--------|-------------|
| Room Database | 2.6.1 | Lokalna baza danych SQLite |
| Jetpack Compose BOM | 2024.09.00 | Deklaratywny UI |
| Material 3 | - | Komponenty Material Design |
| Material Icons Extended | 1.6.8 | Rozszerzony zestaw ikon |
| Lifecycle ViewModel Compose | 2.7.0 | ViewModel w Compose |
| WorkManager | 2.9.0 | Zadania w tle |
| KSP | 2.0.21-1.0.25 | Generowanie kodu Room |
| Chart | Beta-0.0.5 | Wykresy kołowe |

### Struktura bazy danych (Room, wersja 3)

**Tabela `expenses`**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | Long (PK) | Auto-generowane |
| amount | Double | Kwota wydatku |
| category | String | Nazwa kategorii |
| description | String | Opis (opcjonalny) |
| date | Long | Data wydatku (timestamp) |
| timestamp | Long | Czas utworzenia wpisu |

**Tabela `budgets`**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | Long (PK) | Auto-generowane |
| amount | Double | Kwota budżetu |
| month | Int | Miesiąc (1-12) |
| year | Int | Rok |

**Tabela `custom_categories`**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | Long (PK) | Auto-generowane |
| name | String | Nazwa kategorii |
| emoji | String | Emoji kategorii |
| isDefault | Boolean | Czy kategoria domyślna |

**Tabela `recurring_expenses`**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | Long (PK) | Auto-generowane |
| amount | Double | Kwota |
| category | String | Kategoria |
| description | String | Opis |
| frequency | String | DAILY/WEEKLY/MONTHLY/YEARLY |
| startDate | Long | Data rozpoczęcia |
| lastGeneratedDate | Long? | Ostatnie wygenerowanie |
| isActive | Boolean | Czy aktywny |

**Tabela `split_expenses`**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | Long (PK) | Auto-generowane |
| expenseId | Long | ID powiązanego wydatku |
| personName | String | Imię dłużnika |
| amount | Double | Kwota do spłaty |
| isPaid | Boolean | Status spłaty |

## Struktura projektu

```
app/src/main/java/com/maciejtyszczuk/expensetracker/
├── data/
│   ├── database/
│   │   ├── ExpenseDao.kt              # Interfejs dostępu do danych (Room DAO)
│   │   └── ExpenseDatabase.kt         # Konfiguracja bazy z domyślnymi kategoriami
│   └── model/
│       ├── Expense.kt                 # Model wydatku
│       ├── Budget.kt                  # Model budżetu
│       ├── CustomCategory.kt          # Model kategorii
│       ├── RecurringExpense.kt        # Model wydatku cyklicznego
│       └── SplitExpense.kt            # Model podziału wydatku
├── notification/
│   └── NotificationHelper.kt          # System powiadomień (3 kanały)
├── ui/
│   ├── components/
│   │   └── ExpenseItem.kt             # Komponent wydatku (edycja, podział, usuwanie)
│   ├── screens/
│   │   ├── MainScreen.kt              # Ekran główny z listą i filtrami
│   │   ├── StatisticsScreen.kt        # Wykres kołowy i zestawienia
│   │   ├── BudgetScreen.kt            # Zarządzanie budżetem
│   │   ├── RecurringExpenseScreen.kt   # Lista wydatków cyklicznych
│   │   ├── SplitOverviewScreen.kt      # Przegląd podziałów i długów
│   │   ├── AddExpenseDialog.kt         # Dialog dodawania/edycji wydatku
│   │   ├── AddRecurringExpenseDialog.kt # Dialog wydatku cyklicznego
│   │   ├── CategoryManagementDialog.kt # Dialog zarządzania kategoriami
│   │   └── SplitExpenseDialog.kt       # Dialog podziału wydatku
│   └── theme/
│       └── Theme.kt                    # Motyw Material Design 3
├── viewmodel/
│   └── ExpenseViewModel.kt            # ViewModel z logiką biznesową
├── worker/
│   ├── RecurringExpenseWorker.kt       # Worker generujący wydatki cykliczne
│   ├── BudgetCheckWorker.kt           # Worker sprawdzający budżet
│   └── DailyReminderWorker.kt         # Worker z codziennym przypomnieniem
└── MainActivity.kt                     # Nawigacja, uprawnienia, rejestracja workerów
```

## Instalacja i uruchomienie

### Wymagania
- Android Studio Hedgehog 2023.1.1 lub nowszy
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36
- Kotlin 2.0.21+
- Gradle 8.13+

### Kroki instalacji

1. **Sklonuj repozytorium**
```bash
git clone https://github.com/maciejtyszczuk/ExpenseTracker.git
cd ExpenseTracker
```

2. **Otwórz projekt w Android Studio**
   - File -> Open -> wybierz folder `ExpenseTracker/ExpenseTracker` (wewnętrzny folder z `build.gradle.kts`)
   - Zaczekaj na synchronizację Gradle

3. **Skonfiguruj urządzenie**
   - Emulator: Tools -> Device Manager -> Create Virtual Device (min. API 24)
   - Lub fizyczne urządzenie z włączonym USB Debugging

4. **Uruchom aplikację**
   - Kliknij Run (zielona strzałka) lub Ctrl+R (Mac) / Shift+F10 (Windows/Linux)

### Uprawnienia
Aplikacja wymaga następujących uprawnień:
- `POST_NOTIFICATIONS` (Android 13+) - powiadomienia push

## Praca licencjacka

### Cel projektu
Stworzenie praktycznej aplikacji mobilnej demonstrującej:
- Umiejętność projektowania i implementacji aplikacji Android
- Znajomość nowoczesnych technologii (Kotlin, Jetpack Compose, Material 3)
- Zastosowanie wzorców architektonicznych (MVVM)
- Pracę z bazą danych (Room) i reaktywnym programowaniem (Flow)
- Automatyzację zadań w tle (WorkManager)
- System powiadomień na Androidzie

## Autor

**Maciej Tyszczuk**
Informatyka - Praca licencjacka
Rok akademicki: 2025/2026

## Kontakt

maciejtyszczuk@gmail.com
