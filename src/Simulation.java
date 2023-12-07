import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    public static final int MAX_X_POS = 140;
    public static final int MAX_Y_POS = 35;
    public static final long FRAME_DELAY = 1 * 1;
    public static final int SPAWN_PERCENTAGE = 29;
    public static int TICK_COUNT = 0;
    private final GameScreen screen;
    private final List<SimObject> mainObjectList = new ArrayList<>();
    private final List<SimObject> bufferList = new ArrayList<>();

    public Simulation() {
        screen = new GameScreen();
        initializeObjects();
    }

    private void initializeObjects() {
        Random random = new Random();

        /*
         * SimObject sideways = new SimObject(0, 15);
         * sideways.setXVelocity(1);
         * sideways.setYVelocity(0);
         * mainObjectList.add(sideways);
         * bufferList.add(sideways.copy());
         * 
         * SimObject vertical = new SimObject(15, 29);
         * vertical.setXVelocity(0);
         * vertical.setYVelocity(-1);
         * mainObjectList.add(vertical);
         * bufferList.add(vertical.copy());
         */

        for (int i = 0; i < MAX_X_POS; i++) {
            for (int j = 0; j < MAX_Y_POS; j++) {
                int k = random.nextInt(SPAWN_PERCENTAGE);

                if (k == 0) {
                    SimObject simObject = new SimObject(i, j);
                    mainObjectList.add(simObject);
                    bufferList.add(simObject.copy());
                }
            }
        }

    }

    public void startLoop() throws InterruptedException {
        while (true) {
            updateObjectPositions();
            mainObjectList.clear();
            mainObjectList.addAll(this.bufferList.stream().map(SimObject::copy).toList());
            handleClippings();
            displayObjects();
            this.bufferList.clear();
            this.bufferList.addAll(this.mainObjectList.stream().map(SimObject::copy).toList());
            TICK_COUNT++;
        }
    }

    private void updateObjectPositions() {
        for (SimObject object : this.bufferList) {
            SimObject collidingObject = getCollidingObject(object);
            object.tryMove(collidingObject);
        }
    }

    private void handleClippings() {
        for (SimObject object : this.mainObjectList) {
            if (isClippingWithObject(object)) {
                tryClipOut(object);
            }
        }
    }

    private boolean isClippingWithObject(SimObject object) {
        for (SimObject subObject : this.mainObjectList) {
            if (object.getXPos() == subObject.getXPos() && object.getYPos() == subObject.getYPos()
                    && !object.equals(subObject)) {
                return true;
            }
        }
        return false;
    }

    private void tryClipOut(SimObject object) {

        int xPos = object.getXPos();
        int yPos = object.getYPos();

        int[][] clipOutVectors = generateClipOutVectors(object.getVelocity());

        for (int[] clipOutVector : clipOutVectors) {

            int newX = xPos + clipOutVector[0];
            int newY = yPos + clipOutVector[1];

            SimObject objectAt = getObjectFromMain(newX, newY);

            if (objectAt == null && !SimObject.isDirectionOutOfBounds(xPos, yPos, clipOutVector)) {

                object.setXPosition(newX);
                object.setYPosition(newY);

                return;

            }

        }

    }

    private SimObject getCollidingObject(SimObject object) {
        int xPos = object.getXPos();
        int yPos = object.getYPos();

        SimObject clippingObject = getObjectFromBuffer(xPos, yPos);

        if (clippingObject != null && !clippingObject.equals(object)) {
            return clippingObject;
        }

        int[][] moveVectors = generateHitboxVectors(new int[] { object.getVelocity()[0], object.getVelocity()[1] });

        for (int[] radialVector : moveVectors) {

            int searchX = xPos + radialVector[0];
            int searchY = yPos + radialVector[1];

            SimObject collidingObject = getObjectFromBuffer(searchX, searchY);

            if (collidingObject != null && !collidingObject.equals(object)) {

                return collidingObject;

            }

        }

        return null;
    }

    private SimObject getObjectFromBuffer(int x, int y) {
        for (SimObject object : this.bufferList) {
            if (x == object.getXPos() && y == object.getYPos()) {
                return object;
            }
        }
        return null;
    }

    private SimObject getObjectFromMain(int x, int y) {
        for (SimObject object : this.mainObjectList) {
            if (x == object.getXPos() && y == object.getYPos()) {
                return object;
            }
        }
        return null;
    }

    private void displayObjects() throws InterruptedException {
        mainObjectList.forEach(this.screen::setScreenElement);
        System.out.println(mainObjectList.size());
        this.screen.display();
        this.screen.clearDisplay();
    }

    private static int[][] generateHitboxVectors(int[] direction) {
        ArrayList<int[]> moveVectors = new ArrayList<>();

        int xDir = direction[0] == 0 ? 0 : direction[0] > 0 ? 1 : -1;
        int yDir = direction[1] == 0 ? 0 : direction[1] > 0 ? 1 : -1;

        double originalAngle = Math.atan2(yDir, xDir);

        moveVectors.add(new int[] { xDir, yDir });

        for (int i = 1; i <= 0; i++) {
            // Calculate left vector
            double left = originalAngle - Math.toRadians(45 * i);
            int xLeft = (int) Math.round(Math.cos(left));
            int yLeft = (int) Math.round(Math.sin(left));
            moveVectors.add(new int[] { xLeft, yLeft });

            // Calculate right vector
            double right = originalAngle + Math.toRadians(45 * i);
            int xRight = (int) Math.round(Math.cos(right));
            int yRight = (int) Math.round(Math.sin(right));
            moveVectors.add(new int[] { xRight, yRight });
        }

        // moveVectors.add(new int[] { xDir * -1, yDir * -1 });

        return moveVectors.toArray(new int[0][0]);
    }

    private static int[][] generateClipOutVectors(int[] direction) {

        ArrayList<int[]> moveVectors = new ArrayList<>();

        int xDir = direction[0] == 0 ? 0 : direction[0] > 0 ? 1 : -1;
        int yDir = direction[1] == 0 ? 0 : direction[1] > 0 ? 1 : -1;

        double originalAngle = Math.atan2(yDir, xDir);

        moveVectors.add(new int[] { xDir, yDir });

        for (int magnitude = 1; magnitude <= Math.max(MAX_X_POS, MAX_Y_POS); magnitude++) {

            for (int i = 1; i <= 3; i++) {
                // Calculate left vector
                double left = originalAngle - Math.toRadians(45 * i);
                int xLeft = (int) Math.round(Math.cos(left));
                int yLeft = (int) Math.round(Math.sin(left));
                moveVectors.add(new int[] { xLeft * magnitude, yLeft * magnitude });

                // Calculate right vector
                double right = originalAngle + Math.toRadians(45 * i);
                int xRight = (int) Math.round(Math.cos(right));
                int yRight = (int) Math.round(Math.sin(right));
                moveVectors.add(new int[] { xRight * magnitude, yRight * magnitude });
            }

            moveVectors.add(new int[] { xDir * -1 * magnitude, yDir * -1 * magnitude });

        }
        return moveVectors.toArray(new int[0][0]);

    }

}