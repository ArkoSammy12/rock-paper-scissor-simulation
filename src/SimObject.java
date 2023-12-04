import java.util.Objects;
import java.util.Random;

public class SimObject {

    public static final int MAX_VELOCITY = 1;
    public static final int MIN_VELOCITY = -1;

    private SimObjectType type;
    private int xPos;
    private int yPos;
    private int[] velocity = new int[2];

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SimObject object))
            return false;
        return xPos == object.xPos && yPos == object.yPos && getType() == object.getType()
                && getVelocity()[0] == object.getVelocity()[0] && getVelocity()[1] == object.getVelocity()[1];
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), xPos, yPos, getVelocity());
    }

    public SimObject(int x, int y) {
        Random random = new Random();
        int i = random.nextInt(3) + 1;
        switch (i) {
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
        do {
            this.velocity[0] = random.nextInt((MAX_VELOCITY - (MIN_VELOCITY)) + 1) + (MIN_VELOCITY);
        } while (this.velocity[0] == 0);

        do {
            this.velocity[1] = random.nextInt((MAX_VELOCITY - (MIN_VELOCITY)) + 1) + (MIN_VELOCITY);
        } while (this.velocity[1] == 0);

    }

    public void setType(SimObjectType type) {
        this.type = type;
    }

    public int getXPos() {
        return this.xPos;
    }

    public int getYPos() {
        return this.yPos;
    }

    public SimObjectType getType() {
        return this.type;
    }

    public void setXPosition(int x) {
        this.xPos = x;
    }

    public void setYPosition(int y) {
        this.yPos = y;
    }

    public void setXVelocity(int x) {
        this.velocity[0] = x;
    }

    public void setYVelocity(int y) {
        this.velocity[1] = y;
    }

    public int[] getVelocity() {
        return this.velocity;
    }

    public static int[] decrementVector(int[] vector) {

        int x = vector[0] > 0 ? vector[0]-- : vector[0]++;
        int y = vector[1] > 0 ? vector[1]-- : vector[1]++;

        return new int[] { x, y };

    }

    public SimObject copy() {
        SimObject copy = new SimObject(this.xPos, this.yPos);
        copy.setType(this.getType());
        copy.setXVelocity(this.getVelocity()[0]);
        copy.setYVelocity(this.getVelocity()[1]);
        ;
        return copy;
    }

    public void tryMove(SimObject collidingObject) {

        int[] moveVector = this.getVelocity();
        int newX = this.xPos + moveVector[0];
        int newY = this.yPos + moveVector[1];

        if (collidingObject != null) {
            SimObjectType newType = this.getType().getWinningType(collidingObject.getType());
            this.setType(newType);
            int newXComp = Math.clamp((this.velocity[0] + collidingObject.getVelocity()[0]), MIN_VELOCITY,
                    MAX_VELOCITY);
            int newYComp = Math.clamp((this.velocity[1] + collidingObject.getVelocity()[1]), MIN_VELOCITY,
                    MAX_VELOCITY);
            newXComp = newXComp > 0 ? Math.ceilDivExact(newXComp, 2) : Math.floorDivExact(newXComp, 2);
            newYComp = newYComp > 0 ? Math.ceilDivExact(newYComp, 2) : Math.floorDivExact(newYComp, 2);

            if (newXComp == 0 && newYComp == 0) {

                this.velocity[0] *= -1;
                this.velocity[1] *= -1;

            } else {

                this.velocity[0] = newXComp;
                this.velocity[1] = newYComp;

            }

            collidingObject.setType(newType);
            newX = this.xPos + this.getVelocity()[0];
            newY = this.yPos + this.getVelocity()[1];

        }

        boolean movingToOutOfBounds = isDirectionOutOfBounds(this.xPos, this.yPos, this.getVelocity());

        if (movingToOutOfBounds) {

            int xComp = Math.abs(moveVector[0]);
            int yComp = Math.abs(moveVector[1]);

            while (xComp > 0 && yComp > 0) {

                int moveX = moveVector[0] == 0 ? 0 : moveVector[0] > 0 ? xComp : xComp * -1;
                int moveY = moveVector[1] == 0 ? 0 : moveVector[1] > 0 ? yComp : yComp * -1;

                if (!isDirectionOutOfBounds(this.xPos, this.yPos, new int[] { moveX, moveY })) {

                    moveVector = new int[] { moveX, moveY };
                    movingToOutOfBounds = false;
                    break;

                }

                xComp--;
                yComp--;

            }

        }

        if (movingToOutOfBounds) {
            boolean outOfBoundsOverMaxX = newX >= Simulation.MAX_X_POS;
            boolean outOfBoundsOverMinX = newX < 0;
            boolean outOfBoundsOverMaxY = newY >= Simulation.MAX_Y_POS;
            boolean outOfBoundsOverMinY = newY < 0;

            if (outOfBoundsOverMaxX || outOfBoundsOverMinX) {
                this.velocity[0] *= -1;
            }
            if (outOfBoundsOverMaxY || outOfBoundsOverMinY) {
                this.velocity[1] *= -1;
            }

            newX = this.xPos + this.getVelocity()[0];
            newY = this.yPos + this.getVelocity()[1];

        }

        this.xPos = newX;
        this.yPos = newY;

    }

    public static boolean isDirectionOutOfBounds(int x, int y, int[] velocity) {

        int newX = x + velocity[0];
        int newY = y + velocity[1];

        return newX >= Simulation.MAX_X_POS || newX < 0 || newY >= Simulation.MAX_Y_POS || newY < 0;

    }

    public enum SimObjectType {
        SCISSOR('V'),
        ROCK('O'),
        PAPER('E');

        private final char charId;

        SimObjectType(char charId) {
            this.charId = charId;
        }

        public char getCharId() {
            return this.charId;
        }

        public SimObjectType getWinningType(SimObjectType other) {

            if (this == SimObjectType.ROCK) {

                return switch (other) {

                    case ROCK, SCISSOR -> ROCK;
                    case PAPER -> PAPER;

                };

            } else if (this == SimObjectType.PAPER) {

                return switch (other) {

                    case ROCK, PAPER -> PAPER;
                    case SCISSOR -> SCISSOR;

                };

            } else if (this == SimObjectType.SCISSOR) {

                return switch (other) {

                    case ROCK -> ROCK;
                    case PAPER, SCISSOR -> SCISSOR;

                };

            }

            throw new IllegalArgumentException();

        }

    }

}