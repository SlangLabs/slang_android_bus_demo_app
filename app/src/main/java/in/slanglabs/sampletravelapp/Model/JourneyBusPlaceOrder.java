package in.slanglabs.sampletravelapp.Model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class JourneyBusPlaceOrder {
    @Embedded
    public OrderItem order;
    @Relation(
            parentColumn = "journeyId",
            entityColumn = "journeyId",
            entity = Journey.class
    )
    public JourneyBusPlace journey;
}
