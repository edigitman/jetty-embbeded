package pack;

import pack.model.Crop;

import java.util.Comparator;

/**
 * Created by gitmaal on 01/07/2015.
 */
public class CropComparator implements Comparator<Crop> {
    @Override
    public int compare(Crop c1, Crop c2) {

        if(c1.getType()==null){
            return -1;
        }else {
            if(c2.getType()==null){
                return 1;
            }
        }

        return c1.getType().compareTo(c2.getType());
    }
}
