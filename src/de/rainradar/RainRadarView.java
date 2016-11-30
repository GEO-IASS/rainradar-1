package de.rainradar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author maximilianstrauch
 */
public class RainRadarView extends JComponent implements RainRadarViewListener, Runnable {

    private static final String[] LEGEND_NAMES = {
        "gering", "leicht", "mäßig", "stark", "sehr stark", "extrem"
    };
    
    private static final Color[] LEGEND_COLORS = {
        new Color(0x8BF289), new Color(0x1DC3C1), new Color(0x505BFF), 
        new Color(0x840475), new Color(0xFFF909), new Color(0xFF0500)
    };
    
    private List<RainRadarItem> items, newItems;
    private RainRadarItem currentItem;
    
    private int frameTime, lastFrameTime, currentIndex = 0;
    
    private Font theFont;
    
    private ScaleMode scaleMode;
    
    public enum ScaleMode {
        ON_WIDTH, ON_HEIGHT;
    }
    
    public RainRadarView() {
        frameTime = RainRadar.getInstance().getInteger("frame-time", 100);
        lastFrameTime = RainRadar.getInstance().getInteger("last-frame-time", 2100);
        
        // Create the font
        theFont = Font.decode(Font.SANS_SERIF);
        theFont = theFont.deriveFont(Font.BOLD);
        
        scaleMode = ScaleMode.ON_HEIGHT;
        
        // New Thread
        new Thread(this).start();
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = Objects.requireNonNull(scaleMode);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth(), height = getHeight();
        
        // Set font and rendering hint for antialias
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        
        // Paint component
        if (items == null || currentItem == null) {
            g.setColor(new Color(0x87de87));
            g.fillRect(0, 0, width, height);
                    
            g.setColor(Color.white);
            String txt = "Lade Daten, bitte warten ...";
            theFont = theFont.deriveFont(42.0f);
            g.setFont(theFont);
            int x = (width - g.getFontMetrics().stringWidth(txt)) / 2;
            int y = (height - g.getFontMetrics().getHeight()) / 2;
            g.drawString(txt, x + 0, y + g.getFontMetrics().getAscent());
            
            /*
            DEBUG ONLY
                        g.setColor(Color.black);
            
            
            theFont = theFont.deriveFont(12.0f);
            g.setFont(theFont);
            g.drawString("items = " + (items == null ? "null" : items.size()) + ", newitems=" + (newItems == null ? "null" : newItems.size()) + 
                        ", current=" + currentItem, 20, 40);
            */
        } else {
            theFont = theFont.deriveFont(21.0f);
            g.setFont(theFont);
            
            // Resources
            RainRadarItem item = currentItem;
            
            int x, y;
            if (scaleMode == ScaleMode.ON_HEIGHT) {
                BufferedImage totalBackground = BackgroundImageProvider.getImage(
                        "map-europe", null, height);
                BufferedImage background1 = BackgroundImageProvider.getImage(
                        "germany-cities", null, height);
                BufferedImage buf = item.getImage();

                y = 0;
                x = (width - ((int) Math.round(totalBackground.getWidth()))) / 2;
                g.drawImage(totalBackground, x, y, null); // Europe background

                // Radar image
                double scale = ((double) height) / ((double) buf.getHeight());
                x = (width - ((int) Math.round(buf.getWidth() * scale))) / 2;
                g.drawImage(buf, x, y, 
                        ((int) Math.round(buf.getWidth() * scale)), 
                        ((int) Math.round(buf.getHeight() * scale)),
                        null);

                // Citiy names
                g.drawImage(background1, x, y, null);
            } else {
                BufferedImage buf = item.getImage();
                double scale = ((double) width) / ((double) buf.getWidth());

                BufferedImage totalBackground = BackgroundImageProvider.getImage(
                        "map-europe", width + (int) Math.round(1558 * scale), null);
                BufferedImage background1 = BackgroundImageProvider.getImage(
                        "germany-cities", width, null);

                y = (height - background1.getHeight()) / 2;
                x = 0;
                // Europe background
                g.drawImage(totalBackground, 
                        -((int) Math.round(1250 * scale) - width/2), y, null);

                // Radar image
                g.drawImage(buf, x, y, 
                        ((int) Math.round(buf.getWidth() * scale)), 
                        ((int) Math.round(buf.getHeight() * scale)),
                        null);
                
                // Citiy names
                g.drawImage(background1, x, y, null);
            }
            
            // Current date
            g.drawString(item.getName(), 20, 20 + g.getFontMetrics().getAscent());
            
            // Ledgend
            g.setFont(theFont.deriveFont(14.0f).deriveFont(Font.PLAIN));
            for (int i = 0; i < LEGEND_NAMES.length; i++) {
                y = i * (g.getFontMetrics().getHeight() + 5) + 60;
                g.setColor(LEGEND_COLORS[i]);
                g.fillRect(25, y, g.getFontMetrics().getAscent(), 
                        g.getFontMetrics().getAscent() + 2);
                g.setColor(Color.BLACK);
                g.drawString(LEGEND_NAMES[i], 
                        25 + g.getFontMetrics().getAscent() + 4, 
                        y + g.getFontMetrics().getAscent());
            }
            
            // Draw progress bar
            g.setColor(new Color(0xf0f0f0));
            g.fillRect(0, 0, width, 5);
            double prog = ((double) currentIndex) / ((double) items.size());
            g.setColor(new Color(0xaaaaaa));
            g.fillRect(0, 0, (int) Math.ceil(width*prog), 5);
        }
        
        String txt = "RainRadar " + RainRadar.VERSION + ", 2014 - 2016. Maximilian Strauch.";
        theFont = theFont.deriveFont(12.0f);
        g.setFont(theFont);
        g.setColor(Color.DARK_GRAY);
        g.drawString(txt, 7, (height - g.getFontMetrics().getAscent()));
    }

    @Override
    public void run() {
        while (true) {
            if (currentIndex == 0 && newItems != null) {
                items = newItems;
                newItems = null;
            }
            
            if (items == null) {
                // Poll
                try {
                    Thread.sleep(100);
                } catch (Exception ex) { /* Silence is gold */ }
                continue;
            }
            
            // Get the current item
            currentItem = items.get(currentIndex++);
            
            // Draw it
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    repaint();
                }
            });
            
            if (currentIndex < items.size()) {
                try {
                    Thread.sleep(frameTime);
                } catch (Exception ex) { /* Silence is gold */ }
            } else {
                try {
                    Thread.sleep(lastFrameTime);
                } catch (Exception ex) { /* Silence is gold */ }
                
                currentIndex = 0; // Reset cycle
            }
        }
    }
    
    @Override
    public void imagesUpdated(RainRadar source) { 
        newItems = source.getImages();
    }
    
}
