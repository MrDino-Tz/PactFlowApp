package co.dtc.fieldwork.pactflow.ui.contract;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import co.dtc.fieldwork.pactflow.R;
import co.dtc.fieldwork.pactflow.model.Contract;
import co.dtc.fieldwork.pactflow.repository.ContractRepository;
import co.dtc.fieldwork.pactflow.viewmodel.ContractViewModel;

public class CreateContractActivity extends AppCompatActivity {
    private ContractViewModel viewModel;
    private TextInputEditText etTitle, etAmount, etDescription, etStartDate, etEndDate;
    private AutoCompleteTextView actvContractType;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private View progressOverlay;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contract);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ContractViewModel.class);
        viewModel.init(getApplicationContext());

        // Initialize views
        initializeViews();
        setupContractTypeDropdown();
        setupDatePickers();
        setupSaveButton();

        // Initialize progress overlay
        progressOverlay = findViewById(R.id.progressOverlay);
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        actvContractType = findViewById(R.id.actvContractType);
    }

    private void setupContractTypeDropdown() {
        String[] contractTypes = getResources().getStringArray(R.array.contract_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                contractTypes
        );
        actvContractType.setAdapter(adapter);
    }

    private void setupDatePickers() {
        // Start Date Picker
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etStartDate.setFocusable(false);

        // End Date Picker
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        etEndDate.setFocusable(false);
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);
                    String formattedDate = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        etStartDate.setText(formattedDate);
                    } else {
                        etEndDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today for start date
        if (isStartDate) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else if (startDateCalendar.getTimeInMillis() > 0) {
            // For end date, set minimum date to start date
            datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis() - 1000);
        }

        datePickerDialog.show();
    }

    private void setupSaveButton() {
        View saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(v -> {
            if (!isSaving) {
                saveContract();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        isSaving = isLoading;
        progressOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnSave).setEnabled(!isLoading);
    }

    private void saveContract() {
        // Validate inputs
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String contractType = actvContractType.getText() != null ? actvContractType.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDate = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";
        boolean isFinalized = ((android.widget.CheckBox) findViewById(R.id.cbIsFinalized)).isChecked();

        android.util.Log.d("CreateContract", "Saving contract: " + title);

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Please enter a valid amount");
            etAmount.requestFocus();
            return;
        }

        // Create contract object
        Contract contract = new Contract();
        contract.setTitle(title);
        contract.setAmount(amount);
        contract.setContractType(contractType);
        contract.setDescription(description);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setFinalized(isFinalized);
        contract.setVersion(1); // Initial version
        contract.setTimestamp(String.valueOf(System.currentTimeMillis()));

        // Show loading and save contract
        setLoading(true);
        viewModel.createContract(contract, new ContractRepository.ContractCallback() {
            @Override
            public void onSuccess(Contract contract) {
                runOnUiThread(() -> {
                    setLoading(false);
                    // Show a brief success message
                    Toast.makeText(CreateContractActivity.this,
                            "✓ " + getString(R.string.contract_saved_successfully),
                            Toast.LENGTH_LONG).show();

                    // Set result and finish after a short delay to allow the toast to be seen
                    setResult(RESULT_OK);
                    new android.os.Handler().postDelayed(() -> {
                        finish();
                    }, 1000); // 1 second delay
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    new android.app.AlertDialog.Builder(CreateContractActivity.this)
                            .setTitle("Error Saving Contract")
                            .setMessage("Could not save contract: " + errorMessage)
                            .setPositiveButton("Retry", (dialog, which) -> saveContract())
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }
        });
    }

    // ViewModel observation is now handled through callbacks
}
