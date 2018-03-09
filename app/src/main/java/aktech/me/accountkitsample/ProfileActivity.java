package aktech.me.accountkitsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;

public class ProfileActivity extends AppCompatActivity {

    private TextView accountIdTextView;
    private TextView emailTextView;
    private TextView phoneNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        accountIdTextView = findViewById(R.id.textView_account_id);
        emailTextView = findViewById(R.id.textView_email);
        phoneNumberTextView = findViewById(R.id.textView_phone);

        showAccountInfo();
    }


    public void logOut(View view) {
        AccountKit.logOut();
        gotoMainActivity();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showAccountInfo() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                String accountId = account.getId();
                PhoneNumber phoneNumber = account.getPhoneNumber();
                String phoneNumberString =
                        (phoneNumber != null) ? phoneNumberString = phoneNumber.toString() : "";
                String email = account.getEmail();

                accountIdTextView.setText(accountId);
                phoneNumberTextView.setText(phoneNumberString);
                emailTextView.setText(email);
            }

            @Override
            public void onError(final AccountKitError error) {
                Toast.makeText(ProfileActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
