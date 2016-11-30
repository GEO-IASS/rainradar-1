package de.rainradar;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author maximilianstrauch
 */
public class RainRadar extends TimerTask {
    
    public static final String VERSION = "0.42";
    
    private static RainRadar thisInstance;
    
    private final CyclicQueue<RainRadarItem> images;
    private final Timer timer;
    private final List<RainRadarViewListener> listeners;
    
    private final Map<String, Object> settings;
    
    public RainRadar() {
        images = new CyclicQueue<>(1);
        timer = new Timer();
        listeners = new ArrayList<>();
        thisInstance = this;
        settings = new HashMap<>();
    }
    
    private void start() {
        images.setSize(getInteger("image-count", 21));
        timer.scheduleAtFixedRate(this, 0, 120000);
    }
    
    @Override
    public void run() {
        try {
            _run();
        } catch (Exception ex) {
            System.err.println("General image fetch error: " + ex);
        }
    }
    
    public void _run() throws MalformedURLException {
        int quater = images.getSize();
        while (quater >= 0) {
            Date queryDate = now(quater--);
            
            // Get the image and add it to the queue
            RainRadarItem item = new RainRadarItem(queryDate);
            
            if (images.contains(item)) {
                continue;
            }
            
            try {
                item.fetch();
            } catch (Exception ex) {
                System.err.println("Image for " + item + " not exisiting, "
                        + "but should ... more luck next time ...");
                continue;
            }
            
            images.add(item);
        }
        
        for (RainRadarItem rainRadarItem : images) {
            System.out.print(rainRadarItem.getName() + " -> ");
        }
        
        // Queue populated
        for (RainRadarViewListener rainRadarViewListener : listeners) {
            rainRadarViewListener.imagesUpdated(this);
        }
    }
    
    public List<RainRadarItem> getImages() {
        List<RainRadarItem> items = new ArrayList<>();
        for (RainRadarItem rainRadarItem : images) {
            items.add(rainRadarItem);
        }
        return items;
    }
    
    public void addListener(RainRadarViewListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }
    
    private Date now(int minusQuater) {
        Calendar c = Calendar.getInstance(Locale.GERMANY);
        long utcTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000;
        c.setTime(new Date(utcTime));
        utcTime -= c.get(Calendar.SECOND) * 1000;
        utcTime -= (c.get(Calendar.MINUTE) % 15) * 60 * 1000;
        utcTime -= minusQuater * 900000;
        return new Date(utcTime);
    }
    
    public int getInteger(String key, int defaultValue) {
        if (!settings.containsKey(key)) {
            System.err.println("Property " + key + " not set");
            System.err.println("  ... falling back to " + defaultValue);
            settings.put(key, defaultValue);
        }
        
        return (Integer) settings.get(key);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        if (!settings.containsKey(key)) {
            System.err.println("Property " + key + " not set");
            System.err.println("  ... falling back to " + defaultValue);
            settings.put(key, defaultValue);
        }
        
        return (Boolean) settings.get(key);
    }
    
    public static final RainRadar getInstance() {
        return thisInstance;
    }
    
    private static void parseArgs(String[] args) throws IllegalArgumentException {
        int i = 0;
        while (true && args.length > 0) {
            if (i < args.length && args[i].startsWith("--")) {
                String key = args[i].substring(2);
                
                if (!(i+1 < args.length)) {
                    // throw new IllegalArgumentException("No value for " + key + ".");
                    // Assuming boolean
                    getInstance().settings.put(key, true);
                    i++;
                } else {
                    String value = args[i+1];
                    Object valObj = null;
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                        valObj = "true".equalsIgnoreCase(value);
                    } else {
                        try {
                            valObj = Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Cannot parse argument of " + key + ".");
                        }
                    }

                    getInstance().settings.put(key, valObj);
                    i += 2;
                }
                
                if (i >= args.length) {
                    return;
                }
            } else {
                throw new IllegalArgumentException("Illegal command line arguments.");
            }
        }
    }
    
    public static final void man(String msg) {
        if (msg != null) {
            System.out.println("ERROR: " + msg);
            System.out.println("");
        }
        
        System.out.println("RainRadar " + VERSION + ", 2014. Maximilian Strauch.");
        System.out.println("");
        System.out.println("Usage: java -jar rainradar.jar <arguments>");
        System.out.println("");
        System.out.println("Arguments:");
        System.out.println("  --frame-time <ms>\t\tTime in ms a radar image is displayed.");
        System.out.println("  --image-count    \t\tNumber of radar images to play.");
        System.out.println("  --last-frame-time <ms>\tTime in ms the last radar image is displayed.");
        System.out.println("  --fullscreen      \t\tIf set, the window is expanded to fullscreen.");
        System.out.println("  --fullscreen-no-close\t\tIf set, the window is expanded to fullscreen and the close button is hidden.");
        System.out.println("  --screen-width <px>\t\tWidth of the window in px.");
        System.out.println("  --screen-height <px>\t\tHeight of the window in px.");
        System.out.println("  --scale-on-height   \t\tScales the view to always 100% height (default).");
        System.out.println("  --scale-on-width    \t\tScales the view to always 100% width.");
        
        System.out.println("");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        // IMPORTANT
        System.setProperty("user.timezone", "Europe/Berlin");
        
        if (args.length == 1 && "--help".equals(args[0])) {
            man(null);
        }
        
        // Create new main instance
        RainRadar radar = new RainRadar();
        
        // Parse commandline
        try {
            parseArgs(args);
        } catch (IllegalArgumentException ex) {
            man(ex.getMessage());
        }
        
        if (radar.getBoolean("fullscreen-no-close", false)) {
            getInstance().settings.put("fullscreen", true);
            getInstance().settings.put("hide-close", true);
            
        }
        
        // Start timer
        radar.start();
        
        // Open GUI
        RainRadarFrame frame = new RainRadarFrame();
        frame.display();
        
        // Apply listener
        radar.addListener(frame.getView());
        if (getInstance().getBoolean("scale-on-width", false)) {
            System.out.println("Scale on width set");
            frame.getView().setScaleMode(RainRadarView.ScaleMode.ON_WIDTH);
        } else {
            System.out.println("Scale on height set");
            frame.getView().setScaleMode(RainRadarView.ScaleMode.ON_HEIGHT);
        }
        
        // Ready for fun!
    }

}
