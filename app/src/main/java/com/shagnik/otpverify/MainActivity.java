package com.shagnik.otpverify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    TextView send_otp, resend_otp;
    EditText enter_otp, phone1;
    ProgressBar progress_bar;
    CountryCodePicker ccp;
    Button button;
    String verifcationId, code;
    PhoneAuthCredential credential;
    PhoneAuthProvider.ForceResendingToken token;
    boolean verificationInProgress = false;
    FirebaseFirestore fStore;
    String userOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();

        send_otp = findViewById(R.id.send_otp);
        resend_otp = findViewById(R.id.send_otp);
        enter_otp = findViewById(R.id.enter_otp);
        phone1 = findViewById(R.id.phone1);
        progress_bar = findViewById(R.id.progress_bar);
        ccp = findViewById(R.id.ccp);
        button = findViewById(R.id.button);
        userOTP = enter_otp.getText().toString();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phone1.getText().toString().trim().isEmpty() && phone1.getText().toString().trim().length() == 10) {
                    if (!verificationInProgress) {
                        String phone_num = "+" + ccp.getSelectedCountryCode() + phone1.getText().toString().trim();
                        progress_bar.setVisibility(View.VISIBLE);
                        send_otp.setVisibility(View.VISIBLE);
                        send_otp.setText("Sending OTP...");
                        requestOTP(phone_num);
                    } else {

                        phone1.setError("Phone Number is Not Valid");
                    }
                } else {

                    if (!userOTP.isEmpty() && userOTP.length() == 6) {
                        credential = PhoneAuthProvider.getCredential(verifcationId, userOTP);
                        verifyAuth(credential);
                    } else {
                        enter_otp.setError("Valid OTP is required");
                    }
                }
            }
        });


    }

    private void verifyAuth(PhoneAuthCredential credential) {


        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Authentication Successful" , Toast.LENGTH_LONG).show();
                    checkUserProfile();
                } else {
                    progress_bar.setVisibility(View.GONE);
                    send_otp.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void requestOTP(String phone_num) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone_num, 60L, TimeUnit.SECONDS, MainActivity.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progress_bar.setVisibility(View.GONE);
                send_otp.setVisibility(View.GONE);
                enter_otp.setVisibility(View.VISIBLE);
                verifcationId = s;
                token = forceResendingToken;
                button.setText("Verify");
                verificationInProgress = true;


            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(MainActivity.this, "OTP Timeout, Please Re-generate the OTP Again.", Toast.LENGTH_SHORT).show();
                resend_otp.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//
                verifyAuth(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }




        private void checkUserProfile () {
            DocumentReference docRef = fStore.collection("users").document(auth.getCurrentUser().getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Profile Do Not Exists", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

