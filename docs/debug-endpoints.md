# Debug Endpoints для Kubeflow Model Registry Client

Этот документ описывает все debug endpoints, реализованные в классе `DebugResource`. Каждый endpoint вызывает соответствующий метод клиента Model Registry с dummy-данными, логирует детали запроса и возвращает структурированный JSON ответ.

## Базовый URL

Все endpoints доступны по базовому URL:

```
http://localhost:8081/debug
```

Порт может быть изменён в `application.properties` (по умолчанию 8081).

## Формат ответа

Все debug endpoints возвращают JSON объект типа `DebugResponse` со следующей структурой:

```json
{
  "method": "название метода клиента",
  "timestamp": "временная метка",
  "parameters": { "параметр1": "значение", ... },
  "requestDetails": {
    "url": "относительный URL запроса",
    "headers": { "Content-Type": "application/json" },
    "httpMethod": "GET/POST/PATCH"
  },
  "response": {
    "status": "SUCCESS/ERROR",
    "data": "объект ответа (может быть null)"
  },
  "error": {
    "type": "класс исключения",
    "message": "сообщение об ошибке",
    "stackTrace": "стектрейс (если есть)"
  },
  "logTrace": ["лог сообщение 1", "лог сообщение 2", ...]
}
```

Если клиентский метод выполнился успешно, поле `response.data` содержит результат. Если произошла ошибка, поле `error` заполняется, а `response.status` становится `"ERROR"`. HTTP статус ответа всегда 200, чтобы не мешать просмотру логов.

## Список endpoints

### 1. Тест соединения с Model Registry

**Путь:** `GET /debug/test-connection`

**Описание:** Проверяет базовое подключение к Model Registry API, вызывая `client.getRegisteredModels` с параметрами по умолчанию.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/test-connection"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/registered_models?pageSize=10&orderBy=ID&sortOrder=ASC"
```

**Пример успешного ответа (debug):**
```json
"Connection successful! Found 0 models"
```

### 2. Создание тестовой модели (старый метод)

**Путь:** `GET /debug/create-test-model`

**Описание:** Создаёт тестовую зарегистрированную модель с уникальным именем и набором метаданных. Использует прямой вызов `client.createRegisteredModel` с dummy-данными.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/create-test-model"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X POST "http://localhost:8089/api/model_registry/v1alpha3/registered_models" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-model-'$(date +%s)'",
    "description": "Тестовая модель созданная через curl",
    "externalId": "test-external-'$(date +%s)'",
    "customProperties": {
      "framework": { "stringValue": "transformers" },
      "accuracy": { "doubleValue": 0.945 }
    },
    "readme": "# Тестовая модель",
    "maturity": "Generally Available",
    "language": ["en", "ru"],
    "tasks": ["text-classification", "sentiment-analysis"],
    "provider": "TestTeam",
    "license": "apache-2.0",
    "licenseLink": "https://www.apache.org/licenses/LICENSE-2.0"
  }'
```

**Примечание:** Этот метод возвращает объект `RegisteredModel` в случае успеха (статус 201), либо текст ошибки.

### 3. Найти зарегистрированную модель (findRegisteredModel)

**Путь:** `GET /debug/find-registered-model`

**Описание:** Вызывает `client.findRegisteredModel` с именем "test-model". Использует dummy-параметры.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/find-registered-model"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/registered_model?name=test-model"
```

**Параметры:**
- `name`: "test-model"
- `externalId`: null

### 4. Получить список зарегистрированных моделей (getRegisteredModels)

**Путь:** `GET /debug/get-registered-models`

**Описание:** Вызывает `client.getRegisteredModels` с параметрами пагинации по умолчанию.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/get-registered-models"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/registered_models?pageSize=10&orderBy=ID&sortOrder=ASC"
```

**Параметры:**
- `filterQuery`: null
- `pageSize`: 10
- `orderBy`: "ID"
- `sortOrder`: "ASC"
- `nextPageToken`: null

### 5. Создать зарегистрированную модель (createRegisteredModel)

**Путь:** `GET /debug/create-registered-model`

**Описание:** Вызывает `client.createRegisteredModel` с dummy-данными (созданными методом `createDummyRegisteredModelCreate`).

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/create-registered-model"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X POST "http://localhost:8089/api/model_registry/v1alpha3/registered_models" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "dummy-model-'$(date +%s)'",
    "description": "Dummy registered model created via debug",
    "externalId": "dummy-external-'$(date +%s)'",
    "customProperties": {
      "framework": { "stringValue": "transformers" },
      "accuracy": { "doubleValue": 0.945 },
      "iterations": { "intValue": 100 },
      "isProduction": { "boolValue": true }
    },
    "readme": "# Dummy Model",
    "maturity": "Generally Available",
    "language": ["en"],
    "tasks": ["classification"],
    "provider": "DebugTeam",
    "license": "mit",
    "licenseLink": "https://opensource.org/licenses/MIT"
  }'
```

**Параметры:**
- `request`: объект `RegisteredModelCreate` с полями name, description, customProperties и т.д.

### 6. Получить зарегистрированную модель по ID (getRegisteredModel)

**Путь:** `GET /debug/get-registered-model`

**Описание:** Вызывает `client.getRegisteredModel` с dummy ID "test-registered-model-id".

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/get-registered-model"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/registered_models/test-registered-model-id"
```

**Параметры:**
- `registeredModelId`: "test-registered-model-id"

### 7. Обновить зарегистрированную модель (updateRegisteredModel)

**Путь:** `GET /debug/update-registered-model`

**Описание:** Вызывает `client.updateRegisteredModel` с dummy ID и dummy объектом `RegisteredModelUpdate`.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/update-registered-model"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X PATCH "http://localhost:8089/api/model_registry/v1alpha3/registered_models/test-registered-model-id" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description via curl",
    "customProperties": {
      "framework": { "stringValue": "pytorch" }
    }
  }'
```

**Параметры:**
- `registeredModelId`: "test-registered-model-id"
- `update`: объект `RegisteredModelUpdate`

### 8. Получить версии зарегистрированной модели (getRegisteredModelVersions)

**Путь:** `GET /debug/get-registered-model-versions`

**Описание:** Вызывает `client.getRegisteredModelVersions` с dummy ID и параметрами пагинации.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/get-registered-model-versions/${model_id}"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/registered_models/${model_id}/versions?pageSize=10&orderBy=ID&sortOrder=ASC"
```

**Параметры:**
- `registeredModelId`: "test-registered-model-id"
- `name`: null
- `externalId`: null
- `filterQuery`: null
- `pageSize`: 10
- `orderBy`: "ID"
- `sortOrder`: "ASC"
- `nextPageToken`: null

### 9. Создать версию модели для зарегистрированной модели (createRegisteredModelVersion)

**Путь:** `GET /debug/create-registered-model-version`

**Описание:** Вызывает `client.createRegisteredModelVersion` с dummy ID и dummy объектом `ModelVersion`.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/create-registered-model-version"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X POST "http://localhost:8089/api/model_registry/v1alpha3/registered_models/test-registered-model-id/versions" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-version-'$(date +%s)'",
    "description": "Test version created via curl"
  }'
```

**Параметры:**
- `registeredmodelId`: "test-registered-model-id"
- `modelVersion`: объект `ModelVersion`

---

### 10. Найти версию модели (findModelVersion)

**Путь:** `GET /debug/find-model-version`

**Описание:** Вызывает `modelVersionClient.findModelVersion` с именем "test-version".

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/find-model-version"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/model_version?name=test-version"
```

**Параметры:**
- `name`: "test-version"
- `externalId`: null
- `parentResourceId`: null

### 11. Получить список версий моделей (getModelVersions)

**Путь:** `GET /debug/get-model-versions`

**Описание:** Вызывает `modelVersionClient.getModelVersions` с параметрами пагинации по умолчанию.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/get-model-versions"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/model_versions?pageSize=10&orderBy=ID&sortOrder=ASC"
```

**Параметры:**
- `filterQuery`: null
- `pageSize`: 10
- `orderBy`: "ID"
- `sortOrder`: "ASC"
- `nextPageToken`: null

### 12. Создать версию модели (createModelVersion)

**Путь:** `GET /debug/create-model-version`

**Описание:** Вызывает `modelVersionClient.createModelVersion` с dummy-данными (объект `ModelVersionCreate`).

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/create-model-version"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X POST "http://localhost:8089/api/model_registry/v1alpha3/model_versions" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "dummy-version-'$(date +%s)'",
    "description": "Dummy model version created via curl",
    "registeredModelId": "test-registered-model-id",
    "state": "ARCHIVED"
  }'
```

**Параметры:**
- `request`: объект `ModelVersionCreate`

### 13. Получить версию модели по ID (getModelVersion)

**Путь:** `GET /debug/get-model-version`

**Описание:** Вызывает `modelVersionClient.getModelVersion` с dummy ID "test-model-version-id".

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/get-model-version"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X GET "http://localhost:8089/api/model_registry/v1alpha3/model_versions/test-model-version-id"
```

**Параметры:**
- `modelversionId`: "test-model-version-id"

### 14. Обновить версию модели (updateModelVersion)

**Путь:** `GET /debug/update-model-version`

**Описание:** Вызывает `modelVersionClient.updateModelVersion` с dummy ID и dummy объектом `ModelVersionUpdate`.

**CURL команда (debug endpoint):**
```bash
curl -X GET "http://localhost:8081/debug/update-model-version"
```

**Прямой запрос к Model Registry API:**
```bash
curl -X PATCH "http://localhost:8089/api/model_registry/v1alpha3/model_versions/test-model-version-id" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated version description via curl",
    "state": "ARCHIVED"
  }'
```

**Параметры:**
- `modelversionId`: "test-model-version-id"
- `update`: объект `ModelVersionUpdate"

---

## Прямые запросы к Model Registry API

Для проверки работы самого сервиса Model Registry (не через debug endpoint) можно использовать прямые curl команды, перечисленные выше. Они обращаются непосредственно к API Model Registry по базовому URL `http://localhost:8089/api/model_registry/v1alpha3` (значение по умолчанию из `application.properties`).

**Важно:** Перед выполнением прямых запросов убедитесь, что сервер Model Registry запущен и доступен по указанному адресу. Если сервер не запущен, запросы завершатся ошибкой соединения.

## Как использовать

1. Запустите приложение Quarkus:
   ```bash
   ./mvnw quarkus:dev
   ```

2. Убедитесь, что Model Registry сервер доступен по адресу, указанному в `model.registry.url` (по умолчанию `http://localhost:8089`). Если сервер не запущен, debug endpoints вернут ошибку подключения, но всё же покажут логи и структуру ответа.

3. Используйте curl команды из таблицы выше для тестирования каждого endpoint.

4. Анализируйте ответы: поле `logTrace` содержит пошаговые логи вызова, `requestDetails` показывает детали HTTP запроса, `response.data` содержит результат вызова (если успешно), `error` содержит информацию об ошибке (если произошла).

## Примечания

- Все debug endpoints используют GET метод для удобства тестирования через браузер или curl.
- Dummy-данные генерируются внутри методов и не влияют на реальные данные в Model Registry (если только сервер не принимает их).
- При ошибках соединения или валидации ответ будет содержать поле `error` с деталями, но HTTP статус останется 200, чтобы клиент мог увидеть полный JSON.
- Для более сложного тестирования (передача кастомных параметров) можно расширить endpoints, добавив query-параметры.

## Пример полного ответа с ошибкой

```json
{
  "method": "findRegisteredModel",
  "timestamp": "2025-01-01T12:00:00Z",
  "parameters": {
    "name": "test-model",
    "externalId": null
  },
  "requestDetails": {
    "url": "/registered_model?name=test-model",
    "headers": {},
    "httpMethod": "GET"
  },
  "response": {
    "status": "ERROR",
    "data": null
  },
  "error": {
    "type": "jakarta.ws.rs.ProcessingException",
    "message": "Connection refused",
    "stackTrace": "..."
  },
  "logTrace": [
    "Invoking findRegisteredModel with parameters: name=test-model, externalId=null",
    "Error: jakarta.ws.rs.ProcessingException: Connection refused"
  ]
}