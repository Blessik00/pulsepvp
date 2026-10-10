# PulsePvP (Fabric, Minecraft 1.21.4)

Клиентский мод с визуалами для PvP. Меню: **Right Shift** (клавишу можно сменить в настройках управления).

Функции: Target HUD, Damage Numbers, Hit Particles, Keystrokes + CPS, Armor HUD, Potion HUD,
PvP Item Counter, Combo + Reach, Info HUD (FPS/ping/XYZ), Damage Flash, меню с цветом и масштабом HUD.

## Как получить jar

### Вариант 1 — без установки чего-либо (GitHub Actions)
1. Создай пустой репозиторий на github.com и загрузи в него все файлы из этой папки (включая `.github`).
2. Открой вкладку **Actions** -> workflow **build** -> дождись зелёной галочки.
3. Внизу страницы запуска скачай артефакт **PulsePvP-jar**. Нужен файл `pulsepvp-1.0.0.jar` (без `-sources`).

### Вариант 2 — локально
Нужны JDK 21 и Gradle 8.12+:
    gradle build
Готовый jar: `build/libs/pulsepvp-1.0.0.jar`

## Установка
Fabric Loader 0.16+ и Fabric API для 1.21.4 -> положи jar в папку `mods`.

Если сборка ругается на версию Fabric API, возьми актуальную для 1.21.4 на https://fabricmc.net/develop и поправь `gradle.properties`.
