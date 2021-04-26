package in.slanglabs.sampletravelapp.db.Convertors;

import androidx.room.TypeConverter;

import in.slanglabs.sampletravelapp.Model.JourneyBusPlace;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;

import java.io.IOException;
import java.util.Date;

public class JourneyBusPlaceConvertor {

    @TypeConverter
    public static JourneyBusPlace toJourneyBusPlace(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        Moshi moshi = new Moshi.Builder()
                .add(Date.class, new Rfc3339DateJsonAdapter())
                .build();
        JsonAdapter<JourneyBusPlace> jsonAdapter = moshi.adapter(JourneyBusPlace.class);
        try {
            return jsonAdapter.fromJson(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String fromJourneyBusPlace(JourneyBusPlace journeyBusPlace) {
        Moshi moshi = new Moshi.Builder()
                .add(Date.class, new Rfc3339DateJsonAdapter())
                .build();
        JsonAdapter<JourneyBusPlace> jsonAdapter = moshi.adapter(JourneyBusPlace.class);
        return jsonAdapter.toJson(journeyBusPlace);
    }

}
