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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fungsi normalisasi agar nilai riil (puluhan/ratusan) sinkron dengan dataset latih WEKA (skala 0.0 - 1.0)
    private DenseInstance createInstance(Instances dataset, double rawN, double rawP, double rawK, double rawPh, double rawMoist) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);

        // Skala konversi dataset V6: N(max 200), P(max 200), K(max 500), Lembab(max 100), pH(max 14)
        double normN = rawN > 1.0 ? Math.min(1.0, Math.max(0.0, rawN / 200.0)) : rawN;
        double normP = rawP > 1.0 ? Math.min(1.0, Math.max(0.0, rawP / 200.0)) : rawP;
        double normK = rawK > 1.0 ? Math.min(1.0, Math.max(0.0, rawK / 500.0)) : rawK;
        double normMoist = rawMoist > 1.0 ? Math.min(1.0, Math.max(0.0, rawMoist / 100.0)) : rawMoist;
        double normPh = rawPh > 1.0 ? Math.min(1.0, Math.max(0.0, rawPh / 14.0)) : rawPh;
        
        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (i == dataset.classIndex()) continue;
            
            String attrName = dataset.attribute(i).name().toLowerCase();
            
            if (attrName.equals("n") || attrName.contains("nitrogen")) {
                instance.setValue(i, normN);
            } else if (attrName.equals("p") || attrName.contains("phosphor") || attrName.contains("fosfor")) {
                instance.setValue(i, normP);
            } else if (attrName.equals("k") || attrName.contains("potassium") || attrName.contains("kalium")) {
                instance.setValue(i, normK);
            } else if (attrName.contains("ph")) {
                instance.setValue(i, normPh);
            } else if (attrName.contains("moist") || attrName.contains("humid") || attrName.contains("lembab")) {
                instance.setValue(i, normMoist);
            } else {
                instance.setValue(i, 0.5);
            }
        }
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
            double n = request.getN();
            double p = request.getP();
            double k = request.getK();
            double ph = request.getPh();
            double moist = request.getMoisture() == null ? 60.0 : request.getMoisture();

            // 1. PREDIKSI UTAMA (Sesuai kondisi tanah pengguna saat ini)
            double mainPred = model.classifyInstance(createInstance(dataset, n, p, k, ph, moist));
            recommendedCrops.add(dataset.classAttribute().value((int) mainPred));

            // 2. REKOMENDASI ALTERNATIF 1 (Kondisi tanah dengan sedikit peningkatan kesuburan/kompos)
            double altPred1 = model.classifyInstance(createInstance(dataset, n * 1.15, p * 1.10, k * 1.15, ph, moist));
            recommendedCrops.add(dataset.classAttribute().value((int) altPred1));

            // 3. REKOMENDASI ALTERNATIF 2 (Kondisi tanah dengan sedikit penyesuaian hara kalium / fosfor)
            double altPred2 = model.classifyInstance(createInstance(dataset, n * 0.85, p * 0.90, k * 0.85, ph, moist));
            recommendedCrops.add(dataset.classAttribute().value((int) altPred2));

            // 4. Jika masih kurang dari 2 tanaman unik, lakukan variasi kelembaban & pH toleransi
            double step = 0.20;
            while (recommendedCrops.size() < 3 && step <= 0.60) {
                double altUp = model.classifyInstance(createInstance(dataset, n * (1.0 + step), p, k * (1.0 + step), ph + 0.3, moist * 1.1));
                recommendedCrops.add(dataset.classAttribute().value((int) altUp));

                double altDown = model.classifyInstance(createInstance(dataset, n * (1.0 - step), p, k * (1.0 - step), ph - 0.3, moist * 0.9));
                recommendedCrops.add(dataset.classAttribute().value((int) altDown));
                
                step += 0.15;
            }

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