package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gopalapriyadasa.textabase_engine.FolioLibraryService;

import java.util.HashMap;
import java.util.Locale;
import java.util.TimerTask;

/**
 * Created by Gopa702 on 6/29/2016.
 */
public class DatabasePageGroup implements MessageBoxDelegate {

    private MainActivity mainActivity;

    //private View downloadPanel;
    private View parentView;
    private TextView labelTitle;
    private TextView labelMessage;
    //private TextView labelDownloadProgress;
    //private TextView labelLowSpace;
    //private TextView labelEnoughSpace;
    //private ProgressBar progressBarDownload;
    private TextView buttonForceUpdate;
    private TextView buttonShowText;
    private TextView buttonLocateDatabaseFile;
    private TextView buttonShowDownloads;
    private View downloadingPanel;
    private TextView downloadingTitle;
    private ProgressBar downloadingProgress;
    private TextView downloadingDetails;
    private Handler mHandler;

    public DatabasePageGroup(MainActivity ma) {
        // linking views
        mainActivity = ma;
        parentView = ma.findViewById(R.id.databaseFragment);

        mHandler = new Handler();
        //downloadPanel = parentView.findViewById(R.id.downProgressPanel);
        labelTitle = (TextView) parentView.findViewById(R.id.statusTitle);
        labelMessage = (TextView) parentView.findViewById(R.id.messageText);
        //labelDownloadProgress = (TextView) parentView.findViewById(R.id.progressAsPercentage);
        //progressBarDownload = (ProgressBar) parentView.findViewById(R.id.progressBar);
        buttonForceUpdate = (TextView) parentView.findViewById(R.id.buttonForceUpdate);
        buttonShowText = (TextView) parentView.findViewById(R.id.buttonShowText);
        buttonLocateDatabaseFile = (TextView) parentView.findViewById(R.id.buttonRetry);
        buttonShowDownloads = (TextView)parentView.findViewById(R.id.buttonShowDownloads);
        //labelLowSpace = (TextView) parentView.findViewById(R.id.labelLowSpace);
        //labelEnoughSpace = (TextView) parentView.findViewById(R.id.labelEnoughSpace);

        downloadingPanel = parentView.findViewById(R.id.db_downloading);
        downloadingTitle = (TextView)parentView.findViewById(R.id.textView17);
        downloadingProgress = (ProgressBar)parentView.findViewById(R.id.progressBar);
        downloadingDetails = (TextView)parentView.findViewById(R.id.textView18);

        // initialization of views
        downloadingPanel.setVisibility(View.GONE);
        //labelLowSpace.setVisibility(View.GONE);
        //labelEnoughSpace.setVisibility(View.GONE);
        //setCheckingStatus();

        buttonForceUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,String> args = new HashMap<String, String>();
                args.put("Action", "forceUpdate");
                MessageBox.showMessage(DatabasePageGroup.this, "Are you sure to start the downloading of database?", MessageBox.MB_YESNO, args);
            }
        });

        buttonShowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("database", "Current update manager status: " + FolioLibraryService.getUpdateManagerStatus());
                if (FolioLibraryService.getUpdateManagerStatus() == FolioLibraryService.UPDATE_MANAGER_IDLE) {
                    mainActivity.setCurrentTabCode("text");
                } else {
                    mainActivity.setCurrentTabCode("content");
                }
            }
        });

        buttonLocateDatabaseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (FolioLibraryService.getUpdateManagerStatus() == FolioLibraryService.UPDATE_MANAGER_IDLE) {
                    FolioLibraryService service = FolioLibraryService.getInstance();
                    service.downloadDatabaseFile();
                }*/
                onCreateFileDialog();
            }
        });

        buttonShowDownloads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolioLibraryService.ShowDownloadsListActivity();
            }
        });
    }

    public void ViewDidAppear()
    {
        buttonShowText.setVisibility(FolioLibraryService.getInstance().databaseFileExists() ? View.VISIBLE : View.GONE );
    }

    protected void onCreateFileDialog() {

            Intent intent = new Intent(mainActivity, FileOpenActivity.class);
            mainActivity.startActivityForResult(intent, 123);

        /*Intent intent = new Intent()
                .setType("* / *")
                .setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        mainActivity.startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);*/
    }

    public boolean isVisible() {
        return parentView.getVisibility() == View.VISIBLE;
    }

    public void setIdleStatus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                FolioLibraryService service = FolioLibraryService.getInstance();

                if (service.isNetworkAvailable()) {
                    if (service.databaseFileExists()) {
                        labelTitle.setText("Database OK");
                        labelMessage.setText("Database is present and working.");
                    } else {
                        labelTitle.setText("No database");
                        labelMessage.setText("Database file does not exists. Download will be initiated.");
                    }
                    buttonForceUpdate.setVisibility(View.VISIBLE);
                } else {
                    if (service.databaseFileExists()) {
                        labelTitle.setText("Database OK");
                        labelMessage.setText("Internet connection is not available.");
                    } else {
                        labelTitle.setText("No database, no connection");
                        labelMessage.setText("Database file does not exists and connection is not active. Download will be initiated after connecting to the Internet.");
                    }
                    buttonForceUpdate.setVisibility(View.GONE);
                }
                /*if (downloadPanel.getVisibility() != View.GONE) {
                    downloadPanel.setVisibility(View.GONE);
                }
                labelLowSpace.setVisibility(View.GONE);
                labelEnoughSpace.setVisibility(View.GONE);*/
                buttonLocateDatabaseFile.setVisibility(View.GONE);

            }
        });
    }

    public void setDownloadStatusProgress(final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                /*if (downloadPanel.getVisibility() != View.VISIBLE) {
                    downloadPanel.setVisibility(View.VISIBLE);
                }*/

                labelTitle.setText("Downloading " + FolioLibraryService.getInstance().getNewVersionTitle());
                labelMessage.setText("Downloading of new database is in the progress.");
                //labelDownloadProgress.setText(String.format(Locale.getDefault(), "%d %%", progress));
                //progressBarDownload.setProgress(progress);
                if (buttonForceUpdate.getVisibility() != View.GONE) {
                    buttonForceUpdate.setVisibility(View.GONE);
                }
                if (buttonLocateDatabaseFile.getVisibility() != View.GONE) {
                    buttonLocateDatabaseFile.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setDownloadError(final String errorMessage) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                labelMessage.setText(errorMessage);
            }
        });
    }

    public void setCheckingStatus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                labelTitle.setText("Checking...");
                //labelMessage.setText("Current database is checked for relevance.");
                /*if (downloadPanel.getVisibility() != View.GONE) {
                    downloadPanel.setVisibility(View.GONE);
                }*/
                if (buttonForceUpdate.getVisibility() != View.GONE) {
                    buttonForceUpdate.setVisibility(View.GONE);
                }
                //labelLowSpace.setVisibility(View.GONE);
                //labelEnoughSpace.setVisibility(View.GONE);
                buttonLocateDatabaseFile.setVisibility(View.GONE);
            }
        });
    }

    public void setUpdatingStatus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                labelTitle.setText("Updating...");
                //labelMessage.setText("Current database is being initialized.");
                /*if (downloadPanel.getVisibility() != View.GONE) {
                    downloadPanel.setVisibility(View.GONE);
                }*/
                if (buttonForceUpdate.getVisibility() != View.GONE) {
                    buttonForceUpdate.setVisibility(View.GONE);
                }
                //labelLowSpace.setVisibility(View.GONE);
                //labelEnoughSpace.setVisibility(View.GONE);
                buttonLocateDatabaseFile.setVisibility(View.GONE);
            }
        });
    }

    public void setErrorStatus(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                labelTitle.setText("Warning");
                labelMessage.setText(" ");
                /*downloadPanel.setVisibility(View.GONE);
                labelEnoughSpace.setVisibility(View.GONE);
                labelLowSpace.setVisibility(View.VISIBLE);
                labelLowSpace.setText(message);*/
                buttonLocateDatabaseFile.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void messageBoxAnswerOK(HashMap<String, String> mb) {
        if (mb.containsKey("Action")) {
            if (mb.get("Action").equals("forceUpdate")) {
                FolioLibraryService service = FolioLibraryService.getInstance();
                service.Close();
                mainActivity.SetFolio(null);
                //labelTitle.setText("Forced Update");
                //labelMessage.setText("Database is going to be downloaded.");
                service.forceUpdateStart();
                UpdateDownloadingTask(2);
            }
        }

    }

    @Override
    public void messageBoxAnswerCancel(HashMap<String, String> mb) {

    }

    public View getParentView() {
        return parentView;
    }

    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    public void showLowSpaceMessage(final long missingSpace) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showLowSpace(missingSpace);
            }
        });
    }

    private void showLowSpace(long spaceMb) {
        /*if (spaceMb > 0) {
            labelLowSpace.setVisibility(View.GONE);
            labelEnoughSpace.setVisibility(View.VISIBLE);
            labelEnoughSpace.setText(String.format(Locale.getDefault(),
                    "We have enough space to store the database (%s MB free)",
                    Long.toString(spaceMb/1024/1024)));
        } else {
            labelEnoughSpace.setVisibility(View.GONE);
            labelLowSpace.setVisibility(View.VISIBLE);
            labelLowSpace.setText(String.format(Locale.getDefault(),
                    "There are missing %s MBytes of space for complete storage of database. Free some space on your external storage to prevent failure.",
                    Long.toString(-spaceMb/1024/1024)));
        }*/
    }

    void UpdateDownloadingTask(final int numTries) {

        boolean scheduleNext = false;
        int visibility = View.GONE;
        VBDownloadedFileInfo vbi = VBDownloader.CurrentFileInfo(mainActivity);
        if (vbi == null) {
            if (numTries > 0) {
                scheduleNext = true;
            }
        } else {
            downloadingTitle.setText(vbi.StatusText);
            int progress = (int) ((double) vbi.DownloadedSize / (double) vbi.Size * 100);
            downloadingProgress.setProgress(progress);
            downloadingDetails.setText(String.format("%d %%", progress));

            if (numTries > 0 || vbi.Status == DownloadManager.STATUS_RUNNING
                    || vbi.Status == DownloadManager.STATUS_PENDING) {
                scheduleNext = true;
            }
        }

        if (scheduleNext) {
            visibility = View.VISIBLE;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateDownloadingTask(numTries - 1);
                        }
                    });
                }
            }, 2000);
        }

        downloadingPanel.setVisibility(visibility);
    }

}
