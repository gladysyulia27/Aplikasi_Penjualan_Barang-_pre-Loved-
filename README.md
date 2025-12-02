# Aplikasi Penjualan Barang Pre-Loved

Aplikasi web untuk penjualan barang pre-loved (barang bekas) yang dibuat menggunakan Spring Boot untuk Proyek Akhir mata kuliah Pemrograman Berorientasi Objek (PBO).

## Fitur

### Fitur Utama
- ✅ **Struktur Proyek Aplikasi** - Clean architecture dengan pemisahan layer yang jelas
- ✅ **Fitur Tambah Data** - Menambahkan produk baru dengan upload gambar
- ✅ **Fitur Ubah Data** - Mengubah informasi produk
- ✅ **Fitur Ubah Data Gambar** - Mengubah gambar produk
- ✅ **Fitur Hapus Data** - Menghapus produk
- ✅ **Fitur Tampilan Daftar Data** - Menampilkan daftar semua produk
- ✅ **Fitur Tampilan Detail Data** - Menampilkan detail produk
- ✅ **Fitur Tampilan Chart Data** - Statistik produk berdasarkan kategori dan kondisi
- ✅ **UI Aplikasi** - Interface yang menarik menggunakan Bootstrap 5

### Fitur Autentikasi
- Registrasi pengguna baru
- Login dengan JWT token
- Logout
- Proteksi endpoint berdasarkan user

### Entitas

#### User
- id: UUID
- name: String
- email: String
- password: String (dienkripsi dengan BCrypt)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

#### AuthToken
- id: UUID
- token: String
- userId: UUID
- createdAt: LocalDateTime

#### Product
- id: UUID
- userId: UUID
- name: String
- description: String
- price: BigDecimal
- category: String
- condition: String (New, Like New, Good, Fair)
- imageUrl: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

## Teknologi yang Digunakan

- **Spring Boot 4.0.0-RC1**
- **Spring Data JPA** - Untuk akses database
- **PostgreSQL** - Database
- **Thymeleaf** - Template engine
- **Bootstrap 5** - UI framework
- **Chart.js** - Untuk visualisasi data
- **JWT** - Untuk autentikasi
- **BCrypt** - Untuk enkripsi password

## Setup dan Instalasi

### Prerequisites
- Java 25
- Maven
- PostgreSQL

### Konfigurasi Database

1. Buat database PostgreSQL:
```sql
CREATE DATABASE db_preloved;
```

2. Salin file `application.properties.template` ke `application.properties`:
```bash
cp src/main/resources/application.properties.template src/main/resources/application.properties
```

3. Edit `application.properties` dan sesuaikan konfigurasi database:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/db_preloved
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Menjalankan Aplikasi

1. Install dependencies:
```bash
mvn clean install
```

2. Jalankan aplikasi:
```bash
mvn spring-boot:run
```

3. Akses aplikasi di browser:
```
http://localhost:8080
```

## Struktur Proyek

```
src/
├── main/
│   ├── java/org/delcom/app/
│   │   ├── entities/          # Entity classes (User, AuthToken, Product)
│   │   ├── repositories/      # JPA Repositories
│   │   ├── services/          # Business logic layer
│   │   ├── controllers/       # REST/Web controllers
│   │   └── configs/           # Configuration classes
│   └── resources/
│       ├── templates/         # Thymeleaf templates
│       ├── static/            # CSS, JS, images
│       └── application.properties.template
└── test/                      # Test files
```

## Endpoint

### Web Pages
- `GET /` - Halaman beranda
- `GET /products` - Daftar semua produk
- `GET /products/{id}` - Detail produk
- `GET /products/add` - Form tambah produk
- `GET /products/{id}/edit` - Form edit produk
- `GET /products/my-products` - Produk milik user
- `GET /charts` - Halaman statistik
- `GET /auth/login` - Halaman login
- `GET /auth/register` - Halaman registrasi

### API Endpoints
- `POST /auth/register` - Registrasi user baru
- `POST /auth/login` - Login user
- `POST /auth/logout` - Logout user
- `POST /products/add` - Tambah produk baru
- `POST /products/{id}/edit` - Update produk
- `POST /products/{id}/delete` - Hapus produk

## Testing

### Menjalankan Test
```bash
./mvnw test
```

### Menjalankan Test dengan Coverage
```bash
./mvnw clean test jacoco:report
```

### Membuka Laporan Coverage
- Windows: `start target\site\jacoco\index.html`
- Mac: `open target/site/jacoco/index.html`
- Linux: `xdg-open target/site/jacoco/index.html`

## Purpose

Proyek ini dibuat untuk tujuan **Pendidikan** sebagai Proyek Akhir mata kuliah Pemrograman Berorientasi Objek (PBO).

## Author

Dibuat untuk memenuhi requirements Proyek Akhir PBO dengan topik "Aplikasi Penjualan Barang Pre-Loved".
