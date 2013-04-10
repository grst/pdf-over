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
package at.asit.pdfover.gui.exceptions;

import java.io.File;

import at.asit.pdfover.gui.Messages;

/**
 * 
 */
public class OutputfolderDontExistException extends ResumeableException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1005495921021280599L;
	
	/**
	 * @param folder The file
	 * @param resumeIndex The resume Index
	 */
	public OutputfolderDontExistException(final File folder, int resumeIndex) {
		super(String.format(Messages.getString("exception.PathNotExist"), folder.getAbsolutePath()), resumeIndex); //$NON-NLS-1$
	}

}
