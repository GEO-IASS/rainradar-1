package de.rainradar;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author maximilianstrauch
 */
public class RainRadarFrame extends JFrame {
    
    private final RainRadarView view;
    
    public RainRadarFrame() {
        super("RainRadar");
        
        // Radar view
        view = new RainRadarView();
        add(view, BorderLayout.CENTER);
        
        // Exit button
        JPanel glassPane = new JPanel(new BorderLayout());
        glassPane.setOpaque(false);
        glassPane.add(HBox.create()
                .addGlue()
                .add(new JButton(new AbstractAction("Beenden") {
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                }))
                .setEmptyBorder(7), BorderLayout.SOUTH);
        if (!RainRadar.getInstance().getBoolean("hide-close", false)) {
            setGlassPane(glassPane);
            getGlassPane().setVisible(true);   
        }
        
        // Screen size
        setSize(
                RainRadar.getInstance().getInteger("screen-width", 800),
                RainRadar.getInstance().getInteger("screen-height", 600)
        );   
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public RainRadarView getView() {
        return view;
    }
    
    public void display() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (RainRadar.getInstance().getBoolean("fullscreen", false)) {
                    System.out.println("Trying fullscreen mode ...");
                    
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice device = ge.getDefaultScreenDevice();
 
                    if(!device.isFullScreenSupported()){
                        System.err.println("  Fullscreen not supported! Fallback to "
                                + "DIY fullscreen ...");
                        setUndecorated(true);
                        setSize(
                                ge.getMaximumWindowBounds().width,
                                ge.getMaximumWindowBounds().height
                        );
                        setResizable(false);
                        setAlwaysOnTop(true);
                        setLocation(0, 0);
                        setVisible(true);
                        return;
                    }
                    
                    setVisible(true);
                    device.setFullScreenWindow(RainRadarFrame.this);
                    return;
                }
                setVisible(true);
            }
        });
    }
    
}
