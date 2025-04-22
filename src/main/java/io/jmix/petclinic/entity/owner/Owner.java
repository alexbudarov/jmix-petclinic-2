package io.jmix.petclinic.entity.owner;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.petclinic.entity.Person;
import io.jmix.petclinic.entity.pet.Pet;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@JmixEntity
@Table(name = "PETCLINIC_OWNER")
@Entity(name = "petclinic_Owner")
public class Owner extends Person {

    @NotNull
    @Column(name = "ADDRESS", nullable = false)
    private String address;

    @Column(name = "CITY", nullable = false)
    @NotNull
    private String city;

    @Email
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TELEPHONE")
    private String telephone;

    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    @OrderBy("identificationNumber")
    @OneToMany(mappedBy = "owner")
    private List<Pet> pets;

    @Pattern(regexp = "[А-Я]{2} \\d{2} \\d{6}", message = "Wrong passport number format")
    @Column(name = "PASSPORT_NUMBER", length = 12)
    private String passportNumber;

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}