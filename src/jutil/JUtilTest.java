package jutil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JUtilTest {
    public static void main(String args[]) {
        char consoleChar = 0;

        new JUtilFrame("JUtilTest Application");
        while (consoleChar != 'X') {
            consoleChar = JUtil.getConsoleChar();
            System.out.println("You pressed the " + consoleChar + " key!");
        }
    }
}

class JUtilFrame extends Frame implements ActionListener {
    Panel panel1 = new Panel();
    Panel panel2 = new Panel();
    Panel panel3 = new Panel();
    Panel panel4 = new Panel();
    Panel panel5 = new Panel();
    Panel panel6 = new Panel();
    Panel panel7 = new Panel();
    Panel panel8 = new Panel();
    Panel panel9 = new Panel();
    Panel panel10 = new Panel();
    Panel panel11 = new Panel();
    Button disableMenuItems = new Button("Disable System Menu Items");
    Button enableMenuItems = new Button("Enable System Menu Items");
    Button minimize = new Button("Minimize");
    Button maximize = new Button("Maximize");
    Button restore = new Button("Restore");
    Button freeSpace = new Button("Get Free Disk Space");
    Button getVolume = new Button("Get Volume Label");
    Button setVolume = new Button("Set Volume Label");
    Button driveType = new Button("Get Drive Type");
    Button topmost = new Button("Set Topmost");
    Button notopmost = new Button("Unset Topmost");
    Button setCurrentDir = new Button("Set Current Directory");
    Button getCurrentDir = new Button("Get Current Directory");
    Button copyFile = new Button("Copy File");
    Choice driveList = new Choice();
    TextField tfDriveType = new TextField(20);
    TextField tfGetVolumeLabel = new TextField(20);
    TextField tfSetVolumeLabel = new TextField(20);
    TextField tfFreeSpace = new TextField(20);
    TextField tfSetCurrentDir = new TextField(20);
    TextField tfGetCurrentDir = new TextField(20);
    TextField tfSourceFile = new TextField(10);
    TextField tfDestFile = new TextField(10);
    MenuBar mb = new MenuBar();
    Menu file = new Menu("File");
    MenuItem exit = new MenuItem("Exit");

    JUtilFrame(String title) {
        super(title);

        /* Set the layout mananger */
        setLayout(new GridLayout(11, 1));

        /* Add the window listener */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                dispose();
                System.exit(0);
            }
        });

        /* Build and set the menubar */
        file.add(exit);
        mb.add(file);
        setMenuBar(mb);

        /* Add the action listeners */
        exit.addActionListener(this);
        disableMenuItems.addActionListener(this);
        enableMenuItems.addActionListener(this);
        minimize.addActionListener(this);
        maximize.addActionListener(this);
        restore.addActionListener(this);
        freeSpace.addActionListener(this);
        getVolume.addActionListener(this);
        setVolume.addActionListener(this);
        driveType.addActionListener(this);
        topmost.addActionListener(this);
        notopmost.addActionListener(this);
        getCurrentDir.addActionListener(this);
        setCurrentDir.addActionListener(this);
        copyFile.addActionListener(this);

        /* Disable components */
        tfDriveType.setEnabled(false);
        tfFreeSpace.setEnabled(false);
        tfGetVolumeLabel.setEnabled(false);

        /* Build the drive list */
        String drives[] = JUtil.getLogicalDrives();
        for (int i = 0; i < drives.length; i++) {
            driveList.addItem(drives[i]);
        }

        /* Add the components to the panels */
        panel1.add(new Label("Drive List:"));
        panel1.add(driveList);
        panel2.add(driveType);
        panel2.add(tfDriveType);
        panel3.add(freeSpace);
        panel3.add(tfFreeSpace);
        panel4.add(setVolume);
        panel4.add(tfSetVolumeLabel);
        panel5.add(getVolume);
        panel5.add(tfGetVolumeLabel);
        panel6.add(getCurrentDir);
        panel6.add(tfGetCurrentDir);
        panel7.add(setCurrentDir);
        panel7.add(tfSetCurrentDir);
        panel8.add(copyFile);
        panel8.add(new Label("Source:"));
        panel8.add(tfSourceFile);
        panel8.add(new Label("Destination:"));
        panel8.add(tfDestFile);
        panel9.add(topmost);
        panel9.add(notopmost);
        panel10.add(minimize);
        panel10.add(maximize);
        panel10.add(restore);
        panel11.add(disableMenuItems);
        panel11.add(enableMenuItems);

        /* Add the panels to the frame */
        add(panel1);
        add(panel2);
        add(panel3);
        add(panel4);
        add(panel5);
        add(panel6);
        add(panel7);
        add(panel8);
        add(panel9);
        add(panel10);
        add(panel11);

        /* Set the default font for the container and the menubar */
        JUtil.setContainerDefaultFont(this, new Font("Arial", Font.BOLD | Font.ITALIC, 16));
        JUtil.setMenuBarDefaultFont(mb, new Font("Arial", Font.BOLD | Font.ITALIC, 16));

        /* Size the frame */
        pack();

        /* Center the frame */
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameDim = getSize();
        setLocation((screenDim.width - frameDim.width) / 2, (screenDim.height - frameDim.height) / 2);

        /* Display the frame */
        setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        Object obj = evt.getSource();

        if (obj == exit) {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else if (obj == copyFile) {
            try {
                JUtil.copyFile(tfSourceFile.getText(), tfDestFile.getText());
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        } else if (obj == disableMenuItems) {
            JUtil.setWindowMinimizeEnabled(JUtil.getHwnd(getTitle()), false);
            JUtil.setWindowMaximizeEnabled(JUtil.getHwnd(getTitle()), false);
            JUtil.setWindowMoveEnabled(JUtil.getHwnd(getTitle()), false);
            JUtil.setWindowRestoreEnabled(JUtil.getHwnd(getTitle()), false);
            JUtil.setWindowSizeEnabled(JUtil.getHwnd(getTitle()), false);
        } else if (obj == enableMenuItems) {
            JUtil.setWindowMinimizeEnabled(JUtil.getHwnd(getTitle()), true);
            JUtil.setWindowMaximizeEnabled(JUtil.getHwnd(getTitle()), true);
            JUtil.setWindowMoveEnabled(JUtil.getHwnd(getTitle()), true);
            JUtil.setWindowRestoreEnabled(JUtil.getHwnd(getTitle()), true);
            JUtil.setWindowSizeEnabled(JUtil.getHwnd(getTitle()), true);
        } else if (obj == minimize) {
            JUtil.setWindowMinimized(JUtil.getHwnd(getTitle()));
        } else if (obj == maximize) {
            JUtil.setWindowMaximized(JUtil.getHwnd(getTitle()));
        } else if (obj == restore) {
            JUtil.setWindowRestored(JUtil.getHwnd(getTitle()));
        } else if (obj == freeSpace) {
            tfFreeSpace.setText(Long.toString(JUtil.getFreeDiskSpace(driveList.getSelectedItem())));
        } else if (obj == topmost) {
            JUtil.setWindowAlwaysOnTop(JUtil.getHwnd(getTitle()), true);
        } else if (obj == notopmost) {
            JUtil.setWindowAlwaysOnTop(JUtil.getHwnd(getTitle()), false);
        } else if (obj == setVolume) {
            JUtil.setVolumeLabel(driveList.getSelectedItem(), tfSetVolumeLabel.getText());
        } else if (obj == getVolume) {
            tfGetVolumeLabel.setText(JUtil.getVolumeLabel(driveList.getSelectedItem()));
        } else if (obj == getCurrentDir) {
            tfGetCurrentDir.setText(JUtil.getCurrentDirectory());
        } else if (obj == setCurrentDir) {
            JUtil.setCurrentDirectory(tfSetCurrentDir.getText());
        } else if (obj == driveType) {
            switch (JUtil.getDriveType(driveList.getSelectedItem())) {
            case JUtil.DRIVE_UNKNOWN:
                tfDriveType.setText("Unknown drive type!");
                break;
            case JUtil.DRIVE_NO_ROOT_DIR:
                tfDriveType.setText("No root directory!");
                break;
            case JUtil.DRIVE_REMOVABLE:
                tfDriveType.setText("Removable drive!");
                break;
            case JUtil.DRIVE_FIXED:
                tfDriveType.setText("Unremovable drive!");
                break;
            case JUtil.DRIVE_REMOTE:
                tfDriveType.setText("Remote (network) drive!");
                break;
            case JUtil.DRIVE_CDROM:
                tfDriveType.setText("CD-ROM drive!");
                break;
            case JUtil.DRIVE_RAMDISK:
                tfDriveType.setText("RAM disk drive!");
                break;
            }
        }
    }
}
