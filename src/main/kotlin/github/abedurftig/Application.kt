package github.abedurftig

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("github.abedurftig")
		.start()
}

