package com.globalrescue.mzafar.pocbeta_1.ui;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.R;
import com.globalrescue.mzafar.pocbeta_1.models.CountryModel;
import com.globalrescue.mzafar.pocbeta_1.models.GoogleTranslatePOSTRequestModel;
import com.globalrescue.mzafar.pocbeta_1.models.LanguageModel;
import com.globalrescue.mzafar.pocbeta_1.nuance.TTS;
import com.globalrescue.mzafar.pocbeta_1.root.TravelCompanero;
import com.globalrescue.mzafar.pocbeta_1.utilities.ConnectivityReceiver;
import com.globalrescue.mzafar.pocbeta_1.utilities.DataUtil;
import com.globalrescue.mzafar.pocbeta_1.utilities.NetworkUtils;
import com.globalrescue.mzafar.pocbeta_1.utilities.PackageManagerUtils;
import com.globalrescue.mzafar.pocbeta_1.utilities.PermissionUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageContext;
import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ImageTranslationActivity extends AppCompatActivity implements View.OnClickListener,
        ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCvIQEnhzPQ30Y55dHSYWn0HhrR45EDCQ4";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = "ImgTranslation";
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private ImageView mMainImage;
    private ProgressBar mImageProgress;
    private Button mUploadImage;
    private ImageButton mPlayAudio;

    private CountryModel mNativeCountry;
    private CountryModel mForeignCountry;
    private LanguageModel mNativeLanguageModel;
    private LanguageModel mForeignLanguageModel;

    private DataUtil dataUtil;
    private NetworkUtils networkUtils;
    private String[] langHints = new String[1];

    private String translatedText;
    private String previousText;
    private boolean isNativeToForeignConversion = false;

    private TTS ttsService;

    private AlertDialog connectionAlert;

    private ConnectivityReceiver connectivityReceiver;

    DataUtil.FirebaseDataListner NativeLangModelListner = new DataUtil.FirebaseDataListner() {
        @Override
        public void onResultNotification(Object tClass) {
            mNativeLanguageModel = (LanguageModel) tClass;
//            langHints[1] = mNativeLanguageModel.getYandexCode();
        }

        @Override
        public void onResultListNotification(List<?> classList) {

        }

    };

    DataUtil.FirebaseDataListner ForeignLangModelListner = new DataUtil.FirebaseDataListner() {
        @Override
        public void onResultNotification(Object tClass) {
            mForeignLanguageModel = (LanguageModel) tClass;
            langHints[0] = mForeignLanguageModel.getYandexCode();
        }

        @Override
        public void onResultListNotification(List<?> classList) {

        }
    };

    AudioPlayer.Listener audioPlayerListener = new AudioPlayer.Listener() {
        @Override
        public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {
            Log.i(TAG, "\nonBeginPlaying");

            ttsService.setTtsTransaction(null);

            //The TTS Audio will begin playing.

            ttsService.setState(TTS.State.PLAYING);
        }

        @Override
        public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {
            Log.i(TAG, "\nonFinishedPlaying");

            //The TTS Audio has finished playing
            ttsService.setState(TTS.State.IDLE);
            mImageProgress.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_translation);

        mImageDetails = findViewById(R.id.image_details);
        mMainImage = findViewById(R.id.main_image);
        mImageProgress = findViewById(R.id.pg_img_details);
        mUploadImage = findViewById(R.id.btn_upload_image);
        mPlayAudio = findViewById(R.id.btn_play_audio_from_img);

        mUploadImage.setOnClickListener(this);
        mPlayAudio.setOnClickListener(this);

        Bundle extraBundle = getIntent().getExtras();
        mNativeCountry = (CountryModel) extraBundle.getSerializable("NATIVE_COUNTRY_MODEL");
        mForeignCountry = (CountryModel) extraBundle.getSerializable("FOREIGN_COUNTRY_MODEL");

        dataUtil = new DataUtil();
        dataUtil.getLanguagenCodeFirestore(mNativeCountry.getCountry(), NativeLangModelListner);
        dataUtil.getLanguagenCodeFirestore(mForeignCountry.getCountry(), ForeignLangModelListner);

        checkConnection();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Code for Listening to Connection Status Broadcast on Android N and Above

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        /*register connection status listener*/
        TravelCompanero.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(connectivityReceiver);
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if(!isConnected){
            Log.i(TAG, "checkConnection: Not Connected with Internet");
            showConnectionAlert();
        }
    }

    // Show an Alert in case Internet Connection is not Present
    private void showConnectionAlert() {

        if(connectionAlert != null){
            connectionAlert.dismiss();
        }
        // Create an Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the Alert Dialog Message
        builder.setMessage("Internet Connection Required")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Retry",
                        (dialog, id) -> {
                            // Restart the Activity
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        });

        connectionAlert = builder.create();
        connectionAlert.show();

    }

    @Override
    public void onClick(View v) {
        if (v == mUploadImage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ImageTranslationActivity.this);
            builder
                    .setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
            builder.create().show();
            mPlayAudio.setVisibility(View.INVISIBLE);
        }
        if (v == mPlayAudio){
            if(!getTranslatedText().equals(getPreviousText())){

                Log.i(TAG, "onClick: Play Audio -> if Condition = true");

                if(ttsService != null){
                    ttsService = null;
                }
                setPreviousText(getTranslatedText());
                ttsService = new TTS(this, audioPlayerListener, isNativeToForeignConversion,
                        mNativeLanguageModel, mForeignLanguageModel, getTranslatedText());
                mImageProgress.setVisibility(View.VISIBLE);
                ttsService.toggleTTS();
            }else{
                mImageProgress.setVisibility(View.VISIBLE);
                ttsService.toggleTTS();
            }
        }
    }

    public void startGalleryChooser() {
        if (PermissionUtil.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtil.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtil.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtil.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.i(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature textDetection = new Feature();
                //Changed LABEL_DETECTION to TEXT_DETECTION
                textDetection.setType("TEXT_DETECTION");
                textDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(textDetection);
            }});

            // TODO (1): 9/28/2018 - Need to devise some plan and add support for langs to use in LanguageHints
            ImageContext imageContext = new ImageContext();
//            String[] langs = {"hi"};
            imageContext.setLanguageHints(Arrays.asList(langHints));
            Log.i(TAG, "instance initializer: "+langHints[0]);

            annotateImageRequest.setImageContext(imageContext);
            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.i(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static class TextDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<ImageTranslationActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        TextDetectionTask(ImageTranslationActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.i(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.i(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.i(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            ImageTranslationActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.image_details);
//                ProgressBar mImageResultProgress = activity.findViewById(R.id.pg_img_details);
                imageDetail.setText(R.string.getting_translations_message);

                activity.GoogleTranslation(result,activity.mForeignLanguageModel.getYandexCode(),
                        activity.mNativeLanguageModel.getYandexCode());

//                mImageResultProgress.setVisibility(View.INVISIBLE);

            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message_img);
        mImageProgress.setVisibility(View.VISIBLE);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> textDetectionTask = new TextDetectionTask(this, prepareAnnotationRequest(bitmap));
            textDetectionTask.execute();
        } catch (IOException e) {
            Log.i(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
//        StringBuilder message = new StringBuilder("Here are your results:\n\n");
        StringBuilder message = new StringBuilder();

        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            message.append(labels.get(0).getDescription());
            //Use below for detailed  label extraction
//            for (EntityAnnotation label : labels) {
//                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
//                message.append("\n");
//            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }

    void GoogleTranslation(String sourceText, String sourceLanguage, String targetLanguage) {
        String translationResult = null; // Returns the translated text as a String
        try {
            translationResult = new GoogleTextTranslationTask(this).execute(sourceText, sourceLanguage, targetLanguage).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("G-Translation Result", translationResult);
    }

    class GoogleTextTranslationTask extends AsyncTask<String, Void, String> {

        private final WeakReference<ImageTranslationActivity> mActivityWeakReference;

        GoogleTextTranslationTask(ImageTranslationActivity activity){
            mActivityWeakReference = new WeakReference<>(activity);
        }
        @Override
        protected String doInBackground(String... strings) {
            networkUtils = new NetworkUtils();
            String Result = networkUtils.GoogleTextTranslationREST(strings[0], strings[1], strings[2]);
            if (Result != null && !Result.equals("")) {
                return Result;
            }
            return "Got Nothing!";
        }

        @Override
        protected void onPostExecute(String translatedText) {
            super.onPostExecute(translatedText);
            ImageTranslationActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.image_details);
                ProgressBar mImageResultProgress = activity.findViewById(R.id.pg_img_details);
                ImageButton mPlayAudio = activity.findViewById(R.id.btn_play_audio_from_img);

                activity.setTranslatedText(translatedText);
                mImageResultProgress.setVisibility(View.INVISIBLE);
                imageDetail.setText(translatedText);
                mPlayAudio.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.i(TAG, "onNetworkConnectionChanged: -> Network Status has changed -> Status: "+isConnected);
        if(!isConnected) {
            showConnectionAlert();
        }
        if(isConnected && (connectionAlert != null) ){
            connectionAlert.dismiss();
        }
    }

    public String getPreviousText() {
        return previousText;
    }

    public void setPreviousText(String previousText) {
        this.previousText = previousText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
