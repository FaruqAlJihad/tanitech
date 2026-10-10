import React, { useState } from 'react';
import Header from './components/Header';
import FormInput from './components/FormInput';
import HasilAnalisis from './components/HasilAnalisis';
import DaftarTanaman from './components/DaftarTanaman';
import { databaseSayuranNusantara } from './data/sayuran';

export default function App() {
  const [activeTab, setActiveTab] = useState('prediksi');

  const [formData, setFormData] = useState({
    n: '', p: '', k: '', ph: 7, moisture: 50
  });

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    const nVal = parseFloat(formData.n);
    const pVal = parseFloat(formData.p);
    const kVal = parseFloat(formData.k);

    if (
      isNaN(nVal) || nVal < 0 || nVal > 100000 ||
      isNaN(pVal) || pVal < 0 || pVal > 100000 ||
      isNaN(kVal) || kVal < 0 || kVal > 100000
    ) {
      setErrorMessage('Nilai Nitrogen (N), Fosfor (P), dan Kalium (K) harus berupa angka antara 0 hingga 100.000 mg/kg.');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/predict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          n: nVal,
          p: pVal,
          k: kVal,
          ph: Number(formData.ph),
          moisture: Number(formData.moisture)
        })
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => null);
        throw new Error(errData?.error || `Server Error ${response.status}: Gagal memproses prediksi`);
      }

      const data = await response.json();
      
      // LOGIKA PENCEGAH ERROR: Mendukung format backend lama (tanaman_terpilih) & baru (tanaman_terpilih_list)
      let cropList = [];
      if (Array.isArray(data.tanaman_terpilih_list) && data.tanaman_terpilih_list.length > 0) {
        cropList = data.tanaman_terpilih_list;
      } else if (data.tanaman_terpilih) {
        cropList = [data.tanaman_terpilih];
      }

      let tanamanCocok = [];

      cropList.forEach((cropRaw) => {
        const cropName = cropRaw.trim().toLowerCase();
        
        if (cropName && cropName !== 'unknown') {
          const found = databaseSayuranNusantara.find(
            (t) => t.nama.trim().toLowerCase() === cropName
          );
          
          if (found) {
            // Cek agar tidak duplikat
            if (!tanamanCocok.some(t => t.nama.trim().toLowerCase() === cropName)) {
               tanamanCocok.push(found);
            }
          } else {
            // Fallback dinamis jika tanaman tidak ada di database sayuran.js
            if (!tanamanCocok.some(t => t.nama.trim().toLowerCase() === cropName)) {
                tanamanCocok.push({
                  nama: cropName,
                  nama_lokal: "Belum Diterjemahkan",
                  ikon: '🌱',
                  deskripsi: `Model WEKA memprediksi tanaman '${cropRaw}' sebagai alternatif lahan Anda.`,
                  kebutuhan_n: "Menunggu Data",
                  kebutuhan_p: "Menunggu Data",
                  kebutuhan_k: "Menunggu Data",
                  ph_optimal: "Menunggu Data",
                  kelembaban: "Menunggu Data",
                  rekomendasi_pupuk: "Rekomendasi pupuk belum tersedia.",
                  fungsi_pupuk: "Memerlukan validasi pakar pertanian lokal.",
                  hasil_buruk: "Estimasi belum tersedia",
                  hasil_normal: "Estimasi belum tersedia",
                  hasil_optimal: "Estimasi belum tersedia"
                });
            }
          }
        }
      });

      if (tanamanCocok.length === 0) {
        setResult({
          data_tidak_ditemukan: true,
          pesan: "Data tanaman tidak ada.",
          kelas_tanah: "Validasi Gagal"
        });
      } else {
        setResult({
          data_tidak_ditemukan: false,
          kelas_tanah: data.kelas_tanah || "Klasifikasi Model Weka",
          rekomendasi_tanaman: tanamanCocok
        });
      }

    } catch (err) {
      setErrorMessage(err.message || 'Gagal terhubung ke server backend TaniTech.');
      setResult(null); 
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setResult(null);
    setErrorMessage('');
    setFormData({ n: '', p: '', k: '', ph: 7, moisture: 50 });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 via-emerald-100 to-teal-50 font-sans text-gray-800 selection:bg-teal-200 selection:text-teal-900 pb-12">
      <Header />
      
      <div className="max-w-6xl mx-auto px-6 mt-4 flex justify-center gap-4">
        <button 
          onClick={() => setActiveTab('prediksi')}
          className={`px-6 py-2 rounded-full font-bold transition-all ${activeTab === 'prediksi' ? 'bg-emerald-600 text-white shadow-lg' : 'bg-white text-emerald-700 hover:bg-emerald-100'}`}
        >
          🔍 Analisis Lahan
        </button>
        <button 
          onClick={() => setActiveTab('daftar')}
          className={`px-6 py-2 rounded-full font-bold transition-all ${activeTab === 'daftar' ? 'bg-emerald-600 text-white shadow-lg' : 'bg-white text-emerald-700 hover:bg-emerald-100'}`}
        >
          📋 Daftar Tanaman WEKA
        </button>
      </div>

      <main className="max-w-6xl mx-auto px-6 py-8">
        
        {activeTab === 'daftar' && (
          <DaftarTanaman dataset={databaseSayuranNusantara} />
        )}

        {activeTab === 'prediksi' && (
          <>
            {errorMessage && (
              <div className="mb-6 p-4 bg-red-100 border border-red-300 text-red-700 rounded-2xl font-semibold text-center">
                ⚠️ {errorMessage}
              </div>
            )}

            {loading && (
              <div className="flex flex-col items-center justify-center py-20">
                <div className="w-20 h-20 border-4 border-emerald-200 border-t-teal-500 rounded-full animate-spin mb-6 shadow-lg shadow-teal-500/20"></div>
                <h2 className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-emerald-600 to-teal-600 animate-pulse">
                  Menganalisis Data dengan Model WEKA...
                </h2>
              </div>
            )}

            {!loading && !result && (
              <FormInput 
                formData={formData} 
                handleChange={handleChange} 
                handleSubmit={handleSubmit} 
                loading={loading} 
              />
            )}

            {!loading && result && (
              <HasilAnalisis 
                result={result} 
                formData={formData} 
                handleReset={handleReset} 
              />
            )}
          </>
        )}
      </main>
    </div>
  );
}