package com.tanitech.dto;

public class PredictionRequest {
    private Double n;
    private Double p;
    private Double k;
    private Double ph;
    private Double moisture;

    public Double getN() { return n; }
    public void setN(Double n) { this.n = n; }

    public Double getP() { return p; }
    public void setP(Double p) { this.p = p; }

    public Double getK() { return k; }
    public void setK(Double k) { this.k = k; }

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Double getMoisture() { return moisture; }
    public void setMoisture(Double moisture) { this.moisture = moisture; }
}