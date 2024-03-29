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
package at.asit.pdfover.gui.composites;

// Imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.utils.Messages;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * 
 */
public class SignaturePanel extends JPanel {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(SignaturePanel.class);

	/** Default serial version ID */
	private static final long serialVersionUID = 1L;

	/** The PDF file being displayed */
	private PDFFile pdf = null;
	/** The image of the rendered PDF page being displayed */
	Image currentImage = null;
	/** The current PDFPage that was rendered into currentImage */
	private PDFPage currentPage = null;
	/** The current transform from screen to page space */
	AffineTransform currentXform = null;
	/** The horizontal offset of the image from the left edge of the panel */
	int offX = 0;
	/** The vertical offset of the image from the top of the panel */
	int offY = 0;
	/** The size of the image */
	private Dimension prevSize = null;
	/** The position of the signature, in page space */
	Point2D sigPagePos = null;
	/** The position of the signature, in screen space */
	Point2D sigScreenPos = null;
	/** The signature placeholder image */
	private Image sigPlaceholder = null;
	/** Current scaled signature placeholder image */
	BufferedImage sigPlaceholderScaled = null;
	/** Transparency of the signature placeholder (0-255) */
	private int sigPlaceholderTransparency = Constants.DEFAULT_SIGNATURE_PLACEHOLDER_TRANSPARENCY;
	/** Previous Transparency of the signature placeholder */
	private int prevSigPlaceholderTransparency = 0;
	/** Width of the signature placeholder in page space */
	private int sigPageWidth = 0;
	/** Height of the signature placeholder in page space */
	private int sigPageHeight = 0;
	/** Width of the signature placeholder in screen space */
	int sigScreenWidth = 0;
	/** Height of the signature placeholder in screen space */
	int sigScreenHeight = 0;
	/** Previous Width of the signature placeholder in screen space */
	int prevSigScreenWidth = 0;
	/** Previous Height of the signature placeholder in screen space */
	int prevSigScreenHeight = 0;
	/** Color of the signature placeholder border */
	private Color sigPlaceholderBorderColor = Color.BLUE;
	/** Current page */
	int page = 0;
	/** Number of pages in the document */
	int numPages = 0;
	/** Cursor types */
	static enum Cursors {DEFAULT, HAND, MOVE};
	/** Default arrow cursor */
	final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	/** Hand cursor */
	final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	/** Move cursor */
	final Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	/** Current cursor */
	Cursors currentCursor = Cursors.DEFAULT;

	/**
	 * Create a new PagePanel.
	 * @param pdf the PDFFile to display
	 */
	public SignaturePanel(PDFFile pdf) {
		super(new BorderLayout());
		setDocument(pdf);
		setPreferredSize(new Dimension(Constants.DEFAULT_MAINWINDOW_WIDTH, Constants.DEFAULT_MAINWINDOW_HEIGHT - Constants.MAINBAR_HEIGHT));
		setFocusable(true);
		addMouseListener(this.mouseListener);
		addMouseMotionListener(this.mouseListener);
	}

	/**
	 * Set a new document to be displayed
	 * @param pdf the PDFFile to be displayed
	 */
	public void setDocument(PDFFile pdf) {
		this.pdf = pdf;
		this.sigPagePos = null;
		this.numPages = pdf.getNumPages();
		showPage(this.numPages);
	}

	/**
	 * Set the signature placeholder image
	 * @param placeholder signature placeholder
	 * @param width width of the placeholder in page space
	 * @param height height of the placeholder in page space 
	 * @param transparency transparency of the signature placeholder (0 - 255)
	 */
	public void setSignaturePlaceholder(Image placeholder, int width, int height, int transparency) {
		this.sigPlaceholder = placeholder;
		this.sigPageWidth = width;
		this.sigPageHeight = height;
		this.sigPlaceholderTransparency = transparency;
	}

	/**
	 * Set the color of the signature placeholder border
	 * @param color new signature placeholder border color
	 */
	public void setSignaturePlaceholderBorderColor(Color color) {
		this.sigPlaceholderBorderColor = color;
	}

	/**
	 * Change the currently displayed page
	 * @param page the number of the page to display
	 */
	public void showPage(int page) {
		this.page = page;
		showPage(this.pdf.getPage(page));
	}

	/**
	 * Add and display a new page at the end of the document
	 * 
	 * This page has the same dimensions as the old last page
	 */
	public void addNewLastPage() {
		showPage(this.numPages + 1);
	}

	/**
	 * Translate the signature placeholder position
	 * @param sigXOffset signature placeholder horizontal position offset
	 * @param sigYOffset signature placeholder vertical position offset
	 */
	public void translateSignaturePosition(int sigXOffset, int sigYOffset) {
		updateSigPos((int) this.sigScreenPos.getX() + sigXOffset, (int) this.sigScreenPos.getY() + sigYOffset);
	}

	/**
	 * Set the signature placeholder position
	 * Call showPage afterwards to update actual position
	 * @param x the horizontal signature position
	 * @param y the vertical signature position
	 */
	public void setSignaturePosition(float x, float y)
	{
		this.sigPagePos = new Point2D.Double(x, y);
	}

	/**
	 * Get the current horizontal position of the signature
	 * @return signature x coordinate
	 */
	public float getSignaturePositionX() {
		if (this.sigPagePos == null)
			return 0;
		return Math.max((float) this.sigPagePos.getX(), 0);
	}

	/**
	 * Get the current vertical position of the signature
	 * @return signature y coordinate
	 */
	public float getSignaturePositionY() {
		if (this.sigPagePos == null)
			return 0;
		return Math.max((float) this.sigPagePos.getY(), 0);
	}

	/**
	 * Stop the generation of any previous page, and draw the new one.
	 * 
	 * @param page the PDFPage to draw.
	 */
	private synchronized void showPage(PDFPage page) {
		// stop drawing the previous page
		if (this.currentPage != null && this.prevSize != null) {
			this.currentPage.stop(this.prevSize.width, this.prevSize.height, null);
		}

		boolean newPage = false;
		// set up the new page
		if (this.page > this.numPages)
		{
			// New last page - use old last page as template
			this.currentPage = this.pdf.getPage(this.numPages);
			newPage = true;
		}
		else
			this.currentPage = page;


		if (this.currentPage == null) {
			// no page
			this.currentImage = null;
			this.currentXform = null;
			repaint();
		} else {
			// start drawing
			Dimension sz = getSize();
			if (sz.width + sz.height == 0) {
				// no image to draw.
				return;
			}

			Dimension pageSize = this.currentPage.getUnstretchedSize(sz.width, sz.height,
					null);

			// get the new image
			if (newPage)
			{
				this.currentImage = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
				Graphics g = this.currentImage.getGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, pageSize.width, pageSize.height);
			}
			else
				this.currentImage = this.currentPage.getImage(pageSize.width, pageSize.height,
						null, this);

			// calculate the transform from page to screen space
			this.currentXform = new AffineTransform(1, 0, 0, -1, 0, pageSize.height);
			Rectangle2D clip = this.currentPage.getBBox();
			double scaleX = pageSize.width / clip.getWidth();
			double scaleY = pageSize.height / clip.getHeight();
			this.currentXform.scale(scaleX, scaleY);
			this.currentXform.translate(-clip.getMinX(), -clip.getMinY());

			if (this.sigPagePos != null)
				this.sigScreenPos = this.currentXform.transform(this.sigPagePos, this.sigScreenPos);
			this.sigScreenWidth = (int) Math.round(this.sigPageWidth * this.currentXform.getScaleX());
			this.sigScreenHeight = (int) Math.round(this.sigPageHeight * this.currentXform.getScaleX());

			// invert the transform (screen to page space)
			try {
				this.currentXform = this.currentXform.createInverse();
			} catch (NoninvertibleTransformException nte) {
				log.error("Error inverting page transform!", nte); //$NON-NLS-1$
			}

			if (this.sigPagePos == null)
			{
				this.sigScreenPos = new Point2D.Double(
						clamp((int) (pageSize.getWidth() / 2), 0, this.currentImage.getWidth(null) - this.sigScreenWidth),
						clamp((int) ((pageSize.getHeight() / 4) * 3), 0, this.currentImage.getHeight(null) - this.sigScreenHeight));
				this.sigPagePos = this.currentXform.transform(this.sigScreenPos, this.sigPagePos);
			}
			else
				updateSigPos((int) this.sigScreenPos.getX(), (int) this.sigScreenPos.getY());

			this.prevSize = pageSize;

			repaint();
		}
	}

	/**
	 * Draw the image.
	 */
	@Override
	public void paint(Graphics g) {
		Dimension sz = getSize();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (this.currentImage == null) {
			g.setColor(Color.black);
			g.drawString(Messages.getString("error.SignaturePanel.NoPage"), getWidth() / 2 - 30, //$NON-NLS-1$
					getHeight() / 2);
			if (this.currentPage != null) {
				showPage(this.currentPage);
			}
		} else {
			// draw the image
			int imwid = this.currentImage.getWidth(null);
			int imhgt = this.currentImage.getHeight(null);

			// draw it centered within the panel
			this.offX = (sz.width - imwid) / 2;
			this.offY = (sz.height - imhgt) / 2;

			if ((imwid == sz.width && imhgt <= sz.height)
					|| (imhgt == sz.height && imwid <= sz.width)) {

				// draw document
				g.drawImage(this.currentImage, this.offX, this.offY, this);

				// draw signature
				int sigX = (int) (this.offX + this.sigScreenPos.getX());
				int sigY = (int) (this.offY + this.sigScreenPos.getY());
				if (this.sigPlaceholder == null) {
					g.setColor(Color.red);
					g.drawRect(sigX, sigY, 100, 40);
				}
				else {
					if (
							((this.sigScreenWidth != this.prevSigScreenWidth) ||
							(this.sigScreenHeight != this.prevSigScreenHeight) ||
							(this.sigPlaceholderTransparency != this.prevSigPlaceholderTransparency)) &&
							((this.sigScreenWidth != 0) && (this.sigScreenHeight != 0)))
					{
						// redraw scaled transparent placeholder
						this.prevSigScreenWidth = this.sigScreenWidth;
						this.prevSigScreenHeight = this.sigScreenHeight;
						this.prevSigPlaceholderTransparency = this.sigPlaceholderTransparency;
						Image placeholder = this.sigPlaceholder.getScaledInstance(
								this.sigScreenWidth, this.sigScreenHeight, Image.SCALE_SMOOTH);

						//Make placeholder transparent
						this.sigPlaceholderScaled = new BufferedImage(this.sigScreenWidth, this.sigScreenHeight, BufferedImage.TYPE_INT_ARGB);
						Graphics g_phs = this.sigPlaceholderScaled.getGraphics();
						g_phs.drawImage(placeholder, 0, 0, null);

						// Draw grey "Signature" overlay
						String overlay = Messages.getString("positioning.signature"); //$NON-NLS-1$
						// Voodoo to get overlay font scale
						float scale = (((float) this.sigScreenWidth) / this.sigPlaceholder.getWidth(null)) * 150;
						g_phs.setFont(getFont().deriveFont(scale));
						g_phs.setColor(this.sigPlaceholderBorderColor);
						Rectangle2D overlay_size = g_phs.getFontMetrics().getStringBounds(overlay, g_phs);
						int width = (int) overlay_size.getWidth();
						int height = (int) overlay_size.getHeight();
						int x = this.sigScreenWidth / 2 - width / 2;
						int y = this.sigScreenHeight / 2 - height / 2 + g_phs.getFontMetrics().getAscent();
						g_phs.drawString(overlay, x, y);

						g_phs.dispose();
						int[] phpixels = new int[this.sigScreenWidth * this.sigScreenHeight];
						phpixels = this.sigPlaceholderScaled.getRGB(0, 0, this.sigScreenWidth, this.sigScreenHeight, phpixels, 0, this.sigScreenWidth);
						for (int i = 0; i < phpixels.length; ++i) {
							Color c = new Color(phpixels[i]);
							c = new Color(c.getRed(), c.getGreen(), c.getBlue(), this.sigPlaceholderTransparency);
							phpixels[i] = c.getRGB();
						}
						this.sigPlaceholderScaled.setRGB(0, 0, this.sigScreenWidth, this.sigScreenHeight, phpixels, 0, this.sigScreenWidth);
					}
					g.drawImage(this.sigPlaceholderScaled, sigX, sigY, null);
					g.setColor(this.sigPlaceholderBorderColor);
					g.drawRect(sigX, sigY, this.sigScreenWidth-1, this.sigScreenHeight-1);
				}

			} else {
				// the image is bogus. try again, or give up.
				if (this.currentPage != null) {
					showPage(this.currentPage);
				}
				g.setColor(Color.black);
				g.drawString(Messages.getString("error.SignaturePanel.NoRender"), getWidth() / 2 - 30, //$NON-NLS-1$
						getHeight() / 2);
			}
		}
	}

	/**
	 * Handles notification of the fact that some part of the image changed.
	 * Repaints that portion.
	 * 
	 * @return true if more updates are desired.
	 */
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		if ((infoflags & (SOMEBITS | ALLBITS)) != 0) {
			repaint(x + this.offX, y + this.offY, width, height);
		}
		return ((infoflags & (ALLBITS | ERROR | ABORT)) == 0);
	}

	private MouseAdapter mouseListener = new MouseAdapter() {

		private boolean doDrag = false;
		private int dragXOffset = 0;
		private int dragYOffset = 0;

		private void updateSigPos(int sigx, int sigy) {
			SignaturePanel.this.updateSigPos(
					sigx - SignaturePanel.this.offX,
					sigy - SignaturePanel.this.offY);
		}

		/** Handles a mouseMoved event */
		@Override
		public void mouseMoved(MouseEvent evt) {
			try {
				boolean onSig = isOnSignature(evt.getX(), evt.getY());
				setCursor(onSig ? Cursors.HAND : Cursors.DEFAULT);
			} catch (NullPointerException e) {
				// do nothing
			}
		}

		/** Handles a mousePressed event */
		@Override
		public void mousePressed(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1)
			{
				this.doDrag = true;
				if (isOnSignature(evt.getX(), evt.getY())) {
					this.dragXOffset = (int)
							(SignaturePanel.this.sigScreenPos.getX() -
							(evt.getX() - SignaturePanel.this.offX));
					this.dragYOffset = (int)
							(SignaturePanel.this.sigScreenPos.getY() -
							(evt.getY() - SignaturePanel.this.offY));
				}
				else {
					this.dragXOffset = 0;
					this.dragYOffset = 0;
				}
				updateSigPos(evt.getX() + this.dragXOffset, evt.getY() + this.dragYOffset);
				setCursor(Cursors.MOVE);
			}
		}

		/** Handles a mouseReleased event */
		@Override
		public void mouseReleased(MouseEvent evt) {
			this.doDrag = false;
			boolean onSig = isOnSignature(evt.getX(), evt.getY());
			setCursor(onSig ? Cursors.HAND : Cursors.DEFAULT);
		}

		/**
		 * Handles a mouseDragged event.
		 */
		@Override
		public void mouseDragged(MouseEvent evt) {
			if (this.doDrag)
				updateSigPos(evt.getX() + this.dragXOffset, evt.getY() + this.dragYOffset);
		}
	};

	/**
	 * Sets the mouse cursor
	 * @param cursor cursor to set
	 */
	void setCursor(Cursors cursor)
	{
		if (this.currentCursor == cursor)
			return;
		this.currentCursor = cursor;
		Cursor cur = null;
		switch (cursor) {
			case DEFAULT:
				cur = this.defaultCursor;
				break;
			case HAND:
				cur = this.handCursor;
				break;
			case MOVE:
				cur = this.moveCursor;
				break;
		}
		this.getParent().setCursor(cur);
	}

	/**
	 * Check whether given point is on signature placeholder
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return true if given point is on signature placeholder
	 */
	boolean isOnSignature(int x, int y)
	{
		Rectangle2D sig = new Rectangle2D.Double(
				this.sigScreenPos.getX() + this.offX,
				this.sigScreenPos.getY() + this.offY,
				this.sigScreenWidth,
				this.sigScreenHeight);
		Point2D pos = new Point2D.Double(x, y);
		return (sig.contains(pos));
	}

	/**
	 * Update the signature placeholder position
	 * @param sigx X position on the document (screen coordinates)
	 * @param sigy Y position on the document (screen coordinates)
	 */
	void updateSigPos(int sigx, int sigy) {
		if (this.currentImage == null)
			return;
		sigx = clamp(sigx, 0, this.currentImage.getWidth(null) - this.sigScreenWidth);
		sigy = clamp(sigy, 0, this.currentImage.getHeight(null) - this.sigScreenHeight);
		this.sigScreenPos = new Point2D.Double(sigx, sigy);
		this.sigPagePos = this.currentXform.transform(this.sigScreenPos, this.sigPagePos);
		repaint();
	}

	/**
	 * Clamp x to be within [min-max]
	 * @param x int to clamp
	 * @param min minimum value
	 * @param max maximum value
	 * @return clamped x
	 */
	static int clamp(int x, int min, int max)
	{
		if (x < min)
			x = min;
		else if (x > max)
			x = max;
		return x;
	}
}
