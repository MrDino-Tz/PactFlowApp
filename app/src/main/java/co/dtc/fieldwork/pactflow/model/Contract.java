package co.dtc.fieldwork.pactflow.model;

public class Contract {
    private Long id;
    private String title;
    private Double amount;
    private Long workflowId;
    private String contractType;
    private String description;
    private String startDate;
    private String endDate;
    private String template;
    @com.google.gson.annotations.SerializedName("isFinalized")
    private boolean isFinalized;

    // Add this to handle the getter name for Gson
    @com.google.gson.annotations.SerializedName("finalized")
    private boolean finalized;
    private int version;
    private String timestamp;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isFinalized() {
        return isFinalized || finalized;
    }

    public void setFinalized(boolean finalized) {
        this.isFinalized = finalized;
        this.finalized = finalized;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
