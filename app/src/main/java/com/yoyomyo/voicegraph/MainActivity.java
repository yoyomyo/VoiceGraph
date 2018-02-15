package com.yoyomyo.voicegraph;

import android.Manifest;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RECORDER_SAMPLERATE = 11025;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private boolean isRecording = false;
    private Button startRecordingButton;
    private Button stopRecordingButton;
    private static int recordingCount = 1;
    private String recordingFileName;
    private RecyclerView recordingList;
    private RecordingListAdpater recordingListAdapter;
    private RecordingAsyncTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRecordingActions();

        recordingList = findViewById(R.id.recordingList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recordingList.setLayoutManager(llm);
        populateRecordingList();
    }

    private void setRecordingActions() {
        // get permission if needed
        getPermissionIfNecessary();
        // start recording
        startRecordingButton = findViewById(R.id.startRecordingButton);
        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        stopRecordingButton = findViewById(R.id.stopRecordingButton);
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });
        // play the recording
    }

    private void getPermissionIfNecessary() {
        Util.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private File createAudioFile() {
        recordingFileName = String.format(Locale.US, "recording%d.pcm", recordingCount);
        File file = new File(Environment.getExternalStorageDirectory(), recordingFileName);
        recordingCount++;
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.toString());
        }
        return file;
    }

    private void populateRecordingList() {
        if (recordingListAdapter == null) {
            recordingListAdapter = new RecordingListAdpater();
        }
        File dir = Environment.getExternalStorageDirectory();
        recordingListAdapter.audioFiles  = Util.getAudioFiles(dir);
        if (recordingList.getAdapter() == null) {
            // recyclerview creation
            recordingList.setAdapter(recordingListAdapter);
            recordingCount = recordingListAdapter.getItemCount() + 1;
        } else {
            // adapter is already binded
            recordingListAdapter.notifyDataSetChanged();
        }
    }

    private void startRecording() {
        isRecording = true;
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);

        task = new RecordingAsyncTask();
        task.execute();
    }

    private void stopRecording() {
        isRecording = false;
        startRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);
    }

    public class RecordingListAdpater extends RecyclerView.Adapter<RecordingViewHolder> {

        private List<File> audioFiles;
        @Override
        public RecordingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new RecordingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecordingViewHolder holder, int position) {
            holder.bindData(audioFiles.get(position));
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.recording_line_item;
        }
    }

    public class RecordingViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;

        public RecordingViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.recordingName);
        }

        public void bindData(final File file) {
            final PlayingAsyncTask task = new PlayingAsyncTask(file);
            fileName.setText(file.getName());
            fileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // play audio
                    task.execute();
                }
            });
            fileName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Are you sure to delete file dialog
                    AlertDialog dialog = confirmFileDeletion(file);
                    dialog.show();
                    return true;
                }
            });
        }
    }

    public class PlayingAsyncTask extends AsyncTask<Void, Void, Void> {

        File audioFile;

        public PlayingAsyncTask(File file) {
            audioFile = file;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // TODO: show a dialog of waveform recorded
            playAudio(audioFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(LOG_TAG, "finished playing audio");
        }

        

        private void playAudio(File file) {
            int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
            int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

            int audioLength = (int) (file.length() / 2);
            short[] audio = new short[audioLength];

            try{
                InputStream is = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                DataInputStream dis = new DataInputStream(bis);

                int i = 0;
                while(dis.available() > 0) {
                    audio[i] = dis.readShort();
                    i++;
                }

                // Close the input stream
                dis.close();

                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, channelConfig, audioEncoding, audioLength, AudioTrack.MODE_STREAM);
                audioTrack.play();
                audioTrack.write(audio, 0, audioLength);

                // read short in background, put short in AudioTrack, and play

                // display the graph in the foreground
            } catch (Throwable e) {
                Log.e(LOG_TAG, "An error occured during playback", e);
            }
        }
    }

    public class RecordingAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // TODO: show a dialog of waveform recorded
            startRecordingAsync();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // update the recycler view
            populateRecordingList();

        }

        private void startRecordingAsync() {
            File file = createAudioFile();
            try {
                OutputStream os = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);

                int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                        RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
                short[] buffer = new short[bufferSize];
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

                audioRecord.startRecording();
                while(isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                }

                Log.d(LOG_TAG, "Audio is stopped");
                audioRecord.stop();
                dos.close();
            } catch (Throwable e) {
                Log.e(LOG_TAG, "An error occured during recording: " + e.toString());
            }
        }
    }

    private AlertDialog confirmFileDeletion(final File file) {
        final String fileName = file.getName();
        final AlertDialog deleteDialog = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure that you want to delete " + file.getName() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        file.delete();
                        Toast.makeText(MainActivity.this, fileName + " is deleted", Toast.LENGTH_SHORT).show();
                        populateRecordingList();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .create();
        return deleteDialog;
    }

}
