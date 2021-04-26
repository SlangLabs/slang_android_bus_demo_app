package in.slanglabs.sampletravelapp.UI.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import in.slanglabs.sampletravelapp.Model.BusFilterSortOptions;
import in.slanglabs.sampletravelapp.Model.Place;
import in.slanglabs.sampletravelapp.R;
import in.slanglabs.sampletravelapp.UI.Fragment.SearchDialogFragment;
import in.slanglabs.sampletravelapp.UI.ViewModel.AppViewModel;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button search_buses;
    private Place sourcePlace = new Place();
    private Place destinationPlace = new Place();
    private long selectedDateInt;
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
    private AppViewModel appViewModel;
    private TextView destinationTextField;
    private TextView sourceTextField;
    private TextView dateTextField;
    boolean disambiguateSource;
    boolean disambiguateDestination;
    boolean disambiguateDate;
    SearchDialogFragment newFragment;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        appViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);

        ImageView myBookingsButton = findViewById(R.id.my_booking_button);
        myBookingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, OrderActivity.class);
            startActivity(intent);
        });

        search_buses = findViewById(R.id.search_buses);

        destinationTextField = findViewById(R.id.destination_text_field);
        destinationTextField.setOnClickListener(view -> showDialog(destinationTextField, destinationPlace, ""));
        sourceTextField = findViewById(R.id.source_text_field);
        sourceTextField.setOnClickListener(view -> showDialog(sourceTextField, sourcePlace, ""));

        dateTextField = findViewById(R.id.date_text_field);

        dateTextField.setOnClickListener(view -> {
            showDatePicker();
        });

        search_buses.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SearchBusActivity.class);
            if (TextUtils.isEmpty(sourcePlace.id) || TextUtils.isEmpty(destinationPlace.id)) {
                return;
            }
            if (sourcePlace.id.equals(destinationPlace.id)) {
                return;
            }
            intent.putExtra("startLoc", (Serializable) sourcePlace);
            intent.putExtra("endLoc", (Serializable) destinationPlace);
            intent.putExtra("date", selectedDateInt);
            BusFilterSortOptions busFilterSortOptions = new BusFilterSortOptions();
            List<String> busTypes = new ArrayList<>();
            List<String> busFilters = new ArrayList<>();
            busFilterSortOptions.setBusType(busTypes);
            busFilterSortOptions.setBusFilters(busFilters);
            intent.putExtra("busFilterOptions", busFilterSortOptions);
            startActivity(intent);
        });

        appViewModel.getActivityToStart().observe(this, classBundlePair -> {
            Intent intent = new Intent(MainActivity.this,
                    classBundlePair.first);
            if (classBundlePair.second != null) {
                intent.putExtras(classBundlePair.second);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        appViewModel.getSourcePlace().observe(this, place -> {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (newFragment != null) {
                newFragment.dismiss();
                newFragment = null;
            }
            if (place != null) {
                MainActivity.this.sourcePlace = place;
                if(!place.isComplete()) {
                    return;
                }
                String placeString = "";
                if (place.city != null) {
                    placeString = place.city;
                }
                if (place.stop != null) {
                    if (placeString.length() > 0) {
                        placeString += ", ";
                    }
                    placeString += place.stop;
                }
                if (place.state != null) {
                    if (placeString.length() > 0) {
                        placeString += " ";
                    }
                    placeString += " (" + place.state + ")";
                }
                sourceTextField.setText(placeString);
            }
        });

        appViewModel.getDestinationPlace().observe(this, place -> {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (newFragment != null) {
                newFragment.dismiss();
                newFragment = null;
            }
            if (place != null) {
                MainActivity.this.destinationPlace = place;
                if(!place.isComplete()) {
                    return;
                }
                String placeString = "";
                if (place.city != null) {
                    placeString = place.city;
                }
                if (place.stop != null) {
                    if (placeString.length() > 0) {
                        placeString += ", ";
                    }
                    placeString += place.stop;
                }
                if (place.state != null) {
                    if (placeString.length() > 0) {
                        placeString += " ";
                    }
                    placeString += " (" + place.state + ")";
                }
                destinationTextField.setText(placeString);
            }
        });

        appViewModel.getTravelDate().observe(this, aLong -> {
            if (datePickerDialog != null) {
                datePickerDialog.dismiss();
                datePickerDialog = null;
            }
            if (aLong != null) {
                String s1 = sdf.format(aLong);
                dateTextField.setText(s1);
                selectedDateInt = aLong;
            }
        });

        String s1 = sdf.format(new Date().getTime());
        dateTextField.setText(s1);
        selectedDateInt = new Date().getTime();

        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        disambiguateSource = intent.getBooleanExtra("disambiguateSource", false);
        if (disambiguateSource) {

            //Source disambiguation is in process, hence show the source places dialog for the current source term.
            showDialog(sourceTextField, this.sourcePlace, getPlaceString(sourcePlace));
        }

        disambiguateDestination = intent.getBooleanExtra("disambiguateDestination", false);
        if (disambiguateDestination) {

            //Destination disambiguation is in process, hence show the destination places dialog for the current destination term.
            showDialog(destinationTextField, this.destinationPlace, getPlaceString(destinationPlace));
        }

        disambiguateDate = intent.getBooleanExtra("disambiguateDate", false);
        if (disambiguateDate) {

            //Onward date disambiguation is in process, hence show the date picker.
            showDatePicker();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appViewModel.getSlangInterface().showTrigger(this);
    }

    protected void showDialog(TextView textView, Place placeSourceDestination, String searchString) {
        if (newFragment != null) {
            newFragment.dismiss();
            newFragment = null;
        }
        newFragment = SearchDialogFragment.newInstance(searchString);
        newFragment.viewItemListener = place -> {
            if (disambiguateSource) {

                //If source disambiguation is in progress, report that the appropriate source has been chosen from the dropdown.
                appViewModel.notifySearchSourceLocationDisambiguated(place);

                disambiguateSource = false;
                return;
            } else if (disambiguateDestination) {

                //If destination disambiguation is in progress, report that the appropriate destination has been chosen from the dropdown.
                appViewModel.notifySearchDestinationLocationDisambiguated(place);

                disambiguateDestination = false;
                return;
            }
            textView.setText(place.city + ", " + place.stop + " (" + place.state + ")");
            placeSourceDestination.id = place.id;
            placeSourceDestination.city = place.city;
            placeSourceDestination.state = place.state;
            placeSourceDestination.stop = place.stop;
        };
        newFragment.show(getSupportFragmentManager(), "autoCompleteFragment");
    }

    protected void showDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        Calendar minDate = Calendar.getInstance();
        minDate.set(mYear, mMonth, mDay);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(mYear, mMonth, mDay + 30);

        if (datePickerDialog != null) {
            datePickerDialog.dismiss();
            datePickerDialog = null;
        }

        datePickerDialog = new DatePickerDialog(MainActivity.this,
                (view1, year, monthOfYear, dayOfMonth) -> {
                    String s = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = formatter.parse(s);
                        if (disambiguateDate) {

                            //If onward date disambiguation is in progress, report that the appropriate date has been chosen.
                            appViewModel.notifySearchDateDisambiguated(date);

                            disambiguateDate = false;
                        }
                        String dateString = sdf.format(date.getTime());
                        selectedDateInt = date.getTime();
                        dateTextField.setText(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }, mYear, mMonth, mDay);

        final DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(minDate.getTimeInMillis());
        datePicker.setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private String getPlaceString(Place place) {
        String placeString = "";
        if (place.city != null) {
            placeString = place.city;
        }
        if (place.stop != null) {
            if (placeString.length() > 0) {
                placeString += ", ";
            }
            placeString += place.stop;
        }
        if (place.state != null) {
            if (placeString.length() > 0) {
                placeString += " ";
            }
            placeString += place.state;
        }
        return placeString;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (newFragment != null) {
            newFragment.dismiss();
            newFragment = null;
        }
    }
}
