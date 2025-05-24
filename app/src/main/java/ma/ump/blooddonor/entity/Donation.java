package ma.ump.blooddonor.entity;

public class Donation {
    private Long id;
    private String date;
    private String lieu;
    private int amount;

    public Donation(Long id, String date, String lieu,int amount) {
        this.id = id;
        this.date = date;
        this.lieu = lieu;
        this.amount =amount;
    }

    // Getters
    public Long getId() { return id; }
    public String getDate() { return date; }
    public String getLieu() { return lieu; }
    public int getAmount() { return amount; }
}