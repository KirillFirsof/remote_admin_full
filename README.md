Remote Admin Panel
Сервис для удаленного администрирования компьютеров. Позволяет мониторить состояние удаленных машин (загрузка CPU, свободная RAM) и выполнять на них команды через веб-интерфейс.

Архитектура проекта
Проект состоит из трех основных компонентов:

Сервер (server/) — Spring Boot приложение, предоставляющее REST API

Агент (agent/) — Java-приложение, устанавливаемое на удаленные компьютеры

Фронтенд (frontend/) — React-приложение с веб-интерфейсом

Требования
Для запуска проекта необходимо:

Java 21 или выше

Node.js 18+ и npm

PostgreSQL 14+

Maven (или можно использовать встроенный mvnw)

Быстрый старт
1. Клонирование репозитория
bash
git clone https://github.com/KirillFirsof/remote_admin_full.git
cd remote_admin_full
2. Настройка базы данных
Создайте базу данных в PostgreSQL:

sql
CREATE DATABASE remote_admin_db;
3. Запуск сервера
bash
cd server
# Настройте подключение к БД в src/main/resources/application.properties
mvn spring-boot:run
Сервер будет доступен по адресу: http://localhost:8080

Swagger-документация: http://localhost:8080/swagger-ui/index.html

4. Запуск агента
В отдельном терминале:

bash
cd agent
mvn clean package
java -jar target/agent-1.0-SNAPSHOT.jar
Или через Maven (удобно для разработки):

bash
mvn exec:java
Агент автоматически зарегистрируется на сервере и начнет отправлять метрики.

В релизе  уже есть сформированный jar файл. Можно разместить его в одной папке с start-agent.bat, в start-agent.bat необходимо прописать правильный путь до jar файла

5. Запуск фронтенда
В отдельном терминале:

bash
cd frontend
npm install
npm run dev
Фронтенд будет доступен по адресу: http://localhost:5173

Сборка production-версий
Сервер
bash
cd server
mvn clean package
java -jar target/server-0.0.1-SNAPSHOT.jar
Агент
bash
cd agent
mvn clean package
java -jar target/agent-1.0-SNAPSHOT.jar
Для запуска как службы Windows можно использовать start-agent.bat:

batch
@echo off
cd /d %~dp0
java -jar target/agent-1.0-SNAPSHOT.jar
pause
Фронтенд
bash
cd frontend
npm run build
Собранные файлы будут в папке dist/. Их можно развернуть на любом статическом хостинге.
