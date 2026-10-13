import React from 'react';
import { databaseSayuranNusantara } from '../data/sayuran';

export default function HasilAnalisis({ result, formData, handleReset }) {
  if (!result) return null;

  const konversiHasil = (teksHasil) => {
    if (!teksHasil || teksHasil.includes("Menunggu")) return "Estimasi belum tersedia";
    const angkaArray = teksHasil.match(/[\d.]+/g);
    if (!angkaArray || angkaArray.length < 2) return teksHasil; 
    const min = parseFloat(angkaArray[0]) * 10;
    const max = parseFloat(angkaArray[1]) * 10;
    return `${min} - ${max} kg / 100 m² / musim`;
  };

  const cekDalamRentang = (nilaiString, inputUser) => {
    if (!nilaiString || nilaiString === "-") return false;
    const angka = nilaiString.match(/[\d.]+/g);
    if (angka && angka.length >= 2) {
      const min = parseFloat(angka[0]);
      const max = parseFloat(angka[1]);
      const val = parseFloat(inputUser);
      return val >= min && val <= max;
    }
    return false;
  };

  let daftarTampil = [...(result.rekomendasi_tanaman || [])];

  const tanamanSesuaiData = databaseSayuranNusantara.filter(tanaman => {
    const nSesuai = cekDalamRentang(tanaman.kebutuhan_n, formData.n);
    const pSesuai = cekDalamRentang(tanaman.kebutuhan_p, formData.p);
    const kSesuai = cekDalamRentang(tanaman.kebutuhan_k, formData.k);
    const phSesuai = cekDalamRentang(tanaman.ph_optimal, formData.ph);
    const moistSesuai = !formData.moisture || cekDalamRentang(tanaman.kelembaban, formData.moisture);
    
    return nSesuai && pSesuai && kSesuai && phSesuai && moistSesuai;
  });

  tanamanSesuaiData.forEach(tanamanStatis => {
    const sudahAda = daftarTampil.some(t => t.nama.toLowerCase() === tanamanStatis.nama.toLowerCase());
    if (!sudahAda) {
      daftarTampil.push({ ...tanamanStatis, is_fallback_db: true });
    }
  });

  daftarTampil.sort((a, b) => {
    if (a.is_fallback_db && !b.is_fallback_db) return 1;
    if (!a.is_fallback_db && b.is_fallback_db) return -1;
    return 0;
  });

  if (daftarTampil.length === 0) {
    return (
      <div className="animate-fade-in-up mt-6">
        <div className="bg-white rounded-2xl md:rounded-3xl p-6 md:p-8 shadow-xl border-t-8 border-red-500 text-center">
          <span className="text-5xl md:text-6xl block mb-4">🏜️</span>
          <h2 className="text-2xl md:text-3xl font-black text-red-800 mb-6">Status: Data tanaman tidak ada.</h2>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 bg-gray-50 rounded-2xl p-4 md:p-6 mb-8 text-left">
            <div><p className="text-xs font-bold text-gray-500 uppercase">Nitrogen (N)</p><p className="text-xl font-black">{formData.n} <span className="text-sm">mg</span></p></div>
            <div><p className="text-xs font-bold text-gray-500 uppercase">Fosfor (P)</p><p className="text-xl font-black">{formData.p} <span className="text-sm">mg</span></p></div>
            <div><p className="text-xs font-bold text-gray-500 uppercase">Kalium (K)</p><p className="text-xl font-black">{formData.k} <span className="text-sm">mg</span></p></div>
            <div><p className="text-xs font-bold text-gray-500 uppercase">pH Tanah</p><p className="text-xl font-black">{formData.ph}</p></div>
          </div>

          <div className="bg-red-50 text-red-800 px-4 md:px-6 py-4 rounded-xl font-semibold mb-6 text-sm md:text-base">
            Sistem tidak menemukan rekomendasi tanaman yang valid berdasarkan model WEKA maupun Database Tanaman.
          </div>
          
          <button onClick={handleReset} className="w-full md:w-auto bg-red-100 hover:bg-red-200 text-red-700 font-bold py-3 px-8 rounded-xl transition-colors">
            Ubah Input & Analisis Ulang
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in-up mt-6">
      <div className="space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <h2 className="text-xl md:text-2xl font-black text-emerald-800">Laporan Rekomendasi Weka</h2>
          <button onClick={handleReset} className="w-full md:w-auto bg-white border border-emerald-200 hover:bg-emerald-50 text-emerald-700 font-bold py-2 px-6 rounded-xl transition-colors shadow-sm">
            Analisis Lahan Baru
          </button>
        </div>

        {daftarTampil.map((tanaman, index) => (
          <div key={index} className="bg-white rounded-2xl md:rounded-3xl shadow-2xl border border-emerald-100 overflow-hidden mb-8 relative">
            
            <div className="bg-gradient-to-r from-emerald-600 to-teal-700 p-6 md:p-8 text-white flex flex-col md:flex-row items-center gap-4 md:gap-6 pt-10">
              <div className="w-20 h-20 md:w-24 md:h-24 bg-white/20 rounded-2xl flex items-center justify-center text-5xl md:text-6xl shadow-inner backdrop-blur-sm">
                {tanaman.ikon || '🌱'}
              </div>
              <div className="flex-1 text-center md:text-left">
                <div className="flex flex-col md:flex-row items-center gap-2 md:gap-3 mb-2">
                  <h3 className="text-3xl md:text-4xl font-black tracking-tight">{tanaman.nama_lokal || tanaman.nama}</h3>
                  <span className="bg-black/20 text-white font-mono text-xs md:text-sm px-3 py-1 rounded-lg border border-white/30">
                    English Name: {tanaman.nama}
                  </span>
                </div>
                <p className="text-emerald-50 text-base md:text-lg opacity-90">{tanaman.deskripsi}</p>
              </div>
            </div>

            <div className="p-5 md:p-8">
              <h4 className="text-lg md:text-xl font-bold text-gray-800 mb-4 flex items-center gap-2"><span>🧪</span> Rekomendasi Pupuk & Perawatan</h4>
              <div className="border border-gray-200 rounded-2xl overflow-x-auto w-full mb-8">
                <table className="w-full min-w-[600px] text-left text-sm md:text-base">
                  <tbody className="divide-y divide-gray-200">
                    <tr className="bg-gray-50">
                      <td className="py-3 md:py-4 px-4 md:px-6 font-bold text-gray-600 w-1/3">Kondisi Tanah</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 text-gray-800">Berdasarkan analisis WEKA (N: {formData.n}, P: {formData.p}, K: {formData.k}, pH: {formData.ph}, Kelembaban: {formData.moisture}%)</td>
                    </tr>
                    <tr className="bg-amber-50/30">
                      <td className="py-3 md:py-4 px-4 md:px-6 font-bold text-amber-800">Jenis Pupuk</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 font-bold text-amber-900">{tanaman.rekomendasi_pupuk || "Pupuk NPK Seimbang"}</td>
                    </tr>
                    <tr>
                      <td className="py-3 md:py-4 px-4 md:px-6 font-bold text-gray-600">Tujuan Penggunaan</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 text-gray-800">{tanaman.fungsi_pupuk || "Memenuhi kebutuhan unsur hara makro tanaman secara optimal."}</td>
                    </tr>
                    <tr>
                      <td className="py-3 md:py-4 px-4 md:px-6 font-bold text-gray-600">Cara Aplikasi</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 text-gray-800 italic">Mengikuti petunjuk dosis produk agronomi yang valid untuk area lokal Anda.</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <h4 className="text-lg md:text-xl font-bold text-gray-800 mb-4 flex items-center gap-2"><span>📊</span> Estimasi Hasil Panen per 100 m²</h4>
              <div className="border border-gray-200 rounded-2xl overflow-x-auto w-full">
                <table className="w-full min-w-[600px] text-left text-sm md:text-base">
                  <thead className="bg-gray-100">
                    <tr>
                      <th className="py-3 md:py-4 px-4 md:px-6 font-bold text-gray-700">Skenario Lahan</th>
                      <th className="py-3 md:py-4 px-4 md:px-6 font-bold text-gray-700 text-right">Perkiraan Hasil</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    <tr className="hover:bg-red-50 transition-colors">
                      <td className="py-3 md:py-4 px-4 md:px-6 font-medium text-gray-800">A. Tanah kurang sesuai (Kekurangan hara/pH buruk)</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 text-right font-semibold text-red-600">{tanaman.hasil_buruk === "Gagal panen / Berbuah jarang" ? tanaman.hasil_buruk : konversiHasil(tanaman.hasil_buruk)}</td>
                    </tr>
                    <tr className="hover:bg-blue-50 transition-colors">
                      <td className="py-3 md:py-4 px-4 md:px-6 font-medium text-gray-800">B. Kondisi budidaya normal</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 text-right font-semibold text-blue-700">{konversiHasil(tanaman.hasil_normal)}</td>
                    </tr>
                    <tr className="bg-emerald-50 hover:bg-emerald-100 transition-colors">
                      <td className="py-3 md:py-4 px-4 md:px-6 font-medium text-emerald-900">C. Pemupukan sesuai kebutuhan</td>
                      <td className="py-3 md:py-4 px-4 md:px-6 text-right font-bold text-emerald-700">{konversiHasil(tanaman.hasil_optimal)}</td>
                    </tr>
                  </tbody>
                </table>
                <div className="bg-gray-50 px-4 md:px-6 py-3 text-xs text-gray-500 border-t border-gray-200">
                  *Estimasi merupakan konversi rasio produktivitas aktual per hektare (sumber data: literatur agronomi umum).
                </div>
              </div>

            </div>
          </div>
        ))}
      </div>
    </div>
  );
}