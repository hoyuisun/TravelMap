// Copyright 2013 Google Inc. All Rights Reserved.

package com.google.android.gms.drive.sample.texteditor;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveFile.OnContentsOpenedCallback;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.DriveResource.OnMetadataRetrievedCallback;
import com.google.android.gms.drive.DriveResource.OnMetadataUpdatedCallback;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An activity that allows you to edit a text document. It receives the {@link DriveId} to edit via
 * an extra {@link #DRIVE_ID_EXTRA}. It extends from {@link BaseDriveActivity}, which handles
 * authorization and service connections.
 * <p>This activity demonstrates:
 * <ul>
 * <li>Reading metadata: {@link #getMetadata()}
 * <li>Writing metadata: {@link #doSaveMetadata()}
 * <li>Reading contents: {@link #readContents()}
 * <li>Writing contents: {@link #doSaveContents()}
 * <ul>
 */
public class EditActivity extends BaseDriveActivity
        implements DownloadProgressListener, OnContentsOpenedCallback {

    static final String DRIVE_ID_EXTRA = "file";

    private static final String TAG = "EditActivity";

    private DriveFile mDriveFile;
    private DriveId mDriveId;

    // only accessed from UI thread
    private ProgressDialog mProgress;

    private EditText mContents;

    private Metadata mMetadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        final Button button = (Button) findViewById(R.id.back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSaveContents();
                doSaveMetadata();
            }
        });
        mContents = (EditText) findViewById(R.id.contents);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setMax(100);
        mProgress.setTitle("Loading file");
        mProgress.show();
    }

    @Override
    protected void onClientConnected() {
        mDriveId = getIntent().getParcelableExtra(DRIVE_ID_EXTRA);
        mDriveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
        // kick off requests for metadata
        getMetadata();
        readContents();
    }

    /**
     * Retrieves the metadata for the selected file, and sets the title.
     */
    private void getMetadata() {
        mDriveFile.getMetadata(mGoogleApiClient).addResultCallback(
                new OnMetadataRetrievedCallback() {
            @Override
            public void onMetadataRetrieved(MetadataResult result) {
                if (!checkForError(result.getStatus())) {
                    Metadata metadata = result.getMetadata();
                    EditText titleText = (EditText) findViewById(R.id.title);
                    titleText.setText(metadata.getTitle());
                    mMetadata = metadata;
                }
            }
        });
    }

    /**
     * Asynchronously opens the contents of the file for read.  This class is provided as the
     * progress listener and callback.  If the file is not yet available locally,
     * {@link #onProgress(long, long)} is invoked as the file is downloaded.
     * {@link #onOpen(ContentsResult)} is invoked when the contents are available.
     */
    private void readContents() {
        mDriveFile.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, this)
                .addResultCallback(this);
    }

    /**
     * Receives announcements of download progress, and updates the progress dialog appropriately.
     */
    @Override
    public void onProgress(long bytesDownloaded, long bytesExpected) {
        double percent = ((double) bytesDownloaded / bytesExpected) * 100;
        mProgress.setProgress((int) percent);
    }

    /**
     * Invoked when the openContents request from {@link #readContents()} is complete.  Triggers
     * a read of the file.
     */
    @Override
    public void onOpen(ContentsResult result) {
        if (!checkForError(result.getStatus())) {
            readContents(result.getContents());
            mProgress.dismiss();
        }
    }

    public void readContents(final Contents contents) {
        InputStream inputStream = contents.getInputStream();
        new AsyncTask<InputStream, Void, String>() {

            @Override
            protected String doInBackground(InputStream... params) {
                try {
                    return getStringFromFile(params[0]);
                } catch (Exception e) {
                    Log.d(TAG, "contents failed", e);
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                setContents(result);
                mDriveFile.commitAndCloseContents(mGoogleApiClient, contents);
            }
        }.execute(inputStream);

        mProgress.dismiss();
    }

    private void setContents(String contents) {
        if (contents == null) {
            // initialize with empty contents
            mContents.setText("");
        }
        try {
            mContents.setText(contents);
        } catch (Exception e) {
            Log.e(TAG, "Error reading file", e);
        }
    }

    private String getStringFromFile(InputStream stream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && builder.length() < 1000) {
            builder.append(line).append("\n");
        }
        reader.close();
        return builder.toString();
    }

    /**
     * Saves the changes to the contents in a background task.  This uses a ApiClientAsyncTask,
     * which creates a new client connection owned by that task, to ensure that the the lifecycle
     * of the client connection matches that of the AsyncTask.
     */
    private void doSaveContents() {
        final String newContents = mContents.getText().toString();
        new ApiClientAsyncTask<String, Void, Status>(getAccountName(), this) {

            @Override
            protected com.google.android.gms.common.api.Status doInBackgroundConnected(String... params) {
                ContentsResult result = mDriveFile.openContents(
                        mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
                if (!result.getStatus().isSuccess()) {
                    Log.d(TAG, "Failed to open");
                    return result.getStatus();
                }
                Contents contents = result.getContents();
                Writer writer = new OutputStreamWriter(contents.getOutputStream());
                try {
                    writer.write(params[0]);
                } catch (IOException e) {
                    Log.d(TAG, "Failed to write", e);
                    return null;
                } finally {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Log.d(TAG, "Error closing Contents output stream", e);
                    }
                }

                return mDriveFile.commitAndCloseContents(mGoogleApiClient, contents).await();
            }

            @Override
            protected void onPostExecute(com.google.android.gms.common.api.Status result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "Save failed " + result);
                    Toast.makeText(
                            EditActivity.this,
                            "Save failed.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute(newContents);
    }

    /**
     * Saves any changes to the metadata.
     */
    private void doSaveMetadata() {
        // check if title changed.
        EditText title = (EditText) findViewById(R.id.title);
        String titleString = title.getText().toString();
        if (titleString.equals(mMetadata.getTitle())) {
            // no change
            return;
        }
        MetadataChangeSet changeSet =
                new MetadataChangeSet.Builder().setTitle(titleString).build();
        mDriveFile.updateMetadata(mGoogleApiClient, changeSet).addResultCallback(
                new OnMetadataUpdatedCallback() {

            @Override
            public void onMetadataUpdated(MetadataResult result) {
                if (!checkForError(result.getStatus())) {
                    Metadata metadata = result.getMetadata();
                    EditText titleText = (EditText) findViewById(R.id.title);
                    titleText.setText(metadata.getTitle());
                    mMetadata = metadata;
                    Toast.makeText(EditActivity.this, "Metadata saved", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private boolean checkForError(Status status) {
        if (!status.isSuccess()) {
            mProgress.dismiss();
            Log.d(TAG, "Error toast: " + status);
            Toast.makeText(this, status.toString(), Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }
}