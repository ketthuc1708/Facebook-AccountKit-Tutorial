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


public class ProfileActivity extends AppCompatActivity {

    private TextView accountIdTextView;
    private TextView emailTextView;
    private TextView phoneNumberTextView;

    @Override
    protected void onCreat{
        super.onCreate;
        setContentView;

        accountIdTextView = findViewById(R.id.textView_account_id);
        emailTextView = 

        showAccountInfo();
    }


    public void logOut{
        AccountKit.logOut;
        gotoMainActivity;
    }

    private void gotoMainActivity {
        Intent intent = new Intent;
        startActivity;
    }

    private void showAccountInfo() {
        AccountKit.getCurrentAccount {
            @Override
            public void onSuccess(final Account account) {
                String accountId = account.getId();
                PhoneNumber phoneNumber = account.getPhoneNumber();
                String phoneNumberString =
               
                String email = account.;

                accountIdTextView.setText(accountId);
                phoneNumberTextView.setText(phoneNumberString);
                emailTextView.setText(email);
            }

            @Override
            public void onError(final AccountKitError error) {
              
            }
        });
    }
}
