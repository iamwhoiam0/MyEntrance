🏠 MyEntrance — мобильное приложение для жителей МКД
MyEntrance — это современное Android-приложение, позволяющее жителям многоквартирных домов удобно общаться, получать новости, участвовать в обсуждениях и голосованиях, а также безопасно решать жилищные вопросы в единой мобильной системе.

📲 О проекте
MyEntrance — надежное коммуникативное пространство для жильцов дома.
Цель — упростить информирование, обмен мнениями и совместное принятие решений на уровне вашего дома.

✨ Основной функционал
Безопасная авторизация (Firebase Auth: смс и роли пользователей)

Новости дома — актуальные объявления

Общий чат — мгновенное общение с соседями

[TODO] Обсуждения — тематические ветки и форумы для решения вопросов

[TODO] Голосования — принятие коллективных решений онлайн

Вложения и изображения — обмен файлами через Supabase Storage / Firebase Storage

Плавная навигация и адаптивный дизайн (Material Design 3)

⚙️ Используемые технологии и подходы
Android (Kotlin)

Firebase Authentication — безопасная система входа

Firebase Realtime Database — хранение истории чата и данных

Supabase Storage — обмен файлами и изображениями

Navigation Component - навигация между фрагментами

Hilt - Dependency Injection

MVVM архитектура

Pusher (опционально) — real-time коммуникация

Retrofit — работа с HTTP-запросами

Glide — загрузка и оптимизация изображений

DataStore Preferences - хранения настроек

Material Design 3 — современный дизайн приложения

🖼️ Скриншоты
<img width="322" height="639" alt="image" src="https://github.com/user-attachments/assets/219960ac-2e79-4314-9e67-22a186c3e815" />
<img width="324" height="642" alt="image" src="https://github.com/user-attachments/assets/47071e5b-f317-4d13-8d8b-9e62f5f343c9" />
<img width="325" height="642" alt="image" src="https://github.com/user-attachments/assets/bae1648e-59c0-49b5-8119-0dfd03b2383d" />
<img width="325" height="642" alt="image" src="https://github.com/user-attachments/assets/43c444f0-c5f2-49ce-8b21-5a942e1833be" />
<img width="324" height="642" alt="image" src="https://github.com/user-attachments/assets/0f0fdd4f-2140-4b20-b522-ab8c4549fede" />
<img width="313" height="617" alt="image" src="https://github.com/user-attachments/assets/551ffa55-85a9-4769-8e3e-3f952e11fd32" />
<img width="313" height="617" alt="image" src="https://github.com/user-attachments/assets/53272762-0538-4e77-8b47-f9242a5c5003" />
<img width="313" height="617" alt="image" src="https://github.com/user-attachments/assets/0b42c103-0d9c-42d8-a3d8-ac8ee6dc25ae" />
<img width="313" height="617" alt="image" src="https://github.com/user-attachments/assets/5a947794-4427-4541-aaf4-f6180da7fb3e" />


🚀 Установка и запуск
Клонируйте репозиторий:

bash
git clone https://github.com/iamwhoiam0/myentrance.git
Откройте проект в Android Studio (2024.2 или новее)

Добавьте свои ключи Firebase/Supabase в файл local.properties или через систему secrets

Соберите и запустите приложение на эмуляторе или устройстве (Android 7.0+)

📝 Структура проекта (MVVM)
data — работа с данными, API, репозитории

domain — бизнес-сущности, usecases

presentation — UI: фрагменты, компоненты, ViewModel’и

utils — вспомогательные классы, ресурсы
