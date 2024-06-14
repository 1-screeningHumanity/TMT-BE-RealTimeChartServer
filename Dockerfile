FROM openjdk:17
ENV TZ=Asia/Seoul
COPY build/libs/realTimeChartService-0.0.1.jar RealTimeServer.jar
ENTRYPOINT ["java", "-jar", "RealTimeServer.jar"]