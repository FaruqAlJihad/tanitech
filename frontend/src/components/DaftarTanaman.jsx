import React, { useState } from 'react';

export default function DaftarTanaman({ dataset }) {
  const [search, setSearch] = useState('');

  // Fitur Filter/Pencarian
  const filteredData = dataset.filter((item) => 
    item.nama.toLowerCase().includes(search.toLowerCase()) || 
    item.nama_lokal.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="bg-white/90 backdrop-blur-md rounded-3xl p-8 shadow-xl border border-emerald-100 animate-fade-in-up">
      <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
        <div>
          <h2 className="text-3xl font-black text-emerald-800">Database Tanaman Weka</h2>
          <p className="text-emerald-600 font-medium">Dataset referensi komoditas pertanian sistem TaniTech.</p>
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

      <div className="overflow-x-auto rounded-xl border border-emerald-100 shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gradient-to-r from-emerald-600 to-teal-600 text-white">
              <th className="py-4 px-6 font-bold uppercase tracking-wider text-sm border-b border-emerald-700">English Name (WEKA)</th>
              <th className="py-4 px-6 font-bold uppercase tracking-wider text-sm border-b border-emerald-700">Nama Tanaman</th>
              {/* Judul kolom diperbarui agar tidak disalahpahami sebagai standar mutlak */}
              <th className="py-4 px-6 font-bold uppercase tracking-wider text-sm border-b border-emerald-700">Rentang Toleransi (Dataset)</th>
              <th className="py-4 px-6 font-bold uppercase tracking-wider text-sm border-b border-emerald-700">Rekomendasi Pupuk</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-emerald-50">
            {filteredData.length > 0 ? (
              filteredData.map((item, idx) => (
                <tr key={idx} className="hover:bg-emerald-50/50 transition-colors">
                  <td className="py-4 px-6 font-mono text-emerald-700 font-bold bg-emerald-50/30">
                    {item.nama}
                  </td>
                  <td className="py-4 px-6 font-bold text-gray-800 flex items-center gap-2">
                    <span className="text-2xl">{item.ikon}</span> {item.nama_lokal}
                  </td>
                  <td className="py-4 px-6 text-sm text-gray-600">
                    <div><span className="font-semibold">N:</span> {item.kebutuhan_n}</div>
                    <div><span className="font-semibold">P:</span> {item.kebutuhan_p}</div>
                    <div><span className="font-semibold">K:</span> {item.kebutuhan_k}</div>
                    <div><span className="font-semibold text-emerald-600">pH:</span> {item.ph_optimal}</div>
                    {/* Menampilkan data kelembaban yang sudah ditambahkan di sayuran.js */}
                    <div><span className="font-semibold text-blue-500">Kelembaban:</span> {item.kelembaban || "50 - 70 %"}</div>
                  </td>
                  <td className="py-4 px-6 text-sm text-gray-700">
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