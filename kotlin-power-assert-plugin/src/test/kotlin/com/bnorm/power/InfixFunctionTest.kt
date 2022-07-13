/*
 * Copyright (C) 2022 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bnorm.power

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.name.FqName
import java.lang.reflect.InvocationTargetException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InfixFunctionTest {
  @Test
  fun `infix functions include receiver`() {
    val actual = execute(
      """
      (1 + 1) mustEqual (2 + 4)
      """.trimIndent()
    )
    assertEquals(
      """
      (1 + 1) mustEqual (2 + 4)
         |                 |
         |                 6
         2
      """.trimIndent(),
      actual.trim()
    )
  }

  private fun execute(mainBody: String): String {
    val file = SourceFile.kotlin(
      name = "main.kt",
      contents = """
infix fun <V> V.mustEqual(expected: V): Unit = assert(this == expected)

fun <V> V.mustEqual(expected: V, message: () -> String): Unit =
	assert(this == expected, message)

fun main() {
  $mainBody
}
""",
      trimIndent = false
    )

    val result = compile(listOf(file), PowerAssertComponentRegistrar(setOf(FqName("mustEqual"))))
    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    val kClazz = result.classLoader.loadClass("MainKt")
    val main = kClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
    try {
      try {
        main.invoke(null)
      } catch (t: InvocationTargetException) {
        throw t.cause!!
      }
      fail("should have thrown assertion")
    } catch (t: Throwable) {
      return t.message ?: ""
    }
  }
}
