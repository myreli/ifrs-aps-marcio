package com.myreli;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;


/**
 * @author myreli
 *
 */
public abstract class AbstractModel {

	protected Integer id;
	protected Timestamp last_update;
	protected final SimpleDateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected final SimpleDateFormat ISOTimeFormat = new SimpleDateFormat("hh:mm");
	private Map<String, String> errors = new HashMap<>();
	
	public abstract void validate();
		
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getLast_update() {
		return last_update;
	}

	public void setLast_update(Timestamp timestamp) {
		this.last_update = timestamp;
	}

	/**
	 * @return true if the object is valid due to its validation requirements
	 */
	public boolean isValid() {
		return getErrors().isEmpty();
	}
	
	/**
	 * @return true if the object is transient
	 */
	public boolean isTransient() {
		return getId() == null;
	}
	
	/**
	 * @return true if the object is persistent
	 */
	public boolean isPersistent() {
		return getId() != null;
	}

	/**
	 * @return the errors
	 */
	public Map<String, String> getErrors() {
		return errors;
	}

	/**
	 * @param field
	 * @param message
	 */
	protected void addError(String field, String message) {
		errors.put(field, message);
	}
	
	/**
	 * empty the errors map
	 */
	protected void cleanErrors() {
		errors.clear();
	}
	
	protected boolean checkLength(String fieldName, String value, int minLength, int maxLengh) {
		if(!checkEmpty(fieldName, value)) {
			if (value.length() < minLength || value.length() > maxLengh) {
				addError(fieldName, "O " + fieldName + " deve ter entre " + minLength + " e " + maxLengh + " caracteres.");			
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean checkEmpty(String fieldName, String value) {
		if (value == null || value.isEmpty()) {
			addError(fieldName, "O " + fieldName + " não pode ser vazio.");
			return false;
		}
		
		return true;
	}
	
	protected boolean checkEmpty(String fieldName, Object value) {
		if (value == null) {
			addError(fieldName, "O " + fieldName + " não foi informado.");
			return false;
		}
		
		return true;
	}
	
	protected boolean checkRequired(String fieldName, Object value) {
		if (value == null) {
			addError(fieldName, "O " + fieldName + " é obrigatório.");
			return false;
		}
		
		return true;
	}
	
	protected boolean checkQuantity(String fieldName, Integer value) {
		if (value == null || value < 1) {
			addError(fieldName, "A quantidade de " + fieldName + " é obrigatória.");
			return false;
		}
		
		return true;
	}
	
	protected boolean checkDate(String fieldName, LocalDate value) {
		if (value == null) {
			addError(fieldName, "A data " + fieldName + " é obrigatória.");
			return false;
		}
		
		return true;
	}
	
	protected boolean checkTime(String fieldName, LocalTime value) {
		if (value == null) {
			addError(fieldName, fieldName + " não foi informado(a).");
			return false;
		}
		
		return true;
	}
	
	protected boolean checkMail(String value) {
		if (!value.matches("[\\w._-]+@\\w+(\\.\\w+)+")) {
			addError("e-mail", "E-mail inválido, ele deve ter o formato de usuario@provedor");
			return false;
		}
		
		return true;
	}
	
	public abstract void load(int id) throws ModelNotFoundException;	
	
	public boolean save() {  // TEMPLATE METHOD
		validate();
		if (isValid()) {
			doSave(); // GANCHO
			return true;
		}
		return false;
	}
	
	public abstract void delete();
	
	protected abstract void doSave();
	
}
