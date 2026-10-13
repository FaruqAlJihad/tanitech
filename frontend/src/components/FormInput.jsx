import React from 'react';

export default function FormInput({ formData, handleChange, handleSubmit, loading }) {
  const handleNutrientChange = (e) => {
    const { name, value } = e.target;
    
    // Izinkan input kosong (agar pengguna bisa menghapus angka dengan backspace)
    if (value === '') {
      handleChange(e);
      return;
    }
    
    // REGEX MUTLAK: Hanya boleh angka, dan titik desimal maksimal 2 angka di belakangnya
    // Regex ini secara otomatis memblokir huruf, simbol, dan desimal lebih dari 2 digit (contoh: 2.34 lolos, 2.345 diblokir)
    if (!/^\d*\.?\d{0,2}$/.test(value)) {
      return;
    }
    
    // Mencegah input angka melampaui batas maksimal 200
    const parsed = parseFloat(value);
    if (!isNaN(parsed) && parsed > 200) {
      return;
    }
    
    handleChange(e);
  };
  
  // Fungsi untuk menentukan warna dinamis berdasarkan nilai pH (0-14)
  const getPHColor = (phValue) => {
    const ph = parseFloat(phValue);
    if (ph <= 2) return '#c40026'; // Merah tua
    if (ph <= 4) return '#f45a00'; // Oranye
    if (ph <= 5) return '#f8b100'; // Kuning tua
    if (ph <= 6) return '#e9d600'; // Kuning kehijauan
    if (ph <= 7) return '#7fc400'; // Hijau muda (Netral)
    if (ph <= 8) return '#00a831'; // Hijau tua
    if (ph <= 9) return '#00907a'; // Biru kehijauan
    if (ph <= 11) return '#0058b8'; // Biru
    if (ph <= 12) return '#0025a3'; // Biru tua
    return '#10006b'; // Biru sangat gelap/Ungu
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white/80 backdrop-blur-xl p-8 rounded-3xl shadow-xl shadow-emerald-900/5 border border-white/50 mb-8 relative overflow-hidden">
      
      <div className="text-center mb-8">
        <h2 className="text-3xl font-extrabold text-emerald-800 flex justify-center items-center gap-3">
          Parameter Uji Tanah
        </h2>
        <p className="text-emerald-600 font-medium mt-2">Masukkan hasil pengujian metrik tanah lahan pertanian Anda untuk rekomendasi terbaik.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="group">
          <label className="block text-sm font-black text-emerald-800 mb-2 uppercase tracking-wider flex items-center gap-2">
            Nitrogen (N) 
            <span className="text-[10px] normal-case font-bold text-emerald-700 bg-emerald-200/60 px-2 py-0.5 rounded-md ml-auto">mg/kg</span>
          </label>
          <input 
            type="text" 
            inputMode="decimal"
            name="n" 
            value={formData.n} 
            onChange={handleNutrientChange} 
            required 
            placeholder="0.00 - 200.00"
            className="w-full px-5 py-4 bg-emerald-50/50 border-2 border-emerald-200 rounded-xl text-lg font-bold text-emerald-900 focus:bg-white focus:ring-4 focus:ring-emerald-500/30 focus:border-emerald-500 outline-none transition-all shadow-inner group-hover:border-emerald-400" 
          />
        </div>

        <div className="group">
          <label className="block text-sm font-black text-teal-800 mb-2 uppercase tracking-wider flex items-center gap-2">
            Fosfor (P)
            <span className="text-[10px] normal-case font-bold text-teal-700 bg-teal-200/60 px-2 py-0.5 rounded-md ml-auto">mg/kg</span>
          </label>
          <input 
            type="text" 
            inputMode="decimal"
            name="p" 
            value={formData.p} 
            onChange={handleNutrientChange} 
            required 
            placeholder="0.00 - 200.00"
            className="w-full px-5 py-4 bg-teal-50/50 border-2 border-teal-200 rounded-xl text-lg font-bold text-teal-900 focus:bg-white focus:ring-4 focus:ring-teal-500/30 focus:border-teal-500 outline-none transition-all shadow-inner group-hover:border-teal-400" 
          />
        </div>

        <div className="group">
          <label className="block text-sm font-black text-cyan-800 mb-2 uppercase tracking-wider flex items-center gap-2">
            Kalium (K)
            <span className="text-[10px] normal-case font-bold text-cyan-700 bg-cyan-200/60 px-2 py-0.5 rounded-md ml-auto">mg/kg</span>
          </label>
          <input 
            type="text" 
            inputMode="decimal"
            name="k" 
            value={formData.k} 
            onChange={handleNutrientChange} 
            required 
            placeholder="0.00 - 200.00"
            className="w-full px-5 py-4 bg-cyan-50/50 border-2 border-cyan-200 rounded-xl text-lg font-bold text-cyan-900 focus:bg-white focus:ring-4 focus:ring-cyan-500/30 focus:border-cyan-500 outline-none transition-all shadow-inner group-hover:border-cyan-400" 
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10 bg-white/50 p-6 rounded-2xl border border-emerald-100">
        <div>
          <div className="flex justify-between items-end mb-4">
            <label className="text-sm font-black text-emerald-800 uppercase tracking-wider flex items-center gap-2">
              Tingkat pH Tanah
            </label>
            <span 
              className="px-4 py-1.5 rounded-lg font-bold shadow-sm border border-emerald-100 text-white transition-colors"
              style={{ backgroundColor: getPHColor(formData.ph) }}
            >
              {formData.ph}
            </span>
          </div>
          <input 
            type="range" name="ph" min="0" max="14" step="0.1" value={formData.ph} onChange={handleChange} 
            className="w-full h-2 rounded-lg appearance-none cursor-pointer" 
            style={{
              background: `linear-gradient(to right, #c40026 0%, #f45a00 21%, #f8b100 35%, #e9d600 42%, #7fc400 50%, #00a831 57%, #00907a 64%, #0058b8 78%, #0025a3 85%, #10006b 100%)`
            }}
          />
          <div className="flex justify-between text-xs font-bold text-gray-500 mt-2">
            <span>ASAM (0)</span><span>NETRAL (7)</span><span>BASA (14)</span>
          </div>
        </div>

        <div>
          <div className="flex justify-between items-end mb-4">
            <label className="text-sm font-black text-blue-800 uppercase tracking-wider flex items-center gap-2">
              <span className="text-xl">💧</span> Kelembaban Lahan
            </label>
            <span className="bg-white px-4 py-1.5 rounded-lg text-blue-700 font-bold shadow-sm border border-blue-100">{formData.moisture}%</span>
          </div>
          <input 
            type="range" name="moisture" min="0" max="100" value={formData.moisture} onChange={handleChange} 
            className="w-full h-2 bg-blue-200 rounded-lg appearance-none cursor-pointer accent-blue-500" 
          />
          <div className="flex justify-between text-xs font-bold text-blue-600/60 mt-2">
            <span>KERING (0%)</span><span>IDEAL (50%)</span><span>BASAH (100%)</span>
          </div>
        </div>
      </div>

      <button 
        type="submit" disabled={loading} 
        className="w-full bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white font-bold py-5 px-8 rounded-2xl shadow-xl shadow-emerald-600/30 transform transition-all active:scale-[0.98] disabled:opacity-70 flex items-center justify-center gap-3 text-lg"
      >
        Analisis Kandungan Tanah
      </button>
    </form>
  );
}