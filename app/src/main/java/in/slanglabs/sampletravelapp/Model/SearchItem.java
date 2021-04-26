package in.slanglabs.sampletravelapp.Model;

import java.io.Serializable;
import java.util.Date;

public class SearchItem implements Serializable {
    public Place sourcePlace;
    public Place destinationPlace;
    public Date travelDate;
}
