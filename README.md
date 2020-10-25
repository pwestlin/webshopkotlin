# Webshop
An extremely simple, reactive webshop implemented in Kotlin, microservices and Spring Boot Webflux.

## Application components
The application is composed by the following components:
* Service Discovery Server
* Customer
* Product
* Order
* Core

### Service discovery
Eureka.

## Build and test
```./gradlew build```

## Build Docker images
```./gradlew dockerBuildImage```

## Run application
Docker compose is the simplest way of tog get the entire application and all its microservices running: 
```docker-compose up```

The Core microservice (port 8080) and Eureka Server (port 8761) are the only services exposed outside the Docker network. 

### Endpoints
There are a few endpoints but I haven't yet documented them... :)
  
#### /customers
POST a new order:
```json
{"id":99,"customerId":3,"timestamp":[2020,10,23,14,13,40,272000000],"orderRows":[{"productId":3,"quantity":3},{"productId":4,"quantity":4}]}
```
```http POST http://localhost:8080/orders < new_order.json```
