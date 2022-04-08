/**
 * 
 */
package it.sanita.fse.validator.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author AndreaPerquoti
 *
 */
@Slf4j
public class JsonUtility {


	/**
	 * Il costruttore è privato perchè la classe non va istanziata
	 * 
	 * @return void
	 * @throws IllegalStateException
	 */
	private JsonUtility() {
//		throw new IllegalStateException("Questa è una classe di utilità non va istanziata!!!");
	}

	private static ObjectMapper mapper = new ObjectMapper(); 

	/**
	 * Il metodo converte un oggetto in una stringa JSON
	 * 
	 * @param oggetto
	 *            generico
	 * @return una stringa rappresentante l'oggetto in formato JSON
	 */
	public static <T> String objectToJson(T obj) {
		String jsonString = "";

		try {
			jsonString = mapper.writeValueAsString(obj);
		} catch (Exception e) {
			log.error("Errore durante la conversione da oggetto {} a string json: {}", obj.getClass(), e);
		}

		return jsonString;
	}
	
	/**
	 * Il metodo converte una stringa JSON in un oggetto java
	 * 
	 * @param stringa in formato JSON
	 * @return un oggetto della classe specificata dal segnaposto
	 */
	public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
		T obj = null;
		try {
			obj = mapper.readValue(jsonString, clazz);
		} catch (Exception e) {
			log.error("Errore durante la conversione da stringa json a oggetto: {}", e);
		}

		return obj;
	}
	
	/**
	 * Il metodo converte una stringa JSON in una struttura a nodi
	 * 
	 * @param stringa in formato JSON
	 * @return ObjectNode type
	 */
	public static ObjectNode getNodeFromJson(String jsonString) {
		ObjectNode obj = null;
		try {
			obj = mapper.readValue(jsonString, ObjectNode.class);
		} catch (Exception e) {
			log.error("Errore durante la conversione da stringa json a struttura a nodi: {}", e);
		}
		
		return obj;
	}

}
