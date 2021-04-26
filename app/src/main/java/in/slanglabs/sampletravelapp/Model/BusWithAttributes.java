package in.slanglabs.sampletravelapp.Model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class BusWithAttributes {
    @Embedded
    public Bus bus;
    @Relation(
            parentColumn = "id",
            entityColumn = "busId"
    )
    public List<BusAttributes> busAttributesList;
}
