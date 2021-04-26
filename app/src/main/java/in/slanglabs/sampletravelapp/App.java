package in.slanglabs.sampletravelapp;

import android.app.Application;

import in.slanglabs.sampletravelapp.Slang.SlangInterface;
import in.slanglabs.sampletravelapp.db.AppDatabase;

public class App extends Application {

    private AppExecutors mAppExecutors;
    private SlangInterface slangInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = new AppExecutors();
        slangInterface = new SlangInterface(this);
        slangInterface.init("AssistantId","APIKey");
        Repository.getInstance(this, AppDatabase.getInstance(this,mAppExecutors),mAppExecutors,slangInterface);
    }

    public Repository getRepository() {
        return Repository.getInstance(this,
                AppDatabase.getInstance(this,
                        mAppExecutors),mAppExecutors,slangInterface);
    }
}
