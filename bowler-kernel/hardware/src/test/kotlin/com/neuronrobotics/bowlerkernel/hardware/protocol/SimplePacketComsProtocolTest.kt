/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:SuppressWarnings("LargeClass", "TooManyFunctions", "LongMethod", "LongMethod")

package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.effects.IO
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.collections.immutableSetOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolTest {

    private val server = MockDeviceServer()

    private val protocol = SimplePacketComsProtocol(
        server = server,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    @ParameterizedTest
    @MethodSource("resourceTypesAreValidatedSource")
    fun `test resource types are validated in add operations`(
        operation: SimplePacketComsProtocol.() -> IO<Unit>
    ) {
        assertOperationFailedAndNoInteractionsWithDevice { protocol.operation() }
    }

    @ParameterizedTest
    @MethodSource("isGreaterThanUnsignedByteSource")
    fun `test isGreaterThanUnsignedByte`(data: Pair<Int, Boolean>) {
        assertEquals(
            data.second,
            SimplePacketComsProtocol.isGreaterThanUnsignedByte(data.first)
        )
    }

    @Test
    fun `test starting packet id less than zero`() {
        assertThrows<IllegalArgumentException> {
            SimplePacketComsProtocol(
                server = server,
                startPacketId = -1,
                resourceIdValidator = DefaultResourceIdValidator()
            )
        }
    }

    @Test
    fun `test starting packet id equal to discovery packet id`() {
        assertThrows<IllegalArgumentException> {
            SimplePacketComsProtocol(
                server = server,
                startPacketId = SimplePacketComsProtocol.DISCOVERY_PACKET_ID,
                resourceIdValidator = DefaultResourceIdValidator()
            )
        }
    }

    @Test
    fun `test discard success before disconnect`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_DISCARD_COMPLETE)
            )
        )

        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isRight())

        assertAll(
            server.writes.map {
                {
                    val expected = getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(4))
                    assertArrayEquals(
                        expected,
                        it.second,
                        """
                        |The sent payload:
                        |${it.second.joinToString()}
                        |should equal the expected payload:
                        |${expected.joinToString()}
                        """.trimMargin()
                    )
                }
            }
        )
    }

    @Test
    fun `test discard failure before disconnect`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
            )
        )

        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isLeft())
    }

    @Test
    fun `test total discard failure`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
            )
        )

        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isLeft())
    }

    @Test
    fun `allowed to disconnect with no connection`() {
        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isRight())
    }

    /**
     * Connects the protocol and asserts it connected properly because no error was returned.
     */
    private fun connectProtocol() {
        assertTrue(protocol.connect().attempt().unsafeRunSync().isRight())
    }

    /**
     * Connects the protocol, runs the [operation], and asserts that:
     * 1. The operation failed because an error was returned
     * 2. No interactions happened with the device (nothing was written or read)
     *
     * @param operation The operation to perform.
     */
    private fun assertOperationFailedAndNoInteractionsWithDevice(
        operation: () -> IO<Unit>
    ) {
        connectProtocol()

        val result = operation().attempt().unsafeRunSync()

        assertAll(
            { assertTrue(result.isLeft()) },
            { assertNoInteractionsWithDevice() }
        )
    }

    /**
     * Asserts that no interactions have happened with the device (nothing written).
     */
    private fun assertNoInteractionsWithDevice() {
        assertThat(server.writes, isEmpty)
    }

    companion object {

        private fun getWritable() = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(Random.nextInt().toByte())
        )

        private fun getReadable() = ResourceId(
            DefaultResourceTypes.AnalogIn,
            DefaultAttachmentPoints.Pin(Random.nextInt().toByte())
        )

        @Suppress("unused")
        @JvmStatic
        fun isGreaterThanUnsignedByteSource() = listOf(
            254 to false,
            255 to false,
            256 to true
        )

        @Suppress("unused")
        @JvmStatic
        fun resourceTypesAreValidatedSource() =
            listOf<SimplePacketComsProtocol.() -> IO<Unit>>(
                { addRead(getWritable()) },
                { addReadGroup(immutableSetOf(getWritable(), getWritable())) },
                { addWrite(getReadable()) },
                { addWriteGroup(immutableSetOf(getReadable(), getReadable())) }
            )
    }
}
