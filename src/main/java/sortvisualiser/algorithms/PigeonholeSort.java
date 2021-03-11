package sortvisualiser.algorithms;

import sortvisualiser.SortArray;
import sortvisualiser.Util.*;

/**
 * Pigeonhole sort implementation
 *
 * @author Anthony Michael
 */
public class PigeonholeSort implements ISortAlgorithm
{
    private long stepDelay = 5;

    @Override 
    public boolean hasAuxArray() {return true;}
    
    @Override
    public String getName() 
    {
        return "Pigeonhole Sort";
    }
    
    @Override
    public long getDelay()
    {
        return stepDelay;
    }

    @Override
    public void setDelay(long delay) 
    {		
        stepDelay = delay;
    }

    @Override

    /**
     * This is an implementation of a stable version of Counting Sort. 
     * It counts every instance of every number and adds them back in order.
     *
     * @param array the array to be sorted
     */	
    public void runSort(SortArray array)
    {
        //find the min and max values in the array
        int min = array.getValue(Util.findMinValueIndex(array));
        int max = array.getValue(Util.findMaxValueIndex(array));
        
        //find range
        int range = max - min + 1;
        
        //sorting stuff 
        array.initAuxArray(range);
        
        //for visualization, maybe swap the usage of phole and the array?
        
        for(int i = 0; i < array.arraySize(); i++) array.updateAuxSingle(array.getValue(i)-min, array.getAuxValue(i) + 1, getDelay(), true);
        
        
        int index = 0;
        for(int i = 0; i < range; i++) {
            while (array.getAuxValue(i) > 0) {
                array.updateSingle(index++, i+min, getDelay(), true);
                array.updateAuxSingle(i, array.getAuxValue(i) - 1, getDelay(), true);
            }
        }
    }
}
