package com.example.sudoku;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        databaseHelper = DatabaseHelper.getDatabase(this);
        httpClient = new OkHttpClient();
        try {
            cryptoHelper = new CryptoHelper(this);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | java.security.UnrecoverableKeyException e) {
            Toast.makeText(this, "Failed to initialize crypto: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    tvLocation.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
                    // Location-based bonus
                    if (isAtBonusLocation(location)) {
                        locationBonus = 50;
                        updateScore();
                        Toast.makeText(this, "Bonus location! +50 points", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
                UserRecord record = new UserRecord("user123", score, encryptedLocation, System.currentTimeMillis());
                new Thread(() -> {
                    databaseHelper.userRecordDao().insert(record);
                    runOnUiThread(() -> Toast.makeText(this, "Score saved locally!", Toast.LENGTH_SHORT).show());
                }).start();
            } catch (Exception e) {
                // Fallback to unencrypted data
                String locationData = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                UserRecord record = new UserRecord("user123", score, locationData, System.currentTimeMillis());
                new Thread(() -> {
                    databaseHelper.userRecordDao().insert(record);
                    runOnUiThread(() -> Toast.makeText(this, "Score saved locally (unencrypted)!", Toast.LENGTH_SHORT).show());
                }).start();
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulateSync() {
        double lat, lon;
        if (currentLocation != null) {
            lat = currentLocation.getLatitude();
            lon = currentLocation.getLongitude();
        } else {
            // Hardcoded values for simulation (Klaipeda University approx.)
            lat = 55.7033;
            lon = 21.1443;
        }

        try {
            String locationData = lat + "," + lon;
            String encryptedLocation = "";
            if (cryptoHelper != null) {
                encryptedLocation = cryptoHelper.encrypt(locationData);
            } else {
                encryptedLocation = locationData; // Fallback if crypto fails
            }

            JSONObject json = new JSONObject();
            json.put("userId", "user123");
            json.put("score", score);
            json.put("encryptedLocation", encryptedLocation);
            json.put("timestamp", System.currentTimeMillis());

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://httpbin.org/post") // Mock API endpoint for testing
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Sync simulated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Sync failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }
}
