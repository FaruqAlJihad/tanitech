# Menggunakan base image Maven dengan JDK 17
FROM eclipse-temurin:17-jdk-alpine AS build

# Set direktori kerja di dalam kontainer
WORKDIR /app

# Salin semua file dari folder backend ke dalam kontainer
COPY . .

# Beri izin eksekusi untuk Maven Wrapper
RUN chmod +x ./mvnw

# Jalankan proses build Maven (menghasilkan file .jar)
RUN ./mvnw clean package -DskipTests

# Stage kedua untuk mengecilkan ukuran aplikasi
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Salin file .jar yang sudah jadi dari stage pertama
COPY --from=build /app/target/*.jar app.jar

EXPOSE 7860
ENTRYPOINT ["java", "-Dserver.port=7860", "-jar", "app.jar"]