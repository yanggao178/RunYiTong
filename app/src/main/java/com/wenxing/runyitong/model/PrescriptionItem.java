package com.wenxing.runyitong.model;

/**
 * 处方项目数据模型
 * 用于表示处方中的单个药品项目
 */
public class PrescriptionItem {
    private int id;
    private int prescriptionId;
    private String medicineName;
    private String specification;
    private String dosage;
    private String frequency;
    private int quantity;
    private String unit;
    private double unitPrice;
    private double totalPrice;
    private String usage;
    private String notes;

    /**
     * 默认构造函数
     */
    public PrescriptionItem() {
    }

    /**
     * 完整构造函数
     */
    public PrescriptionItem(int id, int prescriptionId, String medicineName, 
                           String specification, String dosage, String frequency, 
                           int quantity, String unit, double unitPrice, 
                           double totalPrice, String usage, String notes) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.medicineName = medicineName;
        this.specification = specification;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.usage = usage;
        this.notes = notes;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * 获取完整的药品描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(medicineName);
        if (specification != null && !specification.isEmpty()) {
            sb.append(" (").append(specification).append(")");
        }
        return sb.toString();
    }

    /**
     * 获取用法用量描述
     */
    public String getDosageDescription() {
        StringBuilder sb = new StringBuilder();
        if (dosage != null && !dosage.isEmpty()) {
            sb.append(dosage);
        }
        if (frequency != null && !frequency.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(frequency);
        }
        return sb.toString();
    }
}