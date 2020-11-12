package net.jmb.tuto.spring.databinding.beans;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import net.jmb.tuto.spring.databinding.validators.constraints.Insee;

public class Person {
	
	@Size(min = 2, max = 30, message = "Le nom doit doit comporter entre 2 et 30 caractères")
	private String name;
	
	@NotNull @Min(16) @Max(130)
    private Integer age;
	
	@NotNull @Valid
    private Adress adress;    
    
	@DateTimeFormat(pattern="dd/MM/yyyy")
    @NotNull @Past()
	private Date birthDate;
	
	@Insee(withKey = true)
	private String insee;
	
	
	
    
    public String getInsee() {
		return insee;
	}
    
	public Person setInsee(String insee) {
		this.insee = insee;
		return this;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public Person setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
		return this;
	}
	public Adress getAdress() {
		return adress;
	}
	public Person setAdress(Adress adress) {
		this.adress = adress;
		return this;
	}
	public String getName() {
		return name;
	}
	public Person setName(String name) {
		this.name = name;
		return this;
	}
	public Integer getAge() {
		return age;
	}
	public Person setAge(Integer age) {
		this.age = age;
		return this;
	}
    

}
