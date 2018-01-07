import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver on 1/1/2018
 */
public class Renderer extends Application {
    private long prevTime = System.nanoTime();
    private Simulation sim = new Simulation();
    private String out_dir = "C:\\Users\\Oliver\\IdeaProjects\\NBodySim\\frames";

    private int width;
    private int height;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.show();

        width = (int) primaryStage.getWidth();
        height = (int) primaryStage.getHeight();

        Canvas canvas = new Canvas(width, height);
        root.getChildren().add(canvas);

        Simulation galaxy1 = sim.createGaussianGalaxy(100000, width / 2, height / 2 + 100, 0.2, 0, 50);
        sim.addBodies(galaxy1);

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                sim.advanceBH();
                renderBodies(canvas, true);
//                saveBodies();

                System.out.println("fps: " + 1000000000.0 / (System.nanoTime() - prevTime));
                prevTime = System.nanoTime();
            }
        }.start();
    }

    @Override
    public void stop() {
//        printState();
//        System.exit(0);
    }

    private void renderBodies(Canvas canvas, boolean save) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        PixelWriter pixelWriter = graphicsContext.getPixelWriter();
        graphicsContext.setLineWidth(0);
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//        if (sim.timeElapsed > 1000) {
//            sim.TIME_STEP = -1;
//        }
//        if (sim.timeElapsed > 2000){
//            sim.TIME_STEP = 0;
//        }
        List<Double> px = sim.getPx();
        List<Double> py = sim.getPy();
//        for (int i = 0; i < sim.size(); i++) {
//            graphicsContext.fillOval(pxOriginal.get(i), pyOriginal.get(i), 1, 1);
//        }
//        graphicsContext.setFill(Color.gray(1, 0.2));
        graphicsContext.setFill(Color.gray(1, 0.02));
        for (int i = 0; i < sim.size(); i++) {
            graphicsContext.fillOval(px.get(i), py.get(i), 3, 3);
//            pixelWriter.setColor(px.get(i).intValue(), py.get(i).intValue(), Color.WHITE);
        }

        if (save) {
            WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, snapshot);
            RenderedImage renderedSnapshot = SwingFXUtils.fromFXImage(snapshot, null);

            File out_file = new File(out_dir + "\\" +
                    String.format("%1$8s", sim.getFrame()
                    ).replace(" ", "0") + ".png");
            try {
                out_file.createNewFile();
                ImageIO.write(renderedSnapshot, "png", out_file);
            } catch (IOException e) {
                System.out.println("Error occurred during image io.");
                e.printStackTrace();
                printState();
                System.exit(1);
            }

        }
    }

    private void saveBodies() {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
//        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
//        canvas.snapshot(null, snapshot);
        List<Double> px = sim.getPx();
        List<Double> py = sim.getPy();
        for (int i = 0; i < sim.size(); i++) {
            int x = px.get(i).intValue();
            int y = py.get(i).intValue();
            if (0 <= x && x < width && 0 <= y && y < height) {
                bufferedImage.setRGB(px.get(i).intValue(), py.get(i).intValue(), 0x00ffffff);
            }
        }
        File out_file = new File(out_dir + "\\" +
                String.format("%1$8s", sim.getFrame()
                ).replace(" ", "0") + ".png");
        try {
            out_file.createNewFile();
            ImageIO.write(bufferedImage, "png", out_file);
        } catch (IOException e) {
            System.out.println("Error occurred during image io.");
            e.printStackTrace();
            printState();
            System.exit(1);
        }
    }

    private void printState() {
        System.out.println();
        System.out.println(Arrays.toString(sim.getPx().toArray()));
        System.out.println(Arrays.toString(sim.getPy().toArray()));
        System.out.println(Arrays.toString(sim.getVx().toArray()));
        System.out.println(Arrays.toString(sim.getVy().toArray()));
    }
}