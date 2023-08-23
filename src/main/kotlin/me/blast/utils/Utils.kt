package me.blast.utils

import java.io.File
import java.util.*

object Utils {
  val DURATION_REGEX = Regex("^(\\d+(\\.\\d+)?)\\s*(\\w+)$")
  
  fun loadClasses(packageName: String): List<Class<*>> {
    fun findClasses(dir: File, packageName: String): List<Class<*>> {
      val classes = mutableListOf<Class<*>>()
      if (!dir.exists()) {
        return classes
      }
      
      val files = dir.listFiles()
      for (file in files!!) {
        if (file.isDirectory) {
          assert(!file.name.contains("."))
          classes.addAll(findClasses(file, "$packageName.${file.name}"))
        } else if (file.name.endsWith(".class")) {
          classes.add(Class.forName("$packageName.${file.name.substring(0, file.name.length - 6)}"))
        }
      }
      return classes
    }
    
    val classLoader = Thread.currentThread().contextClassLoader
    val path = packageName.replace('.', '/')
    val resources = classLoader.getResources(path)
    val dirs = mutableListOf<File>()
    
    while (resources.hasMoreElements()) {
      val resource = resources.nextElement()
      dirs.add(File(resource.file))
    }
    
    val classes = mutableListOf<Class<*>>()
    for (dir in dirs) {
      classes.addAll(findClasses(dir, packageName))
    }
    
    return classes
  }
  
  fun extractDigits(s: String): String {
    return s.replace("[^0-9]".toRegex(), "")
  }
  
  fun convertCamelToKebab(input: String): String {
    val builder = StringBuilder()
    
    for (char in input) {
      if (char.isUpperCase()) {
        builder.append('-')
        builder.append(char.lowercaseChar())
      } else {
        builder.append(char)
      }
    }
    
    return builder.toString()
  }
  
  fun <T> ListIterator<T>.takeWhileWithIndex(predicate: (index: Int, T) -> Boolean): List<T> {
    val resultList = mutableListOf<T>()
    var currentIndex = 0
    
    while (hasNext()) {
      val item = next()
      if (predicate(currentIndex, item)) {
        resultList.add(item)
        currentIndex++
      } else {
        previous()
        break
      }
    }
    
    return resultList
  }
  
  fun <T> Optional<T>.toNullable(): T? {
    return orElse(null)
  }
}