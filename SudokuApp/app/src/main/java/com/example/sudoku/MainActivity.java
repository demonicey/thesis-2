package com.example.sudoku;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private SudokuGame sudokuGame;
    private GridLayout sudokuGrid;
    private TextView tvScore, tvLocation;
    private Button btnNewGame, btnSubmitScore, btnSimulateSync;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper databaseHelper;
    private OkHttpClient httpClient;
    private CryptoHelper cryptoHelper;
    private Location currentLocation;
    private int score = 0;
    private int locationBonus = 0;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize views
        sudokuGrid = findViewById(R.id.sudokuGrid);
        tvScore = findViewById(R.id.tvScore);
        tvLocation = findViewById(R.id.tvLocation);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnSubmitScore = findViewById(R.id.btnSubmitScore);
        btnSimulateSync = findViewById(R.id.btnSimulateSync);

        // Initialize game and services
        sudokuGame = new SudokuGame();
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        } catch (Exception e) {
            fusedLocationClient = null;
            Toast.makeText(this, "Location services initialization failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        databaseHelper = DatabaseHelper.getDatabase(this);
        httpClient = new OkHttpClient();
        try {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            firebaseDatabase = null;
            Toast.makeText(this, "Firebase initialization failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        cryptoHelper = new CryptoHelper(this);

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getLocation();
        }

        // Set up Sudoku grid
        setupSudokuGrid();

        // Button listeners
        btnNewGame.setOnClickListener(v -> startNewGame());
        btnSubmitScore.setOnClickListener(v -> submitScore());
        btnSimulateSync.setOnClickListener(v -> simulateSync());
    }

    private void setupSudokuGrid() {
        sudokuGrid.removeAllViews();
        int[][] board = sudokuGame.getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                EditText cell = new EditText(this);
                cell.setText(board[i][j] == 0 ? "" : String.valueOf(board[i][j]));
                cell.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                cell.setMaxLines(1);
                cell.setTag(i * 9 + j);
                cell.setGravity(android.view.Gravity.CENTER);
                cell.setTextSize(18);
                cell.setPadding(4, 4, 4, 4);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(j, 1, 1f);
                params.rowSpec = GridLayout.spec(i, 1, 1f);
                cell.setLayoutParams(params);
                cell.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(android.text.Editable editable) {
                        int pos = (int) cell.getTag();
                        int row = pos / 9;
                        int col = pos % 9;
                        try {
                            int num = Integer.parseInt(editable.toString());
                            sudokuGame.setCell(row, col, num);
                            updateScore();
                        } catch (NumberFormatException e) {
                            sudokuGame.setCell(row, col, 0);
                            updateScore();
                        }
                    }
                });
                sudokuGrid.addView(cell);
            }
        }
    }

    private void startNewGame() {
        sudokuGame.generatePuzzle();
        setupSudokuGrid();
        score = 0;
        locationBonus = 0;
        updateScore();
    }

    private void updateScore() {
        int[][] board = sudokuGame.getBoard();
        int[][] originalBoard = sudokuGame.getOriginalBoard();
        int correctCells = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (originalBoard[i][j] == 0 && board[i][j] != 0 && sudokuGame.isValidMove(i, j, board[i][j])) {
                    correctCells++;
                }
            }
        }
        score = correctCells * 10 + locationBonus;
        tvScore.setText("Score: " + score);
    }

    private void getLocation() {
        if (fusedLocationClient == null) {
            tvLocation.setText("Location: Service not available");
            return; // Early exit if location client failed to initialize
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @Override
                public boolean isCancellationRequested() {
                    return false;
                }

                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }
            }).addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    tvLocation.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
                    // Location-based bonus
                    if (isAtBonusLocation(location)) {
                        locationBonus = 50;
                        updateScore();
                        Toast.makeText(this, "Bonus location! +50 points", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvLocation.setText("Location: Not available");
                    Toast.makeText(this, "Unable to get current location. Ensure location is enabled on your device.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(this, e -> {
                tvLocation.setText("Location: Error");
                Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            tvLocation.setText("Location: Permission denied");
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAtBonusLocation(Location location) {
        // Example: Bonus if near Klaipeda University (approx coordinates)
        double lat = 55.7033;
        double lon = 21.1443;
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lon, results);
        return results[0] < 1000; // Within 1km
    }

    private void submitScore() {
        if (currentLocation != null) {
            try {
                String locationData = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                String encryptedLocation = cryptoHelper != null ? cryptoHelper.encrypt(locationData) : locationData;
                String encryptedUserId = cryptoHelper != null ? cryptoHelper.encrypt("user123") : "user123";
                String encryptedScore = cryptoHelper != null ? cryptoHelper.encrypt(String.valueOf(score)) : String.valueOf(score);
                String encryptedTimestamp = cryptoHelper != null ? cryptoHelper.encrypt(String.valueOf(System.currentTimeMillis())) : String.valueOf(System.currentTimeMillis());
                UserRecord record = new UserRecord(encryptedUserId, encryptedScore, encryptedLocation, encryptedTimestamp);
                new Thread(() -> {
                    databaseHelper.userRecordDao().insert(record);
                    runOnUiThread(() -> Toast.makeText(this, "Score saved locally!", Toast.LENGTH_SHORT).show());
                }).start();
            } catch (GeneralSecurityException e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to encrypt data. Score not saved.", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(this, "Location not available for score submission", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulateSync() {
        new Thread(() -> {
            try {
                List<UserRecord> records = databaseHelper.userRecordDao().getAllRecords();
                if (records.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "No local records to sync", Toast.LENGTH_SHORT).show());
                    return;
                }
                for (UserRecord record : records) {
                    if (firebaseDatabase != null) {
                        firebaseDatabase.child("userRecords").child(String.valueOf(record.id)).setValue(record)
                                .addOnSuccessListener(aVoid -> {
                                    // Handle success if needed
                                })
                                .addOnFailureListener(e -> {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                });
                    }
                }
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sync to Firebase completed!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                getLocation();
            } else {
                tvLocation.setText("Location: Permission denied");
                Toast.makeText(this, "Location permission denied by user.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
