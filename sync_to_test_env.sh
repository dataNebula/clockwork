#!/bin/bash
service_name="$1"
if test $service_name = "worker"
then
    mvn clean package -DskipTests;
    scp clockwork-worker/target/clockwork-worker-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/worker;
    scp clockwork-worker/target/clockwork-worker-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/worker;
elif test $service_name = "api"
then
    mvn clean package -DskipTests;
    scp clockwork-api/target/clockwork-api-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/api;
    scp clockwork-api/target/clockwork-api-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/api;
elif test $service_name = "master"
then
    mvn clean package -DskipTests;
    scp clockwork-master/target/clockwork-master-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/master;
    scp clockwork-master/target/clockwork-master-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/master;
elif test $service_name = "dfs"
then
    mvn clean package -DskipTests;
    scp clockwork-dfs/target/clockwork-dfs-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/dfs;
    scp clockwork-dfs/target/clockwork-dfs-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/dfs;
elif test $service_name = "web"
then
    mvn clean package -DskipTests;
    scp clockwork-web/target/clockwork-web-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/web;
    scp clockwork-web/target/clockwork-web-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/web;
elif test $service_name = "all"
then
    mvn clean package -DskipTests;
    scp clockwork-master/target/clockwork-master-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/master;
    scp clockwork-worker/target/clockwork-worker-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/worker;
    scp clockwork-api/target/clockwork-api-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/api;
    scp clockwork-dfs/target/clockwork-dfs-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/dfs;
    scp clockwork-web/target/clockwork-web-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/web;
    scp clockwork-master/target/clockwork-master-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/master;
    scp clockwork-worker/target/clockwork-worker-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/worker;
    scp clockwork-api/target/clockwork-api-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/api;
    scp clockwork-dfs/target/clockwork-dfs-1.0-SNAPSHOT.jar adxtest@node6:/data/adx/clockwork/dfs;
    scp clockwork-web/target/clockwork-web-1.0-SNAPSHOT.jar adxtest@node5:/data/adx/clockwork/web;
else
    echo 'Not support servive name, optional value is : api , worker , master , dfs , web, all'
fi
