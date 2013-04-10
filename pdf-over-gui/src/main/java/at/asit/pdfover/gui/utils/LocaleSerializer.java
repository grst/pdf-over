/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.gui.utils;

// Imports
import java.util.Locale;

/**
 * 
 */
public class LocaleSerializer {

	/**
	 * Parse a locale from a string
	 * @param localeString the string
	 * @return the locale
	 */
	public static Locale parseFromString(String localeString) {
		
		if(localeString == null || localeString.isEmpty()) {
			return null;
		}
		
		Locale targetLocal = null;
		Locale[] locale = Locale.getAvailableLocales();
		for(int i = 0; i < locale.length; i++) {
			if(locale[i].toString().equals(localeString)) {
				targetLocal = locale[i];
				break;
			}
		}
		return targetLocal;
	}
	
	/**
	 * creates a parseable string for a locale
	 * @param locale the locale
	 * @return the parseable string
	 */
	public static String getParseableString(Locale locale) {
		return locale.toString();
	}
	
}