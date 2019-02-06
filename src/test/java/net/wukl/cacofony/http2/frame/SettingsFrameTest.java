package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.settings.Setting;
import net.wukl.cacofony.http2.settings.SettingIdentifier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettingsFrameTest {
    @Test
    public void testTypeIsSettings() {
        final var frame = new SettingsFrame(Collections.emptyList());
        assertThat(frame.getType()).isEqualTo(FrameType.SETTINGS);
    }

    @Test
    public void testStreamIdIsZero() {
        final var frame = new SettingsFrame(true);
        assertThat(frame.getStreamId()).isZero();
    }

    @Test
    public void testCommonAckIsAcknowledgement() {
        assertThat(SettingsFrame.ACK.getFlags()).containsExactly(FrameFlag.ACK);
    }

    @Test
    public void testEmptySettingsIsNotAckByDefault() {
        final var frame = new SettingsFrame(Collections.emptyList());
        assertThat(frame.getFlags()).doesNotContain(FrameFlag.ACK);
    }

    @Test
    public void testToBytesOfEmptySettings() throws IOException {
        final var frame = new SettingsFrame(List.of(), false);
        final var bytes = new ByteArrayOutputStream();
        frame.writePayload(bytes);
        assertThat(frame.getLength()).isZero();
        assertThat(bytes.size()).isZero();
    }

    @Test
    public void testToBytesOfAcknowledgement() throws IOException {
        final var frame = SettingsFrame.ACK;
        final var bytes = new ByteArrayOutputStream();
        frame.writePayload(bytes);
        assertThat(frame.getLength()).isZero();
        assertThat(bytes.size()).isZero();
    }

    @Test
    public void testAcknowledgementCannotContainSettings() {
        assertThrows(IllegalArgumentException.class, () -> new SettingsFrame(
                Collections.singletonList(new Setting(SettingIdentifier.MAX_FRAME_SIZE, 65536)),
                true
        ));
    }

    @Test
    public void testToBytesOfSingleSetting() throws IOException {
        final var frame = new SettingsFrame(Collections.singletonList(
            new Setting(SettingIdentifier.ENABLE_PUSH, 1)
        ));
        final var bytes = new ByteArrayOutputStream();
        frame.writePayload(bytes);

        assertThat(frame.getLength()).isEqualTo(6);
        assertThat(bytes.toByteArray()).containsExactly(
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
        frame.writePayload(bytes);

        assertThat(frame.getLength()).isEqualTo(12);
        assertThat(bytes.toByteArray()).containsExactly(
            0x00, 0x01,
            0xf0, 0x0f, 0xf0, 0x0f,
            0x00, 0x03,
            0x11, 0xAA, 0xAC, 0xC6
        );
    }
}
