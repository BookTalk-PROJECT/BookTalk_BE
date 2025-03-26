> 데이터베이스 컨테이너 설정법

```
docker create --name mariadb -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -v c:\work\mariadbdata:/var/lib/mysql mariadb:latest

docker start mariadb

docker exec -it mariadb mariadb -uroot -proot -e "CREATE DATABASE IF NOT EXISTS <booktalk>;"

docker stop mariadb
```