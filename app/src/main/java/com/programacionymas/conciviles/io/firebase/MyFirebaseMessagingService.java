package com.programacionymas.conciviles.io.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.io.sqlite.repository.ReportRepository;
import com.programacionymas.conciviles.model.Report;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
            handleNow(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        /*if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
        }*/
        // sendNotification(...)
    }

    private void handleNow(Map<String, String> data) {
        String updatedEntity = data.get("updated_entity");
        String updatedId = data.get("updated_id");
        String action = data.get("action");

        if (updatedEntity.equals("report")) {
            updateReport(updatedId, action);
        } else if (updatedEntity.equals("inform")) {
            updateInform(updatedId, action);
        }
    }

    private void updateReport(final String reportId, final String action) {
        final int intReportId = Integer.parseInt(reportId);

        if (action.equals("saved")) {
            downloadReport(intReportId);
        } else if (action.equals("deleted")) {
            MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
            Report report = myHelper.getReportById(intReportId);

            if (report != null) {
                ReportRepository.delete(getApplicationContext(), reportId);
                notifyUiToUpdateReport(report.getInformId(), intReportId, "deleted");
            }
        }
    }

    private void downloadReport(final int reportId) {
        Call<Report> call = MyApiAdapter.getApiService().getReportById(reportId);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                if (response.isSuccessful()) {
                    Report report = response.body();
                    ReportRepository.save(getApplicationContext(), report);
                    notifyUiToUpdateReport(report.getInformId(), report.getId(), "saved");
                } else {
                    Toast.makeText(MyFirebaseMessagingService.this, R.string.error_retrofit_response, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Report> call, Throwable t) {
                Toast.makeText(MyFirebaseMessagingService.this, R.string.error_retrofit_callback, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyUiToUpdateReport(final int inform_id, final int report_id, final String action) {
        Intent intent = new Intent("event-update-report");
        intent.putExtra("inform_id", inform_id);
        intent.putExtra("report_id", report_id);
        intent.putExtra("action", action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Broadcast to update report was sent (report_id = "+report_id+", inform_id = "+inform_id+")");
    }

    private void updateInform(final String informId, final String action) {
        final int intInformId = Integer.parseInt(informId);

        if (action.equals("saved")) {
            downloadInform(intInformId);
        }
    }

    private void downloadInform(final int informId) {
        Global.checkForUpdates(getApplicationContext());
    }

    /*
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // 0 = request code
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // id of notification
        notificationManager.notify(0, notificationBuilder.build());
    }*/
}
