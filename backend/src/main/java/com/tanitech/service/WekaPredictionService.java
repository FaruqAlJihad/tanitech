package com.tanitech.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.tanitech.dto.PredictionRequest;

import jakarta.annotation.PostConstruct;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@Service
public class WekaPredictionService {

    private Classifier classifier;
    private Instances dataStructure;

    @PostConstruct
    public void init() {
        try {
            // 1. Memuat file model Weka dari folder resources/models/
            ClassPathResource resource = new ClassPathResource("tanitech-final.model");
            try (InputStream is = resource.getInputStream()) {
                classifier = (Classifier) SerializationHelper.read(is);
            }

            // 2. Mendefinisikan atribut sesuai struktur model (N, P, K, humidity, ph)
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("N"));
            attributes.add(new Attribute("P"));
            attributes.add(new Attribute("K"));
            attributes.add(new Attribute("humidity"));
            attributes.add(new Attribute("ph"));

            // 3. Mendefinisikan 138 label target sesuai log model dataset terbaru
            List<String> cropLabels = Arrays.asList(
                "rice", "maize", "chickpea", "kidneybeans", "pigeonpeas", "mothbeans", "mungbean", "blackgram", 
                "lentil", "pomegranate", "banana", "mango", "grapes", "watermelon", "muskmelon", "apple", 
                "orange", "papaya", "coconut", "cotton", "jute", "coffee", "guinea_corn", "pepper", "tomatoes", 
                "yam", "beans", "soybean", "cassava", "wheat", "sorghum", "Pearl millet", "ragi", "panivaragu", 
                "samai", "thinai", "varagu", "kudiraivali", "greengram", "cowpea", "bengalgram", "horsegram", 
                "redgram", "soyabean", "groundnut", "sunflower", "gingely", "castor", "sugarcane", "sugarbeet", 
                "tomato", "onion", "chillies", "Cabbage", "bhendi", "brinjal", "capsicum", "pumpkin", 
                "snake gourd", "ribbed gourd", "bottle gourd", "bitter gourd", "ash gourd", "cucumber", "tinda", 
                "chowchow", "cluster bean", "vegetable cowpea", "french bean", "peas", "annual moringa", "carrot", 
                "beetroot", "radish", "sweet potato", "tapoica", "elephant foot yam", "cauliflower", "small onion", 
                "almond", "apricot", "asparagus", "avocado", "barley", "bean", "blackberry", "blueberry", 
                "broccoli", "buckwheat", "cabbage", "cashew", "celery", "cherry", "chili", "cranberry", "date", 
                "dragonfruit", "durian", "eggplant", "fig", "garlic", "ginger", "guava", "kiwi", "lettuce", 
                "linseed", "lychee", "mangosteen", "millet", "mustard", "oat", "okra", "passion_fruit", "peach", 
                "peanut", "pear", "pineapple", "plum", "potato", "quinoa", "rambutan", "rapeseed", "raspberry", 
                "rubber", "rye", "safflower", "sesame", "spinach", "squash", "strawberry", "sweetpotato", "taro", 
                "tea", "tobacco", "turmeric", "turnip", "walnut", "zucchini"
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

        // Menyusun data instance baru
        DenseInstance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);
        
        instance.setValue(0, req.getN());
        instance.setValue(1, req.getP());
        instance.setValue(2, req.getK());
        instance.setValue(3, req.getMoisture()); // Kelembaban 
        instance.setValue(4, req.getPh());

        // Menjalankan klasifikasi
        double classIndex = classifier.classifyInstance(instance);
        return dataStructure.classAttribute().value((int) classIndex);
    }
}