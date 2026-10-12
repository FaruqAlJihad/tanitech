package com.tanitech.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tanitech.dto.PredictionRequest;

import jakarta.annotation.PostConstruct;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PredictionController {

    private Classifier model;
    private Instances modelHeader; 

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("tanitech-final.model");
            try (InputStream is = resource.getInputStream()) {
                Object[] loaded = (Object[]) SerializationHelper.readAll(is);
                for (Object obj : loaded) {
                    if (obj instanceof Classifier) {
                        model = (Classifier) obj;
                    } else if (obj instanceof Instances) {
                        modelHeader = (Instances) obj;
                    }
                }
                
                if (model == null && loaded.length > 0 && loaded[0] instanceof Classifier) {
                    model = (Classifier) loaded[0];
                }

                if (model != null) {
                     System.out.println("✅ Model WEKA Multi-Prediksi di Controller Siap!");
                } else {
                     System.err.println("❌ Classifier tidak ditemukan di dalam file model.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Gagal memuat model Weka di Controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fungsi pembantu untuk membuat objek WEKA dengan 5 fitur agar tidak Error Index 6
    private DenseInstance createInstance(Instances dataset, double n, double p, double k, double ph, double moist) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);
        instance.setValue(0, n);
        instance.setValue(1, p);
        instance.setValue(2, k);
        instance.setValue(3, moist); 
        instance.setValue(4, ph);
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

            Set<String> recommendedCrops = new LinkedHashSet<>();
            double moist = request.getMoisture() == null ? 50.0 : request.getMoisture();

            // 1. Dapatkan distribusi probabilitas untuk input asli dari user
            DenseInstance mainInstance = createInstance(dataset, request.getN(), request.getP(), request.getK(), request.getPh(), moist);
            double[] probs = model.distributionForInstance(mainInstance);

            // Ambil hingga 3 tanaman teratas dari algoritma jika model memiliki keraguan (probabilitas terpecah)
            for (int i = 0; i < 3; i++) {
                int maxIdx = -1;
                double maxProb = -1.0;
                for (int j = 0; j < probs.length; j++) {
                    if (probs[j] > maxProb && !recommendedCrops.contains(dataset.classAttribute().value(j))) {
                        maxProb = probs[j];
                        maxIdx = j;
                    }
                }
                if (maxIdx != -1 && maxProb > 0.0) {
                    recommendedCrops.add(dataset.classAttribute().value(maxIdx));
                }
            }

            // 2. Jika model sangat kaku (1 tanaman dominan 100%), paksa perluas variasi pencarian nutrisi
            // Pencarian diperlebar mulai dari 10% hingga 80% pergeseran hara sampai minimal mendapat 3 tanaman
            double multiplier = 1.10;
            while (recommendedCrops.size() < 3 && multiplier <= 1.80) {
                
                // Cari tanaman untuk skenario tanah yang jauh lebih subur
                double predUp = model.classifyInstance(createInstance(dataset, 
                    request.getN() * multiplier, request.getP() * multiplier, request.getK() * multiplier, request.getPh(), moist
                ));
                recommendedCrops.add(dataset.classAttribute().value((int) predUp));

                // Cari tanaman untuk skenario tanah yang jauh lebih tandus
                double factorDown = 2.0 - multiplier; 
                if (factorDown < 0.1) factorDown = 0.1; // Mencegah nilai turun di bawah 0
                double predDown = model.classifyInstance(createInstance(dataset, 
                    request.getN() * factorDown, request.getP() * factorDown, request.getK() * factorDown, request.getPh(), moist
                ));
                recommendedCrops.add(dataset.classAttribute().value((int) predDown));

                multiplier += 0.15; // Lebarkan pencarian sebesar 15% setiap kali gagal mendapat tanaman baru
            }

            // Ubah Set kembali menjadi List untuk dikirimkan ke Vercel sebagai JSON Array
            List<String> finalPredictions = new ArrayList<>(recommendedCrops);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "kelas_tanah", "Tanah Terklasifikasi Model Weka",
                "tanaman_terpilih_list", finalPredictions
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error klasifikasi Weka: " + e.getMessage()));
        }
    }
}