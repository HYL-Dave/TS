package com.toppanidgate.fidouaf.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author preetham
 */
//@TypeDef(
//	    name="encryptedString", 
//	    typeClass=EncryptedStringType.class, 
//	    parameters= {
//	        @Parameter(name="encryptorRegisteredName", value="configurationHibernateEncryptor")
//	    }
//	)
@Entity
@Table(name = "AUTHENTICATOR", schema = "fido")
public class Authenticator {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

//	@Column(unique = true)
	private String devkey;
	
//	@Type(type="encryptedString")
	private String userID;

	@Column(length = 3000)
	private String value;

	@Column(length = 1)
	private String available; // �s�W��ƹw�]�� 'Y' , �R���ɳ]�� 'N' (���|�u���q DB delete �A�H�Q�Ƭd)

	private Date date; // ��ƽs�פ��

	public Authenticator() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDevkey() {
		return devkey;
	}

	public void setDevkey(String devkey) {
		this.devkey = devkey;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAvailable() {
		return available;
	}

	public void setAvailable(String available) {
		this.available = available;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Authenticator [id=" + id + ", devkey=" + devkey + ", value=" + value + "]";
	}

}
