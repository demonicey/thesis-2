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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SudokuGame sudokuGame;
    private GridLayout sudokuGrid;
    private TextView tvScore, tvLocation;
    private Button btnNewGame, btnSubmitScore;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseHelper firebaseHelper;
    private Location currentLocation;
    private int score = 0;

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

        // Initialize game and services
        sudokuGame = new SudokuGame();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firebaseHelper = new FirebaseHelper();

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
                            if (sudokuGame.isValidMove(row, col, num)) {
                                score += 10;
                                updateScore();
                            }
                        } catch (NumberFormatException e) {
                            sudokuGame.setCell(row, col, 0);
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
        updateScore();
    }

    private void updateScore() {
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
                        score += 50;
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
            firebaseHelper.saveUserRecord("user123", score, currentLocation.getLatitude(), currentLocation.getLongitude());
            Toast.makeText(this, "Score submitted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
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
