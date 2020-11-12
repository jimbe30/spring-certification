package net.jmb.tuto.spring.databinding;

import java.util.Properties;

public interface ApplicationMessages {

	String NAME_EMPTY = "name.empty";
	String NEGATIVE_VALUE = "negativevalue";
	String TOO_OLD = "too.darn.old";
	String FIELD_REQUIRED = "field.required";
	String NUMBER_FORMAT = "number.format.error";

	Properties MESSAGES = new Properties() {
		private static final long serialVersionUID = 1L;
		{
			put(NAME_EMPTY, "Le nom doit être renseigné");
			put(NEGATIVE_VALUE, "La valeur du champ ''{0}'' ne doit pas être négative");
			put(TOO_OLD, "L'âge est trop vieux");
			put(FIELD_REQUIRED, "Le champ ''{0}'' est obligatoire");
			put(NUMBER_FORMAT, "Le champ ''{0}'' doit être numérique sur {1} positions");
		}
	};

}
