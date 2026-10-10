package com.tanitech.service;

import com.tanitech.dto.PredictionRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class WekaPredictionService {

    private Classifier classifier;
    private Instances dataStructure;

    @PostConstruct
    public void init() {
        try {
            // 1. Memuat file model Weka dari folder resources/models/
            ClassPathResource resource = new ClassPathResource("models/138 V3.model");
            try (InputStream is = resource.getInputStream()) {
                classifier = (Classifier) SerializationHelper.read(is);
            }

            // 2. Mendefinisikan atribut sesuai struktur model yang dilatih
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("N"));
            attributes.add(new Attribute("P"));
            attributes.add(new Attribute("K"));
            attributes.add(new Attribute("temperature")); // Standar dataset agrikultur
            attributes.add(new Attribute("humidity"));    // Setara dengan moisture
            attributes.add(new Attribute("ph"));
            attributes.add(new Attribute("rainfall"));    // Standar dataset agrikultur

            // 3. Mendefinisikan label target klasifikasi (disesuaikan dengan dataset Kaggle/Weka)
            List<String> cropLabels = Arrays.asList(
                "rice", "maize", "chickpea", "kidneybeans", "pigeonpeas",
                "mothbeans", "mungbean", "blackgram", "lentil", "pomegranate",
                "banana", "mango", "grapes", "watermelon", "muskmelon",
                "apple", "orange", "papaya", "coconut", "cotton", "jute", "coffee"
            );
            attributes.add(new Attribute("label", new ArrayList<>(cropLabels)));

            // 4. Inisialisasi struktur dataset
            dataStructure = new Instances("AgroDataset", attributes, 0);
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);

            System.out.println("Model Weka TaniTech berhasil dimuat ke memori sistem.");
        } catch (Exception e) {
            System.err.println("Gagal memuat model Weka: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String predictCrop(PredictionRequest req) throws Exception {
        if (classifier == null) {
            throw new IllegalStateException("Model klasifikasi belum siap.");
        }

        // Menyusun data instance baru. Jumlah nilai (8) harus sama dengan jumlah atribut.
        DenseInstance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);
        
        instance.setValue(0, req.getN());
        instance.setValue(1, req.getP());
        instance.setValue(2, req.getK());
        // Menggunakan suhu dan curah hujan rata-rata ideal jika tidak diinput via form
        instance.setValue(3, 25.0); 
        instance.setValue(4, req.getMoisture());
        instance.setValue(5, req.getPh());
        instance.setValue(6, 200.0);

        // Menjalankan klasifikasi
        double classIndex = classifier.classifyInstance(instance);
        return dataStructure.classAttribute().value((int) classIndex);
    }
}