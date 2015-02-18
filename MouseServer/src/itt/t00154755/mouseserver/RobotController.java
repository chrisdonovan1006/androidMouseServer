package itt.t00154755.mouseserver;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import static java.lang.Thread.sleep;

/**
 * @author Christopher Donovan
 * @author RobotController.java
 * @version 2.0
 *          <p/>
 *          This class controls the mapping of the accelerometer readings
 *          to the cursor on the screen. It's also controls the moveClicked()
 *          and KeyPressed() method of the robot object.
 * @since 10/02/2015
 */
public class RobotController extends ServerUtils implements Runnable {

    public static final int EXIT = 0;
    // sensor movement direction
    public static final int RIGHTDOWN = 1;
    public static final int LEFTUP = 2;
    public static final int RIGHTUP = 3;
    public static final int LEFTDOWN = 4;

    public static final int MOUSE_MOVE = 6;
    public static final int RIGHT_BUTTON_CLICK = 7;
    public static final int LEFT_BUTTON_CLICK = 8;
    public static final int SEND_TEXT_CLICK = 9;
    private static final String TAG = "Cursor Robot Thread";
    // class variable that gets the screen size of the connected monitor
    private final static Dimension SCREENSIZE = Toolkit.getDefaultToolkit().getScreenSize();
    // control the speed of the robot
    private static final int SPEEDINCREASE = 1;
    // find the center of the screen
    public static int ctrWidth;
    public static int ctrHeight;
    // the robot class object
    private Robot robot;
    private int[] convertedValues;
    private int currentX;
    private int currentY;


    /**
     * Class Constructor, initiates the robot object
     * and positions it in the middle of the screen.
     */
    public RobotController() {
        initRobot();
    }


    /**
     * @param acceloData
     */
    public void setAcceloData(String acceloData) {
        //this.acceloData = acceloData;
        convertedValues = convertStringToIntArray(acceloData);
    }


    /**
     * @return the currentX
     */
    public int getCurrentX() {
        return currentX;
    }


    /**
     * @param currentX the currentX to set
     */
    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }


    /**
     * @return the currentY
     */
    public int getCurrentY() {
        return currentY;
    }


    /**
     * @param currentY the currentY to set
     */
    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }


    /**
     * This run() method overrides the Runnable run() method.
     * This method determines if the incoming packet should be
     * sent to the moveCursor() or mouseClicked() method based
     * on the first value in the array.
     * <p/>
     * It does this every 100m/s
     */
    @Override
    public void run() {
        while (true) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                // catch the interrupted exception
                e.printStackTrace();
            }
            // the current array is null no data has been passed to the setAcceloData method.
            if (convertedValues == null)
                return;

            int robotType = convertedValues[0];
            // move the mouse if the state is between 1 and 4 inclusive
            if (robotType == 0) {
                if (robot != null) {
                    robot = null;
                }
//                System.exit(0);
            } else if (robotType > 0 && robotType < 5) {
                moveCursor(robotType);
            } else
                mouseClicked(robotType);
        }

    }

    /**
     * Resets the robot coordinates to the current pointer position.
     *
     * @throws HeadlessException
     */
    public void resetMousePointerInfo() throws HeadlessException {
        setCurrentX(MouseInfo.getPointerInfo().getLocation().x);
        setCurrentY(MouseInfo.getPointerInfo().getLocation().y);
    }


    // Initiate the robot object and position it in the center of the screen
    private void initRobot() {
        ctrHeight = getCtrHeight();
        ctrWidth = getCtrWidth();

        try {
            // create a new robot object.
            robot = new Robot();
            robot.mouseMove(ctrWidth, ctrHeight);
            resetMousePointerInfo();
        } catch (AWTException eAWT) {
            System.out.print(TAG + "\n");
            eAWT.printStackTrace();
            eAWT.getCause();
            System.exit(-1);
        }

        System.out.println("Starting move at: " + MouseInfo.getPointerInfo()
                .getLocation()
                .toString());
    }


    /*
     * When the mouse is clicked, the direction is passed in
     * depending on the direction the timer is started
     */
    private synchronized void mouseClicked(int clickType) {
        long startTime;
        long endTime;
        long diff;
        switch (clickType) {
            case SEND_TEXT_CLICK:
                robot.keyPress(KeyEvent.VK_ENTER);
                startTime = System.currentTimeMillis();
                robot.keyRelease(KeyEvent.VK_ENTER);
                endTime = System.currentTimeMillis();

                diff = endTime - startTime;
                try {
                    sleep(diff);
                } catch (InterruptedException e) {
                }

                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                break;
            case LEFT_BUTTON_CLICK:
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                startTime = System.currentTimeMillis();
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                endTime = System.currentTimeMillis();

                diff = endTime - startTime;
                try {
                    sleep(diff);
                } catch (InterruptedException e) {
                }

                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                break;
            case RIGHT_BUTTON_CLICK:
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                startTime = System.currentTimeMillis();
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                endTime = System.currentTimeMillis();

                diff = endTime - startTime;
                try {
                    sleep(diff);
                } catch (InterruptedException e) {
                }

                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                break;
        }
    }


    /*
     * Method used to determine the direction of the pointer movement.
     */
    private synchronized void moveCursor(int direction) {
        int x = convertedValues[1];
        int y = convertedValues[2];

        switch (direction) {
            case LEFTDOWN:
                moveLeftDown(x, y);
                break;
            case RIGHTUP:
                moveRightUp(x, y);
                break;
            case LEFTUP:
                moveLeftUp(x, y);
                break;
            case RIGHTDOWN:
                moveRightDown(x, y);
                break;
        }
    }

    /*
     * move the robot in a right-down direction.
     */
    private void moveRightDown(int x, int y) {
        robot.mouseMove(getCurrentX() + (x * SPEEDINCREASE),
                getCurrentY() + (y * SPEEDINCREASE));
        System.out.println("Move right down: " + MouseInfo.getPointerInfo()
                .getLocation()
                .toString());
        resetMousePointerInfo();
    }

    /*
     * move the robot in a left-up direction.
     */
    private void moveLeftUp(int x, int y) {
        robot.mouseMove(getCurrentX() - (x * SPEEDINCREASE),
                getCurrentY() - (y * SPEEDINCREASE));
        System.out.println("Move left up: " + MouseInfo.getPointerInfo()
                .getLocation()
                .toString());
        resetMousePointerInfo();
    }

    /*
     * move the robot in a right-up direction.
     */
    private void moveRightUp(int x, int y) {
        robot.mouseMove(getCurrentX() + (x * SPEEDINCREASE),
                getCurrentY() - (y * SPEEDINCREASE));
        System.out.println("Move right up: " + MouseInfo.getPointerInfo()
                .getLocation()
                .toString());
        resetMousePointerInfo();
    }

    /*
     * move the robot in a left-down direction.
     */
    private void moveLeftDown(int x, int y) {
        robot.mouseMove(getCurrentX() - (x * SPEEDINCREASE),
                getCurrentY() + (y * SPEEDINCREASE));
        System.out.println("Move left down: " + MouseInfo.getPointerInfo()
                .getLocation()
                .toString());

        resetMousePointerInfo();
    }


    /*
     * Convert the incoming data String, which has being formatted to
     * store the data in three blocks "[type] [x] [y]"
     *
     * type: 	signals the message type (Mouse movement, Mouse click, etc)
     * x:		the current x value
     * y:		the current y value
     *
     * @param acceloData
     *        the string data passed, to be converted
     * @return data
     *         the integer[]
     */
    private synchronized int[] convertStringToIntArray(String acceloData) {
        StringTokenizer st = new StringTokenizer(acceloData);
        int[] data = new int[acceloData.length()];
        int i = 0;
        while (st.hasMoreTokens()) {
            int val = Integer.parseInt(st.nextToken());
            if (val >= 0 && val <= 9) {
                data[i] = val;
            }
            i++;
        }

        return data;
    }

    /**
     * @return the ctrWidth
     */
    private int getCtrWidth() {
        return SCREENSIZE.width / 2;
    }

    /**
     * @return the ctrHeight
     */
    private int getCtrHeight() {
        return SCREENSIZE.height / 2;
    }

}// end of class

