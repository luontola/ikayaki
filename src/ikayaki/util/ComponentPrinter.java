/*
 * ComponentPrinter.java
 *
 * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
 *
 * This file is part of Ikayaki.
 *
 * Ikayaki is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Ikayaki is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ikayaki; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ikayaki.util;

import java.awt.*;
import javax.swing.*;
import java.awt.print.*;

/**
 * Offers methods to print Components (only for PrintPanel actually)
 *
 * @author Aki Korpua
 */
public class ComponentPrinter implements Printable {

    /**
     * Component to be printed
     */
    private Component componentToBePrinted;

    /**
     * plots height
     */
    private final int plotHeight = 200;


  /**
   * Creates new printable "component"
   *
   * @param componentToBePrinted Component
   */
  public ComponentPrinter(Component componentToBePrinted) {
    this.componentToBePrinted = componentToBePrinted;
  }

  /**
   * Static printing command
   *
   * @param c Component to be printed (use Only PrintPanel)
   */
  public static void printComponent(Component c) {
  new ComponentPrinter(c).print();
}

    /**
     * Opens printer dialog and start printing job if we get printer
     */
    public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable(this);
    if (printJob.printDialog())
      try {
        printJob.print();
      } catch(PrinterException pe) {
        System.out.println("Error printing: " + pe);
      }
  }

  /**
   * Absolutely chaotic printing mechanism. Spilts component in pages and prevents
   * last 400 pixels on last page to split awfully (we only use this for PrintPanel
   * and last 400 pixels are Plots, so DONT use this in any other component printing :)
   *
   * @param g Graphics
   * @param pageFormat PageFormat
   * @param pageIndex int
   * @return int
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
      Dimension dim = componentToBePrinted.getSize();
      if (dim.getHeight() < (pageIndex * 695))
          return (NO_SUCH_PAGE);
      Graphics2D g2d = (Graphics2D) g;
      if (dim.getHeight() - (695 * (pageIndex)) < plotHeight) {
          g2d.translate(pageFormat.getImageableX(),
                        pageFormat.getImageableY() - 695 * pageIndex+(plotHeight-(dim.getHeight() - (695 * (pageIndex)))));
      }
      else {
          g2d.translate(pageFormat.getImageableX(),
                        pageFormat.getImageableY() - 695 * pageIndex);
      }
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      if (dim.getHeight() - (695 * (pageIndex+1)) < plotHeight && dim.getHeight() - (695 * (pageIndex+1)) >0) {
          System.err.println("lets print page " + (pageIndex+1));
          g2d.setColor(Color.white);
          g2d.fillRect(0,695 * (pageIndex+1)-(plotHeight - (int)dim.getHeight() + (695 * (pageIndex+1))),500,700);
      }
      enableDoubleBuffering(componentToBePrinted);
      return (PAGE_EXISTS);
  }


  public static void disableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  public static void enableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
}
