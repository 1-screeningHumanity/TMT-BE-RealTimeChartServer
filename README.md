# Real Time Chart Server

## 📖 Description
한국 투자 증권 API를 활용하여 주식 데이터 관련된 서비스를 처리 합니다.
소켓 통신을 통해 실시간으로 주식 데이터를 수집하여 클라이언트에게 제공합니다.

## ⚙ Function
1. 주식 가격, 호가 데이터를 실시간 소켓 통신으로 데이터를 수집합니다. 
2. 데이터를 SSE 통신을 통해 실시간으로 클라이언트에게 제공합니다. 
   
## 🔧 Stack
 - **Language** : Java 17
 - **Library & Framework** : Spring Boot 3.3.0
 - **Database** : Reactive Redis
 - **ORM** : ""
 - **Deploy** : AWS EC2 / Jenkins
 - **Dependencies** : Spring WebFlux, Reactive Redis, Validation, Lombok, Model Mapper, Swagger, Eureka, Kakfa

## 🔧 Architecture
- **Design Patter** : Layered Architecture
- **Micro Service Architecture** : Spring Cloud
- **Event-Driven Architecture** : Kafka

## 👨‍👩‍👧‍👦 Developer
*  **강성욱** ([KangBaekGwa](https://github.com/KangBaekGwa))
*  **김도형** ([ddohyeong](https://github.com/ddohyeong))
*  **박태훈** ([hoontaepark](https://github.com/hoontaepark))
