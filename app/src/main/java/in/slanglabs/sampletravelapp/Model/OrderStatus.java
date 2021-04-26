package in.slanglabs.sampletravelapp.Model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static in.slanglabs.sampletravelapp.Model.OrderStatus.CANCELED;
import static in.slanglabs.sampletravelapp.Model.OrderStatus.ON_SCHEDULE;

@IntDef({CANCELED, ON_SCHEDULE})
@Retention(RetentionPolicy.SOURCE)
public @interface OrderStatus {
    int CANCELED = 0;
    int ON_SCHEDULE = 1;
}