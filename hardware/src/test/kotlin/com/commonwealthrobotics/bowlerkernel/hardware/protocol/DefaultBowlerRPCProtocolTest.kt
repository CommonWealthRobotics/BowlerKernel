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
package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)

    @ParameterizedTest
    @MethodSource("isGreaterThanUnsignedByteSource")
    fun `test isGreaterThanUnsignedByte`(data: Pair<Int, Boolean>) {
        assertEquals(
            data.second,
            DefaultBowlerRPCProtocol.isGreaterThanUnsignedByte(data.first)
        )
    }

    @Test
    fun `test starting packet id less than zero`() {
        assertThrows<IllegalArgumentException> {
            DefaultBowlerRPCProtocol(
                server = server,
                startPacketId = -1
            )
        }
    }

    @Test
    fun `test starting packet id equal to discovery packet id`() {
        assertThrows<IllegalArgumentException> {
            DefaultBowlerRPCProtocol(
                server = server,
                startPacketId = DefaultBowlerRPCProtocol.DISCOVERY_PACKET_ID
            )
        }
    }

    @Test
    fun `test discard success before disconnect`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_COMPLETE)
            )
        )

        protocol.disconnect()

        assertAll(
            server.writes.map {
                {
                    val expected = getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(4))
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
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GENERIC)
            )
        )

        shouldThrow<IllegalStateException> {
            protocol.disconnect()
        }
    }

    @Test
    fun `test total discard failure`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GENERIC)
            )
        )

        shouldThrow<IllegalStateException> {
            protocol.disconnect()
        }
    }

    @Test
    fun `allowed to disconnect with no connection`() {
        protocol.disconnect()
    }

    /**
     * Connects the protocol and asserts it connected properly because no error was returned.
     */
    private fun connectProtocol() {
        protocol.connect()
    }

    /**
     * Asserts that no interactions have happened with the device (nothing written).
     */
    private fun assertNoInteractionsWithDevice() {
        server.writes.shouldBeEmpty()
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun isGreaterThanUnsignedByteSource() = listOf(
            254 to false,
            255 to false,
            256 to true
        )
    }
}
