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
Auth:
<img width="260" height="556" alt="Screenshot 2026-06-15 191447" src="https://github.com/user-attachments/assets/71e74421-4e86-49a2-95ca-0c322bfb8062" />
<img width="265" height="554" alt="Screenshot 2026-06-15 191437" src="https://github.com/user-attachments/assets/9474023f-aa8b-4627-b8d7-b15cdd198cca" />


Customer:
<img width="260" height="553" alt="Screenshot 2026-06-15 191131" src="https://github.com/user-attachments/assets/3dcaafc0-9384-41f6-8202-0fba607a9651" />
<img width="262" height="556" alt="Screenshot 2026-06-15 191201" src="https://github.com/user-attachments/assets/a3279a45-deac-45f3-afc1-6ecf12decafb" />
<img width="262" height="554" alt="Screenshot 2026-06-15 191219" src="https://github.com/user-attachments/assets/8881f814-cb57-4eda-a1a6-931d89ba114f" />
<img width="254" height="552" alt="Screenshot 2026-06-15 191229" src="https://github.com/user-attachments/assets/0fbac7b1-dec4-4316-9b2c-293b87559421" />
<img width="259" height="556" alt="Screenshot 2026-06-15 191244" src="https://github.com/user-attachments/assets/e4a2d347-47a5-4561-a217-354448aa1d0d" />

Admin:
<img width="259" height="556" alt="Screenshot 2026-06-15 191307" src="https://github.com/user-attachments/assets/c1bab6b8-009e-435a-a5a1-4fada6c165d3" />
<img width="260" height="552" alt="Screenshot 2026-06-15 191323" src="https://github.com/user-attachments/assets/927035f0-7983-4a4e-aa7e-cf14ffe0689a" />
<img width="263" height="560" alt="Screenshot 2026-06-15 191336" src="https://github.com/user-attachments/assets/1b8cb156-d5e5-4f0b-b9c7-f7bfd6a189cb" />
<img width="269" height="560" alt="Screenshot 2026-06-15 191350" src="https://github.com/user-attachments/assets/f271412f-b9d4-46a1-866a-1b0edfd24830" />


## Lisensi

Proyek ini dibuat untuk tujuan pembelajaran dan pengembangan aplikasi manajemen layanan hewan peliharaan.
