package com.programacionymas.conciviles.ui.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.NewReportResponse;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDialogFragment extends AppCompatActivity implements View.OnClickListener {

    private EditText etDescription, etActions, etInspections, etObservations;
    private EditText etPlannedDate, etDeadline;

    private TextInputLayout tilDescription, tilActions, tilInspections, tilObservations;

    private ImageButton btnTakeImage, btnTakeImageAction;
    private Spinner spinnerWorkFront, spinnerArea, spinnerResponsible, spinnerCriticalRisk;
    private Spinner spinnerState, spinnerAspect, spinnerPotential;

    private ImageView ivImage, ivImageAction;

    // Selected report to edit (or empty for new reports)
    private int report_id;
    // Inform container
    private int inform_id;

    // Spinner options
    private ArrayList<WorkFront> workFronts;
    private ArrayList<Area> areas;
    private ArrayList<User> responsibleUsers;
    private ArrayList<CriticalRisk> criticalRisks;

    // Request codes
    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private static final int REQUEST_CAMERA = 20;
    private static final int SELECT_FILE = 21;

    // Default extension for images (using camera)
    // private static final String DEFAULT_EXTENSION = "jpg";

    // Location of the last photo taken
    // private static String currentPhotoPath;

    // Image view that will be filled with the taken photo / selected image
    private ImageView ivTarget;

    // Images stored as base64
    private String image, imageAction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_report);

        Intent intent = getIntent();
        inform_id = intent.getIntExtra("inform_id", 0);
        report_id = intent.getIntExtra("report_id", 0);

        String title;
        if (report_id == 0)
            title = "Nuevo reporte";
        else {
            title = "Editar reporte";
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }

        // setHasOptionsMenu(true);
        getViewReferences();
    }

    private void getViewReferences() {

        tilDescription = (TextInputLayout) findViewById(R.id.tilDescription);
        tilInspections = (TextInputLayout) findViewById(R.id.tilInspections);
        // tilObservations = (TextInputLayout) findViewById(R.id.tilObservations);

        etDescription = (EditText) findViewById(R.id.etDescription);
        etActions = (EditText) findViewById(R.id.etActions);
        etInspections = (EditText) findViewById(R.id.etInspections);
        etObservations = (EditText) findViewById(R.id.etObservations);

        // date fields references
        etPlannedDate = (EditText) findViewById(R.id.etPlannedDate);
        etPlannedDate.setOnClickListener(this);
        etDeadline = (EditText) findViewById(R.id.etDeadline);
        etDeadline.setOnClickListener(this);

        // spinner references
        spinnerWorkFront = (Spinner) findViewById(R.id.spinnerWorkFront);
        spinnerArea = (Spinner) findViewById(R.id.spinnerArea);
        spinnerResponsible = (Spinner) findViewById(R.id.spinnerResponsible);
        spinnerCriticalRisk = (Spinner) findViewById(R.id.spinnerCriticalRisk);

        // spinner with predefined options
        spinnerState = (Spinner) findViewById(R.id.spinnerState);
        spinnerAspect = (Spinner) findViewById(R.id.spinnerAspect);
        spinnerPotential = (Spinner) findViewById(R.id.spinnerPotential);

        // load spinner data
        fetchSpinnerDataFromPreferences();

        // load report data
        if (report_id > 0)
            fetchReportDataFromServer(report_id);

        // buttons to capture photos or pick images from gallery
        btnTakeImage = (ImageButton) findViewById(R.id.btnTakeImage);
        btnTakeImage.setOnClickListener(this);
        btnTakeImageAction = (ImageButton) findViewById(R.id.btnTakeImageAction);
        btnTakeImageAction.setOnClickListener(this);

        // image references
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImageAction = (ImageView) findViewById(R.id.ivImageAction);
    }

    private void fetchReportDataFromServer(final int report_id) {
        Call<Report> call = MyApiAdapter.getApiService().getReportById(report_id);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                if (response.isSuccessful()) {
                    Report report = response.body();

                    Global.setSpinnerSelectedOption(spinnerWorkFront, report.getWorkFrontName());
                    Global.setSpinnerSelectedOption(spinnerArea, report.getAreaName());
                    Global.setSpinnerSelectedOption(spinnerResponsible, report.getResponsibleName());
                    Global.setSpinnerSelectedOption(spinnerAspect, report.getAspect());
                    Global.setSpinnerSelectedOption(spinnerCriticalRisk, report.getCriticalRisksName());
                    Global.setSpinnerSelectedOption(spinnerPotential, report.getPotential());
                    Global.setSpinnerSelectedOption(spinnerState, report.getState());

                    Picasso.with(getApplicationContext()).load(report.getImage()).into(ivImage);
                    Picasso.with(getApplicationContext()).load(report.getImageAction()).into(ivImageAction);

                    etPlannedDate.setText(report.getPlannedDate());
                    etDeadline.setText(report.getDeadline());
                    etInspections.setText(String.valueOf(report.getInspections()));
                    etDescription.setText(report.getDescription());
                    etActions.setText(report.getActions());
                    etObservations.setText(report.getObservations());
                }
            }

            @Override
            public void onFailure(Call<Report> call, Throwable t) {

            }
        });
    }

    @SuppressWarnings("unchecked")
    private void fetchSpinnerDataFromPreferences() {
        final String workFrontsSerialized = Global.getStringFromPreferences(this, "work_fronts");
        workFronts = new Gson().fromJson(workFrontsSerialized, new TypeToken<ArrayList<WorkFront>>(){}.getType());
        populateWorkFrontSpinner();

        final String areasSerialized = Global.getStringFromPreferences(this, "areas");
        areas = new Gson().fromJson(areasSerialized, new TypeToken<ArrayList<Area>>(){}.getType());
        populateAreaSpinner();

        final String responsibleUsersSerialized = Global.getStringFromPreferences(this, "responsible_users");
        responsibleUsers = new Gson().fromJson(responsibleUsersSerialized, new TypeToken<ArrayList<User>>(){}.getType());
        populateResponsibleSpinner();

        final String criticalRisksSerialized = Global.getStringFromPreferences(this, "critical_risks");
        criticalRisks = new Gson().fromJson(criticalRisksSerialized, new TypeToken<ArrayList<CriticalRisk>>(){}.getType());
        populateCriticalRiskSpinner();
    }

    private void populateWorkFrontSpinner() {
        List<String> options = new ArrayList<String>();
        for (WorkFront workFront : workFronts) {
            options.add(workFront.getName());
        }

        populateSpinner(options, spinnerWorkFront);
    }

    private void populateAreaSpinner() {
        List<String> options = new ArrayList<String>();
        for (Area area : areas) {
            options.add(area.getName());
        }

        populateSpinner(options, spinnerArea);
    }

    private void populateResponsibleSpinner() {
        List<String> options = new ArrayList<String>();
        for (User user : responsibleUsers) {
            options.add(user.getName());
        }

        populateSpinner(options, spinnerResponsible);
    }

    private void populateCriticalRiskSpinner() {
        List<String> options = new ArrayList<String>();
        for (CriticalRisk criticalRisk : criticalRisks) {
            options.add(criticalRisk.getName());
        }

        populateSpinner(options, spinnerCriticalRisk);
    }

    private void populateSpinner(List<String> options, Spinner spinnerTarget) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerTarget.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {
            validateForm();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.etPlannedDate:
                showDatePickerDialog(etPlannedDate);
                break;

            case R.id.etDeadline:
                showDatePickerDialog(etDeadline);
                break;

            case R.id.btnTakeImage:
                ivTarget = ivImage;
                takeImage();
                break;
            case R.id.btnTakeImageAction:
                ivTarget = ivImageAction;
                takeImage();
                break;
        }
    }

    private void showDatePickerDialog(final EditText editText) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = year + "-" + twoDigits(month+1) + "-" + twoDigits(day);
                editText.setText(selectedDate);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean validateEditText(EditText editText, TextInputLayout textInputLayout, int errorString) {
        // Log.d("ReportDialogFragment", "Validating an EditText with this value => " + editText.getText().toString());
        if (editText.getText().toString().length() < 1) {
            textInputLayout.setError(getString(errorString));
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void validateForm() {
        if (! validateEditText(etDescription, tilDescription, R.string.error_description)) {
            return;
        }

        if (! validateEditText(etInspections, tilInspections, R.string.error_inspections)) {
            return;
        }

        // Log.d("ReportDialogFragment", "Validations passed");

        // get edit text values

        final String description = etDescription.getText().toString().trim();
        final String actions = etActions.getText().toString().trim();
        final String inspections = etInspections.getText().toString().trim();
        final String observations = etObservations.getText().toString().trim();

        // get date values
        final String planned_date = etPlannedDate.getText().toString().trim();
        final String deadline = etDeadline.getText().toString().trim();

        // get spinner values

        final int workFrontIndex = Global.getSpinnerSelectedIndex(spinnerWorkFront);
        final int workFront = workFronts.get(workFrontIndex).getId();

        final int areaIndex = Global.getSpinnerSelectedIndex(spinnerArea);
        final int area = areas.get(areaIndex).getId();

        final int responsibleIndex = Global.getSpinnerSelectedIndex(spinnerResponsible);
        final int responsible = responsibleUsers.get(responsibleIndex).getId();

        final int criticalRiskIndex = Global.getSpinnerSelectedIndex(spinnerCriticalRisk);
        final int criticalRisk= criticalRisks.get(criticalRiskIndex).getId();

        final String state = spinnerState.getSelectedItem().toString();
        final String aspect = spinnerAspect.getSelectedItem().toString();
        final String potential = spinnerPotential.getSelectedItem().toString();

        final int user_id = Global.getIntFromPreferences(this, "user_id");

        // If the report ID is ZERO, create a new record
        if (report_id == 0) {
            // Log.d("ReportDialogFragment", "Going to post a new report");


            Call<NewReportResponse> call;

            if ((image == null || image.isEmpty()) && (imageAction == null || imageAction.isEmpty())) {
                call = MyApiAdapter.getApiService().postNewReport(
                        user_id, description, workFront, area, responsible,
                        planned_date, deadline,
                        state, actions, aspect, potential, inspections,
                        criticalRisk, observations, inform_id
                );

            } else {
                call = MyApiAdapter.getApiService().postNewReportWithImages(
                        user_id, description, image, workFront, area, responsible,
                        planned_date, deadline,
                        state, actions, imageAction, aspect, potential, inspections,
                        criticalRisk, observations, inform_id
                );

            }

            call.enqueue(new Callback<NewReportResponse>() {
                @Override
                public void onResponse(Call<NewReportResponse> call, Response<NewReportResponse> response) {
                    if (response.isSuccessful()) {
                        NewReportResponse newReportResponse = response.body();
                        if (newReportResponse.isSuccess()) {
                            Toast.makeText(getApplicationContext(), "El reporte se ha registrado satisfactoriamente.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), newReportResponse.getFirstError(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<NewReportResponse> call, Throwable t) {
                    Log.d("ReportDialogFragment", "onFailure => " + t.getLocalizedMessage());
                }
            });
        } else { // edit the selected report
            Call<NewReportResponse> call;

            if ((image == null || image.isEmpty()) && (imageAction == null || imageAction.isEmpty())) {
                call = MyApiAdapter.getApiService().updateNewReport(
                        report_id, description, workFront, area, responsible,
                        planned_date, deadline,
                        state, actions, aspect, potential, inspections,
                        criticalRisk, observations
                );

            } else {
                call = MyApiAdapter.getApiService().updateNewReportWithImages(
                        report_id, description, image, workFront, area, responsible,
                        planned_date, deadline,
                        state, actions, imageAction, aspect, potential, inspections,
                        criticalRisk, observations
                );

            }

            call.enqueue(new Callback<NewReportResponse>() {
                @Override
                public void onResponse(Call<NewReportResponse> call, Response<NewReportResponse> response) {
                    if (response.isSuccessful()) {
                        NewReportResponse newReportResponse = response.body();
                        if (newReportResponse.isSuccess()) {
                            Toast.makeText(getApplicationContext(), "El reporte se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), newReportResponse.getFirstError(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<NewReportResponse> call, Throwable t) {
                    Log.d("ReportDialogFragment", "onFailure => " + t.getLocalizedMessage());
                }
            });
        }
    }

    private void takeImage() {
        // Options for the alert dialog
        final CharSequence[] items = {
                getResources().getString(R.string.picture_from_camera),
                getResources().getString(R.string.picture_from_gallery),
                getResources().getString(R.string.picture_cancel)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Title
        builder.setTitle(getResources().getString(R.string.picture_title));

        // Actions
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {

                if (option == 0) {

                    // check for permission for newer APIs
                    if (Build.VERSION.SDK_INT >= 23) {
                        Log.d("ReportDialogFragment", "Build.VERSION.SDK_INT >= 23 is TRUE");
                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            // permission granted
                            Log.d("ReportDialogFragment", "Camera permission already granted");
                            startCameraIntent();
                        } else {
                            Log.d("ReportDialogFragment", "Request camera permission fired");
                            // request camera permission
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    } else {
                        Log.d("ReportDialogFragment", "Build.VERSION.SDK_INT >= 23 is FALSE");
                        // old APIs doesn't require to check for camera permission
                        startCameraIntent();
                    }

                } else if (option == 1) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    final String chooserTitle = getResources().getString(R.string.picture_chooser_title);
                    startActivityForResult(Intent.createChooser(intent, chooserTitle), SELECT_FILE);
                } else if (option == 2) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();

    }

    private void startCameraIntent() {
        // Create the File where the photo should go
        /*File photoFile;
        try {
            photoFile = createDestinationFile();
        } catch (IOException ex) {
            return;
        }*/

        // Continue only if the File was successfully created
        /*if (photoFile != null) {*/
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(takePictureIntent, REQUEST_CAMERA);
        //}
    }

    /*private File createDestinationFile() throws IOException {
        // Path for the temporary image and its name
        final File storageDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        final String imageFileName = "" + System.currentTimeMillis();

        File image = File.createTempFile(
                imageFileName,          // prefix
                "." + DEFAULT_EXTENSION, // suffix
                storageDirectory              // directory
        );

        // Save a the file path
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // Get the thumbnail from extras
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ivTarget.setImageBitmap(photo);

                if (ivTarget == ivImage)
                    image = Global.getBase64FromBitmap(photo);
                else
                    imageAction = Global.getBase64FromBitmap(photo);
                /*Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

                boolean deleted = new File(currentPhotoPath).delete();
                if (! deleted)
                    Log.d("ReportDialogFragment", "Cannot delete file: " + currentPhotoPath);*/
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this,
                        selectedImageUri, projection, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(columnIndex);

                // Get the extension from the full path
                // final int lastDot = selectedImagePath.lastIndexOf(".");
                // final String extension = selectedImagePath.substring(lastDot+1);

                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                ivTarget.setImageBitmap(bitmap);

                if (ivTarget == ivImage)
                    image = Global.getBase64FromBitmap(bitmap);
                else
                    imageAction = Global.getBase64FromBitmap(bitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                startCameraIntent();
            } else {
                // Your app will not have this permission.
                Global.showMessageDialog(this, "Alerta", "No podrás subir capturar fotos con la cámara hasta que otorgues este permiso a la aplicación.");
            }
        }
    }

}
