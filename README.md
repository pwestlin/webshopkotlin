# Webshop
An extremely simple, reactive webshop implemented in Kotlin, microservices and Spring Boot Webflux.

## Application components
The application is built with the following microservices
* Customer
* Product
* Order
* Core

### Service discovery
A really simple form of service discovery is achieved by Docker network aliases.
Core: Application.yml:
```yml
customerService.baseUrl: http://customer:8080
```
docker-compose.yml:
```yml
customer:
image: nu.westlin.webshopkotlin/customer:0.1-SNAPSHOT
networks:
  webshop:
    aliases:
      - customer
```

## Build and test
```./gradlew build```

## Build Docker images
```./gradlew dockerBuildImage```

## Run application
```docker-compose up```

The Core microservice is the only one that is exposed outside the Docker network and therefore acts as the application facade. 

### Endpoints
There are a few endpoints but I haven't yet documented them... :)
  
####/customers
POST a new order:
```json
{"id":99,"customerId":3,"timestamp":[2020,10,23,14,13,40,272000000],"orderRows":[{"productId":3,"quantity":3},{"productId":4,"quantity":4}]}
```
```http POST http://localhost:8080/orders < new_order.json```
