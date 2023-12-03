import java.util.Objects;
import java.util.Random;

public class SimObject {

    private SimObjectType type;
    private int xPos;
    private int yPos;
    private Direction direction = Direction.getRandomDirection();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimObject object)) return false;
        return xPos == object.xPos && yPos == object.yPos && getType() == object.getType() && getDirection() == object.getDirection();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), xPos, yPos, getDirection());
    }

    public SimObject(int x, int y){
        Random random = new Random();
        int i = random.nextInt(1, 4);
        switch(i){
            case 1:
                this.type = SimObjectType.ROCK;
                break;
            case 2:
                this.type = SimObjectType.PAPER;
                break;
            case 3:
                this.type = SimObjectType.SCISSOR;
                break;
        }
        this.xPos = x;
        this.yPos = y;
    }


    public void setDirection(Direction direction){
        this.direction = direction;
    }


    public void setType(SimObjectType type){
        this.type = type;
    }

    public int getXPos(){
        return this.xPos;
    }

    public int getYPos(){
        return this.yPos;
    }

    public SimObjectType getType(){
        return this.type;
    }

    public Direction getDirection(){
        return this.direction;
    }

    public SimObject copy() {
        SimObject copy = new SimObject(this.xPos, this.yPos);
        copy.setDirection(this.direction);
        copy.setType(this.type);
        return copy;
    }

    public void tryMove(SimObject collidingObject) {
        int newX = this.xPos + this.getDirection().getVector()[0];
        int newY = this.yPos + this.getDirection().getVector()[1];

        boolean movingToOutOfBounds = Direction.isDirectionOutOfBounds(this.xPos, this.yPos, direction);
        
        if (movingToOutOfBounds) {
            boolean outOfBoundsOverMaxX = newX >= Simulation.MAX_X_POS;
            boolean outOfBoundsOverMinX = newX < 0;
            boolean outOfBoundsOverMaxY = newY >= Simulation.MAX_Y_POS;
            boolean outOfBoundsOverMinY = newY < 0;
            Direction bounceDirection = this.getDirection().getBounceDirection(outOfBoundsOverMaxX, outOfBoundsOverMinX, outOfBoundsOverMaxY, outOfBoundsOverMinY);
            newX = this.xPos + bounceDirection.getVector()[0];
            newY = this.yPos + bounceDirection.getVector()[1];
            this.setDirection(bounceDirection);
        } else if (collidingObject != null) {
            SimObjectType newType = this.getType().getWinningType(collidingObject.getType());
            this.setType(newType);
            boolean outOfBoundsOverMaxX = this.getDirection() == Direction.RIGHT || this.getDirection() == Direction.UP_RIGHT || this.getDirection() == Direction.DOWN_RIGHT;
            boolean outOfBoundsOverMinX = this.getDirection() == Direction.LEFT || this.getDirection() == Direction.UP_LEFT || this.getDirection() == Direction.DOWN_LEFT;
            boolean outOfBoundsOverMaxY = this.getDirection() == Direction.UP || this.getDirection() == Direction.UP_RIGHT || this.getDirection() == Direction.UP_LEFT;
            boolean outOfBoundsOverMinY = this.getDirection() == Direction.DOWN || this.getDirection() == Direction.DOWN_RIGHT || this.getDirection() == Direction.DOWN_LEFT;
            //TODO: Fix this
            Direction bounceDirection = this.getDirection().combineDirection(collidingObject.getDirection());
            if(bounceDirection == null || Direction.isDirectionOutOfBounds(this.getXPos(), this.getYPos(), bounceDirection)) {
                bounceDirection = this.getDirection().getBounceDirection(outOfBoundsOverMaxX, outOfBoundsOverMinX, outOfBoundsOverMaxY, outOfBoundsOverMinY);
            }

            this.setDirection(bounceDirection);
            collidingObject.setType(newType);
            newX = this.xPos + bounceDirection.getVector()[0];
            newY = this.yPos + bounceDirection.getVector()[1];

            
        }

        this.xPos = newX;
        this.yPos = newY;

    }


    public enum SimObjectType{
        SCISSOR('V'),
        ROCK('O'),
        PAPER('E');

        private final char charId;

        SimObjectType(char charId){
            this.charId = charId;
        }

        public char getCharId(){
            return this.charId;
        }

        public SimObjectType getWinningType(SimObjectType other){

            if (this == SimObjectType.ROCK) {

                return switch(other){

                    case ROCK, SCISSOR -> ROCK;
                    case PAPER -> PAPER;

                };

            }  else if (this == SimObjectType.PAPER){

                return switch(other){

                    case ROCK, PAPER -> PAPER;
                    case SCISSOR -> SCISSOR;

                };

            } else if (this == SimObjectType.SCISSOR){

                return switch (other){

                    case ROCK -> ROCK;
                    case PAPER, SCISSOR -> SCISSOR;

                };

            }

            throw new IllegalArgumentException();

        }

    }

    public enum Direction{
        UP(new int[]{0, 1}),
        RIGHT(new int[]{1, 0}),
        DOWN(new int[]{0, -1}),
        LEFT(new int[]{-1, 0}),
        UP_RIGHT(new int[]{1, -1}),
        UP_LEFT(new int[]{-1, -1}),
        DOWN_RIGHT(new int[]{1, 1}),
        DOWN_LEFT(new int[]{-1, 1});

        private final int[] vector;

         Direction(int[] vector){
            this.vector = vector;
        }

        public int[] getVector(){
             return this.vector;
        }

        public static Direction fromVector(int[] vector){

            for(Direction direction : Direction.values()){

                if(direction.getVector()[0] == vector[0] && direction.getVector()[1] == vector[1]){
                    return direction;
                }

            }

            throw new IllegalArgumentException(vector[0] + " " + vector[1]);

        }

        public Direction combineDirection(Direction direction){

            int newX = Math.clamp(this.getVector()[0] + direction.getVector()[0], -1, 1);
            int newY = Math.clamp(this.getVector()[1] + direction.getVector()[1], -1, 1);

            if(newX == 0 && newY == 0){
                return null;
            }

            return Direction.fromVector(new int[]{newX, newY});

        }

        public static boolean isDirectionOutOfBounds(int x, int y, Direction direction){

            int newX = x + direction.getVector()[0];
            int newY = y + direction.getVector()[1];

            return newX >= Simulation.MAX_X_POS || newX < 0 || newY >= Simulation.MAX_Y_POS || newY < 0;

        }

        public Direction getBounceDirection(boolean outOfBoundsOverMaxX, boolean outOfBoundsOverMinX, boolean outOfBoundsOverMaxY, boolean outOfBoundsOverMinY) {
                    
            boolean outOfBoundsFromCorner = (outOfBoundsOverMaxX && outOfBoundsOverMaxY) || (outOfBoundsOverMaxX && outOfBoundsOverMinY) || (outOfBoundsOverMinX && outOfBoundsOverMinY) || (outOfBoundsOverMinX && outOfBoundsOverMaxY);

            if(outOfBoundsFromCorner){
                return switch (this) {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case RIGHT -> LEFT;
                    case LEFT -> RIGHT;
                    case UP_RIGHT -> DOWN_LEFT;
                    case UP_LEFT -> DOWN_RIGHT;
                    case DOWN_RIGHT -> UP_LEFT;
                    case DOWN_LEFT -> UP_RIGHT;
                };
            } else if (outOfBoundsOverMaxX) {

                return switch (this) {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case RIGHT -> LEFT;
                    case LEFT -> RIGHT;
                    case UP_RIGHT -> UP_LEFT;
                    case UP_LEFT, DOWN_LEFT -> DOWN_RIGHT;
                    case DOWN_RIGHT -> DOWN_LEFT;
                };
            } else if (outOfBoundsOverMinX) {

                return switch (this) {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case RIGHT -> LEFT;
                    case LEFT -> RIGHT;
                    case UP_RIGHT -> UP_LEFT;
                    case UP_LEFT -> UP_RIGHT;
                    case DOWN_RIGHT -> DOWN_LEFT;
                    case DOWN_LEFT -> DOWN_RIGHT;
                };

            } else if (outOfBoundsOverMaxY) {

                return switch (this) {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case RIGHT -> LEFT;
                    case LEFT -> RIGHT;
                    case UP_RIGHT, DOWN_LEFT -> UP_LEFT;
                    case UP_LEFT, DOWN_RIGHT -> UP_RIGHT;
                };

            } else if (outOfBoundsOverMinY){

                return switch (this) {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case RIGHT -> LEFT;
                    case LEFT -> RIGHT;
                    case UP_RIGHT -> DOWN_RIGHT;
                    case UP_LEFT -> DOWN_LEFT;
                    case DOWN_RIGHT -> UP_RIGHT;
                    case DOWN_LEFT -> UP_LEFT;
                };               

            } else {
                throw new IllegalArgumentException();
            }


        }

        public static Direction getRandomDirection(){

            Random random = new Random();

            int i = random.nextInt(0, Direction.values().length - 1);
            return Direction.values()[i];

        }

    }


}