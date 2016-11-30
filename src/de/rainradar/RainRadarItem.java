package de.rainradar;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author maximilianstrauch
 */
public class RainRadarItem {
    
    private static final String OVERLAY_IMAGE_URL = "http://data.wetter.info/"
            + "data/layers/test-xxlradar-de/test-xxlradar-de_radar_{0}.gif";
    
    private Logger log = Logger.getLogger(getClass().getSimpleName());
    
    private URL url;
    private Date date;
    private BufferedImage image;
    
    public RainRadarItem(Date utcDate) throws MalformedURLException {
        url = new URL(MessageFormat.format(OVERLAY_IMAGE_URL, format(utcDate)));
        date = new Date(utcDate.getTime() + 2 * 60 * 60 * 1000);
    }
    
    public void fetch() throws IOException {
        image = ImageIO.read(url);
    }
    
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RainRadarItem) {
            return ((RainRadarItem) obj).toString().equals(this.toString());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.date);
        return hash;
    }

    @Override
    public String toString() {
        return "RRI@" + format(date);
    }
    
    public String getName() {
        return new SimpleDateFormat("E dd.MM., HH:mm", Locale.GERMANY).format(date);
    }
    
    public static final String format(Date date) {
        return new SimpleDateFormat("yyyyMMddHHmm").format(date);
    }
    
}
