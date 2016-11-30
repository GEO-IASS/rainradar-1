package de.rainradar;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 *
 * @author maximilianstrauch
 */
public class BackgroundImageProvider {

    private static final Map<String, BufferedImage> ORIGINALS = new HashMap<>();
    
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();
    
    public static final BufferedImage getImage(String name, Integer prefWidth, Integer prefHeight) {
        String fileName = name + "_" + prefWidth + "_" + prefHeight;
        if (IMAGE_CACHE.containsKey(fileName)) {
            return IMAGE_CACHE.get(fileName);
        }
        
        if (IMAGE_CACHE.size() > 42) {
            IMAGE_CACHE.clear();
        }
        
        String path = "/de/rainradar/resources/" + name + ".png";
        try {
            BufferedImage image;
            if (ORIGINALS.containsKey(name)) {
                image = ORIGINALS.get(name);
            } else {
                image = ImageIO.read(BackgroundImageProvider.class.getResource(path));
                ORIGINALS.put(name, image);
            }
            
            System.out.println("Image " + path + " for " + prefWidth + " " + prefHeight + "...");
            
            double scale;
            if (prefHeight != null) {
                scale = ((double) prefHeight) / ((double) image.getHeight());
            } else if (prefWidth != null) {
                scale = ((double) prefWidth) / ((double) image.getWidth());
            } else {
                throw new NullPointerException("Only one null argument allowed");
            }
            
            BufferedImage buf = resizeToBig(image, 
                    (int) Math.round(image.getWidth()*scale), 
                    (int) Math.round(image.getHeight()*scale)
            );
            
            
            IMAGE_CACHE.put(fileName, buf);
            return buf;
        } catch (Exception ex) {
            System.err.println("Image " + path + " not found!");
            return null;
        }
    }
    
    /**
     * we want the x and o to be resized when the JFrame is resized
     *
     * @see http://stackoverflow.com/a/11371387/2429611
     * @param originalImage an x or an o. Use cross or oh fields.
     * @param biggerWidth
     * @param biggerHeight
     */
    private static final BufferedImage resizeToBig(Image originalImage, int biggerWidth, int biggerHeight) {
       int type = BufferedImage.TYPE_INT_ARGB;


       BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
       Graphics2D g = resizedImage.createGraphics();

       g.setComposite(AlphaComposite.Src);
       g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
       g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

       g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, null);
       g.dispose();


       return resizedImage;
    }
    
}
