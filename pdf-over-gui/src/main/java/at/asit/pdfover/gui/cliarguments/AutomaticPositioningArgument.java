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
package at.asit.pdfover.gui.cliarguments;

import at.asit.pdfover.gui.exceptions.InitializationException;
import at.asit.pdfover.signator.SignaturePosition;

/**
 * CLI Argument to set the BKU to use
 */
public class AutomaticPositioningArgument extends Argument {
	/**
	 * Constructor
	 */
	public AutomaticPositioningArgument() {
		super(new String[] { "-a" }, "argument.help.autopos"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.asit.pdfover.gui.cliarguments.CLIArgument#handleArgument(java.lang
	 * .String[], int, at.asit.pdfover.gui.workflow.StateMachine,
	 * at.asit.pdfover.gui.cliarguments.ArgumentHandler)
	 */
	@Override
	public int handleArgument(String[] args, int argOffset,
			ArgumentHandler handler)
			throws InitializationException {
		getConfiguration().setDefaultSignaturePositionOverlay(new SignaturePosition());
		return argOffset;
	}

}
