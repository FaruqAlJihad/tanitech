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

    // PERBAIKAN 1: Mengisi nilai berdasarkan NAMA kolom (Dinamis), sehingga mustahil terbalik/error
    private DenseInstance createInstance(Instances dataset, double n, double p, double k, double ph, double moist) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);
        
        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (i == dataset.classIndex()) continue;
            
            String attrName = dataset.attribute(i).name().toLowerCase();
            
            if (attrName.equals("n") || attrName.contains("nitrogen")) {
                instance.setValue(i, n);
            } else if (attrName.equals("p") || attrName.contains("phosphor") || attrName.contains("fosfor")) {
                instance.setValue(i, p);
            } else if (attrName.equals("k") || attrName.contains("potassium") || attrName.contains("kalium")) {
                instance.setValue(i, k);
            } else if (attrName.contains("ph")) {
                instance.setValue(i, ph);
            } else if (attrName.contains("moist") || attrName.contains("humid") || attrName.contains("lembab")) {
                instance.setValue(i, moist);
            } else {
                instance.setValue(i, 0); // Fallback aman
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
            double moist = request.getMoisture() == null ? 50.0 : request.getMoisture();

            DenseInstance mainInstance = createInstance(dataset, request.getN(), request.getP(), request.getK(), request.getPh(), moist);
            double[] probs = model.distributionForInstance(mainInstance);

            // PERBAIKAN 2: Paksa ambil 3 probabilitas tertinggi teratas agar UI selalu menampilkan 3 tanaman berbeda
            for (int i = 0; i < 3; i++) {
                int maxIdx = -1;
                double maxProb = -1.0;
                for (int j = 0; j < probs.length; j++) {
                    if (probs[j] > maxProb && !recommendedCrops.contains(dataset.classAttribute().value(j))) {
                        maxProb = probs[j];
                        maxIdx = j;
                    }
                }
                // Hapus syarat "maxProb > 0.0" yang lama agar model dipaksa memberikan opsi cadangan
                if (maxIdx != -1) {
                    recommendedCrops.add(dataset.classAttribute().value(maxIdx));
                }
            }

            // PERBAIKAN 3: Lapisan keamanan cadangan eksplorasi (hanya berjalan jika entah kenapa masih gagal)
            double multiplier = 1.10;
            while (recommendedCrops.size() < 3 && multiplier <= 2.00) {
                double predUp = model.classifyInstance(createInstance(dataset, 
                    request.getN() * multiplier, request.getP() * multiplier, request.getK() * multiplier, request.getPh(), moist * 1.1
                ));
                recommendedCrops.add(dataset.classAttribute().value((int) predUp));

                double factorDown = Math.max(0.1, 2.0 - multiplier); 
                double predDown = model.classifyInstance(createInstance(dataset, 
                    request.getN() * factorDown, request.getP() * factorDown, request.getK() * factorDown, request.getPh(), moist * 0.9
                ));
                recommendedCrops.add(dataset.classAttribute().value((int) predDown));

                multiplier += 0.20; 
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