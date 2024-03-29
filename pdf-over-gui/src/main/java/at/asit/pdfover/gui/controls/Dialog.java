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
package at.asit.pdfover.gui.controls;

// Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * A Message dialog
 */
public class Dialog {

	private MessageBox box;
	
	/**
	 * Message box buttons
	 */
	public enum BUTTONS {
		/** Display only ok button */
		OK,
		/** Display buttons ok and cancel */
		OK_CANCEL,
		/** Display retry and cancel buttons */
		RETRY_CANCEL,
		/** Display abort, retry and ignore buttons */
		ABORT_RETRY_IGNORE,
		/** Display yes and no buttons */
		YES_NO
	};
	
	/**
	 * Message box icon
	 */
	public enum ICON {
		/** Error icon */
		ERROR,
		/** Information icon */
		INFORMATION,
		/** Question icon */
		QUESTION,
		/** Warning icon */
		WARNING,
		/** Working icon */
		WORKING
	};
	
	/**
	 * @param parent The parent shell
	 * @param title The dialog title
	 * @param message The dialog message
	 * @param button The BUTTONS to be shown
	 * @param icon The ICON to be displayed
	 */
	public Dialog(Shell parent, String title, String message, BUTTONS button, ICON icon) {
		this.initialize(parent, title, message, button, icon);
	}

	private void initialize(Shell parent, String title, String message, BUTTONS button, ICON icon) {
		int boxstyle = 0;
		switch (icon) {
			case ERROR:
				boxstyle |= SWT.ICON_ERROR;
				break;
			case INFORMATION:
				boxstyle |= SWT.ICON_INFORMATION;
				break;
			case QUESTION:
				boxstyle |= SWT.ICON_QUESTION;
				break;
			case WARNING:
				boxstyle |= SWT.ICON_WARNING;
				break;
			case WORKING:
				boxstyle |= SWT.ICON_WORKING;
				break;
		}

		switch(button) {
			case OK:
				boxstyle |= SWT.OK;
				break;
			case OK_CANCEL:
				boxstyle |= SWT.OK | SWT.CANCEL;
				break;
			case RETRY_CANCEL:
				boxstyle |= SWT.RETRY | SWT.CANCEL;
				break;
			case ABORT_RETRY_IGNORE:
				boxstyle |= SWT.RETRY | SWT.ABORT | SWT.IGNORE;
				break;
			case YES_NO:
				boxstyle |= SWT.YES | SWT.NO;
		}

		this.box = new MessageBox(parent, boxstyle);
		this.box.setMessage(message);
		this.box.setText(title);
	}

	/**
	 * Open error dialog
	 * 
	 * @return SWT.OK | SWT.IGNORE | SWT.ABORT | SWT.RETRY | SWT.CANCEL
	 */
	public int open() {
		return this.box.open();
	}
}
