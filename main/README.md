### Основные компоненты:

1. **Модели (Entities)**
   - TranslationProject - Проекты перевода
   - SourceSegment - Исходные текстовые сегменты
   - Translation - Переведенные сегменты
   - EditorialComment - Редакционные комментарии
   - User - Пользователи системы

2. **Репозитории (Repositories)**
   - Все сущности имеют собственные репозитории с базовыми операциями CRUD
   - Хранение данных в памяти (ConcurrentHashMap)

3. **Сервисы (Services)**
   - Бизнес-логика для каждой сущности
   - Реализованы все базовые операции CRUD

4. **Контроллеры (Controllers)**
   - RESTful API для каждой сущности
   - Поддержка всех стандартных HTTP методов: GET, POST, PUT, DELETE

5. **Дополнительные компоненты**
   - Обработка ошибок (GlobalExceptionHandler)
   - Инициализация тестовых данных (TestDataInitializer)
   - Конфигурационный файл application.properties
   - README.md с документацией
   - Unit тесты

   
## API Endpoints

- `GET /api/translation-projects` - Получить все проекты
- `GET /api/source-segments` - Получить все сегменты
- `GET /api/translations` - Получить все переводы
- `GET /api/editorial-comments` - Получить все комментарии
- `GET /api/users` - Получить всех пользователей

## Тестирование

Проект включает автоматическую инициализацию тестовых данных:
- 3 пользователя (john_doe, editor_smith, admin_user)
- 2 проекта (Project Alpha, Project Beta)
- 3 исходных сегмента
- 3 перевода
- 2 редакционных комментария
