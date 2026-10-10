package com.tanitech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PredictionResponse {
    
    private String status;
    
    // Anotasi ini memastikan JSON yang dikirim ke React bernama "kelas_tanah"
    @JsonProperty("kelas_tanah")
    private String kelasTanah;
    
    // Anotasi ini memastikan JSON bernama "tanaman_terpilih"
    @JsonProperty("tanaman_terpilih")
    private String tanamanTerpilih;

    // Constructor kosong (diperlukan oleh Spring Boot)
    public PredictionResponse() {
    }

    // Constructor dengan parameter
    public PredictionResponse(String status, String kelasTanah, String tanamanTerpilih) {
        this.status = status;
        this.kelasTanah = kelasTanah;
        this.tanamanTerpilih = tanamanTerpilih;
    }

    // Getter dan Setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKelasTanah() {
        return kelasTanah;
    }

    public void setKelasTanah(String kelasTanah) {
        this.kelasTanah = kelasTanah;
    }

    public String getTanamanTerpilih() {
        return tanamanTerpilih;
    }

    public void setTanamanTerpilih(String tanamanTerpilih) {
        this.tanamanTerpilih = tanamanTerpilih;
    }
}