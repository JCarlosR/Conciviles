package com.programacionymas.conciviles.ui.activity;

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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.NewReportResponse;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;
import com.programacionymas.conciviles.ui.fragment.DatePickerFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ReportFormActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ReportDialogFragment";

    private EditText etDescription, etActions, etInspections, etObservations;
    private EditText etPlannedDate, etDeadline;

    private TextInputLayout tilDescription, tilActions, tilInspections, tilObservations;
    private TextInputLayout tilPlannedDate, tilDeadline;

    private ImageButton btnTakeImage, btnTakeImageAction;
    private Spinner spinnerWorkFront, spinnerArea, spinnerResponsible, spinnerCriticalRisk;
    private Spinner spinnerState, spinnerAspect, spinnerPotential;

    private TextView tvEmail, tvPosition, tvDepartment;

    private ImageView ivImage, ivImageAction;

    // Selected report to edit (or empty for new reports)
    private int report_id;
    // Inform container
    private int inform_id;
    // even for report_id = 0, it can be a local edit operation
    private boolean is_new;
    // Row id (used for edit in offline mode)
    private int _id;

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
    // Possible image paths loaded
    private String imagePath, imageActionPath;

    // Already storing the report (request)
    private boolean storing;

    // General views
    private ProgressBar progressBar;
    private NestedScrollView nestedScrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_report);

        Intent intent = getIntent();
        inform_id = intent.getIntExtra("inform_id", 0);
        report_id = intent.getIntExtra("report_id", 0);
        _id = intent.getIntExtra("_id", 0);
        boolean local_edit = intent.getBooleanExtra("local_edit", false);

        is_new = (report_id == 0 && !local_edit);

        String title;
        if (is_new)
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

        tilPlannedDate = (TextInputLayout) findViewById(R.id.tilPlannedDate);
        tilDeadline = (TextInputLayout) findViewById(R.id.tilDeadline);

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

        // additional user indicators
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvDepartment = (TextView) findViewById(R.id.tvDepartment);

        // spinner with predefined options
        spinnerState = (Spinner) findViewById(R.id.spinnerState);
        spinnerAspect = (Spinner) findViewById(R.id.spinnerAspect);
        spinnerPotential = (Spinner) findViewById(R.id.spinnerPotential);

        // load spinner data
        fetchSpinnerDataFromPreferences();

        // buttons to capture photos or pick images from gallery
        btnTakeImage = (ImageButton) findViewById(R.id.btnTakeImage);
        btnTakeImage.setOnClickListener(this);
        btnTakeImageAction = (ImageButton) findViewById(R.id.btnTakeImageAction);
        btnTakeImageAction.setOnClickListener(this);

        // image references
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImageAction = (ImageView) findViewById(R.id.ivImageAction);

        // nested scroll view and progress bar
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // load report data
        if (!is_new) {
            // !is_new == edit mode
            if (Global.isConnected(this))
                fetchReportDataFromServer(report_id);
            else
                readReportDataFromSQLite();
        }
    }

    private void readReportDataFromSQLite() {
        final MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
        Report report = myHelper.getReportByRowId(_id);
        myHelper.close();
        setInitialDataToForm(report);
    }

    private void fetchReportDataFromServer(final int report_id) {
        Call<Report> call = MyApiAdapter.getApiService().getReportById(report_id);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                if (response.isSuccessful()) {
                    Report report = response.body();

                    setInitialDataToForm(report);
                }
            }

            @Override
            public void onFailure(Call<Report> call, Throwable t) {
            }
        });
    }

    private void setInitialDataToForm(final Report report) {
        Global.setSpinnerSelectedOption(spinnerWorkFront, report.getWorkFrontName());
        Global.setSpinnerSelectedOption(spinnerArea, report.getAreaName());
        Global.setSpinnerSelectedOption(spinnerResponsible, report.getResponsibleName());
        Global.setSpinnerSelectedOption(spinnerAspect, report.getAspect());
        Global.setSpinnerSelectedOption(spinnerCriticalRisk, report.getCriticalRisksName());
        Global.setSpinnerSelectedOption(spinnerPotential, report.getPotential());
        Global.setSpinnerSelectedOption(spinnerState, report.getState());

        // to preserve the URLs
        imagePath = report.getImage();
        imageActionPath = report.getImageAction();

        // to preserver the probably captured offline
        image = report.getImageBase64();
        imageAction = report.getImageActionBase64();

        String base64;
        if (report.getImage() == null ||report.getImage().isEmpty()) {
            base64 = report.getImageBase64();
            if (base64 != null && !base64.isEmpty())
                ivImage.setImageBitmap(Global.getBitmapFromBase64(base64));
        } else {
            Picasso.with(getApplicationContext()).load(report.getImage()).into(ivImage);
        }

        if (report.getImage() == null ||report.getImage().isEmpty()) {
            base64 = report.getImageActionBase64();
            if (base64 != null && !base64.isEmpty())
                ivImageAction.setImageBitmap(Global.getBitmapFromBase64(base64));
        } else{
            Picasso.with(getApplicationContext()).load(report.getImageAction()).into(ivImageAction);
        }


        etPlannedDate.setText(report.getPlannedDate());
        etDeadline.setText(report.getDeadline());
        etInspections.setText(String.valueOf(report.getInspections()));
        etDescription.setText(report.getDescription());
        etActions.setText(report.getActions());
        etObservations.setText(report.getObservations());
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
        spinnerResponsible.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                final int i = Global.getSpinnerSelectedIndex(spinnerResponsible);

                tvEmail.setText("Email: " + responsibleUsers.get(i).getEmail());
                tvPosition.setText("Cargo: " + responsibleUsers.get(i).getPosition());
                tvDepartment.setText("Departamento: " + responsibleUsers.get(i).getDepartment());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                tvEmail.setText("Email: ");
                tvPosition.setText("Cargo: ");
                tvDepartment.setText("Departamento: ");
            }

        });

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
/*        MyDbHelper mDbHelper = new MyDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyDbContract.AreaEntry.COLUMN_NAME, "XD");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(MyDbContract.AreaEntry.TABLE_NAME, null, values);

*/

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
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.save).setEnabled(!storing);

        return true;
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
                final String selectedDate = year + "-" + Global.twoDigits(month+1) + "-" + Global.twoDigits(day);
                editText.setText(selectedDate);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private boolean validateEditText(EditText editText, TextInputLayout textInputLayout, int errorString) {
        // Log.d("ReportDialogFragment", "Validating an EditText with this value => " + editText.getText().toString());
        if (editText.getText().toString().length() < 1) {
            textInputLayout.setError(getString(errorString));
            editText.requestFocus();
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

        if (! validateEditText(etPlannedDate, tilPlannedDate, R.string.error_planned_date)) {
            return;
        }

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
        final WorkFront workFront = workFronts.get(workFrontIndex);
        final int workFrontId = workFront.getId();

        final int areaIndex = Global.getSpinnerSelectedIndex(spinnerArea);
        final Area area = areas.get(areaIndex);
        final int areaId = area.getId();

        final int responsibleIndex = Global.getSpinnerSelectedIndex(spinnerResponsible);
        final User responsible = responsibleUsers.get(responsibleIndex);
        final int responsibleId = responsible.getId();

        final int criticalRiskIndex = Global.getSpinnerSelectedIndex(spinnerCriticalRisk);
        final CriticalRisk criticalRisk = criticalRisks.get(criticalRiskIndex);
        final int criticalRiskId = criticalRisk.getId();

        final String state = spinnerState.getSelectedItem().toString();
        final String aspect = spinnerAspect.getSelectedItem().toString();
        final String potential = spinnerPotential.getSelectedItem().toString();

        // additional validations
        if (state.equals("Cerrado") && deadline.isEmpty()) {
            tilDeadline.setError(getString(R.string.error_deadline));
            return;
        } else {
            tilDeadline.setErrorEnabled(false); // clear error
        }

        final int user_id = Global.getIntFromPreferences(this, "user_id");

        startStoringState();

        Report report = new Report();
        report.setRowId(_id);
        report.setUserId(user_id);
        report.setDescription(description);
        report.setWorkFrontId(workFrontId);
        report.setAreaId(areaId);
        report.setResponsibleId(responsibleId);
        report.setPlannedDate(planned_date);
        report.setDeadline(deadline);
        report.setState(state);
        report.setActions(actions);
        report.setAspect(aspect);
        report.setPotential(potential);
        report.setInspections(Integer.parseInt(inspections));
        report.setCriticalRisksId(criticalRiskId);
        report.setObservations(observations);

        // offline values required to set
        report.setInformId(inform_id);

        if (image!=null && !image.isEmpty())
            report.setImageBase64(image);
        else
            report.setImage(imagePath);

        if (imageAction!=null && !imageAction.isEmpty())
            report.setImageActionBase64(imageAction);
        else
            report.setImageAction(imageActionPath);

        report.setWorkFrontName(workFront.getName());
        report.setAreaName(area.getName());
        report.setResponsibleName(responsible.getName());
        

        // Check the internet connection
        // If there is no internet connection, save the changes locally
        if (! Global.isConnected(this)) {

            MyDbHelper myHelper = new MyDbHelper(this);

            // If the report ID is ZERO, create a new record
            if (is_new) {
                // Insert a new record locally
                report.setId(0); // we have to store as NULL in the SQLite db (because UNIQUE column allows multiple NULL values)

                myHelper.insertReport(report);
                Log.d(TAG, "New report was inserted in the db (offline mode).");
            } else {
                // Update the changes locally for the edited report
                report.setId(report_id); // report that will be updated in the local db
                report.setOfflineEdited(true);

                myHelper.updateReport(report);
                Log.d(TAG, "The selected report was updated in the db (offline mode).");
            }
            myHelper.close();

            setResult(RESULT_OK);
            finish();
            return; // stop the online store
        }

        // When the internet connection is established
        // If the report ID is ZERO, create a new record
        if (is_new) {

            // Request to post/store a new report

            Observable<NewReportResponse> observable = report.postToServer();

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<NewReportResponse>() {

                        @Override
                        public void onNext(NewReportResponse newReportResponse) {
                            // just one item
                            if (newReportResponse.isSuccess()) {
                                Toast.makeText(getApplicationContext(), "El reporte se ha registrado satisfactoriamente.", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                stopStoringState();
                                Toast.makeText(getApplicationContext(), newReportResponse.getFirstError(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCompleted() {
                            // Log.d(TAG, "onCompleted" );
                            // caution: finish is called in onNext, be careful with memory leaks at this point
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.d(TAG, "onError Throwable: " + t.toString());
                            // if (t instanceof HttpException) {
                            stopStoringState();
                            Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {

            // Request to edit the selected report
            report.setId(report_id);
            Observable<NewReportResponse> observable = report.updateInServer();

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<NewReportResponse>() {

                        @Override
                        public void onNext(NewReportResponse newReportResponse) {
                            // just one item
                            if (newReportResponse.isSuccess()) {
                                Toast.makeText(getApplicationContext(), "El reporte se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                stopStoringState();
                                Toast.makeText(getApplicationContext(), newReportResponse.getFirstError(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCompleted() {
                            // Log.d(TAG, "onCompleted" );
                            // caution: finish is called in onNext, be careful with memory leaks at this point
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.d(TAG, "onError Throwable: " + t.toString());
                            // if (t instanceof HttpException) {
                            stopStoringState();
                            Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                        // Log.d("ReportDialogFragment", "Build.VERSION.SDK_INT >= 23 is TRUE");
                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            // permission granted
                            // Log.d("ReportDialogFragment", "Camera permission already granted");
                            startCameraIntent();
                        } else {
                            // Log.d("ReportDialogFragment", "Request camera permission fired");
                            // request camera permission
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    } else {
                        // Log.d("ReportDialogFragment", "Build.VERSION.SDK_INT >= 23 is FALSE");
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
                String[] projection = { MediaStore.MediaColumns.DATA };
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
                bitmap = Global.getThumbnailFromBitmap(bitmap);
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

    private void startStoringState() {
        storing = true;
        invalidateOptionsMenu();

        nestedScrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopStoringState() {
        storing = false;
        invalidateOptionsMenu();

        nestedScrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
