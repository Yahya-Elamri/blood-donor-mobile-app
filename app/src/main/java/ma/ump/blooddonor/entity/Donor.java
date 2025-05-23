package ma.ump.blooddonor.entity;

public class Donor {
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String groupeSanguin;
    private String localisation;

    // Constructor
    public Donor(String email, String password, String nom, String prenom, String groupeSanguin , String localisation) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.groupeSanguin = groupeSanguin;
        this.localisation = localisation;
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

    public String getGroupeSanguin() {
        return groupeSanguin;
    }

    public String getLocalisation() {
        return localisation;
    }
}
