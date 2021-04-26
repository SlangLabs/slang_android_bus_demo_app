package in.slanglabs.sampletravelapp.Model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static in.slanglabs.sampletravelapp.Model.RouteStatus.CANCELED;
import static in.slanglabs.sampletravelapp.Model.RouteStatus.DELAYED;

@IntDef({CANCELED, DELAYED})
@Retention(RetentionPolicy.SOURCE)
public @interface RouteStatus {
    int CANCELED = 1;
    int DELAYED = 2;
}