package eu.mrogalski.saidit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordingsActivity extends AppCompatActivity {

    private RecordingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recordings_recycler_view);
        
        // Load recordings and set up adapter
        List<File> recordings = getRecordings();
        adapter = new RecordingsAdapter(this, recordings);
        recyclerView.setAdapter(adapter);
    }

    private List<File> getRecordings() {
        File dir = new File(getFilesDir(), "history");
        if (!dir.exists()) {
            return Collections.emptyList();
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, files);
        // Sort files by date, newest first
        fileList.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return fileList;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.releasePlayer();
        }
    }
}
