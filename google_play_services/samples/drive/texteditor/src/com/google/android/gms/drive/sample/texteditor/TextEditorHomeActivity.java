// Copyright 2013 Google Inc. All Rights Reserved.

package com.google.android.gms.drive.sample.texteditor;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveApi.OnNewContentsCallback;
import com.google.android.gms.drive.DriveApi.OnSyncFinishCallback;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveFolder.OnChildrenRetrievedCallback;
import com.google.android.gms.drive.DriveFolder.OnCreateFileCallback;
import com.google.android.gms.drive.DriveFolder.OnCreateFolderCallback;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.DriveResource.OnMetadataRetrievedCallback;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * The home activity demonstrates the capabilities of the API, including:
 * <ul>
 * <li>Display a file picker to allow the user to open a text file: {@link #doFindFile()}
 * <li>Display a file picker to allow the user to create a new text file in a chosen location:
 * {@link #doCreateFileWithUi()}
 * <li>Display a file picker to allow the user to select a folder: {@link #doFindFolder()}
 * <li>Programmatically create a new text file: {@link #doCreateFileProgrammatically()}
 * <li>Programmatically create a new folder: {@link #doCreateFolder()}
 * <li>Programmatically query for files the app has access to: {@link #refreshFileList()}
 * <li>Request a sync, to get the latest versions of resource metadata on the device:
 * {@link #doSync()}
 * <ul>
 * <p>When a file is selected, it will launch a new EditActivity to allow reading and editing
 * of the file's metadata and contents.
 * <p>This class extends from {@link BaseDriveActivity}, which handles authorization and setup.
 */
public class TextEditorHomeActivity extends BaseDriveActivity
        implements OnChildrenRetrievedCallback, OnCreateFolderCallback {

    private static final String TAG = "TextEditor";
    private static final int REQUEST_CODE_PICK_FILE = NEXT_AVAILABLE_REQUEST_CODE;
    private static final int REQUEST_CODE_CREATE_FILE = NEXT_AVAILABLE_REQUEST_CODE + 1;
    private static final int REQUEST_CODE_PICK_FOLDER = NEXT_AVAILABLE_REQUEST_CODE + 2;

    private MetadataAdapter mFilesAdapter;
    private DriveFolder mRootFolder;
    private String mNextPageToken;

    private final List<Button> buttonsToEnable = new ArrayList<Button>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_editor_home);

        // Configure the buttons, which trigger various API actions.
        final Button createFileWithUiButton = (Button) findViewById(R.id.create);
        createFileWithUiButton.setEnabled(false);
        buttonsToEnable.add(createFileWithUiButton);
        createFileWithUiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCreateFileWithUi();
            }
        });

        final Button createFileProgrammaticallyButton = (Button) findViewById(R.id.create_prog);
        createFileProgrammaticallyButton.setEnabled(false);
        buttonsToEnable.add(createFileProgrammaticallyButton);
        createFileProgrammaticallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCreateFileProgrammatically();
            }
        });

        final Button openButton = (Button) findViewById(R.id.find);
        openButton.setEnabled(false);
        buttonsToEnable.add(openButton);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFindFile();
            }
        });

        final Button findFolderButton = (Button) findViewById(R.id.find_folder);
        findFolderButton.setEnabled(false);
        buttonsToEnable.add(findFolderButton);
        findFolderButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            doFindFolder();
          }
        });

        final Button createFolderButton = (Button) findViewById(R.id.create_folder);
        createFolderButton.setEnabled(false);
        buttonsToEnable.add(createFolderButton);
        createFolderButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            doCreateFolder();
          }
        });

        final Button syncButton = (Button) findViewById(R.id.sync);
        syncButton.setEnabled(false);
        buttonsToEnable.add(syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            doSync();
          }
        });

        // The spinner allows the user to select an account.  By default, the magic "Default"
        // account is used, which uses the account the user has selected as their default for
        // the device.
        final Spinner spinner = (Spinner) findViewById(R.id.accounts_spinner);
        ArrayAdapter<String> accountAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        for (Account account : accounts) {
            accountAdapter.add(account.name);
        }
        accountAdapter.add(DEFAULT_ACCOUNT);

        spinner.setAdapter(accountAdapter);
        spinner.setSelection(accountAdapter.getPosition(mAccountName));
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String) parent.getItemAtPosition(pos);
                if (!mAccountName.equals(selected)) {
                    Intent intent = new Intent(
                            TextEditorHomeActivity.this, TextEditorHomeActivity.class);
                    intent.putExtra(EXTRA_ACCOUNT_NAME, selected);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        // A list adapter displays a list of metadata that is the response to a query.
        mFilesAdapter = new MetadataAdapter(this);
        final ListView listView = (ListView) findViewById(R.id.recentListView);
        listView.setAdapter(mFilesAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Metadata file = (Metadata) parent.getItemAtPosition(position);
                displayFile(file.getDriveId());
            }
        });
        // The metadata buffer can contain multiple pages.  When the user scrolls to the bottom
        // of the known list, query for the next page of results.
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override
            public void onScroll(
                    AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                if (mNextPageToken != null && firstVisibleItem + 15 > totalItemCount) {
                    Query query = new Query.Builder()
                           .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                           .setPageToken(mNextPageToken)
                           .build();
                   mRootFolder.queryChildren(mGoogleApiClient, query)
                           .addResultCallback(TextEditorHomeActivity.this);
                }
            }
        });
    }

    /**
     * Invoked when the client is connected.  Retrieves the root folder, and enables the buttons
     * the user can begin interacting with teh app.
     */
    @Override
    protected void onClientConnected() {
        mRootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        for (Button button : buttonsToEnable) {
          button.setEnabled(true);
        }
        buttonsToEnable.clear();
        refreshFileList();
    }

    /**
     * Requests a metadata sync, and registers a callback which will be invoked when the sync has
     * completed.  Refreshes the recently used file list when the sync has completed.
     */
    protected void doSync() {
        Drive.DriveApi.requestSync(mGoogleApiClient).addResultCallback(new OnSyncFinishCallback() {
            @Override
            public void onSyncFinish(Status result) {
                String message;
                if (!result.getStatus().isSuccess()) {
                    message = result.getStatus().toString();
                } else {
                    message = "Sync complete.";
                }
                Toast.makeText(
                        TextEditorHomeActivity.this, message, Toast.LENGTH_LONG).show();
                refreshFileList();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFileList();
    }

    /**
     * Queries Drive for files with the MimeType 'text/plain' that the app has access to. When
     * the results are retrieved,
     * {@link TextEditorHomeActivity#onChildrenRetrieved(MetadataBufferResult)} will be
     * invoked with the results.
     */
    private void refreshFileList() {
        if (mRootFolder != null) {
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                    .build();
            mRootFolder.queryChildren(mGoogleApiClient, query).addResultCallback(this);
        }
    }

    /**
     * If the request was successful, appends the MetatadataBuffer result to the list adapter.
     */
    @Override
    public void onChildrenRetrieved(MetadataBufferResult result) {
        if (!result.getStatus().isSuccess()) {
            Log.d(TAG, "Children request failed " + result.getStatus().toString());
            return;
        }
        MetadataBuffer buffer = result.getMetadataBuffer();
        mFilesAdapter.append(buffer);
        mNextPageToken = buffer.getNextPageToken();
    }

    /**
     * Opens the file specified by the {@code id} in the EditActivity.
     */
    private void displayFile(final DriveId id) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.DRIVE_ID_EXTRA, id);
        intent.putExtra(EXTRA_ACCOUNT_NAME, mAccountName);
        startActivity(intent);
    }

    /**
     * Displays the Open File Activity, to allow the user to select a file with the MimeType
     * "text/plain".
     */
    private void doFindFile() {
        try {
            IntentSender intentSender = Drive.DriveApi.newOpenFileActivityBuilder()
                    .setMimeType(new String[] {"text/plain"})
                    .setActivityTitle("Select a text file")
                    .setActivityStartFolder(mRootFolder.getDriveId())
                    .build(mGoogleApiClient);
            TextEditorHomeActivity.this.startIntentSenderForResult(intentSender,
                    REQUEST_CODE_PICK_FILE, null, 0, 0, 0);
        } catch (SendIntentException e) {
            Log.e(TAG, "Unable to start open file activity", e);
        }
    }

    /**
     * Displays the Open File Activity, to allow the user to select a folder.
     */
    private void doFindFolder(){
        try {
            IntentSender intentSender = Drive.DriveApi.newOpenFileActivityBuilder()
                    .setMimeType(new String[] {DriveFolder.MIME_TYPE})
                    .setActivityTitle("Select a folder")
                    .setActivityStartFolder(mRootFolder.getDriveId())
                    .build(mGoogleApiClient);
            TextEditorHomeActivity.this.startIntentSenderForResult(intentSender,
                    REQUEST_CODE_PICK_FOLDER, null, 0, 0, 0);
        } catch (SendIntentException e) {
            Log.e(TAG, "Unable to start open file activity", e);
        }
    }

    /**
     * Creates a new file with MimeType 'text/plain', and an empty contents.  The final title and
     * location of the file are selected by the user via the invoked Create File Activity.
     */
    private void doCreateFileWithUi() {
        Drive.DriveApi.newContents(mGoogleApiClient).addResultCallback(new OnNewContentsCallback() {
            @Override
            public void onNewContents(ContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(TextEditorHomeActivity.this, result.getStatus().toString(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder().setMimeType(
                        "text/plain").setTitle("File created through UI").build();
                IntentSender intentSender =
                        Drive.DriveApi.newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialContents(result.getContents())
                                .setActivityTitle("Custom dialog title")
                                .build(mGoogleApiClient);
                try {
                    TextEditorHomeActivity.this.startIntentSenderForResult(intentSender,
                            REQUEST_CODE_CREATE_FILE,
                            null,
                            0,
                            0,
                            0);
                } catch (SendIntentException e) {
                    Log.e(TAG, "Unable to start create file activity", e);
                }
            }
        });
    }

    /**
     * Creates a new file in the root folder without user interaction.
     */
    private void doCreateFileProgrammatically() {
      Drive.DriveApi.newContents(mGoogleApiClient).addResultCallback(new OnNewContentsCallback() {
            @Override
            public void onNewContents(ContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(TextEditorHomeActivity.this, result.getStatus().toString(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                MetadataChangeSet metadataChangeSet =
                        new MetadataChangeSet.Builder()
                            .setMimeType("text/plain")
                            .setTitle("Programmatically created file")
                            .build();
                mRootFolder.createFile(mGoogleApiClient, metadataChangeSet, result.getContents())
                        .addResultCallback(new OnCreateFileCallback() {
                            @Override
                            public void onCreateFile(DriveFileResult result) {
                                Toast.makeText(
                                        TextEditorHomeActivity.this, result.getStatus().toString(),
                                        Toast.LENGTH_LONG).show();
                                refreshFileList();
                            }
                        });
            }
        });
    }

    /**
     * Creates a new folder within the root folder, without user interaction.
     */
    private void doCreateFolder() {
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
              .setTitle("test folder")
              .setStarred(true)
              .setMimeType("application/vnd.google-apps.folder")
              .build();
        mRootFolder.createFolder(mGoogleApiClient, metadataChangeSet).addResultCallback(this);
        refreshFileList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
          case REQUEST_CODE_PICK_FILE:
              handlePickResponse(resultCode, data);
              break;
          case REQUEST_CODE_CREATE_FILE:
              handlePickResponse(resultCode, data);
              break;
          case REQUEST_CODE_PICK_FOLDER:
              handlePickFolderResponse(resultCode, data);
              break;
          default:
              super.onActivityResult(requestCode, resultCode, data);
              break;
        }
    }

    private void handlePickResponse(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                DriveId id = data.getParcelableExtra(
                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                displayFile(id);
                break;
            default:
                Log.i(TAG, "Failed pick request");
                break;
        }
    }

    /**
     * Displays the metadata for a folder that was picked by the user.
     */
    private void handlePickFolderResponse(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                final DriveId id =
                        data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                Drive.DriveApi.getFolder(mGoogleApiClient, id).getMetadata(mGoogleApiClient)
                        .addResultCallback(new OnMetadataRetrievedCallback() {
                            @Override
                            public void onMetadataRetrieved(MetadataResult result) {
                                String title = mRootFolder.getDriveId().equals(id) ? "My Drive"
                                        : result.getMetadata().getTitle();
                                String toast = String.format("Selected folder '%s' with DriveId %s",
                                        title, id);
                                Toast.makeText(TextEditorHomeActivity.this, toast,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            default:
                Log.i(TAG, "Failed pick folder request");
                break;
        }
    }


    @Override
    public void onCreateFolder(DriveFolderResult result) {
      if (!result.getStatus().isSuccess()) {
        Toast.makeText(this, result.getStatus().toString(), Toast.LENGTH_LONG).show();
        return;
      }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFilesAdapter.clear();
    }
}
