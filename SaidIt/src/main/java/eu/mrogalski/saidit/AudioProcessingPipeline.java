package eu.mrogalski.saidit;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import eu.mrogalski.saidit.analysis.SegmentationController;
import eu.mrogalski.saidit.analysis.SimpleSegmentationController;
import eu.mrogalski.saidit.ml.AudioEventClassifier;
import eu.mrogalski.saidit.ml.TfLiteClassifier;
import eu.mrogalski.saidit.storage.AudioTag;
import eu.mrogalski.saidit.storage.RecordingStoreManager;
import eu.mrogalski.saidit.storage.SimpleRecordingStoreManager;
import eu.mrogalski.saidit.vad.EnergyVad;
import eu.mrogalski.saidit.vad.Vad;

public class AudioProcessingPipeline {
    private static final String TAG = "AudioProcessingPipeline";

    private final Context mContext;
    private final int mSampleRate;

    private Vad vad;
    private SegmentationController segmentationController;
    private RecordingStoreManager recordingStoreManager;
    private TfLiteClassifier audioClassifier;

    public AudioProcessingPipeline(Context context, int sampleRate) {
        mContext = context;
        mSampleRate = sampleRate;
    }

    public void start() {
        vad = new EnergyVad();
        vad.init(mSampleRate);
        vad.setMode(2); // TODO: Make VAD sensitivity configurable

        recordingStoreManager = new SimpleRecordingStoreManager(mContext, mSampleRate);
        segmentationController = new SimpleSegmentationController(mSampleRate, 16);
        segmentationController.setListener(new SegmentationController.SegmentListener() {
            @Override
            public void onSegmentStart(long timestamp) {
                try {
                    if (recordingStoreManager != null) {
                        recordingStoreManager.onSegmentStart(timestamp);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to start new segment", e);
                }
            }

            @Override
            public void onSegmentEnd(long timestamp) {
                if (recordingStoreManager != null) {
                    recordingStoreManager.onSegmentEnd(timestamp);
                }
            }

            @Override
            public void onSegmentData(byte[] data, int offset, int length) {
                if (recordingStoreManager != null) {
                    recordingStoreManager.onSegmentData(data, offset, length);
                }
            }
        });

        audioClassifier = new AudioEventClassifier();
        try {
            audioClassifier.load(mContext, "yamnet_tiny.tfile", "yamnet_tiny_labels.txt");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load audio classifier model.", e);
        }
    }

    public void process(byte[] audioData, int offset, int length) {
        boolean isSpeech = vad.process(audioData, offset, length);
        segmentationController.process(audioData, offset, length, isSpeech);

        if (audioClassifier != null) {
            short[] shortArray = new short[length / 2];
            ByteBuffer.wrap(audioData, offset, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray);
            List<TfLiteClassifier.Recognition> results = audioClassifier.recognize(shortArray);
            for (TfLiteClassifier.Recognition result : results) {
                if (result.getConfidence() > 0.3) {
                    Log.d(TAG, "Audio event: " + result.getTitle() + " (" + result.getConfidence() + ")");
                    recordingStoreManager.onTag(new AudioTag(result.getTitle(), result.getConfidence(), System.currentTimeMillis()));
                }
            }
        }
    }

    public void stop() {
        if (audioClassifier != null) {
            audioClassifier.close();
        }
        if (recordingStoreManager != null) {
            recordingStoreManager.close();
        }
        if (segmentationController != null) {
            segmentationController.close();
        }
        if (vad != null) {
            vad.close();
        }
    }

    public RecordingStoreManager getRecordingStoreManager() {
        return recordingStoreManager;
    }
}
