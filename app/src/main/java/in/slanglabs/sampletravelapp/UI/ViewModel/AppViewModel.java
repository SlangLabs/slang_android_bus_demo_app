package in.slanglabs.sampletravelapp.UI.ViewModel;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import in.slanglabs.sampletravelapp.App;
import in.slanglabs.sampletravelapp.Model.BusFilterSortOptions;
import in.slanglabs.sampletravelapp.Model.BusWithAttributes;
import in.slanglabs.sampletravelapp.Model.Journey;
import in.slanglabs.sampletravelapp.Model.JourneyBusPlace;
import in.slanglabs.sampletravelapp.Model.JourneyBusPlaceOrder;
import in.slanglabs.sampletravelapp.Model.OrderBy;
import in.slanglabs.sampletravelapp.Model.OrderItem;
import in.slanglabs.sampletravelapp.Model.Place;
import in.slanglabs.sampletravelapp.Model.TimeRange;
import in.slanglabs.sampletravelapp.Repository;
import in.slanglabs.sampletravelapp.SingleLiveEvent;
import in.slanglabs.sampletravelapp.Slang.SlangInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppViewModel extends AndroidViewModel {

    private static final String TAG = AppViewModel.class.getSimpleName();

    private Repository mRepository;
    private MediatorLiveData<List<JourneyBusPlace>> searchForStartStopMediator =
            new MediatorLiveData<>();

    private LiveData<List<JourneyBusPlace>> searchForStartEndLocation =
            new MutableLiveData<>();

    private BusFilterSortOptions busFilterSortOptions;

    public AppViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((App) application).getRepository();
        busFilterSortOptions = new BusFilterSortOptions();
    }

    public BusFilterSortOptions getBusFilterSortOptions() {
        return busFilterSortOptions;
    }

    public void setBusFilterSortOptions(BusFilterSortOptions busFilterSortOptions) {
        this.busFilterSortOptions = busFilterSortOptions;
    }

    public LiveData<List<Journey>> getAllJournies() {
        return mRepository.getAllJournies();
    }

    public LiveData<JourneyBusPlace> getJourneyItem(long journeyId) {
        return mRepository.getJourneyItem(journeyId);
    }

    public List<String> getBusTypes() {
        return mRepository.getBusTypes();
    }

    public List<TimeRange> getTimeRanges() {
        return mRepository.getTimeRanges();
    }

    public LiveData<List<BusWithAttributes>> getAllBuses() {
        return mRepository.getAllBuses();
    }

    public LiveData<List<String>> getAllBusAttributes() {
        return mRepository.getAllBusAttributes();
    }

    //Functions/Methods related to orders.
    public LiveData<List<JourneyBusPlaceOrder>> getOrderItems() {
        return mRepository.getOrderItems();
    }

    public LiveData<JourneyBusPlaceOrder> getOrderItem(String orderItemId) {
        return mRepository.getOrderItem(orderItemId);
    }

    public void addOrderItem(OrderItem orderItem) {
        mRepository.addOrderItem(orderItem);
    }

    public void removeOrderItem(OrderItem orderItem) {
        mRepository.removeOrderItem(orderItem);
    }


    //Functions/Methods related to search.
    public MediatorLiveData<List<JourneyBusPlace>> getSearchForStartStopLocationMediator() {
        return searchForStartStopMediator;
    }

    public void getItemsForNameOrderBy(Place startLocation,
                                       Place stopLocation,
                                       Date startDate,
                                       List<String> filters,
                                       List<String> busType,
                                       List<String> busOperators,
                                       List<TimeRange> departureTimeRange,
                                       List<TimeRange> arrivalTimeRange,
                                       @OrderBy int orderBy) {
        if (searchForStartEndLocation != null) {
            searchForStartStopMediator.removeSource(searchForStartEndLocation);
        }
        if(startLocation.city.equalsIgnoreCase(stopLocation.city)) {
            searchForStartStopMediator.postValue(new ArrayList<>());
            return;
        }
        searchForStartEndLocation = mRepository.getItemsForNameOrderBy(startLocation.id,
                stopLocation.id, startDate, filters, busType, busOperators, departureTimeRange, arrivalTimeRange, orderBy);
        searchForStartStopMediator.addSource(searchForStartEndLocation, itemOfferCarts
                -> searchForStartStopMediator.postValue(itemOfferCarts));
    }

    public LiveData<List<Place>> getPlacesForName(String search) {
        return mRepository.getPlacesForName(search);
    }

    public SingleLiveEvent<Pair<Class, Bundle>> getActivityToStart() {
        return mRepository.getActivityToStart();
    }

    public SlangInterface getSlangInterface() {
        return mRepository.getSlangInterface();
    }

    public LiveData<Place> getSourcePlace() {
        return mRepository.getSourcePlace();
    }

    public LiveData<Place> getDestinationPlace() {
        return mRepository.getDestinationPlace();
    }

    public LiveData<Long> getTravelDate() {
        return mRepository.getTravelDate();
    }

    public void setSourcePlace(Place place) {
        mRepository.setSourcePlace(place);
    }

    public void setDestinationPlace(Place place) {
        mRepository.setDestinationPlace(place);
    }

    public void setTravelDate(long date) {
        mRepository.setTravelDate(date);
    }

    public void notifySearchSourceLocationDisambiguated(Place place) {
        mRepository.notifySearchSourceLocationDisambiguated(place);
    }

    public void notifySearchDestinationLocationDisambiguated(Place place) {
        mRepository.notifySearchDestinationLocationDisambiguated(place);
    }

    public void notifySearchDateDisambiguated(Date date) {
        mRepository.notifySearchDateDisambiguated(date);
    }

}
