
package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import javax.swing.Icon;
import javax.swing.ImageIcon;


public class ImageIconWrapper extends BufferedImage implements Icon {
    ImageIcon backingIcon;
    BufferedImage backingImage;
    
    public ImageIconWrapper(BufferedImage i) {
        super(i.getWidth(), i.getHeight(), i.getType());
        backingImage = i;
        backingIcon = new ImageIcon(i);
    }

    public int getType() {
        return backingImage.getType();
    }

    public ColorModel getColorModel() {
        return backingImage.getColorModel();
    }

    public WritableRaster getRaster() {
        return backingImage.getRaster();
    }

    public WritableRaster getAlphaRaster() {
        return backingImage.getAlphaRaster();
    }

    public int getRGB(int x, int y) {
        return backingImage.getRGB(x, y);
    }

    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        return backingImage.getRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }

    public synchronized void setRGB(int x, int y, int rgb) {
        backingImage.setRGB(x, y, rgb);
    }

    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        backingImage.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }

    public int getWidth() {
        return backingImage.getWidth();
    }

    public int getHeight() {
        return backingImage.getHeight();
    }

    public int getWidth(ImageObserver observer) {
        return backingImage.getWidth(observer);
    }

    public int getHeight(ImageObserver observer) {
        return backingImage.getHeight(observer);
    }

    public ImageProducer getSource() {
        return backingImage.getSource();
    }

    public Object getProperty(String name, ImageObserver observer) {
        return backingImage.getProperty(name, observer);
    }

    public Object getProperty(String name) {
        return backingImage.getProperty(name);
    }

    public Graphics getGraphics() {
        return backingImage.getGraphics();
    }

    public Graphics2D createGraphics() {
        return backingImage.createGraphics();
    }

    public BufferedImage getSubimage(int x, int y, int w, int h) {
        return backingImage.getSubimage(x, y, w, h);
    }

    public boolean isAlphaPremultiplied() {
        return backingImage.isAlphaPremultiplied();
    }

    public void coerceData(boolean isAlphaPremultiplied) {
        backingImage.coerceData(isAlphaPremultiplied);
    }

    public String toString() {
        return backingImage.toString();
    }

    public Vector<RenderedImage> getSources() {
        return backingImage.getSources();
    }

    public String[] getPropertyNames() {
        return backingImage.getPropertyNames();
    }

    public int getMinX() {
        return backingImage.getMinX();
    }

    public int getMinY() {
        return backingImage.getMinY();
    }

    public SampleModel getSampleModel() {
        return backingImage.getSampleModel();
    }

    public int getNumXTiles() {
        return backingImage.getNumXTiles();
    }

    public int getNumYTiles() {
        return backingImage.getNumYTiles();
    }

    public int getMinTileX() {
        return backingImage.getMinTileX();
    }

    public int getMinTileY() {
        return backingImage.getMinTileY();
    }

    public int getTileWidth() {
        return backingImage.getTileWidth();
    }

    public int getTileHeight() {
        return backingImage.getTileHeight();
    }

    public int getTileGridXOffset() {
        return backingImage.getTileGridXOffset();
    }

    public int getTileGridYOffset() {
        return backingImage.getTileGridYOffset();
    }

    public Raster getTile(int tileX, int tileY) {
        return backingImage.getTile(tileX, tileY);
    }

    public Raster getData() {
        return backingImage.getData();
    }

    public Raster getData(Rectangle rect) {
        return backingImage.getData(rect);
    }

    public WritableRaster copyData(WritableRaster outRaster) {
        return backingImage.copyData(outRaster);
    }

    public void setData(Raster r) {
        backingImage.setData(r);
    }

    public void addTileObserver(TileObserver to) {
        backingImage.addTileObserver(to);
    }

    public void removeTileObserver(TileObserver to) {
        backingImage.removeTileObserver(to);
    }

    public boolean isTileWritable(int tileX, int tileY) {
        return backingImage.isTileWritable(tileX, tileY);
    }

    public Point[] getWritableTileIndices() {
        return backingImage.getWritableTileIndices();
    }

    public boolean hasTileWriters() {
        return backingImage.hasTileWriters();
    }

    public WritableRaster getWritableTile(int tileX, int tileY) {
        return backingImage.getWritableTile(tileX, tileY);
    }

    public void releaseWritableTile(int tileX, int tileY) {
        backingImage.releaseWritableTile(tileX, tileY);
    }

    public int getTransparency() {
        return backingImage.getTransparency();
    }

    public Image getScaledInstance(int width, int height, int hints) {
        return backingImage.getScaledInstance(width, height, hints);
    }

    public void flush() {
        backingImage.flush();
    }

    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        return backingImage.getCapabilities(gc);
    }

    public void setAccelerationPriority(float priority) {
        backingImage.setAccelerationPriority(priority);
    }

    public float getAccelerationPriority() {
        return backingImage.getAccelerationPriority();
    }

    public int getImageLoadStatus() {
        return backingIcon.getImageLoadStatus();
    }

    public Image getImage() {
        return backingIcon.getImage();
    }

    public void setImage(Image image) {
        backingIcon.setImage(image);
    }

    public String getDescription() {
        return backingIcon.getDescription();
    }

    public void setDescription(String description) {
        backingIcon.setDescription(description);
    }

    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        backingIcon.paintIcon(c, g, x, y);
    }

    public int getIconWidth() {
        return backingIcon.getIconWidth();
    }

    public int getIconHeight() {
        return backingIcon.getIconHeight();
    }

    public void setImageObserver(ImageObserver observer) {
        backingIcon.setImageObserver(observer);
    }

    public ImageObserver getImageObserver() {
        return backingIcon.getImageObserver();
    }

    public AccessibleContext getAccessibleContext() {
        return backingIcon.getAccessibleContext();
    }

    
}
