# Introduction
In this step-by-step tutorial we're building an Android example app to show how to integrate Facebook Account Kit with your app and how to handle different login/logout scenarios. In the final version of the app, The user can login with his email or phone number, if he's logged in successfully he's redirected to a simple profile screen that shows some information about his account along with a logout button.

## What is Account Kit
A passwordless login solution made by Facebook to make app user login much easier and avoid the common issues of using saved passwords. see https://developers.facebook.com/docs/accountkit

## Setup your Facebook Application

1- **Create The Application**

To enable Account Kit in your Android app you need to create a Facebook application with your developer account.

If you don't have a facebook developer account, login to https://developers.facebook.com/ with your regular facebook account.

To create a new Facebook app login to your developer account and do the following:

* Go to My Apps.
* Click "Add a new app" button or find "Add a new app" in the upper menu.
* Enter display name.
* Press Create App ID button and verify the CAPTCHA.
* and your done, Now you should be seeing your new application dashboard.

2- **Add Android Platform**

In the left side menu, go to "settings", select "basic", find the "Add Platform" button, click the button and select "Android".

3- **Add Key Hash**

Now in the basic settings, a new section named "Android" should be created. You need to add a key hash in the "key hashes" field.

To generate the key hash run this command if you're a Mac or *nix user
```
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
```
If your using windows, run this command
```
keytool -exportcert -alias androiddebugkey -keystore %HOMEPATH%\.android\debug.keystore | openssl sha1 -binary | openssl base64
```
Now copy the generated hash value into the "key hashes" field.

**Note:** If your application is released to the store, you need to add the release key as well, for more information about signing and releasing your app see https://developer.android.com/studio/publish/app-signing.html#sign-apk

4- **Add Account Kit Product**

In the left side menu, find the "Products" section at the bottom and click the plus sign to add a new product. go to the Account Kit product part and click "Set up". Now a new section named "Account Kit" should be created in the left side menu under "Products". Select "Settings" menu item in the new section and click the "Get Started" button. Now make sure the following settings are turned on:
  * **Allow Email Login:** Set it to ON if you want the user to login with his email address.
  * **Allow SMS Login:** Set it to ON if you want the user to login with his phone number.
  * **Enable Client Access Token Flow:** This is how we implement Account Kit in this tutorial. If you want your own web server to handle the user authentication you need it turned off, to learn more see https://developers.facebook.com/docs/accountkit/accesstokens

  Now your Facebook application is created and all set.



## Setup your Android Project
In order to start working on your app, you need to add the Facebook SDK dependencies to your project. You also need to add some configuration data to enable communication with your created Facebook app. This data include the application Id and some hash keys along with a couple of medata data tags. and finally, you need to add the imported activities and UI components that do the work for you with the help of the Facebook SDK. Here is what we need to do in details:

* First create your Android project in Android Studio and name it whatever you like.

* In your build.gradle file add the Facebook SDK dependency
```
repositories {
  jcenter()
}

dependencies {
  implementation 'com.facebook.android:account-kit-sdk:4.+'
}
```

* Add string resources for the required hash keys as follows:
  * **The Facebook App Id:**
  ```
    <!--Replace 123 with your facebook app Id-->
    <string name="FACEBOOK_APP_ID">123</string>
  ```
  * **ak_login_protocol_scheme**: *ak followed by the Facebook app ID* This is only needed for email login.
  ```
    <!--Replace 123 with your facebook app Id-->
    <string name="ak_login_protocol_scheme">ak123</string>
  ```

  * **Account Kit Client Token** you can find this values in *Prouducts -> Account Kit -> Settings*
  ```
  <string name="ACCOUNT_KIT_CLIENT_TOKEN">[YOUR ACCOUNT CLIENT TOKEN HERE]</string>
  ```


* Add these metadata tags in your application manifest:
```
 <meta-data
     android:name="com.facebook.accountkit.ApplicationName"
     android:value="@string/app_name" />
 <meta-data
     android:name="com.facebook.sdk.ApplicationId"
     android:value="@string/FACEBOOK_APP_ID" />
 <meta-data
     android:name="com.facebook.accountkit.ClientToken"
     android:value="@string/ACCOUNT_KIT_CLIENT_TOKEN" />
```

* **Facebook SDK Activities:**
Those are the activities that do the work for you, they handle the user input, send the login credentials to Facebook backend to be processed, and get back the authentication results so you can inform your user and take the proper action in your app.

  Add this code to your application manifest
 ```
 <activity android:name="com.facebook.accountkit.ui.AccountKitActivity" />

 <!--Only needed for Email login-->
 <activity android:name="com.facebook.accountkit.ui.AccountKitEmailRedirectActivity">
     <intent-filter>
         <action android:name="android.intent.action.VIEW" />
         <category android:name="android.intent.category.DEFAULT" />
         <category android:name="android.intent.category.BROWSABLE" />
         <data android:scheme="@string/ak_login_protocol_scheme" />
     </intent-filter>
 </activity>
```

## Now Let's Code
In our example we're going to have both email and SMS login to demnostrate all the cases.

**Import any missing classes as you go!**

change the layout of MainActivity into this

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    tools:context="aktech.me.accountkitsample.MainActivity">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:onClick="phoneLogin"
        android:text="Phone Login" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:onClick="emailLogin"
        android:text="Email Login" />

</LinearLayout>

```

Here we have two buttons to each login type. Now add a new activity to your project and name it ProfileActivity.

In MainActivity add this method to show the other activity:

```
 private void goToProfileInActivity() {
     Intent intent = new Intent(this, ProfileActivity.class);
     startActivity(intent);
 }
```

In the onCreate callback check if there is a logged in user, if that's the case show the other activity, otherwise we still show the main activity with both login options 

```
 protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);

     AccessToken accessToken = AccountKit.getCurrentAccessToken();
     if (accessToken != null) {
         goToProfileInActivity();
     }
 }
```

We use getCurrentAccessToken static method to check if there's a current logged in user.

Now in the MainActivity class add these methods to implement the login actions

```
    public void phoneLogin(View view) {
        login(LoginType.PHONE);
    }

    public void emailLogin(View view) {
        login(LoginType.EMAIL);
    }

    private void login(LoginType loginType) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        loginType,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);
    }

```
In the login we use the LoginType enum valus to determine the login type. We use the configurationBuilder to build the intent extras we need to pass to AccountKitActivity. This is the activity that does all the work for us, it's imported as a part of the Facebook SDK and it's responsibile for communicating with the Facebook backend to verify the user and retrieve the feedback from the server.

Now run your project and make sure that each login button shows the right activity, Don't enter any email or phone number yet, press the back button after you see each login activity.  

The login activity sends the user input to Facebook and gets the results from there, but in your own code you need to need to act accordingly to different results: namely login success, login failure and cancelled login. You may have noticed that we use startActivityForResult to show the login activity, this is how we can handle the different results from the login activity. We handle the different results in the onActivityResult callback. 

Add this code to MainActivity

```
    public static int REQUEST_CODE = 999;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();
                goToProfileInActivity();
            }

            Toast.makeText(
                    this,
                    toastMessage,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

``` 
Here we check the loginResult object state, give some useful feedback in case of error or canceled login and we move to the ProfileActivity only if the user could succesfully login. Now run your project and make sure everything works, you can test with your email and your phone number. When you try email login with your email address, you recieve and new email message that verifies you and gets you back to the profile acitivity once you're verified, When you try phone login with your phone number, you get a new SMS message with the verification code, once you enter that code in your app you're verified and move to the profile acitivity.

**Note for Email Login:** If you're logged in to the facebook mobile app in your device and your account is associated with a verified phone number, when you test your code you'll notice that you're moved directly to the profile activity without any SMS verification. This is called instant verification. see https://developers.facebook.com/docs/accountkit/overview/#instantverification

Now the Profile Activity is protected by the login process but it doesn't show anything useful. It doesn't show any information about the currently logged in user and what about logout? Let's fix that.

Change ProfileActivity layout 

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    tools:context="aktech.me.accountkitsample.ProfileActivity">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:layout_marginVertical="32dp">

            <TextView
                android:id="@+id/textView_account_id_label"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/account_id"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/textView_account_id"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                tools:text="123456" />
        </LinearLayout>

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:layout_marginVertical="32dp">

            <TextView
                android:id="@+id/textView_email_label"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/email"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/textView_email"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"

                tools:text="123456" />
        </LinearLayout>

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:layout_marginVertical="32dp">

            <TextView
                android:id="@+id/textView_phone_label"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:text="@string/phone_number"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/textView_phone"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"

                tools:text="123456" />
        </LinearLayout>


        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:layout_centerHorizontal="true"
            android:layout_marginHorizontal="80dp"
            android:onClick="logOut"
            android:text="Log out" />
    </LinearLayout>
</LinearLayout>
```


In ProfileActivity add those private fields

```
    private TextView accountIdTextView;
    private TextView emailTextView;
    private TextView phoneNumberTextView;
```

in the onCreate method add this

```
  accountIdTextView = findViewById(R.id.textView_account_id);
  emailTextView = findViewById(R.id.textView_email);
  phoneNumberTextView = findViewById(R.id.textView_phone);
```

Next we show the account information for the logged in user using this method

```
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
```

Here we ask Account Kit to get the current account. If the Account Kit could successfuly get the info we show it in the UI, otherwise if something goes wrong we show some feedback, We call this method in the onCreate callback to start the activity with the proper data. Change the onCreate code to call this method

```
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_profile);

      accountIdTextView = findViewById(R.id.textView_account_id);
      emailTextView = findViewById(R.id.textView_email);
      phoneNumberTextView = findViewById(R.id.textView_phone);

      showAccountInfo();
  }
```


We need to move back to the main activity on logout. For that add this method in ProfileActivity

```
  private void gotoMainActivity() {
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
  }
```

We call this method when the user clicks the logout button

```
  public void logOut(View view) {
      AccountKit.logOut();
      gotoMainActivity();
  }
```

And that's it!

