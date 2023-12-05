
public class GameScreen {

    private final char[][] matrixScreen = new char[Simulation.MAX_Y_POS][Simulation.MAX_X_POS];

    public GameScreen() {

        for (int i = 0; i < Simulation.MAX_X_POS; i++) {
            for (int j = 0; j < Simulation.MAX_Y_POS; j++) {
                matrixScreen[j][i] = ' ';
            }
        }
    }

    public void setScreenElement(SimObject object) {
        matrixScreen[object.getYPos()][object.getXPos()] = object.getType().getCharId();
    }

    public void clearDisplay() {

        for (int i = 0; i < Simulation.MAX_X_POS; i++) {
            for (int j = 0; j < Simulation.MAX_Y_POS; j++) {
                matrixScreen[j][i] = ' ';
            }
        }

    }

    public void display() throws InterruptedException {
        int k = 0;
        System.out.println(Simulation.TICK_COUNT);
        for (int j = 0; j < Simulation.MAX_Y_POS; j++) {
            for (int i = 0; i < Simulation.MAX_X_POS; i++) {
                System.out.print(matrixScreen[j][i]);
                if (matrixScreen[j][i] != ' ') {
                    k++;
                }

            }
            System.out.println();
        }
        System.out.println(k);
        Thread.sleep(Simulation.FRAME_DELAY);
        clearConsole();
    }

    public static void clearConsole() {

        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
