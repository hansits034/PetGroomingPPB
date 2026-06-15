# Pet Bliss - Pet Grooming Management App

**Pet Bliss** adalah aplikasi manajemen layanan perawatan hewan (pet grooming) berbasis Android yang dirancang untuk memudahkan pemilik hewan peliharaan dalam mengelola kebutuhan perawatan anabul mereka secara praktis dan terorganisir.

## Fitur Utama

### Untuk Pengguna (Customer)
- **Manajemen Profil Hewan:** Daftarkan berbagai jenis hewan peliharaan (Anjing, Kucing, Kelinci, dll) dengan informasi detail seperti ras, usia, dan jenis kelamin.
- **Booking Layanan:** Pesan jadwal grooming secara mandiri dengan pilihan layanan yang tersedia dan slot waktu yang fleksibel.
- **Sistem Loyalitas & Rewards:** Kumpulkan poin dari setiap transaksi dan tukarkan dengan voucher layanan gratis (Espresso Wash, Cappuccino Groom, Latte Spa).
- **Riwayat Janji Temu:** Pantau status jadwal mendatang serta lihat riwayat perawatan yang telah diselesaikan.
- **Kartu Keanggotaan Digital:** Akses ID anggota dan total poin secara langsung melalui dashboard.

### Untuk Admin
- **Dashboard Bisnis:** Ringkasan pendapatan total, permintaan baru, janji temu yang sedang diproses, dan yang telah selesai.
- **Manajemen Layanan:** Menambah, mengubah, atau menghapus jenis layanan grooming beserta harganya.
- **Manajemen Hadiah:** Mengelola daftar reward dan poin yang dibutuhkan untuk penukaran voucher.
- **Konfirmasi Janji Temu:** Menyetujui (ACC) atau menyelesaikan permintaan booking dari pelanggan.

## Teknologi yang Digunakan

- **Bahasa Pemrograman:** Kotlin
- **UI Framework:** Jetpack Compose (Modern Android UI)
- **Arsitektur:** MVVM (Model-View-ViewModel)
- **Database Lokal:** Room Database (untuk penyimpanan data offline)
- **Pengolahan Anotasi:** KSP (Kotlin Symbol Processing)
- **Networking:** Retrofit & Gson (untuk integrasi API eksternal)
- **Image Loading:** Coil
- **Dependency:** 
  - AndroidX Lifecycle (ViewModel & Compose integration)
  - Navigation Compose
  - Material Design 3

## Cara Menjalankan Proyek

1. **Clone Repository:**
   ```bash
   git clone https://github.com/username/pet-bliss.git
   ```
2. **Buka di Android Studio:**
   Pastikan Anda menggunakan versi Android Studio terbaru (Ladybug atau yang lebih baru direkomendasikan).
3. **Sync Gradle:**
   Tunggu hingga proses sinkronisasi Gradle selesai.
4. **Jalankan Aplikasi:**
   Pilih emulator atau perangkat fisik dan klik tombol **Run**.

## Tampilan Aplikasi

*(Anda bisa menambahkan screenshot di sini setelah mengunggah gambar ke repository)*

## Lisensi

Proyek ini dibuat untuk tujuan pembelajaran dan pengembangan aplikasi manajemen layanan hewan peliharaan.
