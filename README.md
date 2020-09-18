# Cache Coordination

This project attempts to get cache coordination working as implemented by hazelcast and configured like explained [here](https://blog.payara.fish/setting-up-cache-jpa-coordination-with-the-payara-platform-using-eclipselink-and-jms/hazelcast).

The project was created to support [this](https://stackoverflow.com/questions/63940674/setup-eclipselink-cache-coordination-in-payara) stackoverflow question.

# Requirements

- java8

# Installation

Start containers

    docker-compose up -d
    sleep 10

Log into payara so you can run asadmin commands

    docker exec -ti payara1 asadmin  login
    # admin/admin
    
    docker exec -ti payara2 asadmin  login
    # admin/admin

Set sql logging for debugging purposes

    docker exec -ti payara1 asadmin set-log-levels java.util.logging.ConsoleHandler=FINEST
    docker exec -ti payara2 asadmin set-log-levels java.util.logging.ConsoleHandler=FINEST

Deploy the postgresql driver library

    docker cp postgresql-42.2.6.jar payara1:/
    docker exec -ti payara1 asadmin add-library /postgresql-42.2.6.jar

    docker cp postgresql-42.2.6.jar payara2:/
    docker exec -ti payara2 asadmin add-library /postgresql-42.2.6.jar

Register database

	docker exec -ti payara1 asadmin create-jdbc-connection-pool --datasourceclassname org.postgresql.xa.PGXADataSource --restype javax.sql.XADataSource --property user=database:password=database:databaseName=database:serverName=db dbTestDatabase
	docker exec -ti payara1 asadmin create-jdbc-resource --connectionpoolid dbTestDatabase jdbc/dbTestDatabase

	docker exec -ti payara2 asadmin create-jdbc-connection-pool --datasourceclassname org.postgresql.xa.PGXADataSource --restype javax.sql.XADataSource --property user=database:password=database:databaseName=database:serverName=db dbTestDatabase
	docker exec -ti payara2 asadmin create-jdbc-resource --connectionpoolid dbTestDatabase jdbc/dbTestDatabase

Enable Hazelcast

	docker exec -ti payara1 asadmin set-hazelcast-configuration --enabled=true
	docker exec -ti payara2 asadmin set-hazelcast-configuration --enabled=true

Build

    mvn clean package
    
Deploy

    docker cp target/cache-coordination.war payara1:/
    docker exec -ti payara1 asadmin deploy /cache-coordination.war

    docker cp target/cache-coordination.war payara2:/
    docker exec -ti payara2 asadmin deploy /cache-coordination.war

# Shutdown

    docker-compose down
    
# Testing

As you can see in the persistence.xml L2 caching has been enabled and cache coordination as well.

When an entity in one server is updated the entity in the other server should as well.

Keep in mind though that the entity first needs to be queried once in each server after which its stored in the L2 cache.

## Create

	curl -X POST -s -H "Content-type: application/json" -s -v -d '{"field1": "test"}' "http://localhost:18080/cache-coordination/rest/hello/world"

### Response
```
{
    "field1": "test",
    "id": "a8d73e77-eb9f-474d-8e3a-f68c682b0508"
}
```

## Get

### Request

Server 1

	curl -X GET -s http://localhost:18080/cache-coordination/rest/hello/world/ad4fa830-31e2-435d-bad5-f97c2f2c9c70

### Response

```
{
    "field1": "test",
    "id": "a8d73e77-eb9f-474d-8e3a-f68c682b0508"
}
```

### Request

Server 2

	curl -X GET -s http://localhost:28080/cache-coordination/rest/hello/world/ad4fa830-31e2-435d-bad5-f97c2f2c9c70

### Response

```
{
    "field1": "test",
    "id": "a8d73e77-eb9f-474d-8e3a-f68c682b0508"
}
```

## Update

### Request

Server 1

	curl -X PUT -s "http://localhost:18080/cache-coordination/rest/hello/world/a8d73e77-eb9f-474d-8e3a-f68c682b0508/test2"

So the value of field1 has been set to test2. Cache coordination now should make sure the L2 cache in server2 is 
invalidated and the get call on server2 also will produce the updated value.
 
### Response

```
{
    "field1": "test2",
    "id": "a8d73e77-eb9f-474d-8e3a-f68c682b0508"
}
```

## Get

### Request

Server 1

	curl -X GET -s http://localhost:18080/cache-coordination/rest/hello/world/ad4fa830-31e2-435d-bad5-f97c2f2c9c70

### Response

```
{
    "field1": "test2",
    "id": "a8d73e77-eb9f-474d-8e3a-f68c682b0508"
}
```

### Request

Server 2

	curl -X GET -s http://localhost:28080/cache-coordination/rest/hello/world/ad4fa830-31e2-435d-bad5-f97c2f2c9c70

### Response

```
{
    "field1": "test",
    "id": "a8d73e77-eb9f-474d-8e3a-f68c682b0508"
}
```

As you see the value is still 'test' so cache coordination did not work.
