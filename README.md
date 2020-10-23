An extremely simple, reactive webshop implemented in Kotlin, microservices and Spring Boot Webflux.

POST a new order:
```json
{"id":99,"customerId":3,"timestamp":[2020,10,23,14,13,40,272000000],"orderRows":[{"productId":3,"quantity":3},{"productId":4,"quantity":4}]}
```
http POST http://localhost:8080/orders < new_order.json
