@echo off
REM Import all dummy data SQL files into MySQL
REM Usage: import_all.bat [database_name] [mysql_user] [mysql_password]

SET DB_NAME=%1
SET MYSQL_USER=%2
SET MYSQL_PWD=%3
IF "%DB_NAME%"=="" SET DB_NAME=booktalk
IF "%MYSQL_USER%"=="" SET MYSQL_USER=root
IF "%MYSQL_PWD%"=="" SET MYSQL_PWD=1234

echo Importing dummy data into database: %DB_NAME%
echo MySQL user: %MYSQL_USER%
echo.

FOR %%f IN (%~dp00*.sql) DO (
    echo Importing %%~nxf...
    mysql -u %MYSQL_USER% -p%MYSQL_PWD% %DB_NAME% < "%%f"
    IF ERRORLEVEL 1 (
        echo   Failed!
        exit /b 1
    )
    echo   Done!
)

echo.
echo All imports completed successfully!
