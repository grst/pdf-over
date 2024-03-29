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
package at.asit.pdfover.signator;

// Imports
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;

/**
 * 
 */
public class CachedFileNameEmblem implements Emblem {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(CachedFileNameEmblem.class);

	private final String fileDir = System.getProperty("user.home") + File.separator + ".pdf-over"; //$NON-NLS-1$ //$NON-NLS-2$
	private final String imgFileName = ".emblem"; //$NON-NLS-1$
	private final String imgFileExt = "png"; //$NON-NLS-1$
	private final String propFileName = ".emblem.properties"; //$NON-NLS-1$

	private final String imgProp = "IMG"; //$NON-NLS-1$
	private final String hshProp = "HSH"; //$NON-NLS-1$
	private final int maxWidth  = 480;
	private final int maxHeight = 600;

	private String fileName = null;

	/**
	 * Constructor
	 * @param filename
	 */
	public CachedFileNameEmblem(String filename) {
		this.fileName = filename;
	}

	private String getFileHash(String filename) throws IOException {
		InputStream is = Files.newInputStream(Paths.get(this.fileName));
		return DigestUtils.md5Hex(is);
	}

	/**
	 * Correctly rotate JPEG image by EXIF header
	 * @param img the image
	 * @param imgFile the image file
	 * @return the fixed image
	 */
	public static BufferedImage fixImage(BufferedImage img, File imgFile) {
		int oheight = img.getHeight();
		int owidth = img.getWidth();

		// Read EXIF metadata for jpeg
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(imgFile);
			Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);

			while (imageReaders.hasNext()) {
				ImageReader reader = imageReaders.next();
				log.debug(reader.getFormatName());
				if (reader.getFormatName().equals("JPEG")) {
					try {
						Metadata metadata = ImageMetadataReader.readMetadata(imgFile);

						if(metadata != null) {
							int orientation = metadata.getFirstDirectoryOfType(
									ExifIFD0Directory.class).getInt(
									ExifDirectoryBase.TAG_ORIENTATION);
							if (orientation > 2) {
								// rotate
								double rotation = 0;
								int height = owidth;
								int width = oheight;
								switch ((orientation + 1) / 2) {
									case 2:
										rotation = Math.PI;
										height = oheight;
										width = owidth;
										break;
									case 3:
										rotation = Math.PI / 2;
										break;
									case 4:
										rotation = 3 * Math.PI / 2;
										break;
								}
								log.debug("EXIF orientation " + orientation + ", rotating " + rotation + "rad");
								BufferedImage result = new BufferedImage(width, height, img.getType());
								Graphics2D g = result.createGraphics();
								g.translate((width - owidth) / 2, (height - oheight) / 2);
								g.rotate(rotation, owidth / 2, oheight / 2);
								g.drawRenderedImage(img, null);
								g.dispose();
								img = result;
								owidth = width;
								oheight = height;
							}
							if (((orientation < 5) && (orientation % 2 == 0)) ||
									(orientation > 5) && (orientation % 2 == 1)) {
								// flip
								log.debug("flipping image");
								BufferedImage result = new BufferedImage(owidth, oheight, img.getType());
								Graphics2D g = result.createGraphics();
								g.drawImage(img, owidth, 0, -owidth, oheight, null);
								g.dispose();
								img = result;
							}
						}
					} catch (ImageProcessingException e) {
						log.error("Error reading emblem metadata", e);
					} catch (MetadataException e) {
						log.error("Error reading emblem metadata", e);
					} catch (NullPointerException e) {
						log.error("Error reading emblem metadata", e);
					}
				}
			}
		} catch (IOException e) {
			log.error("Error reading image" , e);
		} finally {
			try {
				if (iis != null)
					iis.close();
			} catch (IOException e) {
				log.debug("Error closing stream", e);
			}
		}
		return img;
	}

	private static BufferedImage scaleImage(BufferedImage img, int maxWidth, int maxHeight) {
		int oheight = img.getHeight();
		int owidth = img.getWidth();

		double ratio = (double)owidth/(double)oheight;

		int height = oheight;
		int width = owidth;
		if (height > maxHeight) {
			height = maxHeight;
			width = (int) (maxHeight * ratio);
		}
		if (width > maxWidth) {
			width = maxWidth;
			height = (int) (maxWidth / ratio);
		}
		BufferedImage result = img;
		if (width != owidth || height == oheight) {
			//scale image
			log.debug("Scaling emblem: " + owidth + "x" + oheight + " to " + width + "x" + height); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			result = new BufferedImage(width, height, img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType());
			Graphics2D g = result.createGraphics();
			g.drawImage(img, 0, 0, width, height, null);
			g.dispose();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.Emblem#getFileName()
	 */
	@Override
	public String getFileName() {
		String emblemImg = this.fileName;
		String emblemHsh = null;
		String cachedEmblemFileName = this.fileDir + File.separator + this.imgFileName + "." + this.imgFileExt; //$NON-NLS-1$

		if (emblemImg == null || !(new File(emblemImg).exists()))
			return null;

		Properties emblemProps = new Properties();
		// compare cache, try to load if match
		try {
			InputStream in = new FileInputStream(new File(this.fileDir, this.propFileName));
			emblemProps.load(in);
			if (emblemImg.equals(emblemProps.getProperty(this.imgProp))) {
				emblemHsh = getFileHash(emblemImg);
				if (emblemHsh.equals(emblemProps.getProperty(this.hshProp))) {
					log.debug("Emblem cache hit: " + cachedEmblemFileName); //$NON-NLS-1$
					return cachedEmblemFileName; //$NON-NLS-1$
				}
			}
			log.debug("Emblem cache miss"); //$NON-NLS-1$
		} catch (Exception e) {
			log.warn("Can't load emblem cache", e); //$NON-NLS-1$
		}

		try {
			// create new cache
			if (emblemHsh == null)
				emblemHsh = getFileHash(emblemImg);
			emblemProps.setProperty(this.imgProp, emblemImg);
			emblemProps.setProperty(this.hshProp, emblemHsh);
			File imgFile = new File(emblemImg);

			BufferedImage img = ImageIO.read(imgFile);
			img = fixImage(img, imgFile);
			img = scaleImage(img, this.maxWidth, this.maxHeight);

			File file = new File(this.fileDir, this.imgFileName + "." + this.imgFileExt); //$NON-NLS-1$
			ImageIO.write(img, this.imgFileExt, file); // ignore returned boolean
			OutputStream out = new FileOutputStream(new File(this.fileDir, this.propFileName));
			emblemProps.store(out, null);
		} catch (IOException e) {
			log.error("Can't save emblem cache", e); //$NON-NLS-1$
			return this.fileName;
		}
		return cachedEmblemFileName;
	}

	/**
	 * Return the original filename
	 * @return the original filename
	 */
	public String getOriginalFileName() {
		return this.fileName;
	}

	/**
	 * Return the original filename
	 * @return the original filename
	 */
	public String getOriginalFileHash() {
		if (this.fileName == null || !(new File(this.fileName).exists()))
			return ""; //$NON-NLS-1$
		try {
			return getFileHash(this.fileName);
		} catch (IOException e) {
			log.debug("Error getting file hash", e);
			return ""; //$NON-NLS-1$
		}
	}
}
