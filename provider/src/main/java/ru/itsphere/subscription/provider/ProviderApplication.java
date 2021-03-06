package ru.itsphere.subscription.provider;

import android.util.Log;

import java.util.Observer;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.itsphere.subscription.common.CommonApplication;
import ru.itsphere.subscription.common.utils.ObservableField;
import ru.itsphere.subscription.domain.Organization;

public class ProviderApplication extends CommonApplication {
    public static final String SERVER_HOST = "subscription-server.herokuapp.com";
    public static final String SERVER_URL = "http://" + SERVER_HOST;
    private static final String tag = ProviderApplication.class.getName();
    public static int CURRENT_ORG_ID = 1;

    private ObservableField<Organization> currentOrganization = new ObservableField<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(tag, "Getting organization by id started...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isServerAvailable()) {
                    initDataFromServer();
                } else {
                    initDataFromDatabase();
                }
            }
        }).start();
    }

    public void registerObserverForCurrentOrganization(Observer observer) {
        currentOrganization.addObserver(observer);
    }

    private void initDataFromDatabase() {
        Log.i(tag, "Getting organization by id from db started...");
        Organization organization = getOrganizationService().getOrganizationById(CURRENT_ORG_ID, true);
        Log.i(tag, "Getting organization by id from db stopped");
        Log.i(tag, String.format("currentOrganization was set %s", organization == null ? organization : organization.getId()));
        currentOrganization.set(organization);
    }

    private void initDataFromServer() {
        Log.i(tag, "Getting organization by id from server started...");
        getServer().getOrganizationById(CURRENT_ORG_ID).enqueue(new Callback<Organization>() {
            @Override
            public void onResponse(Response<Organization> response, Retrofit retrofit) {
                Log.i(tag, "Getting organization by id from server stopped.");
                currentOrganization.set(response.body());
                if (getCurrentOrganization() == null) {
                    Log.i(tag, "Organization hasn't registered yet");
                } else {
                    getOrganizationService().createOrUpdate(getCurrentOrganization());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(tag, String.format("getOrganizationById (id: %d) has thrown: ", CURRENT_ORG_ID), t);
                initDataFromDatabase();
            }
        });
    }

    @Override
    protected String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    protected String getServerHost() {
        return SERVER_HOST;
    }

    public Organization getCurrentOrganization() {
        return currentOrganization.get();
    }
}
