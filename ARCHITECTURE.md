# Архитектура приложения Game of Life

## Обзор

Приложение было рефакторено с использованием архитектуры **MVI (Model-View-Intent)** и **Clean Architecture** с разделением на слои.

## Структура проекта

```
com.example.gameoflive/
├── data/                          # Data Layer
│   ├── local/                     # Локальные источники данных
│   │   ├── LocalSimulationDataSource.kt
│   │   └── LocalSimulationDataSourceImpl.kt
│   ├── remote/                    # Удалённые источники данных
│   │   ├── RemoteSimulationDataSource.kt
│   │   └── RemoteSimulationDataSourceStub.kt
│   └── repository/                # Репозитории
│       ├── SimulationRepository.kt
│       └── SimulationRepositoryImpl.kt
├── domain/                        # Domain Layer
│   ├── model/                     # Доменные модели
│   │   └── SaveInfo.kt
│   └── usecase/                   # Use Cases
│       ├── SaveSimulationUseCase.kt
│       ├── LoadSimulationUseCase.kt
│       ├── TickSimulationUseCase.kt
│       └── SpawnOrganismUseCase.kt
├── presentation/                  # Presentation Layer
│   └── game/                      # MVI для игрового экрана
│       ├── GameIntent.kt
│       ├── GameState.kt
│       ├── GameEffect.kt
│       └── GameViewModel.kt
├── model/                         # Бизнес-модели (перенесены в domain)
│   ├── Simulation.kt
│   ├── Organism.kt
│   └── Genome.kt
└── ui/                           # UI компоненты
    ├── GameScreen.kt             # Обновлён для работы с MVI
    ├── MainMenuScreen.kt
    ├── LoadSimulationScreen.kt
    └── AppNavigation.kt
```

## Архитектурные принципы

### 1. Data Layer
- **LocalSimulationDataSource**: Интерфейс для работы с локальными данными (файлы, БД)
- **RemoteSimulationDataSource**: Интерфейс для работы с сервером (пока заглушка)
- **SimulationRepository**: Объединяет локальные и удалённые источники, предоставляет единый API

### 2. Domain Layer
- **UseCase**: Инкапсулируют бизнес-логику
  - `SaveSimulationUseCase`: Сохранение симуляции
  - `LoadSimulationUseCase`: Загрузка симуляции
  - `TickSimulationUseCase`: Выполнение тиков симуляции
  - `SpawnOrganismUseCase`: Создание новых организмов

### 3. Presentation Layer (MVI)
- **GameIntent**: Все действия пользователя
  - `Init`, `FastForward`, `CellClicked`, `Save`, `BackPressed`, etc.
- **GameState**: Состояние UI
  - `simulation`, `selectedOrganism`, `isSaving`, `showSaveDialog`
- **GameEffect**: Одноразовые события
  - `ShowSnackbar`, `NavigateBack`
- **GameViewModel**: Обрабатывает интенты, обновляет состояние, отправляет эффекты

## Поток данных

```
UI (GameScreen) 
    ↓ dispatch(intent)
GameViewModel 
    ↓ invoke(useCase)
UseCase 
    ↓ call(repository)
Repository 
    ↓ call(dataSource)
DataSource (Local/Remote)
```

## Преимущества новой архитектуры

1. **Разделение ответственности**: Каждый слой имеет чёткую роль
2. **Тестируемость**: UseCase и Repository легко тестировать
3. **Масштабируемость**: Легко добавлять новые функции
4. **Поддерживаемость**: Код структурирован и понятен
5. **Независимость**: Domain слой не зависит от Android

## Следующие шаги

1. Добавить Dependency Injection (Hilt)
2. Добавить Unit тесты для UseCase и Repository
3. Реализовать RemoteDataSource для синхронизации с сервером
4. Добавить кеширование в Repository
5. Создать UseCase для других экранов (меню, загрузка)

## Зависимости

- **Coroutines**: Асинхронное программирование
- **Flow**: Реактивные потоки данных
- **ViewModel**: Управление состоянием UI
- **Compose**: Современный UI фреймворк 