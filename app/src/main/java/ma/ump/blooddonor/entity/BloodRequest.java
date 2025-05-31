package ma.ump.blooddonor.entity;

public class BloodRequest {
    private BloodType groupeSanguin;
    private Integer quantite;
    private String dateDemande;
    private RequestStatus statut;
    private UrgenceLevel urgence;

    // Getters and setters
    public BloodType getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(BloodType groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String dateDemande) { this.dateDemande = dateDemande; }

    public RequestStatus getStatut() { return statut; }
    public void setStatut(RequestStatus statut) { this.statut = statut; }

    public UrgenceLevel getUrgence() { return urgence; }
    public void setUrgence(UrgenceLevel urgence) { this.urgence = urgence; }
}