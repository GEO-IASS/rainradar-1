
package de.rainradar;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author maximilianstrauch
 */
public class HBox extends Box {
    
    private HBox() {
        super(BoxLayout.X_AXIS);
        setOpaque(false);
    }

    @Override
    public HBox add(Component c) {
        super.add(c);
        return this;
    }
    
    public HBox add(JComponent c) {
        super.add(c);
        return this;
    }
    
    public HBox addAll(JComponent...cs) {
        for (JComponent c : cs) {
            super.add(c);
        }
        return this;
    }
    
    public HBox addAll(Component...cs) {
        for (Component c : cs) {
            super.add(c);
        }
        return this;
    }
    
    public HBox addText(String text) {
        super.add(new JLabel(text));
        return this;
    }
    
    public HBox addSpacer() {
        return addSpacer(7);
    }
    
    public HBox addSpacer(int width) {
        super.add(Box.createHorizontalStrut(width));
        return this;
    }
    
    public HBox addGlue() {
        super.add(Box.createHorizontalGlue());
        return this;
    }
    
    public HBox setEmptyBorder(int size) {
        super.setBorder(new EmptyBorder(size, size, size, size));
        return this;
    }
    
    public static HBox create() {
        return new HBox();
    }
         
}
