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
package at.asit.pdfover.gui.workflow.config;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * An interface for setting the configuration overlay
 * 
 * This overlay overrides the actual configuration but will not be saved
 */
public interface ConfigOverlayManipulator {
	/**
	 * Sets the default bku type
	 * @param bku the bku type
	 */
	public void setDefaultBKUOverlay(BKUs bku);

	/**
	 * Sets the default mobile number
	 * @param number the default mobile number
	 */
	public void setDefaultMobileNumberOverlay(String number);

	/**
	 * Sets the default password
	 * @param password the default password
	 */
	public void setDefaultMobilePasswordOverlay(String password);

	/**
	 * Sets the default emblem
	 * @param emblem the default emblem
	 */
	public void setDefaultEmblemOverlay(String emblem);

	/**
	 * Sets the proxy host
	 * @param host the proxy host
	 */
	public void setProxyHostOverlay(String host);

	/**
	 * Sets the proxy port
	 * @param port the proxy port
	 */
	public void setProxyPortOverlay(int port);

	/**
	 * Sets the proxy username
	 * @param user the proxy username
	 */
	public void setProxyUserOverlay(String user);

	/**
	 * Sets the proxy password
	 * @param pass the proxy password
	 */
	public void setProxyPassOverlay(String pass);

	/**
	 * Sets the default output folder
	 * @param outputFolder the default output folder
	 */
	public void setDefaultOutputFolderOverlay(String outputFolder);

	/**
	 * Sets the signature position
	 * @param position the signature position
	 */
	public void setDefaultSignaturePositionOverlay(SignaturePosition position);

	/**
	 * Sets whether to skip the finish screen
	 * @param skipFinish whether to skip the finish screen
	 */
	public void setSkipFinishOverlay(boolean skipFinish);

	/**
	 * Sets whether keystore signing is enabled
	 * @param enabled whether keystore signing is enabled
	 */
	public void setKeyStoreEnabledOverlay(Boolean enabled);

	/**
	 * Sets the keystore file
	 * @param file the keystore file
	 */
	public void setKeyStoreFileOverlay(String file);

	/**
	 * Sets the keystore type
	 * @param type the keystore type
	 */
	public void setKeyStoreTypeOverlay(String type);

	/**
	 * Sets the keystore alias
	 * @param alias the keystore alias
	 */
	public void setKeyStoreAliasOverlay(String alias);

	/**
	 * Sets the keystore store password
	 * @param storePass the keystore store password
	 */
	public void setKeyStoreStorePassOverlay(String storePass);

	/**
	 * Sets the keystore key password
	 * @param keyPass the keystore key password
	 */
	public void setKeyStoreKeyPassOverlay(String keyPass);

	/**
	 * Sets the configuration file
	 * @param configurationFile
	 */
	public void setConfigurationFile(String configurationFile);
}
