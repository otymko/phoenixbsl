[![Actions Status](https://github.com/otymko/phoenixbsl/workflows/Java%20CI/badge.svg)](https://github.com/otymko/phoenixbsl/actions)	
[![Download](https://img.shields.io/github/release/otymko/phoenixbsl.svg?label=download&style=flat)](https://github.com/otymko/phoenixbsl/releases/latest)	
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=phoenixbsl&metric=alert_status)](https://sonarcloud.io/dashboard?id=phoenixbsl)
# Phoenix BSL для 1С

## Описание

Проект позволяет анализировать и форматировать код 1С в конфигураторе. Инструмент основан на проекте [BSL LS](https://github.com/1c-syntax/bsl-language-server).

<img src="docs/assets/images/preview.png" alt="Превью PhoenixBSL" style="zoom:80%;" />

## Установка

Установить приложение можно двумя способами:
* Через msi из релизов или сборок GitHub Action
* Запустить отдельно jar файл (нужно подготовить каталоги, почти во всех случаях это нужно для тестирования данного проекта).

### Установка msi

1. Качаем из релизов файл msi.
2. Устанавливаем на компьютере.
3. Первый раз запускаем под администратором (иначе не работает обработка нажатий кнопок, пока проблема решается).

### Запуск через jar

1. На компьютере должна быть установлена Java не ниже 11 версии. Если нет - устанавливаем.
2. Создаем новый каталог, из релизов копируем файл jar.
3. В каталог создаем каталог app, извлекаем в него архив bsl-language-server_win.zip из релизов проекта [BSL LS](https://github.com/1c-syntax/bsl-language-server/releases/latest).
4. Запускаем jar файл из консоли:
```cmd
java -jar phoenix-{version}.jar 
```
где {version} - версия приложения.

Например:
```cmd
java -jar phoenix-0.3.3.jar 
```

## Как пользоваться

После запуска приложения в конфигураторе нажимаем в модуле с кодом:
* `CTRL` + `I` - анализ кода на замечания.
* `CTRL` + `K` - форматирование кода.
* `CTRL` + `J` - "исправить все в модуле" - автоматическое исправление определенных замечаний (см. "Быстрые исправления").

Так же стоит отметить, что анализ и форматирование работает по выделенному коду.

### Настройки
Настроки приложения размещены: Трей приложения -> Настройки.
Доступно следующее:
* Открыть каталог с логами приложения. Логирование ведется всегда. История сохраняется за последние 7 дней.
* `Использовать BSL LS jar` - определяет, будет ли запущен BSL LS через java. По умолчанию выключено.
* `Путь к JAVA` - путь к Java. Если значение `java` - то берется из PATH системы. 
Для запуска приложения требуется Java не ниже версии 11. 
* `Путь к BSL LS` - путь к приложению BSL LS. Либо это путь к jar файлу (если выключена опция `Использовать BSL LS jar`), либо это 
путь к файлу `bsl-language-server.exe`.
* `Свои настройки BSL LS` - признак использования своих настроек для BSL LS.
* `Путь к настройкам BSL LS` - путь к настройкам BSL LS. По умолчанию `.bsl-language-server.json`. Для работы полного пакета проверок требуется указать в конфигурационном файле путь к выгруженным метаданным.  
* `Группировать замечания` - при включенном флаге замечания группируются по их типу.

#### Поддержка SonarLint

Плагины поддержки 1С в SonarQube:
* [1C (BSL) Community Plugin](https://github.com/1c-syntax/sonar-bsl-plugin-community) (пока не поддерживается)
* [1C (BSL) Plugin от SilverBulleters ](https://silverbulleters.org/sonarqube) с версии 1.33

Есть возможность, используя SonarLint, проанализировать код 1С с помощью плагина 1С на сервере SonarQube. Для этого нужно добавить в конфигурационный файл приложения (`%USER_NAME%\phoenixbsl\Configuration.json`) настройку проекта в `projects`:

```json
{
    // ...
    "projects": [
        {
            "name": "local",
            "basePath": "C://Users//otymko//phoenixbsl//projects//context-collector//",
            "useSonarLint": true,
            "projectKey": "my-project1",
            "serverUrl": "http://localhost:9000",
            "serverId": "0000001",
            "token": "58b7eaaa76be14d94e470ab28376f30f6cc95f55"
        }
    ]
}
```

где свойства:

* `name` - имя проекта внутри приложения
* `basePath` - путь к рабочему каталогу проекта, в дальнейшем каталог должен содержать исходники конфигурации в формате **xml**. Есть каталог не существует, то приложение его создаст.
* `useSonarLint` - опция для включения SonarLint. Работает при правильно заполненных свойствах: `projectKey`, `serverUrl`, `token`. Свойство `serverId` пока не используется.
* `projectKey` - ключ проекта в SonarQube
* `serverUrl` - адрес сервера SonarQube, например: [https://open.checkbsl.org/](https://open.checkbsl.org/)
* `serverId` - идентификатор сервера SonarQube, пока не используется.
* `token` - токен безопасности, для подключения к SonarQube. Генерируется из профиля пользователя.

После заполнения проекта выше, в приложении будет доступен выбор проекта (в главном окне). Для удобства замечания разделены на:

* bsl-ls - замечания, полученные с помощью [BSL LS](https://github.com/1c-syntax/bsl-language-server)
* sonarlint - замечания, полученные с помощью плагинов 1С для SonarQube


## Быстрые исправления

С помощью "Исправить все в модуле" можно автоматически исправить следующие замечания:
* Каноническое написание ключевых слов `CanonicalSpellingKeywords`
* Пробел в начале комментария `SpaceAtStartComment`
* Выражение должно заканчиваться символом ";" `SemicolonPresence`

## Разработка

Разработка ведется по git flow. В разработке используется платформа JAVA не ниже 11 версии.

## Развитие
Идеи, фидбек, баги по проекту кидаем в раздел Досад [Issues](https://github.com/otymko/phoenixbsl/issues).


**P.S.** Зачем это, если есть Снегопат, Turboconf, SmartConfigurator и т.п.? 
Ответ -> использование языка Java, открытый исходный код, прокачка в разработке на Java.
