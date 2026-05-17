-- Création des bases de données
CREATE DATABASE reservationdb;
CREATE USER reservation_user WITH PASSWORD 'reservation_pass';
GRANT ALL PRIVILEGES ON DATABASE reservationdb TO reservation_user;

CREATE DATABASE notificationdb;
CREATE USER notification_user WITH PASSWORD 'notification_pass';
GRANT ALL PRIVILEGES ON DATABASE notificationdb TO notification_user;

-- Permissions schema public
\c reservationdb;
GRANT ALL ON SCHEMA public TO reservation_user;

\c notificationdb;
GRANT ALL ON SCHEMA public TO notification_user;
