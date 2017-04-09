package com.programacionymas.conciviles.ui.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.NewReportResponse;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;
import com.programacionymas.conciviles.ui.activity.ReportsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class ReportDialogFragment extends DialogFragment implements View.OnClickListener {

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

    public static ReportDialogFragment newInstance(int inform_id, int report_id) {
        ReportDialogFragment f = new ReportDialogFragment();

        Bundle args = new Bundle();
        args.putInt("inform_id", inform_id);
        args.putInt("report_id", report_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inform_id = getArguments().getInt("inform_id");
        report_id = getArguments().getInt("report_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_report, container, false);

        // etId = (EditText) view.findViewById(R.id.etId);

        String title;
        if (report_id == 0)
            title = "Nuevo reporte";
        else {
            title = "Editar reporte";
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        tilDescription = (TextInputLayout) view.findViewById(R.id.tilDescription);
        tilInspections = (TextInputLayout) view.findViewById(R.id.tilInspections);
        // tilObservations = (TextInputLayout) view.findViewById(R.id.tilObservations);

        etDescription = (EditText) view.findViewById(R.id.etDescription);
        etActions = (EditText) view.findViewById(R.id.etActions);
        etInspections = (EditText) view.findViewById(R.id.etInspections);
        etObservations = (EditText) view.findViewById(R.id.etObservations);

        // date fields references
        etPlannedDate = (EditText) view.findViewById(R.id.etPlannedDate);
        etPlannedDate.setOnClickListener(this);
        etDeadline = (EditText) view.findViewById(R.id.etDeadline);
        etDeadline.setOnClickListener(this);

        // spinner references
        spinnerWorkFront = (Spinner) view.findViewById(R.id.spinnerWorkFront);
        spinnerArea = (Spinner) view.findViewById(R.id.spinnerArea);
        spinnerResponsible = (Spinner) view.findViewById(R.id.spinnerResponsible);
        spinnerCriticalRisk = (Spinner) view.findViewById(R.id.spinnerCriticalRisk);

        // spinner with predefined options
        spinnerState = (Spinner) view.findViewById(R.id.spinnerState);
        spinnerAspect = (Spinner) view.findViewById(R.id.spinnerAspect);
        spinnerPotential = (Spinner) view.findViewById(R.id.spinnerPotential);

        // load spinner data
        fetchSpinnerDataFromServer();
        // load report data
        if (report_id > 0)
            fetchReportDataFromServer(report_id);

        // buttons to capture photos or pick images from gallery
        btnTakeImage = (ImageButton) view.findViewById(R.id.btnTakeImage);
        btnTakeImage.setOnClickListener(this);
        btnTakeImageAction = (ImageButton) view.findViewById(R.id.btnTakeImageAction);
        btnTakeImageAction.setOnClickListener(this);

        // image view references
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        ivImageAction = (ImageView) view.findViewById(R.id.ivImageAction);

        return view;
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

                    Picasso.with(getActivity()).load(report.getImage()).into(ivImage);
                    Picasso.with(getActivity()).load(report.getImageAction()).into(ivImageAction);

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

    private void fetchSpinnerDataFromServer() {
        final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");

        Call<ArrayList<WorkFront>> callWorkFronts = MyApiAdapter.getApiService()
                .getWorkFrontsByLocationOfUser(user_id);
        callWorkFronts.enqueue(new Callback<ArrayList<WorkFront>>() {
            @Override
            public void onResponse(Call<ArrayList<WorkFront>> call, Response<ArrayList<WorkFront>> response) {
                if (response.isSuccessful()) {
                    workFronts = response.body();
                    populateWorkFrontSpinner(workFronts);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<WorkFront>> call, Throwable t) {

            }
        });

        Call<ArrayList<Area>> callAreas = MyApiAdapter.getApiService().getAreas();
        callAreas.enqueue(new Callback<ArrayList<Area>>() {
            @Override
            public void onResponse(Call<ArrayList<Area>> call, Response<ArrayList<Area>> response) {
                if (response.isSuccessful()) {
                    areas = response.body();
                    populateAreaSpinner(areas);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Area>> call, Throwable t) {

            }
        });

        Call<ArrayList<User>> callUsers = MyApiAdapter.getApiService()
                .getUsersByLocationOfUser(user_id);
        callUsers.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if (response.isSuccessful()) {
                    responsibleUsers = response.body();
                    populateResponsibleSpinner(responsibleUsers);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {

            }
        });

        Call<ArrayList<CriticalRisk>> callCriticalRisks = MyApiAdapter.getApiService()
                .getCriticalRisks();
        callCriticalRisks.enqueue(new Callback<ArrayList<CriticalRisk>>() {
            @Override
            public void onResponse(Call<ArrayList<CriticalRisk>> call, Response<ArrayList<CriticalRisk>> response) {
                if (response.isSuccessful()) {
                    criticalRisks = response.body();
                    populateCriticalRiskSpinner(criticalRisks);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<CriticalRisk>> call, Throwable t) {

            }
        });
    }

    private void populateWorkFrontSpinner(ArrayList<WorkFront> workFronts) {
        List<String> options = new ArrayList<String>();
        for (WorkFront workFront : workFronts) {
            options.add(workFront.getName());
        }

        populateSpinner(options, spinnerWorkFront);
    }

    private void populateAreaSpinner(ArrayList<Area> areas) {
        List<String> options = new ArrayList<String>();
        for (Area area : areas) {
            options.add(area.getName());
        }

        populateSpinner(options, spinnerArea);
    }

    private void populateResponsibleSpinner(ArrayList<User> users) {
        List<String> options = new ArrayList<String>();
        for (User user : users) {
            options.add(user.getName());
        }

        populateSpinner(options, spinnerResponsible);
    }

    private void populateCriticalRiskSpinner(ArrayList<CriticalRisk> criticalRisks) {
        List<String> options = new ArrayList<String>();
        for (CriticalRisk criticalRisk : criticalRisks) {
            options.add(criticalRisk.getName());
        }

        populateSpinner(options, spinnerCriticalRisk);
    }

    private void populateSpinner(List<String> options, Spinner spinnerTarget) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, options);
        spinnerTarget.setAdapter(spinnerArrayAdapter);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.save_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {
            validateForm();
            return true;
        } else if (id == android.R.id.home) {
            dismiss();
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
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean validateEditText(EditText editText, TextInputLayout textInputLayout, int errorString) {
        Log.d("ReportDialogFragment", "Validating an EditText with this value => " + editText.getText().toString());
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

        final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");

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
                            Toast.makeText(getActivity(), "El reporte se ha registrado satisfactoriamente.", Toast.LENGTH_SHORT).show();
                            dismiss();
                            ((ReportsActivity) getActivity()).reloadReportsByInform();
                        } else {
                            Toast.makeText(getActivity(), newReportResponse.getFirstError(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), "El reporte se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                            dismiss();
                            ((ReportsActivity) getActivity()).reloadReportsByInform();
                        } else {
                            Toast.makeText(getActivity(), newReportResponse.getFirstError(), Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
                        if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
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
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
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
                Global.showMessageDialog(getActivity(), "Alerta", "No podrás subir capturar fotos con la cámara hasta que otorgues este permiso a la aplicación.");
            }
        }
    }

}
