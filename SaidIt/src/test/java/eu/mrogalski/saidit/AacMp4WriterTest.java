package eu.mrogalski.saidit;

import org.junit.Ignore;

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

    @Ignore("This test requires an Android device and fails in a unit test environment.")
    @Test(timeout = 1000)
    public void close_doesNotBlock_whenNoDataWritten() throws IOException {
        File outFile = folder.newFile("test.m4a");
        AacMp4Writer writer = new AacMp4Writer(48000, 1, 96000, outFile);
        writer.close();
    }
}
