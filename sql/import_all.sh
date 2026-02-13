#!/bin/bash
# Import all dummy data SQL files into MySQL
# Usage: ./import_all.sh [database_name] [mysql_user] [mysql_password]

DB_NAME="${1:-booktalk}"
MYSQL_USER="${2:-root}"
MYSQL_PWD="${3:-1234}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Importing dummy data into database: $DB_NAME"
echo "MySQL user: $MYSQL_USER"
echo ""

for sql_file in "$SCRIPT_DIR"/0*.sql; do
    if [ -f "$sql_file" ]; then
        filename=$(basename "$sql_file")
        echo "Importing $filename..."
        mysql -u "$MYSQL_USER" -p"$MYSQL_PWD" "$DB_NAME" < "$sql_file"
        if [ $? -eq 0 ]; then
            echo "  Done!"
        else
            echo "  Failed!"
            exit 1
        fi
    fi
done

echo ""
echo "All imports completed successfully!"
