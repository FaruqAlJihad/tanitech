# Menggunakan base image Maven yang sudah dilengkapi JDK 17
FROM maven:3.8.5-openjdk-17 AS build

# Set direktori kerja di dalam kontainer
WORKDIR /app

# Salin seluruh isi folder backend ke dalam kontainer
COPY . .

# Jalankan proses build menggunakan perintah maven standar (mvn)
RUN mvn clean package -DskipTests

# Stage kedua untuk menjalankan aplikasi (ukuran lebih kecil)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Salin file .jar yang sudah jadi dari stage pertama
COPY --from=build /app/target/*.jar app.jar

# Buka port 8080
EXPOSE 8080

# Perintah untuk menjalankan Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]