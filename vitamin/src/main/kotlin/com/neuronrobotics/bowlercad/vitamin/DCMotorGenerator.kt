/*
 * This file is part of bowler-cad.
 *
 * bowler-cad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-cad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-cad.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlercad.vitamin

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Bolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.millimeter
import kotlin.math.truncate

class DCMotorGenerator(
    shaftGenerator: VitaminCadGenerator<Shaft>,
    private val boltGenerator: VitaminCadGenerator<Bolt>,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<DCMotor> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<DCMotor, CSG> {
            it!!

            val case = Cylinder(
                it.diameter.millimeter / 2,
                it.height.millimeter
            ).toCSG().toZMax()

            val shaftSupport = Cylinder(
                it.shaftSupportDiameter.millimeter / 2,
                it.shaftSupportHeight.millimeter
            ).toCSG().toZMax()

            val shaft = shaftGenerator.generateCAD(it.shaft).toZMin()

            val startingBolt = boltGenerator.generateCAD(it.bolt).toZMax()
                .movex(it.boltCircleDiameter.millimeter / 2)
                .rotz(it.boltCircleAngleOffset.degree)

            val bolts = getBolts(it, startingBolt).toZMax()

            case.difference(bolts)
                .movez(-shaftSupport.totalZ)
                .union(shaftSupport)
                .union(shaft)
        })

    private fun getBolts(motor: DCMotor, bolt: CSG): CSG {
        val numberOfBolts = truncate(360 / motor.boltCircleAngleIncrement.degree).toInt()
        val allBolts = (1..numberOfBolts).map { i ->
            bolt.rotz(motor.boltCircleAngleIncrement.degree * i)
        }

        return CSG.unionAll(allBolts).toZMax()
    }

    override fun generateCAD(vitamin: DCMotor): CSG = cache[vitamin]
}
