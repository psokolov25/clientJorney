# Чеклист реализации

## Архитектура

- [ ] Бизнес-логика маршрута находится только в доменном ядре
- [ ] Channel adapters не содержат бизнес-логику маршрута
- [ ] Frontend не содержит бизнес-логику маршрута
- [ ] VisitManager не зашит как единственная СУО
- [ ] VisitCreationClient SPI выделен отдельно
- [ ] Frontend поставляется внутри backend-дистрибутива
- [ ] В production нет отдельного frontend-сервера

## Локализация

- [ ] Admin UI поддерживает русский язык
- [ ] Admin UI поддерживает английский язык
- [ ] Web Chat Widget поддерживает русский язык
- [ ] Web Chat Widget поддерживает английский язык
- [ ] Строки интерфейса вынесены в resource files
- [ ] В React-компонентах нет hardcoded UI-строк

## Выбор услуг пользователем

- [ ] RESULT-узел поддерживает AUTO_SELECTED_SERVICES
- [ ] RESULT-узел поддерживает USER_SELECTABLE_SERVICES
- [ ] Runtime API возвращает SERVICE_SELECTION
- [ ] REST endpoint подтверждения выбранных услуг реализован
- [ ] Web Chat Widget поддерживает выбор услуг
- [ ] Telegram adapter поддерживает выбор услуг
- [ ] Kafka/WebSocket используют единую модель SERVICE_SELECTION
- [ ] VisitCreationRequest получает только выбранные услуги
