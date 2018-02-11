package org.koreader.send2ebook.android.share;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.TextView;

import com.github.mwoz123.send2ebook.creator.Creator;
import com.github.mwoz123.send2ebook.creator.EpubCreator;
import com.github.mwoz123.send2ebook.input.InputProcessor;
import com.github.mwoz123.send2ebook.input.UrlInputProcessor;
import com.github.mwoz123.send2ebook.model.Ebook;
import com.github.mwoz123.send2ebook.model.EbookData;
import com.github.mwoz123.send2ebook.model.epub.EpubEbook;
import com.github.mwoz123.send2ebook.storage.Storage;
import com.github.mwoz123.send2ebook.storage.ftp.FtpConnection;
import com.github.mwoz123.send2ebook.storage.ftp.FtpStorage;

import org.koreader.send2ebook.android.util.FtpConnectionFromProperty;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import koreader.org.send2ebook.android.R;


public class ShareVia extends AsyncTask<IntentAndContext, Void, Void> {
    private final static Logger LOGGER = Logger.getLogger(ShareVia.class.toString());

    private InputProcessor<String> inputProcessor = new UrlInputProcessor();
    private Creator<EpubEbook> creator = new EpubCreator();

    private Storage storage ;
    private Activity mActivity;

    public ShareVia(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    protected Void doInBackground(IntentAndContext... intentAndContext) {

        Intent intent = intentAndContext[0].getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (sharedText != null) {

                try {
                    showMessage("Starting download and clean up document");

                    EbookData ebookData = inputProcessor.transformInput(sharedText);

                    showMessage("Creating Epub");
                    Ebook ebook = creator.createOutputEbook(ebookData);

                    showMessage("Connecting to storage server");

                    FtpConnection connection = FtpConnectionFromProperty.getConnection(intentAndContext[0].getContext());
                    storage = FtpStorage.getInstance();
                    storage.connect(connection);

                    showMessage("Saving file to server");
                    storage.storeFile(ebook);

                    showMessage("Succesfully finished");


                } catch (IOException e) {
                    LOGGER.log(Level.ALL, "IO Exception occured", e);
                    showMessage("exception occured : " + e.getMessage() );
                } finally {
                    if (storage != null) {
                        storage.disconnect();
                    }
                }
            }
        }
        return null;
    }

    private void showMessage(final String message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView centralTextView = (TextView) mActivity.findViewById(R.id.main_text);
                centralTextView.setText(message);
            }
        });
    }


}