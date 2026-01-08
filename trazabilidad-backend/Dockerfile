# Multi-stage build para optimizar el tamaño de la imagen
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Copiar código fuente
COPY src src

# Compilar aplicación (sin tests para deployment rápido)
RUN ./mvnw clean package -DskipTests -B

# Etapa final: imagen ligera para ejecución
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto (Render asigna dinámicamente via $PORT)
EXPOSE 8080

# Configuración de JVM para contenedores
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]