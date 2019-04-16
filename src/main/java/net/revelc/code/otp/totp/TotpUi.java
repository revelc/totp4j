package net.revelc.code.otp.totp;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.revelc.code.otp.totp.TotpGenerator.Totp;

/**
 * A UI for showing TOTP values for a specified key
 */
public class TotpUi {

  public static void main(String[] args) {
    TotpUi otpUi = new TotpUi();
    otpUi.show();
  }

  private static final Font OTP_VALUE_FONT = new Font("arial", Font.BOLD, 160);
  private static final Font OTP_KEY_FONT = new Font("arial", Font.BOLD, 20);
  private JFrame frame;
  private JTextField keyField;
  private Color bgColor = Color.WHITE;

  private String otpString;
  private long nextChangeMillis;

  /** The generator. Starts as null; created when a key is provided */
  private TotpGenerator generator;
  private TotpKey keySource = new UnsecurePersitentTotpKey();

  private Timer valueUpdateTimer = new Timer(100, e -> {
    updateProgress();
  });

  public TotpUi() {
    build();
  }

  private void build() {

    frame = new JFrame("Otp TOTP4j");
    frame.setSize(800, 400);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        String text = keyField.getText();
        if (!text.isBlank()) {
          System.out.println("Saving key");
          keySource.setKey(keyField.getText());
        }
      }
    });

    JPanel keyPanel = createKeyPanel();
    JPanel keyCodePanel = createKeyCodePanel();

    Container parent = frame.getContentPane();
    ((JComponent) parent).setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    parent.setBackground(bgColor);
    parent.setLayout(new BorderLayout());
    parent.add(keyPanel, BorderLayout.NORTH);
    parent.add(keyCodePanel, BorderLayout.CENTER);

    centerFrame();
  }

  private JPanel createKeyPanel() {
    JPanel panel = new JPanel();

    panel.setBackground(bgColor);

    Font font = OTP_KEY_FONT;
    keyField = new JTextField(40) {

      //
      // Overridden to draw 'hint' text
      //
      @Override
      protected void paintComponent(Graphics g) {

        String text = getText();
        if (!isBlank(text)) {
          super.paintComponent(g);
          return;
        }

        Component c = this;
        g.setColor(c.getBackground());
        int w = c.getWidth();
        int h = c.getHeight();
        g.fillRect(0, 0, w, h);

        Graphics2D g2 = (Graphics2D) g;
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = getFont();
        Font italic = f.deriveFont(Font.ITALIC);
        g2.setFont(italic);
        String hint = "Type key...";
        TextLayout layout = new TextLayout(hint, italic, frc);
        Rectangle2D sbounds = layout.getBounds();

        int x = (int) ((w / 2) - (sbounds.getWidth() / 2));
        int y = (int) (h - sbounds.getHeight());
        y = (int) sbounds.getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.drawString(hint, x, y);
      }
    };
    keyField.setFont(font);
    keyField.setHorizontalAlignment(SwingConstants.CENTER);

    Color textColor = Color.LIGHT_GRAY;
    keyField.setForeground(textColor);
    keyField.setBackground(bgColor);
    keyField.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e) {
        updated(e);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        updated(e);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updated(e);
      }

      private void updated(DocumentEvent e) {
        String text = keyField.getText();
        keyUpdated(text);
      }
    });
    CompoundBorder compoundBorder = BorderFactory
        .createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0), keyField.getBorder());
    keyField.setBorder(compoundBorder);
    keyField.setBorder(BorderFactory.createEmptyBorder());
    keyField.setSelectedTextColor(Color.GRAY);

    // sigh, do not select the text when the window gains focus (it looks ugly)
    keyField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        SwingUtilities.invokeLater(() -> keyField.setCaretPosition(keyField.getText().length()));
      }

    });

    panel.setLayout(new BorderLayout());
    panel.add(keyField, BorderLayout.CENTER);

    String key = keySource.getKey();
    if (key != null) {
      System.out.println("Loading saved key");
      keyField.setText(key);
    } else {
      System.out.println("No key provided");
    }

    return panel;
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isBlank();
  }

  private void keyUpdated(String key) {
    if (isBlank(key)) {
      generator = null;
      nextChangeMillis = 0;
      otpString = null;
      frame.repaint();
      valueUpdateTimer.stop();
    } else {
      generator = new TotpGenerator(key);
      valueUpdateTimer.restart();
    }
  }

  private void updateProgress() {

    if (generator == null) {
      return;
    }

    Totp otp = generator.generateTotp();
    String code = otp.getOtp();

    long now = System.currentTimeMillis();
    long nowSeconds = now / 1000;
    int secondsRemaining = otp.getSecondsRemaining();
    long millisRemaining = secondsRemaining * 1000;
    nextChangeMillis = millisRemaining + (nowSeconds * 1000);
    otpString = code;

    frame.repaint();
  }

  private JPanel createKeyCodePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBackground(bgColor);

    JPanel progressPanel = new JPanel(new BorderLayout()) {

      @Override
      protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (otpString == null) {
          return;
        }

        long now = System.currentTimeMillis();
        int h = getHeight();
        int w = getWidth();

        // have the progress bar and as the time expires by subtracting 1 second
        long delta = (nextChangeMillis - 1000) - now;

        double periodSeconds = 30;
        double periodMillis = periodSeconds * 1000;
        double progress = 1 - (delta / periodMillis);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int midx = w / 2;
        int midy = h / 2;
        Font f = OTP_VALUE_FONT;
        g.setFont(f);
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout layout = new TextLayout(otpString, f, frc);
        Rectangle2D sbounds = layout.getBounds();
        float sw = (float) sbounds.getWidth();
        float sh = (float) sbounds.getHeight();

        int x = (int) (midx - (sw / 2));
        int y = (int) (midy + (sh / 2));

        //
        // Drop-shadow
        //
        int n = 10;
        for (int i = n; i > 0; i--) {
          int offset = i;
          int colorDelta = 10 * (n - offset);
          g2.setColor(new Color(255 - colorDelta, 255 - colorDelta, 255 - colorDelta));
          int xoff = x + offset;
          int yoff = y + offset;
          g.drawString(otpString, xoff, yoff);
        }

        //
        // Primary digits
        //
        g.setColor(Color.BLACK);
        g.drawString(otpString, x, y);

        //
        // Seconds remaining
        //
        int buffer = 20; // 'seconds remaining' offset from the primary digits
        int endx = (int) (x + sw + buffer);
        int endy = y;

        g.setColor(Color.LIGHT_GRAY);
        Font secondsFont = new Font("arial", Font.BOLD, 12);
        g2.setFont(secondsFont);

        long millisRemaining = nextChangeMillis - now;
        int secs = (int) (millisRemaining / 1000);
        g2.drawString(Integer.toString(secs) + "s", endx, endy);

        GlyphVector gv = f.createGlyphVector(frc, otpString);
        Shape textShape = gv.getOutline(x, y);
        int textend = x + (int) (sw * progress);
        g2.setColor(Color.PINK);

        //
        // Progress Bar (via the primary digits)
        //
        Shape clip = g2.getClip();
        Rectangle cb = clip.getBounds();
        g2.setClip(cb.x, cb.y, textend, h);

        g2.setColor(new Color(240, 240, 240));
        g2.fill(textShape);
        g2.setColor(Color.LIGHT_GRAY);
        g2.draw(textShape);
      }
    };
    progressPanel.setBackground(bgColor);

    panel.add(progressPanel);

    return panel;
  }

  private void centerFrame() {

    GraphicsDevice defaultDevice =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    GraphicsConfiguration config = defaultDevice.getDefaultConfiguration();

    Rectangle b = config.getBounds();
    Dimension d = frame.getSize();

    // restrict to bounds size
    int userWidth = Math.min(b.width, d.width);
    int userHeigh = Math.min(b.height, d.height);

    int halfScreenWidth = b.width / 2;
    int halfUserWidth = userWidth / 2;
    int halfScreenHeight = b.height / 2;
    int halfUserHeight = userHeigh / 2;
    int widthOffset = halfScreenWidth - halfUserWidth;
    int heightOffset = halfScreenHeight - halfUserHeight;
    int x = b.x + widthOffset;
    int y = b.y + heightOffset;
    Point location = new Point(x, y);
    frame.setLocation(location);
  }

  private void show() {
    frame.setVisible(true);
  }
}
