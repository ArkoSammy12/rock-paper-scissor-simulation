import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    public static final int MAX_X_POS = 130;
    public static final int MAX_Y_POS = 35;
    public static final long FRAME_DELAY = 1 * 1;
    public static final int SPAWN_PERCENTAGE = 45;
    private final GameScreen screen;
    private final List<SimObject> mainObjectList = new ArrayList<>();
    private final List<SimObject> bufferList = new ArrayList<>();

    public Simulation() {
        screen = new GameScreen();
        initializeObjects();
    }

    private void initializeObjects() {
        Random random = new Random();

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
            if (isCollidingWithObject(object)) {
                resolveClipping(object);(object);
            }
        }
    }

    private boolean isCollidingWithObject(SimObject object) {
        for (SimObject subObject : this.mainObjectList) {
            if (object.getXPos() == subObject.getXPos() && object.getYPos() == subObject.getYPos()
                    && !object.equals(subObject)) {
                return true;
            }
        }
        return false;
    }

    private void resolveClipping(SimObject object) {
        int m = Math.max(MAX_Y_POS, MAX_X_POS);
        int i = 1;

        while (i < m) {

            int[][] moveVectors = generateHitboxVectors(
                    new int[] { object.getVelocity()[0] * -1, object.getVelocity()[1] * -1 });

            for (int[] tryMoveVector : moveVectors) {
                if (tryClipOut(object, tryMoveVector[0] * i, tryMoveVector[1] * i)) {
                    return;
                }
            }

            i++;
        }
    }

    private boolean tryClipOut(SimObject object, int deltaX, int deltaY) {
        int newX = object.getXPos() + deltaX;
        int newY = object.getYPos() + deltaY;

        if (!SimObject.isDirectionOutOfBounds(object.getXPos(), object.getYPos(), new int[] { deltaX, deltaY })
                && this.getObjectAt(newX, newY) == null) {

            object.setXPosition(newX);
            object.setYPosition(newY);
            return true;
        }

        return false;
    }

    private SimObject getCollidingObject(SimObject object) {
        int xPos = object.getXPos();
        int yPos = object.getYPos();

        int[][] moveVectors = generateHitboxVectors(new int[] { object.getVelocity()[0], object.getVelocity()[1] });

        for (int[] radialVector : moveVectors) {

            int searchX = xPos + radialVector[0];
            int searchY = yPos + radialVector[1];

            SimObject collidingObject = getObjectAt(searchX, searchY);

            if (collidingObject != null) {

                return collidingObject;

            }

        }

        return null;
    }

    private SimObject getObjectAt(int x, int y) {
        for (SimObject object : this.bufferList) {
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

        double angle = Math.atan2(yDir, xDir);

        moveVectors.add(new int[] { xDir, yDir });

        for (int i = 0; i < 7; i++) {
            angle -= Math.toRadians(45);

            int x = (int) Math.round(Math.cos(angle));
            int y = (int) Math.round(Math.sin(angle));

            moveVectors.add(new int[] { x, y });
        }

        return moveVectors.toArray(new int[8][2]);
    }

}