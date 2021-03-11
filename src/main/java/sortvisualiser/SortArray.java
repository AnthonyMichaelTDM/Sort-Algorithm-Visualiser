
package sortvisualiser;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import sortvisualiser.algorithms.ISortAlgorithm;

/**
 * The array that can be sorted
 * auxArray stuff by Anthony Michael
 *
 * @author mhops
 */
public class SortArray extends JPanel {
    public static final int DEFAULT_WIN_WIDTH = 1280;
    public static final int DEFAULT_WIN_HEIGHT = 720;
    private static final int DEFAULT_BAR_WIDTH = 5;
    /**
     * This is the percent of the panel the bars will consume.
     * Based on the original 256 bars each being 2x their height
     * and 720px window height, or 512/720
     */
    private static final double BAR_HEIGHT_PERCENT = 512.0/720.0;
    private static final int NUM_BARS = DEFAULT_WIN_WIDTH / DEFAULT_BAR_WIDTH;

    /**
     * some variables added for auxilary arrays
     */
    private int[] auxArray;
    private boolean usesAuxArray = false;
    private static int NUM_AUX_BARS;
    private int auxArrayChanges = 0;
    private static final double AUX_PERCENT_OF_BAR_WINDOW = .25;

    private final int[] array;
    private final int[] barColours;
    private int spinnerValue = 0;
    private String algorithmName = "";
    private ISortAlgorithm algorithm;
    private long algorithmDelay = 0;

    private MidiSoundPlayer player;
    private JSpinner spinner;
    private boolean playSounds;

    private int arrayChanges = 0; // Number of changes to the array the current algorithm has taken so far

    public SortArray(boolean playSounds) {
        setBackground(Color.DARK_GRAY);
        array = new int[NUM_BARS];
        barColours = new int[NUM_BARS];
        for (int i = 0; i < NUM_BARS; i++) {
            array[i] = i;
            barColours[i] = 0;
        }
        player = new MidiSoundPlayer(NUM_BARS);
        this.playSounds = playSounds;
        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinner.addChangeListener((event) -> {
                algorithmDelay = (Integer) spinner.getValue();
                algorithm.setDelay(algorithmDelay);
            });
        add(spinner,BorderLayout.LINE_START);
    }

    /**
     * added for aux arrays, initializes the array as an array of length n filled with zeros
     */
    public void initAuxArray(int n) {
        auxArray = new int[n];
        NUM_AUX_BARS = n;
        for (int i = 0; i < n; i++){
            auxArray[i]=0;
        }
    }

    /**
     * added for aux arrays, get the size
     */
    public int auxArraySize() {
        return auxArray.length;
    }

    /**
     * added for aux arrays, get value at index 
     */
    public int getAuxValue(int index) {
        return auxArray[index];
    }
    /**
     * added for aux arrays, zero the value at index, and return what it was 
     */
    public int removeAux(int index) {
        int n = auxArray[index];
        auxArray[index] = 0;
        return n;
    }

    /**
     * added for aux arrays, set value at index
     */
    public void updateAuxSingle(int index, int value, long millisecondDelay, boolean isStep) {
        auxArray[index] = value;

        finaliseUpdate(value, millisecondDelay, false, isStep);
        repaint();
    }

    /**
     * added for aux arrays, get the maximum value in tha aray
     */
    public int getAuxMaxValue() {
        return Arrays.stream(auxArray).max().orElse(Integer.MIN_VALUE);
    }

    public int arraySize() {
        return array.length;
    }

    public int getValue(int index) {
        return array[index];
    }

    /**
     * Gets the max value of the array or Integer.MIN_VALUE if there isn't one.
     * @return the max value or Integer.MIN_VALUE.
     */
    public int getMaxValue() {
        return Arrays.stream(array).max().orElse(NUM_BARS);
    }

    private void finaliseUpdate(int value,  long millisecondDelay, boolean isStep, boolean isAuxStep) {
        repaint();
        try {
            Thread.sleep(millisecondDelay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (playSounds) {
            player.makeSound(value);
        }
        if (isStep) {
            arrayChanges++;
        }
        if (isAuxStep) {
            auxArrayChanges++;
        }
    }

    public void swap(int firstIndex, int secondIndex, long millisecondDelay, boolean isStep) {
        int temp = array[firstIndex];
        array[firstIndex] = array[secondIndex];
        array[secondIndex] = temp;

        barColours[firstIndex] = 100;
        barColours[secondIndex] = 100;

        finaliseUpdate((array[firstIndex] + array[secondIndex]) / 2, millisecondDelay, isStep, false);
    }

    public void updateSingle(int index, int value, long millisecondDelay, boolean isStep) {
        array[index] = value;
        barColours[index] = 100;

        finaliseUpdate(value, millisecondDelay, isStep, false);
        repaint();
    }

    public void shuffle() {
        arrayChanges = 0;
        Random rng = new Random();
        for (int i = 0; i < arraySize(); i++) {
            int swapWithIndex = rng.nextInt(arraySize() - 1);
            swap(i, swapWithIndex, 5, false);
        }
        arrayChanges = 0;
    }

    public void highlightArray() {
        for (int i = 0; i < arraySize(); i++) {
            updateSingle(i, getValue(i), 5, false);
        }
    }

    /**
     * Gets the canvas size
     *
     * @return size
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIN_WIDTH, DEFAULT_WIN_HEIGHT);
    }

    public void resetColours() {
        for (int i = 0; i < NUM_BARS; i++) {
            barColours[i] = 0;
        }
        repaint();
    }

    /**
     * Draws the array
     *
     * @param g The graphics device for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D panelGraphics = (Graphics2D) g.create();

        try
        {
            Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            panelGraphics.addRenderingHints(renderingHints);
            panelGraphics.setColor(Color.WHITE);
            panelGraphics.setFont(new Font("Monospaced", Font.BOLD, 20));
            panelGraphics.drawString(" Current algorithm: " + algorithmName, 10, 30);
            panelGraphics.drawString("Current step delay: " + algorithmDelay + "ms", 10, 55);
            panelGraphics.drawString("     Array Changes: " + arrayChanges, 10, 80);
            panelGraphics.drawString("     Auxillary Array Changes: " + auxArrayChanges, 10, 105);

            drawBars(panelGraphics);
        } finally {
            panelGraphics.dispose();
        }
    }

    private void drawBars(Graphics2D panelGraphics)
    {
        int barWidth = getWidth() / NUM_BARS;
        int auxBarWidth;
        try {auxBarWidth = getWidth() / NUM_AUX_BARS;}
        catch (Exception e) {auxBarWidth = getWidth() / NUM_BARS;}
        int bufferedImageWidth = barWidth * NUM_BARS;
        int bufferedImageHeight = getHeight();

        if(bufferedImageHeight > 0 && bufferedImageWidth > 0) {
            if(bufferedImageWidth < 256) {
                bufferedImageWidth = 256;
            }

            double maxValue = getMaxValue();

            BufferedImage bufferedImage = new BufferedImage(bufferedImageWidth, bufferedImageHeight, BufferedImage.TYPE_INT_ARGB);
            makeBufferedImageTransparent(bufferedImage);
            Graphics2D bufferedGraphics = null;
            Graphics2D auxBufferedGraphics=null;
            try
            {
                bufferedGraphics = bufferedImage.createGraphics();
                auxBufferedGraphics = bufferedImage.createGraphics();
                int xAux = 0; // added for aux array
                for (int x = 0; x < NUM_BARS; x++) {
                    double currentValue = getValue(x);
                    double percentOfMax = currentValue / maxValue;
                    double heightPercentOfPanel = percentOfMax * BAR_HEIGHT_PERCENT;

                    int height = (int) (heightPercentOfPanel * (double) getHeight());
                    if (usesAuxArray) height = (int) ((double) height * (1.0 - AUX_PERCENT_OF_BAR_WINDOW)); //added for aux arrays
                    int xBegin = x + (barWidth - 1) * x;
                    int yBegin = getHeight() - height;

                    int val = barColours[x] * 2;
                    if (val > 190) {
                        bufferedGraphics.setColor(new Color(255 - val, 255, 255 - val));
                    }
                    else {
                        bufferedGraphics.setColor(new Color(255, 255 - val, 255 - val));
                    }
                    bufferedGraphics.fillRect(xBegin, yBegin, barWidth, height);
                    if (barColours[x] > 0) {
                        barColours[x] -= 5;
                    }
                    //added for aux array
                    if (xAux < NUM_AUX_BARS && usesAuxArray)
                    {
                        double auxCurrentValue = getAuxValue(x);
                        double auxPercentOfMax = auxCurrentValue / getAuxMaxValue();
                        double auxHeightPercentOfPanel = auxPercentOfMax * BAR_HEIGHT_PERCENT;
                        
                        
                        int heightAux = (int) (auxHeightPercentOfPanel * ((double)getHeight()*AUX_PERCENT_OF_BAR_WINDOW));
                        int xBeginAux = xAux + (auxBarWidth - 1) * xAux;
                        int yBeginAux = (int)(AUX_PERCENT_OF_BAR_WINDOW*512.0) + (DEFAULT_WIN_HEIGHT - 512)- heightAux;//getHeight() - heightAux - 100;
                        
                        
                        /*auxBufferedGraphics/*/bufferedGraphics.setColor(new Color(255, 255, 255));

                        /*auxBufferedGraphics/*/bufferedGraphics.fillRect(xBeginAux, yBeginAux, auxBarWidth, heightAux);

                        xAux ++;
                    }
                }
                //added for aux arrays
                /*
                if (usesAuxArray) {
                for (int x = 0; x < NUM_AUX_BARS; x++) {
                double currentValue = getAuxValue(x) + 1;
                double percentOfMax = currentValue / NUM_BARS;
                double heightPercentOfPanel = percentOfMax * BAR_HEIGHT_PERCENT/2;

                int height = (int) (heightPercentOfPanel * (double) getHeight()) + (int)(BAR_HEIGHT_PERCENT*(double)getHeight() / 2.0);
                int xBegin = x + (barWidth - 1) * x;
                int yBegin = (getHeight() - height) + (int)(BAR_HEIGHT_PERCENT*(double)getHeight() / 2.0); //start of the aux bar + height of the main array

                bufferedGraphics.setColor(new Color(255, 255, 255));

                bufferedGraphics.fillRect(xBegin, yBegin, barWidth, height);
                }
                }*/
            }
            finally
            {
                if(bufferedGraphics != null)
                {
                    bufferedGraphics.dispose();
                }
            }

            panelGraphics.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
        }
    }

    private void makeBufferedImageTransparent(BufferedImage image)
    {
        Graphics2D bufferedGraphics = null;
        try
        {
            bufferedGraphics = image.createGraphics();

            bufferedGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            bufferedGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            bufferedGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }
        finally
        {
            if(bufferedGraphics != null)
            {
                bufferedGraphics.dispose();
            }
        }
    }

    @Override
    public void setName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setAlgorithm(ISortAlgorithm algorithm) {
        this.algorithm = algorithm;
        algorithmDelay = algorithm.getDelay();
        spinner.setValue((int) algorithm.getDelay());

        //added for aux arrays
        usesAuxArray = algorithm.hasAuxArray();
    }

    public long getAlgorithmDelay(){
        return algorithmDelay;
    }
}
