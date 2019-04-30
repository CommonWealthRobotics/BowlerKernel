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
package com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier

import arrow.core.extensions.`try`.monadThrow.bindingCatch
import arrow.core.getOrElse
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.gitfs.GitHubFS
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import org.kohsuke.github.GitHub
import org.octogonapus.ktguava.collections.toImmutableMap
import org.octogonapus.ktguava.collections.toImmutableSet
import java.io.FileReader
import kotlin.reflect.KClass

fun main() {
    GitVitaminSupplier(
        GitHubFS(
            GitHub.connectAnonymously(),
            "" to ""
        ),
        GitFile(
            "https://github.com/Octogonapus/test-gitvitamins.git",
            "testVitamins.json"
        ),
        DefaultKlaxonVitamin::class
    )
}

@Target(AnnotationTarget.FIELD)
annotation class ConvertImmutableMap

class GitVitaminSupplier(
    private val gitFS: GitFS,
    private val vitaminsGitFile: GitFile,
    private val vitaminType: KClass<out KlaxonVitamin>
) : VitaminSupplier {

    private val klaxon = Klaxon()

    override val name: String
    override val allVitamins: ImmutableSet<Vitamin>

    init {
        val immutableMapConverter = object : Converter {
            override fun canConvert(cls: Class<*>) = cls == ImmutableMap::class.java

            override fun fromJson(jv: JsonValue) =
                (klaxon.parseFromJsonObject<Map<*, *>>(jv.obj!!)!!).toImmutableMap()

            override fun toJson(value: Any) =
                klaxon.toJsonString(value as Map<*, *>)
        }

        klaxon.fieldConverter(ConvertImmutableMap::class, immutableMapConverter)

        val allVitaminsFromGit = bindingCatch {
            val (allFiles) = gitFS.cloneRepoAndGetFiles(vitaminsGitFile.gitUrl)

            val vitaminsFile = allFiles.first { it.name == vitaminsGitFile.filename }

            val gitVitamins =
                klaxon.parse<GitVitaminSupplierData>(vitaminsFile) ?: throw IllegalStateException(
                    """
                    |Could not parse GitVitaminSupplierData from file:
                    |$vitaminsFile
                    """.trimMargin()
                )

            // Search independent of the repo's actual position on disk by only looking for a
            // matching suffix
            val allVitaminFiles = gitVitamins.files.map { vitaminFilePath ->
                allFiles.first { it.path.endsWith(vitaminFilePath) }
            }

            gitVitamins.name to allVitaminFiles.map {
                // We have to parse this "by hand" because variables can't be used as reified
                // type parameters
                val jsonObject = klaxon.parser(vitaminType).parse(FileReader(it)) as JsonObject
                val parsedObject = klaxon.fromJsonObject(jsonObject, vitaminType.java, vitaminType)
                (parsedObject as KlaxonVitamin?)?.vitamin ?: throw IllegalStateException(
                    """
                    |Could not parse DefaultKlaxonVitamin from file:
                    |$it
                    """.trimMargin()
                )
            }
        }

        val (nameFromGit, vitaminsFromGit) = allVitaminsFromGit.getOrElse {
            throw IllegalStateException(
                """
                |Unable to load vitamins from git file:
                |$vitaminsGitFile
                """.trimMargin(),
                it
            )
        }

        name = nameFromGit
        allVitamins = vitaminsFromGit.toImmutableSet()
    }

    override fun partNumberFor(vitamin: Vitamin): String {
        TODO("not implemented")
    }

    override fun priceFor(vitamin: Vitamin, count: Int): Double {
        TODO("not implemented")
    }
}
