package nav.portal.core.entities;

import java.util.Objects;
import java.util.UUID;

public class CitizenUserEntity {
    private UUID userID;
    private String epost;
    private String fornavn;
    private String etternavn;
    private String tlf;

    public UUID getUserID() {
        return userID;
    }

    public CitizenUserEntity setUserID(UUID userID) {
        this.userID = userID;
        return this;
    }

    public String getFornavn() {
        return fornavn;
    }

    public CitizenUserEntity setFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public CitizenUserEntity setEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }

    public String getTlf() {
        return tlf;
    }

    public CitizenUserEntity setTlf(String tlf) {
        this.tlf = tlf;
        return this;
    }

    public String getEpost() {
        return epost;
    }

    public CitizenUserEntity setEpost(String epost) {
        this.epost = epost;
        return this;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CitizenUserEntity that = (CitizenUserEntity) o;
        return Objects.equals(userID, that.userID) && Objects.equals(fornavn, that.fornavn) && Objects.equals(etternavn, that.etternavn) && Objects.equals(tlf, that.tlf) && Objects.equals(epost, that.epost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, fornavn, etternavn, tlf, epost);
    }

}
