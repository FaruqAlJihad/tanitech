package com.tanitech.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DenseInstance createInstance(Instances dataset, double rawN, double rawP, double rawK, double rawPh, double rawMoist) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);

        // Skala norm dataset: N(200), P(200), K(500), Lembab(100), pH(14)
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

    // Class pembantu untuk mengurutkan probabilitas tanaman
    static class CropProb implements Comparable<CropProb> {
        String name;
        double probability;
        CropProb(String name, double probability) {
            this.name = name;
            this.probability = probability;
        }
        @Override
        public int compareTo(CropProb o) {
            return Double.compare(o.probability, this.probability); // Descending
        }
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody PredictionRequest request) {
        try {
            if (model == null || modelHeader == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Model Weka belum siap."));
            }

            Instances dataset = new Instances(modelHeader, 0);
            dataset.setClassIndex(dataset.numAttributes() - 1); 

            double n = request.getN();
            double p = request.getP();
            double k = request.getK();
            double ph = request.getPh();
            double moist = request.getMoisture() == null ? 60.0 : request.getMoisture();

            DenseInstance mainInstance = createInstance(dataset, n, p, k, ph, moist);
            
            // Mengambil seluruh nilai probabilitas dari WEKA murni untuk input ini (tanpa menggeser nilai input)
            double[] probDistributions = model.distributionForInstance(mainInstance);
            
            List<CropProb> allCrops = new ArrayList<>();
            for (int i = 0; i < probDistributions.length; i++) {
                // Hanya ambil tanaman yang benar-benar punya probabilitas terukur (bukan 0.0) di input ini
                if (probDistributions[i] > 0.0) {
                    allCrops.add(new CropProb(dataset.classAttribute().value(i), probDistributions[i]));
                }
            }

            // Urutkan dari probabilitas paling masuk akal ke yang terkecil
            Collections.sort(allCrops);

            List<String> finalPredictions = new ArrayList<>();
            
            // Jika ada data relevan, masukkan maksimal 3. Jika hanya ada 1 data yang cocok, maka muncul 1 saja.
            int limit = Math.min(3, allCrops.size());
            for (int i = 0; i < limit; i++) {
                finalPredictions.add(allCrops.get(i).name);
            }

            // Jika model J48 hanya mengembalikan 1 daun dominan absolut, dan Anda ingin toleransi tambahan organik
            if (finalPredictions.size() < 2) {
                 // Cari tanaman rekomendasi atas (+10%) & bawah (-10%) HANYA JIKA probabilitas dasar kosong melompong.
                 double predUp = model.classifyInstance(createInstance(dataset, n * 1.1, p * 1.1, k * 1.1, ph, moist));
                 String cropUp = dataset.classAttribute().value((int) predUp);
                 if (!finalPredictions.contains(cropUp)) finalPredictions.add(cropUp);

                 double predDown = model.classifyInstance(createInstance(dataset, n * 0.9, p * 0.9, k * 0.9, ph, moist));
                 String cropDown = dataset.classAttribute().value((int) predDown);
                 if (!finalPredictions.contains(cropDown)) finalPredictions.add(cropDown);
            }

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