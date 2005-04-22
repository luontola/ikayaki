/*
 * SquidFront.java
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

package ikayaki.squid;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyBlue;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Graphical front-end for using the SQUID Interface's protocol level commands.
 *
 * @author Esko Luontola
 */
public class SquidFront extends JFrame {

    private JButton hupdateSettings;
    private JButton hgetStatus;
    private JButton hgetPosition;
    private JButton hgetRotation;
    private JButton hisOK;
    private JButton hmoveToHome;
    private JButton hmoveToDegausserZ;
    private JButton hmoveToDegausserY;
    private JButton hmoveToMeasurement;
    private JButton hmoveToBackground;
    private JButton hmoveToPos;
    private JButton hstop;
    private JButton hrotateTo;
    private JButton hsetOnline;
    private JButton hsetAcceleration;
    private JButton hsetDeceleration;
    private JButton hsetBaseSpeed;
    private JButton hsetVelocity;
    private JButton hsetHoldTime;
    private JButton hsetCrystalFrequence;
    private JButton hstopExecution;
    private JButton hperformSlew;
    private JButton hsetMotorPositive;
    private JButton hsetMotorNegative;
    private JButton hsetSteps;
    private JButton hsetPosition;
    private JButton hgo;
    private JButton hjoin;
    private JButton hverify;
    private JButton hsetPositionRegister;
    private JButton htakeMessage;

    private JButton mupdateSettings;
    private JButton mreset;
    private JButton mresetCounter;
    private JButton mconfigure;
    private JButton mlatchAnalog;
    private JButton mlatchCounter;
    private JButton mgetData;
    private JButton mopenLoop;
    private JButton mclearFlux;
    private JButton mjoin;
    private JButton mreadData;
    private JButton mgetFilters;
    private JButton mgetRange;
    private JButton mgetSlew;
    private JButton mgetLoop;
    private JButton misOK;

    private JButton dupdateSettings;
    private JButton dsetCoil;
    private JButton dsetAmplitude;
    private JButton dexecuteRampUp;
    private JButton dexecuteRampDown;
    private JButton dexecuteRampCycle;
    private JButton ddemagnetizeZ;
    private JButton ddemagnetizeY;
    private JButton dgetRampStatus;
    private JButton dgetRamp;
    private JButton dgetDelay;
    private JButton dgetCoil;
    private JButton dgetAmplitude;
    private JButton disOK;

    private JTextField param1;
    private JTextField param2;
    private JTextField param3;

    private JTextArea handlerLog;
    private JTextArea magnetometerLog;
    private JTextArea degausserLog;

    private JPanel contentPane;

    private Squid squid;

    private JTextField hRawCommand;
    private JButton hRawSend;
    private JTextField dRawCommand;
    private JButton dRawSend;
    private JTextField mRawCommand;
    private JButton mRawSend;

    public SquidFront() throws HeadlessException {
        super("SQUID Front");

        /* Init SQUID interface */
        new Thread() {
            @Override public void run() {
                try {
                    final Squid squid = Squid.instance();       // might take a long time
                    if (!squid.isOK()) {
                        JOptionPane.showMessageDialog(null,
                                "SQUID is not OK!", "Squid error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setSquid(squid);
                        }
                    });

                } catch (IOException e) {
                    // TODO: what should be done now? give error message?
                    //e.printStackTrace();
                    System.err.println("Unable to initialize the SQUID interface.");
                }
            }
        }.start();

        initRawActions();
        initHandlerActions();
        initMagnetometerActions();
        initDegausserActions();
        initLogging();

        setLayout(new BorderLayout());
        setContentPane(contentPane);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Sets the fully initialized Squid interface for the use of the program. Sets the active project the owner of the
     * squid by re-setting the active project.
     *
     * @param squid an instance of the Squid.
     * @throws NullPointerException  if squid is null.
     * @throws IllegalStateException if the squid has already been set.
     */
    public void setSquid(Squid squid) {
        if (squid == null) {
            throw new NullPointerException();
        }
        if (this.squid != null) {
            throw new IllegalStateException();
        }
        this.squid = squid;
    }

    private void initRawActions() {
        hRawSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = hRawCommand.getText().trim().toUpperCase();
                try {
                    squid.getHandler().serialIO.writeMessage(command);
                } catch (SerialIOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        mRawSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = mRawCommand.getText().trim().toUpperCase();
                try {
                    squid.getMagnetometer().serialIO.writeMessage(command+"\r");
                } catch (SerialIOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        dRawSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = dRawCommand.getText().trim().toUpperCase();
                try {
                    squid.getDegausser().serialIO.writeMessage(command+"\r");
                } catch (SerialIOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    /**
     * Sets ActionListeners for handler's control buttons.
     */
    private void initHandlerActions() {
      this.hmoveToHome.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().moveToSampleLoad();
          }
          catch (IOException ex) {
            handlerLog.append("MoveToHome failed\r");
          }
        }
      });
      this.hmoveToHome.getAction().putValue(Action.NAME, "MoveToHome()");

      this.hmoveToDegausserZ.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().moveToDegausserZ();
          }
          catch (IOException ex) {
            handlerLog.append("MoveToDegausserZ failed\r");
          }
        }
      });
      this.hmoveToDegausserZ.getAction().putValue(Action.NAME, "MoveToDegausserZ()");

      this.hmoveToDegausserY.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().moveToDegausserY();
          }
          catch (IOException ex) {
            handlerLog.append("MoveToDegausserY failed\r");
          }
        }
      });
      this.hmoveToDegausserY.getAction().putValue(Action.NAME, "MoveToDegausserY()");

      this.hmoveToBackground.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().moveToBackground();
          }
          catch (IOException ex) {
            handlerLog.append("MoveToBackground failed\r");
          }
        }
      });
      this.hmoveToBackground.getAction().putValue(Action.NAME, "MoveToBackground()");

      this.hmoveToMeasurement.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().moveToMeasurement();
          }
          catch (IOException ex) {
            handlerLog.append("MoveToMeasurement failed\r");
          }
        }
      });
      this.hmoveToMeasurement.getAction().putValue(Action.NAME, "MoveToMeasurement()");

      this.hmoveToPos.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
//          try {
//            handlerLog.append(Squid.instance().getHandler().moveToPos(Integer.parseInt(param1.getText()))+ "\r");
//          }
//          catch (IOException ex) {
//            handlerLog.append("MoveToPos failed\r");
//          }
        }
      });
      this.hmoveToPos.getAction().putValue(Action.NAME, "MoveToPos(int)");

      this.hrotateTo.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().rotateTo(Integer.parseInt(param1.getText()));
          }
          catch (IOException ex) {
            handlerLog.append("rotateTo failed\r");
          }
        }
      });
      this.hrotateTo.getAction().putValue(Action.NAME, "rotateTo(int)");

      this.hsetAcceleration.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().setAcceleration(Integer.parseInt(param1.getText()));
          }
          catch (IOException ex) {
            handlerLog.append("setAcceleration failed\r");
          }
        }
      });
      this.hsetAcceleration.getAction().putValue(Action.NAME, "setAcceleration(int)");

      this.hsetDeceleration.setAction(new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                try {
                  Squid.instance().getHandler().setDeceleration(Integer.parseInt(param1.getText()));
                }
                catch (IOException ex) {
                  handlerLog.append("setDeceleration failed\r");
                }
              }
            });
      this.hsetDeceleration.getAction().putValue(Action.NAME, "setDeceleration(int)");

      this.hsetVelocity.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().setVelocity(Integer.parseInt(param1.getText()));
          }
          catch (IOException ex) {
            handlerLog.append("setVelocity failed\r");
          }
        }
      });
      this.hsetVelocity.getAction().putValue(Action.NAME, "setVelocity(int)");

      this.hsetOnline.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().setOnline();
          }
          catch (IOException ex) {
            handlerLog.append("setOnline failed\r");
          }
        }
      });
      this.hsetOnline.getAction().putValue(Action.NAME, "setOnline()");

      this.hjoin.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().join();
          }
          catch (IOException ex) {
            handlerLog.append("join failed\r");
          } catch (InterruptedException e1) {
            handlerLog.append("join failed\r");
          }
        }
      });
      this.hjoin.getAction().putValue(Action.NAME, "join()");

      this.hverify.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().verify(param1.getText().charAt(0));
          }
          catch (IOException ex) {
            handlerLog.append("verify failed\r");
          }
        }
      });
      this.hverify.getAction().putValue(Action.NAME, "verify(char)");

      this.hstop.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
//          try {
//            Squid.instance().getHandler().stop();
//          }
//          catch (IOException ex) {
//            handlerLog.append("stop failed\r");
//          }
        }
      });
      this.hstop.getAction().putValue(Action.NAME, "stop()");

      this.hsetMotorNegative.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().setMotorNegative();
          }
          catch (IOException ex) {
            handlerLog.append("setMotorNegative failed\r");
          }
        }
      });
      this.hsetMotorNegative.getAction().putValue(Action.NAME, "setMotorNegative()");

      this.hsetMotorPositive.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getHandler().setMotorPositive();
          }
          catch (IOException ex) {
            handlerLog.append("setMotorPositive failed\r");
          }
        }
      });
      this.hsetMotorPositive.getAction().putValue(Action.NAME, "setMotorPositive()");

      this.hsetSteps.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
//          try {
//            Squid.instance().getHandler().setSteps(Integer.parseInt(param1.getText()));
//          }
//          catch (IOException ex) {
//            handlerLog.append("setSteps failed\r");
//          }
        }
      });
      this.hsetSteps.getAction().putValue(Action.NAME, "setSteps(int steps)");

      this.hmoveToPos.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
//          try {
//            Squid.instance().getHandler().moveToPos(Integer.parseInt(param1.getText()));
//          }
//          catch (IOException ex) {
//            handlerLog.append("MoveToPos failed\r");
//          }
        }
      });
      this.hmoveToPos.getAction().putValue(Action.NAME, "moveToPos(int position)");

      this.hgetPosition.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            handlerLog.append(Squid.instance().getHandler().getPosition() + "\r");
          }
          catch (IOException ex) {
            handlerLog.append("getPosition failed\r");
          }
        }
      });
      this.hgetPosition.getAction().putValue(Action.NAME, "getPosition()");

      this.hgetRotation.setVisible(false);
      this.hgetStatus.setVisible(false);
      this.hisOK.setVisible(false);
      this.hperformSlew.setVisible(false);
      this.hsetBaseSpeed.setVisible(false);
      this.hsetCrystalFrequence.setVisible(false);
      this.hsetHoldTime.setVisible(false);
      this.hsetPosition.setVisible(false);
      this.hsetPositionRegister.setVisible(false);
      this.hstopExecution.setVisible(false);
      this.htakeMessage.setVisible(false);
      this.hupdateSettings.setVisible(false);

    }

    /**
     * Sets ActionListeners for magnetometer's control buttons.
     */
    private void initMagnetometerActions() {
      this.mclearFlux.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().clearFlux(param1.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("clearFlux failed\r");
        }
      }
    });
    this.mclearFlux.getAction().putValue(Action.NAME, "clearFlux(char)");

    this.mconfigure.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().configure(param1.getText().charAt(0),param2.getText().charAt(0),param3.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("configure failed\r");
        }
      }
    });
    this.mconfigure.getAction().putValue(Action.NAME, "configure(char,char,char)");

    this.mgetData.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          magnetometerLog.append(Squid.instance().getMagnetometer().getData(param1.getText().charAt(0),param2.getText().charAt(0),param3.getText()) + "\r");
        }
        catch (IOException ex) {
          magnetometerLog.append("getDAta failed\r");
        }
      }
    });
    this.mgetData.getAction().putValue(Action.NAME, "getData(char,char,String)");

    this.mgetLoop.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          magnetometerLog.append(Squid.instance().getMagnetometer().getLoop()+ "\r");
        }
        catch (IOException ex) {
          magnetometerLog.append("getLoop failed\r");
        }
      }
    });
    this.mgetLoop.getAction().putValue(Action.NAME, "getLoop)");

    this.mgetRange.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          magnetometerLog.append(Squid.instance().getMagnetometer().getRange()+ "\r");
        }
        catch (IOException ex) {
          magnetometerLog.append("getRange failed\r");
        }
      }
    });
    this.mgetRange.getAction().putValue(Action.NAME, "getRange)");

    this.mgetSlew.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          magnetometerLog.append(Squid.instance().getMagnetometer().getSlew()+ "\r");
        }
        catch (IOException ex) {
          magnetometerLog.append("getSlew failed\r");
        }
      }
    });
    this.mgetSlew.getAction().putValue(Action.NAME, "getSlew()");

    this.misOK.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          magnetometerLog.append(Squid.instance().getMagnetometer().isOK()+ "\r");
        }
        catch (IOException ex) {
          magnetometerLog.append("isOK failed\r");
        }
      }
    });
    this.misOK.getAction().putValue(Action.NAME, "isOK()");

    this.mjoin.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().join();
        }
        catch (IOException ex) {
          magnetometerLog.append("join failed\r");
        }
      }
    });
    this.mjoin.getAction().putValue(Action.NAME, "join()");

    this.mlatchAnalog.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().latchAnalog(param1.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("latchAnalog failed\r");
        }
      }
    });
    this.mlatchAnalog.getAction().putValue(Action.NAME, "latchAnalog(char axis)");

    this.mlatchCounter.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().latchCounter(param1.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("latchCounter failed\r");
        }
      }
    });
    this.mlatchCounter.getAction().putValue(Action.NAME, "latchCounter(char axis)");

    this.mopenLoop.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().openLoop(param1.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("openLoop failed\r");
        }
      }
    });
    this.mopenLoop.getAction().putValue(Action.NAME, "openLoop(char axis)");

    this.mreadData.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Double[] result = Squid.instance().getMagnetometer().readData();
          magnetometerLog.append("{" + result[0] + "," +result[1] + "," + result[2] + "}\r");

        }
        catch (IOException ex) {
          magnetometerLog.append("openLoop failed\r");
        }
      }
    });
    this.mreadData.getAction().putValue(Action.NAME, "readData()");

    this.mgetFilters.setVisible(false);

    this.mreset.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().reset(param1.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("reset failed\r");
        }
      }
    });
    this.mreset.getAction().putValue(Action.NAME, "reset(char axis)");

    this.mresetCounter.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().resetCounter(param1.getText().charAt(0));
        }
        catch (IOException ex) {
          magnetometerLog.append("resetCounter failed\r");
        }
      }
    });
    this.mresetCounter.getAction().putValue(Action.NAME, "resetCounter(char axis)");

    this.mupdateSettings.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Squid.instance().getMagnetometer().updateSettings();
        }
        catch (IOException ex) {
          magnetometerLog.append("updateSettings failed\r");
        }
      }
    });
    this.mupdateSettings.getAction().putValue(Action.NAME, "updateSettings()");

    }

    /**
     * Sets ActionListeners for degausser's control buttons.
     */
    private void initDegausserActions() {
      this.dupdateSettings.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getDegausser().updateSettings();
          }
          catch (IOException ex) {
            degausserLog.append("updateSettings failed\r");
          }
        }
      });
      this.dupdateSettings.getAction().putValue(Action.NAME, "updateSettings()");

      this.ddemagnetizeY.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getDegausser().demagnetizeY(Double.parseDouble(param1.getText()));
          }
          catch (IOException ex) {
            degausserLog.append("demagnetizeY failed\r");
          }
        }
      });
      this.ddemagnetizeY.getAction().putValue(Action.NAME, "demagnetizeY(int amplitude)");

      this.ddemagnetizeZ.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getDegausser().demagnetizeZ(Double.parseDouble(param1.getText()));
          }
          catch (IOException ex) {
            degausserLog.append("demagnetizeZ failed\r");
          }
        }
      });
      this.ddemagnetizeZ.getAction().putValue(Action.NAME, "demagnetizeY(int amplitude)");

      this.dexecuteRampCycle.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getDegausser().executeRampCycle();
          }
          catch (IOException ex) {
            degausserLog.append("demagnetizeZ failed\r");
          }
        }
      });
      this.dexecuteRampCycle.getAction().putValue(Action.NAME, "executeRampCycle()");

      this.dsetAmplitude.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getDegausser().setAmplitude(Integer.parseInt(param1.getText()));
          }
          catch (IOException ex) {
            degausserLog.append("demagnetizeZ failed\r");
          }
        }
      });
      this.dsetAmplitude.getAction().putValue(Action.NAME, "setAmplitude(int)");

      this.dsetCoil.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            Squid.instance().getDegausser().setCoil(param1.getText().charAt(0));
          }
          catch (IOException ex) {
            degausserLog.append("demagnetizeZ failed\r");
          }
        }
      });
      this.dsetCoil.getAction().putValue(Action.NAME, "setCoil(char axis)");

      this.dgetAmplitude.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
            degausserLog.append(Squid.instance().getDegausser().getAmplitude() + "\r");
          }
          catch (IOException ex) {
            degausserLog.append("get failed\r");
          }
        }
      });
      this.dgetAmplitude.getAction().putValue(Action.NAME, "getAmplitude()");

      this.dgetCoil.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
             degausserLog.append(Squid.instance().getDegausser().getCoil() + "\r");
          }
          catch (IOException ex) {
            degausserLog.append("get failed\r");
          }
        }
      });
      this.dgetCoil.getAction().putValue(Action.NAME, "getCoil()");

      this.dgetRamp.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
             degausserLog.append("" + Squid.instance().getDegausser().getRamp() + "\r");
          }
          catch (IOException ex) {
            degausserLog.append("getRamp failed\r");
          }
        }
      });
      this.dgetRamp.getAction().putValue(Action.NAME, "getRamp()");

      this.dgetDelay.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
             degausserLog.append(Squid.instance().getDegausser().getDelay() + "\r");
          }
          catch (IOException ex) {
            degausserLog.append("getDelay failed\r");
          }
        }
      });
      this.dgetDelay.getAction().putValue(Action.NAME, "getDelay()");

      this.dgetRampStatus.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          try {
             degausserLog.append(Squid.instance().getDegausser().getRampStatus() + "\r");
          }
          catch (IOException ex) {
            degausserLog.append("getRampStatus failed\r");
          }
        }
      });
      this.dgetRampStatus.getAction().putValue(Action.NAME, "getRampStatus()");

    }

    /**
     * Sets anything that is needed for logging to file and to screen.
     */
    private void initLogging() {
        // no need
    }

    public static void main(String[] args) {
        PlasticLookAndFeel.setMyCurrentTheme(new SkyBlue());
        try {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println(e);
        }

        new SquidFront();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer !!! IMPORTANT !!! DO NOT edit this method OR call it in your
     * code!
     */
    private void $$$setupUI$$$() {
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label1 = new JLabel();
        label1.setText("Degausser");
        panel2.add(label1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(14, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        dupdateSettings = new JButton();
        dupdateSettings.setHorizontalAlignment(2);
        dupdateSettings.setText("updateSettings():void");
        panel3.add(dupdateSettings,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dsetCoil = new JButton();
        dsetCoil.setHorizontalAlignment(2);
        dsetCoil.setText("setCoil(char coil):void");
        panel3.add(dsetCoil,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dsetAmplitude = new JButton();
        dsetAmplitude.setHorizontalAlignment(2);
        dsetAmplitude.setText("setAmplitude(int amplitude):void");
        panel3.add(dsetAmplitude,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dexecuteRampUp = new JButton();
        dexecuteRampUp.setHorizontalAlignment(2);
        dexecuteRampUp.setText("executeRampUp():void");
        panel3.add(dexecuteRampUp,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dexecuteRampDown = new JButton();
        dexecuteRampDown.setHorizontalAlignment(2);
        dexecuteRampDown.setText("executeRampDown():void");
        panel3.add(dexecuteRampDown,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dexecuteRampCycle = new JButton();
        dexecuteRampCycle.setHorizontalAlignment(2);
        dexecuteRampCycle.setText("executeRampCycle():void");
        panel3.add(dexecuteRampCycle,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        ddemagnetizeZ = new JButton();
        ddemagnetizeZ.setHorizontalAlignment(2);
        ddemagnetizeZ.setText("demagnetizeZ(int amplitude):boolean");
        panel3.add(ddemagnetizeZ,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        ddemagnetizeY = new JButton();
        ddemagnetizeY.setHorizontalAlignment(2);
        ddemagnetizeY.setText("demagnetizeY(int amplitude):boolean");
        panel3.add(ddemagnetizeY,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetRampStatus = new JButton();
        dgetRampStatus.setHorizontalAlignment(2);
        dgetRampStatus.setText("getRampStatus():char");
        panel3.add(dgetRampStatus,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetRamp = new JButton();
        dgetRamp.setHorizontalAlignment(2);
        dgetRamp.setText("getRamp():int");
        panel3.add(dgetRamp,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetDelay = new JButton();
        dgetDelay.setHorizontalAlignment(2);
        dgetDelay.setText("getDelay():int");
        panel3.add(dgetDelay,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetCoil = new JButton();
        dgetCoil.setEnabled(true);
        dgetCoil.setHorizontalAlignment(2);
        dgetCoil.setText("getCoil():char");
        panel3.add(dgetCoil,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetAmplitude = new JButton();
        dgetAmplitude.setHorizontalAlignment(2);
        dgetAmplitude.setText("getAmplitude():int");
        panel3.add(dgetAmplitude,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        disOK = new JButton();
        disOK.setHorizontalAlignment(2);
        disOK.setText("isOK():boolean");
        panel3.add(disOK,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4,
                new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        param1 = new JTextField();
        panel4.add(param1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        param2 = new JTextField();
        panel4.add(param2,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        final JLabel label2 = new JLabel();
        label2.setText("param2");
        panel4.add(label2,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("param1");
        panel4.add(label3,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        param3 = new JTextField();
        panel4.add(param3,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        final JLabel label4 = new JLabel();
        label4.setText("param3");
        panel4.add(label4,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2,
                new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(30);
        scrollPane1.setVerticalScrollBarPolicy(22);
        panel1.add(scrollPane1,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        magnetometerLog = new JTextArea();
        magnetometerLog.setRows(6);
        scrollPane1.setViewportView(magnetometerLog);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label5 = new JLabel();
        label5.setText("Magnetometer");
        panel5.add(label5,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(16, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        mupdateSettings = new JButton();
        mupdateSettings.setHorizontalAlignment(2);
        mupdateSettings.setText("updateSettings():void");
        panel6.add(mupdateSettings,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mreset = new JButton();
        mreset.setHorizontalAlignment(2);
        mreset.setText("reset(char axis):void");
        panel6.add(mreset,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mresetCounter = new JButton();
        mresetCounter.setHorizontalAlignment(2);
        mresetCounter.setText("resetCounter(char axis):void");
        panel6.add(mresetCounter,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mconfigure = new JButton();
        mconfigure.setHorizontalAlignment(2);
        mconfigure.setText("configure(char axis, char subcommand, char option):void");
        panel6.add(mconfigure,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mlatchAnalog = new JButton();
        mlatchAnalog.setHorizontalAlignment(2);
        mlatchAnalog.setText("latchAnalog(char axis):void");
        panel6.add(mlatchAnalog,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mlatchCounter = new JButton();
        mlatchCounter.setHorizontalAlignment(2);
        mlatchCounter.setText("latchCounter(char axis):void");
        panel6.add(mlatchCounter,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetData = new JButton();
        mgetData.setHorizontalAlignment(2);
        mgetData.setText("getData(char axis, char command, String datavalues):String");
        panel6.add(mgetData,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mopenLoop = new JButton();
        mopenLoop.setHorizontalAlignment(2);
        mopenLoop.setText("openLoop(char axis):void");
        panel6.add(mopenLoop,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mclearFlux = new JButton();
        mclearFlux.setHorizontalAlignment(2);
        mclearFlux.setText("clearFlux(char axis):void");
        panel6.add(mclearFlux,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mjoin = new JButton();
        mjoin.setHorizontalAlignment(2);
        mjoin.setText("join():void");
        panel6.add(mjoin,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mreadData = new JButton();
        mreadData.setHorizontalAlignment(2);
        mreadData.setText("readData():Double[3]");
        panel6.add(mreadData,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetFilters = new JButton();
        mgetFilters.setHorizontalAlignment(2);
        mgetFilters.setText("getFilters():char[3]");
        panel6.add(mgetFilters,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetRange = new JButton();
        mgetRange.setHorizontalAlignment(2);
        mgetRange.setText("getRange():char[3]");
        panel6.add(mgetRange,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetSlew = new JButton();
        mgetSlew.setHorizontalAlignment(2);
        mgetSlew.setText("getSlew():boolean[3]");
        panel6.add(mgetSlew,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetLoop = new JButton();
        mgetLoop.setHorizontalAlignment(2);
        mgetLoop.setText("getLoop():boolean[3]");
        panel6.add(mgetLoop,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        misOK = new JButton();
        misOK.setHorizontalAlignment(2);
        misOK.setText("isOK():boolean");
        panel6.add(misOK,
                new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setVerticalScrollBarPolicy(22);
        panel1.add(scrollPane2,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        handlerLog = new JTextArea();
        handlerLog.setRows(6);
        scrollPane2.setViewportView(handlerLog);
        final JScrollPane scrollPane3 = new JScrollPane();
        scrollPane3.setHorizontalScrollBarPolicy(30);
        scrollPane3.setVerticalScrollBarPolicy(22);
        panel1.add(scrollPane3,
                new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        degausserLog = new JTextArea();
        degausserLog.setRows(6);
        scrollPane3.setViewportView(degausserLog);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel7,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label6 = new JLabel();
        label6.setText("Handler");
        panel7.add(label6,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(16, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        hupdateSettings = new JButton();
        hupdateSettings.setHorizontalAlignment(2);
        hupdateSettings.setHorizontalTextPosition(11);
        hupdateSettings.setText("updateSettings():void");
        hupdateSettings.setVerticalTextPosition(0);
        panel8.add(hupdateSettings,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgetStatus = new JButton();
        hgetStatus.setHorizontalAlignment(2);
        hgetStatus.setText("getStatus():char");
        panel8.add(hgetStatus,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgetPosition = new JButton();
        hgetPosition.setHorizontalAlignment(2);
        hgetPosition.setText("getPosition():int");
        panel8.add(hgetPosition,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgetRotation = new JButton();
        hgetRotation.setHorizontalAlignment(2);
        hgetRotation.setText("getRotation():int");
        panel8.add(hgetRotation,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hisOK = new JButton();
        hisOK.setHorizontalAlignment(2);
        hisOK.setText("isOK():boolean");
        panel8.add(hisOK,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToHome = new JButton();
        hmoveToHome.setHorizontalAlignment(2);
        hmoveToHome.setText("moveToHome():void");
        panel8.add(hmoveToHome,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToDegausserZ = new JButton();
        hmoveToDegausserZ.setHorizontalAlignment(2);
        hmoveToDegausserZ.setText("moveToDegausserZ():void");
        panel8.add(hmoveToDegausserZ,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToDegausserY = new JButton();
        hmoveToDegausserY.setHorizontalAlignment(2);
        hmoveToDegausserY.setText("moveToDegausserY():void");
        panel8.add(hmoveToDegausserY,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToMeasurement = new JButton();
        hmoveToMeasurement.setHorizontalAlignment(2);
        hmoveToMeasurement.setText("moveToMeasurement():void");
        panel8.add(hmoveToMeasurement,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToBackground = new JButton();
        hmoveToBackground.setHorizontalAlignment(2);
        hmoveToBackground.setText("moveToBackground():void");
        panel8.add(hmoveToBackground,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToPos = new JButton();
        hmoveToPos.setHorizontalAlignment(2);
        hmoveToPos.setText("moveToPos(int pos):boolean");
        panel8.add(hmoveToPos,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hstop = new JButton();
        hstop.setHorizontalAlignment(2);
        hstop.setText("stop():void");
        panel8.add(hstop,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hrotateTo = new JButton();
        hrotateTo.setHorizontalAlignment(2);
        hrotateTo.setText("rotateTo(int angle):void");
        panel8.add(hrotateTo,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetOnline = new JButton();
        hsetOnline.setHorizontalAlignment(2);
        hsetOnline.setText("setOnline():void");
        panel8.add(hsetOnline,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetAcceleration = new JButton();
        hsetAcceleration.setHorizontalAlignment(2);
        hsetAcceleration.setText("setAcceleration(int a):void");
        panel8.add(hsetAcceleration,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetDeceleration = new JButton();
        hsetDeceleration.setHorizontalAlignment(2);
        hsetDeceleration.setText("setDeceleration(int d):void");
        panel8.add(hsetDeceleration,
                new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetBaseSpeed = new JButton();
        hsetBaseSpeed.setHorizontalAlignment(2);
        hsetBaseSpeed.setText("setBaseSpeed(int b):void");
        panel8.add(hsetBaseSpeed,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetVelocity = new JButton();
        hsetVelocity.setHorizontalAlignment(2);
        hsetVelocity.setText("setVelocity(int v):void");
        panel8.add(hsetVelocity,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetHoldTime = new JButton();
        hsetHoldTime.setHorizontalAlignment(2);
        hsetHoldTime.setText("setHoldTime(int h):void");
        panel8.add(hsetHoldTime,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetCrystalFrequence = new JButton();
        hsetCrystalFrequence.setHorizontalAlignment(2);
        hsetCrystalFrequence.setText("setCrystalFrequence(int cf):void");
        panel8.add(hsetCrystalFrequence,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hstopExecution = new JButton();
        hstopExecution.setHorizontalAlignment(2);
        hstopExecution.setText("stopExecution():void");
        panel8.add(hstopExecution,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hperformSlew = new JButton();
        hperformSlew.setHorizontalAlignment(2);
        hperformSlew.setText("performSlew():void");
        panel8.add(hperformSlew,
                new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetMotorPositive = new JButton();
        hsetMotorPositive.setHorizontalAlignment(2);
        hsetMotorPositive.setText("setMotorPositive():void");
        panel8.add(hsetMotorPositive,
                new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetMotorNegative = new JButton();
        hsetMotorNegative.setHorizontalAlignment(2);
        hsetMotorNegative.setText("setMotorNegative():void");
        panel8.add(hsetMotorNegative,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetSteps = new JButton();
        hsetSteps.setHorizontalAlignment(2);
        hsetSteps.setText("setSteps(int s):void");
        panel8.add(hsetSteps,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetPosition = new JButton();
        hsetPosition.setHorizontalAlignment(2);
        hsetPosition.setText("setPosition(int p):void");
        panel8.add(hsetPosition,
                new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgo = new JButton();
        hgo.setHorizontalAlignment(2);
        hgo.setText("go():void");
        panel8.add(hgo,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hjoin = new JButton();
        hjoin.setHorizontalAlignment(2);
        hjoin.setText("join():void");
        panel8.add(hjoin,
                new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hverify = new JButton();
        hverify.setHorizontalAlignment(2);
        hverify.setText("verify(char v):String");
        panel8.add(hverify,
                new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetPositionRegister = new JButton();
        hsetPositionRegister.setHorizontalAlignment(2);
        hsetPositionRegister.setText("setPositionRegister(int r):void");
        panel8.add(hsetPositionRegister,
                new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        htakeMessage = new JButton();
        htakeMessage.setHorizontalAlignment(2);
        htakeMessage.setText("takeMessage():char");
        panel8.add(htakeMessage,
                new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer4 = new Spacer();
        panel7.add(spacer4,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(contentPane,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        hRawCommand = new JTextField();
        contentPane.add(hRawCommand,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        hRawSend = new JButton();
        hRawSend.setText("Send");
        contentPane.add(hRawSend,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer5 = new Spacer();
        contentPane.add(spacer5,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel9,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        mRawCommand = new JTextField();
        panel9.add(mRawCommand,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        mRawSend = new JButton();
        mRawSend.setText("Send");
        panel9.add(mRawSend,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer6 = new Spacer();
        panel9.add(spacer6,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel10,
                new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        dRawCommand = new JTextField();
        panel10.add(dRawCommand,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        dRawSend = new JButton();
        dRawSend.setText("Send");
        panel10.add(dRawSend,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer7 = new Spacer();
        panel10.add(spacer7,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
    }
}
