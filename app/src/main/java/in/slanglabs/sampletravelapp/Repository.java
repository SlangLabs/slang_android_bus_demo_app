package in.slanglabs.sampletravelapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import in.slanglabs.assistants.travel.SearchInfo;
import in.slanglabs.sampletravelapp.Model.BusFilterSortOptions;
import in.slanglabs.sampletravelapp.Model.BusWithAttributes;
import in.slanglabs.sampletravelapp.Model.Journey;
import in.slanglabs.sampletravelapp.Model.JourneyBusPlace;
import in.slanglabs.sampletravelapp.Model.JourneyBusPlaceOrder;
import in.slanglabs.sampletravelapp.Model.OrderBy;
import in.slanglabs.sampletravelapp.Model.OrderItem;
import in.slanglabs.sampletravelapp.Model.OrderStatus;
import in.slanglabs.sampletravelapp.Model.Place;
import in.slanglabs.sampletravelapp.Model.RouteStatus;
import in.slanglabs.sampletravelapp.Model.SearchItem;
import in.slanglabs.sampletravelapp.Model.TimeRange;
import in.slanglabs.sampletravelapp.Slang.SlangInterface;
import in.slanglabs.sampletravelapp.UI.Activity.MainActivity;
import in.slanglabs.sampletravelapp.UI.Activity.SearchBusActivity;
import in.slanglabs.sampletravelapp.db.AppDatabase;

public class Repository {

    private static final String TAG = Repository.class.getSimpleName();
    private static Repository shared;

    private List<String> busTypes =
            new ArrayList<>();

    private List<TimeRange> timeRanges =
            new ArrayList<>();

    private AppDatabase mDatabase;
    private final AppExecutors appExecutors;

    private final SlangInterface slangInterface;

    private final SingleLiveEvent<Pair<Class, Bundle>> activityToStart =
            new SingleLiveEvent<>();


    private MutableLiveData<Place> sourcePlace =
            new MutableLiveData<>();

    private MutableLiveData<Place> destinationPlace =
            new MutableLiveData<>();

    private MutableLiveData<Long> travelDate =
            new MutableLiveData<>();

    private SearchItem searchItem = new SearchItem();

    private Repository(Context context, final AppDatabase database, final AppExecutors appExecutors, final SlangInterface slangInterface) {
        this.mDatabase = database;
        this.appExecutors = appExecutors;
        this.slangInterface = slangInterface;

        busTypes.add("A/C");
        busTypes.add("Non A/C");

        timeRanges.add(new TimeRange(1800000, 23340000));
        timeRanges.add(new TimeRange(23400000, 44940000));
        timeRanges.add(new TimeRange(45000000, 66540000));
        timeRanges.add(new TimeRange(-19800000, 1740000));

        Random rng = new Random();
        final LiveData<List<Journey>> journies = mDatabase.journayDao().getAllJournies();
        Observer observer = new Observer<List<Journey>>() {
            @Override
            public void onChanged(List<Journey> journeys) {
                if (journeys.size() > 0) {
                    List<Integer> generated = new ArrayList<Integer>();
                    int totalNumberOfOffers = (int) (journeys.size() * 0.3);
                    for (int i = 0; i < totalNumberOfOffers; i++) {
                        while (true) {
                            Integer next = rng.nextInt(journeys.size());
                            if (!generated.contains(next)) {
                                generated.add(next);
                                break;
                            }
                        }
                    }
                    int counter;
                    for (counter = 0; counter < generated.size(); counter++) {
                        int randomNumber = generated.get(counter);
                        Journey journey = journeys.get(randomNumber);
                        Log.d("JourneyId", String.valueOf(journey.journeyId));
                        if (randomNumber % 2 == 0) {
                            appExecutors.diskIO().execute(() -> {
                                mDatabase.journayDao().update(RouteStatus.DELAYED, journey.journeyId);
                            });
                        } else {
                            appExecutors.diskIO().execute(() -> {
                                mDatabase.journayDao().update(RouteStatus.CANCELED, journey.journeyId);
                            });
                        }
                    }
                }
                journies.removeObserver(this);
            }
        };
        journies.observeForever(observer);
    }

    static Repository getInstance(final Context context, final AppDatabase mDatabase, final AppExecutors appExecutors, final SlangInterface slangInterface) {
        if (shared == null) {
            synchronized (Repository.class) {
                if (shared == null) {
                    shared = new Repository(context, mDatabase, appExecutors, slangInterface);
                }
            }
        }
        return shared;
    }

    public LiveData<JourneyBusPlace> getJourneyItem(long journeyId) {
        return mDatabase.journayDao().loadJourney(journeyId);
    }

    public LiveData<List<JourneyBusPlace>> getItemsForNameOrderBy(
            String startLocation,
            String endLocation,
            Date startDate,
            List<String> filters,
            List<String> busType,
            List<String> busOperators,
            List<TimeRange> departureTimeRange,
            List<TimeRange> arrivalTimeRange,
            @OrderBy int orderBy) {

        StringBuilder stringBuilder = new StringBuilder();
        List<Object> args = new ArrayList();
        stringBuilder.append("SELECT * FROM journey");
        stringBuilder.append(" JOIN bus ON bus.id = journey.busId JOIN busAttributes ON busAttributes.busId = journey.busId");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" startLocation =?");
        args.add("place1");
        stringBuilder.append(" AND endLocation =?");
        args.add("place2");

        if (startDate != null) {
            stringBuilder.append(" AND");
            stringBuilder.append(" startTime >= ?");
            args.add(startDate.getTime());
            args.addAll(filters);
        }

        if (!filters.isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" travelClass IN (");
            appendPlaceholders(stringBuilder, filters.size());
            stringBuilder.append(")");
            args.addAll(filters);
        }
        if (!busType.isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" type IN (");
            appendPlaceholders(stringBuilder, busType.size());
            stringBuilder.append(")");
            args.addAll(busType);
        }
        if (!busOperators.isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" travels IN (");
            appendPlaceholders(stringBuilder, busOperators.size());
            stringBuilder.append(")");
            args.addAll(busOperators);
        }

        if (!departureTimeRange.isEmpty()) {
            stringBuilder.append(" AND (");
            for (int i = 0; i < departureTimeRange.size(); i++) {
                TimeRange timeRange = departureTimeRange.get(i);
                if (i != 0) {
                    stringBuilder.append(" OR");
                }
                stringBuilder.append(" (startTime >= ?");
                args.add(timeRange.getStartTime());
                stringBuilder.append("AND startTime <= ?)");
                args.add(timeRange.getEndTime());
            }
            stringBuilder.append(")");
        }

        if (!arrivalTimeRange.isEmpty()) {
            stringBuilder.append(" AND (");
            for (int i = 0; i < arrivalTimeRange.size(); i++) {
                TimeRange timeRange = arrivalTimeRange.get(i);
                if (i != 0) {
                    stringBuilder.append(" OR");
                }
                stringBuilder.append(" (endTime >= ?");
                args.add(timeRange.getStartTime());
                stringBuilder.append("AND endTime <= ?)");
                args.add(timeRange.getEndTime());
            }
            stringBuilder.append(")");
        }

        stringBuilder.append(" GROUP BY id");
        if (orderBy == OrderBy.PRICE) {
            stringBuilder.append(" ORDER BY journey.price ASC");
        } else if (orderBy == OrderBy.RATING) {
            stringBuilder.append(" ORDER BY bus.starRating DESC");
        } else if (orderBy == OrderBy.DEPARTURE_TIME) {
            stringBuilder.append(" ORDER BY startTime ASC");
        } else if (orderBy == OrderBy.TRAVEL_DURATION) {
            stringBuilder.append(" ORDER BY duration ASC");
        } else if (orderBy == OrderBy.RELEVANCE) {
            stringBuilder.append(" ORDER BY travels ASC");
        }
        stringBuilder.append(";");
        return mDatabase.journayDao().getJourneyRawQuery(new SimpleSQLiteQuery(stringBuilder.toString(), args.toArray()));
    }

    //Order Related Opertions/Methods
    public LiveData<JourneyBusPlaceOrder> getOrderItem(String orderItemId) {
        return mDatabase.orderDao().loadOrder(orderItemId);
    }

    public void addOrderItem(OrderItem item) {
        appExecutors.diskIO().execute(() -> {
            mDatabase.orderDao().insert(item);
        });
    }

    public void removeOrderItem(OrderItem item) {
        appExecutors.diskIO().execute(() -> {
            mDatabase.orderDao().update(OrderStatus.CANCELED, item.orderId);
        });
    }

    //Slang callback handlers
    public void onSearch(SearchItem searchInfo) {

        this.searchItem = searchInfo;
        Place sourcePlace = new Place();
        sourcePlace.city = searchInfo.sourcePlace.city;
        sourcePlace.stop = searchInfo.sourcePlace.stop;
        sourcePlace.stateFullName = searchInfo.sourcePlace.state;
        String sourceName = (sourcePlace.city == null ? "" : sourcePlace.city) + " " + (sourcePlace.stop == null ? "" : sourcePlace.stop) + " " + (sourcePlace.stateFullName == null ? "" : (sourcePlace.stateFullName.equalsIgnoreCase("dummy_state") ? "" : sourcePlace.stateFullName));


        if (sourceName.replaceAll("\\s", "").equalsIgnoreCase("")) {

            //Source place is empty, notify that the source is invalid
            slangInterface.notifySourceInvalid();
            return;
        }

        appExecutors.diskIO().execute(() -> {

            //Query the db to find source places
            List<Place> sourcePlaces = mDatabase.placeDao().getPlacesBasedOnSearchSync(fixQuery(sourceName));
            if (sourcePlaces.size() == 0) {

                //Source places is empty for the current source, report source invalid.
                slangInterface.notifySourceInvalid();
                return;
            }
            if (sourcePlaces.size() > 1) {

                //There are multiple source places for the current source, report source is ambiguous.
                appExecutors.mainThread().execute(() -> slangInterface.notifySourceAmbiguous());
                Repository.this.sourcePlace.postValue(sourcePlace);

                //Additionally, show source places dropdown in the UI to select an appropriate one.
                Bundle bundle = new Bundle();
                bundle.putBoolean("disambiguateSource", true);
                appExecutors.mainThread().execute(() -> activityToStart.setValue(new Pair<>(MainActivity.class, bundle)));
                return;
            }

            //There is only one source place for the current source, hence we choose that.
            Repository.this.sourcePlace.postValue(sourcePlaces.get(0));

            //We move on to destination place resolution.
            Place destinationPlace = new Place();
            destinationPlace.city = searchInfo.destinationPlace.city;
            destinationPlace.stop = searchInfo.destinationPlace.stop;
            destinationPlace.stateFullName = searchInfo.destinationPlace.state;
            String destinationName = (destinationPlace.city == null ? "" : destinationPlace.city) + " " + (destinationPlace.stop == null ? "" : destinationPlace.stop) + " " + (destinationPlace.stateFullName == null ? "" : (destinationPlace.stateFullName.equalsIgnoreCase("dummy_state") ? "" : destinationPlace.stateFullName));

            if (destinationName.replaceAll("\\s", "").equalsIgnoreCase("")) {

                //Destination place is empty, notify that the destination is invalid
                slangInterface.notifyDestinationInvalid();
                return;
            }

            //Query the db to find destination places
            List<Place> destinationPlaces = mDatabase.placeDao().getPlacesBasedOnSearchSync(fixQuery(destinationName));

            if (destinationPlaces.size() == 0) {

                //Destination places is empty for the current destination, report destination invalid.
                slangInterface.notifyDestinationInvalid();
                return;
            }
            if (destinationPlaces.size() > 1) {

                //There are multiple destination places for the current destination, report destination is ambiguous.
                appExecutors.mainThread().execute(() -> slangInterface.notifyDestinationAmbiguous());

                //Additionally, show destination places dropdown in the UI to select an appropriate one.
                Repository.this.destinationPlace.postValue(destinationPlace);
                Bundle bundle = new Bundle();
                bundle.putBoolean("disambiguateDestination", true);
                appExecutors.mainThread().execute(()
                        -> activityToStart.setValue(new Pair<>(MainActivity.class, bundle)));
                return;
            }

            //There is only one destination place for the current destination, hence we choose that.
            Repository.this.destinationPlace.postValue(destinationPlaces.get(0));

            //We move on to onwardDate resolution.
            Date date = searchInfo.travelDate;

            //We validate the current date to match the valid dates supported by the current app.
            if (!validateDate(date)) {
                if (date == null) {

                    //Onward date is null, report onward date not specified.
                    slangInterface.notifySearchOnwardDateNotSpecified();
                } else {

                    //Onward date is not a valid date, report onward date invalid.
                    slangInterface.notifySearchOnwardDateInvalid();
                }

                //Additionally, show the date picker UI to selected an appropriate date.
                Bundle bundle = new Bundle();
                bundle.putBoolean("disambiguateDate", true);
                appExecutors.mainThread().execute(()
                        -> activityToStart.setValue(new Pair<>(MainActivity.class, bundle)));
                return;
            }

            //All the required fields are available now, we use the required fields and move to the SearchBusActivity.
            appExecutors.mainThread().execute(() -> {

                Repository.this.sourcePlace.postValue(sourcePlaces.get(0));
                Repository.this.destinationPlace.postValue(destinationPlaces.get(0));
                travelDate.postValue(date.getTime());

                Bundle bundle = new Bundle();
                bundle.putSerializable("startLoc", sourcePlaces.get(0));
                bundle.putSerializable("endLoc", destinationPlaces.get(0));
                bundle.putLong("date", date.getTime());
                BusFilterSortOptions busFilterSortOptions = new BusFilterSortOptions();
                List<String> busTypes = new ArrayList<>();
                List<String> busFilters = new ArrayList<>();
                busFilterSortOptions.setBusType(busTypes);
                busFilterSortOptions.setBusFilters(busFilters);
                bundle.putBoolean("isVoice", true);
                bundle.putParcelable("busFilterOptions", busFilterSortOptions);
                activityToStart.setValue(new Pair<>(SearchBusActivity.class, bundle));
            });
        });
    }

    public void onSearch(SearchInfo searchInfo) {

        SearchItem searchItem = new SearchItem();

        Place sourcePlace = new Place();
        sourcePlace.city = searchInfo.getSource().getCity();
        sourcePlace.stop = searchInfo.getSource().getTerminal();
        sourcePlace.state = searchInfo.getSource().getProvince();
        sourcePlace.stateFullName = searchInfo.getSource().getProvince();

        Place destinationPlace = new Place();
        destinationPlace.city = searchInfo.getDestination().getCity();
        destinationPlace.stop = searchInfo.getDestination().getTerminal();
        destinationPlace.state = searchInfo.getDestination().getProvince();
        destinationPlace.stateFullName = searchInfo.getSource().getProvince();

        searchItem.travelDate = searchInfo.getOnwardDate();
        searchItem.sourcePlace = sourcePlace;
        searchItem.destinationPlace = destinationPlace;

        onSearch(searchItem);
    }


    //Getters
    public LiveData<List<JourneyBusPlaceOrder>> getOrderItems() {
        return mDatabase.orderDao().loadAllOrders();
    }

    public LiveData<List<Journey>> getAllJournies() {
        return mDatabase.journayDao().getAllJournies();
    }

    public List<String> getBusTypes() {
        return busTypes;
    }

    public List<TimeRange> getTimeRanges() {
        return timeRanges;
    }

    public LiveData<List<BusWithAttributes>> getAllBuses() {
        return mDatabase.busDao().getAllBuses();
    }

    public SlangInterface getSlangInterface() {
        return slangInterface;
    }

    public SingleLiveEvent<Pair<Class, Bundle>> getActivityToStart() {
        return activityToStart;
    }

    public LiveData<List<String>> getAllBusAttributes() {
        return mDatabase.busAttributeDao().getAllBusAttributes();
    }

    public LiveData<List<Place>> getPlacesForName(String name) {
        return mDatabase.placeDao().getPlacesBasedOnSearch(fixQuery(name));
    }

    private static String fixQuery(String query) {
        String queryString = query.replaceAll("[-,]", " ");
        return queryString.trim().replaceAll("-,\\s+", "*") + "*";
    }

    //Helpers
    public static void appendPlaceholders(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append("?");
            if (i < count - 1) {
                builder.append(",");
            }
        }
    }

    private boolean validateDate(Date date) {
        if (date == null) {
            return false;
        }
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        Calendar minDate = Calendar.getInstance();
        minDate.set(mYear, mMonth, mDay);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(mYear, mMonth, mDay + 30);

        return date.getTime() >= minDate.getTimeInMillis() && date.getTime()
                <= maxDate.getTimeInMillis();
    }

    public MutableLiveData<Place> getSourcePlace() {
        return sourcePlace;
    }

    public MutableLiveData<Place> getDestinationPlace() {
        return destinationPlace;
    }

    public MutableLiveData<Long> getTravelDate() {
        return travelDate;
    }

    public void setSourcePlace(Place place) {
        sourcePlace.postValue(place);
    }

    public void setDestinationPlace(Place place) {
        destinationPlace.postValue(place);
    }

    public void setTravelDate(long date) {
        travelDate.postValue(date);
    }

    public void notifySearchSourceLocationDisambiguated(Place place) {

        //Update the Assistant SearchContext with the current selected source place.
        slangInterface.setSourceSearchContext(place);

        //Continue with the resolution process, with the current selected source place.
        searchItem.sourcePlace = place;
        onSearch(searchItem);
    }

    public void notifySearchDestinationLocationDisambiguated(Place place) {

        //Update the Assistant SearchContext with the current selected destination place.
        slangInterface.setDestinationSearchContext(place);

        //Continue with the resolution process, with the current selected destination place.
        searchItem.destinationPlace = place;
        onSearch(searchItem);
    }

    public void notifySearchDateDisambiguated(Date date) {

        Repository.this.travelDate.postValue(date.getTime());

        //Update the Assistant SearchContext with the current selected onward date.
        slangInterface.setTravelDateSearchContext(date);

        //Continue with the resolution process, with the current selected onward date.
        searchItem.travelDate = date;
        onSearch(searchItem);
    }

}
