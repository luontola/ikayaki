package jutil;

import java.awt.*;
import java.io.*;

/**
 * A Windows®-specific Java utility class.
 *
 * @author Pat Paternostro, Tri-Com Consulting Group L.L.C.
 * @version 1.0
 */
public final class JUtil {
    /**
     * The drive type cannot be determined.
     */
    public static final int DRIVE_UNKNOWN = 0;

    /**
     * The root directory does not exist.
     */
    public static final int DRIVE_NO_ROOT_DIR = 1;

    /**
     * The disk can be removed from the drive.
     */
    public static final int DRIVE_REMOVABLE = 2;

    /**
     * The disk cannot be removed from the drive.
     */
    public static final int DRIVE_FIXED = 3;

    /**
     * The drive is a remote (network) drive.
     */
    public static final int DRIVE_REMOTE = 4;

    /**
     * The drive is a CD-ROM drive.
     */
    public static final int DRIVE_CDROM = 5;

    /**
     * The drive is a RAM disk.
     */
    public static final int DRIVE_RAMDISK = 6;


    static {
        System.loadLibrary("jutil");
    }

    /* Make the constructor private so the class cannot be instantiated */
    private JUtil() {
    }

    /**
     * Retrieves the character typed at the command console.
     *
     * @return the character typed at the command console
     */
    public static native char getConsoleChar();

    /**
     * Retrieves the system's logical drives.
     *
     * @return an array of <CODE>String</CODE> objects that contains the logical drive names in the form of
     *         <CODE><B>x:\</B></CODE> where <CODE><B>x</B></CODE> denotes the drive letter
     */
    public static native String[] getLogicalDrives();

    /**
     * Retrieves the specified drive's free disk space.
     *
     * @param drive a <CODE>String</CODE> that specifies the root directory of the disk to return free disk space
     *              information about. If <CODE>drive</CODE> is <CODE>null</CODE> the method uses the root of the
     *              current directory.
     * @return the free disk space in bytes
     * @see #getLogicalDrives
     */
    public static native long getFreeDiskSpace(String drive);

    /**
     * Retrieves the specified drive's type.
     *
     * @param drive a <CODE>String</CODE> that specifies the root directory of the disk to return the drive type
     *              information about. If <CODE>drive</CODE> is <CODE>null</CODE> the method uses the root of the
     *              current directory.
     * @return an <CODE>int</CODE> that specifies the drive type
     * @see #getLogicalDrives
     * @see #DRIVE_UNKNOWN
     * @see #DRIVE_NO_ROOT_DIR
     * @see #DRIVE_REMOVABLE
     * @see #DRIVE_FIXED
     * @see #DRIVE_REMOTE
     * @see #DRIVE_CDROM
     * @see #DRIVE_RAMDISK
     */
    public static native int getDriveType(String drive);

    /**
     * Retrieves the specified volume's label.
     *
     * @param drive a <CODE>String</CODE> that specifies the root directory of the disk to return the volume label. If
     *              <CODE>drive</CODE> is <CODE>null</CODE> the method uses the root of the current directory.
     * @return a <CODE>String</CODE> containing the specified volume's label
     * @see #getLogicalDrives
     */
    public static native String getVolumeLabel(String drive);

    /**
     * Sets the specified volume's label.
     *
     * @param drive a <CODE>String</CODE> that specifies the root directory of the disk to set the volume label. If
     *              <CODE>drive</CODE> is <CODE>null</CODE> the method uses the root of the current directory.
     * @param label a <CODE>String</CODE> that represents the volume's label name. The maximum number of characters for
     *              a volume label is 11. If <CODE>label</CODE> is more that 11 characters long the volume's label will
     *              not be set. If <CODE>label</CODE> is <CODE>null</CODE> the specified volume's label is deleted.
     * @return a <CODE>boolean</CODE> specifying whether the volume's label was set. <CODE><B>true</B></CODE> if the
     *         volume's label was set, <CODE><B>false</B></CODE> otherwise.
     * @see #getLogicalDrives
     */
    public static native boolean setVolumeLabel(String drive, String label);

    /**
     * Retrieves the current directory.
     *
     * @return a <CODE>String</CODE> representing the current directory
     */
    public static native String getCurrentDirectory();

    /**
     * Sets the current directory.
     *
     * @param directory a <CODE>String</CODE> that specifies the current directory to set
     * @return a <CODE>boolean</CODE> indicating if the current directory was set. <CODE><B>true</B></CODE> if the
     *         current directory was set, <CODE><B>false</B></CODE> otherwise.
     */
    public static native boolean setCurrentDirectory(String directory);

    /**
     * Retrieves the Win32® window handle for the specified window title.
     *
     * @param title the title of the window whose Win32® window handle to retrieve
     * @return an <CODE>int</CODE> representing the Win32® window handle for the specified window title
     */
    public static native int getHwnd(String title);

    /**
     * Minimizes the specified window.
     *
     * @param hwnd the window's Win32® handle
     * @see #getHwnd
     */
    public static native void setWindowMinimized(int hwnd);

    /**
     * Maximizes the specified window.
     *
     * @param hwnd the window's Win32® handle
     * @see #getHwnd
     */
    public static native void setWindowMaximized(int hwnd);

    /**
     * Restores the specified window.
     *
     * @param hwnd the window's Win32® handle
     * @see #getHwnd
     */
    public static native void setWindowRestored(int hwnd);

    /**
     * Enables/disables the specified window's <CODE><B>Restore</B></CODE> button.
     *
     * @param hwnd the window's Win32® handle
     * @param flag a <CODE>boolean</CODE> parameter that specifies whether to enable or disable the window's
     *             <CODE><B>Restore</B></CODE> button. <CODE><B>true</B></CODE> enables the button,
     *             <CODE><B>false</B></CODE> disables the button.
     * @see #getHwnd
     */
    public static native void setWindowRestoreEnabled(int hwnd, boolean flag);

    /**
     * Enables/disables the specified window's <CODE><B>Move</B></CODE> system menu item.
     *
     * @param hwnd the window's Win32® handle
     * @param flag a <CODE>boolean</CODE> parameter that specifies whether to enable or disable the window's
     *             <CODE><B>Move</B></CODE> system menu item. <CODE><B>true</B></CODE> enables the menu item,
     *             <CODE><B>false</B></CODE> disables the menu item.
     * @see #getHwnd
     */
    public static native void setWindowMoveEnabled(int hwnd, boolean flag);

    /**
     * Enables/disables the specified window's <CODE><B>Size</B></CODE> system menu item.
     *
     * @param hwnd the window's Win32® handle
     * @param flag a <CODE>boolean</CODE> parameter that specifies whether to enable or disable the window's
     *             <CODE><B>Size</B></CODE> system menu item. <CODE><B>true</B></CODE> enables the menu item,
     *             <CODE><B>false</B></CODE> disables the menu item.
     * @see #getHwnd
     */
    public static native void setWindowSizeEnabled(int hwnd, boolean flag);

    /**
     * Enables/disables the specified window's <CODE><B>Minimize</B></CODE> button.
     *
     * @param hwnd the window's Win32® handle
     * @param flag a <CODE>boolean</CODE> parameter that specifies whether to enable or disable the window's
     *             <CODE><B>Minimize</B></CODE> button. <CODE><B>true</B></CODE> enables the button,
     *             <CODE><B>false</B></CODE> disables the button.
     * @see #getHwnd
     */
    public static native void setWindowMinimizeEnabled(int hwnd, boolean flag);

    /**
     * Enables/disables the specified window's <CODE><B>Maximize</B></CODE> button.
     *
     * @param hwnd the window's Win32® handle
     * @param flag a <CODE>boolean</CODE> parameter that specifies whether to enable or disable the window's
     *             <CODE><B>Maximize</B></CODE> button. <CODE><B>true</B></CODE> enables the button,
     *             <CODE><B>false</B></CODE> disables the button.
     * @see #getHwnd
     */
    public static native void setWindowMaximizeEnabled(int hwnd, boolean flag);

    /**
     * Sets the specified window as the topmost window in the z-order.
     *
     * @param hwnd the window's Win32® handle
     * @param flag a <CODE>boolean</CODE> parameter that specifies whether the window will be the topmost window in the
     *             z-order. <CODE><B>true</B></CODE> sets the window as the topmost window in the z-order,
     *             <CODE><B>false</B></CODE> sets the window behind all topmost windows.
     * @see #getHwnd
     */
    public static native void setWindowAlwaysOnTop(int hwnd, boolean flag);

    /**
     * Sets the specified container's components to the specified font.
     *
     * @param cont the <CODE>Container</CODE> whose components will be set with the specified font
     * @param f    the <CODE>Font</CODE> object that will be used
     * @throws IllegalArgumentException if <CODE>cont</CODE> or <CODE>f</CODE> are <CODE>null</CODE>
     * @see java.awt.Container
     * @see java.awt.Font
     */
    public static void setContainerDefaultFont(Container cont, Font f) throws IllegalArgumentException {
        /* Check for illegal arguments */
        if (cont == null || f == null) {
            throw new IllegalArgumentException("This method cannot accept null arguments!");
        }

        /* Get the container's components */
        Component[] comp = cont.getComponents();

        /* Cycle through the components and set the font */
        for (int i = 0; i < comp.length; i++) {
            comp[i].setFont(f);
        }
    }

    /**
     * Sets the specified menubar's menus and menu items to the specified font.
     *
     * @param mb the <CODE>MenuBar</CODE> whose menus and menu items will be set with the specified font
     * @param f  the <CODE>Font</CODE> object that will be used
     * @throws IllegalArgumentException if <CODE>mb</CODE> or <CODE>f</CODE> are <CODE>null</CODE>
     * @see java.awt.MenuBar
     * @see java.awt.Font
     */
    public static void setMenuBarDefaultFont(MenuBar mb, Font f) throws IllegalArgumentException {
        int menuCount = 0, menuItemCount = 0;

        /* Check for legal arguments */
        if (mb == null || f == null) {
            throw new IllegalArgumentException("This method cannot accept null arguments!");
        }

        /* Get the menubar's menu count */
        menuCount = mb.getMenuCount();

        /* Cycle through the menus */
        for (int i = 0; i < menuCount; i++) {
            /* Set the menu's font */
            mb.getMenu(i).setFont(f);

            /* Get the menu's menu item count */
            menuItemCount = mb.getMenu(i).getItemCount();

            /* Cycle through the menu items and set the font */
            for (int j = 0; j < menuItemCount; j++) {
                mb.getMenu(i).getItem(j).setFont(f);
            }
        }
    }

    /**
     * Copies the specified source file to the specified destination file.
     *
     * @param source the source file name
     * @param dest   the destination file name
     * @throws IllegalArgumentException if the <CODE>source</CODE> or <CODE>dest</CODE> file names are null or empty
     * @throws FileNotFoundException    if the source file name does not exist
     * @throws IOException              if a problem was encountered reading from the source file or writing to the
     *                                  destination file
     */
    public static void copyFile(String source, String dest) throws IllegalArgumentException, FileNotFoundException, IOException {
        FileWriter fw = null;
        FileReader fr = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        File fileSource = null;

        /* Check for illegal arguments */
        if ((source == null || source.equals("")) || ((dest == null) || dest.equals(""))) {
            throw new IllegalArgumentException("This method cannot accept null or empty (\"\") String arguments!");
        }

        try {
            fr = new FileReader(source);
            fw = new FileWriter(dest);
            br = new BufferedReader(fr);
            bw = new BufferedWriter(fw);

            /* Determine the size of the buffer to allocate */
            fileSource = new File(source);

            /* Array length must be an int so cast the source file's length */
            int fileLength = (int) fileSource.length();

            char charBuff[] = new char[fileLength];

            while (br.read(charBuff, 0, fileLength) != -1) {
                bw.write(charBuff, 0, fileLength);
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ioe) {
            }
        }
    }
}
