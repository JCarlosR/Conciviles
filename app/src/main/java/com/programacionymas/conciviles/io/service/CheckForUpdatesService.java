package com.programacionymas.conciviles.io.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.NewReportResponse;
import com.programacionymas.conciviles.io.sqlite.MyDbContract;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.model.Inform;
import com.programacionymas.conciviles.model.Report;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CheckForUpdatesService extends Service {
    public Runnable mRunnable = null;

    public CheckForUpdatesService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alreadyRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static int user_id;
    private static boolean alreadyRunning = false;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // avoid multiple executions of onStartCommand (even simultaneously)
        if (alreadyRunning) {
            Toast.makeText(getApplicationContext(), "Ejecutando actualmente las actualizaciones ...", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        alreadyRunning = true;

        if (intent != null)
            user_id = intent.getIntExtra("user_id", 0);

        if (user_id==0) // it should not happen
            return super.onStartCommand(intent, flags, startId);;

        final Handler mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {

                checkForUpdatesAtInformLevel();
                alreadyRunning = false;
                // mHandler.postDelayed(mRunnable, 20 * 1000); // re-call
            }
        };
        mHandler.postDelayed(mRunnable, 3 * 1000); // first time with 3 seconds delay

        return super.onStartCommand(intent, flags, startId);
    }

    private void checkForUpdatesAtInformLevel() {
        MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
        boolean isInfoAvailable = myHelper.isAnyInfoAvailable(MyDbContract.InformEntry.TABLE_NAME);

        if (!isInfoAvailable || differentLoggedUser()) {
            // if there is no info available, start a general download
            downloadAllInformsAndReports();
        } else {
            downloadUpdatedInformation();
        }
    }

    private boolean differentLoggedUser() {
        final int last_download_user_id = Global.getIntFromPreferences(getApplicationContext(), "last_download_user_id");
        return user_id != last_download_user_id;
    }

    private boolean hasPassedAtLeastSeconds(final int seconds) {
        long currentTime = new Date().getTime();
        // Last time saved in shared preference
        final long lastTime = Global.getLongFromPreferences(getApplicationContext(), "lastTime");

        if (lastTime == 0) {
            return true; // perform the first download
        } else {
            // Difference in seconds
            double diff = TimeUnit.MILLISECONDS.toSeconds(currentTime - lastTime);
            return diff >= seconds; // has passed X seconds or more
        }
    }

    private void downloadUpdatedInformation() {
        if (!hasPassedAtLeastSeconds(5)) {
            // min interval for re-check updates
            Toast.makeText(getApplicationContext(), "Espere al menos 5 segundos antes de actualizar nuevamente", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.performing_a_smart_download, Toast.LENGTH_SHORT).show();

        // Check for updated informs (considering the latest 3)
        Call<ArrayList<Inform>> call = MyApiAdapter.getApiService().getInformsByLocationOfUser(user_id);
        call.enqueue(new Callback<ArrayList<Inform>>() {
            @Override
            public void onResponse(Call<ArrayList<Inform>> call, Response<ArrayList<Inform>> response) {
                if (response.isSuccessful()) {

                    ArrayList<Inform> informs = response.body();

                    MyDbHelper myHelper = new MyDbHelper(getApplicationContext());

                    // Get the local stored informs
                    ArrayList<Inform> localInforms = myHelper.getInforms();

                    // Which informs we currently have (?)
                    ArrayList<Inform> informsToDownload = new ArrayList<>();
                    for (Inform inform : informs) {
                        boolean foundInform = false;
                        for (Inform localInform : localInforms) {
                            if (inform.getId() == localInform.getId()) {
                                // if it is found, the download depends in the last updated reports
                                /*if (inform.getId()==30) {
                                    Log.d("debugInform30", "inform.getReportsUpdatedAt() => " + inform.getReportsUpdatedAt());
                                    Log.d("debugInform30", "localInform.getReportsUpdatedAt() => " + localInform.getReportsUpdatedAt());
                                }*/
                                if ( inform.getReportsUpdatedAt()!=null && localInform.getReportsUpdatedAt()!=null && // be careful with null values
                                        ! inform.getReportsUpdatedAt().equals(localInform.getReportsUpdatedAt()) ) { // real condition

                                    informsToDownload.add(inform);
                                }
                                foundInform = true;
                                break;
                            }
                        }
                        if (!foundInform) // recently created
                            informsToDownload.add(inform);
                    }

                    // Replace the local stored informs
                    myHelper.updateInformsTable(informs);
                    notifyUiToUpdateInforms();

                    // Now download the updated reports
                    uploadLocalChangesAndNextDownloadReports(informsToDownload, 3);
                } else {
                    Toast.makeText(CheckForUpdatesService.this, R.string.failure_downloading_all_informs, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Inform>> call, Throwable t) {
                Toast.makeText(CheckForUpdatesService.this, R.string.failure_downloading_all_informs, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadAllInformsAndReports() {
        if (!hasPassedAtLeastSeconds(12)) return; // min interval for repeat a general download

        Toast.makeText(this, R.string.performing_a_full_download, Toast.LENGTH_SHORT).show();

        Call<ArrayList<Inform>> call = MyApiAdapter.getApiService().getInformsByLocationOfUser(user_id);
        call.enqueue(new Callback<ArrayList<Inform>>() {
            @Override
            public void onResponse(Call<ArrayList<Inform>> call, Response<ArrayList<Inform>> response) {
                if (response.isSuccessful()) {

                    ArrayList<Inform> informs = response.body();

                    MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
                    myHelper.updateInformsTable(informs);
                    notifyUiToUpdateInforms();

                    // Continue downloading the reports
                    uploadLocalChangesAndNextDownloadReports(informs, 3);

                    // Save the time for the last full-download
                    long currentTime = new Date().getTime();
                    Global.saveLongPreference(getApplicationContext(), "lastTime", currentTime);

                } else {
                    Toast.makeText(CheckForUpdatesService.this, R.string.failure_downloading_all_informs, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Inform>> call, Throwable t) {
                Toast.makeText(CheckForUpdatesService.this, R.string.failure_downloading_all_informs, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyUiToUpdateInforms() {
        Intent intent = new Intent("event-update-informs");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        // Log.d("MyService", "Broadcast to update informs sent");
    }

    private void notifyUiToUpdateReports(final int inform_id) {
        Intent intent = new Intent("event-update-reports");
        intent.putExtra("inform_id", inform_id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        // Log.d("MyService", "Broadcast to update reports was sent (inform_id = "+inform_id+")");
    }

    private void uploadLocalChangesAndNextDownloadReports(final ArrayList<Inform> informs, final int tries) {
        // TODO: create some logic to first pull and next push the local changes with confirmation dialogs
        // AT LEAST FOR NOW: push the local changes and next PULL all the reports

        final MyDbHelper myHelper = new MyDbHelper(getApplicationContext());

        // first sync uploading the new reports and the edited reports
        ArrayList<Report> createdReports = myHelper.getOfflineCreatedReports();
        ArrayList<Report> editedReports = myHelper.getOfflineEditedReports();

        if (createdReports.size()>0 || editedReports.size()>0)
            Toast.makeText(this, "Subiendo " + createdReports.size() + " reportes nuevos y " + editedReports.size() + " editados", Toast.LENGTH_SHORT).show();

        // it is necessary to subscribeOn a different thread (Retrofit doesn't handle it for Observables)
        Observable<NewReportResponse> createdReportsObs = Observable.from(createdReports) // .just() != .from()
                .flatMap(new Func1<Report, Observable<NewReportResponse>>() {
                    @Override
                    public Observable<NewReportResponse> call(Report report) {
                        return report.postToServer();
                    }
                });

        Observable<NewReportResponse> editedReportsObs = Observable.from(editedReports)
                .flatMap(new Func1<Report, Observable<NewReportResponse>>() {
                    @Override
                    public Observable<NewReportResponse> call(Report report) {
                        return report.updateInServer();
                    }
                });

        Observable.merge(createdReportsObs, editedReportsObs)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread()) // no UI thread required
                .subscribe(new Subscriber<NewReportResponse>() {
                    @Override
                    public void onCompleted() {
                        // delete the offline created/edited reports
                        myHelper.deleteOfflineReports();
                        Log.d("onCompleted changes", "offline reports were deleted, now " + informs.size() + " informs will be downloaded");
                        // because just only the informs with changes will be updated
                        downloadReportsInTheseInforms(informs);
                        Global.saveIntPreference(getApplicationContext(), "last_download_user_id", user_id);
                    }

                    @Override
                    public void onError(Throwable e) {
                        try {
                            Toast.makeText(CheckForUpdatesService.this, "Ha ocurrido un error enviando la información. Volviendo a intentar ...", Toast.LENGTH_LONG).show();
                            if (tries > 0)
                                uploadLocalChangesAndNextDownloadReports(informs, tries-1);
                        } catch (Throwable t) {
                            Toast.makeText(CheckForUpdatesService.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(NewReportResponse o) {
                        // delete the local changes that was uploaded successfully
                        // to avoid duplication in a re-send logic
                        // TODO: I have to mark as uploaded the reports sent successfully
                    }
                });
    }

    private void downloadReportsInTheseInforms(ArrayList<Inform> informs) {
        final MyDbHelper myHelper = new MyDbHelper(getApplicationContext());

        for (final Inform inform : informs) {
            // download the reports contained in each inform
            Call<ArrayList<Report>> call = MyApiAdapter.getApiService().getReportsByInform(inform.getId());
            call.enqueue(new Callback<ArrayList<Report>>() {
                @Override
                public void onResponse(Call<ArrayList<Report>> call, Response<ArrayList<Report>> response) {
                    if (response.isSuccessful()) {
                        ArrayList<Report> reports = response.body();

                        myHelper.updateReportsForInform(reports, inform.getId());
                        notifyUiToUpdateReports(inform.getId());
                    }
                }
                @Override
                public void onFailure(Call<ArrayList<Report>> call, Throwable t) {
                    Toast.makeText(CheckForUpdatesService.this, "No se han podido actualizar los reportes del informe "+inform.getId(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
