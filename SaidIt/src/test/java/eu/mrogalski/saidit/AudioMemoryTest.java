package eu.mrogalski.saidit;

import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;

public class AudioMemoryTest {

    private AudioMemory audioMemory;
    private final int CHUNK_SIZE = AudioMemory.CHUNK_SIZE;

    @Before
    public void setUp() {
        audioMemory = new AudioMemory();
        // Allocate a smaller buffer for easier testing
        audioMemory.allocate(CHUNK_SIZE / 10);
    }

    private static class TestConsumer implements AudioMemory.Consumer {
        private final byte[] dataToProvide;
        private int readOffset = 0;

        TestConsumer(byte[] data) {
            this.dataToProvide = data;
        }

        @Override
        public int consume(byte[] array, int offset, int count) {
            int toRead = Math.min(count, dataToProvide.length - readOffset);
            if (toRead > 0) {
                System.arraycopy(dataToProvide, readOffset, array, offset, toRead);
                readOffset += toRead;
            }
            return toRead;
        }
    }
    
    private static class CapturingConsumer implements AudioMemory.Consumer {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        @Override
        public int consume(byte[] array, int offset, int count) throws IOException {
            baos.write(array, offset, count);
            return count;
        }

        byte[] getCapturedData() {
            return baos.toByteArray();
        }
    }


    @Test
    public void testWriteNoWrap() throws IOException {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;

        audioMemory.fill(new TestConsumer(data));

        AudioMemory.Stats stats = audioMemory.getStats(0);
        assertEquals(100, stats.filled);
        assertFalse(stats.overwriting);
    }

    @Test
    public void testWriteWithWrap() throws IOException {
        int bufferSize = (int) audioMemory.getAllocatedMemorySize();
        byte[] data = new byte[bufferSize + 50];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        
        audioMemory.fill(new TestConsumer(data));

        AudioMemory.Stats stats = audioMemory.getStats(0);
        assertEquals(bufferSize, stats.filled);
        assertTrue(stats.overwriting);
    }

    @Test
    public void testDumpNoWrap() throws IOException {
        byte[] data = new byte[200];
        for (int i = 0; i < 200; i++) {
            data[i] = (byte) i;
        }
        audioMemory.fill(new TestConsumer(data));

        CapturingConsumer consumer = new CapturingConsumer();
        audioMemory.dump(consumer, 100);

        byte[] dumped = consumer.getCapturedData();
        assertEquals(100, dumped.length);
        for (int i = 0; i < 100; i++) {
            assertEquals((byte) (i + 100), dumped[i]);
        }
    }

    @Test
    public void testDumpWithWrap() throws IOException {
        int bufferSize = (int) audioMemory.getAllocatedMemorySize();
        byte[] data = new byte[bufferSize + 50];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        // This will wrap around
        audioMemory.fill(new TestConsumer(data));
        
        CapturingConsumer consumer = new CapturingConsumer();
        audioMemory.dump(consumer, 100);
        
        byte[] dumped = consumer.getCapturedData();
        assertEquals(100, dumped.length);

        // Check if the dumped data is the last 100 bytes of the input data
        byte[] expected = new byte[100];
        System.arraycopy(data, data.length - 100, expected, 0, 100);
        assertArrayEquals(expected, dumped);
    }

    @Test
    public void testDumpFullBuffer() throws IOException {
        int bufferSize = (int) audioMemory.getAllocatedMemorySize();
        byte[] data = new byte[bufferSize];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;

        audioMemory.fill(new TestConsumer(data));

        CapturingConsumer consumer = new CapturingConsumer();
        audioMemory.dump(consumer, bufferSize);
        
        byte[] dumped = consumer.getCapturedData();
        assertEquals(bufferSize, dumped.length);
        assertArrayEquals(data, dumped);
    }
    
    @Test
    public void testDumpZeroBytes() throws IOException {
        byte[] data = new byte[100];
        audioMemory.fill(new TestConsumer(data));

        CapturingConsumer consumer = new CapturingConsumer();
        audioMemory.dump(consumer, 0);

        assertEquals(0, consumer.getCapturedData().length);
    }
    
    @Test
    public void testStats() throws IOException {
        audioMemory.allocate(1000);
        AudioMemory.Stats stats = audioMemory.getStats(0);
        assertEquals(0, stats.filled);
        assertEquals(1000, stats.total);
        assertFalse(stats.overwriting);

        audioMemory.fill(new TestConsumer(new byte[500]));
        stats = audioMemory.getStats(0);
        assertEquals(500, stats.filled);
        assertFalse(stats.overwriting);

        audioMemory.fill(new TestConsumer(new byte[600])); // This will overwrite
        stats = audioMemory.getStats(0);
        assertEquals(1000, stats.filled);
        assertTrue(stats.overwriting);
    }
}