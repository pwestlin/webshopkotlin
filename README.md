# Webshop
An extremely simple, reactive webshop implemented in Kotlin, microservices and Spring Boot Webflux.

## Application components
The application is composed by the following components:
* Service Discovery Server (Eureka).
* Customer Service
* Product Service
* Order Service
* Core Service

### Service discovery
Eureka.

## Build and test
```./gradlew build```

## Build Docker images
```./gradlew dockerBuildImage```

## Run application

**Note! It can take up to 90 seconds before all components have registered with Eureka.**

### Docker Compose 
Docker compose is the simplest way of tog get the entire application and all its microservices running: 
```docker-compose up```
or
```docker-compose up && docker-compose rm -fsv```
if you like the Docker containers to be removed after you shutdown the application (with ctrl+c).

The Core Service (port 8080) and Eureka Server (port 8761) are the only services exposed outside the Docker network.

### IDE
Run all the five applications separately.

### Testdata
The application (and its tests) are configured with some testdata from testdata/src/main/kotlin/nu/westlin/webshop/test/TestData.kt. 

### Endpoints
There are a few endpoints but I haven't yet documented them... :)
  
#### /customers
TODO petves: Table with examples.
POST a new order:
```json
{"id":99,"customerId":3,"timestamp":[2020,10,23,14,13,40,272000000],"orderRows":[{"productId":3,"quantity":3},{"productId":4,"quantity":4}]}
```
```http POST http://localhost:8080/orders < new_order.json```

## TODO's and musings
### R2DBC?

### GUI?

* List
    * customers
    * products
    * orders
* Add
    * customer
    * product
    * order

#### ReactJS?

#### Kotlin/JS?
[Building Web Applications with React and Kotlin/JS](https://play.kotlinlang.org/hands-on/Building%20Web%20Applications%20with%20React%20and%20Kotlin%20JS/01_Introduction)  
[Building a Full Stack Web App with Kotlin Multiplatform](https://play.kotlinlang.org/hands-on/Full%20Stack%20Web%20App%20with%20Kotlin%20Multiplatform/01_Introduction)  
[Kotlin wrappers](https://github.com/JetBrains/kotlin-wrappers/)

### DDD?
[DDD with Kotlin](https://tuhrig.de/ddd-with-kotlin/)

### Event sourcing?
* [Event sourcing](https://microservices.io/patterns/data/event-sourcing.html)
* [Event sourcing with Kotlin](https://tuhrig.de/event-sourcing-with-kotlin/)
    * [GitHub repo for the above](https://github.com/bringmeister/event-sourcing-with-kotlin)
* [Kotlin event sourcing example](https://github.com/nicusX/kotlin-event-sourcing-example)