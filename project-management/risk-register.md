# Реестр рисков

| ID | Риск | Вероятность | Влияние | Митигирующие действия |
|---|---|---|---|---|
| R-01 | Разрастание доменной логики в контроллерах/адаптерах | Средняя | Высокое | Архитектурные тесты и code review по слоям |
| R-02 | Неконсистентность file storage при конкуренции | Средняя | Среднее | JVM-lock + atomic write + backup + single-node ограничение |
| R-03 | Сложность интеграции с внешними каналами | Высокая | Среднее | SPI, stub-first подход, phased delivery |
| R-04 | Vendor lock-in на VisitManager | Средняя | Высокое | VisitCreation SPI + Custom REST client |
| R-05 | Нарушение i18n требований frontend | Средняя | Среднее | lint/check на hardcoded строки |
