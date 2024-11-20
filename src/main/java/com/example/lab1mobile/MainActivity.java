package com.example.lab1mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private EditText coefficientAInput, coefficientBInput, coefficientCInput;
    private EditText leftBoundInput, rightBoundInput, toleranceInput;
    private Spinner equationTypeSpinner;
    private TextView resultText;
    private Button calculateButton, saveButton, loadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coefficientAInput = findViewById(R.id.coefficientAInput);
        coefficientBInput = findViewById(R.id.coefficientBInput);
        coefficientCInput = findViewById(R.id.coefficientCInput);
        leftBoundInput = findViewById(R.id.leftBoundInput);
        rightBoundInput = findViewById(R.id.rightBoundInput);
        toleranceInput = findViewById(R.id.toleranceInput);
        equationTypeSpinner = findViewById(R.id.equationTypeSpinner);
        resultText = findViewById(R.id.resultText);
        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);
        loadButton = findViewById(R.id.loadButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.equation_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equationTypeSpinner.setAdapter(adapter);

        calculateButton.setOnClickListener(view -> solveEquation());

        saveButton.setOnClickListener(view -> saveDataToFile());

        loadButton.setOnClickListener(view -> loadDataFromFile());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void solveEquation() {
        try {
            double a = Double.parseDouble(coefficientAInput.getText().toString());
            double b = Double.parseDouble(coefficientBInput.getText().toString());
            double c = Double.parseDouble(coefficientCInput.getText().toString());
            double left = Double.parseDouble(leftBoundInput.getText().toString());
            double right = Double.parseDouble(rightBoundInput.getText().toString());
            double tolerance = Double.parseDouble(toleranceInput.getText().toString());
            String equationType = equationTypeSpinner.getSelectedItem().toString();

            double root = 0;
            int iterations = 0;

            switch (equationType) {
                case "Лінійне (ax + b = 0)":
                    root = -b / a;
                    iterations = 1;
                    break;
                case "Квадратне (ax^2 + bx + c = 0)":
                    root = solveQuadratic(a, b, c);
                    iterations = 1;
                    break;
                case "ax^3 + bx + c = 0 (метод бісекції)":
                    root = solveBisection(a, b, c, left, right, tolerance);
                    iterations = (int) Math.ceil(Math.log((right - left) / tolerance) / Math.log(2));
                    break;
                default:
                    Toast.makeText(this, "Невірний тип рівняння", Toast.LENGTH_SHORT).show();
                    return;
            }

            resultText.setText("Корінь: " + String.format("%.5f", root) +
                    "\nТочність: " + tolerance +
                    "\nІтерацій: " + iterations);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка", Toast.LENGTH_SHORT).show();
        }
    }

    private double solveQuadratic(double a, double b, double c) {
        double discriminant = b * b - 4 * a * c;
        if (discriminant >= 0) {
            return (-b + Math.sqrt(discriminant)) / (2 * a);
        } else {
            Toast.makeText(this, "Нема коренів", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    private double solveBisection(double a, double b, double c, double left, double right, double tolerance) {
        double mid;
        while ((right - left) / 2 > tolerance) {
            mid = (left + right) / 2;
            if (f2(a, b, c, mid) * f2(a, b, c, left) < 0) {
                right = mid;
            } else {
                left = mid;
            }
        }
        return (left + right) / 2;
    }

    private double f(double a, double b, double c, double x) {
        return a * x * x + b * x + c;
    }

    private double f2(double a, double b, double c, double x) {
        return a * x * x * x + b * x + c;
    }

    private void saveDataToFile() {
        try {
            double a = Double.parseDouble(coefficientAInput.getText().toString());
            double b = Double.parseDouble(coefficientBInput.getText().toString());
            double c = Double.parseDouble(coefficientCInput.getText().toString());
            double left = Double.parseDouble(leftBoundInput.getText().toString());
            double right = Double.parseDouble(rightBoundInput.getText().toString());
            double step = (right - left) / 100;
            String equationType = equationTypeSpinner.getSelectedItem().toString();

            StringBuilder data = new StringBuilder();
            data.append("x, f(x)\n");

            double y = 0;
            for (double x = left; x <= right; x += step) {
                switch (equationType) {
                    case "Лінійне (ax + b = 0)":
                        y = a * x + b;
                        break;
                    case "Квадратне (ax^2 + bx + c = 0)":
                        y = f(a, b, c, x);
                        break;
                    case "ax^3 + bx + c = 0 (метод бісекції)":
                        y = f2(a, b, c, x);
                        break;
                    default:
                        Toast.makeText(this, "Невірний тип рівняння", Toast.LENGTH_SHORT).show();
                        return;
                }
                data.append(String.format("%.5f, %.5f\n", x, y));
            }

            FileOutputStream fos = openFileOutput("function_data.txt", MODE_PRIVATE);
            fos.write(data.toString().getBytes());
            fos.close();

            Toast.makeText(this, "Дані збережені в файл function_data.txt", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка збереження даних", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataFromFile() {
        try {
            FileInputStream fis = openFileInput("function_data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder data = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                data.append(line).append("\n");
            }

            fis.close();

            resultText.setText(data.toString());
            Toast.makeText(this, "Дані завантажені з файлу function_data.txt", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка завантаження даних", Toast.LENGTH_SHORT).show();
        }
    }

}
