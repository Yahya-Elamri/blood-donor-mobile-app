package ma.ump.blooddonor.entity;

public class Donation {
    private Long id;
    private String date;
    private String lieu;

    public Donation(Long id, String date, String lieu) {
        this.id = id;
        this.date = date;
        this.lieu = lieu;
    }

    // Getters
    public Long getId() { return id; }
    public String getDate() { return date; }
    public String getLieu() { return lieu; }
}