
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Simulation {

    public static final int MAX_X_POS = 100;
    public static final int MAX_Y_POS = 30;
    public static final long FRAME_DELAY = 1 * 1;
    GameScreen screen;
    private final List<SimObject> mainObjectList = new ArrayList<>();
    private final List<SimObject> bufferList = new ArrayList<>();

    public Simulation() {

        screen = new GameScreen();
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

            for (SimObject object : this.bufferList) {

                SimObject collidingObject = getCollidingObject(object);
                object.tryMove(collidingObject);

            }

            mainObjectList.clear();
            mainObjectList.addAll(bufferList);

            mainObjectList.forEach(object -> this.screen.setScreenElement(object));
            this.screen.display();
            this.screen.clearDisplay();

            this.bufferList.clear();
            this.bufferList.addAll(this.mainObjectList.stream().map(SimObject::copy).toList());

        }

    }

    public SimObject getCollidingObject(SimObject object) {
        int xPos = object.getXPos();
        int yPos = object.getYPos();
    
        if (!SimObject.Direction.isDirectionOutOfBounds(xPos, yPos, object.getDirection())) {
            int newX = xPos + object.getDirection().getVector()[0];
            int newY = yPos + object.getDirection().getVector()[1];
    
            for (SimObject objectInSim : this.mainObjectList) {
                if(!objectInSim.equals(object)){
                        if((newX == objectInSim.getXPos() && newY == objectInSim.getYPos() || (xPos == objectInSim.getXPos() && yPos == objectInSim.getYPos()))){
                            return  objectInSim;
                        }
                    }   

                }
            }

        return null;

    }

}
