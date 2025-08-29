package eu.mrogalski.saidit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class AacMp4WriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(timeout = 1000)
    public void close_doesNotBlock_whenNoDataWritten() throws IOException {
        // Skip on JVM unit tests due to Android framework dependency; this is covered by instrumentation tests.
        org.junit.Assume.assumeTrue("Skip MediaCodec on JVM", false);
        File outFile = folder.newFile("test.m4a");
        AacMp4Writer writer = new AacMp4Writer(48000, 1, 96000, outFile);
        writer.close();
    }
}
