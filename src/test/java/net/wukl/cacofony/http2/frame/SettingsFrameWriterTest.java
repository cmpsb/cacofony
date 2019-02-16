package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.settings.Setting;
import net.wukl.cacofony.http2.settings.SettingIdentifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class SettingsFrameWriterTest {
    private SecureRandom random;
    private FrameWriter writer;

    @BeforeEach
    public void before() {
        this.random = Mockito.mock(SecureRandom.class);
        this.writer = new FrameWriter(this.random);
    }

    @AfterEach
    public void after() {
        Mockito.verify(this.random, Mockito.never()).nextInt(Mockito.anyInt());
    }

    @Test
    public void testToBytesOfEmptySettings() throws IOException {
        final var frame = new SettingsFrame(List.of(), false);
        final var bytes = new ByteArrayOutputStream();
        this.writer.write(frame, bytes);
        assertThat(bytes.toByteArray()).containsExactly(
                0x00, 0x00, 0x00,
                FrameType.SETTINGS.getValue(),
                0b0000_0000,
                0x00, 0x00, 0x00, 0x00
        );
    }

    @Test
    public void testToBytesOfAcknowledgement() throws IOException {
        final var frame = SettingsFrame.ACK;
        final var bytes = new ByteArrayOutputStream();
        this.writer.write(frame, bytes);

        assertThat(bytes.toByteArray()).containsExactly(
                0x00, 0x00, 0x00,
                FrameType.SETTINGS.getValue(),
                0b0000_0001,
                0x00, 0x00, 0x00, 0x00
        );
    }

    @Test
    public void testToBytesOfSingleSetting() throws IOException {
        final var frame = new SettingsFrame(Collections.singletonList(
                new Setting(SettingIdentifier.ENABLE_PUSH, 1)
        ));
        final var bytes = new ByteArrayOutputStream();
        this.writer.write(frame, bytes);

        assertThat(bytes.toByteArray()).containsExactly(
                0x00, 0x00, 0x06,
                FrameType.SETTINGS.getValue(),
                0b0000_0000,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x02,
                0x00, 0x00, 0x00, 0x01
        );
    }

    @Test
    public void testToBytesOfMultipleSettings() throws IOException {
        final var frame = new SettingsFrame(List.of(
                new Setting(SettingIdentifier.HEADER_TABLE_SIZE, 0xf00ff00f),
                new Setting(SettingIdentifier.MAX_CONCURRENT_STREAMS, 0x11AAACC6L)
        ));

        final var bytes = new ByteArrayOutputStream();
        this.writer.write(frame, bytes);

        assertThat(frame.getPayloadLength()).isEqualTo(12);
        assertThat(bytes.toByteArray()).containsExactly(
                0x00, 0x00, 0x0C,
                FrameType.SETTINGS.getValue(),
                0b0000_0000,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x01,
                0xf0, 0x0f, 0xf0, 0x0f,
                0x00, 0x03,
                0x11, 0xAA, 0xAC, 0xC6
        );
    }
}
