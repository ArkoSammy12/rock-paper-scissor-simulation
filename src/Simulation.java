import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    public static final int MAX_X_POS = 30;
    public static final int MAX_Y_POS = 30;
    public static final long FRAME_DELAY = 10 * 1;
    private GameScreen screen;
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
                int k = random.nextInt(10);

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
            handleCollisions();
            displayObjects();
            updateLists();
        }
    }

    private void updateObjectPositions() {
        for (SimObject object : this.bufferList) {
            SimObject collidingObject = getCollidingObject(object);
            object.tryMove(collidingObject);
        }
    }

    private void handleCollisions() {
        for (SimObject object : this.bufferList) {
            if (isColliding(object)) {
                resolveCollision(object);
            }
        }
    }

    private boolean isColliding(SimObject object) {
        for (SimObject subObject : this.bufferList) {
            if (object.getXPos() == subObject.getXPos() && object.getYPos() == subObject.getYPos()
                    && !object.equals(subObject)) {
                return true;
            }
        }
        return false;
    }

    private void resolveCollision(SimObject object) {
        int m = Math.max(MAX_Y_POS, MAX_X_POS);
        int i = 1;

        x: while (i < m) {
            int[][] moveVectors = { { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }, { 0, -1 },
                    { 1, -1 } };

            for (int[] tryMoveVector : moveVectors) {
                if (tryMove(object, tryMoveVector[0] * i, tryMoveVector[1] * i)) {
                    return;
                }
            }

            i++;
        }
    }

    private boolean tryMove(SimObject object, int deltaX, int deltaY) {
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

        if (!SimObject.isDirectionOutOfBounds(xPos, yPos, object.getVelocity())) {
            int[] moveVector = object.getVelocity();

            for (SimObject objectInSim : this.mainObjectList) {
                int moveX = Math.abs(moveVector[0]);
                int moveY = Math.abs(moveVector[1]);

                while (moveX >= 0 && moveX >= 0) {
                    int checkX = moveVector[0] == 0 ? 0 : moveVector[0] > 0 ? moveX : moveX * -1;
                    int checkY = moveVector[1] == 0 ? 0 : moveVector[1] > 0 ? moveY : moveY * -1;

                    int newX = xPos + checkX;
                    int newY = yPos + checkY;

                    if (!objectInSim.equals(object)) {
                        if ((newX == objectInSim.getXPos() && newY == objectInSim.getYPos())
                                || (xPos == objectInSim.getXPos() && yPos == objectInSim.getYPos())) {
                            return objectInSim;
                        }
                    }

                    moveX--;
                    moveY--;
                }
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
        mainObjectList.forEach(object -> this.screen.setScreenElement(object));
        System.out.println(mainObjectList.size());
        this.screen.display();
        this.screen.clearDisplay();
    }

    private void updateLists() {
        mainObjectList.clear();
        mainObjectList.addAll(bufferList);
        this.bufferList.clear();
        this.bufferList.addAll(this.mainObjectList.stream().map(SimObject::copy).toList());
    }
}