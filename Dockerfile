# 1. Usar JDK 17 (o la versión que uses)
FROM eclipse-temurin:17-jdk-jammy

# 2. Instalar Python y dependencias de sistema
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# 3. Directorio de trabajo
WORKDIR /app

# 4. Copiar el archivo de dependencias de Python y instalarlas
# Asegúrate de tener un requirements.txt con pandas, sklearn, numpy
COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

# 5. Copiar el JAR generado (asegúrate de hacer 'mvn package' primero)
COPY target/*.jar app.jar

# 6. Copiar los scripts de python y el txt de entrenamiento
COPY src/scripts/python /app/src/scripts/python
COPY entrenamiento_tardanzas.txt /app/entrenamiento_tardanzas.txt

# 7. Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]