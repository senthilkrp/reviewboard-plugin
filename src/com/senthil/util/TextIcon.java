package com.senthil.util;

import com.intellij.ide.ui.UISettings;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import java.awt.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Created by spanneer on 1/14/17.
 */
public class TextIcon implements Icon {

  @NotNull
  private final String myText;

  private final int myControlWidth;
  private final int myControlHeight;
  private final Image backgroundImage;

  private int myTextHeight;

  public TextIcon(@NotNull Integer count, @Nullable Image backgroundImage) {
    myText = String.valueOf(count);
    this.backgroundImage = backgroundImage;
    JLabel label = new JLabel("");
    Font font = label.getFont();
    FontMetrics metrics = label.getFontMetrics(font);
    myControlWidth = metrics.stringWidth(myText) + 4;
    myControlHeight = 16;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    UISettings.setupAntialiasing(g);
    g.setFont(new Font("Verdana", Font.PLAIN, 12));
//    if (myTextHeight <= 0) {
//      myTextHeight = g.getFont().createGlyphVector(((Graphics2D)g).getFontRenderContext(), myText).getPixelBounds(null, 0, 0).height;
//    }
    myTextHeight = 12;
    if(backgroundImage != null)
      g.drawImage(backgroundImage, 0, 0, 20, 20, null);
    g.setColor(JBColor.DARK_GRAY);
    g.drawString(myText, x ,  myControlHeight - ((myControlHeight - myTextHeight) / 2));
    g.setColor(JBColor.YELLOW);
  }

  @Override
  public int getIconWidth() {
    return myControlWidth;
  }

  @Override
  public int getIconHeight() {
    return myControlHeight;
  }
}
