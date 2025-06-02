package ma.ump.blooddonor.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hospital {
    private Long id;
    private String nom;
    private String adresse;
    private String telephone;
    public Hospital() {}

    public Hospital(Long id, String nom, String adresse, String telephone) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    private List<BloodRequest> demandes = new ArrayList<>();
    private List<HospitalUser> staff = new ArrayList<>();

    // Add getters and setters
    public List<BloodRequest> getDemandes() {
        return demandes;
    }

    public void setDemandes(List<BloodRequest> demandes) {
        this.demandes = demandes;
    }

    public List<HospitalUser> getStaff() {
        return staff;
    }

    public void setStaff(List<HospitalUser> staff) {
        this.staff = staff;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return nom; // This is important for AutoCompleteTextView display
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hospital hospital = (Hospital) obj;
        return Objects.equals(id, hospital.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}