package in.slanglabs.sampletravelapp.Slang;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import in.slanglabs.assistants.travel.AssistantConfiguration;
import in.slanglabs.assistants.travel.AssistantError;
import in.slanglabs.assistants.travel.NavigationInfo;
import in.slanglabs.assistants.travel.NavigationUserJourney;
import in.slanglabs.assistants.travel.SearchInfo;
import in.slanglabs.assistants.travel.SearchUserJourney;
import in.slanglabs.assistants.travel.SlangTravelAssistant;
import in.slanglabs.assistants.travel.Station;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.sampletravelapp.App;
import in.slanglabs.sampletravelapp.Model.Place;
import in.slanglabs.sampletravelapp.Repository;

public class SlangInterface {
    private final String TAG = "SlangInterface";
    private final Application application;
    private SearchUserJourney searchUserJourney;

    public void init(String assitantId, String apiKey) {
        HashSet<Locale> requestedLocales = new HashSet<>();
        requestedLocales.add(SlangLocale.LOCALE_ENGLISH_IN);
        requestedLocales.add(SlangLocale.LOCALE_HINDI_IN);

        AssistantConfiguration configuration = new AssistantConfiguration.Builder()
                .setRequestedLocales(requestedLocales)
                .setAssistantId(assitantId)
                .setAPIKey(apiKey)
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                .setEnvironment(SlangTravelAssistant.Environment.STAGING)
                .build();

        SlangTravelAssistant.initialize(application, configuration);
    }

    public SlangInterface(Application application) {
        this.application = application;
        SlangTravelAssistant.setAction(new SlangTravelAssistant.Action() {

            @Override
            public SearchUserJourney.AppState onSearch(SearchInfo searchInfo, SearchUserJourney searchUserJourney) {
                SlangInterface.this.searchUserJourney = searchUserJourney;
                new Handler().post(() -> {
                    //Get instance of the repository and perform the logic for this user journey.
                    Repository repository = ((App) (application)).getRepository();
                    repository.onSearch(searchInfo);
                });
                return SearchUserJourney.AppState.WAITING;
            }

            @Override
            public NavigationUserJourney.AppState onNavigation(NavigationInfo navigationInfo, NavigationUserJourney navigationUserJourney) {
                return null;
            }

            @Override
            public void onAssistantError(AssistantError assistantError) {
                Log.e(TAG, "Error: " + assistantError.getDescription());
            }
        });
        SlangTravelAssistant.setLifecycleObserver(new SlangTravelAssistant.LifecycleObserver() {
            @Override
            public void onAssistantInitSuccess() {

            }

            @Override
            public void onAssistantInitFailure(String s) {
                Log.d(TAG, "Initialization failure : " + s);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(application.getApplicationContext(),
                                "Initialization failure : " + s,
                                Toast.LENGTH_LONG).show());
            }

            @Override
            public void onAssistantInvoked() {
                SearchUserJourney.getContext().clear();
            }

            @Override
            public void onAssistantClosed(boolean b) {
                SearchUserJourney.getContext().clear();
            }

            @Override
            public void onAssistantLocaleChanged(Locale locale) {

            }

            @Override
            public Status onUnrecognisedUtterance(String s) {
                return Status.FAILURE;
            }

            @Override
            public void onUtteranceDetected(String s) {

            }

            @Override
            public void onOnboardingSuccess() {

            }

            @Override
            public void onOnboardingFailure() {

            }
        });
    }

    public void showTrigger(Activity activity) {
        SlangTravelAssistant.getUI().showTrigger(activity);
    }

    public void hideTrigger(Activity activity) {
        SlangTravelAssistant.getUI().hideTrigger(activity);
    }

    //This method is to notify SlangTravelAssistant that the search async operation was successful.
    public void notifySearchSuccess() {
        if (searchUserJourney == null) return;
        try {
            searchUserJourney.setSuccess();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
            SearchUserJourney.getContext().clear();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        searchUserJourney = null;
    }

    //This method is to notify SlangTravelAssistant that the search async operation was not successful.
    public void notifySearchFailure() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setFailure();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        searchUserJourney = null;
    }

    //This method is to notify SlangTravelAssistant that the search async operation results in routes being empty.
    public void notifySearchNoRoutesAvailable() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setRouteNotFound();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        searchUserJourney = null;
    }

    //This method is to notify SlangTravelAssistant that the search async operation resulted in source being invalid.
    public void notifySourceInvalid() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setSourceInvalid();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangTravelAssistant that the search async operation resulted in source being ambiguous.
    public void notifySourceAmbiguous() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setSourceAmbiguous();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangTravelAssistant that the search async operation resulted in destination being invalid.
    public void notifyDestinationInvalid() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setDestinationInvalid();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangTravelAssistant that the search async operation resulted in destination being ambiguous.
    public void notifyDestinationAmbiguous() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setDestinationAmbiguous();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangTravelAssistant that the search async operation resulted in date being invalid.
    public void notifySearchOnwardDateInvalid() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setOnwardDateInvalid();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangTravelAssistant that the search async operation resulted in date not specified.
    public void notifySearchOnwardDateNotSpecified() {
        if (searchUserJourney == null) return;
        try {

            //Notify the current appropriate app state for search.
            searchUserJourney.setNeedOnwardDate();
            searchUserJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to set source item in the current search context.
    public void setSourceSearchContext(Place place) {
        if (searchUserJourney == null) return;
        try {
            Station station = new Station.Builder().setCity(place.city)
                    .setTerminal(place.stop)
                    .setProvince(place.stateFullName)
                    .build();

            SearchUserJourney.getContext().setSource(station);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to set destination item in the current search context.
    public void setDestinationSearchContext(Place place) {
        if (searchUserJourney == null) return;
        try {
            Station station = new Station.Builder().setCity(place.city)
                    .setTerminal(place.stop)
                    .setProvince(place.stateFullName)
                    .build();

            SearchUserJourney.getContext().setDestination(station);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to set onward date item in the current search context.
    public void setTravelDateSearchContext(Date date) {
        if (searchUserJourney == null) return;
        try {
            SearchUserJourney.getContext().setOnwardDate(date);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

}