import com.install4j.api.styles.WrapperStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A sample style that wraps another style with a sunny sky background.
 * To nest another user-selectable style it is easiest to extend from WrapperStyle which
 * handles the necessary delegations.
 */
public class SunnySkyBackgroundStyle extends WrapperStyle {

    private static final double SQRT_2 = Math.sqrt(2);

    private String nestedStyleId = "";
    private Insets insets = new Insets(40, 40, 40, 40);
    private int numberOfRays = 20;
    private Color sunColor = new Color(255, 230, 117);
    private Color skyColor = new Color(112, 185, 255);

    @Override
    public String getNestedStyleId() {
        return nestedStyleId;
    }

    public void setNestedStyleId(String nestedStyleId) {
        this.nestedStyleId = nestedStyleId;
    }

    public Insets getInsets() {
        return insets;
    }

    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    public Color getSunColor() {
        return sunColor;
    }

    public void setSunColor(Color sunColor) {
        this.sunColor = sunColor;
    }

    public int getNumberOfRays() {
        return numberOfRays;
    }

    public void setNumberOfRays(int numberOfRays) {
        this.numberOfRays = numberOfRays;
    }

    public Color getSkyColor() {
        return skyColor;
    }

    public void setSkyColor(Color skyColor) {
        this.skyColor = skyColor;
    }

    @Override
    protected JComponent createComponent(JComponent styleComponent) {
        return new WrapperPanel(styleComponent);
    }

    private class WrapperPanel extends JPanel {

        public WrapperPanel(JComponent styleComponent) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(insets));
            setBackground(skyColor);
            // The styleComponent can be null if no style was selected or if the selected style does not exist anymore
            add(styleComponent != null ? styleComponent : createErrorComponent(), BorderLayout.CENTER);
        }

        private JComponent createErrorComponent() {
            return new JLabel("Error, style with ID " + nestedStyleId + " does not exist");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); // paints background
            Graphics2D g = (Graphics2D)graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(sunColor);

            int width = getWidth();
            int height = getHeight();

            // draw sun rays
            int stretchedWidth = (int)Math.round(SQRT_2 * width) + 2;
            int stretchedHeight = (int)Math.round(SQRT_2 * height) + 2;
            int x = (width - stretchedWidth) / 2;
            int y = (height - stretchedHeight) / 2;
            int arcSize = 360 / (2 * numberOfRays);

            for (int i = 0; i < numberOfRays; i++) {
                g.fillArc(x, y, stretchedWidth, stretchedHeight, 2 * i * arcSize, arcSize);
            }

            // draw sun disk
            int squareSize = Math.min(width, height) / 4;
            g.fillOval((width - squareSize) / 2, (height - squareSize) / 2, squareSize, squareSize);
        }
    }
}
