-- Grant access from Docker network
CREATE USER IF NOT EXISTS 'root'@'172.17.0.%' IDENTIFIED BY 'Tiozo#3715';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'172.17.0.%' WITH GRANT OPTION;

-- Grant access from any IP (less secure but works)
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'Tiozo#3715';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS problem_suggest;

FLUSH PRIVILEGES;