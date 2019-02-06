package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.settings.Setting;
import net.wukl.cacofony.http2.settings.SettingIdentifier;
import org.junit.jupiter.api.Test;

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
    public void testToBytesOfEmptySettings() {
        final var frame = new SettingsFrame(List.of(), false);
        assertThat(frame.payloadToBytes()).isEmpty();
    }

    @Test
    public void testToBytesOfAcknowledgement() {
        final var frame = SettingsFrame.ACK;
        assertThat(frame.payloadToBytes()).isEmpty();
    }

    @Test
    public void testAcknowledgementCannotContainSettings() {
        assertThrows(IllegalArgumentException.class, () -> new SettingsFrame(
                Collections.singletonList(new Setting(SettingIdentifier.MAX_FRAME_SIZE, 65536)),
                true
        ));
    }

    @Test
    public void testToBytesOfSingleSetting() {
        final var frame = new SettingsFrame(Collections.singletonList(
            new Setting(SettingIdentifier.ENABLE_PUSH, 1)
        ));
        final var bytes = frame.payloadToBytes();

        assertThat(bytes).containsExactly(
                0x00, 0x02,
                0x00, 0x00, 0x00, 0x01
        );
    }

    @Test
    public void testToBytesOfMultipleSettings() {
        final var frame = new SettingsFrame(List.of(
            new Setting(SettingIdentifier.HEADER_TABLE_SIZE, 0xf00ff00f),
            new Setting(SettingIdentifier.MAX_CONCURRENT_STREAMS, 0x11AAACC6L)
        ));

        final var bytes = frame.payloadToBytes();

        assertThat(bytes).containsExactly(
            0x00, 0x01,
            0xf0, 0x0f, 0xf0, 0x0f,
            0x00, 0x03,
            0x11, 0xAA, 0xAC, 0xC6
        );
    }
}
