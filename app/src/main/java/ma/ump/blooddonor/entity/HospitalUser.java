package ma.ump.blooddonor.entity;

public class HospitalUser {
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String position;

    private Hospital hospital;

    // Constructor
    public HospitalUser(String email, String password, String nom, String prenom, String position , Hospital hospital) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.position=position;
        this.hospital=hospital;
    }

    // Getters and optionally setters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getPosition() {
        return position;
    }

    public Hospital getHospital() {
        return hospital;
    }
}
