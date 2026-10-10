package com.tanitech.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tanitech.dto.PredictionRequest;

import jakarta.annotation.PostConstruct;
import weka.classifiers.trees.RandomTree;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PredictionController {

    private RandomTree model;
    private Instances modelHeader; 

    @PostConstruct
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/138 V3.model");
            if(is == null) {
                is = getClass().getClassLoader().getResourceAsStream("138 V3.model");
            }
            if (is != null) {
                Object[] loaded = (Object[]) SerializationHelper.readAll(is);
                for (Object obj : loaded) {
                    if (obj instanceof RandomTree) {
                        model = (RandomTree) obj;
                    } else if (obj instanceof Instances) {
                        modelHeader = (Instances) obj;
                    }
                }
                if (modelHeader != null) {
                     System.out.println("✅ Model WEKA Multi-Prediksi Siap!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fungsi pembantu untuk membuat objek WEKA
    private DenseInstance createInstance(Instances dataset, double n, double p, double k, double ph, double moist) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);
        instance.setValue(0, n);
        instance.setValue(1, p);
        instance.setValue(2, k);
        instance.setValue(3, 27.0); // Suhu konstan
        instance.setValue(4, moist);
        instance.setValue(5, ph);
        instance.setValue(6, 150.0); // Curah hujan konstan
        return instance;
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody PredictionRequest request) {
        try {
            if (model == null || modelHeader == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Model Weka belum siap."));
            }

            Instances dataset = new Instances(modelHeader, 0);
            dataset.setClassIndex(dataset.numAttributes() - 1); 

            // Menggunakan LinkedHashSet agar hasil prediksi tidak ada yang duplikat
            Set<String> recommendedCrops = new LinkedHashSet<>();

            // 1. PREDIKSI UTAMA (Berdasarkan input presisi)
            double pred1 = model.classifyInstance(createInstance(dataset, 
                request.getN(), request.getP(), request.getK(), request.getPh(), request.getMoisture() == null ? 50.0 : request.getMoisture()
            ));
            recommendedCrops.add(dataset.classAttribute().value((int) pred1));

            // 2. PREDIKSI ALTERNATIF ATAS (Bagaimana jika tanah sedikit lebih subur +10%)
            double pred2 = model.classifyInstance(createInstance(dataset, 
                request.getN() * 1.10, request.getP() * 1.10, request.getK() * 1.10, request.getPh() * 1.05, request.getMoisture() == null ? 50.0 : request.getMoisture() * 1.10
            ));
            recommendedCrops.add(dataset.classAttribute().value((int) pred2));

            // 3. PREDIKSI ALTERNATIF BAWAH (Bagaimana jika tanah sedikit lebih miskin -10%)
            double pred3 = model.classifyInstance(createInstance(dataset, 
                request.getN() * 0.90, request.getP() * 0.90, request.getK() * 0.90, request.getPh() * 0.95, request.getMoisture() == null ? 50.0 : request.getMoisture() * 0.90
            ));
            recommendedCrops.add(dataset.classAttribute().value((int) pred3));

            // Ubah Set kembali menjadi List untuk dikirimkan sebagai JSON Array
            List<String> finalPredictions = new ArrayList<>(recommendedCrops);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "kelas_tanah", "Tanah Terklasifikasi Model Weka",
                "tanaman_terpilih_list", finalPredictions // <-- Mengirimkan ARRAY/Daftar
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error klasifikasi Weka: " + e.getMessage()));
        }
    }
}