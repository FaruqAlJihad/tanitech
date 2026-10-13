# 🌱 TaniTech - Sistem Cerdas Rekomendasi Pemilihan Tanaman

TaniTech adalah **aplikasi web pendukung keputusan pertanian**. Pengguna memasukkan hasil uji tanah lahannya (kandungan N, P, K, pH, dan kelembaban), lalu sistem merekomendasikan tanaman yang paling cocok beserta rekomendasi pupuk dan perkiraan hasil panennya.

Dibuat oleh **Tim ROOT OR ROTI** untuk *Data Mining Competition* **ICONFEST 2026**.

---

## 🎯 Tujuan

Petani sering memilih tanaman berdasarkan kebiasaan, padahal kondisi hara dan pH tanah tiap lahan berbeda. Akibatnya hasil panen kurang optimal dan pemupukan boros. TaniTech dibuat untuk:

- membantu memilih tanaman yang sesuai dengan kondisi tanah, berdasarkan data dan bukan perkiraan;
- memberi acuan pemupukan yang sesuai kebutuhan tanaman terpilih;
- memberi gambaran perkiraan hasil panen pada kondisi buruk, normal, dan optimal;
- menerapkan *data mining* (klasifikasi pohon keputusan J48 dengan Weka) pada kasus nyata di bidang pertanian.

## ✨ Fitur

| Fitur | Keterangan |
|---|---|
| **Analisis Lahan** | Form input Nitrogen (N), Fosfor (P), Kalium (K) dalam mg/kg (0-200, maksimal 2 angka desimal), pH tanah (slider 0-14, warna berubah sesuai tingkat keasaman), dan kelembaban (slider 0-100%). Kolom NPK hanya menerima angka: huruf dan simbol diblokir saat mengetik, dan nilai di atas 200 ditolak. |
| **Laporan Rekomendasi** | Menampilkan hingga 3 tanaman hasil prediksi model, lengkap dengan kebutuhan hara, pH optimal, rekomendasi pupuk beserta fungsinya, dan estimasi hasil panen (buruk / normal / optimal). |
| **Daftar Tanaman WEKA** | Tab berisi database seluruh 138 tanaman yang dikenali model, beserta kebutuhan hara dan rekomendasi pupuknya. |
| **Penanganan galat** | Validasi input di sisi frontend (N, P, K harus 0-200 mg/kg), serta pesan jelas jika server gagal merespons atau tidak ada tanaman yang cocok untuk lahan ekstrem. |

## ⚙️ Cara Kerja

```
Pengguna isi form  ->  Frontend (React)  --POST /api/predict-->  Backend (Spring Boot)
                                                                      |
                                                  normalisasi input ke skala 0-1
                                                                      |
                                              Model Weka J48 (tanitech-final.model)
                                                                      |
                       3 tanaman dengan probabilitas tertinggi  <-----+
                                      |
        Frontend mencocokkannya dengan database tanaman  ->  Laporan rekomendasi
```

1. Frontend mengirim `n, p, k, ph, moisture` ke backend.
2. Backend menormalkan nilai ke skala 0-1 (N dan P dibagi 200, K dibagi 500, kelembaban dibagi 100, pH dibagi 14), sesuai skala data latih model.
3. Model **J48 (pohon keputusan C4.5) Weka** menghitung probabilitas tiap tanaman. Backend mengambil maksimal 3 tanaman dengan probabilitas tertinggi. Jika model hanya menghasilkan satu kandidat, backend menambah kandidat dengan mengubah nilai NPK sebesar +10% dan -10%.
4. Frontend memetakan nama tanaman hasil model ke database `src/data/sayuran.js` (nama lokal, ikon, kebutuhan hara, pupuk, estimasi hasil).

### Tentang model

| Aspek | Nilai |
|---|---|
| Algoritma | J48 (C4.5), Weka 3.8.x |
| Dataset | "Dataset V6 terbaru" |
| Atribut masukan | 5: `N`, `P`, `K`, `humidity`, `ph` |
| Kelas keluaran | `label`, 138 jenis tanaman |
| Ukuran pohon | 11.601 daun, kedalaman 33 |
| File | `backend/src/main/resources/tanitech-final.model` (±67 MB) |

## 🧰 Teknologi

- **Frontend:** React 18, Vite 5, Tailwind CSS 3
- **Backend:** Java 17, Spring Boot 3.2.3, Weka 3.8.6 (`weka-stable`)
- **Deploy:** Vercel (frontend), Railway dengan Docker (backend)

## 📁 Struktur Folder

```
tanitech/
├── frontend/                       React + Vite
│   └── src/
│       ├── App.jsx                 logika utama dan pemanggilan API
│       ├── components/             Header, FormInput, HasilAnalisis, DaftarTanaman
│       └── data/sayuran.js         database 138 tanaman
└── backend/                        Spring Boot + Weka
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/com/tanitech/
        │   ├── controller/PredictionController.java   endpoint POST /api/predict
        │   └── dto/                                   PredictionRequest, PredictionResponse
        └── resources/tanitech-final.model             model Weka
```

## 🚀 Cara Menjalankan

### Prasyarat

| Kebutuhan | Versi | Cek |
|---|---|---|
| JDK | 17 atau lebih baru | `java -version` |
| Maven | 3.8 atau lebih baru | `mvn -version` |
| Node.js | 18 atau lebih baru (+ npm) | `node -v` |

Perlu internet saat pertama kali untuk mengunduh dependensi. Perlu **dua terminal** yang berjalan bersamaan.

### 1. Jalankan backend (Terminal 1)

```bash
cd backend
mvn spring-boot:run
```

Tunggu sampai muncul `Started TanitechApplication`. Backend berjalan di **http://localhost:8080**. Memuat model 67 MB membutuhkan beberapa detik dan memori yang cukup; jika gagal karena memori, jalankan dengan `MAVEN_OPTS="-Xmx1g" mvn spring-boot:run`.

### 2. Arahkan frontend ke backend lokal

> ⚠️ **Penting.** Di `frontend/src/App.jsx`, alamat API saat ini tertulis langsung ke server produksi:
> `https://tanitech-production.up.railway.app/api/predict`.
> Jika frontend dijalankan tanpa mengubahnya, hasil yang tampil berasal dari server Railway, **bukan** backend lokal.

Untuk menguji backend lokal, ubah baris `fetch(...)` di `App.jsx` menjadi:

```js
const response = await fetch('http://localhost:8080/api/predict', {
```

Kembalikan ke alamat Railway sebelum *deploy*.

### 3. Jalankan frontend (Terminal 2)

```bash
cd frontend
npm install        # sekali saja
npm run dev
```

Buka **http://localhost:5173** di browser.

### Build produksi frontend

```bash
cd frontend
npm run build      # hasil di frontend/dist
npm run preview    # uji hasil build
```

## 🧪 Pengujian Cepat

Dengan backend hidup:

```bash
curl -X POST http://localhost:8080/api/predict \
  -H "Content-Type: application/json" \
  -d '{"n":90,"p":42,"k":43,"ph":6.5,"moisture":82}'
```

Contoh response:

```json
{
  "status": "success",
  "kelas_tanah": "Tanah Terklasifikasi Model Weka",
  "tanaman_terpilih_list": ["rice", "..."]
}
```

Di browser: isi form, klik tombol analisis, lalu pastikan laporan menampilkan kartu tanaman. Buka tab **Daftar Tanaman WEKA** untuk melihat database 138 tanaman. Di DevTools (F12) tab *Network*, request `predict` harus berstatus **200**.

## 🔌 API

### `POST /api/predict`

| Field | Arti | Catatan |
|---|---|---|
| `n` | Nitrogen (mg/kg) | wajib |
| `p` | Fosfor (mg/kg) | wajib |
| `k` | Kalium (mg/kg) | wajib |
| `ph` | pH tanah | wajib, 0-14 |
| `moisture` | Kelembaban (%) | opsional, bawaan 60 |

Response sukses berisi `tanaman_terpilih_list` (maksimal 3 nama tanaman sesuai label model). Jika model belum termuat, response berstatus `500` dengan `{"error": "Model Weka belum siap."}`.

## ☁️ Deploy

- **Frontend (Vercel):** arahkan *root directory* ke `frontend`. File `vercel.json` sudah mengatur *rewrite* ke `index.html`.
- **Backend (Railway / Docker):**
  ```bash
  cd backend
  docker build -t tanitech-backend .
  docker run -p 8080:8080 tanitech-backend
  ```

## 🛠️ Pemecahan Masalah

| Gejala | Penyebab | Solusi |
|---|---|---|
| Hasil tampil padahal backend lokal mati | Frontend masih memanggil Railway | Ubah URL di `App.jsx` (lihat langkah 2) |
| `Model Weka belum siap.` | Model gagal dimuat saat start | Periksa log backend; pastikan `tanitech-final.model` ada dan memori cukup |
| `serialVersionUID` / `InvalidClassException` | Versi Weka berbeda dari pembuat model | Samakan versi `weka-stable` di `pom.xml` dengan Weka yang dipakai melatih model |
| Browser menampilkan galat CORS | Backend tidak terjangkau atau salah port | Cek `http://localhost:8080` dan port di `application.properties` |
| `Port 8080 already in use` | Port dipakai aplikasi lain | Matikan aplikasi tersebut atau ubah `server.port` |
| Tampilan polos tanpa warna | Dependensi belum terpasang | Jalankan ulang `npm install` di `frontend/` |
| `Data tanaman tidak ada` di hasil | Tidak ada tanaman dengan probabilitas terukur | Periksa kembali nilai input |
| Kolom N/P/K tidak mau menerima huruf atau lebih dari 2 desimal | Memang dibatasi oleh validasi form | Ketik angka saja, contoh `42.5` |

## 📌 Catatan Penting

- **Rentang input K.** Form dan validasi frontend membatasi N, P, dan K pada 0-200 mg/kg, padahal backend menormalkan K dengan pembagi 500. Akibatnya nilai K yang masuk ke model paling besar 0,4, sedangkan titik pemisah pohon untuk K berada di 0,02-0,4. Perlu dicek apakah batas 200 untuk K disengaja sesuai data latih.
- **Validasi hanya di frontend.** Backend belum memeriksa rentang input, sehingga permintaan langsung ke API (mis. lewat curl) dengan nilai di luar batas tetap diproses. Nilai di atas batas normalisasi dipotong menjadi 1,0.
- **Kode tidak terpakai.** `WekaPredictionService.java` dan `PredictionResponse.java` tidak dipakai oleh controller saat ini, sehingga dapat dihapus atau dirapikan.
- **CORS** pada backend dibuka untuk semua origin (`@CrossOrigin("*")`). Sebaiknya dibatasi ke domain frontend untuk produksi.
- **Konten tanaman** di `sayuran.js` (kebutuhan hara, pupuk, estimasi hasil) adalah panduan umum dan perlu divalidasi pakar pertanian setempat sebelum dijadikan acuan nyata.
- **Cakupan model.** Hasil rekomendasi terbatas pada 138 tanaman di dataset. Sistem ini alat bantu keputusan awal dan tidak menggantikan uji laboratorium maupun penyuluh pertanian.

## 👥 Tim **ROOT OR ROTI**

-Nama dan NPM
1. Najwan Dzimar 257006111081
2. Faruq Al Jihad Awalia Putra 257006111091
3. Fazlee Khayru Aozora 257006111066

- ICONFEST 2026 Data Mining Competition
