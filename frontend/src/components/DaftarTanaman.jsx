import React, { useState, useEffect } from 'react';

export default function DaftarTanaman({ dataset }) {
  // Mengambil nilai pencarian yang tersimpan di sesi browser (jika ada)
  const [search, setSearch] = useState(() => {
    return sessionStorage.getItem('searchTanaman') || '';
  });

  // Menyimpan nilai pencarian ke sesi browser setiap kali ada ketikan baru
  useEffect(() => {
    sessionStorage.setItem('searchTanaman', search);
  }, [search]);

  const filteredData = dataset.filter((item) => 
    item.nama.toLowerCase().includes(search.toLowerCase()) || 
    item.nama_lokal.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="bg-white/90 backdrop-blur-md rounded-2xl md:rounded-3xl p-5 md:p-8 shadow-xl border border-emerald-100 animate-fade-in-up">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 md:mb-8 gap-4">
        <div className="w-full md:w-auto">
          <h2 className="text-2xl md:text-3xl font-black text-emerald-800">Database Tanaman Weka</h2>
          <p className="text-sm md:text-base text-emerald-600 font-medium">Dataset referensi komoditas pertanian sistem TaniTech.</p>
        </div>
        
        <div className="relative w-full md:w-72">
          <input 
            type="text" 
            placeholder="Cari nama tanaman..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-3 bg-emerald-50 border border-emerald-200 rounded-xl focus:ring-2 focus:ring-emerald-500 outline-none"
          />
          <span className="absolute left-3 top-3 text-emerald-400">🔍</span>
        </div>
      </div>

      <div className="overflow-x-auto rounded-xl border border-emerald-100 shadow-sm w-full">
        <table className="w-full min-w-[800px] text-left border-collapse">
          <thead>
            <tr className="bg-gradient-to-r from-emerald-600 to-teal-600 text-white">
              <th className="py-3 md:py-4 px-4 md:px-6 font-bold uppercase tracking-wider text-xs md:text-sm border-b border-emerald-700 whitespace-nowrap">English Name (WEKA)</th>
              <th className="py-3 md:py-4 px-4 md:px-6 font-bold uppercase tracking-wider text-xs md:text-sm border-b border-emerald-700 whitespace-nowrap">Nama Tanaman</th>
              <th className="py-3 md:py-4 px-4 md:px-6 font-bold uppercase tracking-wider text-xs md:text-sm border-b border-emerald-700 whitespace-nowrap">Rentang Toleransi (Dataset)</th>
              <th className="py-3 md:py-4 px-4 md:px-6 font-bold uppercase tracking-wider text-xs md:text-sm border-b border-emerald-700 whitespace-nowrap">Rekomendasi Pupuk</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-emerald-50">
            {filteredData.length > 0 ? (
              filteredData.map((item, idx) => (
                <tr key={idx} className="hover:bg-emerald-50/50 transition-colors">
                  <td className="py-4 px-4 md:px-6 font-mono text-emerald-700 font-bold bg-emerald-50/30">
                    {item.nama}
                  </td>
                  <td className="py-4 px-4 md:px-6 font-bold text-gray-800 flex items-center gap-2">
                    <span className="text-2xl">{item.ikon}</span> {item.nama_lokal}
                  </td>
                  <td className="py-4 px-4 md:px-6 text-sm text-gray-600">
                    <div><span className="font-semibold">N:</span> {item.kebutuhan_n}</div>
                    <div><span className="font-semibold">P:</span> {item.kebutuhan_p}</div>
                    <div><span className="font-semibold">K:</span> {item.kebutuhan_k}</div>
                    <div><span className="font-semibold text-emerald-600">pH:</span> {item.ph_optimal}</div>
                    <div><span className="font-semibold text-blue-500">Kelembaban:</span> {item.kelembaban || "50 - 70 %"}</div>
                  </td>
                  <td className="py-4 px-4 md:px-6 text-sm text-gray-700">
                    <p className="font-semibold text-orange-700 mb-1">{item.rekomendasi_pupuk}</p>
                    <p className="text-xs text-gray-500 italic">{item.fungsi_pupuk}</p>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="4" className="py-8 text-center text-gray-500 font-medium">
                  Tanaman "{search}" tidak ditemukan dalam database.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}