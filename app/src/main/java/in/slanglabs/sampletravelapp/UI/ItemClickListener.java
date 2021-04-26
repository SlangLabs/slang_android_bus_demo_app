package in.slanglabs.sampletravelapp.UI;


import in.slanglabs.sampletravelapp.Model.JourneyBusPlace;

public interface ItemClickListener {
    default void itemClicked(int position) {
    }

    default void itemClicked(JourneyBusPlace journeyBusPlace) {
    }
}